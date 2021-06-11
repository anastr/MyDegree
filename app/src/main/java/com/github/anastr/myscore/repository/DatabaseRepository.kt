package com.github.anastr.myscore.repository

import com.github.anastr.myscore.firebase.documents.DegreeDocument
import com.github.anastr.myscore.firebase.toCourse
import com.github.anastr.myscore.firebase.toYear
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

    suspend fun replaceData(degreeDocument: DegreeDocument) =
        databaseDao.replaceData(
            years = degreeDocument.years!!.map { it.toYear() },
            courses = degreeDocument.courses!!.map { it.toCourse() },
        )
}
