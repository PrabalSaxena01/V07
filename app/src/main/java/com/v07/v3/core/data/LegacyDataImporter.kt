package com.v07.v3.core.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class ImportSummary(
    var sessionsCount: Int = 0,
    var tasksCount: Int = 0,
    var thoughtsCount: Int = 0,
    var dayTrackerCount: Int = 0,
    var recycleBinCount: Int = 0,
    var habitsMigratedToPlugin: Int = 0,
    var runningMigratedToPlugin: Int = 0,
    var vitalsMigratedToPlugin: Int = 0,
    var skippedJunkFiles: Int = 0,
    var errorLines: Int = 0
)

/**
 * Intelligent, Selective Sanitizing Importer for V3.
 *
 * Implements the Core Module boundary:
 * 1. Core Modules:
 *    - Session Timer -> sessions table
 *    - Tasks Discipline -> tasks table
 *    - Thoughts & Reflections -> thoughts table
 * 2. Core Utilities:
 *    - Day Tracker -> day_tracker table
 *    - Recycle Bin -> recycle_bin table
 * 3. Converted Plugins:
 *    - Habits -> plugin_storage ("com.v07.habits")
 *    - Running -> plugin_storage ("com.v07.running")
 *    - Vitals (Mood & Energy) -> plugin_storage ("com.v07.vitals")
 * 4. Everything else (Chess, DSA, temporary UI scrapers) is completely discarded.
 */
class LegacyDataImporter(private val context: Context) {

    private val dbHelper = V07Database.getInstance(context)
    private val repo = EventRepository.getInstance(context)

    companion object {
        const val TAG = "LegacyDataImporter"

        val DISCARD_PATTERNS = listOf(
            "_ui.jsonl",
            "journal.jsonl",
            "chess",
            "dsa",
            "metrics",
            "client_logs",
            ".sync-conflict"
        )
    }
    fun importAllLegacyData(): ImportSummary {
        val candidates = listOf(
            File("/storage/emulated/0/V07Data/events"),
            File("/storage/emulated/0/V07Data/08 - System/Data/events"),
            File("/storage/emulated/0/V07Data"),
            File(context.getExternalFilesDir(null), "events"),
            File(context.filesDir, "events")
        )
        val combined = ImportSummary()
        for (dir in candidates) {
            if (dir.exists() && dir.isDirectory) {
                val res = importFromDirectory(dir)
                combined.sessionsCount += res.sessionsCount
                combined.tasksCount += res.tasksCount
                combined.thoughtsCount += res.thoughtsCount
                combined.dayTrackerCount += res.dayTrackerCount
                combined.recycleBinCount += res.recycleBinCount
                combined.habitsMigratedToPlugin += res.habitsMigratedToPlugin
                combined.runningMigratedToPlugin += res.runningMigratedToPlugin
                combined.vitalsMigratedToPlugin += res.vitalsMigratedToPlugin
                combined.skippedJunkFiles += res.skippedJunkFiles
                combined.errorLines += res.errorLines
                if (combined.sessionsCount > 0 || combined.tasksCount > 0) {
                    break
                }
            }
        }
        return combined
    }

    fun importFromDirectory(eventsDir: File): ImportSummary {
        val summary = ImportSummary()
        if (!eventsDir.exists() || !eventsDir.isDirectory) {
            Log.w(TAG, "Legacy directory not found: ${eventsDir.absolutePath}")
            return summary
        }

        Log.i(TAG, "Beginning selective legacy data import from: ${eventsDir.absolutePath}")
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val fileList = mutableListOf<File>()
            eventsDir.listFiles()?.let { fileList.addAll(it) }

            // Also check parent directory for day_tracker.jsonl if present (common legacy layout)
            eventsDir.parentFile?.let { parent ->
                val dt = File(parent, "day_tracker.jsonl")
                if (dt.exists() && !fileList.contains(dt)) fileList.add(dt)
            }

            for (file in fileList) {
                val name = file.name.lowercase()

                // Skip blacklisted garbage, unapproved files, and temporary UI scrapers
                if (DISCARD_PATTERNS.any { name.contains(it) } || file.length() == 0L) {
                    Log.d(TAG, "Skipping legacy junk file: $name")
                    summary.skippedJunkFiles++
                    continue
                }

                when {
                    // Core Module: Sessions
                    name.startsWith("session") -> importSessions(file, summary)

                    // Core Module: Tasks
                    name.startsWith("task") -> importTasks(file, summary)

                    // Core Module: Thoughts
                    name.startsWith("thought") || name.startsWith("pinned_thought") -> importThoughts(file, summary)

                    // Core Utility: Day Tracker
                    name.startsWith("day_tracker") -> importDayTracker(file, summary)

                    // Core Utility: Recycle Bin
                    name.startsWith("recycle_bin") -> importRecycleBin(file, summary)

                    // Converted Plugin: Habits
                    name.startsWith("habit") || name.startsWith("hbt") -> importHabitsToPlugin(file, summary)

                    // Converted Plugin: Running
                    name.startsWith("running") || name.startsWith("run") -> importRunningToPlugin(file, summary)

                    // Converted Plugin: Vitals
                    name.startsWith("mood") || name.startsWith("energy") -> importVitalsToPlugin(file, summary)

                    else -> {
                        Log.d(TAG, "Ignoring non-core legacy file: $name")
                        summary.skippedJunkFiles++
                    }
                }
            }

            db.setTransactionSuccessful()
            Log.i(TAG, "Legacy import completed successfully: $summary")
        } catch (e: Exception) {
            Log.e(TAG, "Legacy import failed: ${e.message}", e)
        } finally {
            db.endTransaction()
        }

