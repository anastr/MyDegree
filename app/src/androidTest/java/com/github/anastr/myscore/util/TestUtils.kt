package com.github.anastr.myscore.util

import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.Semester

val testCoursesList = listOf(
    Course(
        uid = 1,
        yearId = 1,
        semester = Semester.FirstSemester,
        name = "Course 1",
        hasPractical = true,
        hasTheoretical = true,
    ),
    Course(
        uid = 2,
        yearId = 1,
        semester = Semester.FirstSemester,
        name = "Course 2",
        hasPractical = true,
        hasTheoretical = true,
    ),
    Course(
        uid = 3,
        yearId = 1,
        semester = Semester.FirstSemester,
        name = "Course 3",
        hasPractical = true,
        hasTheoretical = true,
    ),
)
val testCourse = testCoursesList[0]
