package com.afl.waterReminderDrinkAlarmMonitor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Notification")
data class Notification(
    @PrimaryKey
    var id: Int = 0,
    var notificationPreference: Int = 0,
    var startingTime: Int = 0,
    var finishingTime: Int = 0,
    @ColumnInfo(name = "intervalTime")
    var interval: Int = 0
)