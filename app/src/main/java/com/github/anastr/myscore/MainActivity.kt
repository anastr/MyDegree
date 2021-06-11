package com.github.anastr.myscore

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.anastr.myscore.databinding.ActivityMainBinding
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.viewmodel.MainViewModel
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

        addRepeatingJob(Lifecycle.State.STARTED) {
            launch {
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
        return NavigationUI.onNavDestinationSelected(item, navController)
    }

    fun hideFab() = binding.content.fab.hide()

    fun showFab() = binding.content.fab.show()
}
