package com.example.hydraping.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.hydraping.MainActivity
import com.example.hydraping.R
import com.example.hydraping.receiver.NotificationActionReceiver

object NotificationHelper {

    const val CHANNEL_ID = "hydraping_reminders"
    const val NOTIFICATION_ID = 1001
    const val ACTION_LOG_WATER = "com.example.hydraping.ACTION_LOG_WATER"
    const val ACTION_SNOOZE = "com.example.hydraping.ACTION_SNOOZE"
    const val EXTRA_AMOUNT = "extra_amount"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(context: Context) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val logWaterIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_LOG_WATER
            putExtra(EXTRA_AMOUNT, 250)
        }
        val logWaterPendingIntent = PendingIntent.getBroadcast(
            context, 1, logWaterIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, 2, snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to Hydrate! \uD83D\uDCA7")
            .setContentText("Don't forget to drink water. Stay healthy!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "+250ml", logWaterPendingIntent)
            .addAction(0, "Snooze", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
