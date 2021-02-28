package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.repository.YearRepository
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.intLiveData
import com.github.anastr.myscore.util.stringLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class YearViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
    private val yearRepository: YearRepository,
) : ViewModel() {

    private val passDegreeLiveData: LiveData<Int> =
        sharedPreferences.intLiveData("passDegree", 60)

    val themeLiveData: LiveData<String> =
        sharedPreferences.stringLiveData("themePref", "-1")

    val years: LiveData<List<YearWithSemester>> = Transformations.switchMap(passDegreeLiveData) {
        yearRepository.getYearsOrdered(it)
    }

    val finalDegree = Transformations.switchMap(passDegreeLiveData) {
        yearRepository.getFinalDegree(it)
    }

    val yearsCount: LiveData<Int>
            = yearRepository.getYearsCount()

    suspend fun getYears() = databaseRepository.getYears()

    suspend fun getCourses() = databaseRepository.getCourses()

    suspend fun insertYears(vararg years: Year) = databaseRepository.insertAll(*years)

    suspend fun insertCourses(vararg courses: Course) = databaseRepository.insertAll(*courses)

    suspend fun updateYears(vararg years: Year) = yearRepository.updateYears(*years)

    suspend fun deleteYear(year: Year) = databaseRepository.deleteYear(year)

    suspend fun deleteAll() = databaseRepository.deleteAll()

}
