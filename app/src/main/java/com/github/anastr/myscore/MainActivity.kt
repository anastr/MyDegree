package com.github.anastr.myscore

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.github.anastr.myscore.databinding.ActivityMainBinding
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.viewmodel.ErrorCode
import com.github.anastr.myscore.viewmodel.FirebaseState
import com.github.anastr.myscore.viewmodel.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    private var currentYearId: Long = -1L
    private var currentSemester: Semester = Semester.FirstSemester

    private val navController get() = findNavController(R.id.nav_host_fragment)
    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        mainViewModel.themeLiveData.observe(this) { nightMode ->
            AppCompatDelegate.setDefaultNightMode(
                when(nightMode) {
                    "1" -> AppCompatDelegate.MODE_NIGHT_NO
                    "2" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }

        binding.content.fab.rapidClickListener {
            if (currentYearId == -1L) {
                mainViewModel.insertNewYear()
            }
            else {
                val action = CourseListFragmentDirections.actionCourseListFragmentToCourseDialog(
                    CourseMode.New(
                        currentYearId,
                        currentSemester
                    )
                )
                navController.navigate(action)
            }
        }

        NavigationUI.setupWithNavController(binding.toolbar, navController)
        binding.content.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            currentNavigationFragment?.apply {
                exitTransition = MaterialFadeThrough().apply {
                    duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                }
                reenterTransition = exitTransition
            }
            NavigationUI.onNavDestinationSelected(item, navController)
        }
        navController.addOnDestinationChangedListener(
            this@MainActivity
        )

        mainViewModel.loadingLiveData.observe(this) { isLoading ->
            if (isLoading) {
                loading = true
                binding.progress.show()
            }
            else {
                loading = false
                binding.progress.hide()
            }
        }

        lifecycleScope.launchWhenStarted {
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
                        showSnackBar(getString(R.string.backup_saved_to_server), Snackbar.LENGTH_LONG)
                    }
                    FirebaseState.ReceiveBackupSucceeded -> {
                        navController.popBackStack(R.id.year_page_fragment, false)
                        showSnackBar(getString(R.string.backup_received_from_server), Snackbar.LENGTH_LONG)
                    }
                    FirebaseState.DeleteBackupSucceeded -> {
                        showSnackBar(getString(R.string.backup_deleted_successfully), Snackbar.LENGTH_LONG)
                    }
                }
            }
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        manageToolbar(destination)
        manageBottomBar(destination)
        when (destination.id) {
            R.id.settingsFragment,
            R.id.aboutFragment -> {
                binding.content.fab.hide()
            }
            R.id.year_page_fragment -> {
                currentYearId = -1L
            }
            R.id.chart_page_fragment -> {
                binding.content.fab.hide()
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
            R.id.courseDialog -> {
                currentYearId = -1L
            }
        }
    }

    private fun manageToolbar(destination: NavDestination) {
        when (destination.id) {
            R.id.settingsFragment,
            R.id.aboutFragment,
            R.id.year_page_fragment,
            R.id.chart_page_fragment -> {
                title = navController.currentDestination?.label
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
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()
                return true
            }
            R.id.receiveBackup -> {
                if (isLoadingOrNotAuth()) return true
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.receive_data_warning_message)
                    .setPositiveButton(R.string.receive) { _, _ -> mainViewModel.receiveBackup() }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                return true
            }
            else -> {
                currentNavigationFragment?.apply {
                    exitTransition = MaterialFadeThrough().apply {
                        duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                    }
                    reenterTransition = exitTransition
                }
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from googleSignInClient.signInIntent
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                mainViewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showSnackBar(getString(R.string.google_signin_failed))
            }
        }
    }

    fun hideFab() = binding.content.fab.hide()

    fun showFab() = binding.content.fab.show()

    private fun showSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(binding.content.fab, message, duration).show()
    }

    companion object {
        const val RC_SIGN_IN = 101
    }
}
