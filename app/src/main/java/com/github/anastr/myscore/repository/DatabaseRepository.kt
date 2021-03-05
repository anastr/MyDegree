package com.github.anastr.myscore.repository

import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Year
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor (
    private val databaseDao: DatabaseDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun insertAll(vararg years: Year) = withContext(defaultDispatcher) { databaseDao.insertAll(*years) }

    suspend fun insertAll(vararg courses: Course) = withContext(defaultDispatcher) { databaseDao.insertAll(*courses) }

    suspend fun getYears() = withContext(defaultDispatcher) { databaseDao.getAllYears() }

    suspend fun getCourses() = withContext(defaultDispatcher) { databaseDao.getAllCourses() }

    suspend fun deleteYear(year: Year) = withContext(defaultDispatcher) { databaseDao.deleteYear(year) }

    suspend fun deleteAll() = withContext(defaultDispatcher) { databaseDao.deleteAll() }

}
