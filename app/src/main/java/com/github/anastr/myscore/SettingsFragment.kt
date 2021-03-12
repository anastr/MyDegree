package com.github.anastr.myscore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.*
import com.github.anastr.myscore.util.pref.NumberPickerPreference
import com.github.anastr.myscore.util.pref.NumberPreferenceDialogFragmentCompat
import com.github.anastr.myscore.viewmodel.MainViewModel
import com.github.anastr.myscore.worker.UploadBackupWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class SettingsFragment: PreferenceFragmentCompat() {

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    private val mainViewModel: MainViewModel by activityViewModels()

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
                true
            }

        preferenceManager.findPreference<CheckBoxPreference>("syncFirestoreData")
            ?.setOnPreferenceChangeListener { _, newValue ->
                if (FirebaseAuth.getInstance().currentUser == null) {
                    (requireActivity() as MainActivity).registerWithGoogle()
                    return@setOnPreferenceChangeListener false
                }
                if (newValue == true) {
                    val sendLogsWorkRequest =
                        PeriodicWorkRequestBuilder<UploadBackupWorker>(7, TimeUnit.DAYS)
                            .setInitialDelay(7, TimeUnit.DAYS)
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                            .build()
                    WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                        UploadBackupWorker.UNIQUE_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        sendLogsWorkRequest
                    )
                }
                else {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork(UploadBackupWorker.UNIQUE_NAME)
                }
                true
            }

        preferenceManager.findPreference<Preference>("deleteServerData")
            ?.setOnPreferenceClickListener {
                if (mainViewModel.loadingLiveData.value == true) {
                    return@setOnPreferenceClickListener true
                }
                if (FirebaseAuth.getInstance().currentUser == null) {
                    (requireActivity() as MainActivity).registerWithGoogle()
                }
                else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.delete_backup_warning_message)
                        .setPositiveButton(R.string.delete) { _, _ ->
                            mainViewModel.deleteBackup()
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show()
                }
                true
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