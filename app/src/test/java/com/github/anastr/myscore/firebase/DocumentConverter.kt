package com.github.anastr.myscore.firebase

import com.github.anastr.data.mappers.toCourse
import com.github.anastr.data.mappers.toHashMap
import com.github.anastr.data.mappers.toYear
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Year
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentConverter {

    @Test
    fun testConvertYear() {
        val year = Year(
            order = 0,
        )
        val yearHashMap = year.toHashMap()
        // To make sure converting to hashMap will have all fields
        assertEquals(yearHashMap.size, Year::class.java.declaredFields.size)

        val yearFromHashMap = yearHashMap.toYear()
        assertEquals(year, yearFromHashMap)
    }

    @Test
    fun testConvertCourse() {
        val course = Course(
            yearId = 0,
            semester = Semester.FirstSemester,
            name = "",
            hasPractical = true,
            hasTheoretical = true,
        )
        val courseHashMap = course.toHashMap()
        // To make sure converting to hashMap will have all fields
        assertEquals(courseHashMap.size, Course::class.java.declaredFields.size)

        val courseFromHashMap = courseHashMap.toCourse()
        assertEquals(course, courseFromHashMap)
    }
}
