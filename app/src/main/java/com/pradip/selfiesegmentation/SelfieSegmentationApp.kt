package com.pradip.selfiesegmentation

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SelfieSegmentationApp : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
lateinit var appContext: Context
