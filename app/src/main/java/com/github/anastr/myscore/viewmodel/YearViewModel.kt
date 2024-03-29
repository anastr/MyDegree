package com.github.anastr.myscore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.data.hilt.DefaultDispatcher
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.entities.db.YearWithSemester
import com.github.anastr.domain.repositories.PassDegreeRepo
import com.github.anastr.domain.repositories.YearRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class YearViewModel @Inject constructor(
    passDegreeRepo: PassDegreeRepo,
    private val yearRepository: YearRepo,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val passDegree: SharedFlow<Int> =
        passDegreeRepo.getPassDegree()
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

    val yearsFlow: StateFlow<State<List<YearWithSemester>>> = passDegree
        .flatMapLatest { yearRepository.getYearsOrdered(it) }
        .map { State.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = State.Loading
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

    fun deleteYear(year: Year) = viewModelScope.launch { yearRepository.deleteYear(year) }
}
