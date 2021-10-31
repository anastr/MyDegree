package com.github.anastr.myscore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewGroupCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import com.github.anastr.myscore.adapter.YearAdapter
import com.github.anastr.myscore.databinding.FragmentYearListBinding
import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.entities.db.YearWithSemester
import com.github.anastr.myscore.util.MAX_YEARS
import com.github.anastr.myscore.util.drag.DragItemTouchHelper
import com.github.anastr.myscore.util.swipe.SwipeItemTouchHelper
import com.github.anastr.myscore.viewmodel.State
import com.github.anastr.myscore.viewmodel.YearViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class YearListFragment : Fragment(), YearAdapter.YearAdapterListener {

    private var _binding: FragmentYearListBinding? = null
    private val binding get() = _binding!!

    private lateinit var yearAdapter: YearAdapter

    private val yearViewModel: YearViewModel by activityViewModels()

    private val mainActivity: MainActivity? get() = (requireActivity() as? MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYearListBinding.inflate(inflater, container, false)
        ViewGroupCompat.setTransitionGroup(binding.root, true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yearAdapter = YearAdapter(this)
        val itemTouchHelper = ItemTouchHelper(DragItemTouchHelper(yearAdapter))
        val swipeTouchHelper = ItemTouchHelper(SwipeItemTouchHelper(yearAdapter))
        binding.recyclerView.apply {
            itemTouchHelper.attachToRecyclerView(this)
            swipeTouchHelper.attachToRecyclerView(this)
            setHasFixedSize(true)
            adapter = yearAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                yearViewModel.yearsFlow.collect { yearsState ->
                    when (yearsState) {
                        is State.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.textMessage.visibility = View.GONE
                            mainActivity?.hideFab()
                        }
                        is State.Error -> {
                            binding.progressBar.visibility = View.GONE
                            mainActivity?.hideFab()
                            binding.textMessage.visibility = View.VISIBLE
                            binding.textMessage.text = yearsState.error.message
                        }
                        is State.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if (yearsState.data.size >= MAX_YEARS)
                                mainActivity?.hideFab()
                            else
                                mainActivity?.showFab()
                            yearAdapter.updateData(yearsState.data)
                            binding.progressBar.visibility = View.GONE
                            if (yearsState.data.isEmpty()) {
                                binding.textMessage.visibility = View.VISIBLE
                                binding.textMessage.setText(R.string.noData)
                            } else {
                                binding.textMessage.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDegrees(yearPosition: Int, yearId: Long, semester: Semester) {
        val action = YearListFragmentDirections.actionYearPageFragmentToCourseListFragment(
            title = String.format(
                Locale.ENGLISH,
                getString(R.string.title_courses),
                yearPosition + 1,
                semester.position + 1,
            ),
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
            yearPosition = yearWithSemester.year.order,
            yearId = yearWithSemester.year.uid,
            semester = semester,
        )
    }

    override fun onYearItemMoved(newList: List<Year>) {
        yearViewModel.updateYears(*newList.toTypedArray())
    }

    override fun onYearItemSwiped(year: Year) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_year_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                yearViewModel.deleteYear(year.let {
                    Year(
                        uid = it.uid,
                        order = it.order
                    )
                })
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
