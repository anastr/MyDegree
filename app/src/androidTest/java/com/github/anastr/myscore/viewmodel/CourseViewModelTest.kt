package com.github.anastr.myscore.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.github.anastr.data.room.AppDatabase
import com.github.anastr.domain.constant.MAX_YEARS
import com.github.anastr.domain.repositories.WriteCourseRepo
import com.github.anastr.myscore.CourseMode
import com.github.anastr.myscore.util.MainCoroutineRule
import com.github.anastr.myscore.util.testCourse
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class CourseViewModelTest {

    private lateinit var viewModel: CourseViewModel
    private val hiltRule = HiltAndroidRule(this)
    private val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val coroutineRule = MainCoroutineRule()

    @get:Rule
    val rule = RuleChain.outerRule(hiltRule)
        .around(instantTaskExecutorRule)
        .around(coroutineRule)

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var courseRepository: WriteCourseRepo

    @Before
    fun setUp() = coroutineRule.testDispatcher.runBlockingTest {
        hiltRule.inject()

        appDatabase.databaseDao().insertNewYear(MAX_YEARS)
    }

    private fun initViewModel(courseMode: CourseMode) {
        val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
            set("courseMode", courseMode)
        }

        viewModel = CourseViewModel(savedStateHandle, courseRepository)
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Test
    fun insertNewCourse() = coroutineRule.testDispatcher.runBlockingTest {
        initViewModel(CourseMode.New(yearId = testCourse.yearId, semester = testCourse.semester))

        // This should insert new course
        viewModel.insertOrUpdate(
            name = testCourse.name,
            hasTheoretical = testCourse.hasTheoretical,
            hasPractical = testCourse.hasPractical,
            theoreticalScore = testCourse.theoreticalScore,
            practicalScore = testCourse.practicalScore,
        )

        // Get the first course in this database
        val insertedCourse = appDatabase.databaseDao().getAllCourses().firstOrNull()

        assertNotNull(insertedCourse)

        assertEquals(insertedCourse, testCourse)
    }

    @Test
    fun updateCourse() = coroutineRule.testDispatcher.runBlockingTest {
        appDatabase.courseDao().insertAll(testCourse)
        initViewModel(CourseMode.Edit(testCourse.uid))

        assertNotNull(viewModel.course)

        // Change the name only
        viewModel.insertOrUpdate(
            name = "Edited",
            hasTheoretical = testCourse.hasTheoretical,
            hasPractical = testCourse.hasPractical,
            theoreticalScore = testCourse.theoreticalScore,
            practicalScore = testCourse.practicalScore,
        )

        val editedCourse = appDatabase.courseDao().getById(testCourse.uid)

        assertNotEquals(editedCourse, testCourse)
    }
}
