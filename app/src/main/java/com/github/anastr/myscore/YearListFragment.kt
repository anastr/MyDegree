package com.github.anastr.myscore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.util.drag.DragItemTouchHelper
import com.github.anastr.myscore.util.drag.DragTouchHelper
import com.github.anastr.myscore.util.swipe.SwipeItemTouchHelper
import com.github.anastr.myscore.util.swipe.SwipeTouchHelper
import com.github.anastr.myscore.viewmodel.YearViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_year_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class YearListFragment : Fragment() {

    private lateinit var yearAdapter: YearAdapter

    private val yearViewModel: YearViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_year_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearAdapter = YearAdapter(ArrayList())
        val itemTouchHelper = ItemTouchHelper(DragItemTouchHelper(yearAdapter))
        val swipeTouchHelper = ItemTouchHelper(SwipeItemTouchHelper(yearAdapter))
        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            itemTouchHelper.attachToRecyclerView(this)
            swipeTouchHelper.attachToRecyclerView(this)
            setHasFixedSize(true)
            adapter = yearAdapter
        }

        yearViewModel.years.toFlowable(viewLifecycleOwner)
            .subscribe {
                val oldList = yearAdapter.yearsList
                yearAdapter.updateData(it)
                val newList = yearAdapter.yearsList
                val diff = DiffUtil.calculateDiff(YearsDiffUtil(oldList, newList))
                diff.dispatchUpdatesTo(yearAdapter)
                progressBar.visibility = View.GONE
                if (newList.isEmpty())
                    textNoData.visibility = View.VISIBLE
                else
                    textNoData.visibility = View.GONE
            }
            .disposeOnDestroy(viewLifecycleOwner)
    }

    private fun navigateToDegrees(yearPosition: Int, yearId: Long, semester: Semester) {
        val action = YearListFragmentDirections.actionYearPageFragmentToCourseListFragment(
            yearPosition = yearPosition,
            yearId = yearId,
            semester = semester,
        )
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        Navigation.findNavController(requireView()).navigate(action)
    }

    inner class YearAdapter(private val years: ArrayList<YearWithSemester>) :
        RecyclerView.Adapter<YearAdapter.YearViewHolder>(), DragTouchHelper, SwipeTouchHelper {

        val yearsList get() = years.toList()

        inner class YearViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val yearTextView: TextView = itemView.findViewById(R.id.text_year_name)
            private val semester1Button: Button = itemView.findViewById(R.id.button_semester_1)
            private val semester2Button: Button = itemView.findViewById(R.id.button_semester_2)
            val semester1ScoreTextView: TextView = itemView.findViewById(R.id.text_score_semester_1)
            val semester2ScoreTextView: TextView = itemView.findViewById(R.id.text_score_semester_2)
            val scoreTextView: TextView = itemView.findViewById(R.id.text_score)

            init {
                semester1Button.rapidClickListener {
                    navigateToDegrees(
                        yearPosition = adapterPosition,
                        yearId = years[adapterPosition].uid,
                        semester = Semester.FirstSemester,
                    )
                }
                semester2Button.rapidClickListener {
                    navigateToDegrees(
                        yearPosition = adapterPosition,
                        yearId = years[adapterPosition].uid,
                        semester = Semester.SecondSemester,
                    )
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_year, parent, false)
            return YearViewHolder(view)
        }

        override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
            val yearItem = years[position]

            holder.yearTextView.setText(yearsRec[position])

            holder.semester1ScoreTextView.text = yearItem.semester1Score.formattedScore()
            holder.semester2ScoreTextView.text = yearItem.semester2Score.formattedScore()
            holder.scoreTextView.text = yearItem.score.formattedScore()
            holder.scoreTextView.visibility =
                if (yearItem.semester1Score > 0 && yearItem.semester2Score > 0) View.VISIBLE else View.GONE
        }

        fun updateData(newData: List<YearWithSemester>) {
            years.clear()
            years.addAll(newData)
//            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = years.size

        override fun onItemDrag(fromPosition: Int, toPosition: Int) {
            years.swap(fromPosition, toPosition)
            Log.i("adapter", "drag from $fromPosition to $toPosition")
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemMoved() {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    years.forEachIndexed { index, year ->
                        year.order = index
                    }
                    yearViewModel.updateYears(*years.map { Year(it.uid, it.order) }.toTypedArray())
                }
                // to update year name
                notifyItemRangeChanged(0, years.size)
            }
        }

        override fun onItemSwiped(position: Int) {
            MaterialAlertDialogBuilder(context!!)
                .setMessage(R.string.delete_year_message)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.delete) { dialog, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        yearViewModel.deleteYear(years[position].let { Year(uid = it.uid, order = it.order) })
                    }
                    dialog.dismiss()
                }
                .show()
            notifyItemChanged(position)
        }

    }
}