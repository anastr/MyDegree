package com.github.anastr.myscore

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewGroupCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.anastr.myscore.util.pref.NumberPickerPreference
import com.github.anastr.myscore.util.pref.NumberPreferenceDialogFragmentCompat
import com.google.android.material.transition.MaterialFadeThrough

class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewGroupCompat.setTransitionGroup(view as ViewGroup, true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        // Try if the preference is one of our custom Preferences
        var dialogFragment: DialogFragment? = null
        if (preference is NumberPickerPreference) {
            dialogFragment = NumberPreferenceDialogFragmentCompat
                .newInstance(preference.key)
        }

        // If it was one of our custom Preferences, show its dialog
        dialogFragment?.let {
            @Suppress("DEPRECATION")
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(
                parentFragmentManager,
                "NumberPickerPreference"
            )
        } ?: run {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}