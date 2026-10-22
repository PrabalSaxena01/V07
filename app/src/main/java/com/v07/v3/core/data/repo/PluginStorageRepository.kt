package com.v07.v3.core.data.repo

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database

/**
 * Dedicated Repository for Plugin Key-Value Storage and Sync Watermarks.
 */
class PluginStorageRepository(dbHelper: V07Database) : BaseRepository(dbHelper) {

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
        return db.insertWithOnConflict("plugin_storage", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
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

    fun getSyncWatermark(collectionName: String): Long {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "sync_watermarks",
            arrayOf("last_sync_timestamp"),
            "collection_name = ?",
            arrayOf(collectionName),
            null,
            null,
            null
        )
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
        return db.insertWithOnConflict("sync_watermarks", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }
}
