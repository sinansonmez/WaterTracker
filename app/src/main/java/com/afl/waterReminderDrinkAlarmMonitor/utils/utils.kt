package com.afl.waterReminderDrinkAlarmMonitor.utils

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.app.NotificationCompat
import com.afl.waterReminderDrinkAlarmMonitor.MainActivity
import com.afl.waterReminderDrinkAlarmMonitor.R
import com.afl.waterReminderDrinkAlarmMonitor.model.FormattedDate
import com.google.android.material.textfield.TextInputEditText

// Notification ID.
private const val NOTIFICATION_ID = 0

// change listener extension for TextInputEditText
fun EditText.onChange(cb: (String) -> Unit) {

    this.addTextChangedListener(object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {

    // create an intent and pending intent to take the user to the app
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        applicationContext.getColor(R.color.water_blue_100)
    } else {
        0x003064202
    }

    // Notification builder to set the specifications of the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.water_notification_channel_id)
    ).setSmallIcon(R.drawable.ic_not)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setColor(color)
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true) // after intent takes the user to the app notification is automatically canceled

    // finally notification is called
    notify(NOTIFICATION_ID, builder.build())
}

fun TextInputEditText.hideKeyBoardOnPressAway() {
    this.onFocusChangeListener =
        keyboardHider
}

// function to cancel all notification you may think to cancell first previous notifications and send new notification
fun NotificationManager.cancelAllNotification() {
    cancelAll()
}

fun dateParser(date: String): FormattedDate {
    val year = date.substring(0, 4).toInt()
    val month = date.substring(5, 7).toInt()
    val day = date.substring(8, 10).toInt()

    return FormattedDate(
        year,
        month,
        day
    )
}

private val keyboardHider = View.OnFocusChangeListener { v, hasFocus ->
    if (!hasFocus) {
        hideKeyboardFrom(v)
    }
}

private fun hideKeyboardFrom(view: View) {
    val imm = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
