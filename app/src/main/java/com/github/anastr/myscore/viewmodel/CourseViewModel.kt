package com.github.anastr.myscore.viewmodel

import androidx.lifecycle.*
import com.github.anastr.myscore.CourseMode
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
): ViewModel() {

    val courseMode: CourseMode = savedStateHandle.get(COURSE_MODE_KEY)!!

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

    fun insertCourse(course: Course) {
        viewModelScope.launch {
            courseRepository.insertCourses(course)
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            courseRepository.updateCourse(course)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            courseRepository.deleteCourse(course)
        }
    }

    companion object {
        const val COURSE_MODE_KEY = "courseMode"
    }
}
