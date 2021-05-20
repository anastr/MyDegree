package com.github.anastr.myscore.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import com.github.anastr.myscore.repository.CourseRepository
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.intLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoursesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    application: Application,
    courseRepository: CourseRepository,
): ViewModel() {

    private val yearId: Long = savedStateHandle.get(YEAR_ID_KEY)!!
    private val semester: Semester = savedStateHandle.get(SEMESTER_KEY)!!

    val passDegreeLiveData: LiveData<Int> =
        PreferenceManager.getDefaultSharedPreferences(application)
            .intLiveData("passDegree", 60)

    val courses: LiveData<List<Course>> = courseRepository.getCourses(yearId, semester).asLiveData()

    companion object {
        const val YEAR_ID_KEY = "yearId"
        const val SEMESTER_KEY = "semester"
    }
}
