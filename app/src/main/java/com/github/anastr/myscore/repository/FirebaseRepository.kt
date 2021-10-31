package com.github.anastr.myscore.repository

import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.UniversityDataEntity
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.firebase.toCourse
import com.github.anastr.myscore.firebase.toHashMap
import com.github.anastr.myscore.firebase.toYear
import com.github.anastr.myscore.util.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.myscore.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    suspend fun firebaseAuthWithGoogle(idToken: String): String? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = withContext(defaultDispatcher) {
            auth.signInWithCredential(credential).await()
        }
        return result.user?.displayName
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
    suspend fun receiveBackup(): UniversityDataEntity {
        return withContext(defaultDispatcher) {
            val db = Firebase.firestore
            val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            val documentSnapshot = docRef.get().await()
            val degreeDocument = if (documentSnapshot.exists()) {
                documentSnapshot.toObject(DegreeDocument::class.java)!!
            } else {
                DegreeDocument()
            }
            UniversityDataEntity(
                years = degreeDocument.years?.map { it.toYear() } ?: emptyList(),
                courses = degreeDocument.courses?.map { it.toCourse() } ?: emptyList(),
            )
        }
    }

    @Throws(Exception::class)
    suspend fun deleteBackup() {
        return withContext(defaultDispatcher) {
            val db = Firebase.firestore
            val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            db.runTransaction { transaction ->
                transaction.delete(docRef)
            }.await()
        }
    }
}
