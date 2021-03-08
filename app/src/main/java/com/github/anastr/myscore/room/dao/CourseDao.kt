package com.github.anastr.myscore.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao: BaseDao<Course> {

    @Query("SELECT * FROM course WHERE uid = (:subjectId)")
    fun getById(subjectId: Long): Flow<Course?>

    @Query("SELECT * FROM course WHERE year_id = (:yearId) AND semester = (:semester)")
    fun getAll(yearId: Long, semester: Semester): Flow<List<Course>>

//    @Query("SELECT * FROM subject WHERE uid IN (:subjectIds)")
//    fun loadAllByIds(subjectIds: LongArray): List<Subject>

}