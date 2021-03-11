package com.github.anastr.myscore.room.dao

import androidx.room.*
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Year
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg data: Year)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg data: Course)

    @Query("SELECT * FROM year ORDER BY year_order ASC")
    suspend fun getAllYears(): List<Year>

    @Query("SELECT COUNT() FROM year")
    fun getYearsCount(): Flow<Int>

    @Query("SELECT * FROM course")
    suspend fun getAllCourses(): List<Course>

    @Transaction
    suspend fun deleteYear(year: Year) {
        deleteYear(year.uid)
        deleteSubjects(year.uid)
        val years = getAllYears()
        years.forEachIndexed { index, y -> y.order = index }
        updateAll(*years.toTypedArray())
    }

    @Update
    suspend fun updateAll(vararg years: Year)

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