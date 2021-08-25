package com.afl.waterReminderDrinkAlarmMonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Drunk")
data class Drink(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var date: String = "",
    var time: String = "",
    var drink: String = "",
    var amount: Int = 0,
    var metric: String = ""
)