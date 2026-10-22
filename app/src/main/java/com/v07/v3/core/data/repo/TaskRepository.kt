package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3Task
import java.util.UUID

/**
 * Dedicated Repository for Tasks Discipline (Core Module 2).
 */
class TaskRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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
            db.insertWithOnConflict("tasks", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
        } catch (_: Exception) {
            values.remove("deadline")
            db.insertWithOnConflict("tasks", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
}
