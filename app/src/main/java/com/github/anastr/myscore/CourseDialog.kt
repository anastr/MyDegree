package com.github.anastr.myscore

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.rapidClickListener
import com.github.anastr.myscore.viewmodel.CourseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

sealed class CourseMode: Serializable {
    class Edit(val courseId: Long): CourseMode()
    class New(val yearId: Long, val semester: Semester) : CourseMode()
}

@AndroidEntryPoint
class CourseDialog: DialogFragment() {

    private val args: CourseDialogArgs by navArgs()

    private val courseViewModel: CourseViewModel by viewModels()

    private lateinit var nameEditText: EditText
    private lateinit var theoreticalTextInput: TextInputLayout
    private lateinit var theoreticalCheckBox: CheckBox
    private lateinit var practicalTextInput: TextInputLayout
    private lateinit var practicalCheckBox: CheckBox

    private lateinit var course: Course

    private val inputName: String
        get() = nameEditText.text.toString().trim()
    private val inputTheoreticalDegree: Int
        get() = if (theoreticalCheckBox.isChecked) theoreticalTextInput.editText?.text?.toString()?.toIntOrNull() ?: 0 else 0
    private val inputPracticalDegree: Int
        get() = if (practicalCheckBox.isChecked) practicalTextInput.editText?.text?.toString()?.toIntOrNull() ?: 0 else 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        courseViewModel.setInput(args.courseMode)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            val view = requireActivity().layoutInflater.inflate(R.layout.dialog_course, null)

            initUi(view!!)

            builder.apply {
                setView(view)
                setTitle(
                    when (args.courseMode) {
                        is CourseMode.New -> R.string._new
                        is CourseMode.Edit -> R.string.edit
                    }
                )
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initUi(view: View) {
        nameEditText = view.findViewById(R.id.et_name)
        theoreticalTextInput = view.findViewById(R.id.ti_theoretical)
        theoreticalCheckBox = view.findViewById(R.id.cb_theoretical)
        practicalTextInput = view.findViewById(R.id.ti_practical)
        practicalCheckBox = view.findViewById(R.id.cb_practical)

        theoreticalCheckBox.setOnCheckedChangeListener { _, isChecked ->
            theoreticalTextInput.isEnabled = isChecked
        }
        practicalCheckBox.setOnCheckedChangeListener { _, isChecked ->
            practicalTextInput.isEnabled = isChecked
        }

        view.findViewById<Button>(R.id.button_cancel).rapidClickListener { dismiss() }
        view.findViewById<Button>(R.id.button_save).rapidClickListener { save() }
        view.findViewById<Button>(R.id.button_delete).apply {
            visibility = if (args.courseMode is CourseMode.Edit) {
                rapidClickListener { delete() }
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        courseViewModel.course.observe(this) {
            it?.let {
                course = it
                nameEditText.setText(it.name)
                if (it.theoreticalScore != 0)
                    theoreticalTextInput.editText?.setText(it.theoreticalScore.toString())
                if (it.practicalScore != 0)
                    practicalTextInput.editText?.setText(it.practicalScore.toString())
                theoreticalCheckBox.isChecked = it.hasTheoretical
                practicalCheckBox.isChecked = it.hasPractical
            }
        }
    }

    private fun delete() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string._new)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            courseViewModel.deleteCourse(course)
                        }
                        dialog.dismiss()
                        dismiss()
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun save() {
        if (validate()) try {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    course.apply {
                        name = inputName
                        hasTheoretical = theoreticalCheckBox.isChecked
                        hasPractical = practicalCheckBox.isChecked
                        theoreticalScore = inputTheoreticalDegree
                        practicalScore = inputPracticalDegree
                    }
                    when (args.courseMode) {
                        is CourseMode.New -> courseViewModel.insertCourse(course)
                        is CourseMode.Edit -> courseViewModel.updateCourse(course)
                    }
                }
                dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validate(): Boolean {
        return when {
            inputName.isEmpty() -> {
                nameEditText.error = getString(R.string.this_field_required)
                false
            }
            !theoreticalCheckBox.isChecked && !practicalCheckBox.isChecked -> {
                Toast.makeText(activity, getString(R.string.one_degree_is_required), Toast.LENGTH_SHORT).show()
                false
            }
            inputTheoreticalDegree + inputPracticalDegree > 100 -> {
                Toast.makeText(activity, getString(R.string.degree_should_be_smaller_than_100), Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}