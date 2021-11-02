package com.github.anastr.data.hilt

import com.github.anastr.data.datasource.CourseDao
import com.github.anastr.data.datasource.DatabaseDao
import com.github.anastr.data.datasource.YearDao
import com.github.anastr.data.repositories.CourseRepository
import com.github.anastr.data.repositories.DatabaseRepository
import com.github.anastr.data.repositories.FirebaseRepository
import com.github.anastr.data.repositories.YearRepository
import com.github.anastr.domain.repositories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideMainDatabaseRepo(databaseDao: DatabaseDao): MainDatabaseRepo
            = DatabaseRepository(databaseDao)

    @Provides
    fun provideYearRepo(yearDao: YearDao, databaseDao: DatabaseDao): YearRepo
            = YearRepository(yearDao, databaseDao)

    @Provides
    fun provideReadCourseRepo(courseDao: CourseDao): ReadCourseRepo
            = CourseRepository(courseDao)

    @Provides
    fun provideWriteCourseRepo(courseDao: CourseDao): WriteCourseRepo
            = CourseRepository(courseDao)

    @Provides
    fun provideFirebaseRepo(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): FirebaseRepo
            = FirebaseRepository(defaultDispatcher)
}