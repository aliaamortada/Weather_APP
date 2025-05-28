package com.example.weatherapp.ui.notification


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.Notification
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.weatherapp.R
import android.provider.Settings
import android.widget.Toast
import com.example.weatherapp.MainActivity


class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_SNOOZE = "com.example.weatherapp.ACTION_SNOOZE"
        const val ACTION_STOP = "com.example.weatherapp.ACTION_STOP"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        android.util.Log.d("AlarmReceiver", "Alarm triggered with action: ${intent.action}")
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_SHORT).show()

        // Create Notification Channel for Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        when (intent.action) {
            ACTION_SNOOZE -> {
                // Reschedule alarm for 5 minutes later
                AlarmHelper.scheduleAlarm(context, 5 * 60 * 1000L)
                notificationManager.cancel(NOTIFICATION_ID)
            }
            ACTION_STOP -> {
                // Cancel alarm notifications
                notificationManager.cancel(NOTIFICATION_ID)
            }
            else -> {
                // Show notification when alarm rings
                val intentToLaunch = Intent(context, MainActivity::class.java)
                val contentPendingIntent = PendingIntent.getActivity(
                    context, 0, intentToLaunch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmSoundUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Alarm")
                    .setContentText("Your alarm is ringing!")
                    .setSmallIcon(R.drawable.notifications_active_24px)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(alarmSoundUri)
                    .setContentIntent(contentPendingIntent)  // <-- Add this!
                    .setAutoCancel(true)
                    .addAction(R.drawable.snooze_24px, "Snooze", snoozePendingIntent)
                    .addAction(R.drawable.cancel_24px, "Stop", stopPendingIntent)
                    .build()


            }
        }
    }
}
