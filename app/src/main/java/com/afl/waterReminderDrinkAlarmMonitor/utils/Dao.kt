package com.afl.waterReminderDrinkAlarmMonitor.utils

import androidx.room.*
import androidx.room.Dao
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.Notification
import com.afl.waterReminderDrinkAlarmMonitor.model.Sum
import com.afl.waterReminderDrinkAlarmMonitor.model.User

private const val DATABASE_NAME = "SQLITE_DATABASE.db"
private const val DATABASE_VERSION = 2

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


//TODO(https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/index.html?index=..%2F..index#10)
//TODO(https://www.raywenderlich.com/69-data-persistence-with-room)
@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDrinkData(drink: Drink)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotificationInfo(not: Notification)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNotificationInfo(not: Notification)

    @Delete
    fun deleteSelectedDrinkData(not: Notification)

    @Query("SELECT $COL_DATE_DRUNK, SUM($COL_AMOUNT_DRUNK) as Total FROM $TABLE_NAME_DRUNK GROUP BY $COL_DATE_DRUNK")
    fun readDrinkData(): Sum

    @Query("SELECT * FROM $TABLE_NAME_DRUNK WHERE $COL_DATE_DRUNK= :date")
    fun readDrinkDataDetailsSelectedDay(date: String): MutableList<Drink>

    //TODO(Query degistir )
    @Query("SELECT * FROM $TABLE_NAME_DRUNK")
    fun readDrinkDataDetailsDaySum(): MutableList<Drink>

    @Query("SELECT * FROM $TABLE_NAME LIMIT 1")
    fun readData(): User

    @Query("SELECT * FROM $TABLE_NAME_NOT LIMIT 1")
    fun readNotData(): Notification

}