package com.example.eco_plate.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState

suspend fun updateNotificationWidget(context: Context, lines: List<String>) {

    val manager = GlanceAppWidgetManager(context)
    val ids = manager.getGlanceIds(NotificationWidget::class.java)

    ids.forEach { glanceId ->
        updateAppWidgetState(context, glanceId) { prefs ->
            val m = prefs.toMutablePreferences()
            m[KEY_NOTIF_1] = lines.getOrNull(0).orEmpty()
            m[KEY_NOTIF_2] = lines.getOrNull(1).orEmpty()
            m[KEY_NOTIF_3] = lines.getOrNull(2).orEmpty()
            m
        }

        NotificationWidget().update(context, glanceId)
    }
}