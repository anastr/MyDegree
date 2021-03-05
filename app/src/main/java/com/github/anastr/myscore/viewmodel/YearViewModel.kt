package com.github.anastr.myscore.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.firebase.toCourse
import com.github.anastr.myscore.firebase.toHashMap
import com.github.anastr.myscore.firebase.toYear
import com.github.anastr.myscore.repository.DatabaseRepository
import com.github.anastr.myscore.repository.YearRepository
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.myscore.util.intLiveData
import com.github.anastr.myscore.util.stringLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YearViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val databaseRepository: DatabaseRepository,
    private val yearRepository: YearRepository,
) : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val _firebaseState = MutableLiveData<FirebaseState>()
    val firebaseState: LiveData<FirebaseState> = _firebaseState

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

    fun insertYears(vararg years: Year) = viewModelScope.launch { databaseRepository.insertAll(*years) }

    fun updateYears(vararg years: Year) = viewModelScope.launch { yearRepository.updateYears(*years) }

    fun deleteYear(year: Year) = viewModelScope.launch { databaseRepository.deleteYear(year) }

    fun firebaseAuthWithGoogle(idToken: String) {
        _firebaseState.value = FirebaseState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = auth.currentUser
                _firebaseState.value = FirebaseState.GoogleLoginSucceeded(user!!)
            }
            .addOnFailureListener { e ->
                _firebaseState.value = FirebaseState.FirestoreError(e)
            }
    }

    fun sendBackup() {
        _firebaseState.value = FirebaseState.Loading
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
                    _firebaseState.value = FirebaseState.SendBackupSucceeded
                }
                .addOnFailureListener { e ->
                    _firebaseState.value = FirebaseState.FirestoreError(e)
                }
        }
    }

    fun receiveBackup() {
        _firebaseState.value = FirebaseState.Loading
        val db = Firebase.firestore
        val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
        docRef.get(Source.SERVER).addOnSuccessListener { documentSnapshot ->
            try {
                if (documentSnapshot.exists()) {
                    val degreeDocument = documentSnapshot.toObject(DegreeDocument::class.java)
                    saveDataFromFireStore(degreeDocument!!)
                }
                else {
                    _firebaseState.value = FirebaseState.Error(ErrorCode.NoDataOnServer)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                _firebaseState.value = FirebaseState.Error(ErrorCode.DataCorrupted)
            }
        }
            .addOnFailureListener { e ->
                _firebaseState.value = FirebaseState.FirestoreError(e)
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
            _firebaseState.value = FirebaseState.ReceiveBackupSucceeded
        }
    }

}

sealed class FirebaseState {
    object Loading : FirebaseState()
    class GoogleLoginSucceeded(val user: FirebaseUser) : FirebaseState()
    class Error(val errorCode: ErrorCode) : FirebaseState()
    class FirestoreError(val exception: Exception) : FirebaseState()
    object SendBackupSucceeded : FirebaseState()
    object ReceiveBackupSucceeded : FirebaseState()
}

enum class ErrorCode {
    NoDataOnServer, DataCorrupted
}
