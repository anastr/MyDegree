package com.github.anastr.myscore.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.anastr.myscore.room.dao.CourseDao
import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.dao.YearDao
import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Year

@Database(entities = [Course::class, Year::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun databaseDao(): DatabaseDao
    abstract fun courseDao(): CourseDao
    abstract fun yearDao(): YearDao

}
