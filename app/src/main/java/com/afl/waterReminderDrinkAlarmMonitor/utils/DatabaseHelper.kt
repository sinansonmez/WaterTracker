package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.Sum
import com.afl.waterReminderDrinkAlarmMonitor.model.User
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

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
        private const val DATABASE_NAME = "SQLITE_DATABASE.db" //database adı
        private const val DATABASE_VERSION = 1
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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    // her günün toplam içilen miktarı bir liste olarak getiriyor
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
        Timber.d("drunk list: $drunkList")
        Timber.d("drunk list size: ${drunkList.size}")
        result.close()
        sqliteDB.close()
        return drunkList
    }

    fun updateUser(user: User) {
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = sqliteDB.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val cv = ContentValues()
                cv.put(COL_AGE, user.age)
                cv.put(COL_GENDER, user.gender)
                cv.put(COL_WEIGHT, user.weight)
                cv.put(COL_METRIC, user.metric)
                cv.put(COL_WATER, user.water)
                sqliteDB.update(TABLE_NAME, cv, null, null)
            } while (result.moveToNext())
        }

        result.close()
        sqliteDB.close()
    }

    // add new user to database
    fun insertData(user: User) {
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_AGE, user.age)
        contentValues.put(COL_WEIGHT, user.weight)
        contentValues.put(COL_GENDER, user.gender)
        contentValues.put(COL_METRIC, user.metric)
        contentValues.put(COL_WATER, user.water)

        sqliteDB.insert(TABLE_NAME, null, contentValues)

        sqliteDB.close()
    }

    // read all data from user table
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

    // checks if there is an existing user in database
    fun checkUserTableCount(): Int {
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = sqliteDB.rawQuery(query, null)
        val count = result.count

        result.close()
        sqliteDB.close()

        return count
    }

    // get the total drunk amount on the last day
    fun readDrinkData(): Int {
        val sqliteDB = this.writableDatabase
        val query =
            "SELECT $COL_DATE_DRUNK, SUM($COL_AMOUNT_DRUNK) as Total FROM $TABLE_NAME_DRUNK GROUP BY $COL_DATE_DRUNK"
        val result = sqliteDB.rawQuery(query, null)
        result.moveToLast()
        val sum = Sum()
        if (result.moveToLast()) {
            do {
                sum.date = result.getString(result.getColumnIndex(COL_DATE_DRUNK))
                sum.total = result.getString(result.getColumnIndex("Total")).toInt()
            } while (result.moveToNext())
        }

        val date = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()
        result.close()
        sqliteDB.close()
        // if not today set it to zero
        return if (sum.date == date) sum.total else 0
    }

    fun insertDrinkData(drink: Drink) {
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_DATE_DRUNK, drink.date)
        contentValues.put(COL_TIME_DRUNK, drink.time)
        contentValues.put(COL_DRINK_DRUNK, drink.drink)
        contentValues.put(COL_AMOUNT_DRUNK, drink.amount)
        contentValues.put(COL_METRIC_DRUNK, drink.metric)

        sqliteDB.insert(TABLE_NAME_DRUNK, null, contentValues)

        sqliteDB.close()
    }
}
