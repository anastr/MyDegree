package com.github.anastr.data.datasource

import androidx.room.*
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.Year

@Dao
interface DatabaseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg data: Year)

    @Query("""
        INSERT INTO year (year_order)
        SELECT (SELECT COUNT() FROM year)
        WHERE NOT EXISTS (SELECT 1 FROM year WHERE year_order = :maxYears - 1);
    """)
    suspend fun insertNewYear(maxYears: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg data: Course)

    @Query("SELECT * FROM year ORDER BY year_order ASC")
    suspend fun getAllYears(): List<Year>

    @Query("SELECT * FROM course")
    suspend fun getAllCourses(): List<Course>

    @Transaction
    suspend fun deleteYear(year: Year) {
        deleteYear(year.uid)
        deleteCourse(year.uid)
        val years = getAllYears().mapIndexed { index, y -> y.copy(order = index) }
        updateAll(*years.toTypedArray())
    }

    @Transaction
    suspend fun replaceData(years: List<Year>, courses: List<Course>) {
        deleteAll()
        insertAll(*years.toTypedArray())
        insertAll(*courses.toTypedArray())
    }

    @Update
    suspend fun updateAll(vararg years: Year)

    @Query("DELETE FROM year WHERE uid = :yearId")
    suspend fun deleteYear(yearId: Long)

    @Query("DELETE FROM course WHERE year_id = :yearId")
    suspend fun deleteCourse(yearId: Long)

    @Transaction
    private suspend fun deleteAll() {
        deleteAllYears()
        deleteAllCourses()
    }

    @Query("DELETE FROM year")
    suspend fun deleteAllYears()

    @Query("DELETE FROM course")
    suspend fun deleteAllCourses()
}