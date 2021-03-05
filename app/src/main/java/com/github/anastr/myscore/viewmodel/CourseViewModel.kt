package com.github.anastr.myscore.viewmodel

import androidx.lifecycle.*
import com.github.anastr.myscore.CourseMode
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
): ViewModel() {

    private val data = MutableLiveData<CourseMode>()

    val course: LiveData<Course?> = Transformations.switchMap(data) { mode ->
        when (mode) {
            is CourseMode.Edit -> courseRepository.getCourse(mode.courseId)
            is CourseMode.New -> MutableLiveData<Course?>(
                Course(
                    yearId = mode.yearId,
                    semester = mode.semester,
                    name = "",
                    hasPractical = true,
                    hasTheoretical = true,
                    theoreticalScore = 0,
                    practicalScore = 0,
                )
            )
            else -> throw IllegalArgumentException("Mode not supported")
        }
    }

    fun setInput(courseMode: CourseMode) {
        data.value = courseMode
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
}