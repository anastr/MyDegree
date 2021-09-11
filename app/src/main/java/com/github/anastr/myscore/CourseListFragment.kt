package com.github.anastr.myscore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewGroupCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.anastr.myscore.adapter.CourseAdapter
import com.github.anastr.myscore.databinding.FragmentCourseListBinding
import com.github.anastr.myscore.util.MAX_COURSES
import com.github.anastr.myscore.viewmodel.CourseListViewModel
import com.github.anastr.myscore.viewmodel.State
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CourseListFragment : Fragment() {

    private var _binding: FragmentCourseListBinding? = null
    private val binding get() =_binding!!

    private val courseListViewModel: CourseListViewModel by viewModels()

    private val courseAdapter = CourseAdapter()

    private val mainActivity: MainActivity? get() = (requireActivity() as? MainActivity)

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
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        ViewGroupCompat.setTransitionGroup(binding.root, true)
        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = courseAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                courseListViewModel.passDegreeFlow.collect { passDegree ->
                    courseAdapter.passDegree = passDegree
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                courseListViewModel.coursesFlow.collect { state ->
                    when (state) {
                        is State.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.textMessage.visibility = View.GONE
                        }
                        is State.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.textMessage.visibility = View.VISIBLE
                            binding.textMessage.text = state.error.message
                        }
                        is State.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if (state.data.isEmpty()) {
                                binding.textMessage.visibility = View.VISIBLE
                                binding.textMessage.setText(R.string.noData)
                            } else {
                                binding.textMessage.visibility = View.GONE
                            }
                            if (state.data.size >= MAX_COURSES)
                                mainActivity?.hideFab()
                            else
                                mainActivity?.showFab()
                            courseAdapter.submitList(state.data)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
