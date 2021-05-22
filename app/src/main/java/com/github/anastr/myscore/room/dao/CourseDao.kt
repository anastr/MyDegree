package com.github.anastr.myscore.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
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