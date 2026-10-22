package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3Session
import java.util.UUID

/**
 * Dedicated Repository for Session Timer & Focus Sessions (Core Module 1).
 */
class SessionRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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
        return db.insertWithOnConflict("sessions", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
}
