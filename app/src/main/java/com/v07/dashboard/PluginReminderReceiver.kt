package com.v07.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V07Database

/**
 * BroadcastReceiver triggered by AlarmManager to fire scheduled plugin reminder notifications
 * and handle interactive notification actions (Mark Done, Snooze 15m).
 *
 * Configured with dedicated high-priority alarm channels, audible ringtones routed via the
 * alarm audio stream, and customized Android notification presentation.
 */
class PluginReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "v07_sanctuary_reminders_v3"
        const val ALARM_CHANNEL_ID = "v07_sovereign_alarms_v3"

        const val ACTION_PLUGIN_REMINDER = "com.v07.dashboard.ACTION_PLUGIN_REMINDER"
        const val ACTION_TASK_COMPLETE = "com.v07.dashboard.ACTION_TASK_COMPLETE"
        const val ACTION_REMINDER_SNOOZE = "com.v07.dashboard.ACTION_REMINDER_SNOOZE"

        // Signature Sanctuary Emerald Accent Color for Android System Notifications
        const val SANCTUARY_ACCENT_COLOR = 0xFF50BF8E.toInt()

        @Volatile
        private var activeAlarmRingtone: Ringtone? = null

        fun stopRinging() {
            try {
                activeAlarmRingtone?.let {
                    if (it.isPlaying) it.stop()
                }
            } catch (_: Exception) {}
            activeAlarmRingtone = null
        }

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(NotificationManager::class.java) ?: return

                // 1. High-Priority Audible Alarm Channel (for Task Deadlines & Milestones)
                if (nm.getNotificationChannel(ALARM_CHANNEL_ID) == null) {
                    val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val alarmAudioAttrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()

                    val alarmChan = NotificationChannel(
                        ALARM_CHANNEL_ID,
                        "Sovereign Alarms & Deadlines",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Audible alarms for task deadlines and high-priority milestones"
                        enableLights(true)
                        lightColor = SANCTUARY_ACCENT_COLOR
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 450, 150, 450, 150, 450)
                        setSound(alarmSoundUri, alarmAudioAttrs)
                        setShowBadge(true)
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            setAllowBubbles(true)
                        }
                    }
                    nm.createNotificationChannel(alarmChan)
                }

                // 2. Sanctuary Mindful Reminders Channel (for advance reminders & system prompts)
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    val notifSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val notifAudioAttrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()

                    val reminderChan = NotificationChannel(
                        CHANNEL_ID,
                        "Sanctuary Reminders & Prompts",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Mindful check-ins, advance reminders, and system alerts"
                        enableLights(true)
                        lightColor = SANCTUARY_ACCENT_COLOR
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 250, 150, 250)
                        setSound(notifSoundUri, notifAudioAttrs)
                        setShowBadge(true)
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    }
                    nm.createNotificationChannel(reminderChan)
                }
            }
        }

        fun postCustomNotification(
            context: Context,
            pluginId: String,
            id: String,
            title: String,
            body: String,
            isAlarm: Boolean = false
        ): Int {
            ensureNotificationChannel(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    android.util.Log.w("PluginReminder", "POST_NOTIFICATIONS permission not granted")
                    return -1
                }
            }

            val notifId = (pluginId + id + System.currentTimeMillis()).hashCode() and 0x7FFFFFFF

            val targetClass = try {
                Class.forName("com.v07.v3.app.V3Activity")
            } catch (_: Exception) {
                MainActivity::class.java
            }
            val clickIntent = Intent(context, targetClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("openPlugin", pluginId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val displayPlugin = if (pluginId.isBlank() || pluginId == "core" || pluginId == "com.v07.core") {
                "Sanctuary"
            } else {
                pluginId.replaceFirstChar { it.uppercase() }
            }

            val targetChannel = if (isAlarm) ALARM_CHANNEL_ID else CHANNEL_ID
            val soundUri = if (isAlarm) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            val builder = NotificationCompat.Builder(context, targetChannel)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(SANCTUARY_ACCENT_COLOR)
                .setContentTitle(title)
                .setContentText(body)
                .setSubText("Sovereign • $displayPlugin")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                        .setSummaryText(if (isAlarm) "Critical Deadline" else (if (pluginId == "tasks") "Action Priority" else "Sanctuary Prompt"))
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(soundUri, AudioManager.STREAM_ALARM)
                .setVibrate(if (isAlarm) longArrayOf(0, 450, 150, 450, 150, 450) else longArrayOf(0, 250, 150, 250))
                .setLights(SANCTUARY_ACCENT_COLOR, 500, 500)

            // Interactive Actions for Tasks
            if ((pluginId == "tasks" || isAlarm) && id.isNotBlank()) {
                val doneIntent = Intent(context, PluginReminderReceiver::class.java).apply {
                    action = ACTION_TASK_COMPLETE
                    putExtra("id", id)
                    putExtra("notifId", notifId)
                }
                val donePending = PendingIntent.getBroadcast(
                    context,
                    (notifId + 1),
                    doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.checkbox_on_background,
                    "Mark Done",
                    donePending
                )

                val snoozeIntent = Intent(context, PluginReminderReceiver::class.java).apply {
                    action = ACTION_REMINDER_SNOOZE
                    putExtra("id", id)
                    putExtra("pluginId", pluginId)
                    putExtra("title", title)
                    putExtra("body", body)
                    putExtra("isAlarm", isAlarm)
                    putExtra("notifId", notifId)
                }
                val snoozePending = PendingIntent.getBroadcast(
                    context,
                    (notifId + 2),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_lock_idle_alarm,
                    "Snooze 15m",
                    snoozePending
                )
            }

            try {
                val nm = NotificationManagerCompat.from(context)
                nm.notify(notifId, builder.build())
                android.util.Log.d("PluginReminder", "Custom notification posted ($notifId): $title")
                return notifId
            } catch (e: Exception) {
                android.util.Log.e("PluginReminder", "Failed to post notification: ${e.message}")
                return -1
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // 1. Direct interactive notification action: Mark Task as Completed
        if (action == ACTION_TASK_COMPLETE) {
            stopRinging()
            val taskId = intent.getStringExtra("id")?.replace("_due", "")?.replace("_1h", "") ?: ""
            val notifId = intent.getIntExtra("notifId", 0)
            if (notifId != 0) {
                NotificationManagerCompat.from(context).cancel(notifId)
            }
            if (taskId.isNotBlank()) {
                try {
                    val repo = EventRepository.getInstance(context)
                    repo.setTaskStatus(taskId, "completed")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Task marked as completed ✓", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PluginReminder", "Error completing task from notification: ${e.message}")
                }
            }
            return
        }

        // 2. Direct interactive notification action: Snooze Reminder for 15 Minutes
        if (action == ACTION_REMINDER_SNOOZE) {
            stopRinging()
            val id = intent.getStringExtra("id") ?: ""
            val notifId = intent.getIntExtra("notifId", 0)
            val pluginId = intent.getStringExtra("pluginId") ?: "tasks"
            val title = intent.getStringExtra("title") ?: "Task Reminder"
            val body = intent.getStringExtra("body") ?: "Upcoming task deadline"
            val isAlarm = intent.getBooleanExtra("isAlarm", false)

            if (notifId != 0) {
                NotificationManagerCompat.from(context).cancel(notifId)
            }
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                val nextIntent = Intent(context, PluginReminderReceiver::class.java).apply {
                    this.action = ACTION_PLUGIN_REMINDER
                    putExtra("pluginId", pluginId)
                    putExtra("id", id)
                    putExtra("title", title)
                    putExtra("body", body)
                    putExtra("isAlarm", isAlarm)
                }
                val snoozeNotifId = (pluginId + id + "_snooze").hashCode() and 0x7FFFFFFF
                val nextPending = PendingIntent.getBroadcast(
                    context,
                    snoozeNotifId,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val snoozeMs = System.currentTimeMillis() + 15 * 60 * 1000L
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager?.canScheduleExactAlarms() == true) {
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, snoozeMs, nextPending)
                    } else {
                        alarmManager?.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, snoozeMs, nextPending)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager?.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, snoozeMs, nextPending)
                } else {
                    alarmManager?.set(android.app.AlarmManager.RTC_WAKEUP, snoozeMs, nextPending)
                }
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Snoozed for 15 minutes ⏰", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("PluginReminder", "Error snoozing reminder: ${e.message}")
            }
            return
        }

        // 3. Alarm trigger: Fire scheduled reminder notification
        val pluginId = intent.getStringExtra("pluginId") ?: return
        android.util.Log.d("PluginReminder", "Received reminder alarm for plugin: $pluginId")

        // Verify that the plugin is still installed, enabled, and has notification feature toggled on
        val isInternalOrV3 = pluginId in listOf("tasks", "core", "com.v07.core", "timetracker", "daytracker", "sanctuary") || pluginId.startsWith("v07_")
        if (!isInternalOrV3 && !PluginManager.canUseNotifications(pluginId)) {
            android.util.Log.w("PluginReminder", "Plugin $pluginId notifications disallowed in settings or manifest")
            return
        }

        val id = intent.getStringExtra("id") ?: ""
        val title = intent.getStringExtra("title") ?: "Reminder"
        val body = intent.getStringExtra("body") ?: ""
        val isExplicitAlarm = intent.getBooleanExtra("isAlarm", false) || id.endsWith("_due") || pluginId == "tasks"
        val notifId = (pluginId + id).hashCode() and 0x7FFFFFFF

        ensureNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.w("PluginReminder", "POST_NOTIFICATIONS permission not granted")
                return
            }
        }

        val targetClass = try {
            Class.forName("com.v07.v3.app.V3Activity")
        } catch (_: Exception) {
            MainActivity::class.java
        }
        val clickIntent = Intent(context, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openPlugin", pluginId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayPlugin = if (pluginId.isBlank() || pluginId == "core" || pluginId == "com.v07.core") {
            "Sanctuary"
        } else {
            pluginId.replaceFirstChar { it.uppercase() }
        }

        val targetChannel = if (isExplicitAlarm) ALARM_CHANNEL_ID else CHANNEL_ID
        val soundUri = if (isExplicitAlarm) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val builder = NotificationCompat.Builder(context, targetChannel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(SANCTUARY_ACCENT_COLOR)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText("Sovereign • $displayPlugin")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText(if (isExplicitAlarm) "Critical Deadline" else (if (pluginId == "tasks") "Action Priority" else "Sanctuary Prompt"))
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(if (isExplicitAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri, AudioManager.STREAM_ALARM)
            .setVibrate(if (isExplicitAlarm) longArrayOf(0, 450, 150, 450, 150, 450) else longArrayOf(0, 250, 150, 250))
            .setLights(SANCTUARY_ACCENT_COLOR, 500, 500)

        // Interactive Notification Actions for Tasks
        if ((pluginId == "tasks" || isExplicitAlarm) && id.isNotBlank()) {
            val doneIntent = Intent(context, PluginReminderReceiver::class.java).apply {
                this.action = ACTION_TASK_COMPLETE
                putExtra("id", id)
                putExtra("notifId", notifId)
            }
            val donePending = PendingIntent.getBroadcast(
                context,
                (notifId + 1),
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.checkbox_on_background,
                "Mark Done",
                donePending
            )

            val snoozeIntent = Intent(context, PluginReminderReceiver::class.java).apply {
                this.action = ACTION_REMINDER_SNOOZE
                putExtra("id", id)
                putExtra("pluginId", pluginId)
                putExtra("title", title)
                putExtra("body", body)
                putExtra("isAlarm", isExplicitAlarm)
                putExtra("notifId", notifId)
            }
            val snoozePending = PendingIntent.getBroadcast(
                context,
                (notifId + 2),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_lock_idle_alarm,
                "Snooze 15m",
                snoozePending
            )
        }

        try {
            val nm = NotificationManagerCompat.from(context)
            nm.notify(notifId, builder.build())
            android.util.Log.d("PluginReminder", "Fired customized notification $notifId for $pluginId: $title")
        } catch (e: Exception) {
            android.util.Log.e("PluginReminder", "Failed to fire notification: ${e.message}", e)
        }

        // Play audible ringtone for sovereign alarms to guarantee user hears the alert out loud
        if (isExplicitAlarm) {
            try {
                stopRinging()
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, ringtoneUri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ringtone.audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    }
                    ringtone.play()
                    activeAlarmRingtone = ringtone
                    // Automatically stop after 5 seconds if not dismissed sooner
                    Handler(Looper.getMainLooper()).postDelayed({
                        stopRinging()
                    }, 5000L)
                }
            } catch (e: Exception) {
                android.util.Log.w("PluginReminder", "Could not play alert ringtone: ${e.message}")
            }
        }

        // If this is a daily recurring reminder, automatically schedule next occurrence 24 hours later
        if (id == "daily" || intent.getBooleanExtra("repeating", false)) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                val nextIntent = Intent(context, PluginReminderReceiver::class.java).apply {
                    this.action = ACTION_PLUGIN_REMINDER
                    putExtra("pluginId", pluginId)
                    putExtra("id", id)
                    putExtra("title", title)
                    putExtra("body", body)
                    putExtra("isAlarm", isExplicitAlarm)
                    putExtra("repeating", true)
                }
                val nextPending = PendingIntent.getBroadcast(
                    context,
                    notifId,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val nextTriggerMs = System.currentTimeMillis() + 24L * 60L * 60L * 1000L
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager?.canScheduleExactAlarms() == true) {
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, nextTriggerMs, nextPending)
                    } else {
                        alarmManager?.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, nextTriggerMs, nextPending)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager?.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, nextTriggerMs, nextPending)
                } else {
                    alarmManager?.set(android.app.AlarmManager.RTC_WAKEUP, nextTriggerMs, nextPending)
                }
                android.util.Log.d("PluginReminder", "Rescheduled next daily alarm for $pluginId in 24h")
            } catch (e: Exception) {
                android.util.Log.e("PluginReminder", "Failed to reschedule recurring alarm: ${e.message}")
            }
        }
    }
}
