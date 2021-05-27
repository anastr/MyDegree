package com.github.anastr.myscore.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.AppDatabase
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.MAX_YEARS
import com.github.anastr.myscore.util.MainCoroutineRule
import com.github.anastr.myscore.util.testCoursesList
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class CourseListViewModelTest {

    private lateinit var viewModel: CourseListViewModel
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
    lateinit var courseRepository: CourseRepository

    @Before
    fun setUp() = coroutineRule.testDispatcher.runBlockingTest {
        hiltRule.inject()

        appDatabase.databaseDao().insertNewYear(MAX_YEARS)
        appDatabase.courseDao().insertAll(*testCoursesList.toTypedArray())

        val context = ApplicationProvider.getApplicationContext<Context>()
        val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
            set("yearId", 1L)
            set("semester", Semester.FirstSemester)
        }

        viewModel = CourseListViewModel(
            savedStateHandle = savedStateHandle,
            context = context,
            courseRepository = courseRepository,
            defaultDispatcher = coroutineRule.testDispatcher,
        )
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Test
    fun getCoursesFlow() = coroutineRule.testDispatcher.runBlockingTest {
        // Get the first success state.
        val state = withTimeoutOrNull(timeMillis = 3000) {
            viewModel.coursesFlow.first { viewModel.coursesFlow.value is State.Success } as State.Success
        }
        assertNotNull(state)

        val courses: List<Course> = state!!.data
        assertEquals(courses, testCoursesList)
    }
}
