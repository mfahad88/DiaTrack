package com.example.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object LocalNotificationManager {
    private const val TAG = "LocalNotification"
    private const val CHANNEL_ID = "diatrack_reminders_channel"
    private const val CHANNEL_NAME = "DiaTrack Scheduling Reminders"
    private const val CHANNEL_DESC = "Notifications reminding you to log your blood sugar, record meals, check insulin, and update custom health details."

    /**
     * Set up notification channel.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESC
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel registered successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed creating notification channels", e)
            }
        }
    }

    /**
     * Fires a local notification.
     */
    fun fireNotification(context: Context, id: Int, title: String, message: String) {
        // Enforce channel creation
        createNotificationChannel(context)

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Safe, universally available standard system drawable
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(id, builder.build())
            
            // Also notify visually via toast so user sees immediate feedback on local action trigger
            Toast.makeText(context, "$title: $message", Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security permission issue firing local notification", e)
            Toast.makeText(context, "Reminder Triggered: $title - $message", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed firing notification", e)
            Toast.makeText(context, "Reminder State: $title", Toast.LENGTH_SHORT).show()
        }
    }
}
