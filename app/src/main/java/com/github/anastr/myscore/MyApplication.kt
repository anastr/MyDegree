package com.github.anastr.myscore

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = false
        }
    }

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
        // To run on old devices
        MultiDex.install(this)
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}
