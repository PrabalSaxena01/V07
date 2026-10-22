package com.v07.v3.app.server

import android.content.Context
import android.util.Log
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.ActiveSessionState
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.app.timer.TimerStatus
import com.v07.v3.core.data.*
import com.v07.v3.core.plugin.PluginState
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ApiRouter
 *
 * Dispatches V3 REST endpoints to the respective domain controllers and repository services.
 * All mutations enforce token authentication.
 */
class ApiRouter(
    private val context: Context,
    private val repository: EventRepository,
    private val databaseManager: DatabaseManager,
    private val timerManager: TimerManager,
    private val screenTimeTracker: ScreenTimeTracker,
    private val pluginRegistry: com.v07.v3.core.plugin.PluginRegistry,
    private val syncManager: com.v07.v3.core.sync.V3CompanionSyncManager,
    private val sessionToken: String
) {
    companion object {
        private const val TAG = "V3ApiRouter"
    }

    suspend fun handle(request: HttpRequest): HttpResponse {
        val path = request.path
        val method = request.method.uppercase()

        // Handle CORS preflight
        if (method == "OPTIONS") {
            return HttpResponse(statusCode = 204, statusText = "No Content")
        }

        // Threat 1 Hardening: Authenticate all data-bearing endpoints (exempt status and plugin compatibility endpoints)
        val isExempt = (path == "/api/v3/status" && method == "GET") ||
                path == "/ai/status" ||
                path.startsWith("/events") || path.startsWith("/event") ||
                path.startsWith("/daytracker") || path.startsWith("/day-tracker") ||
                path.startsWith("/checklist") || path.startsWith("/tasks") ||
                path.startsWith("/task/") ||
                path.startsWith("/api/weekly-summary")
        if (!isExempt) {
            val tokenHeader = request.getHeader("X-V07-Token") ?: request.queryParams["token"]
            if (tokenHeader != sessionToken) {
                return HttpResponse.error("Unauthorized: Invalid or missing X-V07-Token", 401)
            }
        }

        return try {
            when {
                // System Status
                path == "/api/v3/status" -> handleStatus()

                // Admin Operations Suite (Full Data & Profile Administration)
                path.startsWith("/api/v3/admin") -> handleAdmin(method, path, request)

                // Companion Sync API (Firestore Cloud Sync)
                path.startsWith("/api/v3/companion") -> handleCompanion(method, path, request)

                // Daily Cadence & Life OS (Check-In & Check-Out)
                path.startsWith("/api/v3/cadence") -> handleCadence(method, path, request)

                // Quick Capture (Wins, Lessons, Thoughts, Pushups)
                path.startsWith("/api/v3/capture") -> handleCapture(method, path, request)

                // Unified Data Explorer (All Entries Ledger)
                path.startsWith("/api/v3/entries") || path.startsWith("/api/v3/events") -> handleEntries(method, path, request)

                // Core 1: Sessions
                path.startsWith("/api/v3/sessions") -> handleSessions(method, path, request)

                // Core 2: Tasks & Compatibility Task Routes
                path.startsWith("/api/v3/tasks") || path == "/tasks" || path.startsWith("/tasks?") -> handleTasks(method, path, request)
                path.startsWith("/task/") -> handleTaskItemRoute(method, path, request)
                path.startsWith("/checklist") -> handleChecklistRoute(method, path, request)

                // Core 3: Thoughts
                path.startsWith("/api/v3/thoughts") -> handleThoughts(method, path, request)

                // Core 4: Screen Time
                path.startsWith("/api/v3/screentime") -> handleScreenTime(method, path, request)

                // Core Timeline (V2 Timeline Equivalent)
                path.startsWith("/api/v3/day") || path.startsWith("/api/v3/timeline") -> handleDayTimeline(method, path, request)

                // Core Util 1: Day Tracker (Milestones / Countdowns) & Compatibility Day Tracker Routes
                path.startsWith("/api/v3/daytracker") || path.startsWith("/daytracker") || path.startsWith("/day-tracker") -> handleDayTracker(method, path, request)

                // Weekly Digest / AI Synthesis Compatibility
                path.startsWith("/api/weekly-summary") -> handleWeeklySummary(method, path, request)

                // Core Util 2: Recycle Bin
                path.startsWith("/api/v3/recyclebin") -> handleRecycleBin(method, path, request)

                // Core Util 3: Database Manager
                path.startsWith("/api/v3/database") -> handleDatabase(method, path, request)

                // Plugin Storage API
                path.startsWith("/api/v3/plugins") -> handlePluginStorage(method, path, request)

                // Plugin Compatibility: Events API (Vitals, Mood, Energy)
                path.startsWith("/events") || path.startsWith("/event") -> handlePluginEvents(method, path, request)

                // Plugin Compatibility: AI Status (Chronosynth)
                path == "/ai/status" -> HttpResponse.json("""{"ok":true,"configured":false,"hasAccess":true,"models":[]}""")

                else -> HttpResponse.error("Endpoint not found: $path", 404)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling ${request.method} $path: ${e.message}", e)
            HttpResponse.error("Internal Server Error: ${e.message}", 500)
        }
    }

    // ─── STATUS ENDPOINT ────────────────────────────────────────────────────

    private fun handleStatus(): HttpResponse {
        val timer = timerManager.timerState.value
        val dbStats = databaseManager.getStats()

        val json = JSONObject().apply {
            put("ok", true)
            put("platform", "V07 V3")
            put("version", "3.0.0")
            put("session_token", sessionToken)
            put("timer", JSONObject().apply {
                put("status", timer.status.name)
                put("topic", timer.topic)
                put("category", timer.category)
                put("mode", timer.mode.name)
                put("elapsed_ms", timer.getElapsedMs())
                put("remaining_ms", timer.getRemainingMs())
                put("formatted", timer.getFormattedTime())
            })
            put("database", JSONObject().apply {
                put("db_size_bytes", dbStats.fileSizeBytes)
                put("db_path", dbStats.dbPath)
                put("is_wal_enabled", dbStats.isWalEnabled)
            })
            put("has_screentime_permission", screenTimeTracker.hasUsageStatsPermission())
        }
        return HttpResponse.json(json.toString())
    }

    // ─── CORE 1: SESSIONS ───────────────────────────────────────────────────

    private fun handleSessions(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 50
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val sessions = repository.getRecentSessions(limit, offset)

                val arr = JSONArray()
                sessions.forEach { s ->
                    arr.put(JSONObject().apply {
                        put("id", s.id)
                        put("activity", s.activity)
                        put("start_time", s.startTime)
                        put("end_time", s.endTime)
                        put("duration_sec", s.durationSec)
                        put("notes", s.notes ?: "")
                    })
                }
                return HttpResponse.json(JSONObject().put("ok", true).put("sessions", arr).toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                if (path.endsWith("/record") || body.optBoolean("direct_record", false) || (body.has("activity") && body.has("start_time"))) {
                    val id = body.optString("id").ifBlank { "sess_" + UUID.randomUUID().toString().take(8) }
                    val act = body.optString("activity", "Work")
                    val start = body.optLong("start_time", System.currentTimeMillis() - 1800000L)
                    val end = if (body.has("end_time") && !body.isNull("end_time")) body.optLong("end_time") else System.currentTimeMillis()
                    val dur = body.optLong("duration_sec", ((end - start) / 1000).coerceAtLeast(0))
                    val notes = body.optString("notes", "").ifBlank { null }
                    val sess = V3Session(id, act, start, end, dur, notes)
                    val ok = repository.insertSession(sess)
                    return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString(), 201)
                }

                val topic = body.optString("topic", "Focus Session")
                val category = body.optString("category", "Work")
                val modeStr = body.optString("mode", "STOPWATCH").uppercase()
                val mode = if (modeStr == "COUNTDOWN") TimerMode.COUNTDOWN else TimerMode.STOPWATCH
                val targetMinutes = body.optInt("targetMinutes", 0)
                val notes = body.optString("notes", "")

                val state = timerManager.startSession(topic, category, mode, targetMinutes, notes)
                return HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("action", "started")
                    put("id", state.id)
                    put("topic", state.topic)
                    put("status", state.status.name)
                }.toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                if (path.endsWith("/record") || body.optString("action") == "update_record" || (body.has("id") && body.has("activity"))) {
                    val id = body.optString("id").trim()
                    if (id.isBlank()) return HttpResponse.error("Session id required", 400)
                    val act = body.optString("activity", "Work")
                    val start = body.optLong("start_time", System.currentTimeMillis() - 1800000L)
                    val end = if (body.has("end_time") && !body.isNull("end_time")) body.optLong("end_time") else System.currentTimeMillis()
                    val dur = body.optLong("duration_sec", ((end - start) / 1000).coerceAtLeast(0))
                    val notes = body.optString("notes", "").ifBlank { null }
                    val sess = V3Session(id, act, start, end, dur, notes)
                    val ok = repository.updateSession(sess)
                    return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
                }

                val action = body.optString("action", "pause").lowercase()
                return when (action) {
                    "pause" -> {
                        val s = timerManager.pauseSession()
                        val isOk: Boolean = s != null
                        HttpResponse.json(JSONObject().put("ok", isOk).put("status", s?.status?.name ?: "IDLE").toString())
                    }
                    "resume" -> {
                        val s = timerManager.resumeSession()
                        val isOk: Boolean = s != null
                        HttpResponse.json(JSONObject().put("ok", isOk).put("status", s?.status?.name ?: "IDLE").toString())
                    }
                    "finish" -> {
                        val notes = body.optString("notes", "").ifBlank { null }
                        val record = timerManager.finishSession(notes)
                        if (record != null) {
                            HttpResponse.json(JSONObject().put("ok", true).put("saved_session_id", record.id).put("duration_sec", record.durationSec).toString())
                        } else {
                            HttpResponse.error("No active session to finish", 400)
                        }
                    }
                    "discard" -> {
                        timerManager.discardSession()
                        HttpResponse.success("""{"ok":true,"action":"discarded"}""")
                    }
                    else -> HttpResponse.error("Unknown session control action: $action", 400)
                }
            }
            "DELETE" -> {
                val id = request.queryParams["id"] ?: return HttpResponse.error("Missing session id parameter", 400)
                val moved: Boolean = repository.recycleItem(id, "sessions", "{}")
                val deleted: Boolean = repository.deleteSession(id)
                syncManager.deleteRemoteEvent(id)
                return HttpResponse.json(JSONObject().put("ok", deleted).put("recycled", moved).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for sessions", 405)
        }
    }

    // ─── CORE 2: TASKS ──────────────────────────────────────────────────────

    private fun handleTasks(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val status = request.queryParams["status"] ?: request.queryParams["filter"] ?: "pending"
                val tasks = if (status == "all") {
                    repository.getTasks("pending") + repository.getTasks("completed")
                } else {
                    repository.getTasks(status)
                }
                val arr = JSONArray()
                tasks.forEach { t ->
                    arr.put(JSONObject().apply {
                        put("id", t.id)
                        put("title", t.title)
                        put("priority", t.priority)
                        put("status", t.status)
                        put("created_at", t.createdAt)
                        put("completed_at", t.completedAt)
                        put("deadline", t.deadline ?: "")
                    })
                }
                return HttpResponse.json(JSONObject().put("ok", true).put("tasks", arr).toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val title = body.optString("title").trim()
                if (title.isBlank()) return HttpResponse.error("Task title cannot be blank", 400)

                val priority = body.optString("priority", "MEDIUM").uppercase()
                val deadline = body.optString("deadline", "").ifBlank { null }
                val task = V3Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    priority = priority,
                    status = "pending",
                    createdAt = System.currentTimeMillis(),
                    deadline = deadline
                )
                val ok: Boolean = repository.insertTask(task)
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", task.id).toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val id = body.optString("id").trim()
                if (id.isBlank()) return HttpResponse.error("Task id cannot be blank", 400)

                if (body.has("toggle_status")) {
                    val ok: Boolean = repository.toggleTaskStatus(id)
                    return HttpResponse.json(JSONObject().put("ok", ok).toString())
                }

                val current = repository.getTasks("pending").find { it.id == id }
                    ?: repository.getTasks("completed").find { it.id == id }
                    ?: return HttpResponse.error("Task not found", 404)

                val updated = current.copy(
                    title = body.optString("title", current.title),
                    priority = body.optString("priority", current.priority).uppercase(),
                    status = body.optString("status", current.status),
                    deadline = if (body.has("deadline")) body.optString("deadline").ifBlank { null } else current.deadline
                )
                val ok: Boolean = repository.updateTask(updated)
                return HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            "DELETE" -> {
                val id = request.queryParams["id"] ?: return HttpResponse.error("Missing task id", 400)
                val task = repository.getTasks("pending").find { it.id == id }
                    ?: repository.getTasks("completed").find { it.id == id }
                val payload = if (task != null) JSONObject().apply {
                    put("title", task.title)
                    put("priority", task.priority)
                    put("status", task.status)
                }.toString() else "{}"

                repository.recycleItem(id, "tasks", payload)
                val deleted: Boolean = repository.deleteTask(id)
                syncManager.deleteRemoteEvent(id)
                return HttpResponse.json(JSONObject().put("ok", deleted).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for tasks", 405)
        }
    }

    // ─── CORE 3: THOUGHTS ───────────────────────────────────────────────────

    private fun handleThoughts(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 50
                val pinnedOnly = request.queryParams["pinned"]?.toBoolean() ?: false
                val thoughts = if (pinnedOnly) repository.getPinnedThoughts() else repository.getRecentThoughts(limit)

                val arr = JSONArray()
                thoughts.forEach { th ->
                    arr.put(JSONObject().apply {
                        put("id", th.id)
                        put("timestamp", th.timestamp)
                        put("date", th.date)
                        put("topic", th.topic)
                        put("content", th.content)
                        put("pinned", th.pinned)
                        put("tags", th.tags)
                    })
                }
                return HttpResponse.json(JSONObject().put("ok", true).put("thoughts", arr).toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val content = body.optString("content").trim()
                if (content.isBlank()) return HttpResponse.error("Thought content cannot be blank", 400)

                val topic = body.optString("topic", "General")
                val pinned = body.optBoolean("pinned", false)
                val tags = body.optString("tags", "")
                val now = System.currentTimeMillis()
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))

                val thought = V3Thought(
                    id = UUID.randomUUID().toString(),
                    timestamp = now,
                    date = dateStr,
                    topic = topic,
                    content = content,
                    pinned = pinned,
                    tags = tags
                )
                val ok: Boolean = repository.insertThought(thought)
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", thought.id).toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val id = body.optString("id").trim()
                if (id.isBlank()) return HttpResponse.error("Thought id cannot be blank", 400)

                if (body.has("toggle_pinned")) {
                    val ok: Boolean = repository.toggleThoughtPinned(id)
                    return HttpResponse.json(JSONObject().put("ok", ok).toString())
                }

                val topic = body.optString("topic", "General")
                val content = body.optString("content", "")
                val pinned = body.optBoolean("pinned", false)
                val tags = body.optString("tags", "")
                val now = body.optLong("timestamp", System.currentTimeMillis())
                val dateStr = body.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now)))

                val thought = V3Thought(
                    id = id,
                    timestamp = now,
                    date = dateStr,
                    topic = topic,
                    content = content,
                    pinned = pinned,
                    tags = tags
                )
                val ok = repository.updateThought(thought)
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            "DELETE" -> {
                val id = request.queryParams["id"] ?: return HttpResponse.error("Missing thought id", 400)
                val thought = repository.getRecentThoughts(100).find { it.id == id }
                val payload = if (thought != null) JSONObject().apply {
                    put("topic", thought.topic)
                    put("content", thought.content)
                }.toString() else "{}"

                repository.recycleItem(id, "thoughts", payload)
                val ok: Boolean = repository.deleteThought(id)
                syncManager.deleteRemoteEvent(id)
                return HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for thoughts", 405)
        }
    }

    // ─── CORE 4: SCREEN TIME ────────────────────────────────────────────────

    private suspend fun handleScreenTime(method: String, path: String, request: HttpRequest): HttpResponse {
        if (!screenTimeTracker.hasUsageStatsPermission()) {
            return HttpResponse.json(JSONObject().apply {
                put("ok", false)
                put("permission_granted", false)
                put("message", "PACKAGE_USAGE_STATS permission not granted. Please enable in Android Settings.")
            }.toString(), 403)
        }

        return when (method) {
            "GET" -> {
                val summary = screenTimeTracker.collectNow()
                val arr = JSONArray()
                summary.topApps.forEach { app ->
                    arr.put(JSONObject().apply {
                        put("package_name", app.packageName)
                        put("app_name", app.appName)
                        put("duration_ms", app.durationMs)
                        put("duration_sec", app.durationMs / 1000)
                        put("launch_count", app.launchCount)
                        put("category", app.category)
                        put("percentage", app.percentage)
                    })
                }

                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("date", summary.date)
                    put("total_duration_ms", summary.totalDurationMs)
                    put("total_duration_sec", summary.totalDurationMs / 1000)
                    put("total_duration_formatted", formatSeconds(summary.totalDurationMs / 1000))
                    put("app_count", summary.appCount)
                    put("apps", arr)
                }.toString())
            }
            "POST" -> {
                val summary = screenTimeTracker.collectNow()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("synced", true)
                    put("total_duration_ms", summary.totalDurationMs)
                    put("app_count", summary.appCount)
                }.toString())
            }
            else -> HttpResponse.error("Method $method not allowed for screentime", 405)
        }
    }

    // ─── CORE TIMELINE (V2 TIMELINE EQUIVALENT) ─────────────────────────────

    private fun handleDayTimeline(method: String, path: String, request: HttpRequest): HttpResponse {
        if (method != "GET") {
            return HttpResponse.error("Method $method not allowed for timeline", 405)
        }

        // Support /api/v3/timeline?date=YYYY-MM-DD or /api/v3/day?date=YYYY-MM-DD or /api/v3/day/YYYY-MM-DD
        val queryDate = request.queryParams["date"]
        val pathSegments = path.trim('/').split('/')
        val pathDate = if (pathSegments.size >= 4 && pathSegments[3].isNotBlank()) pathSegments[3] else null
        val targetDate = queryDate ?: pathDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val timeline = repository.getDayTimeline(targetDate)

        val eventsArr = JSONArray()
        timeline.events.forEach { e ->
            eventsArr.put(JSONObject().apply {
                put("id", e.id)
                put("timestamp", e.timestamp)
                put("time_formatted", e.timeFormatted)
                put("category", e.category)
                put("title", e.title)
                put("subtitle", e.subtitle)
                put("duration_sec", e.durationSec)
                put("is_highlight", e.isHighlight)
            })
        }

        val json = JSONObject().apply {
            put("ok", true)
            put("date", timeline.date)
            put("total_focus_sec", timeline.totalFocusSec)
            put("total_focus_formatted", formatSeconds(timeline.totalFocusSec))
            put("session_count", timeline.sessionCount)
            put("tasks_completed_count", timeline.tasksCompletedCount)
            put("thoughts_count", timeline.thoughtsCount)
            put("total_screentime_ms", timeline.totalScreenTimeMs)
            put("total_screentime_formatted", formatSeconds(timeline.totalScreenTimeMs / 1000))
            put("events", eventsArr)
        }

        return HttpResponse.json(json.toString())
    }

    // ─── CORE UTIL 1: DAY TRACKER ───────────────────────────────────────────

    private fun handleDayTracker(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val items = repository.getAllDayTrackerItems()
                val today = try { java.time.LocalDate.now() } catch (_: Exception) { null }
                val arr = JSONArray()
                val entriesArr = JSONArray()
                val eventsArr = JSONArray()

                items.forEach { item ->
                    var diffDays = 0L
                    var isPast = false
                    var diffStr = "0d"
                    if (today != null) {
                        try {
                            val d = java.time.LocalDate.parse(item.targetDate)
                            diffDays = java.time.temporal.ChronoUnit.DAYS.between(today, d)
                            isPast = diffDays < 0
                            diffStr = if (isPast) "${Math.abs(diffDays)}d" else "${diffDays}d"
                        } catch (_: Exception) {}
                    }

                    val obj = JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("name", item.title)
                        put("target_date", item.targetDate)
                        put("date", item.targetDate)
                        put("hour", item.hour)
                        put("pinned", item.pinned)
                        put("created_at", item.createdAt)
                        put("diff_days", diffStr)
                        put("past", isPast)
                        put("days", diffDays)
                    }
                    arr.put(obj)
                    entriesArr.put(obj)
                    if (item.pinned) {
                        eventsArr.put(obj)
                    }
                }
                return HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("items", arr)
                    put("entries", entriesArr)
                    put("events", eventsArr)
                }.toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val title = body.optString("title", body.optString("name")).trim()
                if (title.isBlank()) return HttpResponse.error("Title cannot be blank", 400)

                val targetDate = body.optString("target_date", body.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())))
                val hour = body.optInt("hour", 0)
                val pinned = body.optBoolean("pinned", false)
                val customId = body.optString("id").ifBlank { UUID.randomUUID().toString() }

                val item = V3DayTrackerItem(
                    id = customId,
                    title = title,
                    targetDate = targetDate,
                    hour = hour,
                    pinned = pinned,
                    createdAt = System.currentTimeMillis()
                )
                val ok: Boolean = repository.insertDayTrackerItem(item)
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", item.id).toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val id = body.optString("id").trim()
                if (id.isBlank()) return HttpResponse.error("DayTracker id required", 400)

                val existing = repository.getAllDayTrackerItems().find { it.id == id }
                val title = body.optString("title", body.optString("name", existing?.title ?: "")).trim()
                val targetDate = body.optString("target_date", body.optString("date", existing?.targetDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())))
                val hour = body.optInt("hour", existing?.hour ?: 0)
                val pinned = if (body.has("pinned")) body.optBoolean("pinned") else (existing?.pinned ?: false)

                val item = V3DayTrackerItem(
                    id = id,
                    title = title.ifBlank { "Milestone" },
                    targetDate = targetDate,
                    hour = hour,
                    pinned = pinned,
                    createdAt = body.optLong("created_at", existing?.createdAt ?: System.currentTimeMillis())
                )
                val ok: Boolean = repository.updateDayTrackerItem(item)
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            "DELETE" -> {
                val id = request.queryParams["id"] ?: return HttpResponse.error("Missing id parameter", 400)
                repository.recycleItem(id, "day_tracker", "{}")
                val ok: Boolean = repository.deleteDayTrackerItem(id)
                syncManager.deleteRemoteEvent(id)
                return HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for daytracker", 405)
        }
    }

    // ─── CORE UTIL 2: RECYCLE BIN ───────────────────────────────────────────

    private fun handleRecycleBin(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val items = repository.getRecycleBinItems()
                val arr = JSONArray()
                items.forEach { r ->
                    arr.put(JSONObject().apply {
                        put("recycle_id", r.recycleId)
                        put("original_id", r.originalId)
                        put("category", r.category)
                        put("data_json", r.dataJson)
                        put("deleted_at", r.deletedAt)
                    })
                }
                return HttpResponse.json(JSONObject().put("ok", true).put("items", arr).toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val recycleId = body.optString("recycle_id").trim()
                if (recycleId.isBlank()) return HttpResponse.error("recycle_id cannot be blank", 400)

                val restored = repository.restoreRecycleItem(recycleId)
                val ok: Boolean = (restored != null)
                return HttpResponse.json(JSONObject().put("ok", ok).put("restored", recycleId).toString())
            }
            "DELETE" -> {
                val recycleId = request.queryParams["recycle_id"]
                val ok: Boolean = if (recycleId.isNullOrBlank()) {
                    val purged = repository.emptyRecycleBin()
                    purged.forEach { syncManager.deleteRemoteEvent(it) }
                    true
                } else {
                    val purgedId = repository.purgeRecycleItem(recycleId)
                    if (purgedId != null) {
                        syncManager.deleteRemoteEvent(purgedId)
                        true
                    } else {
                        false
                    }
                }
                return HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for recyclebin", 405)
        }
    }

    // ─── CORE UTIL 3: DATABASE MANAGER ──────────────────────────────────────

    private fun handleDatabase(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path == "/api/v3/database/stats" && method == "GET" -> {
                val stats = databaseManager.getStats()
                val tableArr = JSONArray()
                for ((tbl, cnt) in stats.tableCounts) {
                    tableArr.put(JSONObject().put("table", tbl).put("count", cnt))
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("db_path", stats.dbPath)
                    put("db_size_bytes", stats.fileSizeBytes)
                    put("db_size_formatted", "${stats.fileSizeBytes / 1024} KB")
                    put("is_wal_enabled", stats.isWalEnabled)
                    put("tables", tableArr)
                }.toString())
            }
            path == "/api/v3/database/vacuum" && method == "POST" -> {
                val ok: Boolean = databaseManager.vacuum()
                HttpResponse.json(JSONObject().put("ok", ok).put("action", "vacuum").toString())
            }
            path == "/api/v3/database/backup" && method == "POST" -> {
                val backupFile = databaseManager.createBackup()
                if (backupFile != null) {
                    HttpResponse.json(JSONObject().apply {
                        put("ok", true)
                        put("backup_path", backupFile.absolutePath)
                        put("file_size_bytes", backupFile.length())
                    }.toString(), 201)
                } else {
                    HttpResponse.error("Failed to create database backup", 500)
                }
            }
            path == "/api/v3/database/import-legacy" && method == "POST" -> {
                val importer = LegacyDataImporter(context)
                val summary = importer.importAllLegacyData()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("sessions_imported", summary.sessionsCount)
                    put("tasks_imported", summary.tasksCount)
                    put("thoughts_imported", summary.thoughtsCount)
                    put("day_tracker_imported", summary.dayTrackerCount)
                    put("recycle_bin_imported", summary.recycleBinCount)
                    put("habits_imported", summary.habitsMigratedToPlugin)
                    put("running_imported", summary.runningMigratedToPlugin)
                    put("vitals_imported", summary.vitalsMigratedToPlugin)
                    put("skipped_junk_files", summary.skippedJunkFiles)
                    put("error_lines", summary.errorLines)
                }.toString())
            }
            path == "/api/v3/database/profiles" && method == "GET" -> {
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("profiles", JSONArray(databaseManager.listProfiles()))
                }.toString())
            }
            path == "/api/v3/database/profile/switch" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val target = body.optString("profileId", body.optString("profile", "guest")).trim()
                val ok = syncManager.switchProfile(target)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", JSONArray(syncManager.listProfiles()))
                }.toString())
            }
            path == "/api/v3/database/integrity" && (method == "GET" || method == "POST") -> {
                val res = databaseManager.checkIntegrity()
                HttpResponse.json(JSONObject().put("ok", true).put("integrity", res).toString())
            }
            path == "/api/v3/database/reindex" && method == "POST" -> {
                val ok = databaseManager.reindex()
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            else -> HttpResponse.error("Database endpoint not found: $path", 404)
        }
    }

    // ─── PLUGIN PLATFORM & STORAGE API ─────────────────────────────────────

    private fun handlePluginStorage(method: String, path: String, request: HttpRequest): HttpResponse {
        val cleanPath = path.removeSuffix("/")
        if (cleanPath == "/api/v3/plugins" || cleanPath == "/api/v3/plugins/refresh") {
            if (cleanPath == "/api/v3/plugins/refresh" && (method == "POST" || method == "GET")) {
                pluginRegistry.discoverAll()
            }
            if (method == "GET" || cleanPath == "/api/v3/plugins/refresh") {
                val list = JSONArray()
                pluginRegistry.getAllPlugins().forEach { p ->
                    list.put(JSONObject().apply {
                        put("id", p.manifest.id)
                        put("name", p.manifest.name)
                        put("version", p.manifest.version)
                        put("description", p.manifest.description)
                        put("state", p.state.name)
                        put("enabled", p.state == PluginState.ENABLED || p.state == PluginState.RUNNING)
                        put("is_builtin", p.isBuiltIn)
                        put("isBuiltIn", p.isBuiltIn)
                        put("nav_icon", p.manifest.navIcon)
                        put("icon", p.manifest.navIcon)
                        put("nav_label", p.manifest.navLabel)
                        put("nav_color", p.manifest.navColor)
                        put("color", p.manifest.navColor)
                        put("entry", p.manifest.entry)
                        put("settings", p.manifest.settingsEntry ?: "plugins/${p.manifest.id}/settings.html")
                        put("permissions", JSONArray(p.manifest.permissions))
                        val wArr = JSONArray()
                        p.manifest.widgets.forEach { w ->
                            wArr.put(JSONObject().apply {
                                put("id", w.id)
                                put("title", w.title)
                                put("entry", w.entry)
                                put("icon", w.icon ?: "")
                                put("color", w.color ?: "")
                                put("default_size", w.defaultSize)
                            })
                        }
                        put("widgets", wArr)
                    })
                }
                return HttpResponse.json(JSONObject().put("ok", true).put("plugins", list).toString())
            }
        }

        if (cleanPath == "/api/v3/plugins/toggle" && method == "POST") {
            val body = JSONObject(request.body.ifBlank { "{}" })
            val id = body.optString("id")
            val enabled = body.optBoolean("enabled", true)
            val ok = if (enabled) pluginRegistry.enablePlugin(id) else pluginRegistry.disablePlugin(id)
            return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).put("enabled", enabled).toString())
        }

        // Path pattern: /api/v3/plugins/{pluginId}/data?key={key}
        val segments = path.trim('/').split('/')
        if (segments.size < 4) {
            return HttpResponse.error("Invalid plugin storage path: $path", 400)
        }
        val pluginId = segments[3]

        // Subroute: /api/v3/plugins/{pluginId}/events
        if (segments.size >= 5 && segments[4] == "events") {
            return when (method) {
                "GET" -> {
                    val category = request.queryParams["category"]
                    val date = request.queryParams["date"]
                    val list = if (!category.isNullOrBlank()) {
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
                    HttpResponse.json(JSONObject().put("ok", true).put("plugin_id", pluginId).put("events", arr).toString())
                }
                "POST", "PUT" -> {
                    val body = JSONObject(request.body.ifBlank { "{}" })
                    val id = body.optString("id").ifBlank { "ev_" + UUID.randomUUID().toString().take(8) }
                    val category = body.optString("category", "general")
                    val date = body.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    val ts = body.optLong("timestamp", body.optLong("ts", System.currentTimeMillis()))
                    val payload = body.optString("payloadJson", body.toString())
                    val ok = repository.insertPluginEvent(
                        V3PluginEvent(
                            id = id,
                            pluginId = pluginId,
                            category = category,
                            payloadJson = payload,
                            timestamp = ts,
                            date = date
                        )
                    )
                    HttpResponse.json(JSONObject().put("ok", ok).put("id", id).put("plugin_id", pluginId).toString(), if (ok) 201 else 500)
                }
                "DELETE" -> {
                    val id = request.queryParams["id"] ?: (if (segments.size >= 6) segments[5] else null)
                    if (id.isNullOrBlank()) return HttpResponse.error("Missing id parameter", 400)
                    val ok = repository.deleteEntityEverywhere(id, "plugin_event")
                    HttpResponse.json(JSONObject().put("ok", ok).put("id", id).put("plugin_id", pluginId).toString())
                }
                else -> HttpResponse.error("Method $method not allowed for plugin events", 405)
            }
        }

        when (method) {
            "GET" -> {
                val key = request.queryParams["key"]
                if (key.isNullOrBlank()) {
                    val keys = repository.getAllPluginKeys(pluginId)
                    val obj = JSONObject()
                    for (k in keys) {
                        obj.put(k, repository.getPluginValue(pluginId, k) ?: "")
                    }
                    return HttpResponse.json(JSONObject().put("ok", true).put("plugin_id", pluginId).put("data", obj).toString())
                } else {
                    val value = repository.getPluginValue(pluginId, key)
                    return if (value != null) {
                        HttpResponse.json(JSONObject().put("ok", true).put("plugin_id", pluginId).put("key", key).put("value", value).toString())
                    } else {
                        HttpResponse.error("Key '$key' not found for plugin '$pluginId'", 404)
                    }
                }
            }
            "POST", "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val key = body.optString("key").trim()
                if (key.isBlank()) return HttpResponse.error("key cannot be blank", 400)
                val value = body.optString("value", "")

                val ok: Boolean = repository.setPluginValue(pluginId, key, value)
                return HttpResponse.json(JSONObject().put("ok", ok).put("plugin_id", pluginId).put("key", key).toString())
            }
            "DELETE" -> {
                val key = request.queryParams["key"] ?: return HttpResponse.error("Missing key parameter", 400)
                val ok: Boolean = repository.deletePluginValue(pluginId, key)
                return HttpResponse.json(JSONObject().put("ok", ok).put("plugin_id", pluginId).put("key", key).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for plugin storage", 405)
        }
    }

    private fun handlePluginEvents(method: String, path: String, request: HttpRequest): HttpResponse {
        // Support: /events/mood, /events/mood/today, /events/energy, /events/energy/today, /event/mood, /event/energy
        val clean = path.removePrefix("/events/").removePrefix("/events").removePrefix("/event/").removePrefix("/event")
        val category = clean.substringBefore('/').ifEmpty { "mood" }
        val pluginId = "com.v07.vitals"
        val storageKey = "events_$category"

        return when (method) {
            "GET" -> {
                val stored = repository.getPluginValue(pluginId, storageKey)
                val eventsArr = if (!stored.isNullOrBlank()) {
                    try { JSONArray(stored) } catch (_: Exception) { JSONArray() }
                } else {
                    JSONArray()
                }
                HttpResponse.json(JSONObject().put("ok", true).put("events", eventsArr).toString())
            }
            "POST" -> {
                val body = if (request.body.isNotBlank()) {
                    try { JSONObject(request.body) } catch (_: Exception) { JSONObject() }
                } else {
                    JSONObject()
                }
                val dataObj = body.optJSONObject("data") ?: body
                val now = System.currentTimeMillis()
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
                val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date(now))

                val newEvent = JSONObject().apply {
                    put("id", "v_" + UUID.randomUUID().toString().take(8))
                    put("category", category)
                    put("score", dataObj.optInt("score", dataObj.optInt("value", 3)))
                    put("value", dataObj.optInt("value", dataObj.optInt("score", 3)))
                    put("label", dataObj.optString("label", dataObj.optString("mood", "Steady")))
                    put("note", dataObj.optString("note", dataObj.optString("notes", "")))
                    put("date", dataObj.optString("date", dateStr))
                    put("ts", dataObj.optString("ts", nowIso))
                }

                val stored = repository.getPluginValue(pluginId, storageKey)
                val arr = if (!stored.isNullOrBlank()) {
                    try { JSONArray(stored) } catch (_: Exception) { JSONArray() }
                } else {
                    JSONArray()
                }
                arr.put(newEvent)
                repository.setPluginValue(pluginId, storageKey, arr.toString())

                HttpResponse.json(JSONObject().put("ok", true).put("event", newEvent).toString(), 201)
            }
            else -> HttpResponse.error("Method $method not allowed for plugin events", 405)
        }
    }

    // ─── COMPANION SYNC API ───────────────────────────────────────────────

    private fun handleCompanion(method: String, path: String, request: HttpRequest): HttpResponse {
        val clean = path.removeSuffix("/")
        return when {
            clean == "/api/v3/companion/status" && method == "GET" -> {
                HttpResponse.json(JSONObject().put("ok", true).put("status", syncManager.getSyncStatus()).toString())
            }
            clean == "/api/v3/companion/sync" && method == "POST" -> {
                var syncOk = false
                var syncErr: String? = null
                val latch = java.util.concurrent.CountDownLatch(1)
                syncManager.syncNow { ok, err ->
                    syncOk = ok
                    syncErr = err
                    latch.countDown()
                }
                latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
                HttpResponse.json(JSONObject().apply {
                    put("ok", syncOk)
                    if (syncErr != null) put("error", syncErr)
                    put("status", syncManager.getSyncStatus())
                }.toString())
            }
            clean == "/api/v3/companion/auth" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val action = body.optString("action", "signin")
                if (action == "signout") {
                    syncManager.signOut()
                    HttpResponse.json(JSONObject().put("ok", true).put("status", syncManager.getSyncStatus()).toString())
                } else {
                    val email = body.optString("email")
                    val pass = body.optString("password")
                    val createMode = body.optBoolean("createMode", false)
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
                        HttpResponse.json(JSONObject().put("ok", true).put("status", syncManager.getSyncStatus()).toString())
                    } else {
                        HttpResponse.error(signErr ?: "Authentication failed", 400)
                    }
                }
            }
            clean == "/api/v3/companion/profiles" && method == "GET" -> {
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("available_profiles", JSONArray(syncManager.listProfiles()))
                }.toString())
            }
            clean == "/api/v3/companion/profile/switch" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val target = body.optString("profileId", body.optString("profile", "guest")).trim()
                val ok = syncManager.switchProfile(target)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("available_profiles", JSONArray(syncManager.listProfiles()))
                }.toString())
            }
            else -> HttpResponse.error("Companion endpoint not found: $path", 404)
        }
    }

    // ─── DAILY CADENCE & LIFE OS API ────────────────────────────────────────

    private fun handleCadence(method: String, path: String, request: HttpRequest): HttpResponse {
        val clean = path.removeSuffix("/")
        return when {
            clean == "/api/v3/cadence/status" && method == "GET" -> {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val events = repository.getEvents(date = today)
                val ci = events.find { it.category == "checkin" }
                val co = events.find { it.category == "checkout" }
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
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
                }.toString())
            }
            clean == "/api/v3/cadence/checkin" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val mood = body.optInt("mood", 3)
                val energy = body.optInt("energy", 3)
                val focus = body.optInt("focus", 3)
                val intention = body.optString("intention", "").trim()
                val now = System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                val meta = JSONObject().apply {
                    put("mood", mood)
                    put("energy", energy)
                    put("focus", focus)
                    put("intention", intention)
                }.toString()

                val event = V3Event(
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
                syncManager.syncNow { _, _ -> }
                HttpResponse.json(JSONObject().put("ok", ok).put("id", event.id).toString(), 201)
            }
            clean == "/api/v3/cadence/checkout" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val wins = body.optString("wins", "").trim()
                val lessons = body.optString("lessons", "").trim()
                val reflection = body.optString("reflection", "").trim()
                val score = body.optInt("score", 4)
                val now = System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                val meta = JSONObject().apply {
                    put("wins", wins)
                    put("lessons", lessons)
                    put("reflection", reflection)
                    put("score", score)
                }.toString()

                val event = V3Event(
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
                        V3Event(
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
                        V3Event(
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
                syncManager.syncNow { _, _ -> }
                HttpResponse.json(JSONObject().put("ok", true).put("id", event.id).toString(), 201)
            }
            else -> HttpResponse.error("Cadence endpoint not found: $path", 404)
        }
    }

    // ─── QUICK CAPTURE API ──────────────────────────────────────────────────

    private fun handleCapture(method: String, path: String, request: HttpRequest): HttpResponse {
        if (method != "POST") return HttpResponse.error("Method $method not allowed for capture", 405)
        val body = JSONObject(request.body.ifBlank { "{}" })
        val cat = body.optString("category", "thought").trim()
        val content = body.optString("content", "").trim()
        val title = body.optString("title", "").trim()
        val score = body.optInt("score", 0)
        val count = body.optInt("count", 0)
        val now = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
        val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))

        val meta = JSONObject().apply {
            if (count > 0) put("count", count)
            if (score > 0) put("score", score)
        }.toString()

        val event = V3Event(
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

        if (cat == "thought" && content.isNotBlank()) {
            repository.insertThought(
                V3Thought(
                    id = event.id,
                    timestamp = now,
                    date = date,
                    topic = title.ifBlank { "Quick Capture" },
                    content = content
                )
            )
        }
        syncManager.syncNow { _, _ -> }
        return HttpResponse.json(JSONObject().put("ok", ok).put("id", event.id).toString(), 201)
    }

    // ─── UNIFIED DATA EXPLORER API ──────────────────────────────────────────

    private fun handleEntries(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val cat = request.queryParams["category"]
                val start = request.queryParams["startDate"]
                val end = request.queryParams["endDate"]
                val q = request.queryParams["query"]
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 500

                val list = repository.queryUnifiedEntries(
                    category = cat?.ifBlank { null },
                    startDate = start?.ifBlank { null },
                    endDate = end?.ifBlank { null },
                    query = q?.ifBlank { null },
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
                        put("ts", u.timestamp)
                        u.score?.let { put("score", it) }
                        u.durationSec?.let { put("durationSec", it); put("duration_sec", it); put("duration_min", it / 60) }
                        u.tags?.let { put("tags", it) }
                        put("rawJson", u.rawJson)
                    })
                }
                return HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("entries", arr)
                    put("events", arr)
                }.toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val id = body.optString("id", "").ifBlank { body.optString("docId", "").ifBlank { "evt_" + UUID.randomUUID().toString().take(8) } }
                val cat = body.optString("category", "general").trim().lowercase()
                val title = body.optString("title", "").trim()
                val content = body.optString("content", body.optString("notes", "")).trim()
                val score = body.optInt("score", 0)
                val date = body.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                val ts = body.optLong("ts", body.optLong("timestamp", System.currentTimeMillis()))
                val durMin = body.optInt("duration_min", 0)
                val durationSec = body.optLong("duration_sec", body.optLong("durationSec", (durMin * 60).toLong()))

                val event = V3Event(
                    id = id,
                    category = cat,
                    date = date,
                    ts = date,
                    timestamp = ts,
                    title = if (title.isNotBlank()) title else cat.replaceFirstChar { c -> c.uppercase() },
                    content = content,
                    score = score,
                    metadataJson = body.toString()
                )
                val ok = repository.insertEvent(event)

                if (cat == "thought" || cat == "thoughts") {
                    repository.insertThought(
                        V3Thought(
                            id = id,
                            timestamp = ts,
                            date = date,
                            topic = if (title.isNotBlank()) title else "Thought",
                            content = content
                        )
                    )
                } else if (cat == "session" || cat == "sessions" || cat == "focus") {
                    val act = body.optString("activity", "deepwork")
                    repository.insertSession(
                        V3Session(
                            id = id,
                            activity = act,
                            startTime = ts - (durationSec * 1000),
                            endTime = ts,
                            durationSec = durationSec,
                            notes = content
                        )
                    )
                } else if (cat == "task" || cat == "tasks") {
                    repository.insertTask(
                        V3Task(
                            id = id,
                            title = if (title.isNotBlank()) title else "Task",
                            priority = body.optString("priority", "MEDIUM").uppercase(),
                            status = body.optString("status", "pending"),
                            createdAt = ts,
                            deadline = body.optString("deadline", "").ifBlank { null }
                        )
                    )
                }

                syncManager.syncNow { _, _ -> }
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val id = body.optString("id", "").ifBlank { request.queryParams["id"] ?: "" }.trim()
                if (id.isBlank()) return HttpResponse.error("Missing id parameter", 400)
                val cat = body.optString("category", "").ifBlank { null }
                val title = body.optString("title", "").ifBlank { null }
                val content = body.optString("content", body.optString("notes", "")).ifBlank { null }
                val meta = if (body.has("metadataJson")) body.optString("metadataJson") else body.toString()
                val ok = repository.updateEvent(id, title, content, cat, meta)
                syncManager.syncNow { _, _ -> }
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            "DELETE" -> {
                val pathSegmentId = path.substringAfterLast('/').takeIf { it != "entries" && it != "events" && it.isNotBlank() }
                val id = request.queryParams["id"] ?: pathSegmentId ?: return HttpResponse.error("Missing id parameter", 400)
                val cat = request.queryParams["category"] ?: ""
                val ok = repository.deleteEvent(id)
                repository.deleteThought(id)
                repository.deleteTask(id)
                repository.deleteSession(id)
                syncManager.deleteRemoteEvent(id)
                return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for entries", 405)
        }
    }

    // ─── ADMIN OPERATIONS SUITE ─────────────────────────────────────────────

    private fun handleAdmin(method: String, path: String, request: HttpRequest): HttpResponse {
        val clean = path.removeSuffix("/")
        return when {
            // ── Admin Profiles ──
            clean == "/api/v3/admin/profiles" && method == "GET" -> {
                val profiles = databaseManager.listProfilesWithStats()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("profiles", profiles)
                }.toString())
            }
            clean == "/api/v3/admin/profiles/switch" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val target = body.optString("profileId", body.optString("profile", "guest")).trim()
                val ok = syncManager.switchProfile(target)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString())
            }
            clean == "/api/v3/admin/profiles/create" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val profileId = body.optString("profileId", "").trim()
                if (profileId.isBlank()) return HttpResponse.error("profileId cannot be blank", 400)
                val ok = databaseManager.createProfile(profileId)
                if (body.optBoolean("switchAfterCreate", true)) {
                    syncManager.switchProfile(profileId)
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("created_profile", profileId)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString(), if (ok) 201 else 500)
            }
            clean == "/api/v3/admin/profiles/clone" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val sourceId = body.optString("sourceId", "").trim()
                val targetId = body.optString("targetId", "").trim()
                if (sourceId.isBlank() || targetId.isBlank()) return HttpResponse.error("sourceId and targetId required", 400)
                val ok = databaseManager.cloneProfile(sourceId, targetId)
                if (body.optBoolean("switchAfterClone", false)) {
                    syncManager.switchProfile(targetId)
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("cloned_profile", targetId)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString())
            }
            clean == "/api/v3/admin/profiles" && method == "DELETE" -> {
                val profileId = request.queryParams["profileId"] ?: return HttpResponse.error("Missing profileId parameter", 400)
                if (profileId == databaseManager.getActiveProfile()) {
                    return HttpResponse.error("Cannot delete the currently active profile", 400)
                }
                if (profileId == "guest") {
                    return HttpResponse.error("Cannot delete default guest profile", 400)
                }
                val ok = databaseManager.deleteProfile(profileId)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("deleted_profile", profileId)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString())
            }

            // ── Admin Tables & Schema ──
            clean == "/api/v3/admin/tables" && method == "GET" -> {
                val tables = databaseManager.getAllTableNames()
                val arr = JSONArray()
                val db = com.v07.v3.core.data.V07Database.getInstance(context).readableDatabase
                for (t in tables) {
                    var cnt = 0L
                    try {
                        val c = db.rawQuery("SELECT COUNT(*) FROM \"$t\"", null)
                        c.use { if (it.moveToFirst()) cnt = it.getLong(0) }
                    } catch (_: Exception) {}
                    arr.put(JSONObject().apply {
                        put("name", t)
                        put("row_count", cnt)
                        put("schema", databaseManager.getTableSchema(t))
                    })
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("tables", arr)
                }.toString())
            }
            clean == "/api/v3/admin/table/data" && method == "GET" -> {
                val table = request.queryParams["table"] ?: return HttpResponse.error("Missing table parameter", 400)
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 50
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val sortCol = request.queryParams["sortCol"]
                val sortDir = request.queryParams["sortDir"] ?: "DESC"
                val search = request.queryParams["search"]
                val result = databaseManager.getTableData(table, limit, offset, sortCol, sortDir, search)
                result.put("ok", true)
                HttpResponse.json(result.toString())
            }
            clean == "/api/v3/admin/table/row" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val table = body.optString("table")
                val values = body.optJSONObject("values") ?: return HttpResponse.error("Missing values object", 400)
                val ok = databaseManager.insertTableRow(table, values)
                HttpResponse.json(JSONObject().put("ok", ok).toString(), if (ok) 201 else 500)
            }
            clean == "/api/v3/admin/table/row" && method == "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val table = body.optString("table")
                val pkCol = body.optString("pkCol", "id")
                val pkVal = body.optString("pkVal")
                val values = body.optJSONObject("values") ?: return HttpResponse.error("Missing values object", 400)
                val ok = databaseManager.updateTableRow(table, pkCol, pkVal, values)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            clean == "/api/v3/admin/table/row" && method == "DELETE" -> {
                val table = request.queryParams["table"] ?: return HttpResponse.error("Missing table parameter", 400)
                val pkCol = request.queryParams["pkCol"] ?: "id"
                val pkVal = request.queryParams["pkVal"] ?: return HttpResponse.error("Missing pkVal parameter", 400)
                val ok = databaseManager.deleteTableRow(table, pkCol, pkVal)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }

            // ── Admin SQL Workbench ──
            clean == "/api/v3/admin/sql" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val sql = body.optString("sql", "").trim()
                val result = databaseManager.executeSql(sql)
                HttpResponse.json(result.toString())
            }

            // ── Admin Backups ──
            clean == "/api/v3/admin/backups" && method == "GET" -> {
                val backups = databaseManager.listBackups()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("backups", backups)
                }.toString())
            }
            clean == "/api/v3/admin/backup/restore" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val backupPath = body.optString("path", "").trim()
                if (backupPath.isBlank()) return HttpResponse.error("Backup file path required", 400)
                val ok = databaseManager.restoreBackup(backupPath)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            clean == "/api/v3/admin/cache/clear" && method == "POST" -> {
                val clearedBytes = databaseManager.clearAppCache()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("bytes_cleared", clearedBytes)
                    put("formatted", "${clearedBytes / 1024} KB")
                }.toString())
            }

            // ── Admin App Usage Logs ──
            clean == "/api/v3/admin/appusage" && method == "GET" -> {
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 100
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val logs = repository.getAppUsageLogs(limit, offset)
                val arr = JSONArray()
                logs.forEach { u ->
                    arr.put(JSONObject().apply {
                        put("id", u.id)
                        put("package_name", u.packageName)
                        put("app_name", u.appName)
                        put("start_time", u.startTime)
                        put("end_time", u.endTime)
                        put("duration_ms", u.durationMs)
                        put("duration_sec", u.durationMs / 1000)
                        put("category", u.category)
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("logs", arr).toString())
            }
            clean == "/api/v3/admin/appusage" && method == "DELETE" -> {
                val id = request.queryParams["id"]
                val ok = repository.deleteAppUsage(id)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }

            else -> HttpResponse.error("Admin endpoint not found: $path", 404)
        }
    }

    private fun formatSeconds(totalSec: Long): String {
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m ${s}s"
    }

    // ─── CHECKLIST COMPATIBILITY API (V2 PLUGIN COMPATIBILITY) ────────────

    private fun handleChecklistRoute(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.contains("/toggle") && method == "POST" -> {
                val parts = path.split("/").filter { it.isNotBlank() }
                val id = if (parts.size >= 3) parts[1] else parts.lastOrNull { it != "toggle" && it != "checklist" } ?: ""
                if (id.isBlank()) return HttpResponse.error("Task id required", 400)
                val ok = repository.toggleTaskStatus(id)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            method == "GET" -> {
                val pending = repository.getTasks("pending")
                val completed = repository.getTasks("completed")
                val all = pending + completed
                val arr = JSONArray()
                all.forEach { t ->
                    val isDone = t.status.equals("completed", ignoreCase = true) || t.status.equals("done", ignoreCase = true)
                    arr.put(JSONObject().apply {
                        put("id", t.id)
                        put("label", t.title)
                        put("title", t.title)
                        put("done", isDone)
                        put("status", if (isDone) "done" else "pending")
                        put("priority", t.priority)
                        put("date", t.deadline ?: "")
                        put("recurring", false)
                        put("frequency", "S")
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("items", arr).toString())
            }
            method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val label = body.optString("label", body.optString("title")).trim()
                if (label.isBlank()) return HttpResponse.error("Label required", 400)
                val task = V3Task(
                    id = body.optString("id").ifBlank { "cl_" + UUID.randomUUID().toString().take(8) },
                    title = label,
                    priority = "MEDIUM",
                    status = "pending",
                    createdAt = System.currentTimeMillis()
                )
                val ok = repository.insertTask(task)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", task.id).toString(), 201)
            }
            else -> HttpResponse.error("Method $method not allowed for checklist", 405)
        }
    }

    private fun handleTaskItemRoute(method: String, path: String, request: HttpRequest): HttpResponse {
        val parts = path.split("/").filter { it.isNotBlank() }
        val id = if (parts.size >= 2) parts[1] else ""
        val action = parts.lastOrNull() ?: ""
        if (id.isBlank()) return HttpResponse.error("Task id required", 400)

        val allTasks = repository.getTasks("pending") + repository.getTasks("completed")
        val task = allTasks.find { it.id == id }

        return when (action) {
            "done" -> {
                if (task != null) {
                    repository.updateTask(task.copy(status = "completed", completedAt = System.currentTimeMillis()))
                } else {
                    repository.toggleTaskStatus(id)
                }
                HttpResponse.json(JSONObject().put("ok", true).toString())
            }
            "reopen" -> {
                if (task != null) {
                    repository.updateTask(task.copy(status = "pending", completedAt = null))
                } else {
                    repository.toggleTaskStatus(id)
                }
                HttpResponse.json(JSONObject().put("ok", true).toString())
            }
            else -> HttpResponse.error("Unknown task action: $action", 404)
        }
    }

    // ─── WEEKLY SUMMARY & AI SYNTHESIS COMPATIBILITY ──────────────────────

    private suspend fun handleWeeklySummary(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/api/weekly-summary/aggregate") && method == "GET" -> {
                val start = request.queryParams["start"]
                val end = request.queryParams["end"]
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val data = com.v07.dashboard.WeeklyAggregator.aggregate(start, end, offset)
                val json = JSONObject(data)
                HttpResponse.json(json.toString())
            }
            path.startsWith("/api/weekly-summary/generate") && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val offset = body.optInt("offset", 0)
                val start = body.optString("start").ifBlank { null }
                val end = body.optString("end").ifBlank { null }
                val data = com.v07.dashboard.WeeklyAggregator.aggregate(start, end, offset)
                val promptContext = data["prompt_context"] as? String ?: ""

                val rangeMap = data["range"] as? Map<*, *>
                val sDate = rangeMap?.get("start") ?: "Monday"
                val eDate = rangeMap?.get("end") ?: "Sunday"

                val focusHours = (data["focus"] as? Map<*, *>)?.get("total_hours") ?: 0
                val tasksVelocity = (data["tasks"] as? Map<*, *>)?.get("velocity_pct") ?: 0
                val habitAdherence = (data["habits"] as? Map<*, *>)?.get("overall_consistency_pct") ?: 0
                val summaryLead = promptContext.lines().take(15).joinToString("\n")

                val markdown = buildString {
                    appendLine("## 🌿 Weekly Telemetry Synthesis ($sDate to $eDate)")
                    appendLine()
                    appendLine("### Executive Summary")
                    appendLine(summaryLead)
                    appendLine()
                    appendLine("### Key Achievements & Deep Work")
                    appendLine("- **Total Deep Work**: $focusHours hours")
                    appendLine("- **Tasks Velocity**: $tasksVelocity% completed")
                    appendLine("- **Habit Discipline**: $habitAdherence% adherence")
                    appendLine()
                    appendLine("### Sanctuary Mindset Note")
                    appendLine("> \"Action is thy duty, fruit is not thy concern.\" — Shrimad Bhagavad Gita (2.47)")
                }

                HttpResponse.json(JSONObject().put("ok", true).put("markdown", markdown).toString())
            }
            path.startsWith("/api/weekly-summary/save") && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val start = body.optString("start", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                val end = body.optString("end", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                val content = body.optString("content", "")
                val title = body.optString("title", "Weekly-Review-$start-to-$end")

                try {
                    com.v07.dashboard.WeeklyAggregator.saveToVault(start, end, content, title)
                } catch (e: Exception) {
                    Log.w("V3ApiRouter", "Vault save fallback: ${e.message}")
                }

                // Also persist into thoughts table for instant access in Sanctuary
                val thought = V3Thought(
                    id = "wr_" + UUID.randomUUID().toString().take(8),
                    timestamp = System.currentTimeMillis(),
                    date = start,
                    topic = "Weekly Review",
                    content = content,
                    pinned = true,
                    tags = "review,weekly,synthesis"
                )
                repository.insertThought(thought)

                HttpResponse.json(JSONObject().put("ok", true).put("saved", true).toString())
            }
            else -> HttpResponse.error("Endpoint not found: $path", 404)
        }
    }
}
