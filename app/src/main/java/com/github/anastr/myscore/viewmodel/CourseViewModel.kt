package com.github.anastr.myscore.viewmodel

import androidx.lifecycle.*
import com.github.anastr.myscore.CourseMode
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CourseDialogState {
    object Dismiss: CourseDialogState()
    object EmptyName: CourseDialogState()
    object OneDegreeIsRequired: CourseDialogState()
    object DegreeBiggerThan100: CourseDialogState()
    class ExceptionDialog(val e: Exception): CourseDialogState()
}

@HiltViewModel
class CourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
): ViewModel() {

    val courseMode: CourseMode = savedStateHandle.get(COURSE_MODE_KEY)!!

    private val _courseDialogState = MutableSharedFlow<CourseDialogState>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val courseDialogState: Flow<CourseDialogState> = _courseDialogState

    val course: LiveData<Course?> =
        when (courseMode) {
            is CourseMode.Edit -> courseRepository.getCourse(courseMode.courseId)
                .distinctUntilChanged()
                .asLiveData()
            is CourseMode.New -> MutableLiveData(
                Course(
                    yearId = courseMode.yearId,
                    semester = courseMode.semester,
                    name = "",
                    hasPractical = true,
                    hasTheoretical = true,
                    theoreticalScore = 0,
                    practicalScore = 0,
                )
            )
        }

    fun insertOrUpdate(
        name: String,
        hasTheoretical: Boolean,
        hasPractical: Boolean,
        theoreticalScore: Int,
        practicalScore: Int,
    ) {
        val newOrUpdatedCourse = course.value?.copy(
            name = name,
            hasTheoretical = hasTheoretical,
            hasPractical = hasPractical,
            theoreticalScore = theoreticalScore,
            practicalScore = practicalScore,
        )
        if (newOrUpdatedCourse != null && validate(newOrUpdatedCourse)) {
            when (courseMode) {
                is CourseMode.New -> insertCourse(newOrUpdatedCourse)
                is CourseMode.Edit -> updateCourse(newOrUpdatedCourse)
            }
            _courseDialogState.tryEmit(CourseDialogState.Dismiss)
        }
    }

    private fun validate(course: Course): Boolean {
        return when {
            course.name.isEmpty() -> {
                _courseDialogState.tryEmit(CourseDialogState.EmptyName)
                false
            }
            !course.hasTheoretical && !course.hasPractical -> {
                _courseDialogState.tryEmit(CourseDialogState.OneDegreeIsRequired)
                false
            }
            course.score > 100 -> {
                _courseDialogState.tryEmit(CourseDialogState.DegreeBiggerThan100)
                false
            }
            else -> true
        }
    }

    private fun insertCourse(course: Course) {
        viewModelScope.launch {
            try {
                courseRepository.insertCourses(course)
            } catch (e: Exception) {
                _courseDialogState.tryEmit(CourseDialogState.ExceptionDialog(e))
            }
        }
    }

    private fun updateCourse(course: Course) {
        viewModelScope.launch {
            try {
                courseRepository.updateCourse(course)
            } catch (e: Exception) {
                _courseDialogState.tryEmit(CourseDialogState.ExceptionDialog(e))
            }
        }
    }

    fun deleteCourse() {
        viewModelScope.launch {
            try {
                course.value?.let { courseRepository.deleteCourse(it) }
            } catch (e: Exception) {
                _courseDialogState.tryEmit(CourseDialogState.ExceptionDialog(e))
            }
        }
    }

    companion object {
        const val COURSE_MODE_KEY = "courseMode"
    }
}
