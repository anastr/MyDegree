package com.github.anastr.myscore.viewmodel

import androidx.lifecycle.*
import com.github.anastr.myscore.CourseMode
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class CourseViewModel @AssistedInject constructor(
    private val courseRepository: CourseRepository,
    @Assisted private val courseMode: CourseMode,
): ViewModel() {

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

}

@AssistedFactory
interface CourseViewModelFactory {
    fun create(courseMode: CourseMode): CourseViewModel
}

fun CourseViewModelFactory.provideFactory(
    courseMode: CourseMode,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return create(courseMode) as T
    }
}
