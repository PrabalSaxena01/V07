package com.v07.v3.core.data.repo

import android.database.sqlite.SQLiteDatabase
import com.v07.v3.core.data.V07Database

/**
 * Base repository providing database access and common query helpers.
 */
abstract class BaseRepository(protected val dbHelper: V07Database) {

    fun getSafeExcludeClause(db: SQLiteDatabase? = null): String {
        val targetDb = db ?: try { dbHelper.readableDatabase } catch (_: Exception) { null }
        if (targetDb == null) return SAFE_EXCLUDE_CLAUSE
        return try {
            targetDb.rawQuery("SELECT 1 FROM deleted_tombstones LIMIT 0", null).use { }
            SAFE_EXCLUDE_CLAUSE
        } catch (_: Exception) {
            try {
                V07Database.ensureAllTables(targetDb)
                SAFE_EXCLUDE_CLAUSE
            } catch (_: Exception) {
                "id NOT IN (SELECT original_id FROM recycle_bin WHERE original_id IS NOT NULL AND original_id != '')"
            }
        }
    }

    companion object {
        const val SAFE_EXCLUDE_CLAUSE = "id NOT IN (SELECT original_id FROM recycle_bin WHERE original_id IS NOT NULL AND original_id != '') AND id NOT IN (SELECT id FROM deleted_tombstones WHERE id IS NOT NULL AND id != '')"
    }
}
