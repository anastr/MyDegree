package com.github.anastr.myscore.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.firebase.toHashMap
import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.util.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.myscore.util.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive

@HiltWorker
class UploadBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val databaseDao: DatabaseDao,
): CoroutineWorker(appContext, workerParameters) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun doWork(): Result = coroutineScope {
        if (auth.currentUser != null) {
            try {
                val years = databaseDao.getAllYears()
                val courses = databaseDao.getAllCourses()
                val degreeDocument = DegreeDocument().apply {
                    this.years = years.map { it.toHashMap() }
                    this.courses = courses.map { it.toHashMap() }
                }
                val db = Firebase.firestore
                val docRef = db.collection(FIRESTORE_DEGREES_COLLECTION)
                    .document(auth.currentUser!!.uid)
                ensureActive()
                db.runTransaction { transaction ->
                    transaction.set(docRef, degreeDocument)
                }.await()
                Log.i(Tag, "Succeeded")
                Result.success()
            }
            catch (e: Exception) {
                e.printStackTrace()
                Log.i(Tag, "Failed: ${e.message}")
                Result.failure()
            }
        }
        else {
            Log.i(Tag, "User not registered")
            Result.success()
        }
    }

    companion object {
        const val UNIQUE_NAME = "uploadBackupToFirestoreWorker"
        private const val Tag = "uploadBackupWorker"
    }
}
