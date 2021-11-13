package com.github.anastr.myscore.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.data.hilt.DefaultDispatcher
import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.repositories.PassDegreeRepo
import com.github.anastr.domain.repositories.ReadCourseRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    passDegreeRepo: PassDegreeRepo,
    courseRepository: ReadCourseRepo,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
): ViewModel() {

    private val yearId: Long = savedStateHandle.get(YEAR_ID_KEY)!!
    private val semester: Semester = savedStateHandle.get(SEMESTER_KEY)!!

    val passDegreeFlow: SharedFlow<Int> =
        passDegreeRepo.getPassDegree()
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
