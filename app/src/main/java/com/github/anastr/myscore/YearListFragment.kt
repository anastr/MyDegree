package com.github.anastr.myscore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.anastr.myscore.adapter.YearAdapter
import com.github.anastr.myscore.databinding.FragmentYearListBinding
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.disposeOnDestroy
import com.github.anastr.myscore.util.drag.DragItemTouchHelper
import com.github.anastr.myscore.util.swipe.SwipeItemTouchHelper
import com.github.anastr.myscore.util.toFlowable
import com.github.anastr.myscore.viewmodel.YearViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class YearListFragment : Fragment(), YearAdapter.YearAdapterListener {

    private var _binding: FragmentYearListBinding? = null
    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentYearListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearAdapter = YearAdapter(this)
        val itemTouchHelper = ItemTouchHelper(DragItemTouchHelper(yearAdapter))
        val swipeTouchHelper = ItemTouchHelper(SwipeItemTouchHelper(yearAdapter))
        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            itemTouchHelper.attachToRecyclerView(this)
            swipeTouchHelper.attachToRecyclerView(this)
            setHasFixedSize(true)
            adapter = yearAdapter
        }

        yearViewModel.years.toFlowable(viewLifecycleOwner)
            .subscribe { newList ->
                yearAdapter.updateData(newList)
                binding.progressBar.visibility = View.GONE
                if (newList.isEmpty())
                    binding.textNoData.visibility = View.VISIBLE
                else
                    binding.textNoData.visibility = View.GONE
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

    override fun onClickSemester(yearWithSemester: YearWithSemester, semester: Semester) {
        navigateToDegrees(
            yearPosition = yearWithSemester.order,
            yearId = yearWithSemester.uid,
            semester = semester,
        )
    }

    override fun onYearItemMoved(newList: List<Year>) {
        yearViewModel.updateYears(*newList.toTypedArray())
    }

    override fun onYearItemSwiped(year: Year) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_year_message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                yearViewModel.deleteYear(year.let { Year(uid = it.uid, order = it.order) })
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
