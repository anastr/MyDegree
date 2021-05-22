package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.repository.YearRepository
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.intFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class YearsState {
    object Loading : YearsState()
    class Success(val data: List<YearWithSemester>) : YearsState()
    class Error(val error: Exception) : YearsState()
}

@ExperimentalCoroutinesApi
@HiltViewModel
class YearViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
    private val yearRepository: YearRepository,
    defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val passDegree: SharedFlow<Int> =
        sharedPreferences.intFlow("passDegree", 60)
        // Do preference job on a worker thread.
        .flowOn(defaultDispatcher)
        // Share the same instance of passDegree Flow between yearsFlow and finalDegreeFlow.
        .shareIn(
            scope = viewModelScope,
            // Stop this flow immediately when there are no subscribers,
            // and let the down StateFlows control the time to stop it.
            started = SharingStarted.WhileSubscribed(),
            replay = 1,
        )

    val yearsFlow: StateFlow<YearsState> = passDegree
        .flatMapLatest { yearRepository.getYearsOrdered(it) }
        .map { YearsState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = YearsState.Loading
        )

    val finalDegreeFlow: StateFlow<Float> = passDegree
        .flatMapLatest { yearRepository.getFinalDegree(it) }
        .map { it ?: 0f }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    fun updateYears(vararg years: Year) =
        viewModelScope.launch { yearRepository.updateYears(*years) }

    fun deleteYear(year: Year) = viewModelScope.launch { databaseRepository.deleteYear(year) }
}
