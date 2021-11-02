package com.github.anastr.domain.repositories

import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.UniversityDataEntity
import com.github.anastr.domain.entities.db.Year

interface MainDatabaseRepo {

    suspend fun insertNewYear()

    suspend fun getYears(): List<Year>

    suspend fun getCourses(): List<Course>

    suspend fun replaceData(degreeDocument: UniversityDataEntity)
}
