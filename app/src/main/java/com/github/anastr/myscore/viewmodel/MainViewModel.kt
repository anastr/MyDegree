package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.util.stringFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
    defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    @ExperimentalCoroutinesApi
    val themeFlow: SharedFlow<String> =
        sharedPreferences.stringFlow("themePref", "-1")
            .flowOn(defaultDispatcher)
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1
            )

    fun insertNewYear() = viewModelScope.launch { databaseRepository.insertNewYear() }
}
