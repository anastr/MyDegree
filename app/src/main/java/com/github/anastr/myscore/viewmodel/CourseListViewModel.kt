package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.intFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sharedPreferences: SharedPreferences,
    courseRepository: CourseRepository,
    defaultDispatcher: CoroutineDispatcher,
): ViewModel() {

    private val yearId: Long = savedStateHandle.get(YEAR_ID_KEY)!!
    private val semester: Semester = savedStateHandle.get(SEMESTER_KEY)!!

    @ExperimentalCoroutinesApi
    val passDegreeFlow: SharedFlow<Int> =
        sharedPreferences.intFlow("passDegree", 60)
            // Do preference job on a worker thread.
            .flowOn(defaultDispatcher)
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1,
            )

    val coursesFlow: StateFlow<State<List<Course>>> =
        courseRepository.getCourses(yearId, semester)
            .map { State.Success(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = State.Loading,
            )

    companion object {
        const val YEAR_ID_KEY = "yearId"
        const val SEMESTER_KEY = "semester"
    }
}
