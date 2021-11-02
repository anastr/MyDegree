package com.github.anastr.domain.repositories

import com.github.anastr.domain.entities.db.Course

interface WriteCourseRepo {

    suspend fun getCourse(courseId: Long): Course?

    suspend fun insertCourses(vararg courses: Course)

    suspend fun updateCourse(vararg courses: Course)

    suspend fun deleteCourse(course: Course)
}
