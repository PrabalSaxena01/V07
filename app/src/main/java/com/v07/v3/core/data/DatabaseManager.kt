package com.v07.v3.core.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Core Utility: Database Administration & Health Manager.
 *
 * Provides:
 * - Real-time statistics (file size, table row counts, WAL status).
 * - VACUUM and page optimization.
 * - Root-free snapshot backup creation to /storage/emulated/0/V07Data/backups/.
 * - Safe database restore.
 */
class DatabaseManager private constructor(private val context: Context) {

    private val dbHelper: V07Database
        get() = V07Database.getInstance(context)

    fun getActiveProfile(): String = V07Database.getCurrentProfileId()
    fun listProfiles(): List<String> = V07Database.listProfiles(context)
    fun switchProfile(profileId: String): Boolean = V07Database.switchProfile(context, profileId)
    fun createProfile(profileId: String): Boolean = V07Database.createProfile(context, profileId)
    fun cloneProfile(sourceId: String, targetId: String): Boolean = V07Database.cloneProfile(context, sourceId, targetId)
    fun deleteProfile(profileId: String): Boolean = V07Database.deleteProfile(context, profileId)

    companion object {
        const val TAG = "DatabaseManager"
        const val BACKUP_DIR_NAME = "backups"

        @Volatile
        private var instance: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: DatabaseManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getStats(): V3DatabaseStats {
        val dbFile = V07Database.resolveDatabaseFile(context)
        val sizeBytes = if (dbFile.exists()) dbFile.length() else 0L
        val db = dbHelper.readableDatabase

        val tableCounts = mutableMapOf<String, Long>()
        val tables = listOf("sessions", "tasks", "thoughts", "app_usage", "day_tracker", "recycle_bin", "plugin_storage", "events", "deleted_tombstones")
        for (table in tables) {
            try {
                val cursor = db.rawQuery("SELECT COUNT(*) FROM $table", null)
                cursor.use {
                    if (it.moveToFirst()) {
                        tableCounts[table] = it.getLong(0)
                    }
                }
            } catch (e: Exception) {
                tableCounts[table] = 0L
            }
        }

        return V3DatabaseStats(
            dbPath = dbFile.absolutePath,
            fileSizeBytes = sizeBytes,
            tableCounts = tableCounts,
            isWalEnabled = db.isWriteAheadLoggingEnabled
        )
    }

    fun vacuum(): Boolean {
        return try {
            Log.i(TAG, "Running VACUUM optimization on operational database...")
            val db = dbHelper.writableDatabase
            db.execSQL("VACUUM;")
            Log.i(TAG, "VACUUM completed successfully.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "VACUUM failed: ${e.message}", e)
            false
        }
    }

    fun resolveBackupDirectory(): File {
        // Priority 1: Scoped external files directory (accessible via USB MTP without runtime permissions)
        val extBackupDir = context.getExternalFilesDir(BACKUP_DIR_NAME)
        if (extBackupDir != null && (extBackupDir.exists() || extBackupDir.mkdirs())) {
            return extBackupDir
        }
        // Fallback: Internal app storage directory
        val baseDir = V07Database.resolveDatabaseFile(context).parentFile
            ?: File(context.filesDir, V07Database.DEFAULT_DIR_NAME)
        val backupDir = File(baseDir, BACKUP_DIR_NAME)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }

    fun createBackup(): File? {
        val dbFile = V07Database.resolveDatabaseFile(context)
        if (!dbFile.exists()) return null

        return try {
            val backupDir = resolveBackupDirectory()
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "v07_backup_$timestampStr.db")

            // Flush WAL before copying
            val db = dbHelper.writableDatabase
            val cursor = db.rawQuery("PRAGMA wal_checkpoint(FULL);", null)
            cursor.use { it.moveToFirst() }

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.i(TAG, "Backup created successfully at: ${backupFile.absolutePath}")
            backupFile
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed: ${e.message}", e)
            null
        }
    }

    fun checkIntegrity(): String {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("PRAGMA integrity_check;", null)
            cursor.use {
                if (it.moveToFirst()) it.getString(0) else "ok"
            }
        } catch (e: Exception) {
            "error: ${e.message}"
        }
    }

