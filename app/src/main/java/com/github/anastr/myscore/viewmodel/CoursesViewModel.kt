package com.github.anastr.myscore.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.intLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class CoursesViewModel @AssistedInject constructor(
    application: Application,
    courseRepository: CourseRepository,
    @Assisted private val yearId: Long,
    @Assisted private val semester: Semester,
): ViewModel() {

    val passDegreeLiveData: LiveData<Int> =
        PreferenceManager.getDefaultSharedPreferences(application)
            .intLiveData("passDegree", 60)

    val courses: LiveData<List<Course>> = courseRepository.getCourses(yearId, semester).asLiveData()

}

@AssistedFactory
interface CoursesViewModelFactory {
    fun create(yearId: Long, semester: Semester): CoursesViewModel
}

fun CoursesViewModelFactory.provideFactory(
    yearId: Long,
    semester: Semester,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return create(yearId, semester) as T
    }
}
