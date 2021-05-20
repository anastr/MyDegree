package com.github.anastr.myscore

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.github.anastr.myscore.databinding.DialogCourseBinding
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.rapidClickListener
import com.github.anastr.myscore.viewmodel.CourseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

sealed class CourseMode : Serializable {
    class Edit(val courseId: Long) : CourseMode()
    class New(val yearId: Long, val semester: Semester) : CourseMode()
}

@AndroidEntryPoint
class CourseDialog : DialogFragment() {

    private lateinit var binding: DialogCourseBinding

    private val courseViewModel: CourseViewModel by viewModels()

    private lateinit var course: Course

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            binding = DialogCourseBinding.inflate(requireActivity().layoutInflater)
            val view = binding.root

            initUi()

            builder.apply {
                setView(view)
                setTitle(
                    when (courseViewModel.courseMode) {
                        is CourseMode.New -> R.string._new
                        is CourseMode.Edit -> R.string.edit
                    }
                )
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
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

        courseViewModel.course.observe(this) { course ->
            if (course != null) {
                this.course = course
                binding.nameEditText.setText(course.name)
                if (course.theoreticalScore != 0)
                    binding.theoreticalTextInput.editText?.setText(course.theoreticalScore.toString())
                if (course.practicalScore != 0)
                    binding.practicalTextInput.editText?.setText(course.practicalScore.toString())
                binding.theoreticalCheckBox.isChecked = course.hasTheoretical
                binding.practicalCheckBox.isChecked = course.hasPractical
            } else {
                dismiss()
            }
        }
    }

    private fun delete() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string._new)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                try {
                    courseViewModel.deleteCourse(course)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun save() {
        if (validate()) try {
            course.apply {
                name = inputName
                hasTheoretical = binding.theoreticalCheckBox.isChecked
                hasPractical = binding.practicalCheckBox.isChecked
                theoreticalScore = inputTheoreticalDegree
                practicalScore = inputPracticalDegree
            }
            when (courseViewModel.courseMode) {
                is CourseMode.New -> courseViewModel.insertCourse(course)
                is CourseMode.Edit -> courseViewModel.updateCourse(course)
            }
            dismiss()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validate(): Boolean {
        return when {
            inputName.isEmpty() -> {
                binding.nameEditText.error = getString(R.string.this_field_required)
                false
            }
            !binding.theoreticalCheckBox.isChecked && !binding.practicalCheckBox.isChecked -> {
                Toast.makeText(
                    activity,
                    getString(R.string.one_degree_is_required),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            inputTheoreticalDegree + inputPracticalDegree > 100 -> {
                Toast.makeText(
                    activity,
                    getString(R.string.degree_should_be_smaller_than_100),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            else -> true
        }
    }
}
