package com.v07.v3.app.server.routes

import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.core.data.*
import com.v07.v3.core.sync.V3CompanionSyncManager
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CadenceSubRouter(
    private val repository: EventRepository,
    private val syncManager: V3CompanionSyncManager
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/v3/cadence") ||
                path.startsWith("/api/v3/capture") ||
                path.startsWith("/api/v3/entries") ||
                path.startsWith("/api/v3/events") ||
                path.startsWith("/api/v3/day") ||
                path.startsWith("/api/v3/timeline") ||
                path.startsWith("/api/v3/daytracker") ||
                path.startsWith("/daytracker") ||
                path.startsWith("/day-tracker") ||
                path.startsWith("/day/") ||
                path == "/day" ||
                path == "/timeline" ||
                path.startsWith("/timeline") ||
                path.startsWith("/events") ||
                path.startsWith("/event/") ||
                path == "/database" ||
                path.startsWith("/database?") ||
                path.startsWith("/meta/") ||
                path == "/client/log" ||
                path.startsWith("/edit")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/api/v3/cadence") -> handleCadence(method, path, request)
            path.startsWith("/api/v3/capture") -> handleCapture(method, request)
            path.startsWith("/api/v3/entries") || path.startsWith("/api/v3/events") || path.startsWith("/events") || path.startsWith("/event/") ->
                handleEntries(method, path, request)
            path.startsWith("/api/v3/day") || path.startsWith("/day/") || path == "/day" || path.startsWith("/api/v3/timeline") || path.startsWith("/timeline") ->
                handleDayTimeline(method, path, request)
            path.startsWith("/api/v3/daytracker") || path.startsWith("/daytracker") || path.startsWith("/day-tracker") ->
                handleDayTracker(method, request)
            path.startsWith("/database") -> handleDatabaseDump(request)
            path.startsWith("/meta/") -> handleMeta(method, path)
            path == "/client/log" || path.startsWith("/edit") -> HttpResponse.json(JSONObject().put("ok", true).toString())
            else -> HttpResponse.error("Endpoint not found: $path", 404)
        }
    }

    private fun handleDatabaseDump(request: HttpRequest): HttpResponse {
        val days = request.queryParams["days"]?.toIntOrNull() ?: 365
        val list = repository.getEvents(limit = 1000)
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("category", e.category)
                put("date", e.date)
                put("ts", e.ts)
                put("title", e.title)
                put("content", e.content)
                put("score", e.score)
                put("data", if (!e.metadataJson.isNullOrBlank()) try { JSONObject(e.metadataJson) } catch (_: Exception) { JSONObject() } else JSONObject())
            })
        }
        return HttpResponse.json(JSONObject().put("ok", true).put("events", arr).toString())
    }

    private fun handleMeta(method: String, path: String): HttpResponse {
        val clean = path.removePrefix("/meta/").removeSuffix("/")
        return when {
            clean == "activities" -> {
                val acts = listOf("Work", "Deep Work", "Coding", "Reading", "Exercise", "Writing", "Meditation", "Planning", "Learning")
                HttpResponse.json(JSONArray(acts).toString())
            }
            clean == "think-topics" -> {
                val topics = listOf("General", "Life", "Strategy", "Ideas", "Philosophy", "Tech", "Learning", "Health")
                HttpResponse.json(JSONArray(topics).toString())
            }
            clean == "theme" -> {
                HttpResponse.json(JSONObject().put("ok", true).toString())
            }
            else -> HttpResponse.json(JSONObject().put("ok", true).toString())
        }
    }

    private fun handleDayTimeline(method: String, path: String, request: HttpRequest): HttpResponse {
        if (method != "GET") {
            return HttpResponse.error("Method $method not allowed for timeline", 405)
        }

        val queryDate = request.queryParams["date"]
        val clean = path.removePrefix("/api/v3").trim('/')
        val pathDate = clean.substringAfterLast("/").takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
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

    private fun handleDayTracker(method: String, request: HttpRequest): HttpResponse {
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

    private fun handleCapture(method: String, request: HttpRequest): HttpResponse {
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

    private fun handleEntries(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val pathClean = path.removePrefix("/api/v3").trim('/')
                val pathCat = if (pathClean.startsWith("events/")) {
                    pathClean.removePrefix("events/").substringBefore('/').takeIf { it != "today" && it.isNotBlank() }
                } else null
                val cat = request.queryParams["category"] ?: pathCat
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
                repository.recycleItem(id, "events", "{}")
                val ok = repository.deleteEvent(id)
                repository.deleteThought(id)
                repository.deleteTask(id)
                repository.deleteSession(id)
                syncManager.deleteRemoteEvent(id)
                return HttpResponse.json(JSONObject().put("ok", ok).put("recycled", true).put("id", id).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for entries", 405)
        }
    }

    private fun formatSeconds(totalSec: Long): String {
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return when {
            h > 0 -> String.format(Locale.US, "%dh %dm", h, m)
            m > 0 -> String.format(Locale.US, "%dm %ds", m, s)
            else -> String.format(Locale.US, "%ds", s)
        }
    }
}
