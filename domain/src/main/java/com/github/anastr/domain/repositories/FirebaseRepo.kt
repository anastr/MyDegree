package com.github.anastr.domain.repositories

import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.UniversityDataEntity
import com.github.anastr.domain.entities.db.Year

interface FirebaseRepo {

    @Throws(Exception::class)
    suspend fun firebaseAuthWithGoogle(idToken: String): String?

    @Throws(Exception::class)
    suspend fun sendBackup(years: List<Year>, courses: List<Course>)

    @Throws(Exception::class)
    suspend fun receiveBackup(): UniversityDataEntity

    @Throws(Exception::class)
    suspend fun deleteBackup()
}
