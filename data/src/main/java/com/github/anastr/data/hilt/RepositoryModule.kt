package com.github.anastr.data.hilt

import com.github.anastr.data.datasource.*
import com.github.anastr.data.repositories.*
import com.github.anastr.domain.repositories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
    fun provideFirebaseRepo(firebaseDataSource: FirebaseDataSource): FirebaseRepo
            = FirebaseRepository(firebaseDataSource)

    @ExperimentalCoroutinesApi
    @Provides
    fun providePassDegreePero(sharedPrefDataSource: SharedPrefDataSource): PassDegreeRepo
            = SharedPrefRepository(sharedPrefDataSource)

    @ExperimentalCoroutinesApi
    @Provides
    fun provideThemeRepo(sharedPrefDataSource: SharedPrefDataSource): ThemeRepo
            = SharedPrefRepository(sharedPrefDataSource)
}
