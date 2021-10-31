package com.github.anastr.myscore.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.anastr.myscore.room.dao.YearDao
import com.github.anastr.domain.entities.db.Year
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class RoomInitWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val yearDao: YearDao,
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            yearDao.insertAll(
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
