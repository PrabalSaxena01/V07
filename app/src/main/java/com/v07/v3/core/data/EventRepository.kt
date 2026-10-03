package com.v07.v3.core.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Authoritative Repository for the 4 Core V3 Modules + Plugin Platform Storage.
 * Executes against V07Database using thread-safe SQLite primitives.
 */
class EventRepository private constructor(private val appContext: Context) {

    private val dbHelper: V07Database
        get() = V07Database.getInstance(appContext)

    init {
        try {
            val db = dbHelper.writableDatabase
            V07Database.ensureAllTables(db)
        } catch (e: Exception) {
            Log.w("EventRepository", "Failed to ensure schema at repository init: ${e.message}")
        }
    }

    fun getSafeExcludeClause(db: android.database.sqlite.SQLiteDatabase? = null): String {
        val targetDb = db ?: try { dbHelper.readableDatabase } catch (_: Exception) { null }
        if (targetDb == null) return SAFE_EXCLUDE_CLAUSE
        return try {
            targetDb.rawQuery("SELECT 1 FROM deleted_tombstones LIMIT 0", null).use { }
            SAFE_EXCLUDE_CLAUSE
        } catch (_: Exception) {
            try {
                V07Database.ensureAllTables(targetDb)
                SAFE_EXCLUDE_CLAUSE
            } catch (_: Exception) {
                "id NOT IN (SELECT original_id FROM recycle_bin WHERE original_id IS NOT NULL AND original_id != '')"
            }
        }
    }

