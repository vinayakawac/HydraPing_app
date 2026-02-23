package com.example.hydraping.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.hydraping.data.local.PreferencesDataStore
import com.example.hydraping.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferencesDataStore: PreferencesDataStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val settings = preferencesDataStore.userSettings.first()

        if (!settings.notificationsEnabled) return Result.success()

        if (!isInSleepWindow(settings.sleepStartHour, settings.sleepEndHour)) {
            NotificationHelper.showReminderNotification(applicationContext)
        }

        return Result.success()
    }

    private fun isInSleepWindow(sleepStart: Int, sleepEnd: Int): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (sleepStart <= sleepEnd) {
            currentHour in sleepStart until sleepEnd
        } else {
            currentHour >= sleepStart || currentHour < sleepEnd
        }
    }

    companion object {
        const val WORK_NAME = "hydration_reminder"

        fun schedule(context: Context, intervalMinutes: Int) {
            val interval = intervalMinutes.toLong().coerceAtLeast(15)
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                interval, TimeUnit.MINUTES
            )
                .setInitialDelay(interval, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
