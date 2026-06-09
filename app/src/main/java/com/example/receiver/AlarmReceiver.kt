package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.ui.components.LocalNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmReceiver", "Received intent with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            // Re-schedule all alarms on rebuild
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val reminders = db.trackerDao().getAllRemindersSync()
                    for (reminder in reminders) {
                        if (reminder.isEnabled) {
                            LocalNotificationManager.scheduleReminder(context, reminder)
                        }
                    }
                    Log.d("AlarmReceiver", "Rescheduled custom alarms successfully on device boot.")
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Failed to reschedule alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            // Normal alarm trigger
            val reminderId = intent.getIntExtra("reminder_id", 0)
            val title = intent.getStringExtra("reminder_title") ?: "Health Reminder"
            val type = intent.getStringExtra("reminder_type") ?: "General"
            
            Log.d("AlarmReceiver", "Triggering alarm: $title ($reminderId)")
            
            val message = when (type) {
                "Blood Sugar" -> "Time to check your blood glucose level."
                "Insulin" -> "Time to take your scheduled insulin dose."
                "Medication" -> "Time to take your medication."
                "Meal" -> "Time to log your meal."
                "Appointment" -> "You have a scheduled medical appointment."
                else -> "Time for your scheduled health check-in."
            }
            
            LocalNotificationManager.fireNotification(context, reminderId, title, message)
        }
    }
}
