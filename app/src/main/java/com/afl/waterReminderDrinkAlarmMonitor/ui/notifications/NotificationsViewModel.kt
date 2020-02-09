package com.afl.waterReminderDrinkAlarmMonitor.ui.notifications

import android.app.Application
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afl.waterReminderDrinkAlarmMonitor.utils.AlarmScheduler
import com.afl.waterReminderDrinkAlarmMonitor.utils.DatabaseHelper

class NotificationsViewModel(private val app: Application) : AndroidViewModel(app) {

    val db by lazy { DatabaseHelper(app.applicationContext) }

    private val _notPreference = MutableLiveData<Int>()
    val notPreference: LiveData<Int> = _notPreference

    private val _startingTime = MutableLiveData<Int>()
    val startingTime: LiveData<Int> = _startingTime

    private val _finishingTime = MutableLiveData<Int>()
    val finishingTime: LiveData<Int> = _finishingTime

    private val _intervalTime = MutableLiveData<Int>()
    val intervalTime: LiveData<Int> = _intervalTime

    private val _notificationTimes =
        MutableLiveData<MutableList<Int>>().apply { value = mutableListOf() }
    val notificationTimes: LiveData<MutableList<Int>> = _notificationTimes


    fun notificationPreferenceHandler(preference: Int, action: String = "preference") {
        _notPreference.value = preference
        db.updateNotificationInfo(preference, action)
    }

    // after you receive selected item from listener you check
    fun timeHandler(selectedItem: String, spinnerType: String) {

        when (spinnerType) {
            "starting_time" -> {
                val startTime = selectedItem.substring(0, 2).toInt()
                _startingTime.value = startTime
                db.updateNotificationInfo(startTime, "starting_time")
            }
            "finishing_time" -> {
                val finishTime = selectedItem.substring(0, 2).toInt()
                _finishingTime.value = finishTime
                db.updateNotificationInfo(finishTime, "finishing_time")
            }
            "interval_time" -> {
                val intervalTime = selectedItem.substring(0, 1).toInt()
                _intervalTime.value = intervalTime
                db.updateNotificationInfo(intervalTime, "interval_time")
            }
            else -> return
        }

        if (_startingTime.value != null && _finishingTime.value != null && _intervalTime.value != null) {
            notificationTimesHandler()
        }


    }

    // function to calculate notification time array based on starting, finishing and interval time
    private fun notificationTimesHandler() {
        var startingTime = _startingTime.value

        // first clear the array
        _notificationTimes.value = mutableListOf()

        // add starting date to array as first notification time
        _notificationTimes.value?.add(startingTime!!)

        // then while starting time is smaller than finishing time add interval time
        while (startingTime!! < _finishingTime.value!!) {
            startingTime += _intervalTime.value!!
            _notificationTimes.value?.add(startingTime)
        }


        if (_notificationTimes.value != null) {
            AlarmScheduler.scheduleAlarm(app, _notificationTimes.value!!)
        }


//        Log.d("database", "new notification times are ${notificationTimes.value}")

    }

    val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            val selectedItem = parent?.getItemAtPosition(pos).toString()
            val spinnerType = parent?.tag.toString()

            timeHandler(selectedItem, spinnerType)

        }
    }

}