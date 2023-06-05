package com.android.kotlinmvvmtodolist.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.android.kotlinmvvmtodolist.R

const val channelID = "Channel 1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"


class Notification: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationID = intent.getIntExtra("notificationId", 0)
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle(intent.getStringExtra(titleExtra))
            .setContentText(intent.getStringExtra(messageExtra))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }

}