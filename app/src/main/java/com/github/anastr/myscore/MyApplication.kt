package com.github.anastr.myscore

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {

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

}
