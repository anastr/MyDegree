package com.github.anastr.myscore.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.MAX_YEARS
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DatabaseDaoTest {

    private lateinit var databaseDao: DatabaseDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        databaseDao = db.databaseDao()
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
