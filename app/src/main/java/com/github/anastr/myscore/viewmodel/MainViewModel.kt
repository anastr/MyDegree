package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.data.hilt.DefaultDispatcher
import com.github.anastr.domain.entities.db.UniversityDataEntity
import com.github.anastr.domain.repositories.FirebaseRepo
import com.github.anastr.domain.repositories.MainDatabaseRepo
import com.github.anastr.myscore.util.stringFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: MainDatabaseRepo,
    private val firebaseRepository: FirebaseRepo,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loadingFlow

    private val _firebaseStateFlow = MutableSharedFlow<FirebaseState>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val firebaseStateFlow: Flow<FirebaseState> = _firebaseStateFlow

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

    fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launchLoading {
            _firebaseStateFlow.tryEmit(
                safeFirebaseCall {
                    val userName = firebaseRepository.firebaseAuthWithGoogle(idToken)
                    FirebaseState.GoogleLoginSucceeded(userName ?: "")
                }
            )
        }
    }

    fun sendBackup() {
        viewModelScope.launchLoading {
            _firebaseStateFlow.tryEmit(
                safeFirebaseCall {
                    val years = databaseRepository.getYears()
                    val courses = databaseRepository.getCourses()
                    firebaseRepository.sendBackup(years = years, courses = courses)
                    FirebaseState.SendBackupSucceeded
                }
            )
        }
    }

    fun receiveBackup() {
        viewModelScope.launchLoading {
            _firebaseStateFlow.tryEmit(
                safeFirebaseCall {
                    val remoteData = firebaseRepository.receiveBackup()
                    if (remoteData.years.isEmpty() && remoteData.courses.isEmpty()) {
                        FirebaseState.Error(ErrorCode.NoDataOnServer)
                    } else {
                        saveDataFromFireStore(remoteData)
                    }
                }
            )
        }
    }

    private suspend fun saveDataFromFireStore(data: UniversityDataEntity): FirebaseState =
        try {
            databaseRepository.replaceData(data)
            FirebaseState.ReceiveBackupSucceeded
        } catch (e: Exception) {
            FirebaseState.Error(ErrorCode.DataCorrupted)
        }

    fun deleteBackup() {
        viewModelScope.launchLoading {
            _firebaseStateFlow.tryEmit(
                safeFirebaseCall {
                    firebaseRepository.deleteBackup()
                    FirebaseState.DeleteBackupSucceeded
                }
            )
        }
    }

    private inline fun CoroutineScope.launchLoading(crossinline block: suspend CoroutineScope.() -> Unit) =
        launch {
            _loadingFlow.value = true
            block()
            _loadingFlow.value = false
        }
}

sealed class FirebaseState {
    class GoogleLoginSucceeded(val userName: String) : FirebaseState()
    class Error(val errorCode: ErrorCode) : FirebaseState()
    class FirestoreError(val exception: Exception) : FirebaseState()
    object SendBackupSucceeded : FirebaseState()
    object ReceiveBackupSucceeded : FirebaseState()
    object DeleteBackupSucceeded : FirebaseState()
}

enum class ErrorCode {
    NoDataOnServer, DataCorrupted
}

inline fun safeFirebaseCall(block: () -> FirebaseState): FirebaseState =
    try {
        block()
    } catch (e: Exception) {
        FirebaseState.FirestoreError(e)
    }