    companion object {
        const val SAFE_EXCLUDE_CLAUSE = "id NOT IN (SELECT original_id FROM recycle_bin WHERE original_id IS NOT NULL AND original_id != '') AND id NOT IN (SELECT id FROM deleted_tombstones WHERE id IS NOT NULL AND id != '')"

        @Volatile
        private var instance: EventRepository? = null

        fun getInstance(context: Context): EventRepository {
            return instance ?: synchronized(this) {
                instance ?: EventRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    // ─── CORE MODULE 1: SESSION TIMER ────────────────────────────────────────

    fun insertSession(session: V3Session): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", session.id.ifEmpty { UUID.randomUUID().toString() })
            put("activity", session.activity)
            put("start_time", session.startTime)
            session.endTime?.let { put("end_time", it) }
            put("duration_sec", session.durationSec)
            session.notes?.let { put("notes", it) }
        }
        return db.insertWithOnConflict("sessions", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getActiveSession(): V3Session? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "sessions",
            null,
            "end_time IS NULL",
            null,
            null,
            null,
            "start_time DESC",
            "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return V3Session(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    activity = it.getString(it.getColumnIndexOrThrow("activity")),
                    startTime = it.getLong(it.getColumnIndexOrThrow("start_time")),
                    endTime = null,
                    durationSec = it.getLong(it.getColumnIndexOrThrow("duration_sec")),
                    notes = it.getString(it.getColumnIndexOrThrow("notes"))
                )
            }
        }
        return null
    }

    fun stopActiveSession(endTime: Long, notes: String? = null): Boolean {
        val active = getActiveSession() ?: return false
        val durationSec = ((endTime - active.startTime) / 1000).coerceAtLeast(0)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("end_time", endTime)
            put("duration_sec", durationSec)
            notes?.let { put("notes", it) }
        }
        return db.update("sessions", values, "id = ?", arrayOf(active.id)) > 0
    }

    fun getTodaySessions(todayStartMs: Long): List<V3Session> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3Session>()
        val safeExclude = getSafeExcludeClause(db)
        val cursor = db.query(
            "sessions",
            null,
            "start_time >= ? AND $safeExclude",
            arrayOf(todayStartMs.toString()),
            null,
            null,
            "start_time DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3Session(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        activity = it.getString(it.getColumnIndexOrThrow("activity")),
                        startTime = it.getLong(it.getColumnIndexOrThrow("start_time")),
                        endTime = if (it.isNull(it.getColumnIndexOrThrow("end_time"))) null else it.getLong(it.getColumnIndexOrThrow("end_time")),
                        durationSec = it.getLong(it.getColumnIndexOrThrow("duration_sec")),
                        notes = it.getString(it.getColumnIndexOrThrow("notes"))
                    )
                )
            }
        }
        return list
    }

    fun getRecentSessions(limit: Int = 50, offset: Int = 0): List<V3Session> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3Session>()
        val safeExclude = getSafeExcludeClause(db)
        val cursor = db.query(
            "sessions",
            null,
            safeExclude,
            null,
            null,
            null,
            "start_time DESC",
            "$offset, $limit"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3Session(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        activity = it.getString(it.getColumnIndexOrThrow("activity")),
                        startTime = it.getLong(it.getColumnIndexOrThrow("start_time")),
                        endTime = if (it.isNull(it.getColumnIndexOrThrow("end_time"))) null else it.getLong(it.getColumnIndexOrThrow("end_time")),
                        durationSec = it.getLong(it.getColumnIndexOrThrow("duration_sec")),
                        notes = it.getString(it.getColumnIndexOrThrow("notes"))
                    )
                )
            }
        }
        return list
    }

    fun deleteSession(id: String): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            val r1 = db.delete("sessions", "id = ?", arrayOf(id))
            val r2 = db.delete("events", "id = ?", arrayOf(id))
            db.setTransactionSuccessful()
            r1 > 0 || r2 > 0
        } finally {
            db.endTransaction()
        }
    }

    fun updateSession(session: V3Session): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("activity", session.activity)
            put("start_time", session.startTime)
            session.endTime?.let { put("end_time", it) }
            put("duration_sec", session.durationSec)
            session.notes?.let { put("notes", it) }
        }
        val rows = db.update("sessions", values, "id = ?", arrayOf(session.id))
        return if (rows > 0) true else insertSession(session)
    }

    // ─── CORE MODULE 2: TASKS DISCIPLINE ────────────────────────────────────

    fun getTasks(status: String? = null): List<V3Task> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3Task>()
        val safeExclude = getSafeExcludeClause(db)
        val selection = if (!status.isNullOrBlank()) {
            "LOWER(status) = LOWER(?) AND $safeExclude"
        } else {
            safeExclude
        }
        val selectionArgs = if (!status.isNullOrBlank()) arrayOf(status) else null
        val cursor = db.query(
            "tasks",
            null,
            selection,
            selectionArgs,
            null,
            null,
            "CASE LOWER(status) WHEN 'completed' THEN 2 WHEN 'done' THEN 2 ELSE 1 END ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC, created_at DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val deadlineCol = it.getColumnIndex("deadline")
                val deadline = if (deadlineCol >= 0 && !it.isNull(deadlineCol)) it.getString(deadlineCol) else null
                list.add(
                    V3Task(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        priority = it.getString(it.getColumnIndexOrThrow("priority")),
                        status = it.getString(it.getColumnIndexOrThrow("status")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at")),
                        completedAt = if (it.isNull(it.getColumnIndexOrThrow("completed_at"))) null else it.getLong(it.getColumnIndexOrThrow("completed_at")),
                        deadline = deadline
                    )
                )
            }
        }
        return list
    }

    fun insertTask(task: V3Task): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", task.id.ifEmpty { UUID.randomUUID().toString() })
            put("title", task.title)
            put("priority", task.priority.uppercase())
            put("status", task.status)
            put("created_at", task.createdAt)
            task.completedAt?.let { put("completed_at", it) }
            task.deadline?.let { put("deadline", it) }
        }
        return try {
            db.insertWithOnConflict("tasks", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
        } catch (_: Exception) {
            values.remove("deadline")
            db.insertWithOnConflict("tasks", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
        }
    }

    fun updateTask(task: V3Task): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", task.title)
            put("priority", task.priority.uppercase())
            put("status", task.status)
            task.completedAt?.let { put("completed_at", it) } ?: putNull("completed_at")
            task.deadline?.let { put("deadline", it) } ?: putNull("deadline")
        }
        return try {
            db.update("tasks", values, "id = ?", arrayOf(task.id)) > 0
        } catch (_: Exception) {
            values.remove("deadline")
            db.update("tasks", values, "id = ?", arrayOf(task.id)) > 0
        }
    }

    fun updateTaskDetails(id: String, title: String? = null, priority: String? = null, deadline: String? = null): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        title?.let { values.put("title", it) }
        priority?.let { values.put("priority", it.uppercase()) }
        if (deadline != null) {
            if (deadline.isBlank()) values.putNull("deadline")
            else values.put("deadline", deadline)
        }
        return if (values.size() > 0) {
            try {
                db.update("tasks", values, "id = ?", arrayOf(id)) > 0
            } catch (_: Exception) {
                values.remove("deadline")
                if (values.size() > 0) db.update("tasks", values, "id = ?", arrayOf(id)) > 0 else true
            }
        } else true
    }

    fun setTaskStatus(id: String, status: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", status)
            if (status == "completed") {
                put("completed_at", System.currentTimeMillis())
            } else {
                putNull("completed_at")
            }
        }
        return db.update("tasks", values, "id = ?", arrayOf(id)) > 0
    }

    fun toggleTaskStatus(id: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query("tasks", arrayOf("status"), "id = ?", arrayOf(id), null, null, null)
        val currentStatus = cursor.use {
            if (it.moveToFirst()) it.getString(0) else return false
        }
        val isCompleted = currentStatus.equals("completed", ignoreCase = true) || currentStatus.equals("done", ignoreCase = true)
        val newStatus = if (isCompleted) "pending" else "completed"
        return setTaskStatus(id, newStatus)
    }

    fun deleteTask(id: String): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            val r1 = db.delete("tasks", "id = ?", arrayOf(id))
            val r2 = db.delete("events", "id = ?", arrayOf(id))
            db.setTransactionSuccessful()
            r1 > 0 || r2 > 0
        } finally {
            db.endTransaction()
        }
    }

    // ─── CORE MODULE 3: THOUGHTS & REFLECTIONS ──────────────────────────────

    fun insertThought(thought: V3Thought): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", thought.id.ifEmpty { UUID.randomUUID().toString() })
            put("timestamp", thought.timestamp)
            put("date", thought.date)
            put("topic", thought.topic)
            put("content", thought.content)
            put("pinned", if (thought.pinned) 1 else 0)
            put("tags", thought.tags)
        }
        return db.insertWithOnConflict("thoughts", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getRecentThoughts(limit: Int = 50): List<V3Thought> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3Thought>()
        val safeExclude = getSafeExcludeClause(db)
        val cursor = db.query(
            "thoughts",
            null,
            safeExclude,
            null,
            null,
            null,
            "pinned DESC, timestamp DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToThought(it))
            }
        }
        return list
    }

    fun getPinnedThoughts(): List<V3Thought> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3Thought>()
        val safeExclude = getSafeExcludeClause(db)
        val cursor = db.query(
            "thoughts",
            null,
            "pinned = 1 AND $safeExclude",
            null,
            null,
            null,
            "timestamp DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToThought(it))
            }
        }
        return list
    }

    fun toggleThoughtPinned(id: String): Boolean {
        val db = dbHelper.writableDatabase
        db.execSQL("UPDATE thoughts SET pinned = CASE WHEN pinned = 1 THEN 0 ELSE 1 END WHERE id = ?", arrayOf(id))
        return true
    }

    fun deleteThought(id: String): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            val r1 = db.delete("thoughts", "id = ?", arrayOf(id))
            val r2 = db.delete("events", "id = ?", arrayOf(id))
            db.setTransactionSuccessful()
            r1 > 0 || r2 > 0
        } finally {
            db.endTransaction()
        }
    }

    fun updateThought(thought: V3Thought): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("timestamp", thought.timestamp)
            put("date", thought.date)
            put("topic", thought.topic)
            put("content", thought.content)
            put("pinned", if (thought.pinned) 1 else 0)
            thought.tags?.let { put("tags", it) }
        }
        val rows = db.update("thoughts", values, "id = ?", arrayOf(thought.id))
        return if (rows > 0) true else insertThought(thought)
    }

    private fun cursorToThought(c: Cursor): V3Thought {
        return V3Thought(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp")),
            date = c.getString(c.getColumnIndexOrThrow("date")),
            topic = c.getString(c.getColumnIndexOrThrow("topic")),
            content = c.getString(c.getColumnIndexOrThrow("content")),
            pinned = c.getInt(c.getColumnIndexOrThrow("pinned")) == 1,
            tags = c.getString(c.getColumnIndexOrThrow("tags"))
        )
    }

    // ─── CORE MODULE 4: SCREEN TIME TELEMETRY ────────────────────────────────

    fun insertAppUsage(usage: V3AppUsage): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", usage.id.ifEmpty { UUID.randomUUID().toString() })
            put("package_name", usage.packageName)
            put("app_name", usage.appName)
            put("start_time", usage.startTime)
            put("end_time", usage.endTime)
            put("duration_ms", usage.durationMs)
            put("category", usage.category)
        }
        return db.insertWithOnConflict("app_usage", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getAppUsageForDate(startMs: Long, endMs: Long): List<V3AppUsage> {
        return getAppUsageForDate("", startMs, endMs)
    }

    fun getAppUsageForDate(dateStr: String, startMs: Long = 0L, endMs: Long = 0L): List<V3AppUsage> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3AppUsage>()
        val query = if (dateStr.isNotBlank() && startMs > 0L && endMs > 0L) {
            "id LIKE ? OR (start_time >= ? AND start_time <= ?)"
        } else if (dateStr.isNotBlank()) {
            "id LIKE ?"
        } else {
            "start_time >= ? AND start_time <= ?"
        }
        val args = if (dateStr.isNotBlank() && startMs > 0L && endMs > 0L) {
            arrayOf("${dateStr}%", startMs.toString(), endMs.toString())
        } else if (dateStr.isNotBlank()) {
            arrayOf("${dateStr}%")
        } else {
            arrayOf(startMs.toString(), endMs.toString())
        }
        val cursor = db.query(
            "app_usage",
            null,
            query,
            args,
            null,
            null,
            "duration_ms DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val dur = it.getLong(it.getColumnIndexOrThrow("duration_ms"))
                if (dur in 1..86400000L) {
                    list.add(
                        V3AppUsage(
                            id = it.getString(it.getColumnIndexOrThrow("id")),
                            packageName = it.getString(it.getColumnIndexOrThrow("package_name")),
                            appName = it.getString(it.getColumnIndexOrThrow("app_name")),
                            startTime = it.getLong(it.getColumnIndexOrThrow("start_time")),
                            endTime = it.getLong(it.getColumnIndexOrThrow("end_time")),
                            durationMs = dur,
                            category = it.getString(it.getColumnIndexOrThrow("category"))
                        )
                    )
                }
            }
        }
        return list
    }

    fun getAppUsageLogs(limit: Int = 200, offset: Int = 0): List<V3AppUsage> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3AppUsage>()
        val cursor = db.query(
            "app_usage",
            null,
            null,
            null,
            null,
            null,
            "start_time DESC",
            "$offset, $limit"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3AppUsage(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        packageName = it.getString(it.getColumnIndexOrThrow("package_name")),
                        appName = it.getString(it.getColumnIndexOrThrow("app_name")),
                        startTime = it.getLong(it.getColumnIndexOrThrow("start_time")),
                        endTime = it.getLong(it.getColumnIndexOrThrow("end_time")),
                        durationMs = it.getLong(it.getColumnIndexOrThrow("duration_ms")),
                        category = it.getString(it.getColumnIndexOrThrow("category"))
                    )
                )
            }
        }
        return list
    }

    fun deleteAppUsage(id: String? = null): Boolean {
        val db = dbHelper.writableDatabase
        return if (id.isNullOrBlank()) {
            db.delete("app_usage", null, null) >= 0
        } else {
            db.delete("app_usage", "id = ?", arrayOf(id)) > 0
        }
    }

    // ─── CORE UTILITY 1: DAY TRACKER ────────────────────────────────────────

    fun insertDayTrackerItem(item: V3DayTrackerItem): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", item.id.ifEmpty { UUID.randomUUID().toString() })
            put("title", item.title)
            put("target_date", item.targetDate)
            put("hour", item.hour)
            put("pinned", if (item.pinned) 1 else 0)
            put("created_at", item.createdAt)
        }
        return db.insertWithOnConflict("day_tracker", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getAllDayTrackerItems(): List<V3DayTrackerItem> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3DayTrackerItem>()
        val safeExclude = getSafeExcludeClause(db)
        val cursor = db.query(
            "day_tracker",
            null,
            safeExclude,
            null,
            null,
            null,
            "pinned DESC, target_date ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3DayTrackerItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        targetDate = it.getString(it.getColumnIndexOrThrow("target_date")),
                        hour = it.getInt(it.getColumnIndexOrThrow("hour")),
                        pinned = it.getInt(it.getColumnIndexOrThrow("pinned")) == 1,
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    fun getPinnedDayTrackerItems(): List<V3DayTrackerItem> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3DayTrackerItem>()
        val safeExclude = getSafeExcludeClause(db)
        val cursor = db.query(
            "day_tracker",
            null,
            "pinned = 1 AND $safeExclude",
            null,
            null,
            null,
            "target_date ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3DayTrackerItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        targetDate = it.getString(it.getColumnIndexOrThrow("target_date")),
                        hour = it.getInt(it.getColumnIndexOrThrow("hour")),
                        pinned = true,
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    fun toggleDayTrackerPinned(id: String): Boolean {
        val db = dbHelper.writableDatabase
        db.execSQL("UPDATE day_tracker SET pinned = CASE WHEN pinned = 1 THEN 0 ELSE 1 END WHERE id = ?", arrayOf(id))
        return true
    }

    fun deleteDayTrackerItem(id: String): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            val r1 = db.delete("day_tracker", "id = ?", arrayOf(id))
            val r2 = db.delete("events", "id = ?", arrayOf(id))
            db.setTransactionSuccessful()
            r1 > 0 || r2 > 0
        } finally {
            db.endTransaction()
        }
    }

    fun updateDayTrackerItem(item: V3DayTrackerItem): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", item.title)
            put("target_date", item.targetDate)
            put("hour", item.hour)
            put("pinned", if (item.pinned) 1 else 0)
        }
        val rows = db.update("day_tracker", values, "id = ?", arrayOf(item.id))
        return if (rows > 0) true else insertDayTrackerItem(item)
    }

    // ─── CORE UTILITY 2: RECYCLE BIN (SOFT DELETE & RECOVERY) ────────────────

    fun recycleItem(originalId: String, category: String, dataJson: String): Boolean {
        val db = dbHelper.writableDatabase
        val recycleId = "r_" + UUID.randomUUID().toString().take(8)

        db.beginTransaction()
        return try {
            // 1. Snapshot existing SQLite record with 100% column fidelity
            var capturedJson: String? = null
            var resolvedCategory = category

            // Check 'events' table
            var c = db.rawQuery("SELECT id, category, date, ts, timestamp, title, content, score, metadata_json FROM events WHERE id = ?", arrayOf(originalId))
            c.use {
                if (it.moveToFirst()) {
                    resolvedCategory = it.getString(1) ?: category
                    val obj = JSONObject().apply {
                        put("table", "events")
                        put("id", it.getString(0))
                        put("category", it.getString(1))
                        put("date", it.getString(2))
                        put("ts", it.getString(3))
                        put("timestamp", it.getLong(4))
                        put("title", it.getString(5) ?: "")
                        put("content", it.getString(6) ?: "")
                        put("score", it.getInt(7))
                        put("metadata_json", it.getString(8) ?: "")
                    }
                    capturedJson = obj.toString()
                }
            }

            // Check 'sessions' table
            if (capturedJson == null) {
                c = db.rawQuery("SELECT id, activity, start_time, end_time, duration_sec, notes FROM sessions WHERE id = ?", arrayOf(originalId))
                c.use {
                    if (it.moveToFirst()) {
                        resolvedCategory = "session"
                        val obj = JSONObject().apply {
                            put("table", "sessions")
                            put("id", it.getString(0))
                            put("activity", it.getString(1))
                            put("start_time", it.getLong(2))
                            if (!it.isNull(3)) put("end_time", it.getLong(3))
                            put("duration_sec", it.getInt(4))
                            put("notes", it.getString(5) ?: "")
                        }
                        capturedJson = obj.toString()
                    }
                }
            }

            // Check 'tasks' table
            if (capturedJson == null) {
                c = db.rawQuery("SELECT id, title, priority, status, created_at, completed_at, deadline FROM tasks WHERE id = ?", arrayOf(originalId))
                c.use {
                    if (it.moveToFirst()) {
                        resolvedCategory = "task"
                        val obj = JSONObject().apply {
                            put("table", "tasks")
                            put("id", it.getString(0))
                            put("title", it.getString(1))
                            put("priority", it.getString(2))
                            put("status", it.getString(3))
                            put("created_at", it.getLong(4))
                            if (!it.isNull(5)) put("completed_at", it.getLong(5))
                            put("deadline", it.getString(6) ?: "")
                        }
                        capturedJson = obj.toString()
                    }
                }
            }

            // Check 'thoughts' table
            if (capturedJson == null) {
                c = db.rawQuery("SELECT id, timestamp, date, topic, content, pinned, tags FROM thoughts WHERE id = ?", arrayOf(originalId))
                c.use {
                    if (it.moveToFirst()) {
                        resolvedCategory = "thought"
                        val obj = JSONObject().apply {
                            put("table", "thoughts")
                            put("id", it.getString(0))
                            put("timestamp", it.getLong(1))
                            put("date", it.getString(2))
                            put("topic", it.getString(3) ?: "")
                            put("content", it.getString(4))
                            put("pinned", it.getInt(5))
                            put("tags", it.getString(6) ?: "")
                        }
                        capturedJson = obj.toString()
                    }
                }
            }

            // Check 'day_tracker' table
            if (capturedJson == null) {
                c = db.rawQuery("SELECT id, title, target_date, hour, pinned, created_at FROM day_tracker WHERE id = ?", arrayOf(originalId))
                c.use {
                    if (it.moveToFirst()) {
                        resolvedCategory = "day_tracker"
                        val obj = JSONObject().apply {
                            put("table", "day_tracker")
                            put("id", it.getString(0))
                            put("title", it.getString(1))
                            put("target_date", it.getString(2))
                            put("hour", it.getInt(3))
                            put("pinned", it.getInt(4))
                            put("created_at", it.getLong(5))
                        }
                        capturedJson = obj.toString()
                    }
                }
            }

            // Check 'plugin_events' table
            if (capturedJson == null) {
                c = db.rawQuery("SELECT id, plugin_id, category, payload_json, timestamp, date FROM plugin_events WHERE id = ?", arrayOf(originalId))
                c.use {
                    if (it.moveToFirst()) {
                        resolvedCategory = "plugin_event"
                        val obj = JSONObject().apply {
                            put("table", "plugin_events")
                            put("id", it.getString(0))
                            put("plugin_id", it.getString(1))
                            put("category", it.getString(2))
                            put("payload_json", it.getString(3))
                            put("timestamp", it.getLong(4))
                            put("date", it.getString(5))
                        }
                        capturedJson = obj.toString()
                    }
                }
            }

            // Merge fallbackDataJson if provided
            val finalJson = try {
                if (capturedJson != null && dataJson.isNotBlank() && dataJson != "{}") {
                    val base = JSONObject(capturedJson!!)
                    val extra = JSONObject(dataJson)
                    val itKeys = extra.keys()
                    while (itKeys.hasNext()) {
                        val k = itKeys.next()
                        if (!base.has(k)) {
                            base.put(k, extra.get(k))
                        }
                    }
                    base.toString()
                } else {
                    capturedJson ?: (if (dataJson.isNotBlank()) dataJson else JSONObject().put("id", originalId).put("category", category).toString())
                }
            } catch (_: Exception) {
                capturedJson ?: dataJson
            }

            val values = ContentValues().apply {
                put("recycle_id", recycleId)
                put("original_id", originalId)
                put("category", resolvedCategory)
                put("data_json", finalJson)
                put("deleted_at", System.currentTimeMillis())
            }

            val inserted = db.insertWithOnConflict("recycle_bin", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L

            // Record permanent tombstone to guarantee zero resurrection across sync/imports
            val tombstoneValues = ContentValues().apply {
                put("id", originalId)
                put("category", resolvedCategory)
                put("deleted_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)

            // Atomically clear from all possible active tables
            db.delete("events", "id = ?", arrayOf(originalId))
            db.delete("sessions", "id = ?", arrayOf(originalId))
            db.delete("thoughts", "id = ?", arrayOf(originalId))
            db.delete("tasks", "id = ?", arrayOf(originalId))
            db.delete("day_tracker", "id = ?", arrayOf(originalId))
            db.delete("app_usage", "id = ?", arrayOf(originalId))
            db.delete("plugin_events", "id = ?", arrayOf(originalId))

            db.setTransactionSuccessful()
            inserted
        } finally {
            db.endTransaction()
        }
    }

    // ─── PERMANENT TOMBSTONES ───────────────────────────────────────────────

    fun isDeletedOrTombstoned(id: String): Boolean {
        if (id.isBlank()) return false
        val db = dbHelper.readableDatabase
        return try {
            val c1 = db.rawQuery("SELECT 1 FROM deleted_tombstones WHERE id = ? LIMIT 1", arrayOf(id))
            c1.use { if (it.moveToFirst()) return true }
            val c2 = db.rawQuery("SELECT 1 FROM recycle_bin WHERE original_id = ? LIMIT 1", arrayOf(id))
            c2.use { if (it.moveToFirst()) return true }
            false
        } catch (_: Exception) {
            false
        }
    }

    fun getAllTombstoneIds(): Set<String> {
        val db = dbHelper.readableDatabase
        val set = mutableSetOf<String>()
        try {
            val c1 = db.rawQuery("SELECT id FROM deleted_tombstones", null)
            c1.use { while (it.moveToNext()) set.add(it.getString(0)) }
            val c2 = db.rawQuery("SELECT original_id FROM recycle_bin", null)
            c2.use { while (it.moveToNext()) set.add(it.getString(0)) }
        } catch (_: Exception) {}
        return set
    }

    fun recordTombstone(id: String, category: String = "general"): Boolean {
        if (id.isBlank()) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", id)
            put("category", category)
            put("deleted_at", System.currentTimeMillis())
        }
        return db.insertWithOnConflict("deleted_tombstones", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun clearTombstone(id: String): Boolean {
        if (id.isBlank()) return false
        val db = dbHelper.writableDatabase
        return db.delete("deleted_tombstones", "id = ?", arrayOf(id)) > 0
    }

    fun getRecycleBinItems(limit: Int = 100): List<V3RecycleItem> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3RecycleItem>()
        val cursor = db.query(
            "recycle_bin",
            null,
            null,
            null,
            null,
            null,
            "deleted_at DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3RecycleItem(
                        recycleId = it.getString(it.getColumnIndexOrThrow("recycle_id")),
                        originalId = it.getString(it.getColumnIndexOrThrow("original_id")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        dataJson = it.getString(it.getColumnIndexOrThrow("data_json")),
                        deletedAt = it.getLong(it.getColumnIndexOrThrow("deleted_at"))
                    )
                )
            }
        }
        return list
    }

    fun restoreRecycleItem(recycleId: String): V3RecycleItem? {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            var item: V3RecycleItem? = null
            val cursor = db.query(
                "recycle_bin",
                null,
                "recycle_id = ?",
                arrayOf(recycleId),
                null,
                null,
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    item = V3RecycleItem(
                        recycleId = it.getString(it.getColumnIndexOrThrow("recycle_id")),
                        originalId = it.getString(it.getColumnIndexOrThrow("original_id")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        dataJson = it.getString(it.getColumnIndexOrThrow("data_json")),
                        deletedAt = it.getLong(it.getColumnIndexOrThrow("deleted_at"))
                    )
                }
            }
            if (item == null) {
                db.setTransactionSuccessful()
                return null
            }

            val targetItem = item!!
            val rawJson = targetItem.dataJson
            val cat = targetItem.category.lowercase()
            var restored = false

            try {
                val data = JSONObject(rawJson)
                val table = data.optString("table", "")

                when {
                    table == "sessions" || cat == "session" || cat == "sessions" -> {
                        val values = ContentValues().apply {
                            put("id", targetItem.originalId.ifBlank { data.optString("id", UUID.randomUUID().toString()) })
                            put("activity", data.optString("activity", data.optString("title", "Focus Session")))
                            put("start_time", data.optLong("start_time", data.optLong("timestamp", System.currentTimeMillis())))
                            if (data.has("end_time") && !data.isNull("end_time")) {
                                put("end_time", data.optLong("end_time"))
                            }
                            put("duration_sec", data.optInt("duration_sec", data.optInt("duration", 1500)))
                            put("notes", data.optString("notes", data.optString("content", "")))
                        }
                        db.insertWithOnConflict("sessions", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        restored = true
                    }
                    table == "tasks" || cat == "task" || cat == "tasks" -> {
                        val values = ContentValues().apply {
                            put("id", targetItem.originalId.ifBlank { data.optString("id", UUID.randomUUID().toString()) })
                            put("title", data.optString("title", "Restored Task"))
                            put("priority", data.optString("priority", "MEDIUM"))
                            put("status", data.optString("status", "pending"))
                            put("created_at", data.optLong("created_at", data.optLong("createdAt", System.currentTimeMillis())))
                            if (data.has("completed_at") && !data.isNull("completed_at")) {
                                put("completed_at", data.optLong("completed_at"))
                            }
                            if (data.has("deadline") && !data.isNull("deadline")) {
                                put("deadline", data.optString("deadline"))
                            }
                        }
                        db.insertWithOnConflict("tasks", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        restored = true
                    }
                    table == "thoughts" -> {
                        val values = ContentValues().apply {
                            put("id", targetItem.originalId.ifBlank { data.optString("id", UUID.randomUUID().toString()) })
                            put("timestamp", data.optLong("timestamp", System.currentTimeMillis()))
                            put("date", data.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())))
                            put("topic", data.optString("topic", "Reflection"))
                            put("content", data.optString("content", ""))
                            put("pinned", data.optInt("pinned", 0))
                            put("tags", data.optString("tags", ""))
                        }
                        db.insertWithOnConflict("thoughts", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        restored = true
                    }
                    table == "day_tracker" -> {
                        val values = ContentValues().apply {
                            put("id", targetItem.originalId.ifBlank { data.optString("id", UUID.randomUUID().toString()) })
                            put("title", data.optString("title", "Milestone"))
                            put("target_date", data.optString("target_date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())))
                            put("hour", data.optInt("hour", 0))
                            put("pinned", data.optInt("pinned", 0))
                            put("created_at", data.optLong("created_at", System.currentTimeMillis()))
                        }
                        db.insertWithOnConflict("day_tracker", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        restored = true
                    }
                    table == "plugin_events" || cat == "plugin_event" -> {
                        val values = ContentValues().apply {
                            put("id", targetItem.originalId.ifBlank { data.optString("id", UUID.randomUUID().toString()) })
                            put("plugin_id", data.optString("plugin_id", "com.v07.plugin"))
                            put("category", data.optString("category", "general"))
                            put("payload_json", data.optString("payload_json", "{}"))
                            put("timestamp", data.optLong("timestamp", System.currentTimeMillis()))
                            put("date", data.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())))
                        }
                        db.insertWithOnConflict("plugin_events", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        restored = true
                    }
                    cat == "habit" -> {
                        // Habits are stored client-side in localStorage; restored by returning data to frontend
                        restored = true
                    }
                    else -> {
                        // Default / Events / Ledger entry
                        val ts = data.optString("ts", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date()))
                        val date = data.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
                        val timestamp = data.optLong("timestamp", System.currentTimeMillis())
                        val title = data.optString("title", "Restored Entry")
                        val content = data.optString("content", data.optString("subtitle", ""))
                        val score = data.optInt("score", 0)
                        val meta = if (data.has("metadata_json")) data.optString("metadata_json") else if (data.has("metadataJson")) data.optString("metadataJson") else null

                        val values = ContentValues().apply {
                            put("id", targetItem.originalId.ifBlank { data.optString("id", UUID.randomUUID().toString()) })
                            put("category", targetItem.category.ifBlank { "quick" })
                            put("date", date)
                            put("ts", ts)
                            put("timestamp", timestamp)
                            put("title", title)
                            put("content", content)
                            put("score", score)
                            if (meta != null && meta.isNotBlank()) put("metadata_json", meta)
                        }
                        db.insertWithOnConflict("events", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        restored = true
                    }
                }
            } catch (e: Exception) {
                Log.e("EventRepository", "Error deserializing recycle item during restore", e)
                restored = false
            }

            if (restored) {
                db.delete("recycle_bin", "recycle_id = ?", arrayOf(recycleId))
                db.delete("deleted_tombstones", "id = ?", arrayOf(targetItem.originalId))
                db.setTransactionSuccessful()
                targetItem
            } else {
                null
            }
        } finally {
            db.endTransaction()
        }
    }

    fun purgeRecycleItem(recycleId: String): String? {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            var originalId: String? = null
            var category = "general"
            val cursor = db.query(
                "recycle_bin",
                arrayOf("original_id", "category"),
                "recycle_id = ?",
                arrayOf(recycleId),
                null, null, null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    originalId = it.getString(0)
                    category = it.getString(1) ?: "general"
                }
            }
            if (!originalId.isNullOrBlank()) {
                val tombstoneValues = ContentValues().apply {
                    put("id", originalId)
                    put("category", category)
                    put("deleted_at", System.currentTimeMillis())
                }
                db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                db.delete("events", "id = ?", arrayOf(originalId))
                db.delete("sessions", "id = ?", arrayOf(originalId))
                db.delete("thoughts", "id = ?", arrayOf(originalId))
                db.delete("tasks", "id = ?", arrayOf(originalId))
                db.delete("day_tracker", "id = ?", arrayOf(originalId))
                db.delete("plugin_events", "id = ?", arrayOf(originalId))
            }
            val deleted = db.delete("recycle_bin", "recycle_id = ?", arrayOf(recycleId))
            if (deleted > 0) {
                db.setTransactionSuccessful()
                originalId ?: recycleId
            } else {
                null
            }
        } finally {
            db.endTransaction()
        }
    }

    fun emptyRecycleBin(): List<String> {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        val purgedIds = mutableListOf<String>()
        return try {
            val cursor = db.query(
                "recycle_bin",
                arrayOf("original_id", "category"),
                null, null, null, null, null
            )
            cursor.use {
                while (it.moveToNext()) {
                    val origId = it.getString(0)
                    val cat = it.getString(1) ?: "general"
                    if (!origId.isNullOrBlank()) {
                        purgedIds.add(origId)
                        val tombstoneValues = ContentValues().apply {
                            put("id", origId)
                            put("category", cat)
                            put("deleted_at", System.currentTimeMillis())
                        }
                        db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                        db.delete("events", "id = ?", arrayOf(origId))
                        db.delete("sessions", "id = ?", arrayOf(origId))
                        db.delete("thoughts", "id = ?", arrayOf(origId))
                        db.delete("tasks", "id = ?", arrayOf(origId))
                        db.delete("day_tracker", "id = ?", arrayOf(origId))
                        db.delete("plugin_events", "id = ?", arrayOf(origId))
                    }
                }
            }
            db.delete("recycle_bin", null, null)
            db.setTransactionSuccessful()
            purgedIds
        } finally {
            db.endTransaction()
        }
    }

    // ─── PLUGIN PLATFORM STORAGE ────────────────────────────────────────────

    fun getPluginValue(pluginId: String, key: String): String? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "plugin_storage",
            arrayOf("value_json"),
            "plugin_id = ? AND key = ?",
            arrayOf(pluginId, key),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(0)
            }
        }
        return null
    }

    fun setPluginValue(pluginId: String, key: String, valueJson: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("plugin_id", pluginId)
            put("key", key)
            put("value_json", valueJson)
            put("updated_at", System.currentTimeMillis())
        }
        return db.insertWithOnConflict("plugin_storage", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun deletePluginValue(pluginId: String, key: String): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete("plugin_storage", "plugin_id = ? AND key = ?", arrayOf(pluginId, key)) > 0
    }

    fun getAllPluginKeys(pluginId: String): List<String> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<String>()
        val cursor = db.query(
            "plugin_storage",
            arrayOf("key"),
            "plugin_id = ?",
            arrayOf(pluginId),
            null,
            null,
            "key ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        return list
    }

    // ─── PLUGIN PLATFORM DOMAIN EVENTS (SANDBOXED) ──────────────────────────

    fun insertPluginEvent(event: V3PluginEvent): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", event.id.ifEmpty { UUID.randomUUID().toString() })
            put("plugin_id", event.pluginId)
            put("category", event.category)
            put("payload_json", event.payloadJson)
            put("timestamp", event.timestamp)
            put("date", event.date)
        }
        return db.insertWithOnConflict("plugin_events", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getPluginEvents(pluginId: String, limit: Int = 100): List<V3PluginEvent> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3PluginEvent>()
        val safeExclude = getSafeExcludeClause(db)
        val cursor = db.query(
            "plugin_events",
            null,
            "plugin_id = ? AND $safeExclude",
            arrayOf(pluginId),
            null, null,
            "timestamp DESC",
            limit.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3PluginEvent(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        pluginId = it.getString(it.getColumnIndexOrThrow("plugin_id")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        payloadJson = it.getString(it.getColumnIndexOrThrow("payload_json")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        date = it.getString(it.getColumnIndexOrThrow("date"))
                    )
                )
            }
        }
        return list
    }

    fun getPluginEventsByCategory(pluginId: String, category: String, date: String? = null): List<V3PluginEvent> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3PluginEvent>()
        val safeExclude = getSafeExcludeClause(db)
        val whereClause: String
        val args: Array<String>
        if (!date.isNullOrBlank()) {
            whereClause = "plugin_id = ? AND category = ? AND date = ? AND $safeExclude"
            args = arrayOf(pluginId, category, date)
        } else {
            whereClause = "plugin_id = ? AND category = ? AND $safeExclude"
            args = arrayOf(pluginId, category)
        }
        val cursor = db.query("plugin_events", null, whereClause, args, null, null, "timestamp DESC", "200")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3PluginEvent(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        pluginId = it.getString(it.getColumnIndexOrThrow("plugin_id")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        payloadJson = it.getString(it.getColumnIndexOrThrow("payload_json")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        date = it.getString(it.getColumnIndexOrThrow("date"))
                    )
                )
            }
        }
        return list
    }

    fun deletePluginEvent(id: String): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete("plugin_events", "id = ?", arrayOf(id)) > 0
    }

    fun getAllPluginEventsForSync(sinceTs: Long = 0L): List<V3PluginEvent> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<V3PluginEvent>()
        val safeExclude = getSafeExcludeClause(db)
        val whereClause = if (sinceTs > 0) "timestamp >= ? AND $safeExclude" else safeExclude
        val args = if (sinceTs > 0) arrayOf(sinceTs.toString()) else null
        val cursor = db.query("plugin_events", null, whereClause, args, null, null, "timestamp ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    V3PluginEvent(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        pluginId = it.getString(it.getColumnIndexOrThrow("plugin_id")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        payloadJson = it.getString(it.getColumnIndexOrThrow("payload_json")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        date = it.getString(it.getColumnIndexOrThrow("date"))
                    )
                )
            }
        }
        return list
    }

    // ─── SYNC WATERMARKS ────────────────────────────────────────────────────

    fun getSyncWatermark(collectionName: String): Long {
        val db = dbHelper.readableDatabase
        val cursor = db.query("sync_watermarks", arrayOf("last_sync_timestamp"), "collection_name = ?", arrayOf(collectionName), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        return 0L
    }

    fun setSyncWatermark(collectionName: String, timestamp: Long): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("collection_name", collectionName)
            put("last_sync_timestamp", timestamp)
        }
        return db.insertWithOnConflict("sync_watermarks", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    // ─── DAILY TIMELINE (V2 TIMELINE EQUIVALENT) ───────────────────────────

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

    // ─── CORE CADENCE & LIFE OS EVENTS ──────────────────────────────────────

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
        return db.insertWithOnConflict("events", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
            db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
            r1 > 0 || r2 > 0 || r3 > 0 || r4 > 0 || r5 > 0 || r6 > 0 || r7 > 0
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Batch purge of entities from all operational tables in a single SQLite transaction.
     */
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
                db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
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

    /**
     * Batch soft-delete of entities to recycle bin in a single SQLite transaction.
     */
    fun batchRecycleEntities(items: List<Triple<String, String, String>>): Int {
        if (items.isEmpty()) return 0
        var count = 0
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for ((originalId, category, dataJson) in items) {
                if (originalId.isBlank()) continue
                if (recycleItem(originalId, category, dataJson)) {
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

        // Parse metadataJson if present to sync specialized tables
        var metaObj: org.json.JSONObject? = null
        if (!metadataJson.isNullOrBlank()) {
            try {
                metaObj = org.json.JSONObject(metadataJson)
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

    // ─── SANCTUARY LEDGER AUTHORITATIVE QUERY ENGINE ──────────────────────────

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

    // ─── UNIFIED DATA EXPLORER ENGINE ───────────────────────────────────────

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
                        Log.w("EventRepository", "Skipping malformed event row: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("EventRepository", "Error querying events table: ${e.message}")
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
                            Log.w("EventRepository", "Skipping malformed thought row: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EventRepository", "Error querying thoughts table: ${e.message}")
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
                val historyStr = getPluginValue("com.v07.running", "history")
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
                Log.w("EventRepository", "Error parsing running plugin storage: ${e.message}")
            }
        }

        // 8. Ingest plugin_storage for Habits (com.v07.habits -> definitions & logs)
        if (checkCat("habit") || checkCat("habits")) {
            try {
                val defsMap = mutableMapOf<String, String>()
                val defsStr = getPluginValue("com.v07.habits", "definitions")
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

                val logsStr = getPluginValue("com.v07.habits", "logs")
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
                Log.w("EventRepository", "Error parsing habits plugin storage: ${e.message}")
            }
        }

        // 9. Ingest plugin_storage for Vitals (com.v07.vitals -> logs)
        if (checkCat("vitals") || checkCat("mood") || checkCat("energy")) {
            try {
                val vitalsStr = getPluginValue("com.v07.vitals", "logs")
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
                Log.w("EventRepository", "Error parsing vitals plugin storage: ${e.message}")
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
            if (!keepPluginConfigs) {
                db.delete("plugin_storage", null, null)
            } else {
                db.delete("plugin_storage", "key IN ('history', 'logs', 'definitions', 'entries', 'shelf') OR key LIKE 'run_%' OR key LIKE 'entry_%' OR plugin_id = 'com.v07.running'", null)
            }
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.e("EventRepository", "Purge all data failed: ${e.message}", e)
            false
        } finally {
            db.endTransaction()
        }
    }
}
