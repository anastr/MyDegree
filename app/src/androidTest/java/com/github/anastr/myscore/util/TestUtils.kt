package com.github.anastr.myscore.util

import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester

val testCourse = Course(
    uid = 1,
    yearId = 1,
    semester = Semester.FirstSemester,
    name = "Course 1",
    hasPractical = true,
    hasTheoretical = true,
)
