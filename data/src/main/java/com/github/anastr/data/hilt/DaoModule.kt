package com.github.anastr.data.hilt

import com.github.anastr.data.datasource.CourseDao
import com.github.anastr.data.datasource.DatabaseDao
import com.github.anastr.data.datasource.YearDao
import com.github.anastr.data.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DaoModule {

    @Provides
    @Singleton
    fun provideDatabaseDao(appDatabase: AppDatabase): DatabaseDao
            = appDatabase.databaseDao()

    @Provides
    @Singleton
    fun provideYearDao(appDatabase: AppDatabase): YearDao
            = appDatabase.yearDao()

    @Provides
    @Singleton
    fun provideCourseDao(appDatabase: AppDatabase): CourseDao
            = appDatabase.courseDao()
}
