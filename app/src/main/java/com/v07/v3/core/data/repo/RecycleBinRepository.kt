package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3RecycleItem
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Dedicated Repository for Soft Delete, Recycle Bin & Permanent Tombstones.
 */
class RecycleBinRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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

            val inserted = db.insertWithOnConflict("recycle_bin", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L

            // Record permanent tombstone to guarantee zero resurrection across sync/imports
            val tombstoneValues = ContentValues().apply {
                put("id", originalId)
                put("category", resolvedCategory)
                put("deleted_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, SQLiteDatabase.CONFLICT_REPLACE)

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
        return db.insertWithOnConflict("deleted_tombstones", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
            var restored: Boolean

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
                        db.insertWithOnConflict("sessions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
                        db.insertWithOnConflict("tasks", null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
                        db.insertWithOnConflict("thoughts", null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
                        db.insertWithOnConflict("day_tracker", null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
                        db.insertWithOnConflict("plugin_events", null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
                        db.insertWithOnConflict("events", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                        restored = true
                    }
                }
            } catch (e: Exception) {
                Log.e("RecycleBinRepository", "Error deserializing recycle item during restore", e)
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
                db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, SQLiteDatabase.CONFLICT_REPLACE)
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
                        db.insertWithOnConflict("deleted_tombstones", null, tombstoneValues, SQLiteDatabase.CONFLICT_REPLACE)
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
}
