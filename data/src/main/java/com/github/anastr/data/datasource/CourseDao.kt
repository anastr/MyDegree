package com.github.anastr.data.datasource

import androidx.room.Dao
import androidx.room.Query
import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Course
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao: BaseDao<Course> {

    @Query("SELECT * FROM course WHERE uid = :courseId LIMIT 1")
    suspend fun getById(courseId: Long): Course?

    @Query("SELECT * FROM course WHERE year_id = (:yearId) AND semester = (:semester)")
    fun getAll(yearId: Long, semester: Semester): Flow<List<Course>>

//    @Query("SELECT * FROM course WHERE uid IN (:courseIds)")
//    fun loadAllByIds(courseIds: LongArray): Flow<List<Course>>

}