package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3Thought
import java.util.UUID

/**
 * Dedicated Repository for Thoughts & Reflections (Core Module 3).
 */
class ThoughtRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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
        return db.insertWithOnConflict("thoughts", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
}
