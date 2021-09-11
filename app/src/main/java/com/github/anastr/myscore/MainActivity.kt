package com.github.anastr.myscore

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.anastr.myscore.databinding.ActivityMainBinding
import com.github.anastr.myscore.firebase.GoogleSignInContent
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.viewmodel.ErrorCode
import com.github.anastr.myscore.viewmodel.FirebaseState
import com.github.anastr.myscore.viewmodel.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    private var currentYearId: Long = -1L
    private var currentSemester: Semester = Semester.FirstSemester

    private val navController get() = findNavController(R.id.nav_host_fragment)

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var loading = false

    private val googleSignInLauncher = registerForActivityResult(GoogleSignInContent()) { result ->
        result?.let {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                mainViewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showSnackBar(getString(R.string.google_signin_failed))
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.content.fab.rapidClickListener {
            if (currentYearId == -1L) {
                mainViewModel.insertNewYear()
            } else {
                val action = CourseListFragmentDirections.actionCourseListFragmentToCourseDialog(
                    CourseMode.New(
                        currentYearId,
                        currentSemester
                    )
                )
                navController.navigate(action)
            }
        }

        setupActionBarWithNavController(
            navController, AppBarConfiguration(
                topLevelDestinationIds = setOf(R.id.year_page_fragment, R.id.chart_page_fragment),
            )
        )
        binding.content.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(
            this@MainActivity
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.themeFlow.collect { nightMode ->
                    AppCompatDelegate.setDefaultNightMode(
                        when (nightMode) {
                            "1" -> AppCompatDelegate.MODE_NIGHT_NO
                            "2" -> AppCompatDelegate.MODE_NIGHT_YES
                            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    )
                }
            }

            launch {
                mainViewModel.loadingFlow.collect { isLoading ->
                    if (isLoading) {
                        loading = true
                        binding.progress.show()
                    } else {
                        loading = false
                        binding.progress.hide()
                    }
                }
            }

            launch {
                mainViewModel.firebaseStateFlow.collect { state ->
                    when (state) {
                        is FirebaseState.GoogleLoginSucceeded -> {
                            MaterialAlertDialogBuilder(this@MainActivity)
                                .setMessage(
                                    String.format(
                                        getString(R.string.firebase_signin_succeeded),
                                        state.user.displayName
                                    )
                                )
                                .setPositiveButton(R.string.ok, null)
                                .show()
                        }
                        is FirebaseState.Error -> {
                            val message = when (state.errorCode) {
                                ErrorCode.NoDataOnServer -> getString(R.string.backup_data_empty)
                                ErrorCode.DataCorrupted -> getString(R.string.backup_data_corrupted)
                            }
                            showSnackBar(message)
                        }
                        is FirebaseState.FirestoreError -> {
                            state.exception.printStackTrace()
                            showSnackBar(getString(R.string.message_need_vpn))
                        }
                        FirebaseState.SendBackupSucceeded -> {
                            showSnackBar(
                                getString(R.string.backup_saved_to_server),
                                Snackbar.LENGTH_LONG
                            )
                        }
                        FirebaseState.ReceiveBackupSucceeded -> {
                            navController.popBackStack(R.id.year_page_fragment, false)
                            showSnackBar(
                                getString(R.string.backup_received_from_server),
                                Snackbar.LENGTH_LONG
                            )
                        }
                        FirebaseState.DeleteBackupSucceeded -> {
                            showSnackBar(
                                getString(R.string.backup_deleted_successfully),
                                Snackbar.LENGTH_LONG
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        manageBottomBar(destination)
        when (destination.id) {
            R.id.chart_page_fragment,
            R.id.settingsFragment,
            R.id.aboutFragment -> {
                binding.content.fab.hide()
            }
            R.id.year_page_fragment -> {
                currentYearId = -1L
            }
            R.id.courseListFragment -> {
                if (arguments == null) {
                    currentYearId = -1L
                } else {
                    currentYearId = CourseListFragmentArgs.fromBundle(arguments).yearId
                    currentSemester = CourseListFragmentArgs.fromBundle(arguments).semester
                }
            }
        }
    }

    private fun manageBottomBar(destination: NavDestination) {
        when (destination.id) {
            R.id.year_page_fragment,
            R.id.chart_page_fragment -> binding.content.motionLayout.transitionToStart()
            else -> binding.content.motionLayout.transitionToEnd()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sendBackup -> {
                if (isLoadingOrNotAuth()) return true
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.send_backup_warning_message)
                    .setPositiveButton(R.string._continue) { _, _ -> mainViewModel.sendBackup() }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            R.id.receiveBackup -> {
                if (isLoadingOrNotAuth()) return true
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.receive_data_warning_message)
                    .setPositiveButton(R.string.receive) { _, _ -> mainViewModel.receiveBackup() }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            else -> {
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
    }

    private fun isLoadingOrNotAuth(): Boolean {
        if (loading) {
            Toast.makeText(this, getString(R.string.loading_in_progress), Toast.LENGTH_SHORT)
                .show()
            return true
        }
        if (auth.currentUser == null) {
            registerWithGoogle()
            return true
        }
        return false
    }

    fun registerWithGoogle() {
        googleSignInLauncher.launch(0)
    }

    fun hideFab() = binding.content.fab.hide()

    fun showFab() = binding.content.fab.show()

    private fun showSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(binding.content.fab, message, duration).show()
    }
}
