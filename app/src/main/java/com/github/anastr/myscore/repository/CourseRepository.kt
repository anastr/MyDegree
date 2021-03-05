package com.github.anastr.myscore.repository

import com.github.anastr.myscore.room.dao.CourseDao
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CourseRepository @Inject constructor (
    private val courseDao: CourseDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getCourses(yearId: Long, semester: Semester) = courseDao.getAll(yearId, semester)

    fun getCourse(courseId: Long) = courseDao.getById(courseId)

    suspend fun insertCourses(vararg courses: Course) = withContext(defaultDispatcher) { courseDao.insertAll(*courses) }

    suspend fun updateCourse(vararg courses: Course) = withContext(defaultDispatcher) { courseDao.updateAll(*courses) }

    suspend fun deleteCourse(course: Course) = withContext(defaultDispatcher) { courseDao.delete(course) }
}
