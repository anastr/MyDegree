package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.repository.YearRepository
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.intLiveData
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YearViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
    private val yearRepository: YearRepository,
) : ViewModel() {

    private val passDegreeLiveData: LiveData<Int> =
        sharedPreferences.intLiveData("passDegree", 60)

    val years: LiveData<List<YearWithSemester>> = Transformations.switchMap(passDegreeLiveData) {
        yearRepository.getYearsOrdered(it).asLiveData()
    }

    val finalDegree = Transformations.switchMap(passDegreeLiveData) {
        yearRepository.getFinalDegree(it)
            .distinctUntilChanged()
            .asLiveData()
    }

    fun updateYears(vararg years: Year) = viewModelScope.launch { yearRepository.updateYears(*years) }

    fun deleteYear(year: Year) = viewModelScope.launch { databaseRepository.deleteYear(year) }
}
