package com.github.anastr.data.repositories

import com.github.anastr.data.datasource.FirebaseDataSource
import com.github.anastr.data.mappers.toCourse
import com.github.anastr.data.mappers.toYear
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.UniversityDataEntity
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.repositories.FirebaseRepo

internal class FirebaseRepository(
    private val firebaseDataSource: FirebaseDataSource,
): FirebaseRepo {

    @Throws(Exception::class)
    override suspend fun firebaseAuthWithGoogle(idToken: String): String? =
        firebaseDataSource.firebaseAuthWithGoogle(idToken).user?.displayName

    @Throws(Exception::class)
    override suspend fun sendBackup(years: List<Year>, courses: List<Course>) =
        firebaseDataSource.sendBackup(years, courses)

    @Throws(Exception::class)
    override suspend fun receiveBackup(): UniversityDataEntity {
        val degreeDocument = firebaseDataSource.receiveBackup()
        return UniversityDataEntity(
            years = degreeDocument.years?.map { it.toYear() } ?: emptyList(),
            courses = degreeDocument.courses?.map { it.toCourse() } ?: emptyList(),
        )
    }

    @Throws(Exception::class)
    override suspend fun deleteBackup() =
        firebaseDataSource.deleteBackup()
}
