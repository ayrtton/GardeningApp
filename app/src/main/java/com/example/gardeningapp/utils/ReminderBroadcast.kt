package com.example.gardeningapp.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gardeningapp.activities.MainActivity
import android.media.RingtoneManager
import android.net.Uri
import com.example.gardeningapp.R


class ReminderBroadcast: BroadcastReceiver() {
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onReceive(context: Context, intent: Intent?) {
        val notifyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notifyPendingIntent = PendingIntent.getActivity(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context,"notify")
            .setSmallIcon(R.drawable.ic_plant)
            .setContentTitle("Watering reminder")
            .setContentText("It's time to water your plants...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(200, builder.build())
    }
}