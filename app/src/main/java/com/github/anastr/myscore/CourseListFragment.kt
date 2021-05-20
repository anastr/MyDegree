package com.github.anastr.myscore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewGroupCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.anastr.myscore.adapter.CourseAdapter
import com.github.anastr.myscore.databinding.FragmentCourseListBinding
import com.github.anastr.myscore.util.MAX_COURSES
import com.github.anastr.myscore.viewmodel.CoursesViewModel
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseListFragment : Fragment() {

    private var _binding: FragmentCourseListBinding? = null
    private val binding get() =_binding!!

    private val coursesViewModel: CoursesViewModel by viewModels()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = courseAdapter
        }

        coursesViewModel.passDegreeLiveData.observe(viewLifecycleOwner) { passDegree ->
            courseAdapter.passDegree = passDegree
        }
        coursesViewModel.courses.observe(viewLifecycleOwner) { list ->
            if (list.size >= MAX_COURSES)
                mainActivity?.hideFab()
            else
                mainActivity?.showFab()
            courseAdapter.submitList(list)
            binding.progressBar.visibility = View.GONE
            binding.textNoData.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
