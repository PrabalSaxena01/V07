package com.v07.v3.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.v07.dashboard.PluginReminderReceiver
import com.v07.dashboard.R
import com.v07.v3.app.V3Activity
import com.v07.v3.app.feedback.SanctuaryFeedbackHelper
import com.v07.v3.core.data.EventRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class NotificationPriority { LOW, NORMAL, HIGH, URGENT }

data class SanctuaryNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val category: String, // "tasks", "focus", "cadence", "screentime", "habits", "system"
    val timestamp: Long,
    val isRead: Boolean = false,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val actionType: String? = null,
    val actionPayload: String? = null
)

/**
 * Intelligent background notification polling engine for Sanctuary.
 * Synthesizes timely directives, focus prompts, habit reminders, and digital stillness alerts.
 * Posts customized Android notifications with signature Teal accents and interactive actions.
 */
class SanctuaryNotificationManager private constructor(
    private val context: Context,
    private val repository: EventRepository
) {
    companion object {
        const val CHANNEL_DIRECTIVES = "v07_sanctuary_directives"
        const val CHANNEL_MINDFULNESS = "v07_sanctuary_rhythm"
        const val CHANNEL_STILLNESS = "v07_sanctuary_stillness"

        const val COLOR_SANCTUARY_TEAL = 0xFF2DD4BF.toInt()

        @Volatile
        private var instance: SanctuaryNotificationManager? = null

        fun getInstance(context: Context): SanctuaryNotificationManager {
            return instance ?: synchronized(this) {
                val appCtx = context.applicationContext
                instance ?: SanctuaryNotificationManager(
                    appCtx,
                    EventRepository.getInstance(appCtx)
                ).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("v07_sanctuary_notifications", Context.MODE_PRIVATE)

    private val _notifications = MutableStateFlow<List<SanctuaryNotificationItem>>(emptyList())
    val notifications: StateFlow<List<SanctuaryNotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var pollingJob: Job? = null
    private val dismissedIds = mutableSetOf<String>()
    private val postedSystemIds = mutableSetOf<String>()

    init {
        loadDismissedIds()
        ensureChannels()
    }

    private fun ensureChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return

            if (nm.getNotificationChannel(CHANNEL_DIRECTIVES) == null) {
                val channel = NotificationChannel(
                    CHANNEL_DIRECTIVES,
                    "Sanctuary Directives & Tasks",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High-priority directives, tasks, and deadlines"
                    enableLights(true)
                    lightColor = COLOR_SANCTUARY_TEAL
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 150, 80, 150)
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            if (nm.getNotificationChannel(CHANNEL_MINDFULNESS) == null) {
                val channel = NotificationChannel(
                    CHANNEL_MINDFULNESS,
                    "Sanctuary Daily Rhythm",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Gentle morning check-ins, evening stillness, and habit reminders"
                    enableLights(true)
                    lightColor = COLOR_SANCTUARY_TEAL
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }

            if (nm.getNotificationChannel(CHANNEL_STILLNESS) == null) {
                val channel = NotificationChannel(
                    CHANNEL_STILLNESS,
                    "Digital Stillness & Screen Time",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Screen time threshold alerts and mindfulness reminders"
                    enableLights(true)
                    lightColor = COLOR_SANCTUARY_TEAL
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun loadDismissedIds() {
        val raw = prefs.getStringSet("dismissed_ids", emptySet()) ?: emptySet()
        dismissedIds.addAll(raw)
    }

    private fun saveDismissedIds() {
        prefs.edit().putStringSet("dismissed_ids", dismissedIds).apply()
    }

    private fun getReadIds(): Set<String> {
        return prefs.getStringSet("read_ids", emptySet()) ?: emptySet()
    }

    private fun markIdAsRead(id: String) {
        val current = getReadIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("read_ids", current).apply()
    }

    fun startPolling(scope: CoroutineScope) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    pollOnce()
                } catch (e: Exception) {
                    android.util.Log.w("NotificationManager", "Error during notification polling: ${e.message}")
                }
                delay(45_000L) // Poll every 45 seconds
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    suspend fun pollOnce() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val readIds = getReadIds()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdfDate.format(Date(now))
        val cal = Calendar.getInstance()
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)

        val items = mutableListOf<SanctuaryNotificationItem>()

        // 1. Task Directives: Check high-priority or pending tasks
        try {
            val pendingTasks = repository.getTasks("pending")
            val highPriorityTasks = pendingTasks.filter { it.priority.equals("HIGH", ignoreCase = true) || it.priority.equals("URGENT", ignoreCase = true) }
            if (highPriorityTasks.isNotEmpty()) {
                val t = highPriorityTasks.first()
                items.add(
                    SanctuaryNotificationItem(
                        id = "task_urgent_${t.id}",
                        title = "Priority Directive Pending",
                        message = "\"${t.title}\" awaits your execution.",
                        category = "tasks",
                        timestamp = t.createdAt,
                        isRead = readIds.contains("task_urgent_${t.id}"),
                        priority = NotificationPriority.HIGH,
                        actionType = "COMPLETE_TASK",
                        actionPayload = t.id
                    )
                )
            } else if (pendingTasks.isNotEmpty() && hourOfDay in 9..18) {
                items.add(
                    SanctuaryNotificationItem(
                        id = "tasks_pending_$todayStr",
                        title = "Active Directives",
                        message = "You have ${pendingTasks.size} directives waiting in your queue.",
                        category = "tasks",
                        timestamp = now,
                        isRead = readIds.contains("tasks_pending_$todayStr"),
                        priority = NotificationPriority.NORMAL,
                        actionType = "VIEW_TASKS"
                    )
                )
            }
        } catch (_: Exception) {}

        // 2. Habits & Rituals: Evening or afternoon check
        try {
            val defsJson = repository.getPluginValue("com.v07.habits", "definitions")
            val totalHabits = if (!defsJson.isNullOrBlank()) {
                try { org.json.JSONArray(defsJson).length() } catch (_: Exception) { 0 }
            } else 0
            val logsJson = repository.getPluginValue("com.v07.habits", "logs")
            var completedCount = 0
            if (!logsJson.isNullOrBlank()) {
                try {
                    val arr = org.json.JSONArray(logsJson)
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        if (item.optString("date") == todayStr && item.optBoolean("on", item.optBoolean("completed", true))) {
                            completedCount++
                        }
                    }
                } catch (_: Exception) {}
            }
            val incompleteCount = (totalHabits - completedCount).coerceAtLeast(0)

            if (incompleteCount > 0 && hourOfDay >= 18) {
                items.add(
                    SanctuaryNotificationItem(
                        id = "habits_evening_$todayStr",
                        title = "Evening Rituals Await",
                        message = "$incompleteCount daily ritual${if (incompleteCount > 1) "s" else ""} remaining for completion today.",
                        category = "habits",
                        timestamp = now - 600_000L,
                        isRead = readIds.contains("habits_evening_$todayStr"),
                        priority = NotificationPriority.NORMAL,
                        actionType = "LOG_HABITS"
                    )
                )
            }
        } catch (_: Exception) {}

        // 3. Digital Stillness & Screen Time: Alerts for >3h usage
        try {
            val usages = repository.getAppUsageForDate(todayStr)
            val totalMs = usages.sumOf { it.durationMs }
            val totalHours = totalMs / (1000f * 3600f)
            if (totalHours >= 3.0f) {
                val formattedH = String.format(Locale.US, "%.1fh", totalHours)
                items.add(
                    SanctuaryNotificationItem(
                        id = "screen_time_alert_$todayStr",
                        title = "Digital Stillness Reminder",
                        message = "Device screen usage reached $formattedH today. Pause and rest your senses.",
                        category = "screentime",
                        timestamp = now - 300_000L,
                        isRead = readIds.contains("screen_time_alert_$todayStr"),
                        priority = NotificationPriority.HIGH,
                        actionType = "VIEW_SCREEN_TIME"
                    )
                )
            }
        } catch (_: Exception) {}

        // 4. Daily Rhythm: Morning Mindfulness (07:00 - 10:00) & Evening Reflection (20:00 - 23:30)
        if (hourOfDay in 7..10) {
            items.add(
                SanctuaryNotificationItem(
                    id = "rhythm_morning_$todayStr",
                    title = "Morning Presence Check-in",
                    message = "Set your deliberate focus intentions for a purposeful day.",
                    category = "cadence",
                    timestamp = cal.apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0) }.timeInMillis,
                    isRead = readIds.contains("rhythm_morning_$todayStr"),
                    priority = NotificationPriority.NORMAL,
                    actionType = "MORNING_CHECKIN"
                )
            )
        } else if (hourOfDay in 20..23) {
            items.add(
                SanctuaryNotificationItem(
                    id = "rhythm_evening_$todayStr",
                    title = "Evening Sanctuary Reflection",
                    message = "Unwind your day and review your completed milestones in stillness.",
                    category = "cadence",
                    timestamp = cal.apply { set(Calendar.HOUR_OF_DAY, 21); set(Calendar.MINUTE, 0) }.timeInMillis,
                    isRead = readIds.contains("rhythm_evening_$todayStr"),
                    priority = NotificationPriority.NORMAL,
                    actionType = "EVENING_REFLECTION"
                )
            )
        }

        // 5. Focus Flow: Active or Suggested Focus
        try {
            val activeSession = repository.getActiveSession()
            if (activeSession != null) {
                val durationMins = (now - activeSession.startTime) / 60000L
                items.add(
                    SanctuaryNotificationItem(
                        id = "focus_active_${activeSession.id}",
                        title = "Deep Work Session Active",
                        message = "Focused on \"${activeSession.activity}\" for ${durationMins}m.",
                        category = "focus",
                        timestamp = activeSession.startTime,
                        isRead = true,
                        priority = NotificationPriority.LOW
                    )
                )
            } else if (hourOfDay in 10..17) {
                val todaySessions = repository.getTodaySessions(cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis)

                if (todaySessions.isEmpty()) {
                    items.add(
                        SanctuaryNotificationItem(
                            id = "focus_prompt_$todayStr",
                            title = "Sanctuary Focus Flow",
                            message = "Enter a quiet 25-minute deep work interval.",
                            category = "focus",
                            timestamp = now - 900_000L,
                            isRead = readIds.contains("focus_prompt_$todayStr"),
                            priority = NotificationPriority.NORMAL,
                            actionType = "START_FOCUS"
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // Filter out dismissed notifications
        val filtered = items.filter { !dismissedIds.contains(it.id) }
            .sortedWith(compareBy<SanctuaryNotificationItem> { it.isRead }
                .thenByDescending { it.priority }
                .thenByDescending { it.timestamp })

        _notifications.value = filtered
        _unreadCount.value = filtered.count { !it.isRead }

        // Post newly found high/urgent or unread notifications to Android System Tray
        filtered.filter { !it.isRead && !postedSystemIds.contains(it.id) && it.priority != NotificationPriority.LOW }.forEach {
            dispatchSystemNotification(it)
            postedSystemIds.add(it.id)
        }
    }

    /**
     * Builds and posts an impeccably crafted Android System Notification with Sanctuary styling.
     */
    fun dispatchSystemNotification(item: SanctuaryNotificationItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val channelId = when (item.category) {
            "tasks" -> CHANNEL_DIRECTIVES
            "screentime" -> CHANNEL_STILLNESS
            else -> CHANNEL_MINDFULNESS
        }

        val openIntent = Intent(context, V3Activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_id", item.id)
            putExtra("notification_category", item.category)
        }
        val openPending = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(COLOR_SANCTUARY_TEAL)
            .setContentTitle(item.title)
            .setContentText(item.message)
            .setSubText("Sanctuary · ${item.category.uppercase()}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.message))
            .setPriority(
                when (item.priority) {
                    NotificationPriority.URGENT, NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                    NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(true)
            .setContentIntent(openPending)

        // Custom 1-tap action for task completion
        if (item.category == "tasks" && item.actionPayload != null) {
            val completeIntent = Intent(context, PluginReminderReceiver::class.java).apply {
                action = PluginReminderReceiver.ACTION_TASK_COMPLETE
                putExtra("task_id", item.actionPayload)
                putExtra("notification_id", item.id)
            }
            val completePending = PendingIntent.getBroadcast(
                context,
                (item.id.hashCode() + 101),
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.checkbox_on_background,
                "Complete",
                completePending
            )
        }

        // Action to open sanctuary directly
        builder.addAction(
            android.R.drawable.ic_menu_view,
            "Open Sanctuary",
            openPending
        )

        if (SanctuaryFeedbackHelper.isHapticEnabled(context)) {
            builder.setVibrate(longArrayOf(0, 100, 50, 100))
        }

        try {
            NotificationManagerCompat.from(context).notify(item.id.hashCode(), builder.build())
        } catch (_: SecurityException) {}
    }

    fun cancelSystemNotification(id: String) {
        try {
            NotificationManagerCompat.from(context).cancel(id.hashCode())
        } catch (_: Exception) {}
    }

    fun markAsRead(id: String) {
        markIdAsRead(id)
        cancelSystemNotification(id)
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    fun markAllAsRead() {
        val current = getReadIds().toMutableSet()
        _notifications.value.forEach {
            current.add(it.id)
            cancelSystemNotification(it.id)
        }
        prefs.edit().putStringSet("read_ids", current).apply()

        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0
    }

    fun dismiss(id: String) {
        dismissedIds.add(id)
        saveDismissedIds()
        cancelSystemNotification(id)
        _notifications.value = _notifications.value.filter { it.id != id }
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    fun clearAll() {
        _notifications.value.forEach {
            dismissedIds.add(it.id)
            cancelSystemNotification(it.id)
        }
        saveDismissedIds()
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}
