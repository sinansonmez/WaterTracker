package com.afl.waterReminderDrinkAlarmMonitor.model

data class User(
    var id: Int = 0,
    var age: Int = 0,
    var weight: Int = 0,
    var gender: String = "",
    var metric: String = "",
    var water: Int = 0
)