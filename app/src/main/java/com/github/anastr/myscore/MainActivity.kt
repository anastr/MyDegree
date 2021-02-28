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
import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.firebase.toCourse
import com.github.anastr.myscore.firebase.toHashMap
import com.github.anastr.myscore.firebase.toYear
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.viewmodel.YearViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    NavController.OnDestinationChangedListener {

    private var yearsCount = 0

    private val yearViewModel: YearViewModel by viewModels()

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
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        hideProgress()

        yearViewModel.themeLiveData.observe(this) { nightMode ->
            AppCompatDelegate.setDefaultNightMode(
                when(nightMode) {
                    "1" -> AppCompatDelegate.MODE_NIGHT_NO
                    "2" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }

        yearViewModel.yearsCount.observe(this) {
            yearsCount = it
            manageFabVisibility()
        }

        fab.rapidClickListener {
            if (currentYearId == -1L) {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (yearsCount < MAX_YEARS)
                        yearViewModel.insertYears(Year(order = yearsCount))
                }
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

        NavigationUI.setupWithNavController(toolbar, navController)
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
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
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.settingsFragment -> {
                motion_layout.transitionToEnd()
                fab.hideFab()
            }
            R.id.aboutFragment -> {
                motion_layout.transitionToEnd()
                fab.hideFab()
            }
            R.id.year_page_fragment -> {
                motion_layout.transitionToStart()
                manageFabVisibility()
                currentYearId = -1L
            }
            R.id.chart_page_fragment -> {
                motion_layout.transitionToStart()
                fab.hideFab()
                currentYearId = -1L
            }
            R.id.courseListFragment -> {
                motion_layout.transitionToEnd()
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

    private fun manageFabVisibility() {
        if (navController.currentDestination?.id == R.id.year_page_fragment) {
            if (yearsCount >= MAX_YEARS)
                fab.hideFab()
            else
                fab.showFab()
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
                        .setPositiveButton(R.string._continue) { _, _ -> sendBackup() }
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
                        .setPositiveButton(R.string.receive) { _, _ -> receiveBackup() }
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

    private fun sendBackup() {
        showProgress()
        lifecycleScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore
            val years = yearViewModel.getYears()
            val courses = yearViewModel.getCourses()
            val degreeDocument = DegreeDocument().apply {
                this.years = years.map { it.toHashMap() }
                this.courses = courses.map { it.toHashMap() }
            }
            val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            db.runTransaction { transaction ->
                transaction.set(docRef, degreeDocument)
            }
                .addOnSuccessListener {
                    hideProgress()
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setMessage(R.string.backup_saved_to_server)
                        .setPositiveButton(R.string.ok) { _, _ -> }
                        .show()
                }
                .addOnFailureListener { e ->
                    hideProgress()
                    manageFirebaseError(e)
                }
        }
    }

    private fun receiveBackup() {
        showProgress()
        val db = Firebase.firestore
        val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
        docRef.get(Source.SERVER).addOnSuccessListener { documentSnapshot ->
            try {
                val degreeDocument = documentSnapshot.toObject(DegreeDocument::class.java)
                saveDataFromFireStore(degreeDocument!!)
            }
            catch (e: Exception) {
                hideProgress()
                e.printStackTrace()
                Snackbar.make(fab, getString(R.string.backup_data_corrupted), Snackbar.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener { e ->
                hideProgress()
                manageFirebaseError(e)
            }
    }

    private fun saveDataFromFireStore(degreeDocument: DegreeDocument) {
        lifecycleScope.launch {
            navController.popBackStack(R.id.year_page_fragment, false)
            withContext(Dispatchers.IO) {
                yearViewModel.deleteAll()
                if (degreeDocument.years != null) {
                    val years = degreeDocument.years!!.map { it.toYear() }
                    yearViewModel.insertYears(*years.toTypedArray())
                }
                if (degreeDocument.courses != null) {
                    val courses = degreeDocument.courses!!.map { it.toCourse() }
                    yearViewModel.insertCourses(*courses.toTypedArray())
                }
            }
            hideProgress()
            Snackbar.make(
                fab,
                getString(R.string.backup_received_from_server),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun registerWithGoogle() {
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
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Snackbar.make(fab, getString(R.string.google_signin_failed), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showProgress()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                hideProgress()
                val user = auth.currentUser
                MaterialAlertDialogBuilder(this)
                    .setMessage(
                        String.format(
                            getString(R.string.firebase_signin_succeeded),
                            user?.displayName
                        )
                    )
                    .setPositiveButton(R.string.ok) { _, _ -> }
                    .show()
            }
            .addOnFailureListener { e ->
                hideProgress()
                manageFirebaseError(e)
            }
    }

    private fun manageFirebaseError(e: Exception) {
        e.printStackTrace()
//        val message = if (e.isError403())
//            getString(R.string.message_need_vpn)
//        else
//            getString(R.string.message_failed_connect)
        Snackbar.make(fab, getString(R.string.message_need_vpn), Snackbar.LENGTH_SHORT).show()
    }

    private fun showProgress() {
        loading = true
        progress.show()
    }

    private fun hideProgress() {
        loading = false
        progress.hide()
    }

    companion object {
        const val RC_SIGN_IN = 101
    }
}
