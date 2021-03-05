package com.github.anastr.myscore.hilt

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.anastr.myscore.room.AppDatabase
import com.github.anastr.myscore.room.dao.CourseDao
import com.github.anastr.myscore.room.dao.DatabaseDao
import com.github.anastr.myscore.room.dao.YearDao
import com.github.anastr.myscore.room.entity.Year
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        lateinit var appDatabase: AppDatabase
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "score.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    GlobalScope.launch(Dispatchers.IO) {
                        appDatabase.yearDao().insertAll(
                            Year(order = 0),
                            Year(order = 1),
                            Year(order = 2),
                            Year(order = 3),
                        )
                    }
                }
            })
            .build()
        return appDatabase
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
