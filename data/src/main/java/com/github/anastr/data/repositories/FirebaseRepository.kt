package com.github.anastr.data.repositories

import com.github.anastr.data.models.DegreeDocument
import com.github.anastr.domain.constant.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.UniversityDataEntity
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.repositories.FirebaseRepo
import com.github.anastr.data.mappers.toCourse
import com.github.anastr.data.mappers.toHashMap
import com.github.anastr.data.mappers.toYear
import com.github.anastr.data.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class FirebaseRepository(
    private val defaultDispatcher: CoroutineDispatcher,
): FirebaseRepo {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    @Throws(Exception::class)
    override suspend fun firebaseAuthWithGoogle(idToken: String): String? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = withContext(defaultDispatcher) {
            auth.signInWithCredential(credential).await()
        }
        return result.user?.displayName
    }

    @Throws(Exception::class)
    override suspend fun sendBackup(years: List<Year>, courses: List<Course>) {
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
    override suspend fun receiveBackup(): UniversityDataEntity {
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
    override suspend fun deleteBackup() {
        return withContext(defaultDispatcher) {
            val db = Firebase.firestore
            val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION).document(auth.currentUser!!.uid)
            db.runTransaction { transaction ->
                transaction.delete(docRef)
            }.await()
        }
    }
}
