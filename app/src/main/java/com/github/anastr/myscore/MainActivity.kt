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
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.github.anastr.myscore.databinding.ActivityMainBinding
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.room.entity.Year
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

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding

    private var yearsCount = 0

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

        mainViewModel.yearsCount.observe(this) {
            yearsCount = it
            manageFabVisibility()
        }

        binding.content.fab.rapidClickListener {
            if (currentYearId == -1L) {
                if (yearsCount < MAX_YEARS)
                    mainViewModel.insertYears(Year(order = yearsCount))
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

        mainViewModel.firebaseState.observe(this) { state ->
            when (state) {
                FirebaseState.Normal -> hideProgress()
                FirebaseState.Loading -> showProgress()
                is FirebaseState.GoogleLoginSucceeded -> {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(
                            String.format(
                                getString(R.string.firebase_signin_succeeded),
                                state.user.displayName
                            )
                        )
                        .setPositiveButton(R.string.ok) { _, _ -> }
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
                    manageFirebaseError(state.exception)
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
            if (state !is FirebaseState.Normal && state !is FirebaseState.Loading) {
                mainViewModel.toNormalState()
            }
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        manageToolbar(destination)
        when (destination.id) {
            R.id.settingsFragment -> {
                binding.content.motionLayout.transitionToEnd()
                binding.content.fab.hideFab()
            }
            R.id.aboutFragment -> {
                binding.content.motionLayout.transitionToEnd()
                binding.content.fab.hideFab()
            }
            R.id.year_page_fragment -> {
                binding.content.motionLayout.transitionToStart()
                manageFabVisibility()
                currentYearId = -1L
            }
            R.id.chart_page_fragment -> {
                binding.content.motionLayout.transitionToStart()
                binding.content.fab.hideFab()
                currentYearId = -1L
            }
            R.id.courseListFragment -> {
                binding.content.motionLayout.transitionToEnd()
                if (arguments == null) {
                    currentYearId = -1L
                } else {
                    currentYearId = CourseListFragmentArgs.fromBundle(arguments).yearId
                    currentSemester = CourseListFragmentArgs.fromBundle(arguments).semester
                }
            }
            R.id.courseDialog -> {
                binding.content.motionLayout.transitionToEnd()
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

    private fun manageFabVisibility() {
        if (navController.currentDestination?.id == R.id.year_page_fragment) {
            if (yearsCount >= MAX_YEARS)
                binding.content.fab.hideFab()
            else
                binding.content.fab.showFab()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sendBackup -> {
                if (loading) {
                    Toast.makeText(
                        this,
                        getString(R.string.loading_in_progress),
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }
                if (auth.currentUser == null) {
                    registerWithGoogle()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.send_backup_warning_message)
                        .setPositiveButton(R.string._continue) { _, _ -> mainViewModel.sendBackup() }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show()
                }
                return true
            }
            R.id.receiveBackup -> {
                if (loading) {
                    Toast.makeText(
                        this,
                        getString(R.string.loading_in_progress),
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }
                if (auth.currentUser == null) {
                    registerWithGoogle()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.receive_data_warning_message)
                        .setPositiveButton(R.string.receive) { _, _ -> mainViewModel.receiveBackup() }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show()
                }
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

    fun registerWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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

    private fun manageFirebaseError(e: Exception) {
        e.printStackTrace()
//        val message = if (e.isError403())
//            getString(R.string.message_need_vpn)
//        else
//            getString(R.string.message_failed_connect)
        showSnackBar(getString(R.string.message_need_vpn))
    }

    private fun showProgress() {
        loading = true
        binding.progress.show()
    }

    private fun hideProgress() {
        loading = false
        binding.progress.hide()
    }

    private fun showSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(binding.content.fab, message, duration).show()
    }

    companion object {
        const val RC_SIGN_IN = 101
    }
}
