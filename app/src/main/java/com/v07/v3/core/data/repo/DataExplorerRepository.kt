package com.v07.v3.core.data.repo

import android.util.Log
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3UnifiedEntry
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated Repository for Unified Data Explorer Engine across all subsystems and operational purge.
 */
class DataExplorerRepository(
    dbHelper: V07Database,
    private val pluginStorage: PluginStorageRepository
) : BaseRepository(dbHelper) {

    fun queryUnifiedEntries(
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        query: String? = null,
        limit: Int = 200
    ): List<V3UnifiedEntry> {
        val db = dbHelper.readableDatabase
        val entries = mutableListOf<V3UnifiedEntry>()
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val catFilter = category?.trim()?.lowercase()
        val q = query?.trim()?.lowercase()
        val isQuick = catFilter == "quick"
        val quickCategories = setOf("thought", "thoughts", "win", "lesson", "quick")

        // Map plugin IDs to constituent categories
        val pluginCategoryMap = mapOf(
            "com.v07.chronosynth" to setOf("session", "sessions", "focus"),
            "focus" to setOf("session", "sessions", "focus"),
            "session" to setOf("session", "sessions", "focus"),
            "sessions" to setOf("session", "sessions", "focus"),
            "com.v07.tasks" to setOf("task", "tasks"),
            "tasks" to setOf("task", "tasks"),
            "task" to setOf("task", "tasks"),
            "com.v07.vitals" to setOf("mood", "vitals", "energy"),
            "vitals" to setOf("mood", "vitals", "energy"),
            "mood" to setOf("mood", "vitals", "energy"),
            "energy" to setOf("mood", "vitals", "energy"),
            "com.v07.habits" to setOf("habit", "habits", "pushups"),
            "habits" to setOf("habit", "habits", "pushups"),
            "habit" to setOf("habit", "habits", "pushups"),
            "com.v07.reader" to setOf("book", "reading", "reader", "share", "shares"),
            "reading" to setOf("book", "reading", "reader", "share", "shares"),
            "reader" to setOf("book", "reading", "reader", "share", "shares"),
            "share" to setOf("share", "shares"),
            "shares" to setOf("share", "shares"),
            "com.v07.timetracker" to setOf("screentime", "app_usage", "usage", "timetracker"),
            "screentime" to setOf("screentime", "app_usage", "usage", "timetracker"),
            "timetracker" to setOf("screentime", "app_usage", "usage", "timetracker"),
            "com.v07.gamify" to setOf("level", "xp", "milestone", "daytracker"),
            "gamify" to setOf("level", "xp", "milestone", "daytracker"),
            "milestone" to setOf("milestone", "milestones", "daytracker"),
            "milestones" to setOf("milestone", "milestones", "daytracker"),
            "daytracker" to setOf("milestone", "milestones", "daytracker"),
            "gita" to setOf("gita", "verse", "wisdom", "contemplation"),
            "weekly" to setOf("weekly", "digest", "briefing"),
            "com.v07.running" to setOf("running", "run", "runtime"),
            "com.v07.runner" to setOf("running", "run", "runtime"),
            "running" to setOf("running", "run", "runtime"),
            "thought" to setOf("thought", "thoughts"),
            "thoughts" to setOf("thought", "thoughts"),
            "core" to setOf("thought", "thoughts", "win", "lesson", "quick", "checkin", "checkout")
        )

        val checkCat = { cat: String ->
            val lc = cat.lowercase()
            if (catFilter.isNullOrBlank() || catFilter == "all") {
                true
            } else if (isQuick) {
                quickCategories.contains(lc) || (!listOf("session", "sessions", "task", "tasks", "app_usage", "screentime", "checkin", "checkout").contains(lc))
            } else if (pluginCategoryMap.containsKey(catFilter)) {
                pluginCategoryMap[catFilter]?.contains(lc) == true
            } else {
                catFilter == lc
            }
        }

        val checkText = { texts: List<String?> ->
            if (q.isNullOrBlank()) true
            else texts.any { it != null && it.lowercase().contains(q) }
        }

        fun checkDate(d: String?, ts: Long = 0L): Boolean {
            if (startDate.isNullOrBlank() && endDate.isNullOrBlank()) {
                return true
            }
            val dateStr = if (!d.isNullOrBlank()) d else (if (ts > 0L) dateFmt.format(Date(ts)) else "")
            var ok = true
            if (!startDate.isNullOrBlank()) ok = ok && dateStr >= startDate
            if (!endDate.isNullOrBlank()) ok = ok && dateStr <= endDate
            return ok
        }

        val excludeClause = getSafeExcludeClause(db)

        val targetCats = if (catFilter.isNullOrBlank() || catFilter == "all") {
            null
        } else if (isQuick) {
            quickCategories
        } else if (pluginCategoryMap.containsKey(catFilter)) {
            pluginCategoryMap[catFilter]
        } else {
            setOf(catFilter)
        }

        val subqueryLimit = limit.coerceAtLeast(1000).toString()

        // 1. Ingest events table with SQL category push-down
        try {
            val eventsSelection = if (!targetCats.isNullOrEmpty()) {
                val inList = targetCats.joinToString("','", "LOWER(category) IN ('", "')")
                "$excludeClause AND $inList"
            } else {
                excludeClause
            }
            val eventLimit = if (targetCats.isNullOrEmpty()) "2000" else subqueryLimit
            val cur = db.query("events", null, eventsSelection, null, null, null, "timestamp DESC", eventLimit)
            cur.use {
                while (it.moveToNext()) {
                    try {
                        val id = it.getString(it.getColumnIndexOrThrow("id"))
                        val cat = it.getString(it.getColumnIndexOrThrow("category")) ?: "general"
                        val ts = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                        val date = it.getString(it.getColumnIndexOrThrow("date")) ?: dateFmt.format(Date(ts))
                        val rawTitle = it.getString(it.getColumnIndexOrThrow("title"))
                        val content = it.getString(it.getColumnIndexOrThrow("content")) ?: ""
                        val title = if (!rawTitle.isNullOrBlank()) rawTitle else cat.replaceFirstChar { c -> c.uppercase() }
                        val score = it.getInt(it.getColumnIndexOrThrow("score"))
                        val meta = it.getString(it.getColumnIndexOrThrow("metadata_json")) ?: ""

                        if (checkCat(cat) && checkDate(date, ts) && checkText(listOf(title, content, cat, meta))) {
                            val displayCat = when (cat) {
                                "win" -> "Daily Win"
                                "lesson" -> "Daily Lesson"
                                "thought" -> "Thought"
                                "pushups" -> "Physical Cadence"
                                "habit" -> "Gentle Habit"
                                "screentime" -> "Digital Stillness"
                                "running", "run" -> "Runtime Tracker"
                                "session", "focus" -> "Focus Sanctuary"
                                "task" -> "Task"
                                "milestone", "daytracker" -> "Milestone Target"
                                "share" -> "Saved Link"
                                "checkin" -> "Morning Check-In"
                                "checkout" -> "Evening Check-Out"
                                "vitals", "mood", "energy" -> "Vitals & Energy"
                                else -> cat.replaceFirstChar { c -> c.uppercase() }
                            }
                            entries.add(
                                V3UnifiedEntry(
                                    id = id,
                                    category = cat,
                                    displayCategory = displayCat,
                                    title = title,
                                    subtitle = if (score > 0) "Score: $score/5" else "",
                                    content = content,
                                    date = date,
                                    timeFormatted = timeFmt.format(Date(ts)),
                                    timestamp = ts,
                                    score = if (score > 0) score else null,
                                    rawJson = meta
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("DataExplorerRepository", "Skipping malformed event row: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DataExplorerRepository", "Error querying events table: ${e.message}")
        }

        // 2. Ingest thoughts table
        val shouldIngestThoughts = catFilter.isNullOrBlank() || catFilter == "all" || isQuick ||
            catFilter == "thought" || catFilter == "thoughts" ||
            catFilter == "core"
        if (shouldIngestThoughts) {
            try {
                val cur = db.query("thoughts", null, excludeClause, null, null, null, "timestamp DESC", subqueryLimit)
                cur.use {
                    while (it.moveToNext()) {
                        try {
                            val id = it.getString(it.getColumnIndexOrThrow("id"))
                            val ts = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                            val date = it.getString(it.getColumnIndexOrThrow("date")) ?: dateFmt.format(Date(ts))
                            val topic = it.getString(it.getColumnIndexOrThrow("topic")) ?: "Thought"
                            val content = it.getString(it.getColumnIndexOrThrow("content")) ?: ""
                            val tags = it.getString(it.getColumnIndexOrThrow("tags")) ?: ""

                            if (checkDate(date, ts) && checkText(listOf(topic, content, tags, "thought"))) {
                                entries.add(
                                    V3UnifiedEntry(
                                        id = id,
                                        category = "thought",
                                        displayCategory = "Thought",
                                        title = if (topic.isNotBlank()) topic else "Thought",
                                        subtitle = if (tags.isNotBlank()) "#$tags" else "",
                                        content = content,
                                        date = date,
                                        timeFormatted = timeFmt.format(Date(ts)),
                                        timestamp = ts,
                                        tags = tags
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.w("DataExplorerRepository", "Skipping malformed thought row: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DataExplorerRepository", "Error querying thoughts table: ${e.message}")
            }
        }

        // 3. Ingest sessions table
        if (checkCat("session") || checkCat("sessions") || checkCat("focus")) {
            try {
                val cur = db.query("sessions", null, excludeClause, null, null, null, "start_time DESC", subqueryLimit)
                cur.use {
                    while (it.moveToNext()) {
                        val id = it.getString(it.getColumnIndexOrThrow("id"))
                        val act = it.getString(it.getColumnIndexOrThrow("activity"))
                        val st = it.getLong(it.getColumnIndexOrThrow("start_time"))
                        val durSec = it.getLong(it.getColumnIndexOrThrow("duration_sec"))
                        val notes = it.getString(it.getColumnIndexOrThrow("notes")) ?: ""
                        val date = dateFmt.format(Date(st))

                        if (checkDate(date) && checkText(listOf(act, notes, "focus", "session"))) {
                            val mins = durSec / 60
                            entries.add(
                                V3UnifiedEntry(
                                    id = id,
                                    category = "session",
                                    displayCategory = "Focus Sanctuary",
                                    title = act,
                                    subtitle = "${mins}m focused duration",
                                    content = notes,
                                    date = date,
                                    timeFormatted = timeFmt.format(Date(st)),
                                    timestamp = st,
                                    durationSec = durSec
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 4. Ingest tasks table
        if (checkCat("task") || checkCat("tasks")) {
            try {
                val cur = db.query("tasks", null, excludeClause, null, null, null, "created_at DESC", subqueryLimit)
                cur.use {
                    while (it.moveToNext()) {
                        val id = it.getString(it.getColumnIndexOrThrow("id"))
                        val title = it.getString(it.getColumnIndexOrThrow("title"))
                        val priority = it.getString(it.getColumnIndexOrThrow("priority"))
                        val status = it.getString(it.getColumnIndexOrThrow("status"))
                        val createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                        val compAt = if (it.isNull(it.getColumnIndexOrThrow("completed_at"))) null else it.getLong(it.getColumnIndexOrThrow("completed_at"))
                        val deadline = it.getString(it.getColumnIndexOrThrow("deadline")) ?: ""
                        val ts = compAt ?: createdAt
                        val date = dateFmt.format(Date(ts))

                        if (checkDate(date) && checkText(listOf(title, priority, status, deadline, "task"))) {
                            entries.add(
                                V3UnifiedEntry(
                                    id = id,
                                    category = "task",
                                    displayCategory = if (status == "completed") "Task (Done)" else "Task (Pending)",
                                    title = title,
                                    subtitle = "Priority: $priority • Status: $status",
                                    content = if (deadline.isNotBlank()) "Deadline: $deadline" else "",
                                    date = date,
                                    timeFormatted = timeFmt.format(Date(ts)),
                                    timestamp = ts
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 5. Ingest day_tracker (milestones & targets)
        if (checkCat("milestone") || checkCat("daytracker") || checkCat("gamify")) {
            try {
                val cur = db.query("day_tracker", null, excludeClause, null, null, null, "target_date DESC", subqueryLimit)
                cur.use {
                    while (it.moveToNext()) {
                        val id = it.getString(it.getColumnIndexOrThrow("id"))
                        val title = it.getString(it.getColumnIndexOrThrow("title"))
                        val targetDate = it.getString(it.getColumnIndexOrThrow("target_date"))
                        val hour = it.getInt(it.getColumnIndexOrThrow("hour"))
                        val createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                        val timeStr = if (hour in 0..23) String.format(Locale.getDefault(), "%02d:00", hour) else "All Day"

                        if (checkDate(targetDate, createdAt) && checkText(listOf(title, targetDate, "milestone"))) {
                            entries.add(
                                V3UnifiedEntry(
                                    id = id,
                                    category = "milestone",
                                    displayCategory = "Milestone Target",
                                    title = title,
                                    subtitle = "Target: $targetDate • $timeStr",
                                    content = "Target Date: $targetDate",
                                    date = targetDate,
                                    timeFormatted = timeStr,
                                    timestamp = createdAt
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 6. Ingest app_usage (screen time & digital stillness) - only if screentime domain is specifically filtered
        if (catFilter != null && catFilter != "all" && (checkCat("screentime") || checkCat("app_usage") || checkCat("timetracker"))) {
            try {
                val cur = db.query("app_usage", null, excludeClause, null, null, null, "start_time DESC", subqueryLimit)
                cur.use {
                    while (it.moveToNext()) {
                        val id = it.getString(it.getColumnIndexOrThrow("id"))
                        val pkg = it.getString(it.getColumnIndexOrThrow("package_name"))
                        val appName = it.getString(it.getColumnIndexOrThrow("app_name"))
                        val st = it.getLong(it.getColumnIndexOrThrow("start_time"))
                        val durMs = it.getLong(it.getColumnIndexOrThrow("duration_ms"))
                        val date = dateFmt.format(Date(st))
                        val mins = durMs / 60000

                        if (checkDate(date, st) && checkText(listOf(appName, pkg, "screentime", "stillness"))) {
                            entries.add(
                                V3UnifiedEntry(
                                    id = id,
                                    category = "screentime",
                                    displayCategory = "Digital Stillness",
                                    title = appName,
                                    subtitle = "${mins}m active • $pkg",
                                    content = "Package: $pkg",
                                    date = date,
                                    timeFormatted = timeFmt.format(Date(st)),
                                    timestamp = st,
                                    durationSec = durMs / 1000
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 7. Ingest plugin_storage for Running (com.v07.running -> history)
        if (checkCat("running") || checkCat("run") || checkCat("runtime")) {
            try {
                val historyStr = pluginStorage.getPluginValue("com.v07.running", "history")
                if (!historyStr.isNullOrBlank()) {
                    val arr = JSONArray(historyStr)
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        val id = item.optString("id", "run_$i")
                        val date = item.optString("date", "")
                        val ts = item.optLong("timestamp", 0L).let { if (it > 0L) it else (try { dateFmt.parse(date)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }) }
                        val dist = item.optDouble("distanceKm", item.optDouble("distance_km", 0.0))
                        val durSec = item.optLong("durationSec", item.optLong("duration_sec", 0L))
                        val pace = item.optString("pace", item.optString("pace_formatted", ""))
                        val title = item.optString("title", "").trim().ifEmpty { "${dist} km Run" }
                        val note = item.optString("note", item.optString("notes", "")).trim()
                        val rType = item.optString("run_type", "")

                        if (checkDate(date, ts) && checkText(listOf(title, note, pace, rType, "running", "run"))) {
                            val mins = durSec / 60
                            entries.add(
                                V3UnifiedEntry(
                                    id = id,
                                    category = "running",
                                    displayCategory = "Runtime Tracker",
                                    title = title,
                                    subtitle = "${dist} km • ${if (pace.isNotBlank()) pace else "${mins}m"}",
                                    content = if (note.isNotBlank()) note else "${dist} km run in ${mins}m",
                                    date = if (date.isNotBlank()) date else dateFmt.format(Date(ts)),
                                    timeFormatted = timeFmt.format(Date(ts)),
                                    timestamp = ts,
                                    durationSec = durSec,
                                    rawJson = item.toString()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("DataExplorerRepository", "Error parsing running plugin storage: ${e.message}")
            }
        }

        // 8. Ingest plugin_storage for Habits (com.v07.habits -> definitions & logs)
        if (checkCat("habit") || checkCat("habits")) {
            try {
                val defsMap = mutableMapOf<String, String>()
                val defsStr = pluginStorage.getPluginValue("com.v07.habits", "definitions")
                if (!defsStr.isNullOrBlank()) {
                    val dArr = JSONArray(defsStr)
                    for (i in 0 until dArr.length()) {
                        val d = dArr.getJSONObject(i)
                        val hid = d.optString("id", "")
                        val hname = d.optString("name", d.optString("title", "Habit"))
                        val hIcon = d.optString("icon", d.optString("emoji", "🔥"))
                        if (hid.isNotBlank()) {
                            defsMap[hid] = "$hIcon $hname"
                        }
                    }
                }

                val logsStr = pluginStorage.getPluginValue("com.v07.habits", "logs")
                if (!logsStr.isNullOrBlank()) {
                    val lArr = JSONArray(logsStr)
                    for (i in 0 until lArr.length()) {
                        val item = lArr.getJSONObject(i)
                        val hid = item.optString("habit_id", "")
                        val date = item.optString("date", "")
                        val isDone = item.optBoolean("completed", true)
                        val habitName = item.optString("name", item.optString("habit_name", defsMap[hid] ?: "Habit Completed"))
                        val ts = item.optLong("timestamp", 0L).let { if (it > 0L) it else (try { dateFmt.parse(date)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }) }

                        if (isDone && checkDate(date, ts) && checkText(listOf(habitName, hid, "habit"))) {
                            entries.add(
                                V3UnifiedEntry(
                                    id = item.optString("id", "hbt_log_${date}_$hid"),
                                    category = "habit",
                                    displayCategory = "Gentle Habit",
                                    title = habitName,
                                    subtitle = "Ritual Completed",
                                    content = "Completed on $date",
                                    date = if (date.isNotBlank()) date else dateFmt.format(Date(ts)),
                                    timeFormatted = timeFmt.format(Date(ts)),
                                    timestamp = ts,
                                    rawJson = item.toString()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("DataExplorerRepository", "Error parsing habits plugin storage: ${e.message}")
            }
        }

        // 9. Ingest plugin_storage for Vitals (com.v07.vitals -> logs)
        if (checkCat("vitals") || checkCat("mood") || checkCat("energy")) {
            try {
                val vitalsStr = pluginStorage.getPluginValue("com.v07.vitals", "logs")
                if (!vitalsStr.isNullOrBlank()) {
                    val vArr = JSONArray(vitalsStr)
                    for (i in 0 until vArr.length()) {
                        val item = vArr.getJSONObject(i)
                        val id = item.optString("id", "vit_$i")
                        val type = item.optString("type", "mood")
                        val score = item.optDouble("score", 3.0)
                        val date = item.optString("date", "")
                        val ts = item.optLong("timestamp", 0L).let { if (it > 0L) it else (try { dateFmt.parse(date)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }) }
                        val note = item.optString("note", "").trim()

                        if (checkCat(type) && checkDate(date, ts) && checkText(listOf(type, note, "vital", "mood", "energy"))) {
                            entries.add(
                                V3UnifiedEntry(
                                    id = id,
                                    category = type,
                                    displayCategory = "Vitals & Energy",
                                    title = "${type.replaceFirstChar { c -> c.uppercase() }} Check-in",
                                    subtitle = "Rating: ${score.toInt()}/5",
                                    content = if (note.isNotBlank()) note else "Rating: $score/5",
                                    date = if (date.isNotBlank()) date else dateFmt.format(Date(ts)),
                                    timeFormatted = timeFmt.format(Date(ts)),
                                    timestamp = ts,
                                    score = score.toInt(),
                                    rawJson = item.toString()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("DataExplorerRepository", "Error parsing vitals plugin storage: ${e.message}")
            }
        }

        val distinct = entries.distinctBy { it.id }.toMutableList()
        distinct.sortByDescending { it.timestamp }
        return distinct.take(limit)
    }

    /**
     * Complete purge of operational data tables.
     * Deletes sessions, tasks, thoughts, milestones, screen time, recycle bin, tombstones, and ledger events.
     */
    fun purgeAllData(keepPluginConfigs: Boolean = true): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            db.delete("sessions", null, null)
            db.delete("tasks", null, null)
            db.delete("thoughts", null, null)
            db.delete("day_tracker", null, null)
            db.delete("app_usage", null, null)
            db.delete("recycle_bin", null, null)
            db.delete("deleted_tombstones", null, null)
            db.delete("events", null, null)
            db.delete("plugin_events", null, null)
            if (!keepPluginConfigs) {
                db.delete("plugin_storage", null, null)
            } else {
                db.delete("plugin_storage", "key IN ('history', 'logs', 'definitions', 'entries', 'shelf') OR key LIKE 'run_%' OR key LIKE 'entry_%' OR plugin_id = 'com.v07.running'", null)
            }
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.e("DataExplorerRepository", "Purge all data failed: ${e.message}", e)
            false
        } finally {
            db.endTransaction()
        }
    }
}
