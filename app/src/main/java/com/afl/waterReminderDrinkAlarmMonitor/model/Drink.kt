package com.afl.waterReminderDrinkAlarmMonitor.model

data class Drink(
    var id: Int = 0,
    var date: String = "",
    var time: String = "",
    var drink: String = "",
    var amount: Int = 0,
    var metric: String = ""
)