    fun reindex(): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            db.execSQL("REINDEX;")
            true
        } catch (e: Exception) {
            Log.e(TAG, "REINDEX failed: ${e.message}", e)
            false
        }
    }

    fun clearAppCache(): Long {
        var bytesDeleted = 0L
        try {
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.exists()) {
                val files = cacheDir.listFiles() ?: emptyArray()
                for (f in files) {
                    bytesDeleted += f.length()
                    f.deleteRecursively()
                }
            }
            val extCacheDir = context.externalCacheDir
            if (extCacheDir != null && extCacheDir.exists()) {
                val files = extCacheDir.listFiles() ?: emptyArray()
                for (f in files) {
                    bytesDeleted += f.length()
                    f.deleteRecursively()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "clearAppCache error: ${e.message}")
        }
        return bytesDeleted
    }

    fun getProfileDetails(profileId: String): JSONObject {
        val cleanId = V07Database.sanitizeProfileId(profileId)
        val dir = V07Database.resolveDatabaseDir(context)
        val dbFile = File(dir, "v07_profile_${cleanId}.db")
        val sizeBytes = if (dbFile.exists()) dbFile.length() else 0L
        val lastMod = if (dbFile.exists()) dbFile.lastModified() else 0L
        val isActive = cleanId == V07Database.getCurrentProfileId()
        val tableCounts = mutableMapOf<String, Long>()
        val tables = listOf("sessions", "tasks", "thoughts", "app_usage", "day_tracker", "recycle_bin", "plugin_storage", "events", "deleted_tombstones", "plugin_events", "sync_watermarks")
        if (dbFile.exists()) {
            try {
                val db = if (isActive) dbHelper.readableDatabase else SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
                for (t in tables) {
                    try {
                        val cursor = db.rawQuery("SELECT COUNT(*) FROM \"$t\"", null)
                        cursor.use { if (it.moveToFirst()) tableCounts[t] = it.getLong(0) }
                    } catch (_: Exception) {
                        tableCounts[t] = 0L
                    }
                }
                if (!isActive) db.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error inspecting profile $cleanId: ${e.message}")
            }
        }
        val tObj = JSONObject()
        tableCounts.forEach { (k, v) -> tObj.put(k, v) }
        return JSONObject().apply {
            put("profile_id", cleanId)
            put("is_active", isActive)
            put("file_size_bytes", sizeBytes)
            put("file_size_formatted", "${sizeBytes / 1024} KB")
            put("last_modified", lastMod)
            put("last_modified_date", if (lastMod > 0) SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(lastMod)) else "")
            put("table_counts", tObj)
            put("total_records", tableCounts.values.sum())
        }
    }

    fun listProfilesWithStats(): JSONArray {
        val profiles = listProfiles()
        val arr = JSONArray()
        for (p in profiles) {
            arr.put(getProfileDetails(p))
        }
        return arr
    }

    fun getAllTableNames(): List<String> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<String>()
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' ORDER BY name ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        return list
    }

    fun getTableSchema(tableName: String): JSONArray {
        val safeTable = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
        val db = dbHelper.readableDatabase
        val arr = JSONArray()
        val cursor = db.rawQuery("PRAGMA table_info(\"$safeTable\");", null)
        cursor.use {
            while (it.moveToNext()) {
                arr.put(JSONObject().apply {
                    put("cid", it.getInt(0))
                    put("name", it.getString(1))
                    put("type", it.getString(2))
                    put("notnull", it.getInt(3) == 1)
                    put("dflt_value", it.getString(4) ?: JSONObject.NULL)
                    put("pk", it.getInt(5) == 1)
                })
            }
        }
        return arr
    }

    fun getTableData(
        tableName: String,
        limit: Int = 100,
        offset: Int = 0,
        sortCol: String? = null,
        sortDir: String? = "DESC",
        search: String? = null
    ): JSONObject {
        val safeTable = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
        val allTables = getAllTableNames()
        if (safeTable !in allTables) {
            throw IllegalArgumentException("Invalid table name: $tableName")
        }

        val db = dbHelper.readableDatabase
        val schema = getTableSchema(safeTable)
        val colNames = mutableListOf<String>()
        for (i in 0 until schema.length()) {
            colNames.add(schema.getJSONObject(i).getString("name"))
        }

        val whereClause = StringBuilder()
        val whereArgs = mutableListOf<String>()
        if (!search.isNullOrBlank() && colNames.isNotEmpty()) {
            val terms = colNames.map { "\"$it\" LIKE ?" }
            whereClause.append(" WHERE (").append(terms.joinToString(" OR ")).append(")")
            val pattern = "%$search%"
            repeat(colNames.size) { whereArgs.add(pattern) }
        }

        val countSql = "SELECT COUNT(*) FROM \"$safeTable\"$whereClause"
        var totalRows = 0L
        val countCursor = db.rawQuery(countSql, if (whereArgs.isEmpty()) null else whereArgs.toTypedArray())
        countCursor.use {
            if (it.moveToFirst()) totalRows = it.getLong(0)
        }

        val safeSortCol = if (sortCol != null && sortCol in colNames) sortCol else (if (colNames.isNotEmpty()) colNames[0] else null)
        val safeDir = if (sortDir?.uppercase() == "ASC") "ASC" else "DESC"
        val orderClause = if (safeSortCol != null) " ORDER BY \"$safeSortCol\" $safeDir" else ""

        val querySql = "SELECT * FROM \"$safeTable\"$whereClause$orderClause LIMIT $limit OFFSET $offset"
        val rows = JSONArray()
        val dataCursor = db.rawQuery(querySql, if (whereArgs.isEmpty()) null else whereArgs.toTypedArray())
        dataCursor.use { cursor ->
            val colCount = cursor.columnCount
            while (cursor.moveToNext()) {
                val rowObj = JSONObject()
                for (i in 0 until colCount) {
                    val cName = cursor.getColumnName(i)
                    when (cursor.getType(i)) {
                        Cursor.FIELD_TYPE_NULL -> rowObj.put(cName, JSONObject.NULL)
                        Cursor.FIELD_TYPE_INTEGER -> rowObj.put(cName, cursor.getLong(i))
                        Cursor.FIELD_TYPE_FLOAT -> rowObj.put(cName, cursor.getDouble(i))
                        Cursor.FIELD_TYPE_STRING -> rowObj.put(cName, cursor.getString(i))
                        Cursor.FIELD_TYPE_BLOB -> rowObj.put(cName, "[BLOB ${cursor.getBlob(i).size} bytes]")
                        else -> rowObj.put(cName, cursor.getString(i))
                    }
                }
                rows.put(rowObj)
            }
        }

        return JSONObject().apply {
            put("table", safeTable)
            put("schema", schema)
            put("columns", JSONArray(colNames))
            put("rows", rows)
            put("total_count", totalRows)
            put("limit", limit)
            put("offset", offset)
            put("page", if (limit > 0) (offset / limit) + 1 else 1)
        }
    }

    fun insertTableRow(tableName: String, values: JSONObject): Boolean {
        val safeTable = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
        if (safeTable !in getAllTableNames()) throw IllegalArgumentException("Invalid table: $tableName")
        val db = dbHelper.writableDatabase
        val cv = ContentValues()
        val keys = values.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (values.isNull(k)) {
                cv.putNull(k)
            } else {
                val v = values.get(k)
                when (v) {
                    is Boolean -> cv.put(k, if (v) 1 else 0)
                    is Int -> cv.put(k, v)
                    is Long -> cv.put(k, v)
                    is Double -> cv.put(k, v)
                    is Float -> cv.put(k, v)
                    else -> cv.put(k, v.toString())
                }
            }
        }
        return db.insertWithOnConflict(safeTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun updateTableRow(tableName: String, pkCol: String, pkVal: String, values: JSONObject): Boolean {
        val safeTable = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
        val safePkCol = pkCol.replace(Regex("[^a-zA-Z0-9_]"), "")
        if (safeTable !in getAllTableNames()) throw IllegalArgumentException("Invalid table: $tableName")
        val db = dbHelper.writableDatabase
        val cv = ContentValues()
        val keys = values.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k == safePkCol) continue
            if (values.isNull(k)) {
                cv.putNull(k)
            } else {
                val v = values.get(k)
                when (v) {
                    is Boolean -> cv.put(k, if (v) 1 else 0)
                    is Int -> cv.put(k, v)
                    is Long -> cv.put(k, v)
                    is Double -> cv.put(k, v)
                    is Float -> cv.put(k, v)
                    else -> cv.put(k, v.toString())
                }
            }
        }
        return db.update(safeTable, cv, "\"$safePkCol\" = ?", arrayOf(pkVal)) > 0
    }

    fun deleteTableRow(tableName: String, pkCol: String, pkVal: String): Boolean {
        val safeTable = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
        val safePkCol = pkCol.replace(Regex("[^a-zA-Z0-9_]"), "")
        if (safeTable !in getAllTableNames()) throw IllegalArgumentException("Invalid table: $tableName")
        val db = dbHelper.writableDatabase
        return db.delete(safeTable, "\"$safePkCol\" = ?", arrayOf(pkVal)) > 0
    }

    fun executeSql(sql: String): JSONObject {
        val trimmed = sql.trim()
        if (trimmed.isBlank()) {
            return JSONObject().apply {
                put("ok", false)
                put("error", "Empty SQL statement")
            }
        }

        val start = System.currentTimeMillis()
        val isQuery = trimmed.startsWith("SELECT", ignoreCase = true) ||
                      trimmed.startsWith("PRAGMA", ignoreCase = true) ||
                      trimmed.startsWith("EXPLAIN", ignoreCase = true)

        return try {
            if (isQuery) {
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery(trimmed, null)
                val rows = JSONArray()
                val colNames = JSONArray()
                cursor.use { c ->
                    for (i in 0 until c.columnCount) {
                        colNames.put(c.getColumnName(i))
                    }
                    var count = 0
                    while (c.moveToNext() && count < 1000) {
                        val row = JSONObject()
                        for (i in 0 until c.columnCount) {
                            val cName = c.getColumnName(i)
                            when (c.getType(i)) {
                                Cursor.FIELD_TYPE_NULL -> row.put(cName, JSONObject.NULL)
                                Cursor.FIELD_TYPE_INTEGER -> row.put(cName, c.getLong(i))
                                Cursor.FIELD_TYPE_FLOAT -> row.put(cName, c.getDouble(i))
                                Cursor.FIELD_TYPE_STRING -> row.put(cName, c.getString(i))
                                Cursor.FIELD_TYPE_BLOB -> row.put(cName, "[BLOB ${c.getBlob(i).size} bytes]")
                                else -> row.put(cName, c.getString(i))
                            }
                        }
                        rows.put(row)
                        count++
                    }
                }
                val elapsed = System.currentTimeMillis() - start
                JSONObject().apply {
                    put("ok", true)
                    put("is_query", true)
                    put("columns", colNames)
                    put("rows", rows)
                    put("row_count", rows.length())
                    put("execution_time_ms", elapsed)
                }
            } else {
                val db = dbHelper.writableDatabase
                var affected = 0
                try {
                    val stmt = db.compileStatement(trimmed)
                    affected = stmt.executeUpdateDelete()
                } catch (_: Exception) {
                    db.execSQL(trimmed)
                    affected = 1
                }
                val elapsed = System.currentTimeMillis() - start
                JSONObject().apply {
                    put("ok", true)
                    put("is_query", false)
                    put("rows_affected", affected)
                    put("execution_time_ms", elapsed)
                }
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            JSONObject().apply {
                put("ok", false)
                put("error", e.message ?: "Unknown SQL error")
                put("execution_time_ms", elapsed)
            }
        }
    }

    fun listBackups(): JSONArray {
        val dir = resolveBackupDirectory()
        val arr = JSONArray()
        val files = dir.listFiles { _, name -> name.endsWith(".db") } ?: emptyArray()
        files.sortedByDescending { it.lastModified() }.forEach { f ->
            arr.put(JSONObject().apply {
                put("filename", f.name)
                put("path", f.absolutePath)
                put("file_size_bytes", f.length())
                put("file_size_formatted", "${f.length() / 1024} KB")
                put("timestamp", f.lastModified())
                put("date_formatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(f.lastModified())))
            })
        }
        return arr
    }

    fun restoreBackup(backupFilePath: String): Boolean {
        val backupFile = File(backupFilePath)
        if (!backupFile.exists() || !backupFile.isFile) {
            Log.e(TAG, "Restore failed: backup file does not exist ($backupFilePath)")
            return false
        }
        val targetDbFile = V07Database.resolveDatabaseFile(context)
        return try {
            Log.i(TAG, "Restoring database from ${backupFile.absolutePath} to ${targetDbFile.absolutePath}...")
            val currentProfile = V07Database.getCurrentProfileId()
            V07Database.switchProfile(context, currentProfile)
            val dbHelperInstance = V07Database.getInstance(context)
            try { dbHelperInstance.close() } catch (_: Exception) {}

            FileInputStream(backupFile).use { input ->
                FileOutputStream(targetDbFile).use { output ->
                    input.copyTo(output)
                }
            }
            val wal = File(targetDbFile.parentFile, "${targetDbFile.name}-wal")
            val shm = File(targetDbFile.parentFile, "${targetDbFile.name}-shm")
            if (wal.exists()) wal.delete()
            if (shm.exists()) shm.delete()

            V07Database.getInstance(context)
            Log.i(TAG, "Database restore completed successfully.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Database restore failed: ${e.message}", e)
            false
        }
    }
}
