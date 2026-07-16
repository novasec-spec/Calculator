package com.novasec.nova

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.novasec.nova.utils.AppPreferences

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize preferences
        AppPreferences.init(this)

        // Create notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                "chat_channel",
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ),
            NotificationChannel(
                "love_channel",
                "Love Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ),
            NotificationChannel(
                "reminder_channel",
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                "widget_channel",
                "Widget Updates",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        val manager = getSystemService(NotificationManager::class.java)
        channels.forEach { channel ->
            channel.enableVibration(true)
            channel.setShowBadge(true)
            manager.createNotificationChannel(channel)
        }
    }
}