        return summary
    }

    // ─── CORE MODULE: SESSIONS ───────────────────────────────────────────────

    private fun importSessions(file: File, summary: ImportSummary) {
        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                val activity = json.optString("activity", "Focus").trim()
                val startTime = parseTimestamp(json.optString("start_time", json.optString("ts", "")))
                val endTime = if (json.has("end_time")) parseTimestamp(json.optString("end_time")) else null
                val durationSec = json.optLong("duration_sec", json.optLong("duration", 0))

                val sId = json.optString("id", "").ifEmpty { "sess_${startTime}_${java.util.UUID.randomUUID().toString().take(6)}" }
                repo.insertSession(
                    V3Session(
                        id = sId,
                        activity = activity,
                        startTime = startTime,
                        endTime = endTime,
                        durationSec = durationSec,
                        notes = json.optString("notes", json.optString("note", "")).ifEmpty { null }
                    )
                )
                val sDate = parseDate(json.optString("date", startTime.toString()))
                repo.insertEvent(
                    V3Event(
                        id = sId,
                        category = "session",
                        date = sDate,
                        ts = sDate,
                        timestamp = startTime,
                        title = activity,
                        content = json.optString("notes", json.optString("note", "")).ifEmpty { "${durationSec / 60}m focus session" },
                        score = 0,
                        metadataJson = json.toString()
                    )
                )
                summary.sessionsCount++
            } catch (e: Exception) {
                summary.errorLines++
            }
        }
    }

    // ─── CORE MODULE: TASKS ──────────────────────────────────────────────────

    private fun importTasks(file: File, summary: ImportSummary) {
        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                var title = json.optString("title", json.optString("text", "")).trim()
                if (title.isBlank()) return@forEachLine

                // Strip legacy markdown checkboxes if present: "- [ ] " or "- [x] "
                if (title.startsWith("- [ ]") || title.startsWith("- [x]")) {
                    title = title.substring(5).trim()
                }

                val priority = when (json.optString("priority", "").uppercase()) {
                    "HIGH", "P1", "RED" -> "HIGH"
                    "LOW", "P3", "GRAY" -> "LOW"
                    else -> "MEDIUM"
                }

                val status = if (json.optBoolean("done", false) ||
                    json.optString("status", "") == "completed" ||
                    json.optString("action", "") == "done") "completed" else "pending"

                val createdAt = parseTimestamp(json.optString("ts", json.optString("date", "")))

                val tId = json.optString("id", json.optString("task_id", "")).ifEmpty { "task_${createdAt}_${java.util.UUID.randomUUID().toString().take(6)}" }
                val deadline = json.optString("deadline").ifEmpty { null }
                repo.insertTask(
                    V3Task(
                        id = tId,
                        title = title,
                        priority = priority,
                        status = status,
                        createdAt = createdAt,
                        deadline = deadline
                    )
                )
                val tDate = parseDate(json.optString("date", createdAt.toString()))
                repo.insertEvent(
                    V3Event(
                        id = tId,
                        category = "task",
                        date = tDate,
                        ts = tDate,
                        timestamp = createdAt,
                        title = title,
                        content = "Priority: $priority • Status: $status${if (!deadline.isNullOrBlank()) " • Deadline: $deadline" else ""}",
                        score = 0,
                        metadataJson = json.toString()
                    )
                )
                summary.tasksCount++
            } catch (e: Exception) {
                summary.errorLines++
            }
        }
    }

    // ─── CORE MODULE: THOUGHTS ───────────────────────────────────────────────

    private fun importThoughts(file: File, summary: ImportSummary) {
        val isPinnedFile = file.name.lowercase().contains("pinned")
        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                val text = json.optString("text", json.optString("content", "")).trim()
                if (text.isBlank()) return@forEachLine

                val topic = json.optString("topic", "").ifEmpty { null }
                val date = parseDate(json.optString("date", json.optString("ts", "")))
                val ts = parseTimestamp(json.optString("ts", json.optString("date", "")))
                val pinned = isPinnedFile || json.optBoolean("pinned", false)

                val thId = json.optString("id", "").ifEmpty { "thought_${ts}_${java.util.UUID.randomUUID().toString().take(6)}" }
                repo.insertThought(
                    V3Thought(
                        id = thId,
                        timestamp = ts,
                        date = date,
                        topic = topic,
                        content = text,
                        pinned = pinned
                    )
                )
                repo.insertEvent(
                    V3Event(
                        id = thId,
                        category = "thought",
                        date = date,
                        ts = date,
                        timestamp = ts,
                        title = if (!topic.isNullOrBlank()) topic else "Thought",
                        content = text,
                        score = 0,
                        metadataJson = json.toString()
                    )
                )
                summary.thoughtsCount++
            } catch (e: Exception) {
                summary.errorLines++
            }
        }
    }

    // ─── CORE UTILITY: DAY TRACKER ───────────────────────────────────────────

    private fun importDayTracker(file: File, summary: ImportSummary) {
        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                val title = json.optString("title", "Milestone").trim()
                val targetDate = parseDate(json.optString("date", json.optString("target_date", "")))
                if (title.isBlank() || targetDate.isBlank()) return@forEachLine
                val createdAt = parseTimestamp(json.optString("ts", json.optString("created_at", "")))
                val hour = json.optInt("hour", 0)
                val pinned = json.optBoolean("pinned", false)
                val dtId = json.optString("id", "").ifEmpty { "dt_${createdAt}_${java.util.UUID.randomUUID().toString().take(6)}" }

                repo.insertDayTrackerItem(
                    V3DayTrackerItem(
                        id = dtId,
                        title = title,
                        targetDate = targetDate,
                        hour = hour,
                        pinned = pinned,
                        createdAt = createdAt
                    )
                )
                val timeStr = if (hour in 0..23) String.format(Locale.getDefault(), "%02d:00", hour) else "All Day"
                repo.insertEvent(
                    V3Event(
                        id = dtId,
                        category = "milestone",
                        date = targetDate,
                        ts = targetDate,
                        timestamp = createdAt,
                        title = "🎯 $title",
                        content = "Target Date: $targetDate • $timeStr",
                        score = 0,
                        metadataJson = json.toString()
                    )
                )
                summary.dayTrackerCount++
            } catch (e: Exception) {
                summary.errorLines++
            }
        }
    }

    // ─── CORE UTILITY: RECYCLE BIN ───────────────────────────────────────────

    private fun importRecycleBin(file: File, summary: ImportSummary) {
        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                val recycleId = json.optString("recycle_id", "")
                val category = json.optString("category", "general")
                val dataObj = json.optJSONObject("data")
                val originalId = dataObj?.optString("id") ?: json.optString("id", "")
                val dataJson = dataObj?.toString() ?: json.toString()

                if (recycleId.isNotBlank() && originalId.isNotBlank()) {
                    repo.recycleItem(
                        originalId = originalId,
                        category = category,
                        dataJson = dataJson
                    )
                    summary.recycleBinCount++
                }
            } catch (e: Exception) {
                summary.errorLines++
            }
        }
    }

    // ─── CONVERTED PLUGIN: HABITS ────────────────────────────────────────────

    private fun importHabitsToPlugin(file: File, summary: ImportSummary) {
        val pluginId = "com.v07.habits"
        val habitsList = JSONArray()
        val logsList = JSONArray()

        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                val kind = json.optString("kind", "")
                val id = json.optString("id", json.optString("habit_id", ""))

                if (kind == "def" || json.has("title")) {
                    val title = json.optString("title", "").trim()
                    if (title.isNotEmpty()) {
                        habitsList.put(JSONObject().apply {
                            put("id", id.ifEmpty { "hbt_${title.lowercase().replace(" ", "_")}" })
                            put("title", title)
                            put("icon", json.optString("icon", "🔥"))
                            put("target_freq", json.optString("target_freq", "daily"))
                        })
                        summary.habitsMigratedToPlugin++
                    }
                } else if (kind == "log" || json.has("completed")) {
                    val habitId = json.optString("habit_id", id)
                    val date = parseDate(json.optString("date", json.optString("ts", "")))
                    if (habitId.isNotEmpty() && date.isNotEmpty()) {
                        logsList.put(JSONObject().apply {
                            put("habit_id", habitId)
                            put("date", date)
                            put("completed", json.optBoolean("completed", true))
                        })
                    }
                }
            } catch (e: Exception) {
                summary.errorLines++
            }
        }

        if (habitsList.length() > 0) {
            repo.setPluginValue(pluginId, "definitions", habitsList.toString())
        }
        if (logsList.length() > 0) {
            repo.setPluginValue(pluginId, "logs", logsList.toString())
        }
    }

    // ─── CONVERTED PLUGIN: RUNNING ───────────────────────────────────────────

    private fun importRunningToPlugin(file: File, summary: ImportSummary) {
        val pluginId = "com.v07.running"
        val runsList = JSONArray()

        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                val distanceKm = json.optDouble("distance_km", json.optDouble("km", 0.0))
                if (distanceKm <= 0.0) return@forEachLine

                // Fix running duration discrepancy strictly to integer seconds:
                val rawDur = json.optDouble("duration_sec", json.optDouble("duration_min", json.optDouble("duration", 0.0)))
                val durationSec: Long = when {
                    json.has("duration_sec") -> json.optLong("duration_sec")
                    rawDur / distanceKm > 20.0 || rawDur >= 100.0 -> rawDur.toLong()
                    else -> (rawDur * 60.0).toLong()
                }
                val paceSecKm = if (distanceKm > 0) (durationSec / distanceKm).toLong() else 0L

                runsList.put(JSONObject().apply {
                    put("id", json.optString("id", ""))
                    put("date", parseDate(json.optString("date", json.optString("ts", ""))))
                    put("timestamp", parseTimestamp(json.optString("ts", json.optString("date", ""))))
                    put("distance_km", distanceKm)
                    put("duration_sec", durationSec)
                    put("pace_sec_km", paceSecKm)
                    put("notes", json.optString("note", json.optString("notes", "")).ifEmpty { null })
                })
                summary.runningMigratedToPlugin++
            } catch (e: Exception) {
                summary.errorLines++
            }
        }

        if (runsList.length() > 0) {
            repo.setPluginValue(pluginId, "history", runsList.toString())
        }
    }

    // ─── CONVERTED PLUGIN: VITALS ────────────────────────────────────────────

    private fun importVitalsToPlugin(file: File, summary: ImportSummary) {
        val pluginId = "com.v07.vitals"
        val vitalsList = JSONArray()

        file.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            try {
                val json = JSONObject(line)
                val isMood = file.name.lowercase().contains("mood")
                val category = if (isMood) "mood" else "energy"
                val rawScore = json.optDouble("${category}_score", json.optDouble("score", json.optDouble("val", 0.0)))
                val score = if (rawScore > 5.0) rawScore / 2.0 else rawScore

                vitalsList.put(JSONObject().apply {
                    put("id", json.optString("id", ""))
                    put("type", category)
                    put("score", if (score > 0.0) score else 3.0)
                    put("date", parseDate(json.optString("date", json.optString("ts", ""))))
                    put("timestamp", parseTimestamp(json.optString("ts", json.optString("date", ""))))
                    put("note", json.optString("note", json.optString("text", "")).ifEmpty { null })
                })
                summary.vitalsMigratedToPlugin++
            } catch (e: Exception) {
                summary.errorLines++
            }
        }

        if (vitalsList.length() > 0) {
            val existing = repo.getPluginValue(pluginId, "logs")
            val combined = if (existing != null) {
                val arr = JSONArray(existing)
                for (i in 0 until vitalsList.length()) {
                    arr.put(vitalsList.getJSONObject(i))
                }
                arr
            } else {
                vitalsList
            }
            repo.setPluginValue(pluginId, "logs", combined.toString())
        }
    }

    private fun parseTimestamp(raw: String): Long {
        if (raw.isBlank()) return System.currentTimeMillis()
        return try {
            raw.toLong()
        } catch (e: Exception) {
            try {
                OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                try {
                    LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                } catch (e3: Exception) {
                    System.currentTimeMillis()
                }
            }
        }
    }

    private fun parseDate(raw: String): String {
        if (raw.isBlank()) return LocalDate.now().toString()
        if (raw.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) return raw
        return try {
            OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate().toString()
        } catch (e: Exception) {
            try {
                Instant.ofEpochMilli(raw.toLong()).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
            } catch (e2: Exception) {
                LocalDate.now().toString()
            }
        }
    }

    /**
     * Reads the pre-bundled legacy dataset from app assets for in-app review and editing.
     */
    fun getBundledData(): String {
        return try {
            context.assets.open("v3/legacy_bundle.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load legacy_bundle.json: ${e.message}", e)
            "{}"
        }
    }

    /**
     * Commits user-reviewed and approved legacy records into the V3 operational SQLite database.
     * Inserts into target domain tables and double-writes to Sanctuary Ledger events table.
     */
    fun commitCuratedRecords(bundleObj: JSONObject): JSONObject {
        val result = JSONObject()
        var totalCommitted = 0
        var sessionsCount = 0
        var tasksCount = 0
        var runningCount = 0
        var habitsCount = 0
        var thoughtsCount = 0
        var milestonesCount = 0
        var vitalsCount = 0
        var recycleBinCount = 0
        var screentimeCount = 0
        var sharesCount = 0
        var errorCount = 0

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Sessions
            if (bundleObj.has("sessions")) {
                val sessionsArr = bundleObj.getJSONArray("sessions")
                for (i in 0 until sessionsArr.length()) {
                    try {
                        val item = sessionsArr.getJSONObject(i)
                        if (!item.optBoolean("selected", true)) continue

                        val id = item.optString("id", "")
                        val activity = item.optString("activity", "Focus").trim().ifEmpty { "Focus" }
                        val startTime = item.optLong("start_time", System.currentTimeMillis())
                        val endTime = if (item.has("end_time") && !item.isNull("end_time")) item.optLong("end_time") else null
                        val durationSec = item.optLong("duration_sec", 0L)
                        val notes = item.optString("notes", "").trim().ifEmpty { null }

                        repo.insertSession(
                            V3Session(
                                id = id,
                                activity = activity,
                                startTime = startTime,
                                endTime = endTime,
                                durationSec = durationSec,
                                notes = notes
                            )
                        )
                        val sDate = parseDate(startTime.toString())
                        repo.insertEvent(
                            V3Event(
                                id = id,
                                category = "session",
                                date = sDate,
                                ts = sDate,
                                timestamp = startTime,
                                title = activity,
                                content = notes ?: "${durationSec / 60}m focus session",
                                score = 0,
                                metadataJson = item.toString()
                            )
                        )
                        sessionsCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }

            // 2. Tasks
            if (bundleObj.has("tasks")) {
                val tasksArr = bundleObj.getJSONArray("tasks")
                for (i in 0 until tasksArr.length()) {
                    try {
                        val item = tasksArr.getJSONObject(i)
                        if (!item.optBoolean("selected", true)) continue

                        val id = item.optString("id", "")
                        val title = item.optString("title", "").trim()
                        if (title.isBlank()) continue
                        val priority = item.optString("priority", "MEDIUM").uppercase()
                        val status = item.optString("status", "pending")
                        val createdAt = item.optLong("created_at", System.currentTimeMillis())
                        val deadline = item.optString("deadline", "").trim().ifEmpty { null }

                        repo.insertTask(
                            V3Task(
                                id = id,
                                title = title,
                                priority = priority,
                                status = status,
                                createdAt = createdAt,
                                deadline = deadline
                            )
                        )
                        val tDate = parseDate(createdAt.toString())
                        repo.insertEvent(
                            V3Event(
                                id = id,
                                category = "task",
                                date = tDate,
                                ts = tDate,
                                timestamp = createdAt,
                                title = title,
                                content = "Priority: $priority • Status: $status${if (!deadline.isNullOrBlank()) " • Deadline: $deadline" else ""}",
                                score = 0,
                                metadataJson = item.toString()
                            )
                        )
                        tasksCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }

            // 3. Thoughts
            if (bundleObj.has("thoughts")) {
                val thoughtsArr = bundleObj.getJSONArray("thoughts")
                for (i in 0 until thoughtsArr.length()) {
                    try {
                        val item = thoughtsArr.getJSONObject(i)
                        if (!item.optBoolean("selected", true)) continue

                        val id = item.optString("id", "")
                        val content = (if (item.has("content")) item.optString("content") else item.optString("text", "")).trim()
                        if (content.isBlank()) continue
                        val topic = (if (item.has("topic")) item.optString("topic") else item.optString("title", "Thought")).trim().ifEmpty { "Thought" }
                        val date = item.optString("date", "")
                        val ts = item.optLong("timestamp", System.currentTimeMillis())
                        val pinned = item.optBoolean("pinned", false)
                        val thDate = date.ifEmpty { parseDate(ts.toString()) }

                        repo.insertThought(
                            V3Thought(
                                id = id,
                                timestamp = ts,
                                date = thDate,
                                topic = topic,
                                content = content,
                                pinned = pinned
                            )
                        )
                        repo.insertEvent(
                            V3Event(
                                id = id,
                                category = "thought",
                                date = thDate,
                                ts = thDate,
                                timestamp = ts,
                                title = if (!topic.isNullOrBlank()) topic else "Thought",
                                content = content,
                                score = 0,
                                metadataJson = item.toString()
                            )
                        )
                        thoughtsCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }

            // 4. Milestones (Day Tracker)
            if (bundleObj.has("milestones")) {
                val msArr = bundleObj.getJSONArray("milestones")
                for (i in 0 until msArr.length()) {
                    try {
                        val item = msArr.getJSONObject(i)
                        if (!item.optBoolean("selected", true)) continue

                        val id = item.optString("id", "")
                        val title = item.optString("title", "").trim()
                        val targetDate = item.optString("target_date", "").trim()
                        if (title.isBlank() || targetDate.isBlank()) continue
                        val hour = item.optInt("hour", 0)
                        val pinned = item.optBoolean("pinned", false)
                        val createdAt = item.optLong("created_at", System.currentTimeMillis())

                        repo.insertDayTrackerItem(
                            V3DayTrackerItem(
                                id = id,
                                title = title,
                                targetDate = targetDate,
                                hour = hour,
                                pinned = pinned,
                                createdAt = createdAt
                            )
                        )
                        val timeStr = if (hour in 0..23) String.format(Locale.getDefault(), "%02d:00", hour) else "All Day"
                        repo.insertEvent(
                            V3Event(
                                id = id,
                                category = "milestone",
                                date = targetDate,
                                ts = targetDate,
                                timestamp = createdAt,
                                title = "🎯 $title",
                                content = "Target Date: $targetDate • $timeStr",
                                score = 0,
                                metadataJson = item.toString()
                            )
                        )
                        milestonesCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }

            // 5. Running -> plugin_storage + events
            if (bundleObj.has("running")) {
                val runsArr = bundleObj.getJSONArray("running")
                val pluginRuns = JSONArray()
                for (i in 0 until runsArr.length()) {
                    try {
                        val item = runsArr.getJSONObject(i)
                        if (!item.optBoolean("selected", true)) continue

                        val id = item.optString("id", "")
                        val date = item.optString("date", "")
                        val ts = item.optLong("timestamp", System.currentTimeMillis())
                        val dist = item.optDouble("distanceKm", item.optDouble("distance_km", 0.0))
                        val durSec = item.optLong("durationSec", item.optLong("duration_sec", 0L))
                        val paceSec = item.optLong("pace_sec_km", 0L)
                        val paceFormatted = item.optString("pace", item.optString("pace_formatted", ""))
                            .ifEmpty {
                                if (dist > 0 && durSec > 0) {
                                    val pSec = durSec / dist
                                    val pMin = (pSec / 60).toLong()
                                    val pS = (pSec % 60).toLong().toString().padStart(2, '0')
                                    "$pMin'$pS\"/km"
                                } else "--'--\"/km"
                            }
                        val note = item.optString("note", item.optString("notes", "")).trim().ifEmpty { null }
                        val title = item.optString("title", "").trim().ifEmpty { "${dist} km Run" }
                        val runType = item.optString("run_type", item.optString("runType", "easy"))

                        pluginRuns.put(JSONObject().apply {
                            put("id", id)
                            put("date", date)
                            put("timestamp", ts)
                            put("distanceKm", dist)
                            put("distance_km", dist)
                            put("durationSec", durSec)
                            put("duration_sec", durSec)
                            put("pace", paceFormatted)
                            put("pace_formatted", paceFormatted)
                            put("pace_sec_km", paceSec)
                            put("title", title)
                            put("note", note)
                            put("notes", note)
                            put("run_type", runType)
                        })

                        val metaJson = JSONObject().apply {
                            put("distanceKm", dist)
                            put("distance_km", dist)
                            put("durationSec", durSec)
                            put("duration_sec", durSec)
                            put("pace", paceFormatted)
                            put("pace_formatted", paceFormatted)
                            put("title", title)
                            put("run_type", runType)
                            note?.let {
                                put("note", it)
                                put("notes", it)
                            }
                        }.toString()

                        repo.insertEvent(
                            V3Event(
                                id = id,
                                category = "running",
                                date = date,
                                ts = date,
                                timestamp = ts,
                                title = title,
                                content = note ?: "${dist} km run in ${durSec / 60}m",
                                score = 0,
                                metadataJson = metaJson
                            )
                        )
                        runningCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
                if (pluginRuns.length() > 0) {
                    val existing = repo.getPluginValue("com.v07.running", "history")
                    val merged = if (existing != null) {
                        val exArr = JSONArray(existing)
                        val seen = mutableSetOf<String>()
                        for (k in 0 until exArr.length()) {
                            seen.add(exArr.getJSONObject(k).optString("id"))
                        }
                        for (k in 0 until pluginRuns.length()) {
                            val r = pluginRuns.getJSONObject(k)
                            if (!seen.contains(r.optString("id"))) {
                                exArr.put(r)
                            }
                        }
                        exArr
                    } else {
                        pluginRuns
                    }
                    repo.setPluginValue("com.v07.running", "history", merged.toString())
                }
            }

            // 6. Habits -> definitions & logs
            if (bundleObj.has("habits")) {
                val habitsObj = bundleObj.getJSONObject("habits")
                val defsNameMap = mutableMapOf<String, String>()
                if (habitsObj.has("definitions")) {
                    val defsArr = habitsObj.getJSONArray("definitions")
                    val selectedDefs = JSONArray()
                    for (i in 0 until defsArr.length()) {
                        val d = defsArr.getJSONObject(i)
                        if (d.optBoolean("selected", true)) {
                            selectedDefs.put(d)
                            val hName = d.optString("name", d.optString("title", "Habit")).trim()
                            val hId = d.optString("id", UUID.randomUUID().toString())
                            val hCadence = d.optString("cadence", d.optString("target_freq", "daily"))
                            val hIcon = d.optString("icon", d.optString("emoji", "🔥"))
                            defsNameMap[hId] = "$hIcon $hName"
                            repo.insertEvent(
                                V3Event(
                                    id = "hbt_def_$hId",
                                    category = "habit",
                                    date = LocalDate.now().toString(),
                                    ts = LocalDate.now().toString(),
                                    timestamp = System.currentTimeMillis(),
                                    title = "Gentle Habit: $hIcon $hName",
                                    content = "Cadence: $hCadence",
                                    score = 0,
                                    metadataJson = d.toString()
                                )
                            )
                            habitsCount++
                            totalCommitted++
                        }
                    }
                    if (selectedDefs.length() > 0) {
                        repo.setPluginValue("com.v07.habits", "definitions", selectedDefs.toString())
                    }
                }
                if (habitsObj.has("logs")) {
                    val logsArr = habitsObj.getJSONArray("logs")
                    if (logsArr.length() > 0) {
                        repo.setPluginValue("com.v07.habits", "logs", logsArr.toString())
                        for (i in 0 until logsArr.length()) {
                            val l = logsArr.getJSONObject(i)
                            val date = l.optString("date", "")
                            val hid = l.optString("habit_id", i.toString())
                            val habitName = l.optString("name", l.optString("habit_name", defsNameMap[hid] ?: "Habit Completed"))
                            val value = l.optString("value", "Done")
                            val ts = parseTimestamp(date)
                            repo.insertEvent(
                                V3Event(
                                    id = "hbt_log_${date}_$hid",
                                    category = "habit",
                                    date = date,
                                    ts = date,
                                    timestamp = ts,
                                    title = "🌱 $habitName",
                                    content = value,
                                    score = 0,
                                    metadataJson = l.toString()
                                )
                            )
                        }
                    }
                }
            }

            // 7. Vitals (Mood & Energy) -> plugin_storage + events
            if (bundleObj.has("vitals")) {
                val vitalsArr = bundleObj.getJSONArray("vitals")
                val pluginVitals = JSONArray()
                for (i in 0 until vitalsArr.length()) {
                    try {
                        val item = vitalsArr.getJSONObject(i)
                        if (!item.optBoolean("selected", true)) continue

                        val id = item.optString("id", "")
                        val type = item.optString("type", "mood")
                        val score = item.optDouble("score", 3.0)
                        val date = item.optString("date", "")
                        val ts = item.optLong("timestamp", System.currentTimeMillis())
                        val note = item.optString("note", "").trim().ifEmpty { null }

                        pluginVitals.put(JSONObject().apply {
                            put("id", id)
                            put("type", type)
                            put("score", score)
                            put("date", date)
                            put("timestamp", ts)
                            put("note", note)
                        })

                        repo.insertEvent(
                            V3Event(
                                id = id,
                                category = type,
                                date = date,
                                ts = date,
                                timestamp = ts,
                                title = "${type.replaceFirstChar { c -> c.uppercase() }} Check-in",
                                content = note ?: "Score: $score",
                                score = score.toInt(),
                                metadataJson = JSONObject().put("score", score).put("type", type).toString()
                            )
                        )
                        vitalsCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
                if (pluginVitals.length() > 0) {
                    val existing = repo.getPluginValue("com.v07.vitals", "logs")
                    val merged = if (existing != null) {
                        val exArr = JSONArray(existing)
                        for (k in 0 until pluginVitals.length()) {
                            exArr.put(pluginVitals.getJSONObject(k))
                        }
                        exArr
                    } else {
                        pluginVitals
                    }
                    repo.setPluginValue("com.v07.vitals", "logs", merged.toString())
                }
            }

            // 8. Screen Time -> app_usage table + events table
            if (bundleObj.has("screentime")) {
                val stArr = bundleObj.getJSONArray("screentime")
                for (i in 0 until stArr.length()) {
                    try {
                        val dayObj = stArr.getJSONObject(i)
                        if (!dayObj.optBoolean("selected", true)) continue
                        val apps = dayObj.optJSONArray("all_apps") ?: dayObj.optJSONArray("top_apps")
                        val dateStr = dayObj.optString("date", "")
                        val ts = parseTimestamp(dateStr)
                        val totalSec = dayObj.optLong("total_duration_sec", 0L)
                        val formattedDuration = dayObj.optString("formatted_duration", "${totalSec / 60}m")
                        val appCount = dayObj.optInt("app_count", apps?.length() ?: 0)

                        if (apps != null && apps.length() > 0) {
                            for (j in 0 until apps.length()) {
                                val a = apps.getJSONObject(j)
                                val pkg = a.optString("package_name", "unknown")
                                val name = a.optString("app_name", "Unknown")
                                val dur = a.optLong("duration_ms", 0L)
                                val cat = a.optString("category", "Productivity")

                                val values = android.content.ContentValues().apply {
                                    put("id", "${dateStr}_$pkg")
                                    put("package_name", pkg)
                                    put("app_name", name)
                                    put("start_time", ts)
                                    put("end_time", ts + dur)
                                    put("duration_ms", dur)
                                    put("category", cat)
                                }
                                db.insertWithOnConflict("app_usage", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                            }
                        }

                        // Also index as an Event so Data Center and Universal Search index Digital Stillness
                        val topAppObj = if (apps != null && apps.length() > 0) apps.getJSONObject(0) else null
                        val topAppName = topAppObj?.optString("app_name", "") ?: ""
                        val topAppDur = topAppObj?.optString("formatted", "") ?: ""
                        val sub = if (topAppName.isNotBlank()) "$appCount apps • Top: $topAppName ($topAppDur)" else "$appCount apps used"

                        repo.insertEvent(
                            V3Event(
                                id = "st_$dateStr",
                                category = "screentime",
                                date = dateStr,
                                ts = dateStr,
                                timestamp = ts,
                                title = "Digital Stillness · $formattedDuration",
                                content = sub,
                                score = 0,
                                metadataJson = dayObj.toString()
                            )
                        )

                        screentimeCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }

            // 9. Recycle Bin
            if (bundleObj.has("recycle_bin")) {
                val rbArr = bundleObj.getJSONArray("recycle_bin")
                for (i in 0 until rbArr.length()) {
                    try {
                        val item = rbArr.getJSONObject(i)
                        if (!item.optBoolean("selected", false)) continue // default unselected
                        val origId = item.optString("original_id", "")
                        val cat = item.optString("category", "general")
                        val dataJson = item.optString("data_json", "{}")
                        if (origId.isNotBlank()) {
                            repo.recycleItem(origId, cat, dataJson)
                            recycleBinCount++
                            totalCommitted++
                        }
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }

            // 10. Shares
            if (bundleObj.has("shares")) {
                val sharesArr = bundleObj.getJSONArray("shares")
                for (i in 0 until sharesArr.length()) {
                    try {
                        val item = sharesArr.getJSONObject(i)
                        if (!item.optBoolean("selected", true)) continue
                        val id = item.optString("id", "")
                        val title = item.optString("title", "Saved Link")
                        val url = item.optString("url", "")
                        val ts = item.optLong("timestamp", System.currentTimeMillis())
                        val dStr = parseDate(ts.toString())

                        repo.insertEvent(
                            V3Event(
                                id = id,
                                category = "share",
                                date = dStr,
                                ts = dStr,
                                timestamp = ts,
                                title = title,
                                content = url,
                                score = 0,
                                metadataJson = JSONObject().put("url", url).toString()
                            )
                        )
                        sharesCount++
                        totalCommitted++
                    } catch (e: Exception) {
                        errorCount++
                    }
                }
            }

            db.setTransactionSuccessful()
            Log.i(TAG, "Legacy curated commit completed: total=$totalCommitted, errors=$errorCount")
        } catch (e: Exception) {
            Log.e(TAG, "Curated commit transaction failed: ${e.message}", e)
            errorCount++
        } finally {
            db.endTransaction()
        }

        result.apply {
            put("success", errorCount == 0 || totalCommitted > 0)
            put("totalCommitted", totalCommitted)
            put("sessionsCount", sessionsCount)
            put("tasksCount", tasksCount)
            put("runningCount", runningCount)
            put("habitsCount", habitsCount)
            put("thoughtsCount", thoughtsCount)
            put("milestonesCount", milestonesCount)
            put("vitalsCount", vitalsCount)
            put("screentimeCount", screentimeCount)
            put("recycleBinCount", recycleBinCount)
            put("sharesCount", sharesCount)
            put("errorCount", errorCount)
        }
        return result
    }

    /**
     * Legacy auto-seed disabled in V3. Clean launch with isolated multi-profile databases and zero mock data.
     */
    fun autoSeedIfEmpty(): Boolean {
        return false
    }
}
