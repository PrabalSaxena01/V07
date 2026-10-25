package com.v07.v3.app.server.routes

import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3PluginEvent
import com.v07.v3.core.plugin.PluginRegistry
import com.v07.v3.core.plugin.PluginState
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PluginSubRouter(
    private val repository: EventRepository,
    private val pluginRegistry: PluginRegistry
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/v3/plugins") ||
                path.startsWith("/plugins") ||
                path.startsWith("/events") ||
                path.startsWith("/event")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/events") || path.startsWith("/event") -> handlePluginEvents(method, path, request)
            else -> handlePluginStorage(method, path, request)
        }
    }

    private fun handlePluginStorage(method: String, path: String, request: HttpRequest): HttpResponse {
        val cleanPath = path.removeSuffix("/")
        if (cleanPath == "/api/v3/plugins" || cleanPath == "/plugins/registry" || cleanPath == "/api/v3/plugins/refresh") {
            if (cleanPath == "/api/v3/plugins/refresh" && (method == "POST" || method == "GET")) {
                pluginRegistry.discoverAll()
            }
            if (method == "GET" || cleanPath == "/api/v3/plugins/refresh") {
                val list = JSONArray()
                pluginRegistry.getAllPlugins().forEach { p ->
                    list.put(JSONObject().apply {
                        put("id", p.manifest.id)
                        put("name", p.manifest.name)
                        put("version", p.manifest.version)
                        put("description", p.manifest.description)
                        put("state", p.state.name)
                        put("enabled", p.state == PluginState.ENABLED || p.state == PluginState.RUNNING)
                        put("is_builtin", p.isBuiltIn)
                        put("isBuiltIn", p.isBuiltIn)
                        put("nav_icon", p.manifest.navIcon)
                        put("icon", p.manifest.navIcon)
                        put("nav_label", p.manifest.navLabel)
                        put("nav_color", p.manifest.navColor)
                        put("color", p.manifest.navColor)
                        put("entry", p.manifest.entry)
                        put("settings", p.manifest.settingsEntry ?: "plugins/${p.manifest.id}/settings.html")
                        put("permissions", JSONArray(p.manifest.permissions))
                        val wArr = JSONArray()
                        p.manifest.widgets.forEach { w ->
                            wArr.put(JSONObject().apply {
                                put("id", w.id)
                                put("title", w.title)
                                put("entry", w.entry)
                                put("icon", w.icon ?: "")
                                put("color", w.color ?: "")
                                put("default_size", w.defaultSize)
                            })
                        }
                        put("widgets", wArr)
                    })
                }
                return HttpResponse.json(JSONObject().put("ok", true).put("plugins", list).toString())
            }
        }

        if (cleanPath.startsWith("/plugins/") && cleanPath.endsWith("/feature") && method == "POST") {
            val pluginId = cleanPath.removePrefix("/plugins/").removeSuffix("/feature")
            return HttpResponse.json(JSONObject().put("ok", true).put("pluginId", pluginId).toString())
        }

        if (cleanPath == "/api/v3/plugins/toggle" && method == "POST") {
            val body = JSONObject(request.body.ifBlank { "{}" })
            val id = body.optString("id")
            val enabled = body.optBoolean("enabled", true)
            val ok = if (enabled) pluginRegistry.enablePlugin(id) else pluginRegistry.disablePlugin(id)
            return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).put("enabled", enabled).toString())
        }

        // Path pattern: /api/v3/plugins/{pluginId}/data?key={key}
        val segments = path.trim('/').split('/')
        if (segments.size < 4) {
            return HttpResponse.error("Invalid plugin storage path: $path", 400)
        }
        val pluginId = segments[3]

        // Subroute: /api/v3/plugins/{pluginId}/events
        if (segments.size >= 5 && segments[4] == "events") {
            return when (method) {
                "GET" -> {
                    val category = request.queryParams["category"]
                    val date = request.queryParams["date"]
                    val list = if (!category.isNullOrBlank()) {
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
                    HttpResponse.json(JSONObject().put("ok", true).put("plugin_id", pluginId).put("events", arr).toString())
                }
                "POST", "PUT" -> {
                    val body = JSONObject(request.body.ifBlank { "{}" })
                    val id = body.optString("id").ifBlank { "ev_" + UUID.randomUUID().toString().take(8) }
                    val category = body.optString("category", "general")
                    val date = body.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    val ts = body.optLong("timestamp", body.optLong("ts", System.currentTimeMillis()))
                    val payload = body.optString("payloadJson", body.toString())
                    val ok = repository.insertPluginEvent(
                        V3PluginEvent(
                            id = id,
                            pluginId = pluginId,
                            category = category,
                            payloadJson = payload,
                            timestamp = ts,
                            date = date
                        )
                    )
                    HttpResponse.json(JSONObject().put("ok", ok).put("id", id).put("plugin_id", pluginId).toString(), if (ok) 201 else 500)
                }
                "DELETE" -> {
                    val id = request.queryParams["id"] ?: (if (segments.size >= 6) segments[5] else null)
                    if (id.isNullOrBlank()) return HttpResponse.error("Missing id parameter", 400)
                    val ok = repository.deleteEntityEverywhere(id, "plugin_event")
                    HttpResponse.json(JSONObject().put("ok", ok).put("id", id).put("plugin_id", pluginId).toString())
                }
                else -> HttpResponse.error("Method $method not allowed for plugin events", 405)
            }
        }

        return when (method) {
            "GET" -> {
                val key = request.queryParams["key"]
                if (key.isNullOrBlank()) {
                    val keys = repository.getAllPluginKeys(pluginId)
                    val obj = JSONObject()
                    for (k in keys) {
                        obj.put(k, repository.getPluginValue(pluginId, k) ?: "")
                    }
                    HttpResponse.json(JSONObject().put("ok", true).put("plugin_id", pluginId).put("data", obj).toString())
                } else {
                    val value = repository.getPluginValue(pluginId, key)
                    if (value != null) {
                        HttpResponse.json(JSONObject().put("ok", true).put("plugin_id", pluginId).put("key", key).put("value", value).toString())
                    } else {
                        HttpResponse.error("Key '$key' not found for plugin '$pluginId'", 404)
                    }
                }
            }
            "POST", "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val key = body.optString("key").trim()
                if (key.isBlank()) return HttpResponse.error("key cannot be blank", 400)
                val value = body.optString("value", "")

                val ok: Boolean = repository.setPluginValue(pluginId, key, value)
                HttpResponse.json(JSONObject().put("ok", ok).put("plugin_id", pluginId).put("key", key).toString())
            }
            "DELETE" -> {
                val key = request.queryParams["key"] ?: return HttpResponse.error("Missing key parameter", 400)
                val ok: Boolean = repository.deletePluginValue(pluginId, key)
                HttpResponse.json(JSONObject().put("ok", ok).put("plugin_id", pluginId).put("key", key).toString())
            }
            else -> HttpResponse.error("Method $method not allowed for plugin storage", 405)
        }
    }

    private fun handlePluginEvents(method: String, path: String, request: HttpRequest): HttpResponse {
        val clean = path.removePrefix("/events/").removePrefix("/events").removePrefix("/event/").removePrefix("/event")
        val category = clean.substringBefore('/').ifEmpty { "mood" }
        val pluginId = "com.v07.vitals"
        val storageKey = "events_$category"

        return when (method) {
            "GET" -> {
                val stored = repository.getPluginValue(pluginId, storageKey)
                val eventsArr = if (!stored.isNullOrBlank()) {
                    try { JSONArray(stored) } catch (_: Exception) { JSONArray() }
                } else {
                    JSONArray()
                }
                HttpResponse.json(JSONObject().put("ok", true).put("events", eventsArr).toString())
            }
            "POST" -> {
                val body = if (request.body.isNotBlank()) {
                    try { JSONObject(request.body) } catch (_: Exception) { JSONObject() }
                } else {
                    JSONObject()
                }
                val dataObj = body.optJSONObject("data") ?: body
                val now = System.currentTimeMillis()
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
                val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date(now))

                val newEvent = JSONObject().apply {
                    put("id", "v_" + UUID.randomUUID().toString().take(8))
                    put("category", category)
                    put("score", dataObj.optInt("score", dataObj.optInt("value", 3)))
                    put("value", dataObj.optInt("value", dataObj.optInt("score", 3)))
                    put("label", dataObj.optString("label", dataObj.optString("mood", "Steady")))
                    put("note", dataObj.optString("note", dataObj.optString("notes", "")))
                    put("date", dataObj.optString("date", dateStr))
                    put("ts", dataObj.optString("ts", nowIso))
                }

                val stored = repository.getPluginValue(pluginId, storageKey)
                val arr = if (!stored.isNullOrBlank()) {
                    try { JSONArray(stored) } catch (_: Exception) { JSONArray() }
                } else {
                    JSONArray()
                }
                arr.put(newEvent)
                repository.setPluginValue(pluginId, storageKey, arr.toString())

                HttpResponse.json(JSONObject().put("ok", true).put("event", newEvent).toString(), 201)
            }
            else -> HttpResponse.error("Method $method not allowed for plugin events", 405)
        }
    }
}
