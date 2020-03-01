package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.User


class DatabaseHelper(val context: Context) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME, null,
        DATABASE_VERSION
    ) {
    private val TABLE_NAME = "User"
    private val COL_ID = "id"
    private val COL_AGE = "age"
    private val COL_WEIGHT = "weight"
    private val COL_GENDER = "gender"
    private val COL_METRIC = "metric"
    private val COL_WATER = "water"

    private val TABLE_NAME_DRUNK = "Drunk"
    private val COL_ID_DRUNK = "id"
    private val COL_DATE_DRUNK = "date"
    private val COL_TIME_DRUNK = "time"
    private val COL_DRINK_DRUNK = "drink"
    private val COL_AMOUNT_DRUNK = "amount"
    private val COL_METRIC_DRUNK = "metric"

    private val TABLE_NAME_NOT = "Notification"
    private val COL_ID_NOT = "id"
    private val COL_PREF_NOT = "notificationPreference"
    private val COL_START_NOT = "startingTime"
    private val COL_FINISH_NOT = "finishingTime"
    private val COL_INTERVAL_NOT = "intervalTime"


    companion object {
        private const val DATABASE_NAME = "SQLITE_DATABASE.db"//database adı
        private const val DATABASE_VERSION = 5
    }

    // iki tablo var birincisi user tablosu, her uygulamada bir user oluyor
    // ikincisi ise icilen icecegin historysini tutan tablo
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $COL_WEIGHT INTEGER NOT NULL, $COL_GENDER  TEXT NOT NULL, $COL_METRIC TEXT NOT NULL, $COL_AGE INTEGER NOT NULL ,$COL_WATER INTEGER NOT NULL)"
        val createTableDrunk =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME_DRUNK ($COL_ID_DRUNK INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $COL_DATE_DRUNK TEXT NOT NULL, $COL_TIME_DRUNK TEXT NOT NULL,$COL_DRINK_DRUNK TEXT NOT NULL, $COL_AMOUNT_DRUNK INTEGER NOT NULL,$COL_METRIC_DRUNK TEXT NOT NULL)"
        val createTableNotification =
            "CREATE TABLE IF NOT EXISTS  $TABLE_NAME_NOT ($COL_ID_NOT INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $COL_PREF_NOT INTEGER NOT NULL, $COL_START_NOT INTEGER NOT NULL, $COL_FINISH_NOT INTEGER NOT NULL, $COL_INTERVAL_NOT INTEGER NOT NULL)"
        db?.execSQL(createTable)
        db?.execSQL(createTableDrunk)
        db?.execSQL(createTableNotification)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun readDrinkDataDetailsDaySum(): MutableList<Drink> {
        val drunkList = mutableListOf<Drink>()
        val sqliteDB = this.writableDatabase
        val result = sqliteDB.query(
            TABLE_NAME_DRUNK,
            arrayOf(COL_DATE_DRUNK, "SUM($COL_AMOUNT_DRUNK) as amount", COL_METRIC_DRUNK), //columns
            null,
            null,
            COL_DATE_DRUNK, // group by
            null,
            null
        )
        if (result.moveToFirst()) {
            do {
                val drink = Drink()
                drink.date = result.getString(result.getColumnIndex(COL_DATE_DRUNK))
                drink.amount = result.getString(result.getColumnIndex(COL_AMOUNT_DRUNK)).toInt()
                drink.metric = result.getString(result.getColumnIndex(COL_METRIC_DRUNK))
                drunkList.add(drink)
            } while (result.moveToNext())
        }
        result.close()
        sqliteDB.close()
        return drunkList
    }

    // user tablosundaki tum datayi okuyor
    fun readData(): User {
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME LIMIT 1"
        val result = sqliteDB.rawQuery(query, null)
        val user = User()
        if (result.moveToFirst()) {
            user.age = result.getString(result.getColumnIndex(COL_AGE)).toInt()
            user.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
            user.weight = result.getString(result.getColumnIndex(COL_WEIGHT)).toInt()
            user.gender = result.getString(result.getColumnIndex(COL_GENDER))
            user.metric = result.getString(result.getColumnIndex(COL_METRIC))
            user.water = result.getString(result.getColumnIndex(COL_WATER)).toInt()
        }
        result.close()
        sqliteDB.close()
        return user
    }

    // user tablosunda kullanici var mi kontrol ediyor
    fun checkUserTableCount(): Int {
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = sqliteDB.rawQuery(query, null)
        val count = result.count

        result.close()
        sqliteDB.close()

        return count
    }

}
