package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.repository.FirebaseRepository
import com.github.anastr.myscore.util.stringLiveData
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
    private val firebaseRepository: FirebaseRepository,
): ViewModel() {

    private val _loadingLiveData = MutableLiveData(false)
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    private val _firebaseStateFlow = MutableSharedFlow<FirebaseState>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val firebaseStateFlow: Flow<FirebaseState> =
        _firebaseStateFlow.onEach { _loadingLiveData.value = false }

    val themeLiveData: LiveData<String> =
        sharedPreferences.stringLiveData("themePref", "-1")

    fun insertNewYear() = viewModelScope.launch { databaseRepository.insertNewYear() }

    fun firebaseAuthWithGoogle(idToken: String) {
        _loadingLiveData.value = true
        viewModelScope.launch {
            try {
                val user = firebaseRepository.firebaseAuthWithGoogle(idToken)
                _firebaseStateFlow.tryEmit(FirebaseState.GoogleLoginSucceeded(user))
            }
            catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            }
        }
    }

    fun sendBackup() {
        _loadingLiveData.value = true
        viewModelScope.launch {
            val years = databaseRepository.getYears()
            val courses = databaseRepository.getCourses()
            try {
                firebaseRepository.sendBackup(years = years, courses = courses)
                _firebaseStateFlow.tryEmit(FirebaseState.SendBackupSucceeded)
            }
            catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            }
        }
    }

    fun receiveBackup() {
        _loadingLiveData.value = true
        viewModelScope.launch {
            try {
                val documentSnapshot = firebaseRepository.receiveBackup()
                if (documentSnapshot.exists()) {
                    val degreeDocument = documentSnapshot.toObject(DegreeDocument::class.java)
                    saveDataFromFireStore(degreeDocument!!)
                } else {
                    _firebaseStateFlow.tryEmit(FirebaseState.Error(ErrorCode.NoDataOnServer))
                }
            } catch (e:Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            }
        }
    }

    private fun saveDataFromFireStore(degreeDocument: DegreeDocument) {
        viewModelScope.launch {
            try {
                databaseRepository.replaceData(degreeDocument)
                _firebaseStateFlow.tryEmit(FirebaseState.ReceiveBackupSucceeded)
            }
            catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.Error(ErrorCode.DataCorrupted))
            }
        }
    }

    fun deleteBackup() {
        _loadingLiveData.value = true
        viewModelScope.launch {
            try {
                firebaseRepository.deleteBackup()
                _firebaseStateFlow.tryEmit(FirebaseState.DeleteBackupSucceeded)
            }
            catch (e: Exception) {
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
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
