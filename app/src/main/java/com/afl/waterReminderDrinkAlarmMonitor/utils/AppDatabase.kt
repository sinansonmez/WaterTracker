package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.Notification
import com.afl.waterReminderDrinkAlarmMonitor.model.User


private const val DATABASE_NAME = "SQLITE_DATABASE.db"
private const val DATABASE_VERSION = 1

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

@Database(
    entities = [User::class, Drink::class, Notification::class],
    version = DATABASE_VERSION,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): Dao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context?): AppDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context?.applicationContext!!,
                    AppDatabase::class.java, DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }
}
