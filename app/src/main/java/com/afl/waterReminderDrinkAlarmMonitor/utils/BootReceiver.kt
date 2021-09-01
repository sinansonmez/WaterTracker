package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val dao = AppDatabase.getDatabase(context).dao()

        var notPermission: Int = 0
        var startingTime: Int = 0
        var finishingTime: Int = 0
        var intervalTime: Int = 0
        var notTimes = mutableListOf<Int>()

        // function to calculate notification time array based on starting, finishing and interval time
        fun notificationTimesHandler(
            startingTime: Int,
            finishingTime: Int,
            intervalTime: Int
        ): MutableList<Int> {
            var startingTimeForFunction = startingTime
            // first clear the array
            val times = mutableListOf<Int>()

            // add starting date to array as first notification time
            times.add(startingTimeForFunction)

            // then while starting time is smaller than finishing time add interval time
            while (startingTimeForFunction < finishingTime) {
                startingTimeForFunction += intervalTime
                times.add(startingTimeForFunction)
            }

            return times
        }

        // if notification information is already included in the database
        GlobalScope.launch(Dispatchers.IO) {
            val notification = Repository(dao).readNotData()

            if (notification != null) {

                notPermission = notification.notificationPreference

                if (notPermission == 1) {
                    startingTime = notification.startingTime
                    finishingTime = notification.finishingTime
                    intervalTime = notification.interval
                    notTimes = notificationTimesHandler(
                        startingTime = startingTime,
                        finishingTime = finishingTime,
                        intervalTime = intervalTime
                    )

                    if (context != null && intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {
                         AlarmScheduler.scheduleAlarm(context, notTimes)
                    }
                }
            }
        }

    }
}