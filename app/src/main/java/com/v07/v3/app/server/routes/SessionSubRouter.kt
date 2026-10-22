package com.v07.v3.app.server.routes

import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Session
import com.v07.v3.core.sync.V3CompanionSyncManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SessionSubRouter(
    private val repository: EventRepository,
    private val timerManager: TimerManager,
    private val syncManager: V3CompanionSyncManager
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/v3/sessions")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        return when (method) {
            "GET" -> {
                val limit = request.queryParams["limit"]?.toIntOrNull() ?: 50
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val sessions = repository.getRecentSessions(limit, offset)

                val arr = JSONArray()
                sessions.forEach { s ->
                    arr.put(JSONObject().apply {
                        put("id", s.id)
                        put("activity", s.activity)
                        put("start_time", s.startTime)
                        put("end_time", s.endTime)
                        put("duration_sec", s.durationSec)
                        put("notes", s.notes ?: "")
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("sessions", arr).toString())
            }
            "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                if (path.endsWith("/record") || body.optBoolean("direct_record", false) || (body.has("activity") && body.has("start_time"))) {
                    val id = body.optString("id").ifBlank { "sess_" + UUID.randomUUID().toString().take(8) }
                    val act = body.optString("activity", "Work")
                    val start = body.optLong("start_time", System.currentTimeMillis() - 1800000L)
                    val end = if (body.has("end_time") && !body.isNull("end_time")) body.optLong("end_time") else System.currentTimeMillis()
                    val dur = body.optLong("duration_sec", ((end - start) / 1000).coerceAtLeast(0))
                    val notes = body.optString("notes", "").ifBlank { null }
                    val sess = V3Session(id, act, start, end, dur, notes)
                    val ok = repository.insertSession(sess)
                    return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString(), 201)
                }

                val topic = body.optString("topic", "Focus Session")
                val category = body.optString("category", "Work")
                val modeStr = body.optString("mode", "STOPWATCH").uppercase()
                val mode = if (modeStr == "COUNTDOWN") TimerMode.COUNTDOWN else TimerMode.STOPWATCH
                val targetMinutes = body.optInt("targetMinutes", 0)
                val notes = body.optString("notes", "")

                val state = timerManager.startSession(topic, category, mode, targetMinutes, notes)
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("action", "started")
                    put("id", state.id)
                    put("topic", state.topic)
                    put("status", state.status.name)
                }.toString(), 201)
            }
            "PUT" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                if (path.endsWith("/record") || body.optString("action") == "update_record" || (body.has("id") && body.has("activity"))) {
                    val id = body.optString("id").trim()
                    if (id.isBlank()) return HttpResponse.error("Session id required", 400)
                    val act = body.optString("activity", "Work")
                    val start = body.optLong("start_time", System.currentTimeMillis() - 1800000L)
                    val end = if (body.has("end_time") && !body.isNull("end_time")) body.optLong("end_time") else System.currentTimeMillis()
                    val dur = body.optLong("duration_sec", ((end - start) / 1000).coerceAtLeast(0))
                    val notes = body.optString("notes", "").ifBlank { null }
                    val sess = V3Session(id, act, start, end, dur, notes)
                    val ok = repository.updateSession(sess)
                    return HttpResponse.json(JSONObject().put("ok", ok).put("id", id).toString())
                }

                val action = body.optString("action", "pause").lowercase()
                when (action) {
                    "pause" -> {
                        val s = timerManager.pauseSession()
                        val isOk: Boolean = s != null
                        HttpResponse.json(JSONObject().put("ok", isOk).put("status", s?.status?.name ?: "IDLE").toString())
                    }
                    "resume" -> {
                        val s = timerManager.resumeSession()
                        val isOk: Boolean = s != null
                        HttpResponse.json(JSONObject().put("ok", isOk).put("status", s?.status?.name ?: "IDLE").toString())
                    }
                    "finish" -> {
                        val notes = body.optString("notes", "").ifBlank { null }
                        val record = timerManager.finishSession(notes)
                        if (record != null) {
                            HttpResponse.json(JSONObject().put("ok", true).put("saved_session_id", record.id).put("duration_sec", record.durationSec).toString())
                        } else {
                            HttpResponse.error("No active session to finish", 400)
                        }
                    }
                    "discard" -> {
                        timerManager.discardSession()
                        HttpResponse.success("""{"ok":true,"action":"discarded"}""")
                    }
                    else -> HttpResponse.error("Unknown session control action: $action", 400)
                }
            }
            "DELETE" -> {
                val id = request.queryParams["id"] ?: return HttpResponse.error("Missing session id parameter", 400)
                val moved: Boolean = repository.recycleItem(id, "sessions", "{}")
                val deleted: Boolean = repository.deleteSession(id)
                syncManager.deleteRemoteEvent(id)
                HttpResponse.json(JSONObject().put("ok", deleted).put("recycled", moved).toString())
            }
            else -> HttpResponse.error("Method $method not allowed for sessions", 405)
        }
    }
}
