package com.github.anastr.myscore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.github.anastr.domain.entities.Semester
import com.github.anastr.myscore.databinding.BottomSheetCourseBinding
import com.github.anastr.myscore.util.launchAndRepeatOnLifecycle
import com.github.anastr.myscore.util.rapidClickListener
import com.github.anastr.myscore.viewmodel.CourseDialogState
import com.github.anastr.myscore.viewmodel.CourseViewModel
import com.github.anastr.myscore.viewmodel.State
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.Serializable

sealed class CourseMode : Serializable {
    class Edit(val courseId: Long) : CourseMode()
    class New(val yearId: Long, val semester: Semester) : CourseMode()
}

@AndroidEntryPoint
class CourseBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCourseBinding

    private val courseViewModel: CourseViewModel by viewModels()

    private val inputName: String
        get() = binding.nameEditText.text.toString().trim()
    private val inputTheoreticalDegree: Int
        get() =
            if (binding.theoreticalCheckBox.isChecked)
                binding.theoreticalTextInput.editText?.text?.toString()?.toIntOrNull() ?: 0
            else 0
    private val inputPracticalDegree: Int
        get() =
            if (binding.practicalCheckBox.isChecked)
                binding.practicalTextInput.editText?.text?.toString()?.toIntOrNull() ?: 0
            else 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCourseBinding.inflate(requireActivity().layoutInflater)
        initUi()
        return binding.root
    }

    private fun initUi() {

        binding.theoreticalCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.theoreticalTextInput.isEnabled = isChecked
        }
        binding.practicalCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.practicalTextInput.isEnabled = isChecked
        }

        binding.buttonCancel.rapidClickListener { dismiss() }
        binding.buttonSave.rapidClickListener { save() }
        binding.buttonDelete.apply {
            visibility = if (courseViewModel.courseMode is CourseMode.Edit) {
                rapidClickListener { delete() }
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        launchAndRepeatOnLifecycle(Lifecycle.State.CREATED) {
            courseViewModel.courseFlow.collect { state ->
                when (state) {
                    State.Loading -> {
                        // Do nothing!
                    }
                    is State.Error -> {
                        Toast.makeText(
                            requireContext(),
                            state.error.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
                    is State.Success -> {
                        binding.nameEditText.setText(state.data.name)
                        if (state.data.theoreticalScore != 0)
                            binding.theoreticalTextInput.editText?.setText(state.data.theoreticalScore.toString())
                        if (state.data.practicalScore != 0)
                            binding.practicalTextInput.editText?.setText(state.data.practicalScore.toString())
                        binding.theoreticalCheckBox.isChecked = state.data.hasTheoretical
                        binding.practicalCheckBox.isChecked = state.data.hasPractical
                    }
                }
            }
        }
        launchAndRepeatOnLifecycle(Lifecycle.State.STARTED) {
            courseViewModel.courseDialogState.collect { errorState ->
                when (errorState) {
                    CourseDialogState.Dismiss -> dismiss()
                    CourseDialogState.EmptyName -> {
                        binding.nameEditText.error = getString(R.string.this_field_required)
                    }
                    CourseDialogState.OneDegreeIsRequired -> {
                        Toast.makeText(
                            activity,
                            getString(R.string.one_degree_is_required),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CourseDialogState.DegreeBiggerThan100 -> {
                        Toast.makeText(
                            activity,
                            getString(R.string.degree_should_be_smaller_than_100),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is CourseDialogState.ExceptionDialog -> {
                        Toast.makeText(activity, errorState.e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun delete() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_course_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                courseViewModel.deleteCourse()
            }
            .show()
    }

    private fun save() {
        courseViewModel.insertOrUpdate(
            name = inputName,
            hasTheoretical = binding.theoreticalCheckBox.isChecked,
            hasPractical = binding.practicalCheckBox.isChecked,
            theoreticalScore = inputTheoreticalDegree,
            practicalScore = inputPracticalDegree,
        )
    }
}
