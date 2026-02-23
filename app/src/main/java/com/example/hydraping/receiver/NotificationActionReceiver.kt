package com.example.hydraping.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.hydraping.data.repository.WaterRepository
import com.example.hydraping.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: WaterRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    NotificationHelper.ACTION_LOG_WATER -> {
                        val amount = intent.getIntExtra(NotificationHelper.EXTRA_AMOUNT, 250)
                        repository.logWater(amount)
                    }
                }
                val notificationManager =
                    context.getSystemService(NotificationManager::class.java)
                notificationManager.cancel(NotificationHelper.NOTIFICATION_ID)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
