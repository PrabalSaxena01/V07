package com.v07.v3.core.plugin.handlers

import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Event
import com.v07.v3.core.data.V3Task
import com.v07.v3.core.data.V3Thought
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

class TasksThoughtsBridgeHandler(
    private val repository: EventRepository,
    private val syncManager: V3CompanionSyncManager
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("tasks:") || method.startsWith("thoughts:")
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "tasks:add" -> {
                val title = params.optString("title", "").trim()
                if (title.isBlank()) {
                    return bridgeError("Task title cannot be blank")
                }
                val deadline = params.optString("deadline", "").ifBlank { null }
                val task = V3Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    priority = params.optString("priority", "MEDIUM").uppercase(),
                    status = "pending",
                    createdAt = System.currentTimeMillis(),
                    deadline = deadline
                )
                val ok = repository.insertTask(task)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().apply {
                    put("success", ok)
                    put("id", task.id)
                    put("task", JSONObject().apply {
                        put("id", task.id)
                        put("title", task.title)
                        put("priority", task.priority)
                        put("status", task.status)
                        put("done", false)
                        put("created_at", task.createdAt)
                        put("deadline", task.deadline ?: "")
                    })
                })
            }
            "tasks:toggle" -> {
                val id = params.optString("id", "")
                val ok = repository.toggleTaskStatus(id)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", id))
            }
            "tasks:delete" -> {
                val id = params.optString("id", "")
                val ok = repository.recycleItem(id, "task", "{}")
                if (ok && id.isNotBlank()) {
                    syncManager.deleteRemoteEvent(id)
                }
                bridgeSuccess(JSONObject().put("success", ok))
            }
            "tasks:update" -> {
                val id = params.optString("id", "")
                val title = params.optString("title", "").ifBlank { null }
                val priority = params.optString("priority", "").ifBlank { null }
                val deadline = params.optString("deadline", "").ifBlank { null }
                val ok = repository.updateTaskDetails(id, title, priority, deadline)
                if (ok) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", ok))
            }
            "thoughts:list" -> {
                val thoughts = repository.getRecentThoughts(50)
                val arr = JSONArray()
                thoughts.forEach { th ->
                    arr.put(JSONObject().apply {
                        put("id", th.id)
                        put("content", th.content)
                        put("date", th.date)
                        put("timestamp", th.timestamp)
                        put("topic", th.topic ?: "")
                        put("tags", th.tags ?: "")
                    })
                }
                bridgeSuccess(JSONObject().put("thoughts", arr))
            }
            "thoughts:add" -> {
                val content = (if (params.has("content")) params.optString("content", "")
                else if (params.has("text")) params.optString("text", "")
                else params.optString("note", "")).trim()
                if (content.isBlank()) {
                    return bridgeError("Content cannot be blank")
                }
                val topic = (if (params.has("topic")) params.optString("topic", "")
                else params.optString("title", "")).ifBlank { "Thought" }
                val tags = params.optString("tags", "").ifBlank { null }
                val now = System.currentTimeMillis()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = sdf.format(Date(now))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                val thoughtId = "thought_" + UUID.randomUUID().toString().take(8)

                val thought = V3Thought(
                    id = thoughtId,
                    timestamp = now,
                    date = date,
                    topic = topic,
                    content = content,
                    tags = tags
                )
                val ok = repository.insertThought(thought)

                val event = V3Event(
                    id = thoughtId,
                    category = "thought",
                    date = date,
                    ts = ts,
                    timestamp = now,
                    title = topic,
                    content = content,
                    score = 0,
                    metadataJson = if (tags != null) JSONObject().put("tags", tags).toString() else "{}"
                )
                repository.insertEvent(event)

                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", thoughtId))
            }
            "thoughts:delete" -> {
                val id = params.optString("id", "")
                val ok = repository.deleteThought(id)
                if (ok && id.isNotBlank()) {
                    syncManager.deleteRemoteEvent(id)
                }
                bridgeSuccess(JSONObject().put("success", ok))
            }
            else -> null
        }
    }
}
