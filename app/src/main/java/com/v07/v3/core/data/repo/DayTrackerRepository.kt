package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3DayTrackerItem
import java.util.UUID

/**
 * Dedicated Repository for Day Tracker & Milestone Tracking.
 */
class DayTrackerRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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
        return db.insertWithOnConflict("day_tracker", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
}
