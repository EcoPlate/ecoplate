package com.example.eco_plate.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.eco_plate.MainActivity
import com.example.eco_plate.R

object NotificationNotifier {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showArrivingToday(context: Context, orderNumber: String) {

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_orders", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, "orders_channel")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("EcoPlate")
            .setContentText("Order #$orderNumber is arriving today!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(orderNumber.hashCode(), notif)
    }
}