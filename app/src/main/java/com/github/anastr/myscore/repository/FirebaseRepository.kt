package com.github.anastr.myscore.repository

import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.firebase.toHashMap
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.util.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.myscore.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseRepository @Inject constructor (
    private val defaultDispatcher: CoroutineDispatcher,
) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    @Throws(Exception::class)
    suspend fun firebaseAuthWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = withContext(defaultDispatcher) {
            auth.signInWithCredential(credential).await()
        }
        return result.user!!
    }

    @Throws(Exception::class)
    suspend fun sendBackup(years: List<Year>, courses: List<Course>) {
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
    }

    @Throws(Exception::class)
    suspend fun receiveBackup(): DocumentSnapshot {
        return withContext(defaultDispatcher) {
            val db = Firebase.firestore
            val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            docRef.get(Source.SERVER).await()
        }
    }

    @Throws(Exception::class)
    suspend fun deleteBackup() {
        return withContext(defaultDispatcher) {
            val db = Firebase.firestore
            val docRef =
                db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            db.runTransaction { transaction ->
                transaction.delete(docRef)
            }.await()
        }
    }
}
