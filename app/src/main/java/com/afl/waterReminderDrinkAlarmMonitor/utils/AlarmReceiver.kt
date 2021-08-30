package com.afl.waterReminderDrinkAlarmMonitor.utils


import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.afl.waterReminderDrinkAlarmMonitor.R

class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            if (intent.extras != null) {
                val notificationManager =
                    ContextCompat.getSystemService(
                        context,
                        NotificationManager::class.java
                    ) as NotificationManager

                notificationManager.sendNotification(context.getString(R.string.notification_info), context)
            }
        }
    }
}