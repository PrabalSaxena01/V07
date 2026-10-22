package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3DayTimeline
import com.v07.v3.core.data.V3Event
import com.v07.v3.core.data.V3TimelineEvent
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Dedicated Repository for Cadence, Life OS Events, Daily Timeline and Ledger Query Engine.
 */
class CadenceLedgerRepository(
    dbHelper: V07Database,
    private val recycleRepo: RecycleBinRepository
) : BaseRepository(dbHelper) {

    fun getDayTimeline(dateStr: String): V3DayTimeline {
        val db = dbHelper.readableDatabase
        val safeExclude = getSafeExcludeClause(db)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = try { sdf.parse(dateStr) } catch (_: Exception) { Date() } ?: Date()
        val cal = Calendar.getInstance().apply {
            time = dateObj
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMs = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endMs = cal.timeInMillis - 1

        val events = mutableListOf<V3TimelineEvent>()
        var totalFocusSec = 0L
        var sessionCount = 0
        var tasksCompletedCount = 0
        var thoughtsCount = 0
        var totalScreenTimeMs = 0L

        // 1. Sessions on this day
        try {
            val sc = db.query(
                "sessions", null,
                "start_time >= ? AND start_time <= ? AND $safeExclude",
                arrayOf(startMs.toString(), endMs.toString()),
                null, null, "start_time ASC"
            )
            sc.use {
                while (it.moveToNext()) {
                    sessionCount++
                    val id = it.getString(it.getColumnIndexOrThrow("id"))
                    val act = it.getString(it.getColumnIndexOrThrow("activity"))
                    val st = it.getLong(it.getColumnIndexOrThrow("start_time"))
                    val durSec = it.getLong(it.getColumnIndexOrThrow("duration_sec"))
                    val notes = it.getString(it.getColumnIndexOrThrow("notes"))
                    totalFocusSec += durSec

                    val min = durSec / 60
                    val sub = "${min}m focus${if (!notes.isNullOrBlank()) " • $notes" else ""}"
                    events.add(
                        V3TimelineEvent(
                            id = id,
                            timestamp = st,
                            timeFormatted = timeFormatter.format(Date(st)),
                            category = "sessions",
                            title = act,
                            subtitle = sub,
                            durationSec = durSec,
                            isHighlight = true
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 2. Tasks completed on this day
        try {
            val tc = db.query(
                "tasks", null,
                "completed_at >= ? AND completed_at <= ? AND $safeExclude",
                arrayOf(startMs.toString(), endMs.toString()),
                null, null, "completed_at ASC"
            )
            tc.use {
                while (it.moveToNext()) {
                    tasksCompletedCount++
                    val id = it.getString(it.getColumnIndexOrThrow("id"))
                    val title = it.getString(it.getColumnIndexOrThrow("title"))
                    val compAt = it.getLong(it.getColumnIndexOrThrow("completed_at"))
                    val prio = it.getString(it.getColumnIndexOrThrow("priority"))

                    events.add(
                        V3TimelineEvent(
                            id = id,
                            timestamp = compAt,
                            timeFormatted = timeFormatter.format(Date(compAt)),
                            category = "tasks",
                            title = "✓ $title",
                            subtitle = "Priority: $prio",
                            isHighlight = false
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 3. Thoughts captured on this day
        try {
            val thc = db.query(
                "thoughts", null,
                "(date = ? OR (timestamp >= ? AND timestamp <= ?)) AND $safeExclude",
                arrayOf(dateStr, startMs.toString(), endMs.toString()),
                null, null, "timestamp ASC"
            )
            thc.use {
                while (it.moveToNext()) {
                    thoughtsCount++
                    val id = it.getString(it.getColumnIndexOrThrow("id"))
                    val topic = it.getString(it.getColumnIndexOrThrow("topic"))
                    val content = it.getString(it.getColumnIndexOrThrow("content"))
                    val ts = it.getLong(it.getColumnIndexOrThrow("timestamp"))

                    events.add(
                        V3TimelineEvent(
                            id = id,
                            timestamp = ts,
                            timeFormatted = timeFormatter.format(Date(ts)),
                            category = "thoughts",
                            title = "💭 [$topic] ${content.take(60)}",
                            subtitle = content,
                            isHighlight = false
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 4. Screen time telemetry for this day
        try {
            val uc = db.query(
                "app_usage", null,
                "id LIKE ? OR (start_time >= ? AND start_time <= ?)",
                arrayOf("${dateStr}%", startMs.toString(), endMs.toString()),
                null, null, "duration_ms DESC"
            )
            uc.use {
                while (it.moveToNext()) {
                    val dur = it.getLong(it.getColumnIndexOrThrow("duration_ms"))
                    if (dur in 1..86400000L) {
                        totalScreenTimeMs += dur
                    }
                }
            }
            if (totalScreenTimeMs == 0L) {
                val stEv = getEventById("st_$dateStr")
                if (stEv != null && !stEv.metadataJson.isNullOrBlank()) {
                    val meta = JSONObject(stEv.metadataJson)
                    val sec = meta.optLong("total_duration_sec", 0L)
                    if (sec in 1..86400L) {
                        totalScreenTimeMs = sec * 1000L
                    }
                }
            }
            totalScreenTimeMs = totalScreenTimeMs.coerceAtMost(86400000L)
        } catch (_: Exception) {}

        // 5. Day tracker milestones targeted for this date
        try {
            val dtc = db.query(
                "day_tracker", null,
                "target_date = ? AND $safeExclude",
                arrayOf(dateStr),
                null, null, "hour ASC, created_at ASC"
            )
            dtc.use {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndexOrThrow("id"))
                    val title = it.getString(it.getColumnIndexOrThrow("title"))
                    val hour = it.getInt(it.getColumnIndexOrThrow("hour"))
                    val timeStr = if (hour in 0..23) String.format(Locale.getDefault(), "%02d:00", hour) else "All Day"
                    events.add(
                        V3TimelineEvent(
                            id = id,
                            timestamp = startMs + (hour.coerceIn(0, 23) * 3600000L),
                            timeFormatted = timeStr,
                            category = "daytracker",
                            title = "🎯 $title",
                            subtitle = "Milestone Target",
                            isHighlight = true
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 6. Core Cadence & Life OS Events (Check-Ins, Check-Outs, Wins, Lessons) on this date
        try {
            val evc = db.query(
                "events", null,
                "date = ? AND $safeExclude",
                arrayOf(dateStr),
                null, null, "timestamp ASC"
            )
            evc.use {
                val existingIds = events.map { e -> e.id }.toSet()
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndexOrThrow("id"))
                    if (existingIds.contains(id)) continue
                    val cat = it.getString(it.getColumnIndexOrThrow("category"))
                    val title = it.getString(it.getColumnIndexOrThrow("title")) ?: cat.replaceFirstChar { c -> c.uppercase() }
                    val content = it.getString(it.getColumnIndexOrThrow("content")) ?: ""
                    val ts = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                    val score = it.getInt(it.getColumnIndexOrThrow("score"))

                    val icon = when (cat) {
                        "checkin" -> "🌅"
                        "checkout" -> "🌙"
                        "win" -> "🏆"
                        "lesson" -> "💡"
                        "pushups" -> "💪"
                        "habit" -> "🌱"
                        "running", "run" -> "🏃"
                        else -> "✨"
                    }

                    events.add(
                        V3TimelineEvent(
                            id = id,
                            timestamp = ts,
                            timeFormatted = timeFormatter.format(Date(ts)),
                            category = cat,
                            title = "$icon $title",
                            subtitle = if (content.isNotBlank()) content.take(100) else if (score > 0) "Score: $score/5" else "",
                            isHighlight = cat in listOf("checkin", "checkout", "win")
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        val distinctEvents = events.distinctBy { it.id }.sortedBy { it.timestamp }

        return V3DayTimeline(
            date = dateStr,
            totalFocusSec = totalFocusSec,
            sessionCount = sessionCount,
            tasksCompletedCount = tasksCompletedCount,
            thoughtsCount = thoughtsCount,
            totalScreenTimeMs = totalScreenTimeMs,
            events = distinctEvents
        )
    }

    fun insertEvent(event: V3Event): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", event.id.ifEmpty { UUID.randomUUID().toString() })
            put("category", event.category)
            put("date", event.date)
            put("ts", event.ts)
            put("timestamp", event.timestamp)
            put("title", event.title)
            put("content", event.content)
            put("score", event.score)
            put("metadata_json", event.metadataJson)
        }
        return db.insertWithOnConflict("events", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun deleteEvent(id: String): Boolean {
        return deleteEntityEverywhere(id)
    }

    fun deleteEntityEverywhere(id: String, category: String? = null): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            val r1 = db.delete("events", "id = ?", arrayOf(id))
            val r2 = db.delete("sessions", "id = ?", arrayOf(id))
            val r3 = db.delete("thoughts", "id = ?", arrayOf(id))
            val r4 = db.delete("tasks", "id = ?", arrayOf(id))
            val r5 = db.delete("day_tracker", "id = ?", arrayOf(id))
            val r6 = db.delete("app_usage", "id = ?", arrayOf(id))
            val r7 = db.delete("plugin_events", "id = ?", arrayOf(id))
            val tombstoneValues = ContentValues().apply {
                put("id", id)
                put("category", category ?: "general")
                put("deleted_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
            r1 > 0 || r2 > 0 || r3 > 0 || r4 > 0 || r5 > 0 || r6 > 0 || r7 > 0
        } finally {
            db.endTransaction()
        }
    }

    fun batchDeleteEntities(ids: List<String>): Int {
        if (ids.isEmpty()) return 0
        val db = dbHelper.writableDatabase
        var count = 0
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            for (id in ids) {
                if (id.isBlank()) continue
                val r1 = db.delete("events", "id = ?", arrayOf(id))
                val r2 = db.delete("sessions", "id = ?", arrayOf(id))
                val r3 = db.delete("thoughts", "id = ?", arrayOf(id))
                val r4 = db.delete("tasks", "id = ?", arrayOf(id))
                val r5 = db.delete("day_tracker", "id = ?", arrayOf(id))
                val r6 = db.delete("app_usage", "id = ?", arrayOf(id))
                val r7 = db.delete("plugin_events", "id = ?", arrayOf(id))
                val tombstoneValues = ContentValues().apply {
                    put("id", id)
                    put("category", "deleted")
                    put("deleted_at", now)
                }
                db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, SQLiteDatabase.CONFLICT_REPLACE)
                if (r1 > 0 || r2 > 0 || r3 > 0 || r4 > 0 || r5 > 0 || r6 > 0 || r7 > 0) {
                    count++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return count
    }

    fun batchRecycleEntities(items: List<Triple<String, String, String>>): Int {
        if (items.isEmpty()) return 0
        var count = 0
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for ((originalId, category, dataJson) in items) {
                if (originalId.isBlank()) continue
                if (recycleRepo.recycleItem(originalId, category, dataJson)) {
                    count++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return count
    }

    fun updateEvent(
        id: String,
        title: String? = null,
        content: String? = null,
        category: String? = null,
        metadataJson: String? = null
    ): Boolean {
        val db = dbHelper.writableDatabase
        var updated = false

        val evVals = ContentValues().apply {
            title?.let { put("title", it) }
            content?.let { put("content", it) }
            category?.let { put("category", it) }
            metadataJson?.let { put("metadata_json", it) }
        }
        if (evVals.size() > 0) {
            val rows = db.update("events", evVals, "id = ?", arrayOf(id))
            if (rows > 0) updated = true
        }

        var metaObj: JSONObject? = null
        if (!metadataJson.isNullOrBlank()) {
            try {
                metaObj = JSONObject(metadataJson)
            } catch (_: Exception) {}
        }

        // 1. thoughts
        val thVals = ContentValues().apply {
            content?.let { put("content", it) }
            title?.let { put("topic", it) }
            metaObj?.let { m ->
                if (m.has("pinned")) put("pinned", if (m.optBoolean("pinned")) 1 else 0)
                if (m.has("tags")) put("tags", m.optString("tags"))
            }
        }
        if (thVals.size() > 0) {
            val rows = db.update("thoughts", thVals, "id = ?", arrayOf(id))
            if (rows > 0) updated = true
        }

        // 2. tasks
        val tVals = ContentValues().apply {
            title?.let { put("title", it) }
            metaObj?.let { m ->
                if (m.has("priority")) put("priority", m.optString("priority").uppercase())
                if (m.has("status")) put("status", m.optString("status").lowercase())
                if (m.has("deadline")) {
                    val dl = m.optString("deadline")
                    if (dl.isBlank()) putNull("deadline") else put("deadline", dl)
                }
            }
        }
        if (tVals.size() > 0) {
            val rows = db.update("tasks", tVals, "id = ?", arrayOf(id))
            if (rows > 0) updated = true
        }

        // 3. sessions
        val sVals = ContentValues().apply {
            title?.let { put("activity", it) }
            content?.let { put("notes", it) }
            metaObj?.let { m ->
                if (m.has("durationSec")) put("duration_sec", m.optLong("durationSec"))
                else if (m.has("duration_sec")) put("duration_sec", m.optLong("duration_sec"))
            }
        }
        if (sVals.size() > 0) {
            val rows = db.update("sessions", sVals, "id = ?", arrayOf(id))
            if (rows > 0) updated = true
        }

        // 4. day_tracker (milestones)
        val dVals = ContentValues().apply {
            title?.let { put("title", it) }
            metaObj?.let { m ->
                if (m.has("target_date")) put("target_date", m.optString("target_date"))
                else if (m.has("targetDate")) put("target_date", m.optString("targetDate"))
                if (m.has("hour")) put("hour", m.optInt("hour"))
                if (m.has("pinned")) put("pinned", if (m.optBoolean("pinned")) 1 else 0)
            }
        }
        if (dVals.size() > 0) {
            val rows = db.update("day_tracker", dVals, "id = ?", arrayOf(id))
            if (rows > 0) updated = true
        }

        return updated
    }

    fun getEvents(category: String? = null, date: String? = null, limit: Int = 100): List<V3Event> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3Event>()
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        whereClauses.add(getSafeExcludeClause(db))

        if (!category.isNullOrBlank() && category != "all") {
            whereClauses.add("category = ?")
            args.add(category)
        }
        if (!date.isNullOrBlank()) {
            whereClauses.add("date = ?")
            args.add(date)
        }

        val where = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else null
        val cursor = db.query("events", null, where, args.toTypedArray(), null, null, "timestamp DESC", limit.toString())
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3Event(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        date = it.getString(it.getColumnIndexOrThrow("date")),
                        ts = it.getString(it.getColumnIndexOrThrow("ts")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        content = it.getString(it.getColumnIndexOrThrow("content")),
                        score = it.getInt(it.getColumnIndexOrThrow("score")),
                        metadataJson = it.getString(it.getColumnIndexOrThrow("metadata_json"))
                    )
                )
            }
        }
        return list
    }

    fun getEventById(id: String): V3Event? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("events", null, "id = ?", arrayOf(id), null, null, null, "1")
        cursor.use {
            if (it.moveToNext()) {
                return V3Event(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    category = it.getString(it.getColumnIndexOrThrow("category")),
                    date = it.getString(it.getColumnIndexOrThrow("date")),
                    ts = it.getString(it.getColumnIndexOrThrow("ts")),
                    timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    content = it.getString(it.getColumnIndexOrThrow("content")),
                    score = it.getInt(it.getColumnIndexOrThrow("score")),
                    metadataJson = it.getString(it.getColumnIndexOrThrow("metadata_json"))
                )
            }
        }
        return null
    }

    fun getAllEventsForSync(): List<V3Event> {
        return getEvents(category = null, date = null, limit = 500)
    }

    fun queryLedgerEntries(
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        query: String? = null,
        limit: Int = 200
    ): List<V3Event> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3Event>()
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        whereClauses.add(getSafeExcludeClause(db))

        val catFilter = category?.trim()?.lowercase()
        if (!catFilter.isNullOrBlank() && catFilter != "all") {
            if (catFilter == "quick") {
                whereClauses.add("(category = 'quick' OR category = 'thought')")
            } else {
                whereClauses.add("LOWER(category) = ?")
                args.add(catFilter)
            }
        }

        if (!startDate.isNullOrBlank() && !endDate.isNullOrBlank()) {
            whereClauses.add("date >= ? AND date <= ?")
            args.add(startDate)
            args.add(endDate)
        } else if (!startDate.isNullOrBlank()) {
            whereClauses.add("date >= ?")
            args.add(startDate)
        } else if (!endDate.isNullOrBlank()) {
            whereClauses.add("date <= ?")
            args.add(endDate)
        }

        if (!query.isNullOrBlank()) {
            val q = "%${query.trim().lowercase()}%"
            whereClauses.add("(LOWER(title) LIKE ? OR LOWER(content) LIKE ? OR LOWER(category) LIKE ?)")
            args.add(q)
            args.add(q)
            args.add(q)
        }

        val where = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else null
        val cursor = db.query(
            "events",
            null,
            where,
            if (args.isNotEmpty()) args.toTypedArray() else null,
            null,
            null,
            "timestamp DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3Event(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        date = it.getString(it.getColumnIndexOrThrow("date")),
                        ts = it.getString(it.getColumnIndexOrThrow("ts")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        content = it.getString(it.getColumnIndexOrThrow("content")),
                        score = it.getInt(it.getColumnIndexOrThrow("score")),
                        metadataJson = it.getString(it.getColumnIndexOrThrow("metadata_json"))
                    )
                )
            }
        }

        // Fallback for thoughts table if catFilter is 'quick' or 'all' to guarantee legacy thoughts also show
        if (catFilter.isNullOrBlank() || catFilter == "all" || catFilter == "quick") {
            try {
                val existingIds = list.map { it.id }.toSet()
                val thoughtCursor = db.query("thoughts", null, null, null, null, null, "timestamp DESC", "100")
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                thoughtCursor.use {
                    while (it.moveToNext()) {
                        val tid = it.getString(it.getColumnIndexOrThrow("id"))
                        if (!existingIds.contains(tid)) {
                            val ts = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                            val d = it.getString(it.getColumnIndexOrThrow("date")) ?: sdf.format(Date(ts))
                            val top = it.getString(it.getColumnIndexOrThrow("topic")) ?: "Quick Capture"
                            val cnt = it.getString(it.getColumnIndexOrThrow("content")) ?: ""
                            val tags = it.getString(it.getColumnIndexOrThrow("tags"))

                            var ok = true
                            if (!startDate.isNullOrBlank() && d < startDate) ok = false
                            if (!endDate.isNullOrBlank() && d > endDate) ok = false
                            if (!query.isNullOrBlank()) {
                                val q = query.trim().lowercase()
                                if (!top.lowercase().contains(q) && !cnt.lowercase().contains(q) && (tags == null || !tags.lowercase().contains(q))) {
                                    ok = false
                                }
                            }
                            if (ok) {
                                list.add(
                                    V3Event(
                                        id = tid,
                                        category = "quick",
                                        date = d,
                                        ts = isoFmt.format(Date(ts)),
                                        timestamp = ts,
                                        title = top,
                                        content = cnt,
                                        score = 0,
                                        metadataJson = if (tags != null) "{\"tags\":\"$tags\"}" else "{}"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
            list.sortByDescending { it.timestamp }
        }

        return list.take(limit)
    }
}
