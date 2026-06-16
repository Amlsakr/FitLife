package com.aml_sakr.fitlife

import android.app.Application
import android.content.pm.ApplicationInfo
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitnessApplication : Application() {
    override fun onCreate() {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            System.setProperty("fitlife.firestore.useEmulator", "true")
        }
        super.onCreate()
    }
}
