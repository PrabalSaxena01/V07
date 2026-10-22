package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3AppUsage
import java.util.UUID

/**
 * Dedicated Repository for Screen Time & Passive Attention Telemetry (Core Module 4).
 */
class ScreenTimeRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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
        return db.insertWithOnConflict("app_usage", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
}
