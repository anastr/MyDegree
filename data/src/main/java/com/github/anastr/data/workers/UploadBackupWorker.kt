package com.github.anastr.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.anastr.data.datasource.DatabaseDao
import com.github.anastr.data.models.DegreeDocument
import com.github.anastr.domain.constant.FIRESTORE_DEGREES_COLLECTION
import com.github.anastr.data.mappers.toHashMap
import com.github.anastr.data.utils.await
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
