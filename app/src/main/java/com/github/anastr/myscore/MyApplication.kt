package com.github.anastr.myscore

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
        // To run on old devices
        MultiDex.install(this)
    }

}
