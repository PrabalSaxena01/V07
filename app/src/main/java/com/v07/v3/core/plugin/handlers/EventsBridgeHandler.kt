package com.v07.v3.core.plugin.handlers

import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3PluginEvent
import com.v07.v3.core.sync.V3CompanionSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EventsBridgeHandler(
    private val repository: EventRepository,
    private val syncManager: V3CompanionSyncManager
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("events:") || method.startsWith("plugin:event:")
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "events:insert" -> {
                val eventId = params.optString("id").ifBlank { "ev_" + UUID.randomUUID().toString().take(8) }
                val category = params.optString("category", "general")
                val date = params.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                val ts = params.optLong("timestamp", params.optLong("ts", System.currentTimeMillis()))
                val payload = params.optString("payloadJson", params.toString())
                val ok = repository.insertPluginEvent(
                    V3PluginEvent(
                        id = eventId,
                        pluginId = pluginId,
                        category = category,
                        payloadJson = payload,
                        timestamp = ts,
                        date = date
                    )
                )
                bridgeSuccess(JSONObject().put("success", ok).put("id", eventId))
            }
            "events:query" -> {
                val category = params.optString("category", "").ifBlank { null }
                val date = params.optString("date", "").ifBlank { null }
                val list = if (category != null) {
                    repository.getPluginEventsByCategory(pluginId, category, date)
                } else {
                    repository.getPluginEvents(pluginId, 200)
                }
                val arr = JSONArray()
                list.forEach { pe ->
                    arr.put(JSONObject().apply {
                        put("id", pe.id)
                        put("pluginId", pe.pluginId)
                        put("category", pe.category)
                        put("payloadJson", pe.payloadJson)
                        put("timestamp", pe.timestamp)
                        put("date", pe.date)
                    })
                }
                bridgeSuccess(JSONObject().put("events", arr))
            }
            "events:delete" -> {
                val eventId = params.optString("id")
                val ok = if (eventId.isNotBlank()) {
                    repository.deleteEntityEverywhere(eventId, "plugin_event")
                } else false
                bridgeSuccess(JSONObject().put("success", ok))
            }
            "plugin:event:insert" -> {
                val pId = params.optString("pluginId", pluginId).ifBlank { pluginId }
                val cat = params.optString("category", "plugin")
                val id = params.optString("id", UUID.randomUUID().toString())
                val payload = params.optString("payloadJson", params.optString("payload", "{}"))
                val now = System.currentTimeMillis()
                val date = params.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now)))
                val pe = V3PluginEvent(
                    id = id,
                    pluginId = pId,
                    category = cat,
                    payloadJson = payload,
                    timestamp = now,
                    date = date
                )
                val ok = repository.insertPluginEvent(pe)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", id))
            }
            "plugin:event:query" -> {
                val pId = params.optString("pluginId", pluginId).ifBlank { pluginId }
                val cat = params.optString("category", "").ifBlank { null }
                val date = params.optString("date", "").ifBlank { null }
                val events = if (cat != null) {
                    repository.getPluginEventsByCategory(pId, cat, date)
                } else {
                    repository.getPluginEvents(pId, 100)
                }
                val arr = JSONArray()
                events.forEach { e ->
                    arr.put(JSONObject().apply {
                        put("id", e.id)
                        put("pluginId", e.pluginId)
                        put("category", e.category)
                        put("payloadJson", e.payloadJson)
                        put("timestamp", e.timestamp)
                        put("date", e.date)
                    })
                }
                bridgeSuccess(JSONObject().put("success", true).put("events", arr))
            }
            "plugin:event:delete" -> {
                val id = params.optString("id", "")
                val ok = repository.deletePluginEvent(id)
                if (ok && id.isNotBlank()) {
                    syncManager.deleteRemoteEvent(id)
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", id))
            }
            else -> null
        }
    }
}
