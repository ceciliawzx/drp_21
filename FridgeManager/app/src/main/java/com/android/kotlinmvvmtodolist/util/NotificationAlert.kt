package com.android.kotlinmvvmtodolist.util

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.time.LocalDate

object NotificationAlert {

    // Schedule a notification based on notificationID
    fun scheduleNotification(context: Context, activity: FragmentActivity, title: String, message: String, notificationTime: Long, notificationID: Int) {
        val intent = Intent(context, Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        intent.putExtra("notificationId", notificationID)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
        activity.runOnUiThread {
            showAlert(notificationTime, title, message, context)
        }
    }

    // Calculate when to notify the expiring item. For now notify one day before
    fun getNotificationTime(expirationDate: String, daysBefore: String): Long {
        var offset: Long
        try {
            offset = daysBefore.toLong()
        } catch (e: Exception) {
            offset = 1
        }

        val expiryDate = LocalDate.parse(expirationDate)
        val notifyDate = expiryDate.minusDays(offset)

        val year = notifyDate.year
        val month = notifyDate.monthValue - 1
        val day = notifyDate.dayOfMonth  // default: notify the day before expiration

        /* TODO: This is for the sake of the test. When you run the test,
        set the hour and minute to be the time you expect the notification to happen */
        val hour = 9  // 0 ~ 23
        val minute = 0
        val second = 0

        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute, second)
        return calendar.timeInMillis
    }

    // Show an alert to users that a notification has been scheduled
    // TODO: maybe delete this later, based on user feedback
    fun showAlert(time: Long, title: String, message: String, context: Context) {
//        val date = Date(time)
//        val dateFormat = DateFormat.getLongDateFormat(context)
//        val timeFormat = DateFormat.getTimeFormat(context)
//        AlertDialog.Builder(context)
//            .setTitle("Notification scheduled")
//            .setMessage(
//                "Title: " + title
//                        + "\nMessage: " + message
//                        + "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date)
//            )
//            .setPositiveButton("Okay") { _, _ -> }
//            .show()
        Toast.makeText(context, "Notification Scheduled", Toast.LENGTH_SHORT).show()
    }

    // Calculate days left between now and expirationDate
    fun calculateDaysLeft(expirationDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        val expiryDate = dateFormat.parse(expirationDate)
        val diff = (expiryDate?.time ?: currentDate.time) - currentDate.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }

}