package com.afl.waterReminderDrinkAlarmMonitor.utils

import androidx.lifecycle.LiveData
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.Notification
import com.afl.waterReminderDrinkAlarmMonitor.model.Sum
import com.afl.waterReminderDrinkAlarmMonitor.model.User
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class Repository(private val dao: Dao?) {

    suspend fun readDrinkDataDetailsSelectedDay(date: String): MutableList<Drink>? {
        return dao?.readDrinkDataDetailsSelectedDay(date)
    }

    // this function returning live data as opposed to below readUserData
    fun readData(): LiveData<User>? = dao?.readData()

    suspend fun readUserData(): User? = dao?.readUserData()

    suspend fun insertDrinkData(drink: Drink): Unit? = dao?.insertDrinkData(drink)

    suspend fun readDrinkSumData(): MutableList<Sum>? = dao?.readDrinkSumData()

    suspend fun insertData(user: User): Unit? = dao?.insertData(user)

    suspend fun updateUser(user: User): Unit? = dao?.updateUser(user)

    suspend fun readNotData(): Notification? = dao?.readNotData()

    suspend fun insertNotificationInfo(not: Notification): Unit? = dao?.insertNotificationInfo(not)

    suspend fun updateNotificationInfo(not: Notification): Unit? = dao?.updateNotificationInfo(not)

    suspend fun deleteSelectedDrinkData(drink: Drink): Unit? = dao?.deleteSelectedDrinkData(drink)

    suspend fun readDrinkDataDetailsDaySum() = dao?.readDrinkDataDetailsDaySum()

}
