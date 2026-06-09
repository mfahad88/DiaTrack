package com.example.ui.components

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.Reminder
import com.example.receiver.AlarmReceiver
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

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
            
            // Play custom decent & elegant sound
            playElegantChime(context)
            
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

    /**
     * Schedules an alarm with Android's AlarmManager for a specific Reminder.
     */
    fun scheduleReminder(context: Context, reminder: Reminder) {
        if (!reminder.isEnabled) {
            cancelReminder(context, reminder)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_type", reminder.type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.timeHour)
            set(Calendar.MINUTE, reminder.timeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time already elapsed today, set for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Successfully scheduled alarm for ${reminder.title} at ${calendar.time}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm with system AlarmManager", e)
        }
    }

    /**
     * Cancels an existing scheduled alarm with Android's AlarmManager for a specific Reminder.
     */
    fun cancelReminder(context: Context, reminder: Reminder) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id,
                intent,
                PendingIntent.FLAG_NO_CREATE or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Successfully canceled alarm for ${reminder.title} (ID: ${reminder.id})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling alarm", e)
        }
    }

    /**
     * Synthesizes and writes an elegant, rich, and naturally loud ascending chords chime with resonant overtones.
     */
    private fun generateElegantChimeWav(targetFile: File) {
        val sampleRate = 44100
        val totalDuration = 3.2
        val numSamples = (sampleRate * totalDuration).toInt()
        val samples = FloatArray(numSamples) { 0.0f }

        // Frequency, StartTime, Duration
        val notes = listOf(
            Triple(261.63f,  0.0f,  2.8f),  // C4 (Robust base warm foundation)
            Triple(329.63f,  0.12f, 2.5f),  // E4
            Triple(392.00f,  0.24f, 2.2f),  // G4
            Triple(493.88f,  0.36f, 1.9f),  // B4
            Triple(523.25f,  0.48f, 1.6f),  // C5 (Gleaming high chime)
            Triple(659.25f,  0.60f, 1.3f)   // E5 (Ultra-clear high-frequency presence)
        )

        for ((freq, start, dur) in notes) {
            val startSample = (start * sampleRate).toInt()
            var endSample = ((start + dur) * sampleRate).toInt()
            if (endSample > numSamples) endSample = numSamples

            val noteLength = endSample - startSample
            for (i in 0 until noteLength) {
                val t = i.toFloat() / sampleRate.toFloat()
                // Reduced exponential decay rate (-1.5) for long, resonant, and loud sustain holding maximum energy.
                val decay = kotlin.math.exp(-1.5f * t)
                
                // 4ms smooth fade-in to prevent digital pop
                val attack = if (t < 0.004f) t / 0.004f else 1.0f

                // Form an incredibly rich & pleasant sound using fundamental sine wave and multiple bright overtones
                val waveVal = 1.00f * kotlin.math.sin(2.0f * kotlin.math.PI.toFloat() * freq * t) + 
                              0.45f * kotlin.math.sin(2.0f * kotlin.math.PI.toFloat() * (freq * 2.0f) * t) +
                              0.25f * kotlin.math.sin(2.0f * kotlin.math.PI.toFloat() * (freq * 3.0f) * t)

                samples[startSample + i] += waveVal * decay * attack
            }
        }

        // Search for peak amplitude
        var maxVal = 0.01f
        for (s in samples) {
            val absVal = kotlin.math.abs(s)
            if (absVal > maxVal) {
                maxVal = absVal
            }
        }

        // Scale to 0.98f of absolute full-scale range (maximizing absolute volume while ensuring zero clipping)
        val scale = 0.98f * 32767.0f / maxVal
        val pcmData = ByteArray(numSamples * 2)
        for (i in 0 until numSamples) {
            val sampleInt = (samples[i] * scale).toInt().coerceIn(-32768, 32767)
            pcmData[i * 2] = (sampleInt and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sampleInt shr 8) and 0xFF).toByte()
        }

        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xFF).toByte()
        header[5] = ((totalDataLen shr 8) and 0xFF).toByte()
        header[6] = ((totalDataLen shr 16) and 0xFF).toByte()
        header[7] = ((totalDataLen shr 24) and 0xFF).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = 1 // Mono
        header[23] = 0
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = ((sampleRate shr 8) and 0xFF).toByte()
        header[26] = ((sampleRate shr 16) and 0xFF).toByte()
        header[27] = ((sampleRate shr 24) and 0xFF).toByte()
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()
        header[32] = 2
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmData.size and 0xFF).toByte()
        header[41] = ((pcmData.size shr 8) and 0xFF).toByte()
        header[42] = ((pcmData.size shr 16) and 0xFF).toByte()
        header[43] = ((pcmData.size shr 24) and 0xFF).toByte()

        try {
            FileOutputStream(targetFile).use { fos ->
                fos.write(header)
                fos.write(pcmData)
            }
            Log.d(TAG, "Loud elegant chime sound file created successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write synthesized sound file: ${e.message}", e)
        }
    }

    /**
     * Plays the custom elegant bell sound at maximum hardware output.
     */
    fun playElegantChime(context: Context) {
        try {
            val soundFile = File(context.cacheDir, "elegant_chime_loud.wav")
            if (!soundFile.exists() || soundFile.length() < 1000) {
                generateElegantChimeWav(soundFile)
            }
            if (soundFile.exists()) {
                val mediaUri = Uri.fromFile(soundFile)
                val mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, mediaUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    prepare()
                    setVolume(1.0f, 1.0f) // Set maximum player volume output
                    start()
                    setOnCompletionListener {
                        it.release()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing customized elegant chime sound", e)
        }
    }
}
