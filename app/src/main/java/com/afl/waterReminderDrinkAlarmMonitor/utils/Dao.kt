package com.afl.waterReminderDrinkAlarmMonitor.utils

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.Notification
import com.afl.waterReminderDrinkAlarmMonitor.model.Sum
import com.afl.waterReminderDrinkAlarmMonitor.model.User

private const val DATABASE_NAME = "SQLITE_DATABASE.db"

private const val TABLE_NAME = "User"
private const val COL_ID = "id"
private const val COL_AGE = "age"
private const val COL_WEIGHT = "weight"
private const val COL_GENDER = "gender"
private const val COL_METRIC = "metric"
private const val COL_WATER = "water"

private const val TABLE_NAME_DRUNK = "Drunk"
private const val COL_ID_DRUNK = "id"
private const val COL_DATE_DRUNK = "date"
private const val COL_TIME_DRUNK = "time"
private const val COL_DRINK_DRUNK = "drink"
private const val COL_AMOUNT_DRUNK = "amount"
private const val COL_METRIC_DRUNK = "metric"

private const val TABLE_NAME_NOT = "Notification"
private const val COL_ID_NOT = "id"
private const val COL_PREF_NOT = "notificationPreference"
private const val COL_START_NOT = "startingTime"
private const val COL_FINISH_NOT = "finishingTime"
private const val COL_INTERVAL_NOT = "intervalTime"

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinkData(drink: Drink)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationInfo(not: Notification)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNotificationInfo(not: Notification)

    @Delete
    suspend fun deleteSelectedDrinkData(drink: Drink)

    @Query("SELECT $COL_DATE_DRUNK, SUM($COL_AMOUNT_DRUNK) as Total FROM $TABLE_NAME_DRUNK GROUP BY $COL_DATE_DRUNK")
    fun readDrinkData(): LiveData<MutableList<Sum>>

    @Query("SELECT $COL_DATE_DRUNK, SUM($COL_AMOUNT_DRUNK) as Total FROM $TABLE_NAME_DRUNK GROUP BY $COL_DATE_DRUNK")
    suspend fun readDrinkSumData(): MutableList<Sum>

    @Query("SELECT * FROM $TABLE_NAME_DRUNK WHERE $COL_DATE_DRUNK= :date")
    suspend fun readDrinkDataDetailsSelectedDay(date: String): MutableList<Drink>

    @Query("SELECT $COL_ID_DRUNK, $COL_TIME_DRUNK, $COL_DATE_DRUNK, $COL_DRINK_DRUNK, SUM($COL_AMOUNT_DRUNK) as amount, $COL_METRIC_DRUNK FROM $TABLE_NAME_DRUNK GROUP BY $COL_DATE_DRUNK")
    suspend fun readDrinkDataDetailsDaySum(): MutableList<Drink>

    @Query("SELECT * FROM $TABLE_NAME LIMIT 1")
    fun readData(): LiveData<User>

    @Query("SELECT * FROM $TABLE_NAME LIMIT 1")
    suspend fun readUserData(): User

    @Query("SELECT * FROM $TABLE_NAME_NOT LIMIT 1")
    suspend fun readNotData(): Notification
}
