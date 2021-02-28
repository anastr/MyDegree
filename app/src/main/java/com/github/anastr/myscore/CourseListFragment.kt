package com.github.anastr.myscore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.viewmodel.AllCoursesViewModel
import com.github.anastr.myscore.viewmodel.CoursesViewModelData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_course_list.*
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class CourseListFragment : Fragment() {

    private val args: CourseListFragmentArgs by navArgs()

    private val allCoursesViewModel: AllCoursesViewModel by viewModels()

    private val courseAdapter = CourseAdapter(ArrayList())

    private val fab: FloatingActionButton by lazy { requireActivity().findViewById(R.id.fab) }

    private var passDegree = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title =
            String.format(Locale.ENGLISH,
                requireContext().getString(R.string.title_courses),
                args.yearPosition+1,
                args.semester.position+1,
            )

        recycler_view.apply {
            setHasFixedSize(true)
            adapter = courseAdapter
        }

        allCoursesViewModel.setInput(CoursesViewModelData(args.yearId, args.semester))

        allCoursesViewModel.passDegreeLiveData.observe(viewLifecycleOwner) {
            passDegree = it
            courseAdapter.notifyDataSetChanged()
        }
        allCoursesViewModel.courses.toFlowable(this)
            .subscribe {
                if (it.size >= MAX_COURSES)
                    fab.hideFab()
                else
                    fab.showFab()
                val oldList = courseAdapter.coursesList
                courseAdapter.updateData(it)
                val newList = courseAdapter.coursesList
                val diff = DiffUtil.calculateDiff(CoursesDiffUtil(oldList, newList))
                diff.dispatchUpdatesTo(courseAdapter)
                progressBar.visibility = View.GONE
                if (newList.isEmpty())
                    textNoData.visibility = View.VISIBLE
                else
                    textNoData.visibility = View.GONE
            }
            .disposeOnDestroy(this)
    }

    inner class CourseAdapter(private val courses: ArrayList<Course>) :
        RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

        val coursesList get() = courses.toList()

        inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            val countTextView: TextView = itemView.findViewById(R.id.tv_count)
            val courseTextView: TextView = itemView.findViewById(R.id.tv_course_name)
            val theoreticalTextView: TextView = itemView.findViewById(R.id.tv_theoretical)
            val practicalTextView: TextView = itemView.findViewById(R.id.tv_practical)
            val totalTextView: TextView = itemView.findViewById(R.id.tv_total)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
            return CourseViewHolder(view)
        }

        override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
            val courseItem = courses[position]

//            holder.countTextView.text = "${position + 1}"
            holder.courseTextView.text = courseItem.name

            holder.theoreticalTextView.visibility = if (courseItem.hasTheoretical) View.VISIBLE else View.GONE
            holder.theoreticalTextView.text = String.format(
                Locale.ENGLISH,
                getString(R.string.theoretical),
                courseItem.theoreticalScore,
            )
            holder.practicalTextView.visibility = if (courseItem.hasPractical) View.VISIBLE else View.GONE
            holder.practicalTextView.text = String.format(
                Locale.ENGLISH,
                getString(R.string.practical),
                courseItem.practicalScore,
            )
            holder.totalTextView.text = "${courseItem.score}"
            val backColor = if (courseItem.score < passDegree)
                R.color.noneDegree
            else when(courseItem.score) {
                in 90 ..100 -> R.color.veryGoodDegree
                in 80 ..89 -> R.color.goodDegree
                in 70 ..79 -> R.color.lowDegree
                else -> R.color.veryLowDegree
            }
            ViewCompat.setBackgroundTintList(holder.totalTextView, ContextCompat.getColorStateList(requireContext(), backColor))

            holder.itemView.rapidClickListener {
                val action = CourseListFragmentDirections.actionCourseListFragmentToCourseDialog(CourseMode.Edit(courseItem.uid))
                Navigation.findNavController(requireView()).navigate(action)
            }
        }

        fun updateData(newData: List<Course>) {
            courses.clear()
            courses.addAll(newData)
        }

        override fun getItemCount(): Int = courses.size

    }
}