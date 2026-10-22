package com.v07.v3.app.notification

import android.content.Context
import android.content.SharedPreferences
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
 */
class SanctuaryNotificationManager private constructor(
    private val context: Context,
    private val repository: EventRepository
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("v07_sanctuary_notifications", Context.MODE_PRIVATE)

    private val _notifications = MutableStateFlow<List<SanctuaryNotificationItem>>(emptyList())
    val notifications: StateFlow<List<SanctuaryNotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var pollingJob: Job? = null
    private val dismissedIds = mutableSetOf<String>()

    init {
        loadDismissedIds()
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
            val highPriorityTasks = pendingTasks.filter { it.priority.equals("HIGH", ignoreCase = true) }
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
                        id = "tasks_summary_$todayStr",
                        title = "Focus Directives",
                        message = "You have ${pendingTasks.size} tasks in flight today.",
                        category = "tasks",
                        timestamp = now - 300_000L,
                        isRead = readIds.contains("tasks_summary_$todayStr"),
                        priority = NotificationPriority.NORMAL
                    )
                )
            }
        } catch (_: Exception) {}

        // 2. Cadence & Check-In Reminders
        try {
            val todayEvents = repository.getEvents(date = todayStr)
            val hasCheckin = todayEvents.any { it.category == "checkin" }
            val hasCheckout = todayEvents.any { it.category == "checkout" }

            if (hourOfDay in 6..11 && !hasCheckin) {
                items.add(
                    SanctuaryNotificationItem(
                        id = "cadence_morning_$todayStr",
                        title = "Morning Check-In",
                        message = "Ground your awareness and set your core intention for today.",
                        category = "cadence",
                        timestamp = now - 60_000L,
                        isRead = readIds.contains("cadence_morning_$todayStr"),
                        priority = NotificationPriority.NORMAL,
                        actionType = "CHECK_IN"
                    )
                )
            }

            if (hourOfDay in 20..23 && !hasCheckout) {
                items.add(
                    SanctuaryNotificationItem(
                        id = "cadence_evening_$todayStr",
                        title = "Evening Sanctuary Reflection",
                        message = "Acknowledge your wins, log lessons, and prepare for restful slumber.",
                        category = "cadence",
                        timestamp = now - 60_000L,
                        isRead = readIds.contains("cadence_evening_$todayStr"),
                        priority = NotificationPriority.NORMAL,
                        actionType = "CHECK_OUT"
                    )
                )
            }
        } catch (_: Exception) {}

        // 3. Digital Stillness Telemetry
        try {
            val appUsageList = repository.getAppUsageForDate(todayStr)
            val totalUsageMs = appUsageList.sumOf { it.durationMs }
            val totalMinutes = totalUsageMs / 60000L

            if (totalMinutes >= 180L) { // 3+ hours
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                items.add(
                    SanctuaryNotificationItem(
                        id = "stillness_high_$todayStr",
                        title = "Mindful Stillness Advisory",
                        message = "Screen activity has reached ${hours}h ${mins}m today. Consider an unplugged pause.",
                        category = "screentime",
                        timestamp = now - 120_000L,
                        isRead = readIds.contains("stillness_high_$todayStr"),
                        priority = NotificationPriority.HIGH,
                        actionType = "START_FOCUS"
                    )
                )
            }
        } catch (_: Exception) {}

        // 4. Focus Session Flow
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
    }

    fun markAsRead(id: String) {
        markIdAsRead(id)
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    fun markAllAsRead() {
        val current = getReadIds().toMutableSet()
        _notifications.value.forEach { current.add(it.id) }
        prefs.edit().putStringSet("read_ids", current).apply()

        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0
    }

    fun dismiss(id: String) {
        dismissedIds.add(id)
        saveDismissedIds()
        _notifications.value = _notifications.value.filter { it.id != id }
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    fun clearAll() {
        _notifications.value.forEach { dismissedIds.add(it.id) }
        saveDismissedIds()
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }

    companion object {
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
}
