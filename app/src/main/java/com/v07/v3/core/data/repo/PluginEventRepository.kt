package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3PluginEvent
import java.util.UUID

/**
 * Dedicated Repository for Sandboxed Plugin Domain Events.
 */
class PluginEventRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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
        return db.insertWithOnConflict("plugin_events", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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
}
