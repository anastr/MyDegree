package com.github.anastr.myscore.room.dao

import androidx.room.*
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Year

@Dao
interface DatabaseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg data: Year)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg data: Course)

    @Query("SELECT * FROM year")
    suspend fun getAllYears(): List<Year>

    @Query("SELECT * FROM course")
    suspend fun getAllCourses(): List<Course>

    @Transaction
    suspend fun deleteYear(year: Year) {
        deleteYear(year.uid)
        deleteSubjects(year.uid)
    }

    @Query("DELETE FROM year WHERE uid = :yearId")
    suspend fun deleteYear(yearId: Long)

    @Query("DELETE FROM course WHERE year_id = :yearId")
    suspend fun deleteSubjects(yearId: Long)

    @Transaction
    suspend fun deleteAll() {
        deleteAllYears()
        deleteAllCourses()
    }

    @Query("DELETE FROM year")
    suspend fun deleteAllYears()

    @Query("DELETE FROM course")
    suspend fun deleteAllCourses()
}