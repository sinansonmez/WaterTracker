package com.afl.waterReminderDrinkAlarmMonitor.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val db by lazy { DatabaseHelper(context!!) }

        Log.d("database", "onReceive is called")

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
        if (db.checkNotTableCount() == 1) {

            val notificationInfo = db.readNotData()

            notPermission = notificationInfo[0]

            Log.d("database", " permission is $notPermission")

            if (notPermission == 1) {
                startingTime = notificationInfo[1]
                finishingTime = notificationInfo[2]
                intervalTime = notificationInfo[3]
                notTimes = notificationTimesHandler(
                    startingTime = startingTime,
                    finishingTime = finishingTime,
                    intervalTime = intervalTime
                )

                if (context != null && intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {

                    Log.d("database", "alarm scheduler is called")

                    AlarmScheduler.scheduleAlarm(context, notTimes)

                }
            }
        }


    }
}