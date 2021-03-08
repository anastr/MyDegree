package com.github.anastr.myscore.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.intLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class CoursesViewModel @AssistedInject constructor(
    application: Application,
    private val courseRepository: CourseRepository,
    @Assisted private val yearId: Long,
    @Assisted private val semester: Semester,
): ViewModel() {

    val passDegreeLiveData: LiveData<Int> =
        PreferenceManager.getDefaultSharedPreferences(application)
            .intLiveData("passDegree", 60)

    val courses: LiveData<List<Course>> = courseRepository.getCourses(yearId, semester).asLiveData()

    fun insertCourses(vararg courses: Course) {
        viewModelScope.launch {
            courseRepository.insertCourses(*courses)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            courseRepository.deleteCourse(course)
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: CoursesViewModelFactory,
            yearId: Long,
            semester: Semester,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(yearId, semester) as T
            }
        }
    }
}

@AssistedFactory
interface CoursesViewModelFactory {
    fun create(yearId: Long, semester: Semester): CoursesViewModel
}
