package com.github.anastr.myscore.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.anastr.myscore.room.AppDatabase
import com.github.anastr.myscore.room.entity.Year
import kotlinx.coroutines.coroutineScope

class RoomInitWorker(
    appContext: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            AppDatabase.getInstance(applicationContext).yearDao().insertAll(
                Year(order = 0),
                Year(order = 1),
                Year(order = 2),
                Year(order = 3),
            )
            Result.success()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.failure()
        }
    }

}
