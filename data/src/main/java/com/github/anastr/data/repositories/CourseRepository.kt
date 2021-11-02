package com.github.anastr.data.repositories

import com.github.anastr.data.datasource.CourseDao
import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.repositories.ReadCourseRepo
import com.github.anastr.domain.repositories.WriteCourseRepo
import kotlinx.coroutines.flow.Flow

internal class CourseRepository(
    private val courseDao: CourseDao,
): ReadCourseRepo, WriteCourseRepo {

    override fun getCourses(yearId: Long, semester: Semester): Flow<List<Course>> = courseDao.getAll(yearId, semester)

    override suspend fun getCourse(courseId: Long): Course? = courseDao.getById(courseId)

    override suspend fun insertCourses(vararg courses: Course) = courseDao.insertAll(*courses)

    override suspend fun updateCourse(vararg courses: Course) = courseDao.updateAll(*courses)

    override suspend fun deleteCourse(course: Course) = courseDao.delete(course)
}
