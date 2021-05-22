package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.repository.FirebaseRepository
import com.github.anastr.myscore.util.stringFlow
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
    private val firebaseRepository: FirebaseRepository,
    defaultDispatcher: CoroutineDispatcher,
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
        _loadingFlow.value = true
        viewModelScope.launch {
            try {
                val user = firebaseRepository.firebaseAuthWithGoogle(idToken)
                _firebaseStateFlow.tryEmit(FirebaseState.GoogleLoginSucceeded(user))
            } catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun sendBackup() {
        _loadingFlow.value = true
        viewModelScope.launch {
            val years = databaseRepository.getYears()
            val courses = databaseRepository.getCourses()
            try {
                firebaseRepository.sendBackup(years = years, courses = courses)
                _firebaseStateFlow.tryEmit(FirebaseState.SendBackupSucceeded)
            } catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun receiveBackup() {
        _loadingFlow.value = true
        viewModelScope.launch {
            try {
                val documentSnapshot = firebaseRepository.receiveBackup()
                if (documentSnapshot.exists()) {
                    val degreeDocument = documentSnapshot.toObject(DegreeDocument::class.java)
                    saveDataFromFireStore(degreeDocument!!)
                } else {
                    _firebaseStateFlow.tryEmit(FirebaseState.Error(ErrorCode.NoDataOnServer))
                }
            } catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    private suspend fun saveDataFromFireStore(degreeDocument: DegreeDocument) {
        try {
            databaseRepository.replaceData(degreeDocument)
            _firebaseStateFlow.tryEmit(FirebaseState.ReceiveBackupSucceeded)
        } catch (e: Exception) {
            _firebaseStateFlow.tryEmit(FirebaseState.Error(ErrorCode.DataCorrupted))
        }
    }

    fun deleteBackup() {
        _loadingFlow.value = true
        viewModelScope.launch {
            try {
                firebaseRepository.deleteBackup()
                _firebaseStateFlow.tryEmit(FirebaseState.DeleteBackupSucceeded)
            } catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            } finally {
                _loadingFlow.value = false
            }
        }
    }
}

sealed class FirebaseState {
    class GoogleLoginSucceeded(val user: FirebaseUser) : FirebaseState()
    class Error(val errorCode: ErrorCode) : FirebaseState()
    class FirestoreError(val exception: Exception) : FirebaseState()
    object SendBackupSucceeded : FirebaseState()
    object ReceiveBackupSucceeded : FirebaseState()
    object DeleteBackupSucceeded : FirebaseState()
}

enum class ErrorCode {
    NoDataOnServer, DataCorrupted
}
