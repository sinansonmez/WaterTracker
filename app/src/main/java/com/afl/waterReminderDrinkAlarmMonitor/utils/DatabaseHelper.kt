package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.afl.waterReminderDrinkAlarmMonitor.model.Drink
import com.afl.waterReminderDrinkAlarmMonitor.model.User
import com.afl.waterReminderDrinkAlarmMonitor.model.sum
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
        private val DATABASE_NAME = "SQLITE_DATABASE.db"//database adı
        private val DATABASE_VERSION = 1
    }

    // iki tablo var birincisi user tablosu, her uygulamada bir user oluyor
    // ikincisi ise icilen icecegin historysini tutan tablo
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_WEIGHT INTEGER, $COL_GENDER  VARCHAR(256),$COL_METRIC VARCHAR(256), $COL_AGE INTEGER ,$COL_WATER INTEGER)"
        val createTableDrunk =
            "CREATE TABLE $TABLE_NAME_DRUNK ($COL_ID_DRUNK INTEGER PRIMARY KEY AUTOINCREMENT, $COL_DATE_DRUNK VARCHAR(256), $COL_TIME_DRUNK  VARCHAR(256),$COL_DRINK_DRUNK VARCHAR(256), $COL_AMOUNT_DRUNK INTEGER ,$COL_METRIC_DRUNK VARCHAR(256))"
        val createTableNotification =
            "CREATE TABLE $TABLE_NAME_NOT ($COL_ID_NOT INTEGER PRIMARY KEY AUTOINCREMENT, $COL_PREF_NOT INTEGER, $COL_START_NOT INTEGER, $COL_FINISH_NOT INTEGER,$COL_INTERVAL_NOT INTEGER)"
        db?.execSQL(createTable)
        db?.execSQL(createTableDrunk)
        db?.execSQL(createTableNotification)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    // yeni kullanici ekliyor
    fun insertData(
        user: User
    ) {
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_AGE, user.age)
        contentValues.put(COL_WEIGHT, user.weight)
        contentValues.put(COL_GENDER, user.gender)
        contentValues.put(COL_METRIC, user.metric)
        contentValues.put(COL_WATER, user.water)

        val result = sqliteDB.insert(TABLE_NAME, null, contentValues)

        sqliteDB.close()

//        Toast.makeText(
//            context,
//            if (result != -1L) "Kayıt Başarılı + $result" else "Kayıt yapılamadı.",
//            Toast.LENGTH_SHORT
//        ).show()
    }

    // icilen icecegi ek bilgiler ile birlikte database yaziyor
    fun insertDrinkData(drink: Drink) {
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_DATE_DRUNK, drink.date)
        contentValues.put(COL_TIME_DRUNK, drink.time)
        contentValues.put(COL_DRINK_DRUNK, drink.drink)
        contentValues.put(COL_AMOUNT_DRUNK, drink.amount)
        contentValues.put(COL_METRIC_DRUNK, drink.metric)

        val result = sqliteDB.insert(TABLE_NAME_DRUNK, null, contentValues)

//        Log.d("database", contentValues.toString())

