package com.github.anastr.myscore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.anastr.myscore.util.pref.NumberPickerPreference
import com.github.anastr.myscore.util.pref.NumberPreferenceDialogFragmentCompat
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class SettingsFragment: PreferenceFragmentCompat() {

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager.findPreference<ListPreference>("themePref")
            ?.setOnPreferenceChangeListener { _, newValue ->
                firebaseAnalytics.logEvent("theme_changed", Bundle().apply {
                    val value = when(newValue) {
                        "1" -> "Lite"
                        "2" -> "Dark"
                        else -> "Follow system"
                    }
                    putString(FirebaseAnalytics.Param.ITEM_VARIANT, value)
                })
                return@setOnPreferenceChangeListener true
            }
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