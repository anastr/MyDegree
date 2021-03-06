package com.github.anastr.myscore.hilt

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.github.anastr.myscore.room.AppDatabase
import com.github.anastr.myscore.room.dao.CourseDao
import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.dao.YearDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return AppDatabase.getInstance(applicationContext)
    }

    @Provides
    fun provideDatabaseDao(appDatabase: AppDatabase): DatabaseDao
            = appDatabase.databaseDao()

    @Provides
    fun provideYearDao(appDatabase: AppDatabase): YearDao
            = appDatabase.yearDao()

    @Provides
    fun provideSubjectDao(appDatabase: AppDatabase): CourseDao
            = appDatabase.courseDao()

    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences
            = PreferenceManager.getDefaultSharedPreferences(application)

    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher
            = Dispatchers.IO

}
