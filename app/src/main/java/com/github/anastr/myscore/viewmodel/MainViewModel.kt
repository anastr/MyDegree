package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.firebase.toCourse
import com.github.anastr.myscore.firebase.toHashMap
import com.github.anastr.myscore.firebase.toYear
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.util.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.myscore.util.stringLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
): ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _loadingLiveData = MutableLiveData(false)
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    private val _firebaseStateFlow = MutableSharedFlow<FirebaseState>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val firebaseStateFlow: Flow<FirebaseState> =
        _firebaseStateFlow
            .onEach { _loadingLiveData.value = false }

    val themeLiveData: LiveData<String> =
        sharedPreferences.stringLiveData("themePref", "-1")

    val yearsCount: LiveData<Int> = databaseRepository.getYearsCount()
        .distinctUntilChanged()
        .asLiveData()

    fun insertYears(vararg years: Year) = viewModelScope.launch { databaseRepository.insertAll(*years) }

    fun firebaseAuthWithGoogle(idToken: String) {
        _loadingLiveData.value = true
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = auth.currentUser
                _firebaseStateFlow.tryEmit(FirebaseState.GoogleLoginSucceeded(user!!))
            }
            .addOnFailureListener { e ->
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            }
    }

    fun sendBackup() {
        _loadingLiveData.value = true
        viewModelScope.launch {
            val db = Firebase.firestore
            val years = databaseRepository.getYears()
            val courses = databaseRepository.getCourses()
            val degreeDocument = DegreeDocument().apply {
                this.years = years.map { it.toHashMap() }
                this.courses = courses.map { it.toHashMap() }
            }
            val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            db.runTransaction { transaction ->
                transaction.set(docRef, degreeDocument)
            }
                .addOnSuccessListener {
                    _firebaseStateFlow.tryEmit(FirebaseState.SendBackupSucceeded)
                }
                .addOnFailureListener { e ->
                    _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
                }
        }
    }

    fun receiveBackup() {
        _loadingLiveData.value = true
        val db = Firebase.firestore
        val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
        docRef.get(Source.SERVER).addOnSuccessListener { documentSnapshot ->
            try {
                if (documentSnapshot.exists()) {
                    val degreeDocument = documentSnapshot.toObject(DegreeDocument::class.java)
                    saveDataFromFireStore(degreeDocument!!)
                } else {
                    _firebaseStateFlow.tryEmit(FirebaseState.Error(ErrorCode.NoDataOnServer))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _firebaseStateFlow.tryEmit(FirebaseState.Error(ErrorCode.DataCorrupted))
            }
        }
            .addOnFailureListener { e ->
                _firebaseStateFlow.tryEmit(FirebaseState.FirestoreError(e))
            }
    }

    private fun saveDataFromFireStore(degreeDocument: DegreeDocument) {
        viewModelScope.launch {
            databaseRepository.deleteAll()
            if (degreeDocument.years != null) {
                val years = degreeDocument.years!!.map { it.toYear() }
                databaseRepository.insertAll(*years.toTypedArray())
            }
            if (degreeDocument.courses != null) {
                val courses = degreeDocument.courses!!.map { it.toCourse() }
                databaseRepository.insertAll(*courses.toTypedArray())
            }
            _firebaseStateFlow.tryEmit(FirebaseState.ReceiveBackupSucceeded)
        }
    }

    fun deleteBackup() {
        _loadingLiveData.value = true
        viewModelScope.launch {
            val db = Firebase.firestore
            val docRef =
                db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            db.runTransaction { transaction ->
                transaction.delete(docRef)
            }
                .addOnSuccessListener {
                    _firebaseStateFlow.tryEmit(FirebaseState.DeleteBackupSucceeded)
                }
                .addOnFailureListener { e ->
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
