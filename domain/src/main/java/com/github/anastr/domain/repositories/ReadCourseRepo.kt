package com.github.anastr.domain.repositories

import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Course
import kotlinx.coroutines.flow.Flow

interface ReadCourseRepo {

    fun getCourses(yearId: Long, semester: Semester): Flow<List<Course>>
}