//        Toast.makeText(
//            context,
//            if (result != -1L) "Kayıt Başarılı + $result" else "Kayıt yapılamadı.",
//            Toast.LENGTH_SHORT
//        ).show()

        sqliteDB.close()
    }

    // bu fonksiyon sadece son gunun toplam icilen miktari getiriyor
    fun readDrinkData(): Int {
        val sqliteDB = this.writableDatabase
        val query =
            "SELECT $COL_DATE_DRUNK, SUM($COL_AMOUNT_DRUNK) as Total FROM $TABLE_NAME_DRUNK GROUP BY $COL_DATE_DRUNK"
        val result = sqliteDB.rawQuery(query, null)
        result.moveToLast()
        val sum = sum()
        if (result.moveToLast()) {
            do {
                sum.date = result.getString(result.getColumnIndex(COL_DATE_DRUNK))
                sum.total = result.getString(result.getColumnIndex("Total")).toInt()
            } while (result.moveToNext())
        }

        val date = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time).toString()
        result.close()
        sqliteDB.close()
        // eger gun bugunun gunu degilse 0 getiriyor
        return if (sum.date == date) sum.total else 0
    }

    // bu fonksiyon sadece bugun icilen icecekleri getiriyor
    fun readDrinkDataDetailsSelectedDay(date: String): MutableList<Drink> {
        val drunkList = mutableListOf<Drink>()
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME_DRUNK WHERE $COL_DATE_DRUNK=?"
        val result = sqliteDB.rawQuery(query, arrayOf(date))
        if (result.moveToFirst()) {
            do {
                val drink = Drink()
                drink.id = result.getString(result.getColumnIndex(COL_ID_DRUNK)).toInt()
                drink.date = result.getString(result.getColumnIndex(COL_DATE_DRUNK))
                drink.time = result.getString(result.getColumnIndex(COL_TIME_DRUNK))
                drink.drink = result.getString(result.getColumnIndex(COL_DRINK_DRUNK))
                drink.amount = result.getString(result.getColumnIndex(COL_AMOUNT_DRUNK)).toInt()
                drink.metric = result.getString(result.getColumnIndex(COL_METRIC_DRUNK))
                drunkList.add(drink)
            } while (result.moveToNext())
        }
        result.close()
        sqliteDB.close()
//        Log.d("database", drunkList.toString())
        return drunkList
    }

    fun readDrinkDataDetailsDaySum(): MutableList<Drink> {
        val drunkList = mutableListOf<Drink>()
        val sqliteDB = this.writableDatabase
        val result = sqliteDB.query(
            TABLE_NAME_DRUNK,
            arrayOf(COL_DATE_DRUNK, "SUM($COL_AMOUNT_DRUNK) as amount", COL_METRIC_DRUNK),
            null,
            null,
            COL_DATE_DRUNK,
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

    // function to delete selected drink from the drunk list
    fun deleteSelectedDrinkData(id: Int) {
        val sqliteDB = this.writableDatabase
        sqliteDB.delete(TABLE_NAME_DRUNK, "$COL_ID_DRUNK =?", arrayOf(id.toString()))
        sqliteDB.close()
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

    // user tablosunda kullanici var mi kontrol ediyor
    fun checkNotTableCount(): Int {
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME_NOT"
        val result = sqliteDB.rawQuery(query, null)
        val count = result.count

        result.close()
        sqliteDB.close()

        return count
    }

    fun insertNotificationInfo(
        preference: Int,
        startingTime: Int,
        finishingTime: Int,
        interval: Int
    ) {
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_PREF_NOT, preference)
        contentValues.put(COL_START_NOT, startingTime)
        contentValues.put(COL_FINISH_NOT, finishingTime)
        contentValues.put(COL_INTERVAL_NOT, interval)

        val result = sqliteDB.insert(TABLE_NAME_NOT, null, contentValues)

//        Log.d("database", if (result != -1L) "Kayıt Başarılı + $result" else "Kayıt yapılamadı.")

//        Toast.makeText(
//            context,
//            if (result != -1L) "Kayıt Başarılı + $result" else "Kayıt yapılamadı.",
//            Toast.LENGTH_SHORT
//        ).show()

        sqliteDB.close()

    }

    // function to update notification information for each column
    fun updateNotificationInfo(
        variable: Int,
        action: String
    ) {
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME_NOT"
        val result = sqliteDB.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val cv = ContentValues()

                when (action) {
                    "preference" -> {
                        cv.put(COL_PREF_NOT, variable)
                    }
                    "starting_time" -> {
                        cv.put(COL_START_NOT, variable)
                    }
                    "finishing_time" -> {
                        cv.put(COL_FINISH_NOT, variable)
                    }
                    "interval_time" -> {
                        cv.put(COL_INTERVAL_NOT, variable)
                    }
                }

                sqliteDB.update(TABLE_NAME_NOT, cv, null, null)
            } while (result.moveToNext())
        }

        result.close()
        sqliteDB.close()

    }

    // user tablosundaki tum datayi okuyor
    fun readNotData(): MutableList<Int> {
        val sqliteDB = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME_NOT LIMIT 1"
        val result = sqliteDB.rawQuery(query, null)
        val notification = mutableListOf<Int>()
        if (result.moveToFirst()) {
            notification.add(result.getInt(result.getColumnIndex(COL_PREF_NOT)))
            notification.add(result.getInt(result.getColumnIndex(COL_START_NOT)))
            notification.add(result.getInt(result.getColumnIndex(COL_FINISH_NOT)))
            notification.add(result.getInt(result.getColumnIndex(COL_INTERVAL_NOT)))
//            Log.d("database", " notification info is  $notification")
        }
        result.close()
        sqliteDB.close()
        return notification
    }
}
