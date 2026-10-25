package com.v07.v3.app.server.routes

import android.content.Context
import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.core.data.DatabaseManager
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.LegacyDataImporter
import com.v07.v3.core.sync.V3CompanionSyncManager
import org.json.JSONArray
import org.json.JSONObject

class AdminSubRouter(
    private val context: Context,
    private val repository: EventRepository,
    private val databaseManager: DatabaseManager,
    private val syncManager: V3CompanionSyncManager
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/v3/admin") ||
                path.startsWith("/api/v3/recyclebin") ||
                path.startsWith("/recycle-bin") ||
                path.startsWith("/recyclebin") ||
                path.startsWith("/api/v3/database") ||
                path.startsWith("/api/v3/companion")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/api/v3/admin") -> handleAdmin(method, path, request)
            path.startsWith("/api/v3/recyclebin") || path.startsWith("/recycle-bin") || path.startsWith("/recyclebin") ->
                handleRecycleBin(method, path, request)
            path.startsWith("/api/v3/database") -> handleDatabase(method, path, request)
            path.startsWith("/api/v3/companion") -> handleCompanion(method, path, request)
            else -> HttpResponse.error("Endpoint not found: $path", 404)
        }
    }

    private fun handleRecycleBin(method: String, path: String, request: HttpRequest): HttpResponse {
        when (method) {
            "GET" -> {
                val category = request.queryParams["category"]
                val items = repository.getRecycleBinItems(200)
                val arr = JSONArray()
                val isoFmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                items.forEach { r ->
                    if (category.isNullOrBlank() || r.category.equals(category, ignoreCase = true)) {
                        val parsedData = try { JSONObject(r.dataJson) } catch (_: Exception) { JSONObject() }
                        val iso = try { isoFmt.format(java.util.Date(r.deletedAt)) } catch (_: Exception) { r.deletedAt.toString() }
                        arr.put(JSONObject().apply {
                            put("recycle_id", r.recycleId)
                            put("original_id", r.originalId)
                            put("category", r.category)
                            put("data_json", r.dataJson)
                            put("data", parsedData)
                            put("deleted_at", iso)
                            put("deleted_timestamp", r.deletedAt)
                        })
                    }
                }
                return HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("items", arr)
                    put("entries", arr)
                }.toString())
            }
            "POST" -> {
                if (path == "/recycle-bin/clear" || path == "/recyclebin/clear") {
                    val purged = repository.emptyRecycleBin()
                    purged.forEach { syncManager.deleteRemoteEvent(it) }
                    return HttpResponse.json(JSONObject().put("ok", true).put("cleared_count", purged.size).toString())
                }

                val body = JSONObject(request.body.ifBlank { "{}" })
                val pathId = if (path.contains("/restore")) {
                    path.removePrefix("/recycle-bin/").removePrefix("/recyclebin/").removeSuffix("/restore").trim('/')
                } else null
                val recycleId = body.optString("recycle_id", pathId ?: "").trim()
                if (recycleId.isBlank()) return HttpResponse.error("recycle_id cannot be blank", 400)

                val restored = repository.restoreRecycleItem(recycleId)
                val ok: Boolean = (restored != null)
                return HttpResponse.json(JSONObject().put("ok", ok).put("restored", recycleId).toString())
            }
            "DELETE" -> {
                val pathSegment = path.removePrefix("/recycle-bin/").removePrefix("/recyclebin/").removePrefix("/api/v3/recyclebin/").trim('/')
                val isClearAll = path.endsWith("/clear") || (request.queryParams["recycle_id"].isNullOrBlank() && (pathSegment.isBlank() || pathSegment == "clear"))
                val ok: Boolean = if (isClearAll) {
                    val purged = repository.emptyRecycleBin()
                    purged.forEach { syncManager.deleteRemoteEvent(it) }
                    true
                } else {
                    val targetId = request.queryParams["recycle_id"] ?: pathSegment
                    val purgedId = repository.purgeRecycleItem(targetId)
                    if (purgedId != null) {
                        syncManager.deleteRemoteEvent(purgedId)
                        true
                    } else {
                        false
                    }
                }
                return HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            else -> return HttpResponse.error("Method $method not allowed for recyclebin", 405)
        }
    }

    private fun handleDatabase(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path == "/api/v3/database/stats" && method == "GET" -> {
                val stats = databaseManager.getStats()
                val tableArr = JSONArray()
                for ((tbl, cnt) in stats.tableCounts) {
                    tableArr.put(JSONObject().put("table", tbl).put("count", cnt))
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("db_path", stats.dbPath)
                    put("db_size_bytes", stats.fileSizeBytes)
                    put("db_size_formatted", "${stats.fileSizeBytes / 1024} KB")
                    put("is_wal_enabled", stats.isWalEnabled)
                    put("tables", tableArr)
                }.toString())
            }
            path == "/api/v3/database/vacuum" && method == "POST" -> {
                val ok: Boolean = databaseManager.vacuum()
                HttpResponse.json(JSONObject().put("ok", ok).put("action", "vacuum").toString())
            }
            path == "/api/v3/database/purge-all" && method == "POST" -> {
                val ok: Boolean = repository.purgeAllData(keepPluginConfigs = false)
                databaseManager.clearAppCache()
                HttpResponse.json(JSONObject().put("ok", ok).put("action", "purged_all").toString())
            }
            path == "/api/v3/database/backup" && method == "POST" -> {
                val backupFile = databaseManager.createBackup()
                if (backupFile != null) {
                    HttpResponse.json(JSONObject().apply {
                        put("ok", true)
                        put("backup_path", backupFile.absolutePath)
                        put("file_size_bytes", backupFile.length())
                    }.toString(), 201)
                } else {
                    HttpResponse.error("Failed to create database backup", 500)
                }
            }
            path == "/api/v3/database/import-legacy" && method == "POST" -> {
                val importer = LegacyDataImporter(context)
                val summary = importer.importAllLegacyData()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("sessions_imported", summary.sessionsCount)
                    put("tasks_imported", summary.tasksCount)
                    put("thoughts_imported", summary.thoughtsCount)
                    put("day_tracker_imported", summary.dayTrackerCount)
                    put("recycle_bin_imported", summary.recycleBinCount)
                    put("habits_imported", summary.habitsMigratedToPlugin)
                    put("running_imported", summary.runningMigratedToPlugin)
                    put("vitals_imported", summary.vitalsMigratedToPlugin)
                    put("skipped_junk_files", summary.skippedJunkFiles)
                    put("error_lines", summary.errorLines)
                }.toString())
            }
            path == "/api/v3/database/profiles" && method == "GET" -> {
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("profiles", JSONArray(databaseManager.listProfiles()))
                }.toString())
            }
            path == "/api/v3/database/profile/switch" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val target = body.optString("profileId", body.optString("profile", "guest")).trim()
                val ok = syncManager.switchProfile(target)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", JSONArray(syncManager.listProfiles()))
                }.toString())
            }
            path == "/api/v3/database/integrity" && (method == "GET" || method == "POST") -> {
                val res = databaseManager.checkIntegrity()
                HttpResponse.json(JSONObject().put("ok", true).put("integrity", res).toString())
            }
            path == "/api/v3/database/reindex" && method == "POST" -> {
                val ok = databaseManager.reindex()
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            else -> HttpResponse.error("Database endpoint not found: $path", 404)
        }
    }

    private fun handleCompanion(method: String, path: String, request: HttpRequest): HttpResponse {
        val clean = path.removeSuffix("/")
        return when {
            clean == "/api/v3/companion/status" && method == "GET" -> {
                HttpResponse.json(JSONObject().put("ok", true).put("status", syncManager.getSyncStatus()).toString())
            }
            clean == "/api/v3/companion/sync" && method == "POST" -> {
                var syncOk = false
                var syncErr: String? = null
                val latch = java.util.concurrent.CountDownLatch(1)
                syncManager.syncNow { ok, err ->
                    syncOk = ok
                    syncErr = err
                    latch.countDown()
                }
                latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
                HttpResponse.json(JSONObject().apply {
                    put("ok", syncOk)
                    if (syncErr != null) put("error", syncErr)
                    put("status", syncManager.getSyncStatus())
                }.toString())
            }
            clean == "/api/v3/companion/auth" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val action = body.optString("action", "signin")
                if (action == "signout") {
                    syncManager.signOut()
                    HttpResponse.json(JSONObject().put("ok", true).put("status", syncManager.getSyncStatus()).toString())
                } else {
                    val email = body.optString("email")
                    val pass = body.optString("password")
                    val createMode = body.optBoolean("createMode", false)
                    var signOk = false
                    var signErr: String? = null
                    val latch = java.util.concurrent.CountDownLatch(1)
                    syncManager.signIn(email, pass, createMode) { ok, err ->
                        signOk = ok
                        signErr = err
                        latch.countDown()
                    }
                    latch.await(10, java.util.concurrent.TimeUnit.SECONDS)
                    if (signOk) {
                        HttpResponse.json(JSONObject().put("ok", true).put("status", syncManager.getSyncStatus()).toString())
                    } else {
                        HttpResponse.error(signErr ?: "Authentication failed", 400)
                    }
                }
            }
            clean == "/api/v3/companion/profiles" && method == "GET" -> {
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("available_profiles", JSONArray(syncManager.listProfiles()))
                }.toString())
            }
            clean == "/api/v3/companion/profile/switch" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val target = body.optString("profileId", body.optString("profile", "guest")).trim()
                val ok = syncManager.switchProfile(target)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("available_profiles", JSONArray(syncManager.listProfiles()))
                }.toString())
            }
            else -> HttpResponse.error("Companion endpoint not found: $path", 404)
        }
    }

    private fun handleAdmin(method: String, path: String, request: HttpRequest): HttpResponse {
        val clean = path.removeSuffix("/")
        return when {
            // ── Admin Profiles ──
            clean == "/api/v3/admin/profiles" && method == "GET" -> {
                val profiles = databaseManager.listProfilesWithStats()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("profiles", profiles)
                }.toString())
            }
            clean == "/api/v3/admin/profiles/switch" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val target = body.optString("profileId", body.optString("profile", "guest")).trim()
                val ok = syncManager.switchProfile(target)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString())
            }
            clean == "/api/v3/admin/profiles/create" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val profileId = body.optString("profileId", "").trim()
                if (profileId.isBlank()) return HttpResponse.error("profileId cannot be blank", 400)
                val ok = databaseManager.createProfile(profileId)
                if (body.optBoolean("switchAfterCreate", true)) {
                    syncManager.switchProfile(profileId)
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("created_profile", profileId)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString(), if (ok) 201 else 500)
            }
            clean == "/api/v3/admin/profiles/clone" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val sourceId = body.optString("sourceId", "").trim()
                val targetId = body.optString("targetId", "").trim()
                if (sourceId.isBlank() || targetId.isBlank()) return HttpResponse.error("sourceId and targetId required", 400)
                val ok = databaseManager.cloneProfile(sourceId, targetId)
                if (body.optBoolean("switchAfterClone", false)) {
                    syncManager.switchProfile(targetId)
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("cloned_profile", targetId)
                    put("active_profile", syncManager.getActiveProfileId())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString())
            }
            clean == "/api/v3/admin/profiles" && method == "DELETE" -> {
                val profileId = request.queryParams["profileId"] ?: return HttpResponse.error("Missing profileId parameter", 400)
                if (profileId == databaseManager.getActiveProfile()) {
                    return HttpResponse.error("Cannot delete the currently active profile", 400)
                }
                if (profileId == "guest") {
                    return HttpResponse.error("Cannot delete default guest profile", 400)
                }
                val ok = databaseManager.deleteProfile(profileId)
                HttpResponse.json(JSONObject().apply {
                    put("ok", ok)
                    put("deleted_profile", profileId)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("profiles", databaseManager.listProfilesWithStats())
                }.toString())
            }

            // ── Admin Tables & Schema ──
            clean == "/api/v3/admin/tables" && method == "GET" -> {
                val tables = databaseManager.getAllTableNames()
                val arr = JSONArray()
                val db = com.v07.v3.core.data.V07Database.getInstance(context).readableDatabase
                for (t in tables) {
                    var cnt = 0L
                    try {
                        val c = db.rawQuery("SELECT COUNT(*) FROM \"$t\"", null)
                        c.use { if (it.moveToFirst()) cnt = it.getLong(0) }
                    } catch (_: Exception) {}
                    arr.put(JSONObject().apply {
                        put("name", t)
                        put("row_count", cnt)
                        put("schema", databaseManager.getTableSchema(t))
                    })
                }
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("active_profile", databaseManager.getActiveProfile())
                    put("tables", arr)
                }.toString())
            }
            clean == "/api/v3/admin/table/data" && method == "GET" -> {
                val table = request.queryParams["table"] ?: return HttpResponse.error("Missing table parameter", 400)
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 50
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val sortCol = request.queryParams["sortCol"]
                val sortDir = request.queryParams["sortDir"] ?: "DESC"
                val search = request.queryParams["search"]
                val result = databaseManager.getTableData(table, limit, offset, sortCol, sortDir, search)
                result.put("ok", true)
                HttpResponse.json(result.toString())
            }
            clean == "/api/v3/admin/table/row" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val table = body.optString("table")
                val values = body.optJSONObject("values") ?: return HttpResponse.error("Missing values object", 400)
                val ok = databaseManager.insertTableRow(table, values)
                HttpResponse.json(JSONObject().put("ok", ok).toString(), if (ok) 201 else 500)
            }
            clean == "/api/v3/admin/table/row" && method == "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val table = body.optString("table")
                val pkCol = body.optString("pkCol", "id")
                val pkVal = body.optString("pkVal")
                val values = body.optJSONObject("values") ?: return HttpResponse.error("Missing values object", 400)
                val ok = databaseManager.updateTableRow(table, pkCol, pkVal, values)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            clean == "/api/v3/admin/table/row" && method == "DELETE" -> {
                val table = request.queryParams["table"] ?: return HttpResponse.error("Missing table parameter", 400)
                val pkCol = request.queryParams["pkCol"] ?: "id"
                val pkVal = request.queryParams["pkVal"] ?: return HttpResponse.error("Missing pkVal parameter", 400)
                val ok = databaseManager.deleteTableRow(table, pkCol, pkVal)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }

            // ── Admin SQL Workbench ──
            clean == "/api/v3/admin/sql" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val sql = body.optString("sql", "").trim()
                val result = databaseManager.executeSql(sql)
                HttpResponse.json(result.toString())
            }

            // ── Admin Backups ──
            clean == "/api/v3/admin/backups" && method == "GET" -> {
                val backups = databaseManager.listBackups()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("backups", backups)
                }.toString())
            }
            clean == "/api/v3/admin/backup/restore" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val backupPath = body.optString("path", "").trim()
                if (backupPath.isBlank()) return HttpResponse.error("Backup file path required", 400)
                val ok = databaseManager.restoreBackup(backupPath)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            clean == "/api/v3/admin/cache/clear" && method == "POST" -> {
                val clearedBytes = databaseManager.clearAppCache()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("bytes_cleared", clearedBytes)
                    put("formatted", "${clearedBytes / 1024} KB")
                }.toString())
            }

            // ── Admin App Usage Logs ──
            clean == "/api/v3/admin/appusage" && method == "GET" -> {
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 100
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val logs = repository.getAppUsageLogs(limit, offset)
                val arr = JSONArray()
                logs.forEach { u ->
                    arr.put(JSONObject().apply {
                        put("id", u.id)
                        put("package_name", u.packageName)
                        put("app_name", u.appName)
                        put("start_time", u.startTime)
                        put("end_time", u.endTime)
                        put("duration_ms", u.durationMs)
                        put("duration_sec", u.durationMs / 1000)
                        put("category", u.category)
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("logs", arr).toString())
            }
            clean == "/api/v3/admin/appusage" && method == "DELETE" -> {
                val id = request.queryParams["id"]
                val ok = repository.deleteAppUsage(id)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }

            else -> HttpResponse.error("Admin endpoint not found: $path", 404)
        }
    }
}
