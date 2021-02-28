package com.github.anastr.myscore.util.pref

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import com.github.anastr.myscore.R


class NumberPreferenceDialogFragmentCompat: PreferenceDialogFragmentCompat() {

    private lateinit var picker: NumberPicker

    companion object {
        fun newInstance(key: String): NumberPreferenceDialogFragmentCompat {
            val fragment = NumberPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b

            return fragment
        }
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        picker = view?.findViewById(R.id.pref_num_picker)!!
        picker.minValue = 40
        picker.maxValue = 80

        var value: Int? = null
        val preference: DialogPreference = preference
        if (preference is NumberPickerPreference) {
            value = preference.value
        }

        value?.let {
            picker.value = value
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val newValue = picker.value

            // Get the related Preference and save the value
            val preference = preference
            if (preference is NumberPickerPreference) {
                // This allows the client to ignore the user value.
                if (preference.callChangeListener(
                        newValue
                    )
                ) {
                    // Save the value
                    preference.value = newValue
                }
            }
        }
    }
}