package com.afl.waterReminderDrinkAlarmMonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User" )
data class User(
    @PrimaryKey
    var id: Int = 0,
    var age: Int = 0,
    var weight: Int = 0,
    var gender: String = "",
    var metric: String = "",
    var water: Int = 0
)