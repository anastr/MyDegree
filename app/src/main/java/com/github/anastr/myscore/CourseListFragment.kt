package com.github.anastr.myscore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.github.anastr.myscore.adapter.CourseAdapter
import com.github.anastr.myscore.databinding.FragmentCourseListBinding
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.viewmodel.CoursesViewModel
import com.github.anastr.myscore.viewmodel.CoursesViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CourseListFragment : Fragment(), CourseAdapter.CourseAdapterListener {

    private var _binding: FragmentCourseListBinding? = null
    private val binding get() =_binding!!

    private val args: CourseListFragmentArgs by navArgs()

    @Inject
    lateinit var coursesViewModelFactory: CoursesViewModelFactory
    private val coursesViewModel: CoursesViewModel by viewModels {
        CoursesViewModel.provideFactory(coursesViewModelFactory, args.yearId, args.semester)
    }

    private val courseAdapter = CourseAdapter(this)

    private val fab: FloatingActionButton by lazy { requireActivity().findViewById(R.id.fab) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }

        requireActivity().title =
            String.format(Locale.ENGLISH,
                requireContext().getString(R.string.title_courses),
                args.yearPosition+1,
                args.semester.position+1,
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
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
        coursesViewModel.courses.toFlowable(this)
            .subscribe { list ->
                if (list.size >= MAX_COURSES)
                    fab.hideFab()
                else
                    fab.showFab()
                courseAdapter.submitList(list)
                binding.progressBar.visibility = View.GONE
                if (list.isEmpty())
                    binding.textNoData.visibility = View.VISIBLE
                else
                    binding.textNoData.visibility = View.GONE
            }
            .disposeOnDestroy(this)
    }

    override fun onClickCourse(course: Course) {
        val action = CourseListFragmentDirections.actionCourseListFragmentToCourseDialog(CourseMode.Edit(course.uid))
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
