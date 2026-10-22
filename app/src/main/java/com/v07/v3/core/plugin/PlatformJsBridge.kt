package com.v07.v3.core.plugin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.GitaProvider
import com.v07.v3.core.data.V3Task
import com.v07.v3.core.data.V3Thought
import com.v07.v3.core.data.V3PluginEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * PlatformJsBridge
 *
 * Secure native Android JavaScript interface bound to WebViews.
 * Translates window.V07Platform RPC calls to core native services while enforcing
 * manifest-declared permissions for every caller.
 */
class PlatformJsBridge(
    private val context: Context,
    private val repository: EventRepository,
    private val timerManager: TimerManager,
    private val screenTimeTracker: ScreenTimeTracker,
    private val registry: PluginRegistry,
    private val syncManager: com.v07.v3.core.sync.V3CompanionSyncManager
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    var activity: com.v07.v3.app.V3Activity? = null

    @JavascriptInterface
    fun invoke(pluginId: String, method: String, paramsJson: String): String {
        return try {
            val pluginRecord = registry.getPlugin(pluginId)
            val manifest = pluginRecord?.manifest

            // Permission check
            when {
                method.startsWith("storage:") -> {
                    if (manifest != null && !manifest.hasPermission("storage:isolated")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'storage:isolated' permission")
                    }
                }
                method.startsWith("telemetry:") || method.startsWith("timer:") -> {
                    if (manifest != null && !manifest.hasPermission("telemetry:read")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'telemetry:read' permission")
                    }
                }
                method.startsWith("events:") -> {
                    if (manifest != null && !manifest.hasPermission("events:listen") && !manifest.hasPermission("events:emit")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'events:listen' or 'events:emit' permission")
                    }
                }
                method.startsWith("ai:") -> {
                    if (manifest != null && !manifest.hasPermission("ai")) {
                        return errorJson("Permission Denied: Plugin '$pluginId' requires 'ai' permission")
                    }
                }
            }

            val params = JSONObject(if (paramsJson.isNotBlank()) paramsJson else "{}")

            when (method) {
                // Storage API
                "storage:get" -> {
                    val key = params.optString("key")
                    val value = repository.getPluginValue(pluginId, key)
                    val res = JSONObject().apply {
                        put("found", value != null)
                        put("value", value ?: JSONObject.NULL)
                    }
                    successJson(res)
                }
                "storage:set" -> {
                    val key = params.optString("key")
                    val value = params.optString("value")
                    val ok = repository.setPluginValue(pluginId, key, value)
                    val res = JSONObject().put("success", ok)
                    successJson(res)
                }
                "storage:delete" -> {
                    val key = params.optString("key")
                    val ok = repository.deletePluginValue(pluginId, key)
                    val res = JSONObject().put("success", ok)
                    successJson(res)
                }
                "storage:keys" -> {
                    val keys = repository.getAllPluginKeys(pluginId)
                    val arr = JSONArray()
                    keys.forEach { arr.put(it) }
                    val res = JSONObject().put("keys", arr)
                    successJson(res)
                }

                // Plugin Domain Events API
                "events:insert" -> {
                    val eventId = params.optString("id").ifBlank { "ev_" + UUID.randomUUID().toString().take(8) }
                    val category = params.optString("category", "general")
                    val date = params.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    val ts = params.optLong("timestamp", params.optLong("ts", System.currentTimeMillis()))
                    val payload = params.optString("payloadJson", params.toString())
                    val ok = repository.insertPluginEvent(
                        V3PluginEvent(
                            id = eventId,
                            pluginId = pluginId,
                            category = category,
                            payloadJson = payload,
                            timestamp = ts,
                            date = date
                        )
                    )
                    successJson(JSONObject().put("success", ok).put("id", eventId))
                }
                "events:query" -> {
                    val category = params.optString("category", "").ifBlank { null }
                    val date = params.optString("date", "").ifBlank { null }
                    val list = if (category != null) {
                        repository.getPluginEventsByCategory(pluginId, category, date)
                    } else {
                        repository.getPluginEvents(pluginId, 200)
                    }
                    val arr = JSONArray()
                    list.forEach { pe ->
                        arr.put(JSONObject().apply {
                            put("id", pe.id)
                            put("pluginId", pe.pluginId)
                            put("category", pe.category)
                            put("payloadJson", pe.payloadJson)
                            put("timestamp", pe.timestamp)
                            put("date", pe.date)
                        })
                    }
                    successJson(JSONObject().put("events", arr))
                }
                "events:delete" -> {
                    val eventId = params.optString("id")
                    val ok = if (eventId.isNotBlank()) {
                        repository.deleteEntityEverywhere(eventId, "plugin_event")
                    } else false
                    successJson(JSONObject().put("success", ok))
                }

                // Telemetry API
                "telemetry:getScreenTime" -> {
                    val requestedDate = params.optString("date", "").ifBlank { null }
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val (dateStr, totalMs, appCount) = if (requestedDate != null && requestedDate != todayStr) {
                        val tl = repository.getDayTimeline(requestedDate)
                        Triple(requestedDate, tl.totalScreenTimeMs, 0)
                    } else {
                        val summary = runBlocking { screenTimeTracker.collectNow() }
                        Triple(summary.date, summary.totalDurationMs, summary.appCount)
                    }
                    val res = JSONObject().apply {
                        put("date", dateStr)
                        put("total_duration_ms", totalMs)
                        put("total_duration_sec", totalMs / 1000)
                        put("app_count", appCount)
                    }
                    successJson(res)
                }
                "telemetry:getDetailedScreenTime", "telemetry:getSummary" -> {
                    val requestedDate = params.optString("date", "").ifBlank { null }
                    val summary = runBlocking {
                        if (requestedDate != null) screenTimeTracker.collectForDate(requestedDate)
                        else screenTimeTracker.collectNow()
                    }
                    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val hasPerm = screenTimeTracker.hasUsageStatsPermission() || summary.totalDurationMs > 0L || (requestedDate != null && requestedDate != todayDateStr)
                    val appsArr = JSONArray()
                    summary.topApps.take(15).forEach { a ->
                        val totalSec = a.durationMs / 1000
                        val hrs = totalSec / 3600
                        val mins = (totalSec % 3600) / 60
                        val formatted = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                        appsArr.put(JSONObject().apply {
                            put("packageName", a.packageName)
                            put("appName", a.appName)
                            put("durationMs", a.durationMs)
                            put("durationSec", totalSec)
                            put("formatted", formatted)
                            put("durationFormatted", formatted)
                            put("category", a.category)
                            put("percentage", a.percentage)
                            put("percentOfTotal", a.percentage)
                            put("launchCount", a.launchCount)
                        })
                    }
                    val totalSec = summary.totalDurationMs / 1000
                    val totalHrs = totalSec / 3600
                    val totalMins = (totalSec % 3600) / 60
                    val formattedTotal = if (totalHrs > 0) "${totalHrs}h ${totalMins}m" else "${totalMins}m"

                    val weeklyArr = JSONArray()
                    val weekly = screenTimeTracker.getWeeklyHistory()
                    weekly.forEach { w ->
                        weeklyArr.put(JSONObject().apply {
                            put("date", w.date)
                            put("dayOfWeek", w.dayOfWeek)
                            put("totalDurationMs", w.totalDurationMs)
                            put("formatted", w.formatted)
                            put("isToday", w.isToday)
                        })
                    }

                    val hourlyArr = JSONArray()
                    summary.hourlyMinutes.forEach { hourlyArr.put(it) }

                    val res = JSONObject().apply {
                        put("hasPermission", hasPerm)
                        put("totalDurationMs", summary.totalDurationMs)
                        put("totalDurationSec", totalSec)
                        put("formattedTotal", formattedTotal)
                        put("totalFormatted", formattedTotal)
                        put("appCount", summary.appCount)
                        put("topApps", appsArr)
                        put("weeklyHistory", weeklyArr)
                        put("hourlyMinutes", hourlyArr)
                    }
                    successJson(res)
                }
                "telemetry:getWeekly" -> {
                    val weekly = screenTimeTracker.getWeeklyHistory()
                    val weeklyArr = JSONArray()
                    weekly.forEach { w ->
                        weeklyArr.put(JSONObject().apply {
                            put("date", w.date)
                            put("dayOfWeek", w.dayOfWeek)
                            put("totalDurationMs", w.totalDurationMs)
                            put("formatted", w.formatted)
                            put("isToday", w.isToday)
                        })
                    }
                    successJson(JSONObject().put("days", weeklyArr))
                }
                "telemetry:requestPermission" -> {
                    mainHandler.post {
                        try {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", true))
                }
                "telemetry:hasUsagePermission" -> {
                    val hasPerm = screenTimeTracker.hasUsageStatsPermission()
                    successJson(JSONObject().put("hasPermission", hasPerm).put("granted", hasPerm))
                }

                // Overlay API
                "overlay:getStatus" -> {
                    val enabled = com.v07.v3.app.overlay.V3BubbleOverlayService.isOverlayEnabled(context)
                    val running = com.v07.v3.app.overlay.V3BubbleOverlayService.isRunning
                    val hasOverlay = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Settings.canDrawOverlays(context)
                    } else true
                    val hasUsage = screenTimeTracker.hasUsageStatsPermission()
                    val res = JSONObject().apply {
                        put("enabled", enabled)
                        put("running", running)
                        put("hasOverlayPermission", hasOverlay)
                        put("hasUsagePermission", hasUsage)
                    }
                    successJson(res)
                }
                "overlay:start" -> {
                    val hasOverlay = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Settings.canDrawOverlays(context)
                    } else true
                    if (!hasOverlay) {
                        errorJson("OVERLAY_PERMISSION_REQUIRED")
                    } else {
                        mainHandler.post {
                            com.v07.v3.app.overlay.V3BubbleOverlayService.start(context)
                        }
                        successJson(JSONObject().put("success", true).put("running", true))
                    }
                }
                "overlay:stop" -> {
                    mainHandler.post {
                        com.v07.v3.app.overlay.V3BubbleOverlayService.stop(context)
                    }
                    successJson(JSONObject().put("success", true).put("running", false))
                }
                "overlay:toggle" -> {
                    val running = com.v07.v3.app.overlay.V3BubbleOverlayService.isRunning
                    val hasOverlay = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Settings.canDrawOverlays(context)
                    } else true
                    if (!running && !hasOverlay) {
                        errorJson("OVERLAY_PERMISSION_REQUIRED")
                    } else {
                        mainHandler.post {
                            com.v07.v3.app.overlay.V3BubbleOverlayService.toggle(context)
                        }
                        successJson(JSONObject().put("success", true).put("running", !running))
                    }
                }
                "overlay:requestPermission" -> {
                    mainHandler.post {
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                        } catch (_: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", true))
                }

                // Timer API
                "timer:start" -> {
                    val topic = params.optString("topic", params.optString("title", "Focus Sanctuary"))
                    val category = params.optString("category", "Sanctuary")
                    val modeStr = params.optString("mode", "COUNTDOWN")
                    val mode = if (modeStr.equals("STOPWATCH", ignoreCase = true) || modeStr.equals("timer", ignoreCase = true)) {
                        TimerMode.STOPWATCH
                    } else {
                        TimerMode.COUNTDOWN
                    }
                    val targetMinutes = when {
                        params.has("targetMinutes") -> params.optInt("targetMinutes", 25)
                        params.has("durationSec") -> (params.optInt("durationSec", 1500) / 60).coerceAtLeast(1)
                        params.has("duration") -> (params.optInt("duration", 1500) / 60).coerceAtLeast(1)
                        else -> 25
                    }
                    val state = timerManager.startSession(topic, category, mode, targetMinutes)
                    val res = JSONObject().apply {
                        put("success", true)
                        put("status", state.status.name)
                        put("formatted", state.getFormattedTime())
                    }
                    successJson(res)
                }
                "timer:cancel", "timer:discard", "timer:stop" -> {
                    timerManager.discardSession()
                    val res = JSONObject().apply {
                        put("success", true)
                        put("status", "IDLE")
                    }
                    successJson(res)
                }
                "timer:pause" -> {
                    val state = timerManager.pauseSession()
                    val res = JSONObject().apply {
                        put("success", state != null)
                        put("status", state?.status?.name ?: "IDLE")
                    }
                    successJson(res)
                }
                "timer:resume" -> {
                    val state = timerManager.resumeSession()
                    val res = JSONObject().apply {
                        put("success", state != null)
                        put("status", state?.status?.name ?: "IDLE")
                    }
                    successJson(res)
                }
                "timer:addMinutes" -> {
                    val delta = params.optInt("minutes", 5)
                    val state = timerManager.addMinutes(delta)
                    val res = JSONObject().apply {
                        put("success", state != null)
                        if (state != null) {
                            put("remaining_ms", state.getRemainingMs())
                            put("formatted", state.getFormattedTime())
                        }
                    }
                    successJson(res)
                }
                "timer:finish" -> {
                    val notes = params.optString("notes", "")
                    val session = timerManager.finishSession(notes.ifBlank { null })
                    if (session != null) {
                        val now = session.endTime ?: System.currentTimeMillis()
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                        val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                        val durSec = session.durationSec
                        val mins = (durSec / 60L).coerceAtLeast(1L)
                        val ev = com.v07.v3.core.data.V3Event(
                            id = session.id,
                            category = "session",
                            date = date,
                            ts = ts,
                            timestamp = now,
                            title = session.activity,
                            content = "${mins}m focus session${if (!session.notes.isNullOrBlank()) " • ${session.notes}" else ""}",
                            metadataJson = JSONObject().put("durationSec", durSec).put("activity", session.activity).toString()
                        )
                        repository.insertEvent(ev)
                    }
                    val res = JSONObject().apply {
                        put("success", session != null)
                        if (session != null) put("id", session.id)
                    }
                    successJson(res)
                }
                "timer:getState" -> {
                    val state = timerManager.timerState.value
                    val res = JSONObject().apply {
                        put("status", state.status.name)
                        put("topic", state.topic)
                        put("category", state.category)
                        put("mode", state.mode.name)
                        put("elapsed_ms", state.getElapsedMs())
                        put("remaining_ms", state.getRemainingMs())
                        put("formatted", state.getFormattedTime())
                    }
                    successJson(res)
                }
                "timer:getNotificationConfig" -> {
                    val config = com.v07.v3.app.timer.FocusNotificationConfig.getInstance(context)
                    successJson(config.toJsonObject())
                }
                "timer:saveNotificationConfig" -> {
                    val config = com.v07.v3.app.timer.FocusNotificationConfig.getInstance(context)
                    config.updateFromJson(params)
                    com.v07.v3.app.timer.FocusTimerService.updateNotification(context)
                    successJson(JSONObject().put("success", true).put("config", config.toJsonObject()))
                }

                // Day Timeline API
                "day:timeline" -> {
                    val dateStr = params.optString("date", "").ifBlank {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    }
                    val timeline = repository.getDayTimeline(dateStr)
                    val evArr = JSONArray()
                    timeline.events.forEach { e ->
                        evArr.put(JSONObject().apply {
                            put("id", e.id)
                            put("timestamp", e.timestamp)
                            put("timeFormatted", e.timeFormatted)
                            put("category", e.category)
                            put("title", e.title)
                            put("subtitle", e.subtitle)
                            e.durationSec?.let { put("durationSec", it) }
                            put("isHighlight", e.isHighlight)
                        })
                    }
                    val res = JSONObject().apply {
                        put("date", timeline.date)
                        put("totalFocusSec", timeline.totalFocusSec)
                        put("sessionCount", timeline.sessionCount)
                        put("tasksCompletedCount", timeline.tasksCompletedCount)
                        put("thoughtsCount", timeline.thoughtsCount)
                        put("totalScreenTimeMs", timeline.totalScreenTimeMs)
                        put("events", evArr)
                    }
                    successJson(res)
                }

                // Sessions API (Elapsed Focus Intervals)
                "sessions:list", "sessions:get" -> {
                    val dateStr = params.optString("date", "").ifBlank {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    }
                    val timeline = repository.getDayTimeline(dateStr)
                    val arr = JSONArray()
                    timeline.events.filter { it.category == "sessions" || it.category == "session" }.forEach { s ->
                        arr.put(JSONObject().apply {
                            put("id", s.id)
                            put("title", s.title)
                            put("subtitle", s.subtitle)
                            put("timeFormatted", s.timeFormatted)
                            put("durationSec", s.durationSec ?: 0)
                            put("timestamp", s.timestamp)
                        })
                    }
                    successJson(JSONObject().put("sessions", arr))
                }
                "sessions:add" -> {
                    val act = params.optString("activity", "Deep Work")
                    val durSec = params.optLong("durationSec", 1500L)
                    val notes = params.optString("notes", "")
                    val now = System.currentTimeMillis()
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                    val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                    val id = "ses_" + UUID.randomUUID().toString().take(8)
                    val session = com.v07.v3.core.data.V3Session(
                        id = id,
                        activity = act,
                        startTime = now - (durSec * 1000),
                        endTime = now,
                        durationSec = durSec,
                        notes = notes
                    )
                    repository.insertSession(session)
                    val event = com.v07.v3.core.data.V3Event(
                        id = id,
                        category = "session",
                        date = date,
                        ts = ts,
                        timestamp = now,
                        title = act,
                        content = "${durSec / 60}m focus session${if (notes.isNotBlank()) " • $notes" else ""}",
                        metadataJson = JSONObject().put("durationSec", durSec).put("activity", act).toString()
                    )
                    repository.insertEvent(event)
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", true).put("id", id))
                }
                "sessions:delete", "session:delete" -> {
                    val id = params.optString("id", "")
                    val ok = repository.recycleItem(id, "session", "{}")
                    if (ok && id.isNotBlank()) {
                        syncManager.deleteRemoteEvent(id)
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }

                // Tasks API
                "tasks:list", "tasks:get" -> {
                    val statusFilter = params.optString("status", "").ifBlank { null }
                    val tasks = repository.getTasks(statusFilter)
                    val arr = JSONArray()
                    tasks.forEach { t ->
                        arr.put(JSONObject().apply {
                            put("id", t.id)
                            put("title", t.title)
                            put("priority", t.priority)
                            put("status", t.status)
                            put("done", t.status.equals("completed", ignoreCase = true) || t.status.equals("done", ignoreCase = true))
                            put("created_at", t.createdAt)
                            put("completed_at", t.completedAt)
                            put("deadline", t.deadline ?: "")
                        })
                    }
                    successJson(JSONObject().put("tasks", arr))
                }
                "tasks:add" -> {
                    val title = params.optString("title", "").trim()
                    if (title.isBlank()) {
                        return errorJson("Task title cannot be blank")
                    }
                    val deadline = params.optString("deadline", "").ifBlank { null }
                    val task = V3Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        priority = params.optString("priority", "MEDIUM").uppercase(),
                        status = "pending",
                        createdAt = System.currentTimeMillis(),
                        deadline = deadline
                    )
                    val ok = repository.insertTask(task)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().apply {
                        put("success", ok)
                        put("id", task.id)
                        put("task", JSONObject().apply {
                            put("id", task.id)
                            put("title", task.title)
                            put("priority", task.priority)
                            put("status", task.status)
                            put("done", false)
                            put("created_at", task.createdAt)
                            put("deadline", task.deadline ?: "")
                        })
                    })
                }
                "tasks:toggle" -> {
                    val id = params.optString("id", "")
                    val ok = repository.toggleTaskStatus(id)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }
                "tasks:delete" -> {
                    val id = params.optString("id", "")
                    val ok = repository.recycleItem(id, "task", "{}")
                    if (ok && id.isNotBlank()) {
                        syncManager.deleteRemoteEvent(id)
                    }
                    successJson(JSONObject().put("success", ok))
                }
                "tasks:update" -> {
                    val id = params.optString("id", "")
                    val title = params.optString("title", "").ifBlank { null }
                    val priority = params.optString("priority", "").ifBlank { null }
                    val deadline = params.optString("deadline", "").ifBlank { null }
                    val ok = repository.updateTaskDetails(id, title, priority, deadline)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", ok))
                }

                // Instant Custom Android Notification API
                "notifications:requestPermission" -> {
                    if (Build.VERSION.SDK_INT >= 33) {
                        val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            mainHandler.post {
                                activity?.requestNotificationPermission()
                            }
                        }
                        successJson(JSONObject().put("requested", true).put("granted", granted))
                    } else {
                        val granted = NotificationManagerCompat.from(context).areNotificationsEnabled()
                        successJson(JSONObject().put("requested", false).put("granted", granted))
                    }
                }
                "notifications:checkPermission", "notifications:hasPermission" -> {
                    val granted = if (Build.VERSION.SDK_INT >= 33) {
                        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else {
                        NotificationManagerCompat.from(context).areNotificationsEnabled()
                    }
                    successJson(JSONObject().put("granted", granted).put("hasPermission", granted))
                }
                "notifications:send", "notifications:post", "notifications:test" -> {
                    val title = params.optString("title", "Sanctuary Notification")
                    val body = params.optString("body", "Stay centered. A mindful breath restores clear presence.")
                    val pluginId = params.optString("pluginId", "sanctuary")
                    val id = params.optString("id", UUID.randomUUID().toString())
                    val notifId = com.v07.dashboard.PluginReminderReceiver.postCustomNotification(
                        context, pluginId, id, title, body
                    )
                    successJson(JSONObject().put("success", notifId != -1).put("notifId", notifId))
                }

                // Alarm & 1-Hour Reminder API for Tasks & Milestones
                "alarms:schedule", "reminders:schedule", "tasks:scheduleReminder" -> {
                    com.v07.dashboard.PluginReminderReceiver.ensureNotificationChannel(context)
                    val id = params.optString("id", UUID.randomUUID().toString())
                    val title = params.optString("title", "Task Reminder").ifBlank { "Task Reminder" }
                    val body = params.optString("body", params.optString("message", "Upcoming task deadline in 1 hour"))
                    val triggerAtMs = params.optLong("triggerAtMs", 0L)
                    val pluginId = params.optString("pluginId", "tasks")
                    val isAlarm = params.optBoolean("isAlarm", id.endsWith("_due") || pluginId == "tasks")

                    if (triggerAtMs > System.currentTimeMillis()) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                        val notifId = (pluginId + id).hashCode() and 0x7FFFFFFF
                        val intent = Intent(context, com.v07.dashboard.PluginReminderReceiver::class.java).apply {
                            action = "com.v07.dashboard.ACTION_PLUGIN_REMINDER"
                            putExtra("pluginId", pluginId)
                            putExtra("id", id)
                            putExtra("title", title)
                            putExtra("body", body)
                            putExtra("isAlarm", isAlarm)
                            putExtra("repeating", false)
                        }
                        val pendingIntent = android.app.PendingIntent.getBroadcast(
                            context,
                            notifId,
                            intent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                if (alarmManager?.canScheduleExactAlarms() == true) {
                                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                                } else {
                                    alarmManager?.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                                }
                            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                alarmManager?.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                            } else {
                                alarmManager?.set(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                            }
                            successJson(JSONObject().put("scheduled", true).put("id", id).put("triggerAtMs", triggerAtMs))
                        } catch (e: Exception) {
                            errorJson("Failed to schedule alarm: ${e.message}")
                        }
                    } else {
                        successJson(JSONObject().put("scheduled", false).put("reason", "time_in_past"))
                    }
                }
                "alarms:cancel", "reminders:cancel", "tasks:cancelReminder" -> {
                    val id = params.optString("id", "")
                    val pluginId = params.optString("pluginId", "tasks")
                    if (id.isNotBlank()) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                        listOf(id, "${id}_due", "${id}_1h").forEach { subId ->
                            val notifId = (pluginId + subId).hashCode() and 0x7FFFFFFF
                            val intent = Intent(context, com.v07.dashboard.PluginReminderReceiver::class.java).apply {
                                action = "com.v07.dashboard.ACTION_PLUGIN_REMINDER"
                            }
                            val pendingIntent = android.app.PendingIntent.getBroadcast(
                                context,
                                notifId,
                                intent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )
                            alarmManager?.cancel(pendingIntent)
                            pendingIntent.cancel()
                        }
                    }
                    successJson(JSONObject().put("cancelled", true))
                }

                // Time Tracker / Day Tracker API (Milestones, Past & Future Countdowns)
                "timetracker:list", "daytracker:list" -> {
                    val items = repository.getAllDayTrackerItems()
                    val arr = JSONArray()
                    items.forEach { item ->
                        arr.put(JSONObject().apply {
                            put("id", item.id)
                            put("title", item.title)
                            put("targetDate", item.targetDate)
                            put("target_date", item.targetDate)
                            put("hour", item.hour)
                            put("pinned", item.pinned)
                            put("createdAt", item.createdAt)
                            put("created_at", item.createdAt)
                        })
                    }
                    successJson(JSONObject().put("items", arr))
                }
                "timetracker:add", "daytracker:add" -> {
                    val title = params.optString("title", "").trim()
                    if (title.isBlank()) return errorJson("Milestone title cannot be blank")
                    val targetDate = params.optString("targetDate", params.optString("target_date", "")).trim()
                    if (targetDate.isBlank()) return errorJson("Target date required (YYYY-MM-DD)")
                    val hour = params.optInt("hour", 0)
                    val pinned = params.optBoolean("pinned", false)
                    val item = com.v07.v3.core.data.V3DayTrackerItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        targetDate = targetDate,
                        hour = hour,
                        pinned = pinned,
                        createdAt = System.currentTimeMillis()
                    )
                    val ok = repository.insertDayTrackerItem(item)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", ok).put("id", item.id))
                }
                "timetracker:togglePinned", "daytracker:togglePinned" -> {
                    val id = params.optString("id", "")
                    val ok = repository.toggleDayTrackerPinned(id)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", ok))
                }
                "timetracker:delete", "daytracker:delete" -> {
                    val id = params.optString("id", "")
                    val ok = repository.recycleItem(id, "day_tracker", "{}")
                    if (ok && id.isNotBlank()) {
                        syncManager.deleteRemoteEvent(id)
                        syncManager.deleteRemoteEvent("dt_$id")
                    }
                    successJson(JSONObject().put("success", ok))
                }

                // Thoughts / Reflections API
                "thoughts:list" -> {
                    val thoughts = repository.getRecentThoughts(50)
                    val arr = JSONArray()
                    thoughts.forEach { th ->
                        arr.put(JSONObject().apply {
                            put("id", th.id)
                            put("content", th.content)
                            put("date", th.date)
                            put("timestamp", th.timestamp)
                            put("topic", th.topic ?: "")
                            put("tags", th.tags ?: "")
                        })
                    }
                    successJson(JSONObject().put("thoughts", arr))
                }
                "thoughts:add" -> {
                    val content = (if (params.has("content")) params.optString("content", "")
                                   else if (params.has("text")) params.optString("text", "")
                                   else params.optString("note", "")).trim()
                    if (content.isBlank()) {
                        return errorJson("Content cannot be blank")
                    }
                    val topic = (if (params.has("topic")) params.optString("topic", "")
                                 else params.optString("title", "")).ifBlank { "Thought" }
                    val tags = params.optString("tags", "").ifBlank { null }
                    val now = System.currentTimeMillis()
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val date = sdf.format(Date(now))
                    val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                    val thoughtId = "thought_" + UUID.randomUUID().toString().take(8)

                    val thought = V3Thought(
                        id = thoughtId,
                        timestamp = now,
                        date = date,
                        topic = topic,
                        content = content,
                        tags = tags
                    )
                    val ok = repository.insertThought(thought)

                    // Unified atomic insertion into events table so it appears in Ledger, Data Center, and Day
                    val event = com.v07.v3.core.data.V3Event(
                        id = thoughtId,
                        category = "thought",
                        date = date,
                        ts = ts,
                        timestamp = now,
                        title = topic,
                        content = content,
                        score = 0,
                        metadataJson = if (tags != null) JSONObject().put("tags", tags).toString() else "{}"
                    )
                    repository.insertEvent(event)

                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", ok).put("id", thoughtId))
                }
                "thoughts:delete" -> {
                    val id = params.optString("id", "")
                    val ok = repository.deleteThought(id)
                    if (ok && id.isNotBlank()) {
                        syncManager.deleteRemoteEvent(id)
                    }
                    successJson(JSONObject().put("success", ok))
                }

                // UI API
                "ui:toast" -> {
                    val msg = params.optString("message", "")
                    if (msg.isNotBlank()) {
                        mainHandler.post {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    successJson(JSONObject().put("success", true))
                }

                // Plugin Management API
                "plugin:list" -> {
                    val all = registry.getAllPlugins()
                    val arr = JSONArray()
                    all.forEach { rec ->
                        arr.put(JSONObject().apply {
                            put("id", rec.manifest.id)
                            put("name", rec.manifest.name)
                            put("version", rec.manifest.version)
                            put("description", rec.manifest.description)
                            put("publisher", rec.manifest.publisher)
                            put("icon", rec.manifest.navIcon)
                            put("color", rec.manifest.navColor)
                            put("state", rec.state.name)
                            put("isBuiltIn", rec.isBuiltIn)
                            put("permissions", JSONArray(rec.manifest.permissions))
                        })
                    }
                    successJson(JSONObject().put("plugins", arr))
                }
                "plugin:enable" -> {
                    val targetId = params.optString("id", "")
                    val ok = registry.enablePlugin(targetId)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", ok).put("id", targetId))
                }
                "plugin:disable" -> {
                    val targetId = params.optString("id", "")
                    val ok = registry.disablePlugin(targetId)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", ok).put("id", targetId))
                }
                "plugin:register" -> {
                    val manifestJson = params.optString("manifest", "")
                    val manifest = PluginManifestParser.parse(manifestJson)
                    if (manifest == null) {
                        return errorJson("Invalid plugin manifest JSON")
                    }
                    val ok = registry.registerCustomPlugin(manifest)
                    successJson(JSONObject().put("success", ok).put("id", manifest.id))
                }
                "plugin:unregister" -> {
                    val targetId = params.optString("id", "")
                    val ok = registry.unregisterPlugin(targetId)
                    try {
                        val pDir = java.io.File(context.filesDir, "plugins/$targetId")
                        if (pDir.exists()) pDir.deleteRecursively()
                    } catch (_: Exception) {}
                    successJson(JSONObject().put("success", ok).put("id", targetId))
                }
                "plugin:importZip" -> {
                    val act = activity
                    if (act == null) {
                        return errorJson("Activity not attached to bridge")
                    }
                    mainHandler.post {
                        act.launchZipPicker { _ -> }
                    }
                    successJson(JSONObject().put("success", true).put("launched", true))
                }
                "plugin:installZipBase64" -> {
                    val act = activity
                    if (act == null) {
                        return errorJson("Activity not attached to bridge")
                    }
                    val b64 = params.optString("data", "")
                    if (b64.isBlank()) {
                        return errorJson("No zip data provided")
                    }
                    val rawBytes = try {
                        val clean = if (b64.contains(",")) b64.substringAfter(",") else b64
                        android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                    } catch (e: Exception) {
                        return errorJson("Failed to decode base64: ${e.message}")
                    }
                    val res = act.installPluginFromZipBytes(rawBytes)
                    if (res.optBoolean("success")) {
                        successJson(res)
                    } else {
                        errorJson(res.optString("error", "Failed to install plugin zip"))
                    }
                }
                "plugin:event:insert" -> {
                    val pId = params.optString("pluginId", pluginId).ifBlank { pluginId }
                    val cat = params.optString("category", "plugin")
                    val id = params.optString("id", UUID.randomUUID().toString())
                    val payload = params.optString("payloadJson", params.optString("payload", "{}"))
                    val now = System.currentTimeMillis()
                    val date = params.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now)))
                    val pe = V3PluginEvent(
                        id = id,
                        pluginId = pId,
                        category = cat,
                        payloadJson = payload,
                        timestamp = now,
                        date = date
                    )
                    val ok = repository.insertPluginEvent(pe)
                    if (ok) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                        }
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }
                "plugin:event:query" -> {
                    val pId = params.optString("pluginId", pluginId).ifBlank { pluginId }
                    val cat = params.optString("category", "").ifBlank { null }
                    val date = params.optString("date", "").ifBlank { null }
                    val events = if (cat != null) {
                        repository.getPluginEventsByCategory(pId, cat, date)
                    } else {
                        repository.getPluginEvents(pId, 100)
                    }
                    val arr = JSONArray()
                    events.forEach { e ->
                        arr.put(JSONObject().apply {
                            put("id", e.id)
                            put("pluginId", e.pluginId)
                            put("category", e.category)
                            put("payloadJson", e.payloadJson)
                            put("timestamp", e.timestamp)
                            put("date", e.date)
                        })
                    }
                    successJson(JSONObject().put("success", true).put("events", arr))
                }
                "plugin:event:delete" -> {
                    val id = params.optString("id", "")
                    val ok = repository.deletePluginEvent(id)
                    if (ok && id.isNotBlank()) {
                        syncManager.deleteRemoteEvent(id)
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }

                // Cadence API (Daily Attendance: Check-In & Check-Out)
                "cadence:checkin" -> {
                    val mood = params.optInt("mood", 3)
                    val energy = params.optInt("energy", 3)
                    val focus = params.optInt("focus", 3)
                    val intention = params.optString("intention", "").trim()
                    val now = System.currentTimeMillis()
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                    val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                    val meta = JSONObject().apply {
                        put("mood", mood)
                        put("energy", energy)
                        put("focus", focus)
                        put("intention", intention)
                    }.toString()

                    val event = com.v07.v3.core.data.V3Event(
                        id = "ci_" + UUID.randomUUID().toString().take(8),
                        category = "checkin",
                        date = date,
                        ts = ts,
                        timestamp = now,
                        title = "Morning Check-In",
                        content = intention,
                        score = mood,
                        metadataJson = meta
                    )
                    val ok = repository.insertEvent(event)
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", ok).put("id", event.id))
                }
                "cadence:checkout" -> {
                    val wins = params.optString("wins", "").trim()
                    val lessons = params.optString("lessons", "").trim()
                    val reflection = params.optString("reflection", "").trim()
                    val score = params.optInt("score", 4)
                    val now = System.currentTimeMillis()
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                    val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                    val meta = JSONObject().apply {
                        put("wins", wins)
                        put("lessons", lessons)
                        put("reflection", reflection)
                        put("score", score)
                    }.toString()

                    val event = com.v07.v3.core.data.V3Event(
                        id = "co_" + UUID.randomUUID().toString().take(8),
                        category = "checkout",
                        date = date,
                        ts = ts,
                        timestamp = now,
                        title = "Evening Check-Out",
                        content = reflection,
                        score = score,
                        metadataJson = meta
                    )
                    repository.insertEvent(event)

                    if (wins.isNotBlank()) {
                        repository.insertEvent(
                            com.v07.v3.core.data.V3Event(
                                id = "win_" + UUID.randomUUID().toString().take(8),
                                category = "win",
                                date = date,
                                ts = ts,
                                timestamp = now + 1,
                                title = "Daily Win",
                                content = wins
                            )
                        )
                    }
                    if (lessons.isNotBlank()) {
                        repository.insertEvent(
                            com.v07.v3.core.data.V3Event(
                                id = "les_" + UUID.randomUUID().toString().take(8),
                                category = "lesson",
                                date = date,
                                ts = ts,
                                timestamp = now + 2,
                                title = "Daily Lesson",
                                content = lessons
                            )
                        )
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", true).put("id", event.id))
                }
                "cadence:getStatus" -> {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val events = repository.getEvents(date = today)
                    val ci = events.find { it.category == "checkin" }
                    val co = events.find { it.category == "checkout" }
                    val res = JSONObject().apply {
                        put("hasCheckin", ci != null)
                        put("hasCheckout", co != null)
                        if (ci != null) {
                            put("checkin", JSONObject().apply {
                                put("id", ci.id)
                                put("intention", ci.content ?: "")
                                put("score", ci.score)
                                put("meta", if (!ci.metadataJson.isNullOrBlank()) JSONObject(ci.metadataJson) else JSONObject())
                            })
                        }
                        if (co != null) {
                            put("checkout", JSONObject().apply {
                                put("id", co.id)
                                put("reflection", co.content ?: "")
                                put("score", co.score)
                                put("meta", if (!co.metadataJson.isNullOrBlank()) JSONObject(co.metadataJson) else JSONObject())
                            })
                        }
                    }
                    successJson(res)
                }

                // Dedicated Sanctuary Ledger APIs (Instant, Clean, Reactive)
                "ledger:save", "ledger:add" -> {
                    val rawCat = params.optString("category", "quick").trim().lowercase()
                    val cat = if (rawCat.isBlank()) "quick" else rawCat
                    val content = (if (params.has("content")) params.optString("content", "")
                                   else if (params.has("note")) params.optString("note", "")
                                   else params.optString("text", "")).trim()
                    val title = params.optString("title", "").trim()
                    val score = params.optInt("score", 0)
                    val now = if (params.has("timestamp") && params.optLong("timestamp") > 0L) params.optLong("timestamp") else System.currentTimeMillis()
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = params.optString("date", sdf.format(Date(now)))
                    val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date(now))
                    val id = params.optString("id", "${cat}_" + UUID.randomUUID().toString().take(10))
                    val meta = if (params.has("metadata")) {
                        params.optJSONObject("metadata")?.toString() ?: "{}"
                    } else params.optString("metadataJson", "{}")

                    val event = com.v07.v3.core.data.V3Event(
                        id = id,
                        category = cat,
                        date = date,
                        ts = ts,
                        timestamp = now,
                        title = if (title.isNotBlank()) title else cat.replaceFirstChar { c -> c.uppercase() },
                        content = content,
                        score = score,
                        metadataJson = meta
                    )
                    val ok = repository.insertEvent(event)
                    if (cat == "thought" || cat == "quick") {
                        try {
                            repository.insertThought(
                                com.v07.v3.core.data.V3Thought(
                                    id = event.id,
                                    timestamp = now,
                                    date = date,
                                    topic = event.title ?: "Quick Entry",
                                    content = content
                                )
                            )
                        } catch (_: Exception) {}
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    val res = JSONObject().apply {
                        put("success", ok)
                        put("id", event.id)
                        put("category", event.category)
                        put("title", event.title)
                        put("content", event.content)
                        put("date", event.date)
                        put("timeFormatted", timeFmt.format(Date(now)))
                        put("timestamp", event.timestamp)
                        put("score", event.score)
                        put("metadataJson", event.metadataJson)
                    }
                    successJson(res)
                }
                "ledger:query", "ledger:list" -> {
                    val cat = params.optString("category", "").ifBlank { null }
                    val start = params.optString("startDate", "").ifBlank { null }
                    val end = params.optString("endDate", "").ifBlank { null }
                    val q = params.optString("query", "").ifBlank { null }
                    val limit = params.optInt("limit", 200)

                    val events = repository.queryLedgerEntries(cat, start, end, q, limit)
                    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val arr = JSONArray()
                    events.forEach { e ->
                        arr.put(JSONObject().apply {
                            put("id", e.id)
                            put("category", e.category)
                            put("displayCategory", when (e.category) {
                                "quick" -> "Quick"
                                "thought" -> "Quick"
                                "win" -> "Daily Win"
                                "lesson" -> "Daily Lesson"
                                "pushups" -> "Physical"
                                else -> e.category.replaceFirstChar { c -> c.uppercase() }
                            })
                            put("title", e.title ?: "")
                            put("content", e.content ?: "")
                            put("date", e.date)
                            put("timeFormatted", timeFmt.format(Date(e.timestamp)))
                            put("timestamp", e.timestamp)
                            put("score", e.score)
                            put("metadataJson", e.metadataJson ?: "")
                        })
                    }
                    successJson(JSONObject().put("entries", arr))
                }
                "ledger:delete" -> {
                    val id = params.optString("id", "")
                    val cat = params.optString("category", "quick")
                    val title = params.optString("title", "Ledger Entry")
                    val content = params.optString("content", "")
                    val timestamp = params.optLong("timestamp", System.currentTimeMillis())
                    val dataJson = JSONObject().apply {
                        put("id", id)
                        put("title", title)
                        put("content", content)
                        put("category", cat)
                        put("timestamp", timestamp)
                        put("deletedAt", System.currentTimeMillis())
                    }.toString()
                    val ok = repository.recycleItem(id, cat, dataJson)
                    if (ok && id.isNotBlank()) {
                        syncManager.deleteRemoteEvent(id)
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }
                "ledger:update" -> {
                    val id = params.optString("id", "")
                    if (id.isBlank()) {
                        return errorJson("Entry ID cannot be blank")
                    }
                    val title = if (params.has("title")) params.optString("title") else null
                    val content = if (params.has("content")) params.optString("content") else (if (params.has("note")) params.optString("note") else null)
                    val cat = if (params.has("category")) params.optString("category") else null
                    val meta = if (params.has("metadata")) {
                        params.optJSONObject("metadata")?.toString()
                    } else if (params.has("metadataJson")) {
                        params.optString("metadataJson")
                    } else null
                    val ok = repository.updateEvent(id, title, content, cat, meta)
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }

                // Quick Capture API (Wins, Lessons, Thoughts, Pushups, Custom Categories)
                "capture:quick" -> {
                    val rawCat = params.optString("category", "quick").trim().lowercase()
                    val customCat = params.optString("customCategory", "").trim().lowercase()
                    val cat = if (customCat.isNotBlank()) customCat else (if (rawCat.isNotBlank()) rawCat else "quick")
                    val noteStr = if (params.has("note")) params.optString("note", "") else (if (params.has("content")) params.optString("content", "") else params.optString("text", ""))
                    val content = noteStr.trim()
                    val defaultTitle = if (customCat.isNotBlank()) customCat.replaceFirstChar { c -> c.uppercase() } else cat.replaceFirstChar { c -> c.uppercase() }
                    val title = params.optString("title", "").ifBlank { defaultTitle }.trim()
                    val score = params.optInt("score", 0)
                    val count = if (params.has("count")) params.optInt("count", 0) else params.optInt("value", 0)
                    val now = System.currentTimeMillis()
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                    val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))

                    val meta = JSONObject().apply {
                        if (count > 0) put("count", count)
                        if (score > 0) put("score", score)
                        if (params.has("metadata")) {
                            val customMeta = params.optJSONObject("metadata")
                            customMeta?.keys()?.forEach { k ->
                                put(k, customMeta.get(k))
                            }
                        } else if (params.has("metadataJson")) {
                            try {
                                val customMeta = JSONObject(params.optString("metadataJson"))
                                customMeta.keys().forEach { k ->
                                    put(k, customMeta.get(k))
                                }
                            } catch (_: Exception) {}
                        }
                    }.toString()

                    val event = com.v07.v3.core.data.V3Event(
                        id = "${cat}_" + UUID.randomUUID().toString().take(8),
                        category = cat,
                        date = date,
                        ts = ts,
                        timestamp = now,
                        title = if (title.isNotBlank()) title else cat.replaceFirstChar { c -> c.uppercase() },
                        content = if (content.isNotBlank()) content else if (count > 0) "$count reps" else "",
                        score = score,
                        metadataJson = meta
                    )
                    val ok = repository.insertEvent(event)

                    if ((cat == "thought" || cat == "quick") && content.isNotBlank()) {
                        repository.insertThought(
                            com.v07.v3.core.data.V3Thought(
                                id = event.id,
                                timestamp = now,
                                date = date,
                                topic = title.ifBlank { "Quick Capture" },
                                content = content
                            )
                        )
                    } else if (cat == "session" || cat == "sessions") {
                        val durSec = if (params.has("durationSec")) params.optLong("durationSec", 0L) else (params.optLong("duration", 0L) * 60)
                        val session = com.v07.v3.core.data.V3Session(
                            id = event.id,
                            activity = if (title.isNotBlank()) title else "Focus Session",
                            startTime = now - (durSec * 1000),
                            endTime = now,
                            durationSec = durSec,
                            notes = content
                        )
                        repository.insertSession(session)
                    } else if (cat == "task" || cat == "tasks") {
                        val task = V3Task(
                            id = event.id,
                            title = if (title.isNotBlank()) title else (if (content.isNotBlank()) content else "New Task"),
                            priority = params.optString("priority", "MEDIUM").uppercase(),
                            status = "pending",
                            createdAt = now,
                            deadline = params.optString("deadline", "").ifBlank { null }
                        )
                        repository.insertTask(task)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", ok).put("id", event.id))
                }

                // Unified Data Explorer API (Single Place for All Entries)
                "entries:query" -> {
                    val cat = params.optString("category", "").ifBlank { null }
                    val start = params.optString("startDate", "").ifBlank { null }
                    val end = params.optString("endDate", "").ifBlank { null }
                    val q = params.optString("query", "").ifBlank { null }
                    val limit = params.optInt("limit", 200)

                    val list = repository.queryUnifiedEntries(
                        category = cat,
                        startDate = start,
                        endDate = end,
                        query = q,
                        limit = limit
                    )
                    val arr = JSONArray()
                    list.forEach { u ->
                        arr.put(JSONObject().apply {
                            put("id", u.id)
                            put("category", u.category)
                            put("displayCategory", u.displayCategory)
                            put("title", u.title)
                            put("subtitle", u.subtitle)
                            put("content", u.content)
                            put("date", u.date)
                            put("timeFormatted", u.timeFormatted)
                            put("timestamp", u.timestamp)
                            u.score?.let { put("score", it) }
                            u.durationSec?.let { put("durationSec", it) }
                            u.tags?.let { put("tags", it) }
                            put("rawJson", u.rawJson)
                        })
                    }
                    successJson(JSONObject().put("entries", arr))
                }
                "entries:delete" -> {
                    val id = params.optString("id", "")
                    val cat = params.optString("category", "").lowercase()
                    val ok = repository.deleteEntityEverywhere(id, cat)
                    if (ok && id.isNotBlank()) {
                        syncManager.deleteRemoteEvent(id)
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }
                "entries:recycle" -> {
                    val originalId = params.optString("originalId", params.optString("id", ""))
                    val category = params.optString("category", "general")
                    val dataJson = params.optString("dataJson", "{}")
                    val ok = repository.recycleItem(originalId, category, dataJson)
                    if (ok && originalId.isNotBlank()) {
                        syncManager.deleteRemoteEvent(originalId)
                    }
                    successJson(JSONObject().put("success", ok).put("originalId", originalId))
                }
                "entries:batchDelete" -> {
                    val idsArr = params.optJSONArray("ids")
                    val idList = mutableListOf<String>()
                    if (idsArr != null) {
                        for (i in 0 until idsArr.length()) {
                            val id = idsArr.optString(i, "")
                            if (id.isNotBlank()) idList.add(id)
                        }
                    }
                    val count = repository.batchDeleteEntities(idList)
                    CoroutineScope(Dispatchers.IO).launch {
                        idList.forEach { syncManager.deleteRemoteEvent(it) }
                    }
                    successJson(JSONObject().put("success", true).put("deletedCount", count))
                }
                "entries:batchRecycle" -> {
                    val itemsArr = params.optJSONArray("items")
                    val tripleList = mutableListOf<Triple<String, String, String>>()
                    if (itemsArr != null) {
                        for (i in 0 until itemsArr.length()) {
                            val itm = itemsArr.optJSONObject(i)
                            if (itm != null) {
                                val oid = itm.optString("originalId", itm.optString("id", ""))
                                val cat = itm.optString("category", "general")
                                val data = itm.optString("dataJson", "{}")
                                if (oid.isNotBlank()) {
                                    tripleList.add(Triple(oid, cat, data))
                                }
                            }
                        }
                    }
                    val count = repository.batchRecycleEntities(tripleList)
                    CoroutineScope(Dispatchers.IO).launch {
                        tripleList.forEach { syncManager.deleteRemoteEvent(it.first) }
                    }
                    successJson(JSONObject().put("success", true).put("recycledCount", count))
                }
                "entries:update" -> {
                    val id = params.optString("id", "")
                    if (id.isBlank()) {
                        return errorJson("Entry ID cannot be blank")
                    }
                    val title = if (params.has("title")) params.optString("title") else null
                    val content = if (params.has("content")) params.optString("content") else (if (params.has("note")) params.optString("note") else null)
                    val cat = if (params.has("category")) params.optString("category") else null
                    val meta = if (params.has("metadata")) {
                        params.optJSONObject("metadata")?.toString()
                    } else if (params.has("metadataJson")) {
                        params.optString("metadataJson")
                    } else null

                    val ok = repository.updateEvent(id, title, content, cat, meta)
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                    successJson(JSONObject().put("success", ok).put("id", id))
                }

                // Companion Sync API (Live Firestore Cloud Sync)
                "companion:getStatus" -> {
                    successJson(syncManager.getSyncStatus())
                }
                "companion:syncNow", "companion:sync" -> {
                    var syncOk = false
                    var syncErr: String? = null
                    val latch = java.util.concurrent.CountDownLatch(1)
                    syncManager.syncNow { ok, err ->
                        syncOk = ok
                        syncErr = err
                        latch.countDown()
                    }
                    latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
                    val res = JSONObject().apply {
                        put("success", syncOk)
                        if (syncErr != null) put("error", syncErr)
                        put("status", syncManager.getSyncStatus())
                    }
                    successJson(res)
                }
                "companion:signIn", "companion:login" -> {
                    val email = params.optString("email")
                    val pass = params.optString("password")
                    val createMode = params.optBoolean("createMode", false)
                    var signOk = false
                    var signErr: String? = null
                    val latch = java.util.concurrent.CountDownLatch(1)
                    syncManager.signIn(email, pass, createMode) { ok, err ->
                        signOk = ok
                        signErr = err
                        latch.countDown()
                    }
                    latch.await(10, java.util.concurrent.TimeUnit.SECONDS)
                    if (signOk) {
                        successJson(JSONObject().put("success", true).put("status", syncManager.getSyncStatus()))
                    } else {
                        errorJson(signErr ?: "Sign in failed")
                    }
                }
                "companion:signOut" -> {
                    syncManager.signOut()
                    successJson(JSONObject().put("success", true).put("status", syncManager.getSyncStatus()))
                }
                "companion:migrate", "companion:pullAll" -> {
                    val targetUid = params.optString("uid", "").ifBlank { null }
                    var migOk = false
                    var migErr: String? = null
                    var migStats: JSONObject? = null
                    val latch = java.util.concurrent.CountDownLatch(1)
                    syncManager.migrateWebCompanionData(targetUid) { ok, err, stats ->
                        migOk = ok
                        migErr = err
                        migStats = stats
                        latch.countDown()
                    }
                    latch.await(25, java.util.concurrent.TimeUnit.SECONDS)
                    val res = JSONObject().apply {
                        put("success", migOk)
                        if (migErr != null) put("error", migErr)
                        put("stats", migStats ?: JSONObject())
                        put("syncStatus", syncManager.getSyncStatus())
                    }
                    successJson(res)
                }
                "companion:setUid" -> {
                    val uid = params.optString("uid", "").trim()
                    if (uid.isNotBlank()) {
                        syncManager.setTargetUid(uid)
                        successJson(JSONObject().put("success", true).put("uid", uid))
                    } else {
                        errorJson("UID cannot be blank")
                    }
                }
                "companion:getTargetUid" -> {
                    successJson(JSONObject().put("success", true).put("uid", syncManager.getTargetUid()))
                }
                "companion:switchProfile", "profile:switch" -> {
                    val target = params.optString("profileId", params.optString("profile", "guest")).trim()
                    val ok = syncManager.switchProfile(target)
                    successJson(JSONObject().put("success", ok).put("active_profile", syncManager.getActiveProfileId()).put("profiles", JSONArray(syncManager.listProfiles())))
                }
                "companion:getProfiles", "profile:list" -> {
                    successJson(JSONObject().put("success", true).put("active_profile", syncManager.getActiveProfileId()).put("profiles", JSONArray(syncManager.listProfiles())))
                }
                "companion:getActiveProfile", "profile:getActive" -> {
                    successJson(JSONObject().put("success", true).put("active_profile", syncManager.getActiveProfileId()))
                }

                // Recycle Bin API (Soft Delete, Restore, Purge, Empty)
                "recycle:list" -> {
                    val limit = params.optInt("limit", 100)
                    val items = repository.getRecycleBinItems(limit)
                    val arr = JSONArray()
                    items.forEach { itm ->
                        arr.put(JSONObject().apply {
                            put("recycleId", itm.recycleId)
                            put("originalId", itm.originalId)
                            put("category", itm.category)
                            put("dataJson", itm.dataJson)
                            put("deletedAt", itm.deletedAt)
                        })
                    }
                    val res = JSONObject().apply {
                        put("items", arr)
                        put("count", items.size)
                    }
                    successJson(res)
                }
                "recycle:add", "recycle:recycleItem" -> {
                    val originalId = params.optString("originalId", "")
                    val category = params.optString("category", "general")
                    val dataJson = params.optString("dataJson", "{}")
                    val ok = repository.recycleItem(originalId, category, dataJson)
                    if (ok && originalId.isNotBlank()) {
                        syncManager.deleteRemoteEvent(originalId)
                    }
                    successJson(JSONObject().put("success", ok).put("originalId", originalId))
                }
                "recycle:restore" -> {
                    val recycleId = params.optString("recycleId", "")
                    val item = repository.restoreRecycleItem(recycleId)
                    if (item != null) {
                        val obj = JSONObject().apply {
                            put("success", true)
                            put("restored", true)
                            put("recycleId", item.recycleId)
                            put("originalId", item.originalId)
                            put("category", item.category)
                            put("dataJson", item.dataJson)
                            try {
                                put("data", JSONObject(item.dataJson))
                            } catch (_: Exception) {}
                        }
                        successJson(obj)
                    } else {
                        errorJson("Failed to restore item $recycleId")
                    }
                }
                "recycle:purge" -> {
                    val recycleId = params.optString("recycleId", "")
                    val purgedId = repository.purgeRecycleItem(recycleId)
                    if (purgedId != null) {
                        syncManager.deleteRemoteEvent(purgedId)
                    }
                    successJson(JSONObject().put("success", purgedId != null).put("purgedId", purgedId ?: ""))
                }
                "recycle:empty" -> {
                    val purgedIds = repository.emptyRecycleBin()
                    purgedIds.forEach { pid ->
                        syncManager.deleteRemoteEvent(pid)
                    }
                    successJson(JSONObject().put("success", true).put("purgedCount", purgedIds.size))
                }

                // AI Integration API
                "ai:status" -> {
                    com.v07.dashboard.GeminiConfig.init(context)
                    val configured = com.v07.dashboard.GeminiConfig.isConfigured
                    val res = JSONObject().apply {
                        put("configured", configured)
                        put("defaultModel", com.v07.dashboard.GeminiConfig.defaultModel)
                        put("provider", com.v07.dashboard.GeminiConfig.provider)
                        put("maskedKey", com.v07.dashboard.GeminiConfig.maskedKey())
                        put("models", JSONArray(com.v07.dashboard.GeminiConfig.AVAILABLE_MODELS))
                    }
                    successJson(res)
                }
                "ai:generate" -> {
                    com.v07.dashboard.GeminiConfig.init(context)
                    val prompt = params.optString("prompt").trim()
                    if (prompt.isBlank()) return errorJson("Missing required 'prompt'")
                    val sysInst = params.optString("systemInstruction").ifBlank { null }
                    val model = params.optString("model").ifBlank { null }
                    val temp = if (params.has("temperature")) params.optDouble("temperature").toFloat() else null
                    val maxTok = if (params.has("maxTokens")) params.optInt("maxTokens") else null
                    val format = params.optString("responseFormat", "text")
                    val schema = params.optJSONObject("jsonSchema")?.let { s ->
                        val map = mutableMapOf<String, Any?>()
                        s.keys().forEach { k -> map[k] = s.get(k) }
                        map
                    }

                    val result = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                        com.v07.dashboard.GeminiClient.generate(
                            com.v07.dashboard.GeminiClient.GenerateOptions(
                                prompt = prompt,
                                systemInstruction = sysInst,
                                model = model,
                                temperature = temp,
                                maxTokens = maxTok,
                                responseFormat = format,
                                responseSchema = schema
                            )
                        )
                    }
                    if (result.ok) {
                        val promptTok = (result.usage["promptTokens"] as? Number)?.toInt() ?: 0
                        val candTok = (result.usage["candidatesTokens"] as? Number)?.toInt() ?: 0
                        com.v07.dashboard.GeminiManager.recordUsage(promptTok, candTok)
                    }
                    val res = JSONObject().apply {
                        put("ok", result.ok)
                        put("text", result.text ?: "")
                        put("model", result.model)
                        put("latencyMs", result.latencyMs)
                        if (result.error != null) put("error", result.error)
                    }
                    if (result.ok) successJson(res) else errorJson(result.error ?: "AI generation failed")
                }
                "ai:chat" -> {
                    com.v07.dashboard.GeminiConfig.init(context)
                    val rawMessages = params.optJSONArray("messages") ?: JSONArray()
                    val messages = mutableListOf<Map<String, String>>()
                    for (i in 0 until rawMessages.length()) {
                        val m = rawMessages.optJSONObject(i) ?: continue
                        messages.add(mapOf(
                            "role" to m.optString("role", "user"),
                            "content" to m.optString("content", m.optString("text", ""))
                        ))
                    }
                    val sysInst = params.optString("systemInstruction").ifBlank { null }
                    val model = params.optString("model").ifBlank { null }
                    val temp = if (params.has("temperature")) params.optDouble("temperature").toFloat() else null

                    val result = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                        com.v07.dashboard.GeminiClient.chat(messages, sysInst, model, temp)
                    }
                    if (result.ok) {
                        val promptTok = (result.usage["promptTokens"] as? Number)?.toInt() ?: 0
                        val candTok = (result.usage["candidatesTokens"] as? Number)?.toInt() ?: 0
                        com.v07.dashboard.GeminiManager.recordUsage(promptTok, candTok)
                    }
                    val res = JSONObject().apply {
                        put("ok", result.ok)
                        put("text", result.text ?: "")
                        put("model", result.model)
                        put("latencyMs", result.latencyMs)
                        if (result.error != null) put("error", result.error)
                    }
                    if (result.ok) successJson(res) else errorJson(result.error ?: "AI chat failed")
                }

                // Gita Wisdom API (Access to 700+ Verses from assets/gita.json)
                "gita:getVerse", "gita:getDaily" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 0)
                    val vs = params.optInt("verse", 0)
                    val verseObj = if (ch > 0 && vs > 0) {
                        gitaProvider.getVerse(ch, vs)
                    } else {
                        gitaProvider.getDailyVerse()
                    }
                    if (verseObj != null) {
                        successJson(verseObj)
                    } else {
                        errorJson("Verse not found")
                    }
                }
                "gita:nextVerse" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 2)
                    val vs = params.optInt("verse", 47)
                    val verseObj = gitaProvider.getNextVerse(ch, vs)
                    if (verseObj != null) successJson(verseObj) else errorJson("Next verse not found")
                }
                "gita:prevVerse" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 2)
                    val vs = params.optInt("verse", 47)
                    val verseObj = gitaProvider.getPrevVerse(ch, vs)
                    if (verseObj != null) successJson(verseObj) else errorJson("Prev verse not found")
                }
                "gita:randomVerse" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val verseObj = gitaProvider.getRandomVerse()
                    if (verseObj != null) successJson(verseObj) else errorJson("Random verse not found")
                }
                "gita:markRead" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 0)
                    val vs = params.optInt("verse", 0)
                    val isRead = params.optBoolean("is_read", true)
                    if (ch > 0 && vs > 0) {
                        val progress = gitaProvider.markVerseRead(ch, vs, isRead)
                        successJson(JSONObject().put("success", true).put("progress", progress))
                    } else {
                        errorJson("Missing chapter or verse")
                    }
                }
                "gita:unmarkRead" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 0)
                    val vs = params.optInt("verse", 0)
                    val progress = gitaProvider.markVerseRead(ch, vs, false)
                    successJson(JSONObject().put("success", true).put("progress", progress))
                }
                "gita:getProgress" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    successJson(gitaProvider.getProgress())
                }
                "gita:getNextUnread" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 1)
                    val vs = params.optInt("verse", 1)
                    val unread = gitaProvider.getNextUnreadVerse(ch, vs)
                    if (unread != null) successJson(unread) else errorJson("All verses read!")
                }
                "gita:saveReflection", "gita:saveReview" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 0)
                    val vs = params.optInt("verse", 0)
                    val text = params.optString("text", "").trim()
                    val rating = params.optInt("rating", 3)
                    if (ch > 0 && vs > 0 && text.isNotBlank()) {
                        val entry = gitaProvider.saveReflection(ch, vs, text, rating)
                        // Also record in events ledger for universal timeline
                        try {
                            val now = System.currentTimeMillis()
                            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(now))
                            val tsStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US).format(java.util.Date(now))
                            repository.insertEvent(com.v07.v3.core.data.V3Event(
                                id = "gv_" + java.util.UUID.randomUUID().toString().take(8),
                                category = "gita",
                                date = dateStr,
                                ts = tsStr,
                                timestamp = now,
                                title = "Gita Reflection · BG $ch.$vs",
                                content = text,
                                score = rating,
                                metadataJson = entry.toString()
                            ))
                        } catch (_: Exception) {}
                        successJson(JSONObject().put("success", true).put("entry", entry))
                    } else {
                        errorJson("Missing chapter, verse, or reflection text")
                    }
                }
                "gita:getReflections", "gita:getReviews" -> {
                    val gitaProvider = GitaProvider.getInstance(context)
                    val ch = params.optInt("chapter", 0)
                    val vs = params.optInt("verse", 0)
                    val list = gitaProvider.getReflectionsForVerse(ch, vs)
                    successJson(JSONObject().put("reflections", list))
                }

                // ─── LEGACY DATA MIGRATION & REVIEW STUDIO API ────────────────
                "migration:getBundledData", "legacy:getBundledData" -> {
                    val importer = com.v07.v3.core.data.LegacyDataImporter(context)
                    val bundleJsonStr = importer.getBundledData()
                    try {
                        val bundleObj = JSONObject(bundleJsonStr)
                        successJson(JSONObject().put("success", true).put("bundle", bundleObj))
                    } catch (e: Exception) {
                        errorJson("Failed to parse bundled legacy data: ${e.message}")
                    }
                }
                "migration:commitRecords", "legacy:commitRecords" -> {
                    val importer = com.v07.v3.core.data.LegacyDataImporter(context)
                    val recordsToCommit = params.optJSONObject("records") ?: params
                    val res = importer.commitCuratedRecords(recordsToCommit)
                    successJson(res)
                }
                "migration:runAutoScan", "legacy:runAutoScan" -> {
                    val importer = com.v07.v3.core.data.LegacyDataImporter(context)
                    val summary = importer.importAllLegacyData()
                    val res = JSONObject().apply {
                        put("success", true)
                        put("sessionsCount", summary.sessionsCount)
                        put("tasksCount", summary.tasksCount)
                        put("thoughtsCount", summary.thoughtsCount)
                        put("dayTrackerCount", summary.dayTrackerCount)
                        put("recycleBinCount", summary.recycleBinCount)
                        put("habitsMigrated", summary.habitsMigratedToPlugin)
                        put("runningMigrated", summary.runningMigratedToPlugin)
                        put("vitalsMigrated", summary.vitalsMigratedToPlugin)
                        put("skippedJunkFiles", summary.skippedJunkFiles)
                        put("errorLines", summary.errorLines)
                    }
                    successJson(res)
                }
                "migration:autoSeed", "legacy:autoSeed" -> {
                    val importer = com.v07.v3.core.data.LegacyDataImporter(context)
                    val seeded = importer.autoSeedIfEmpty()
                    successJson(JSONObject().put("success", true).put("seeded", seeded))
                }
                "database:purge", "admin:purgeDatabase", "vault:purge" -> {
                    val keepSettings = if (params.has("keepPluginConfigs")) params.optBoolean("keepPluginConfigs", true) else params.optBoolean("keepSettings", true)
                    val ok = repository.purgeAllData(keepPluginConfigs = keepSettings)
                    if (ok) {
                        successJson(JSONObject().put("success", true).put("purged", true))
                    } else {
                        errorJson("Failed to purge database tables")
                    }
                }
                "database:stats", "db:stats" -> {
                    val dbMgr = com.v07.v3.core.data.DatabaseManager.getInstance(context)
                    val stats = dbMgr.getStats()
                    val tbls = JSONArray()
                    stats.tableCounts.forEach { (t, c) ->
                        tbls.put(JSONObject().put("table", t).put("count", c))
                    }
                    successJson(JSONObject().apply {
                        put("success", true)
                        put("db_path", stats.dbPath)
                        put("file_size_bytes", stats.fileSizeBytes)
                        put("file_size_formatted", "${stats.fileSizeBytes / 1024} KB")
                        put("is_wal_enabled", stats.isWalEnabled)
                        put("tables", tbls)
                    })
                }
                "database:vacuum", "db:vacuum" -> {
                    val dbMgr = com.v07.v3.core.data.DatabaseManager.getInstance(context)
                    val ok = dbMgr.vacuum()
                    val stats = dbMgr.getStats()
                    successJson(JSONObject().apply {
                        put("success", ok)
                        put("file_size_bytes", stats.fileSizeBytes)
                        put("file_size_formatted", "${stats.fileSizeBytes / 1024} KB")
                    })
                }
                "database:integrityCheck", "db:integrityCheck" -> {
                    val dbMgr = com.v07.v3.core.data.DatabaseManager.getInstance(context)
                    val checkRes = dbMgr.checkIntegrity()
                    successJson(JSONObject().apply {
                        put("success", checkRes.trim().equals("ok", ignoreCase = true))
                        put("result", checkRes)
                    })
                }
                "database:reindex", "db:reindex" -> {
                    val dbMgr = com.v07.v3.core.data.DatabaseManager.getInstance(context)
                    val ok = dbMgr.reindex()
                    successJson(JSONObject().put("success", ok))
                }
                "app:clearCache", "cache:clear" -> {
                    val dbMgr = com.v07.v3.core.data.DatabaseManager.getInstance(context)
                    val bytesFreed = dbMgr.clearAppCache()
                    successJson(JSONObject().apply {
                        put("success", true)
                        put("bytes_freed", bytesFreed)
                        put("formatted", "${bytesFreed / 1024} KB")
                    })
                }
                else -> errorJson("Unknown bridge method: $method")
            }
        } catch (e: Exception) {
            errorJson("Bridge execution failed: ${e.message}")
        }
    }

    private fun successJson(data: JSONObject): String {
        return JSONObject().apply {
            put("ok", true)
            put("status", "success")
            put("result", data)
            put("data", data)
        }.toString()
    }

    private fun errorJson(errorMessage: String): String {
        return JSONObject().apply {
            put("ok", false)
            put("status", "error")
            put("error", errorMessage)
            put("message", errorMessage)
        }.toString()
    }

    companion object {
        /**
         * Generates the client-side JavaScript injection script defining `window.V07Platform`.
         */
        fun generateInjectionScript(pluginId: String, baseUrl: String, token: String): String {
            return """
            (function() {
                if (window.V07Platform) return;
                
                const PLUGIN_ID = '$pluginId';
                const BASE_URL = '$baseUrl';
                const TOKEN = '$token';
                
                function callNative(method, params) {
                    return new Promise((resolve, reject) => {
                        try {
                            if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                                const respStr = window.V3NativeBridge.invoke(PLUGIN_ID, method, JSON.stringify(params || {}));
                                const resp = JSON.parse(respStr);
                                if (resp.ok) resolve(resp.result);
                                else reject(new Error(resp.error));
                            } else {
                                // Fallback to HTTP API
                                fetch(BASE_URL + '/api/v3/plugins/' + PLUGIN_ID + '/data', {
                                    headers: { 'X-V07-Token': TOKEN }
                                }).then(r => r.json()).then(resolve).catch(reject);
                            }
                        } catch (e) {
                            reject(e);
                        }
                    });
                }
                
                window.V07Platform = {
                    version: '3.0.0',
                    pluginId: PLUGIN_ID,
                    baseUrl: BASE_URL,
                    invoke: callNative,
                    callNative: callNative,
                    
                    storage: {
                        get: async function(key) {
                            const res = await callNative('storage:get', { key: key });
                            if (!res.found || res.value === null) return null;
                            try { return JSON.parse(res.value); } catch(_) { return res.value; }
                        },
                        set: async function(key, value) {
                            const valStr = typeof value === 'string' ? value : JSON.stringify(value);
                            return await callNative('storage:set', { key: key, value: valStr });
                        },
                        delete: async function(key) {
                            return await callNative('storage:delete', { key: key });
                        },
                        keys: async function() {
                            const res = await callNative('storage:keys', {});
                            return res.keys || [];
                        }
                    },
                    
                    telemetry: {
                        getScreenTimeToday: async function() {
                            return await callNative('telemetry:getScreenTime', {});
                        },
                        getDetailedScreenTime: async function() {
                            return await callNative('telemetry:getDetailedScreenTime', {});
                        },
                        hasUsagePermission: async function() {
                            const res = await callNative('telemetry:hasUsagePermission', {});
                            return res.hasPermission || res.granted || false;
                        },
                        requestPermission: async function() {
                            return await callNative('telemetry:requestPermission', {});
                        },
                        getActiveFocusSession: async function() {
                            return await callNative('timer:getState', {});
                        }
                    },

                    cadence: {
                        checkin: async function(data) {
                            return await callNative('cadence:checkin', data || {});
                        },
                        checkout: async function(data) {
                            return await callNative('cadence:checkout', data || {});
                        },
                        getStatus: async function() {
                            return await callNative('cadence:getStatus', {});
                        }
                    },

                    capture: {
                        quick: async function(data) {
                            return await callNative('capture:quick', data || {});
                        }
                    },

                    entries: {
                        query: async function(filters) {
                            const res = await callNative('entries:query', filters || {});
                            return res.entries || [];
                        },
                        delete: async function(id, category) {
                            return await callNative('entries:delete', { id: id, category: category });
                        },
                        recycle: async function(id, category, dataJson) {
                            return await callNative('entries:recycle', { originalId: id, category: category, dataJson: dataJson || '{}' });
                        },
                        update: async function(id, data) {
                            return await callNative('entries:update', Object.assign({ id: id }, data || {}));
                        },
                        batchDelete: async function(ids) {
                            return await callNative('entries:batchDelete', { ids: ids || [] });
                        },
                        batchRecycle: async function(items) {
                            return await callNative('entries:batchRecycle', { items: items || [] });
                        }
                    },
                    sessions: {
                        list: async function(date) {
                            const res = await callNative('sessions:list', { date: date || '' });
                            return res.sessions || [];
                        },
                        add: async function(activity, durationSec, notes) {
                            return await callNative('sessions:add', { activity: activity, durationSec: durationSec, notes: notes || '' });
                        },
                        delete: async function(id) {
                            return await callNative('sessions:delete', { id: id });
                        }
                    },

                    gita: {
                        getDaily: async function() {
                            return await callNative('gita:getDaily', {});
                        },
                        getVerse: async function(chapter, verse) {
                            return await callNative('gita:getVerse', { chapter: chapter, verse: verse });
                        },
                        nextVerse: async function(chapter, verse) {
                            return await callNative('gita:nextVerse', { chapter: chapter, verse: verse });
                        },
                        prevVerse: async function(chapter, verse) {
                            return await callNative('gita:prevVerse', { chapter: chapter, verse: verse });
                        },
                        randomVerse: async function() {
                            return await callNative('gita:randomVerse', {});
                        }
                    },

                    companion: {
                        getStatus: async function() {
                            return await callNative('companion:getStatus', {});
                        },
                        syncNow: async function() {
                            return await callNative('companion:syncNow', {});
                        },
                        signIn: async function(email, password, createMode) {
                            return await callNative('companion:signIn', { email: email, password: password, createMode: !!createMode });
                        },
                        signOut: async function() {
                            return await callNative('companion:signOut', {});
                        },
                        migrate: async function(uid) {
                            return await callNative('companion:migrate', { uid: uid || null });
                        },
                        switchProfile: async function(profileId) {
                            return await callNative('companion:switchProfile', { profileId: profileId });
                        },
                        getProfiles: async function() {
                            return await callNative('companion:getProfiles', {});
                        },
                        getActiveProfile: async function() {
                            return await callNative('companion:getActiveProfile', {});
                        }
                    },
                    
                    ai: {
                        isAvailable: async function() {
                            try {
                                const res = await callNative('ai:status', {});
                                return !!(res && res.configured);
                            } catch(e) { return false; }
                        },
                        getStatus: async function() {
                            return await callNative('ai:status', {});
                        },
                        generateText: async function(opts) {
                            if (typeof opts === 'string') opts = { prompt: opts };
                            return await callNative('ai:generate', opts || {});
                        },
                        generateJson: async function(opts) {
                            opts = opts || {};
                            opts.responseFormat = 'json';
                            return await callNative('ai:generate', opts);
                        },
                        chat: async function(opts) {
                            if (Array.isArray(opts)) opts = { messages: opts };
                            return await callNative('ai:chat', opts || {});
                        }
                    },

                    ui: {
                        toast: function(message) {
                            callNative('ui:toast', { message: message });
                        },
                        requestResize: function(height) {
                            // Notify parent iframe host if running inside dashboard widget
                            window.parent && window.parent.postMessage({ type: 'v3:widget:resize', pluginId: PLUGIN_ID, height: height }, '*');
                        }
                    }
                };
                
                // Dispatch event so plugins know platform is ready
                window.dispatchEvent(new CustomEvent('V07PlatformReady', { detail: window.V07Platform }));
            })();
            """.trimIndent()
        }

        /**
         * Generates a backwards-compatible plugin bridge combining window.V07Platform and window.V07.
         * Ensures all starter pack plugins work synchronously and persist to SQLite plugin storage.
         */
        fun generatePluginCompatibilityScript(pluginId: String, baseUrl: String, token: String): String {
            val v3Script = generateInjectionScript(pluginId, baseUrl, token)
            return """
            $v3Script
            (function() {
                function detectTheme() {
                    try {
                        var params = new URLSearchParams(window.location.search);
                        if (params.has('theme')) return params.get('theme');
                    } catch(e){}
                    try {
                        if (window.AndroidTheme && typeof window.AndroidTheme.isDark === 'function') {
                            return window.AndroidTheme.isDark() ? 'dark' : 'light';
                        }
                    } catch(e){}
                    try {
                        var saved = localStorage.getItem('v07_theme') || localStorage.getItem('v07_app_theme');
                        if (saved) return saved;
                    } catch(e){}
                    return 'dark';
                }
                function applyTheme(t) {
                    var isLight = (t === 'light');
                    var themeName = isLight ? 'light' : 'dark';
                    document.documentElement.setAttribute('data-theme', themeName);
                    if (isLight) {
                        document.documentElement.classList.add('light');
                        document.documentElement.classList.remove('dark');
                        if (document.body) {
                            document.body.classList.add('light');
                            document.body.classList.remove('dark');
                        }
                    } else {
                        document.documentElement.classList.add('dark');
                        document.documentElement.classList.remove('light');
                        if (document.body) {
                            document.body.classList.add('dark');
                            document.body.classList.remove('light');
                        }
                    }
                }
                applyTheme(detectTheme());
                window.addEventListener('DOMContentLoaded', function() { applyTheme(detectTheme()); });
                window.addEventListener('storage', function() { applyTheme(detectTheme()); });
            })();
            (function() {
                if (window.V07) return;
                var pid = '$pluginId';
                var baseUrl = '$baseUrl';
                var token = '$token';
                
                function getLocal(k) {
                    try {
                        var v = localStorage.getItem('v07_' + k + '_events') || localStorage.getItem('v07_plugin_' + k);
                        return v ? JSON.parse(v) : [];
                    } catch(e) { return []; }
                }
                function setLocal(k, evs) {
                    try { 
                        localStorage.setItem('v07_' + k + '_events', JSON.stringify(evs)); 
                    } catch(e) {}
                    try {
                        fetch(baseUrl + '/api/v3/plugins/' + pid + '/data', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json', 'X-V07-Token': token },
                            body: JSON.stringify({ key: 'events_' + k, value: JSON.stringify(evs) })
                        }).catch(function(){});
                    } catch(e) {}
                }

                window.V07 = {
                    pluginId: pid,
                    features: { notifications: true, ai: true },
                    readEventsAll: function(cat) {
                        return { ok: true, events: getLocal(cat) };
                    },
                    readEvents: function(cat, date) {
                        var all = getLocal(cat);
                        var filtered = date ? all.filter(function(e) { return e.date === date; }) : all;
                        return { ok: true, events: filtered };
                    },
                    writeEvent: function(cat, data) {
                        var all = getLocal(cat);
                        var now = new Date();
                        var ev = Object.assign({
                            id: 'ev_' + Date.now().toString(36) + '_' + Math.random().toString(36).substr(2, 4),
                            date: now.toISOString().slice(0, 10),
                            ts: now.toISOString()
                        }, data || {});
                        all.push(ev);
                        setLocal(cat, all);
                        return { ok: true, event: ev };
                    },
                    updateEvent: function(cat, id, fields) {
                        var all = getLocal(cat);
                        all.forEach(function(e) {
                            if (e.id === id) Object.assign(e, fields || {});
                        });
                        setLocal(cat, all);
                        return { ok: true };
                    },
                    deleteEvent: function(cat, id) {
                        var all = getLocal(cat).filter(function(e) { return e.id !== id; });
                        setLocal(cat, all);
                        return { ok: true };
                    },
                    toast: function(msg) {
                        try {
                            if (window.V3NativeBridge && window.V3NativeBridge.invoke) {
                                window.V3NativeBridge.invoke(pid, 'ui:toast', JSON.stringify({ message: msg }));
                            } else if (window.parent && window.parent.showToast) {
                                window.parent.showToast(msg, 'info');
                            } else {
                                console.log('[V07 Toast]', msg);
                            }
                        } catch(e) {}
                    },
                    getTheme: function() {
                        var t = 'dark';
                        try {
                            t = (window.parent && window.parent.document.documentElement.dataset.theme) || 
                                document.documentElement.dataset.theme || 'dark';
                        } catch(e) {}
                        return { theme: t };
                    },
                    api: function(method, path, data) {
                        return fetch(baseUrl + path, {
                            method: method || 'GET',
                            headers: { 'Content-Type': 'application/json', 'X-V07-Token': token },
                            body: data ? JSON.stringify(data) : undefined
                        }).then(function(r) { return r.json(); });
                    }
                };
            })();
            """.trimIndent()
        }
    }
}
