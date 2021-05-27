package com.github.anastr.myscore.hilt

import com.github.anastr.myscore.room.AppDatabase
import com.github.anastr.myscore.room.dao.CourseDao
import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.dao.YearDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DaoModule {

    @Provides
    fun provideDatabaseDao(appDatabase: AppDatabase): DatabaseDao
            = appDatabase.databaseDao()

    @Provides
    fun provideYearDao(appDatabase: AppDatabase): YearDao
            = appDatabase.yearDao()

    @Provides
    fun provideCourseDao(appDatabase: AppDatabase): CourseDao
            = appDatabase.courseDao()
}
