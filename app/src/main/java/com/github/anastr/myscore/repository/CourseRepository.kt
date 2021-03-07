package com.github.anastr.myscore.repository

import com.github.anastr.myscore.room.dao.CourseDao
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import javax.inject.Inject

class CourseRepository @Inject constructor (
    private val courseDao: CourseDao,
) {

    fun getCourses(yearId: Long, semester: Semester) = courseDao.getAll(yearId, semester)

    fun getCourse(courseId: Long) = courseDao.getById(courseId)

    suspend fun insertCourses(vararg courses: Course) = courseDao.insertAll(*courses)

    suspend fun updateCourse(vararg courses: Course) = courseDao.updateAll(*courses)

    suspend fun deleteCourse(course: Course) = courseDao.delete(course)
}
