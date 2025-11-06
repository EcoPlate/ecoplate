package com.example.eco_plate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EcoPlateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}
