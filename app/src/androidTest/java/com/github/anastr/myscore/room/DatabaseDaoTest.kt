package com.github.anastr.myscore.room

import com.github.anastr.data.datasource.DatabaseDao
import com.github.anastr.data.room.AppDatabase
import com.github.anastr.domain.constant.MAX_YEARS
import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Course
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class DatabaseDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var databaseDao: DatabaseDao
    @Inject
    lateinit var db: AppDatabase

    @Before
    fun createDb() {
        hiltRule.inject()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertOneYear() = runBlocking {
        databaseDao.insertNewYear(MAX_YEARS)
        assertEquals(databaseDao.getAllYears().size, 1)
    }

    @Test
    fun testRapidInsertYears() = runBlocking {
        // Try to insert more than MAX_YEARS
        for (i in 1.. MAX_YEARS + 2)
            databaseDao.insertNewYear(MAX_YEARS)
        // Only MAX_YEARS is allowed
        assertEquals(databaseDao.getAllYears().size, MAX_YEARS)
    }

    @Test
    fun testDeleteYear() = runBlocking {
        databaseDao.insertNewYear(MAX_YEARS)
        val year = databaseDao.getAllYears().first()
        databaseDao.insertAll(
            Course(
                yearId = year.uid,
                semester = Semester.FirstSemester,
                name = "",
                hasPractical = true,
                hasTheoretical = true,
            )
        )
        assertEquals(databaseDao.getAllYears().size, 1)
        assertEquals(databaseDao.getAllCourses().size, 1)

        databaseDao.deleteYear(year)
        assertEquals(databaseDao.getAllYears().size, 0)
        assertEquals(databaseDao.getAllCourses().size, 0)
    }
}
