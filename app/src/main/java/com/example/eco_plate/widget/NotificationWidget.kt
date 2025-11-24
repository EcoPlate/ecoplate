package com.example.eco_plate.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider

class NotificationWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val prefs = currentState<Preferences>()
            NotificationWidgetContent(prefs)
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun NotificationWidgetContent(prefs: Preferences) {
    val items = listOfNotNull(
        prefs[KEY_NOTIF_1], prefs[KEY_NOTIF_2], prefs[KEY_NOTIF_3]

    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF111827)))
            .cornerRadius(12.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Header
        Text(
            text = "EcoPlate",
            style = androidx.glance.text.TextStyle(
                color = ColorProvider(Color(0xFFFFFFFF)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(GlanceModifier.height(8.dp))

        if (items.isEmpty()) {
            Text(
                text = "No recent updates",
                style = androidx.glance.text.TextStyle(
                    color = ColorProvider(Color(0xFFFFFFFF)),
                    fontSize = 14.sp
                )
            )
        } else {
            items.forEach { line ->
                NotificationRow(line)
                Spacer(GlanceModifier.height(6.dp))
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun NotificationRow(text: String) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color(0xFF111827)))
            .padding(8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = text,
            style = androidx.glance.text.TextStyle(
                color = ColorProvider(Color(0xFFFFFFFF)),
                fontSize = 14.sp
            ),
            maxLines = 2
        )
    }
}

//////////////////////////////////////
//             PREVIEW              //
//////////////////////////////////////

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 150)
@Composable
fun NotificationWidgetPreview() {
    val fakePrefs = mutablePreferencesOf(
    KEY_NOTIF_1 to "Order #1243 • ETA Nov 25",
    KEY_NOTIF_2 to "Order #1240 • Out for delivery",
    KEY_NOTIF_3 to "New order placed • ETA Nov 28"
)
    NotificationWidgetContent(fakePrefs)
}