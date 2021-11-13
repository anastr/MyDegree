package com.github.anastr.data.datasource

import com.github.anastr.data.hilt.DefaultDispatcher
import com.github.anastr.data.mappers.toHashMap
import com.github.anastr.data.models.DegreeDocument
import com.github.anastr.data.utils.await
import com.github.anastr.domain.constant.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.Year
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    @Throws(Exception::class)
    suspend fun firebaseAuthWithGoogle(idToken: String): AuthResult =
        withContext(defaultDispatcher) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            return@withContext auth.signInWithCredential(credential).await()
        }

    @Throws(Exception::class)
    suspend fun sendBackup(years: List<Year>, courses: List<Course>): Unit =
        withContext(defaultDispatcher) {
            val db = Firebase.firestore
            val degreeDocument = DegreeDocument().apply {
                this.years = years.map { it.toHashMap() }
                this.courses = courses.map { it.toHashMap() }
            }
            val docRef =
                db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            db.runTransaction { transaction ->
                transaction.set(docRef, degreeDocument)
            }.await()
        }

    @Throws(Exception::class)
    internal suspend fun receiveBackup(): DegreeDocument = withContext(defaultDispatcher) {
        val db = Firebase.firestore
        val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
        val documentSnapshot = docRef.get().await()
        return@withContext if (documentSnapshot.exists()) {
            documentSnapshot.toObject(DegreeDocument::class.java)!!
        } else {
            DegreeDocument()
        }
    }

    @Throws(Exception::class)
    suspend fun deleteBackup(): Unit = withContext(defaultDispatcher) {
        val db = Firebase.firestore
        val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
        db.runTransaction { transaction ->
            transaction.delete(docRef)
        }.await()
    }
}
