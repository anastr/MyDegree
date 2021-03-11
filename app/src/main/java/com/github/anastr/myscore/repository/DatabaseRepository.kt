package com.github.anastr.myscore.repository

import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Year
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DatabaseRepository @Inject constructor (
    private val databaseDao: DatabaseDao,
) {

    suspend fun insertAll(vararg years: Year) = databaseDao.insertAll(*years)

    suspend fun insertAll(vararg courses: Course) = databaseDao.insertAll(*courses)

    suspend fun getYears() = databaseDao.getAllYears()

    suspend fun getCourses() = databaseDao.getAllCourses()

    suspend fun deleteYear(year: Year) = databaseDao.deleteYear(year)

    suspend fun deleteAll() = databaseDao.deleteAll()

    fun getYearsCount(): Flow<Int> = databaseDao.getYearsCount()

}
