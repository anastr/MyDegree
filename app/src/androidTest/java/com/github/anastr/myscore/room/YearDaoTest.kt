package com.github.anastr.myscore.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.dao.YearDao
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.util.MAX_YEARS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class YearDaoTest {

    private lateinit var databaseDao: DatabaseDao
    private lateinit var yearDao: YearDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        databaseDao = db.databaseDao()
        yearDao = db.yearDao()

        for (i in 1..MAX_YEARS)
            databaseDao.insertNewYear(MAX_YEARS)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testYearOrder() = runBlocking {
        val years = yearDao.getAllOrdered(60).first()
        assertEquals(years.size, MAX_YEARS)
        years.forEachIndexed { index, yearWithSemester ->
            assertEquals(index, yearWithSemester.year.order)
        }
    }

    @Test
    fun testFinalDegree() = runBlocking {
        val years = yearDao.getAllOrdered(60).first()
        databaseDao.insertAll(
            Course(
                yearId = years[0].year.uid,
                semester = Semester.FirstSemester,
                name = "Course 1",
                hasPractical = true,
                hasTheoretical = true,
                theoreticalScore = 50,
                practicalScore = 30,
            ),
            Course(
                yearId = years[1].year.uid,
                semester = Semester.SecondSemester,
                name = "Course 2",
                hasPractical = true,
                hasTheoretical = true,
                theoreticalScore = 50,
                practicalScore = 20,
            ),
        )
        val finalDegreeOnPassDegree60 = yearDao.getFinalDegree(60).first()
        assertNotNull(finalDegreeOnPassDegree60)
        assertEquals(finalDegreeOnPassDegree60, 75f)

        val finalDegreeOnPassDegree71 = yearDao.getFinalDegree(71).first()
        assertNotNull(finalDegreeOnPassDegree71)
        assertEquals(finalDegreeOnPassDegree71, 80f)
    }
}
