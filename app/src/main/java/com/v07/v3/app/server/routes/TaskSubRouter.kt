package com.v07.v3.app.server.routes

import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Task
import com.v07.v3.core.sync.V3CompanionSyncManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TaskSubRouter(
    private val repository: EventRepository,
    private val syncManager: V3CompanionSyncManager
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/v3/tasks") ||
                path == "/tasks" || path.startsWith("/tasks?") ||
                path.startsWith("/task") ||
                path.startsWith("/checklist")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/task/") || path == "/task" -> handleTaskItemRoute(method, path, request)
            path.startsWith("/checklist") -> handleChecklistRoute(method, path, request)
            else -> handleTasks(method, request)
        }
    }

    private fun handleTasks(method: String, request: HttpRequest): HttpResponse {
        return when (method) {
            "GET" -> {
                val status = request.queryParams["status"] ?: request.queryParams["filter"] ?: "pending"
                val tasks = if (status == "all") {
                    repository.getTasks("pending") + repository.getTasks("completed")
                } else {
                    repository.getTasks(status)
                }
                val arr = JSONArray()
                tasks.forEach { t ->
                    arr.put(JSONObject().apply {
                        put("id", t.id)
                        put("title", t.title)
                        put("priority", t.priority)
                        put("status", t.status)
                        put("created_at", t.createdAt)
                        put("completed_at", t.completedAt)
                        put("deadline", t.deadline ?: "")
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("tasks", arr).toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val title = body.optString("title").trim()
                if (title.isBlank()) return HttpResponse.error("Task title cannot be blank", 400)

                val priority = body.optString("priority", "MEDIUM").uppercase()
                val deadline = body.optString("deadline", "").ifBlank { null }
                val task = V3Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    priority = priority,
                    status = "pending",
                    createdAt = System.currentTimeMillis(),
                    deadline = deadline
                )
                val ok: Boolean = repository.insertTask(task)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", task.id).toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val id = body.optString("id").trim()
                if (id.isBlank()) return HttpResponse.error("Task id cannot be blank", 400)

                if (body.has("toggle_status")) {
                    val ok: Boolean = repository.toggleTaskStatus(id)
                    return HttpResponse.json(JSONObject().put("ok", ok).toString())
                }

                val current = repository.getTasks("pending").find { it.id == id }
                    ?: repository.getTasks("completed").find { it.id == id }
                    ?: return HttpResponse.error("Task not found", 404)

                val updated = current.copy(
                    title = body.optString("title", current.title),
                    priority = body.optString("priority", current.priority).uppercase(),
                    status = body.optString("status", current.status),
                    deadline = if (body.has("deadline")) body.optString("deadline").ifBlank { null } else current.deadline
                )
                val ok: Boolean = repository.updateTask(updated)
                HttpResponse.json(JSONObject().put("ok", ok).toString())
            }
            "DELETE" -> {
                val id = request.queryParams["id"] ?: return HttpResponse.error("Missing task id", 400)
                val task = repository.getTasks("pending").find { it.id == id }
                    ?: repository.getTasks("completed").find { it.id == id }
                val payload = if (task != null) JSONObject().apply {
                    put("title", task.title)
                    put("priority", task.priority)
                    put("status", task.status)
                }.toString() else "{}"

                repository.recycleItem(id, "tasks", payload)
                val deleted: Boolean = repository.deleteTask(id)
                syncManager.deleteRemoteEvent(id)
                HttpResponse.json(JSONObject().put("ok", deleted).toString())
            }
            else -> HttpResponse.error("Method $method not allowed for tasks", 405)
        }
    }

    private fun handleChecklistRoute(method: String, path: String, request: HttpRequest): HttpResponse {
        val parts = path.split("/").filter { it.isNotBlank() }
        val id = if (parts.size >= 2 && parts[1] != "today" && parts[1] != "week") parts[1] else request.queryParams["id"] ?: ""

        return when {
            method == "DELETE" -> {
                if (id.isBlank()) return HttpResponse.error("Task id required", 400)
                val recycled = repository.recycleItem(id, "tasks", "{}")
                val deleted = repository.deleteTask(id)
                val ok = recycled || deleted
                syncManager.deleteRemoteEvent(id)
                HttpResponse.json(JSONObject().put("ok", ok).put("recycled", recycled).toString())
            }
            path.contains("/edit") && method == "POST" -> {
                if (id.isBlank()) return HttpResponse.error("Task id required", 400)
                val body = JSONObject(request.body.ifBlank { "{}" })
                val label = body.optString("label", body.optString("title")).trim()
                val ok = repository.updateTaskDetails(id, title = label)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            path.contains("/toggle") && method == "POST" -> {
                if (id.isBlank()) return HttpResponse.error("Task id required", 400)
                val ok = repository.toggleTaskStatus(id)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
            }
            method == "GET" -> {
                val pending = repository.getTasks("pending")
                val completed = repository.getTasks("completed")
                val all = pending + completed
                val arr = JSONArray()
                all.forEach { t ->
                    val isDone = t.status.equals("completed", ignoreCase = true) || t.status.equals("done", ignoreCase = true)
                    arr.put(JSONObject().apply {
                        put("id", t.id)
                        put("label", t.title)
                        put("title", t.title)
                        put("done", isDone)
                        put("status", if (isDone) "done" else "pending")
                        put("priority", t.priority)
                        put("date", t.deadline ?: "")
                        put("recurring", false)
                        put("frequency", "S")
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("items", arr).toString())
            }
            method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val label = body.optString("label", body.optString("title")).trim()
                if (label.isBlank()) return HttpResponse.error("Label required", 400)
                val task = V3Task(
                    id = body.optString("id").ifBlank { "cl_" + UUID.randomUUID().toString().take(8) },
                    title = label,
                    priority = "MEDIUM",
                    status = "pending",
                    createdAt = System.currentTimeMillis()
                )
                val ok = repository.insertTask(task)
                HttpResponse.json(JSONObject().put("ok", ok).put("id", task.id).toString(), 201)
            }
            else -> HttpResponse.error("Method $method not allowed for checklist", 405)
        }
    }

    private fun handleTaskItemRoute(method: String, path: String, request: HttpRequest): HttpResponse {
        val parts = path.split("/").filter { it.isNotBlank() }
        val id = if (parts.size >= 2) parts[1] else request.queryParams["id"] ?: ""
        val action = if (parts.size >= 3) parts[2] else ""
        if (id.isBlank()) return HttpResponse.error("Task id required", 400)

        if (method == "DELETE") {
            val recycled = repository.recycleItem(id, "tasks", "{}")
            val deleted = repository.deleteTask(id)
            val ok = recycled || deleted
            syncManager.deleteRemoteEvent(id)
            return HttpResponse.json(JSONObject().put("ok", ok).put("recycled", recycled).toString())
        }

        val allTasks = repository.getTasks("pending") + repository.getTasks("completed")
        val task = allTasks.find { it.id == id }

        return when (action) {
            "done" -> {
                if (task != null) {
                    repository.updateTask(task.copy(status = "completed", completedAt = System.currentTimeMillis()))
                } else {
                    repository.toggleTaskStatus(id)
                }
                HttpResponse.json(JSONObject().put("ok", true).toString())
            }
            "reopen" -> {
                if (task != null) {
                    repository.updateTask(task.copy(status = "pending", completedAt = null))
                } else {
                    repository.toggleTaskStatus(id)
                }
                HttpResponse.json(JSONObject().put("ok", true).toString())
            }
            else -> HttpResponse.error("Unknown task action: $action", 404)
        }
    }
}
