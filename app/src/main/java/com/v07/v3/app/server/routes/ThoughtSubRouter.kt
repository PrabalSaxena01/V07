package com.v07.v3.app.server.routes

import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Thought
import com.v07.v3.core.sync.V3CompanionSyncManager
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ThoughtSubRouter(
    private val repository: EventRepository,
    private val syncManager: V3CompanionSyncManager
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/v3/thoughts") ||
                path == "/thoughts" || path.startsWith("/thoughts?") ||
                path.startsWith("/thought") ||
                path.startsWith("/event/thought")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        if (path.startsWith("/thought/visit") && method == "POST") {
            return HttpResponse.json(JSONObject().put("ok", true).toString())
        }

        if (path.startsWith("/event/thought") && method == "DELETE") {
            val id = path.substringAfterLast("/").trim()
            if (id.isNotBlank()) {
                val recycled = repository.recycleItem(id, "thoughts", "{}")
                val deleted = repository.deleteThought(id)
                val ok = recycled || deleted
                syncManager.deleteRemoteEvent(id)
                return HttpResponse.json(JSONObject().put("ok", ok).put("recycled", recycled).toString())
            }
        }

        return when (method) {
            "GET" -> {
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 50
                val pinnedOnly = request.queryParams["pinned"]?.toBoolean() ?: false
                val thoughts = if (pinnedOnly) repository.getPinnedThoughts() else repository.getRecentThoughts(limit)

                val arr = JSONArray()
                val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                thoughts.forEach { th ->
                    val isoTs = try { isoFmt.format(Date(th.timestamp)) } catch (_: Exception) { th.date + "T00:00:00Z" }
                    arr.put(JSONObject().apply {
                        put("id", th.id)
                        put("timestamp", th.timestamp)
                        put("ts", isoTs)
                        put("date", th.date)
                        put("topic", th.topic)
                        put("content", th.content)
                        put("text", th.content)
                        put("pinned", th.pinned)
                        put("visited", true)
                        put("tags", th.tags)
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("thoughts", arr).toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val content = body.optString("content", body.optString("text")).trim()
                if (content.isBlank()) return HttpResponse.error("Thought content cannot be blank", 400)

                val topic = body.optString("topic", "General")
                val pinned = body.optBoolean("pinned", false)
                val tags = body.optString("tags", "")
                val now = System.currentTimeMillis()
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))

                val thought = V3Thought(
                    id = body.optString("id").ifBlank { UUID.randomUUID().toString() },
                    timestamp = now,
                    date = dateStr,
                    topic = topic,
                    content = content,
                    pinned = pinned,
                    tags = tags
                )
                val ok: Boolean = repository.insertThought(thought)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", thought.id).toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val id = body.optString("id").trim()
                if (id.isBlank()) return HttpResponse.error("Thought id cannot be blank", 400)

                if (body.has("toggle_pinned")) {
                    val ok: Boolean = repository.toggleThoughtPinned(id)
                    return HttpResponse.json(JSONObject().put("ok", ok).toString())
                }

                val topic = body.optString("topic", "General")
                val content = body.optString("content", body.optString("text", ""))
                val pinned = body.optBoolean("pinned", false)
                val tags = body.optString("tags", "")
                val now = body.optLong("timestamp", System.currentTimeMillis())
                val dateStr = body.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now)))

                val thought = V3Thought(
                    id = id,
                    timestamp = now,
                    date = dateStr,
                    topic = topic,
                    content = content,
                    pinned = pinned,
                    tags = tags
                )
                val ok = repository.updateThought(thought)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            "DELETE" -> {
                val id = request.queryParams["id"] ?: path.substringAfterLast("/").takeIf { it != "thoughts" } ?: return HttpResponse.error("Missing thought id", 400)
                val thought = repository.getRecentThoughts(100).find { it.id == id }
                val payload = if (thought != null) JSONObject().apply {
                    put("topic", thought.topic)
                    put("content", thought.content)
                }.toString() else "{}"

                val recycled = repository.recycleItem(id, "thoughts", payload)
                val deleted = repository.deleteThought(id)
                val ok = recycled || deleted
                syncManager.deleteRemoteEvent(id)
                HttpResponse.json(JSONObject().put("ok", ok).put("recycled", recycled).toString())
            }
            else -> HttpResponse.error("Method $method not allowed for thoughts", 405)
        }
    }
}
