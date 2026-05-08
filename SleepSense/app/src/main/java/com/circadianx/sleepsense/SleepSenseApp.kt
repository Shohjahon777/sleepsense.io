package com.circadianx.sleepsense

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

const val NOTIFICATION_CHANNEL_ID = "sleep_summary"

@HiltAndroidApp
class SleepSenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Sleep Summary",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Morning summary of your sleep recording"
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}
