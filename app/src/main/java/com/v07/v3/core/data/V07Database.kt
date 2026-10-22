package com.v07.v3.core.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * V3 Authoritative Operational SQLite Database Helper.
 * Configured with Write-Ahead Logging (WAL) for high concurrency, zero file-locking contention,
 * and sub-millisecond indexed queries.
 *
 * Core Modules:
 * 1. Session Timer (sessions table)
 * 2. Tasks (tasks table)
 * 3. Thoughts (thoughts table)
 * 4. Screen Time (app_usage table)
 *
 * Core Utilities:
 * 1. Day Tracker (day_tracker table)
 * 2. Recycle Bin (recycle_bin table)
 * 3. Database Manager (admin & health operations)
 *
 * Extensibility:
 * + Isolated Plugin Platform Storage (plugin_storage table)
 *
 * Placed in accessible shared storage (/storage/emulated/0/V07Data/v07.db) so that the user
 * can copy, inspect, or back up the database without root access.
 */
class V07Database private constructor(context: Context, dbPath: String) :
    SQLiteOpenHelper(context, dbPath, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 3
        const val TAG = "V07Database"
        const val DEFAULT_DIR_NAME = "V07Data"
        const val DB_FILE_NAME = "v07.db"

        @Volatile
        private var instance: V07Database? = null

        fun ensureDatabaseSchema(db: SQLiteDatabase) {
            ensureAllTables(db)
        }

        fun ensureAllTables(db: SQLiteDatabase) {
            // 1. Core Module: Session Timer (Deep Work & Focus Sessions)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS sessions (
                    id TEXT PRIMARY KEY,
                    activity TEXT NOT NULL,
                    start_time INTEGER NOT NULL,
                    end_time INTEGER,
                    duration_sec INTEGER NOT NULL,
                    notes TEXT
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sessions_start ON sessions(start_time);")

            // 2. Core Module: Tasks Discipline & Priorities
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    status TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    completed_at INTEGER,
                    deadline TEXT
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status, priority);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_created ON tasks(created_at);")

            // 3. Core Module: Thoughts & Reflections
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS thoughts (
                    id TEXT PRIMARY KEY,
                    timestamp INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    topic TEXT,
                    content TEXT NOT NULL,
                    pinned INTEGER NOT NULL DEFAULT 0,
                    tags TEXT
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_thoughts_date ON thoughts(date);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_thoughts_pinned ON thoughts(pinned, timestamp);")

            // 4. Core Module: Screen Time & Passive Attention Telemetry
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS app_usage (
                    id TEXT PRIMARY KEY,
                    package_name TEXT NOT NULL,
                    app_name TEXT NOT NULL,
                    start_time INTEGER NOT NULL,
                    end_time INTEGER NOT NULL,
                    duration_ms INTEGER NOT NULL,
                    category TEXT NOT NULL
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_app_usage_time ON app_usage(start_time, end_time);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_app_usage_pkg ON app_usage(package_name);")

            // 5. Core Utility 1: Day Tracker (Milestones, Targets & Countdowns)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS day_tracker (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    target_date TEXT NOT NULL,
                    hour INTEGER DEFAULT 0,
                    pinned INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_day_tracker_date ON day_tracker(target_date);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_day_tracker_pinned ON day_tracker(pinned);")

            // 6. Core Utility 2: Recycle Bin (Soft-Delete & Item Recovery)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS recycle_bin (
                    recycle_id TEXT PRIMARY KEY,
                    original_id TEXT NOT NULL,
                    category TEXT NOT NULL,
                    data_json TEXT NOT NULL,
                    deleted_at INTEGER NOT NULL
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recycle_bin_time ON recycle_bin(deleted_at);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recycle_bin_cat ON recycle_bin(category);")

            // 7. Plugin Platform Storage (Isolated key-value partition per plugin ID)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS plugin_storage (
                    plugin_id TEXT NOT NULL,
                    key TEXT NOT NULL,
                    value_json TEXT NOT NULL,
                    updated_at INTEGER NOT NULL,
                    PRIMARY KEY (plugin_id, key)
                );
            """.trimIndent())

            // 8. Core Cadence & Life OS Events (Check-Ins, Check-Outs, Wins, Lessons, Micro-Captures)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS events (
                    id TEXT PRIMARY KEY,
                    category TEXT NOT NULL,
                    date TEXT NOT NULL,
                    ts TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    title TEXT,
                    content TEXT,
                    score INTEGER DEFAULT 0,
                    metadata_json TEXT
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_events_date ON events(date);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_events_cat ON events(category);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_events_ts ON events(timestamp);")

            // 9. Permanent Tombstones (Prevent cloud sync/import resurrections of deleted records)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS deleted_tombstones (
                    id TEXT PRIMARY KEY,
                    category TEXT,
                    deleted_at INTEGER NOT NULL
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_deleted_tombstones_id ON deleted_tombstones(id);")

            // 10. Plugin Platform Events (Domain records emitted by sandboxed plugins)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS plugin_events (
                    id TEXT PRIMARY KEY,
                    plugin_id TEXT NOT NULL,
                    category TEXT NOT NULL,
                    payload_json TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    date TEXT NOT NULL
                );
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_plugin_events_pid ON plugin_events(plugin_id);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_plugin_events_date ON plugin_events(date);")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_plugin_events_ts ON plugin_events(timestamp);")

            // 11. Sync Watermarks (Delta tracking for partitioned cloud synchronization)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS sync_watermarks (
                    collection_name TEXT PRIMARY KEY,
                    last_sync_timestamp INTEGER NOT NULL
                );
            """.trimIndent())

            // Schema evolution: ensure tasks table has deadline column on existing databases
            try {
                db.execSQL("ALTER TABLE tasks ADD COLUMN deadline TEXT;")
            } catch (_: Exception) {
                // Column already present
            }
        }

        private fun isDirectoryWritable(dir: File): Boolean {
            return try {
                if (!dir.exists()) {
                    if (!dir.mkdirs()) return false
                }
                val testFile = File(dir, ".v3_write_test_${System.currentTimeMillis()}")
                if (testFile.createNewFile()) {
                    testFile.delete()
                    true
                } else {
                    false
                }
            } catch (_: Throwable) {
                false
            }
        }

        const val DEFAULT_PROFILE_ID = "guest"
        private const val PREFS_NAME = "v07_profiles"
        private const val KEY_ACTIVE_PROFILE = "active_profile_id"

        @Volatile
        private var currentProfileId: String = DEFAULT_PROFILE_ID

        fun getCurrentProfileId(): String = currentProfileId

        fun sanitizeProfileId(profileId: String): String {
            val clean = profileId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return if (clean.isBlank()) DEFAULT_PROFILE_ID else clean
        }

        fun resolveDatabaseDir(context: Context): File {
            // Priority 1: Private Sandboxed Internal Storage (OWASP M1 & M2 compliant)
            // Isolated by Linux UID; inaccessible to other applications and malware
            val internalDir = File(context.filesDir, DEFAULT_DIR_NAME)
            if (!internalDir.exists()) internalDir.mkdirs()

            // Auto-migration check: If internalDir has no database files yet, check for legacy external files
            val internalFiles = internalDir.listFiles { _, name -> name.endsWith(".db") }
            if (internalFiles.isNullOrEmpty()) {
                migrateLegacyStorageIfFound(context, internalDir)
            }

            return internalDir
        }

        private fun migrateLegacyStorageIfFound(context: Context, targetDir: File) {
            try {
                // Check app-specific external storage first
                val candidates = mutableListOf<File>()
                context.getExternalFilesDir(null)?.let { extDir ->
                    candidates.add(File(extDir, DEFAULT_DIR_NAME))
                    candidates.add(extDir)
                }
                try {
                    candidates.add(File(Environment.getExternalStorageDirectory(), DEFAULT_DIR_NAME))
                } catch (_: Exception) {}

                for (candDir in candidates) {
                    if (candDir.exists() && candDir.isDirectory) {
                        val dbFiles = candDir.listFiles { _, name -> name.endsWith(".db") || name.endsWith(".db-wal") || name.endsWith(".db-shm") }
                        if (!dbFiles.isNullOrEmpty()) {
                            Log.i(TAG, "Migrating ${dbFiles.size} database files from legacy ${candDir.absolutePath} to secured internal storage...")
                            for (dbFile in dbFiles) {
                                val dest = File(targetDir, dbFile.name)
                                if (!dest.exists()) {
                                    dbFile.copyTo(dest, overwrite = true)
                                }
                            }
                            break
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Legacy database auto-migration skipped or failed: ${t.message}")
            }
        }

        fun resolveDatabaseFile(context: Context, profileId: String = currentProfileId): File {
            val dir = resolveDatabaseDir(context)
            val cleanId = sanitizeProfileId(profileId)
            val profileFile = File(dir, "v07_profile_${cleanId}.db")

            // Safe backward compatibility migration:
            // If v07_profile_guest.db does not exist yet, but legacy v07.db exists, rename it so data is preserved.
            if (cleanId == DEFAULT_PROFILE_ID && !profileFile.exists()) {
                val legacyFile = File(dir, DB_FILE_NAME)
                if (legacyFile.exists() && legacyFile.length() > 0) {
                    try {
                        Log.i(TAG, "Migrating legacy v07.db to v07_profile_guest.db...")
                        legacyFile.renameTo(profileFile)
                        val legacyWal = File(dir, "$DB_FILE_NAME-wal")
                        if (legacyWal.exists()) legacyWal.renameTo(File(dir, "v07_profile_guest.db-wal"))
                        val legacyShm = File(dir, "$DB_FILE_NAME-shm")
                        if (legacyShm.exists()) legacyShm.renameTo(File(dir, "v07_profile_guest.db-shm"))
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to migrate legacy v07.db to profile DB: ${e.message}")
                    }
                }
            }

            if (cleanId == DEFAULT_PROFILE_ID && profileFile.exists()) {
                mergeUserProfilesIntoGuest(context, dir, profileFile)
            }
            return profileFile
        }

        private fun mergeUserProfilesIntoGuest(context: Context, dir: File, guestFile: File) {
            try {
                val userFiles = dir.listFiles { _, name ->
                    name.startsWith("v07_profile_") && name.endsWith(".db") && name != "v07_profile_guest.db" && !name.endsWith(".merged")
                }
                if (userFiles.isNullOrEmpty()) return
                val guestDb = SQLiteDatabase.openOrCreateDatabase(guestFile, null)
                guestDb.enableWriteAheadLogging()
                ensureAllTables(guestDb)

                for (uFile in userFiles) {
                    try {
                        Log.i(TAG, "Consolidating user profile db into single sovereign vault: ${uFile.name}")
                        val uDb = SQLiteDatabase.openDatabase(uFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

                        // Copy sessions
                        try {
                            val c = uDb.rawQuery("SELECT id, activity, start_time, end_time, duration_sec, notes FROM sessions", null)
                            c.use {
                                while (it.moveToNext()) {
                                    val cv = ContentValues().apply {
                                        put("id", it.getString(0))
                                        put("activity", it.getString(1))
                                        put("start_time", it.getLong(2))
                                        if (!it.isNull(3)) put("end_time", it.getLong(3))
                                        put("duration_sec", it.getInt(4))
                                        put("notes", it.getString(5))
                                    }
                                    guestDb.insertWithOnConflict("sessions", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                                }
                            }
                        } catch (_: Exception) {}

                        // Copy tasks
                        try {
                            val c = uDb.rawQuery("SELECT id, title, priority, status, created_at, completed_at, deadline FROM tasks", null)
                            c.use {
                                while (it.moveToNext()) {
                                    val cv = ContentValues().apply {
                                        put("id", it.getString(0))
                                        put("title", it.getString(1))
                                        put("priority", it.getString(2))
                                        put("status", it.getString(3))
                                        put("created_at", it.getLong(4))
                                        if (!it.isNull(5)) put("completed_at", it.getLong(5))
                                        put("deadline", it.getString(6))
                                    }
                                    guestDb.insertWithOnConflict("tasks", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                                }
                            }
                        } catch (_: Exception) {}

                        // Copy thoughts
                        try {
                            val c = uDb.rawQuery("SELECT id, timestamp, date, topic, content, pinned, tags FROM thoughts", null)
                            c.use {
                                while (it.moveToNext()) {
                                    val cv = ContentValues().apply {
                                        put("id", it.getString(0))
                                        put("timestamp", it.getLong(1))
                                        put("date", it.getString(2))
                                        put("topic", it.getString(3))
                                        put("content", it.getString(4))
                                        put("pinned", it.getInt(5))
                                        put("tags", it.getString(6))
                                    }
                                    guestDb.insertWithOnConflict("thoughts", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                                }
                            }
                        } catch (_: Exception) {}

                        // Copy events
                        try {
                            val c = uDb.rawQuery("SELECT id, category, date, ts, timestamp, title, content, score, metadata_json FROM events", null)
                            c.use {
                                while (it.moveToNext()) {
                                    val cv = ContentValues().apply {
                                        put("id", it.getString(0))
                                        put("category", it.getString(1))
                                        put("date", it.getString(2))
                                        put("ts", it.getString(3))
                                        put("timestamp", it.getLong(4))
                                        put("title", it.getString(5))
                                        put("content", it.getString(6))
                                        put("score", it.getInt(7))
                                        put("metadata_json", it.getString(8))
                                    }
                                    guestDb.insertWithOnConflict("events", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                                }
                            }
                        } catch (_: Exception) {}

                        // Copy day_tracker
                        try {
                            val c = uDb.rawQuery("SELECT id, title, target_date, hour, pinned, created_at FROM day_tracker", null)
                            c.use {
                                while (it.moveToNext()) {
                                    val cv = ContentValues().apply {
                                        put("id", it.getString(0))
                                        put("title", it.getString(1))
                                        put("target_date", it.getString(2))
                                        put("hour", it.getInt(3))
                                        put("pinned", it.getInt(4))
                                        put("created_at", it.getLong(5))
                                    }
                                    guestDb.insertWithOnConflict("day_tracker", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                                }
                            }
                        } catch (_: Exception) {}

                        // Copy plugin_storage
                        try {
                            val c = uDb.rawQuery("SELECT plugin_id, key, value_json, updated_at FROM plugin_storage", null)
                            c.use {
                                while (it.moveToNext()) {
                                    val cv = ContentValues().apply {
                                        put("plugin_id", it.getString(0))
                                        put("key", it.getString(1))
                                        put("value_json", it.getString(2))
                                        put("updated_at", it.getLong(3))
                                    }
                                    guestDb.insertWithOnConflict("plugin_storage", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                                }
                            }
                        } catch (_: Exception) {}

                        uDb.close()

                        // Mark as merged so it is never re-processed
                        val targetMerged = File(dir, "${uFile.name}.merged")
                        uFile.renameTo(targetMerged)
                        File(dir, "${uFile.name}-wal").delete()
                        File(dir, "${uFile.name}-shm").delete()
                        File(dir, "${uFile.name}-journal").delete()
                        Log.i(TAG, "Successfully consolidated and archived ${uFile.name} into guest vault")
                    } catch (e: Exception) {
                        Log.w(TAG, "Error consolidating ${uFile.name}: ${e.message}")
                    }
                }
                guestDb.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error in mergeUserProfilesIntoGuest: ${e.message}")
            }
        }

        fun listProfiles(context: Context): List<String> {
            return listOf(DEFAULT_PROFILE_ID)
        }

        @Synchronized
        fun switchProfile(context: Context, newProfileId: String): Boolean {
            // Strictly single-profile operational model: Always maintain DEFAULT_PROFILE_ID
            currentProfileId = DEFAULT_PROFILE_ID
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ACTIVE_PROFILE, DEFAULT_PROFILE_ID).apply()
            return true
        }

        fun createProfile(context: Context, profileId: String): Boolean {
            val cleanId = sanitizeProfileId(profileId)
            val dir = resolveDatabaseDir(context)
            val dbFile = File(dir, "v07_profile_${cleanId}.db")
            if (dbFile.exists()) return true
            return try {
                val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
                db.enableWriteAheadLogging()
                ensureAllTables(db)
                db.close()
                Log.i(TAG, "Created new profile database: $cleanId")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create profile database $cleanId: ${e.message}", e)
                false
            }
        }

        fun cloneProfile(context: Context, sourceId: String, targetId: String): Boolean {
            val cleanSource = sanitizeProfileId(sourceId)
            val cleanTarget = sanitizeProfileId(targetId)
            if (cleanSource == cleanTarget) return false
            val dir = resolveDatabaseDir(context)
            val srcFile = File(dir, "v07_profile_${cleanSource}.db")
            if (!srcFile.exists()) return false
            val targetFile = File(dir, "v07_profile_${cleanTarget}.db")
            return try {
                if (cleanSource == currentProfileId && instance != null) {
                    val db = instance!!.writableDatabase
                    val cursor = db.rawQuery("PRAGMA wal_checkpoint(FULL);", null)
                    cursor.use { it.moveToFirst() }
                }
                FileInputStream(srcFile).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.i(TAG, "Cloned profile $cleanSource to $cleanTarget")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clone profile $cleanSource to $cleanTarget: ${e.message}", e)
                false
            }
        }

        fun deleteProfile(context: Context, profileId: String): Boolean {
            val cleanId = sanitizeProfileId(profileId)
            if (cleanId == currentProfileId) {
                Log.w(TAG, "Cannot delete currently active profile ($cleanId)")
                return false
            }
            if (cleanId == DEFAULT_PROFILE_ID) {
                Log.w(TAG, "Cannot delete default guest profile")
                return false
            }
            val dir = resolveDatabaseDir(context)
            val dbFile = File(dir, "v07_profile_${cleanId}.db")
            val walFile = File(dir, "v07_profile_${cleanId}.db-wal")
            val shmFile = File(dir, "v07_profile_${cleanId}.db-shm")
            var ok = true
            if (dbFile.exists()) ok = ok && dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()
            return ok
        }

        fun getInstance(context: Context): V07Database {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putString(KEY_ACTIVE_PROFILE, DEFAULT_PROFILE_ID).apply()
                    currentProfileId = DEFAULT_PROFILE_ID
                    val dbFile = resolveDatabaseFile(context, currentProfileId)
                    Log.i(TAG, "Initializing V3 operational database for profile '$currentProfileId' at: ${dbFile.absolutePath}")
                    V07Database(context.applicationContext, dbFile.absolutePath).also {
                        instance = it
                    }
                }
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Enable Write-Ahead Logging for high-concurrency non-blocking reads/writes
        db.enableWriteAheadLogging()
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.i(TAG, "Creating V3 operational schema for 4 Core Modules + 3 Core Utilities + Plugin Platform...")
        ensureAllTables(db)
        Log.i(TAG, "V3 operational schema created successfully.")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        ensureAllTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "Upgrading database from version $oldVersion to $newVersion")
        ensureAllTables(db)
    }
}
