package com.v07.v3.core.plugin.handlers

import android.content.Context
import android.os.Handler
import android.util.Base64
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3DayTrackerItem
import com.v07.v3.core.plugin.PluginManifestParser
import com.v07.v3.core.plugin.PluginRegistry
import com.v07.v3.core.sync.V3CompanionSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PluginAdminBridgeHandler(
    private val context: Context,
    private val repository: EventRepository,
    private val registry: PluginRegistry,
    private val syncManager: V3CompanionSyncManager,
    private val mainHandler: Handler,
    private val activityProvider: () -> com.v07.v3.app.V3Activity?
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("timetracker:") ||
                method.startsWith("daytracker:") ||
                method.startsWith("plugin:") ||
                method.startsWith("companion:") ||
                method.startsWith("profile:") ||
                method.startsWith("recycle:")
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "timetracker:list", "daytracker:list" -> {
                val items = repository.getAllDayTrackerItems()
                val arr = JSONArray()
                items.forEach { item ->
                    arr.put(JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("targetDate", item.targetDate)
                        put("target_date", item.targetDate)
                        put("hour", item.hour)
                        put("pinned", item.pinned)
                        put("createdAt", item.createdAt)
                        put("created_at", item.createdAt)
                    })
                }
                bridgeSuccess(JSONObject().put("items", arr))
            }
            "timetracker:add", "daytracker:add" -> {
                val title = params.optString("title", "").trim()
                if (title.isBlank()) return bridgeError("Milestone title cannot be blank")
                val targetDate = params.optString("targetDate", params.optString("target_date", "")).trim()
                if (targetDate.isBlank()) return bridgeError("Target date required (YYYY-MM-DD)")
                val hour = params.optInt("hour", 0)
                val pinned = params.optBoolean("pinned", false)
                val item = V3DayTrackerItem(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    targetDate = targetDate,
                    hour = hour,
                    pinned = pinned,
                    createdAt = System.currentTimeMillis()
                )
                val ok = repository.insertDayTrackerItem(item)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", item.id))
            }
            "timetracker:togglePinned", "daytracker:togglePinned" -> {
                val id = params.optString("id", "")
                val ok = repository.toggleDayTrackerPinned(id)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", ok))
            }
            "timetracker:delete", "daytracker:delete" -> {
                val id = params.optString("id", "")
                val ok = repository.recycleItem(id, "day_tracker", "{}")
                if (ok && id.isNotBlank()) {
                    syncManager.deleteRemoteEvent(id)
                    syncManager.deleteRemoteEvent("dt_$id")
                }
                bridgeSuccess(JSONObject().put("success", ok))
            }
            "plugin:list" -> {
                val all = registry.getAllPlugins()
                val arr = JSONArray()
                all.forEach { rec ->
                    arr.put(JSONObject().apply {
                        put("id", rec.manifest.id)
                        put("name", rec.manifest.name)
                        put("version", rec.manifest.version)
                        put("description", rec.manifest.description)
                        put("publisher", rec.manifest.publisher)
                        put("icon", rec.manifest.navIcon)
                        put("color", rec.manifest.navColor)
                        put("state", rec.state.name)
                        put("isBuiltIn", rec.isBuiltIn)
                        put("permissions", JSONArray(rec.manifest.permissions))
                    })
                }
                bridgeSuccess(JSONObject().put("plugins", arr))
            }
            "plugin:enable" -> {
                val targetId = params.optString("id", "")
                val ok = registry.enablePlugin(targetId)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", targetId))
            }
            "plugin:disable" -> {
                val targetId = params.optString("id", "")
                val ok = registry.disablePlugin(targetId)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", targetId))
            }
            "plugin:register" -> {
                val manifestJson = params.optString("manifest", "")
                val manifest = PluginManifestParser.parse(manifestJson)
                if (manifest == null) {
                    return bridgeError("Invalid plugin manifest JSON")
                }
                val ok = registry.registerCustomPlugin(manifest)
                bridgeSuccess(JSONObject().put("success", ok).put("id", manifest.id))
            }
            "plugin:unregister" -> {
                val targetId = params.optString("id", "")
                val ok = registry.unregisterPlugin(targetId)
                try {
                    val pDir = File(context.filesDir, "plugins/$targetId")
                    if (pDir.exists()) pDir.deleteRecursively()
                } catch (_: Exception) {}
                bridgeSuccess(JSONObject().put("success", ok).put("id", targetId))
            }
            "plugin:importZip" -> {
                val act = activityProvider()
                if (act == null) {
                    return bridgeError("Activity not attached to bridge")
                }
                mainHandler.post {
                    act.launchZipPicker { _ -> }
                }
                bridgeSuccess(JSONObject().put("success", true).put("launched", true))
            }
            "plugin:installZipBase64" -> {
                val act = activityProvider()
                if (act == null) {
                    return bridgeError("Activity not attached to bridge")
                }
                val b64 = params.optString("data", "")
                if (b64.isBlank()) {
                    return bridgeError("No zip data provided")
                }
                val rawBytes = try {
                    val clean = if (b64.contains(",")) b64.substringAfter(",") else b64
                    Base64.decode(clean, Base64.DEFAULT)
                } catch (e: Exception) {
                    return bridgeError("Failed to decode base64: ${e.message}")
                }
                val res = act.installPluginFromZipBytes(rawBytes)
                if (res.optBoolean("success")) {
                    bridgeSuccess(res)
                } else {
                    bridgeError(res.optString("error", "Failed to install plugin zip"))
                }
            }
            "companion:getStatus" -> {
                bridgeSuccess(syncManager.getSyncStatus())
            }
            "companion:syncNow", "companion:sync" -> {
                var syncOk = false
                var syncErr: String? = null
                val latch = CountDownLatch(1)
                syncManager.syncNow { ok, err ->
                    syncOk = ok
                    syncErr = err
                    latch.countDown()
                }
                latch.await(8, TimeUnit.SECONDS)
                val res = JSONObject().apply {
                    put("success", syncOk)
                    if (syncErr != null) put("error", syncErr)
                    put("status", syncManager.getSyncStatus())
                }
                bridgeSuccess(res)
            }
            "companion:signIn", "companion:login" -> {
                val email = params.optString("email")
                val pass = params.optString("password")
                val createMode = params.optBoolean("createMode", false)
                var signOk = false
                var signErr: String? = null
                val latch = CountDownLatch(1)
                syncManager.signIn(email, pass, createMode) { ok, err ->
                    signOk = ok
                    signErr = err
                    latch.countDown()
                }
                latch.await(10, TimeUnit.SECONDS)
                if (signOk) {
                    bridgeSuccess(JSONObject().put("success", true).put("status", syncManager.getSyncStatus()))
                } else {
                    bridgeError(signErr ?: "Sign in failed")
                }
            }
            "companion:signOut" -> {
                syncManager.signOut()
                bridgeSuccess(JSONObject().put("success", true).put("status", syncManager.getSyncStatus()))
            }
            "companion:migrate", "companion:pullAll" -> {
                val targetUid = params.optString("uid", "").ifBlank { null }
                var migOk = false
                var migErr: String? = null
                var migStats: JSONObject? = null
                val latch = CountDownLatch(1)
                syncManager.migrateWebCompanionData(targetUid) { ok, err, stats ->
                    migOk = ok
                    migErr = err
                    migStats = stats
                    latch.countDown()
                }
                latch.await(25, TimeUnit.SECONDS)
                val res = JSONObject().apply {
                    put("success", migOk)
                    if (migErr != null) put("error", migErr)
                    put("stats", migStats ?: JSONObject())
                    put("syncStatus", syncManager.getSyncStatus())
                }
                bridgeSuccess(res)
            }
            "companion:setUid" -> {
                val uid = params.optString("uid", "").trim()
                if (uid.isNotBlank()) {
                    syncManager.setTargetUid(uid)
                    bridgeSuccess(JSONObject().put("success", true).put("uid", uid))
                } else {
                    bridgeError("UID cannot be blank")
                }
            }
            "companion:getTargetUid" -> {
                bridgeSuccess(JSONObject().put("success", true).put("uid", syncManager.getTargetUid()))
            }
            "companion:switchProfile", "profile:switch" -> {
                val target = params.optString("profileId", params.optString("profile", "guest")).trim()
                val ok = syncManager.switchProfile(target)
                bridgeSuccess(JSONObject().put("success", ok).put("active_profile", syncManager.getActiveProfileId()).put("profiles", JSONArray(syncManager.listProfiles())))
            }
            "companion:getProfiles", "profile:list" -> {
                bridgeSuccess(JSONObject().put("success", true).put("active_profile", syncManager.getActiveProfileId()).put("profiles", JSONArray(syncManager.listProfiles())))
            }
            "companion:getActiveProfile", "profile:getActive" -> {
                bridgeSuccess(JSONObject().put("success", true).put("active_profile", syncManager.getActiveProfileId()))
            }
            "recycle:list" -> {
                val limit = params.optInt("limit", 100)
                val items = repository.getRecycleBinItems(limit)
                val arr = JSONArray()
                items.forEach { itm ->
                    arr.put(JSONObject().apply {
                        put("recycleId", itm.recycleId)
                        put("originalId", itm.originalId)
                        put("category", itm.category)
                        put("dataJson", itm.dataJson)
                        put("deletedAt", itm.deletedAt)
                    })
                }
                val res = JSONObject().apply {
                    put("items", arr)
                    put("count", items.size)
                }
                bridgeSuccess(res)
            }
            "recycle:add", "recycle:recycleItem" -> {
                val originalId = params.optString("originalId", "")
                val category = params.optString("category", "general")
                val dataJson = params.optString("dataJson", "{}")
                val ok = repository.recycleItem(originalId, category, dataJson)
                if (ok && originalId.isNotBlank()) {
                    syncManager.deleteRemoteEvent(originalId)
                }
                bridgeSuccess(JSONObject().put("success", ok).put("originalId", originalId))
            }
            "recycle:restore" -> {
                val recycleId = params.optString("recycleId", "")
                val item = repository.restoreRecycleItem(recycleId)
                if (item != null) {
                    val obj = JSONObject().apply {
                        put("success", true)
                        put("restored", true)
                        put("recycleId", item.recycleId)
                        put("originalId", item.originalId)
                        put("category", item.category)
                        put("dataJson", item.dataJson)
                        try {
                            put("data", JSONObject(item.dataJson))
                        } catch (_: Exception) {}
                    }
                    bridgeSuccess(obj)
                } else {
                    bridgeError("Failed to restore item $recycleId")
                }
            }
            "recycle:purge" -> {
                val recycleId = params.optString("recycleId", "")
                val purgedId = repository.purgeRecycleItem(recycleId)
                if (purgedId != null) {
                    syncManager.deleteRemoteEvent(purgedId)
                }
                bridgeSuccess(JSONObject().put("success", purgedId != null).put("purgedId", purgedId ?: ""))
            }
            "recycle:empty" -> {
                val purgedIds = repository.emptyRecycleBin()
                purgedIds.forEach { pid ->
                    syncManager.deleteRemoteEvent(pid)
                }
                bridgeSuccess(JSONObject().put("success", true).put("purgedCount", purgedIds.size))
            }
            else -> null
        }
    }
}
