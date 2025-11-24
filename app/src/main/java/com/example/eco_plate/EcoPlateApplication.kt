package com.example.eco_plate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EcoPlateApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        createOrdersChannel()
    }

    private fun createOrdersChannel() {
        val channel = NotificationChannel(
            "orders_channel",
            "Order Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about your EcoPlate orders"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
