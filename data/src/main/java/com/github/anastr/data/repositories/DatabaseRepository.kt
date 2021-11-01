package com.github.anastr.data.repositories

import com.github.anastr.data.datasource.DatabaseDao
import com.github.anastr.domain.constant.MAX_YEARS
import com.github.anastr.domain.entities.db.UniversityDataEntity
import com.github.anastr.domain.repositories.MainDatabaseRepo

internal class DatabaseRepository(
    private val databaseDao: DatabaseDao,
): MainDatabaseRepo {

    override suspend fun insertNewYear() = databaseDao.insertNewYear(MAX_YEARS)

    override suspend fun getYears() = databaseDao.getAllYears()

    override suspend fun getCourses() = databaseDao.getAllCourses()

    override suspend fun replaceData(newData: UniversityDataEntity) =
        databaseDao.replaceData(
            years = newData.years,
            courses = newData.courses,
        )
}
