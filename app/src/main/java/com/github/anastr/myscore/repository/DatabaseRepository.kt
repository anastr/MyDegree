package com.github.anastr.myscore.repository

import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.util.MAX_YEARS
import javax.inject.Inject

class DatabaseRepository @Inject constructor (
    private val databaseDao: DatabaseDao,
) {

    suspend fun insertNewYear() = databaseDao.insertNewYear(MAX_YEARS)

    suspend fun getYears() = databaseDao.getAllYears()

    suspend fun getCourses() = databaseDao.getAllCourses()

    suspend fun deleteYear(year: Year) = databaseDao.deleteYear(year)
}
