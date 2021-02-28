package com.github.anastr.myscore.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.intLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class CoursesViewModelData(val yearId: Long, val semester: Semester)

@HiltViewModel
class AllCoursesViewModel @Inject constructor(
    application: Application,
    private val courseRepository: CourseRepository,
): ViewModel() {

    val passDegreeLiveData: LiveData<Int> =
        PreferenceManager.getDefaultSharedPreferences(application)
            .intLiveData("passDegree", 60)

    private val data = MutableLiveData<CoursesViewModelData>()

    val courses: LiveData<List<Course>> = Transformations.switchMap(data) {
            d -> courseRepository.getCourses(d.yearId, d.semester)
    }

    fun setInput(newData: CoursesViewModelData) {
        data.value = newData
    }

    suspend fun insertCourses(vararg courses: Course) = courseRepository.insertCourses(*courses)

    suspend fun deleteCourse(course: Course) = courseRepository.deleteCourse(course)
}