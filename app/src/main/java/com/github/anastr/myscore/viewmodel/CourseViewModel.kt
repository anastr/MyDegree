package com.github.anastr.myscore.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.anastr.myscore.CourseMode
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import dagger.hilt.android.lifecycle.HiltViewModel
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

    suspend fun insertCourse(course: Course) = courseRepository.insertCourses(course)

    suspend fun updateCourse(course: Course) = courseRepository.updateCourse(course)

    suspend fun deleteCourse(course: Course) = courseRepository.deleteCourse(course)
}