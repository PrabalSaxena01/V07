package com.v07.v3.core.plugin.handlers

import android.content.Context
import com.v07.v3.app.timer.FocusNotificationConfig
import com.v07.v3.app.timer.FocusTimerService
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Event
import com.v07.v3.core.data.V3Session
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

class TimerBridgeHandler(
    private val context: Context,
    private val repository: EventRepository,
    private val timerManager: TimerManager,
    private val syncManager: V3CompanionSyncManager
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("timer:") ||
                method.startsWith("sessions:") ||
                method == "session:delete" ||
                method == "day:timeline"
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "timer:start" -> {
                val topic = params.optString("topic", params.optString("title", "Focus Sanctuary"))
                val category = params.optString("category", "Sanctuary")
                val modeStr = params.optString("mode", "COUNTDOWN")
                val mode = if (modeStr.equals("STOPWATCH", ignoreCase = true) || modeStr.equals("timer", ignoreCase = true)) {
                    TimerMode.STOPWATCH
                } else {
                    TimerMode.COUNTDOWN
                }
                val targetMinutes = when {
                    params.has("targetMinutes") -> params.optInt("targetMinutes", 25)
                    params.has("durationSec") -> (params.optInt("durationSec", 1500) / 60).coerceAtLeast(1)
                    params.has("duration") -> (params.optInt("duration", 1500) / 60).coerceAtLeast(1)
                    else -> 25
                }
                val state = timerManager.startSession(topic, category, mode, targetMinutes)
                val res = JSONObject().apply {
                    put("success", true)
                    put("status", state.status.name)
                    put("formatted", state.getFormattedTime())
                }
                bridgeSuccess(res)
            }
            "timer:cancel", "timer:discard", "timer:stop" -> {
                timerManager.discardSession()
                val res = JSONObject().apply {
                    put("success", true)
                    put("status", "IDLE")
                }
                bridgeSuccess(res)
            }
            "timer:pause" -> {
                val state = timerManager.pauseSession()
                val res = JSONObject().apply {
                    put("success", state != null)
                    put("status", state?.status?.name ?: "IDLE")
                }
                bridgeSuccess(res)
            }
            "timer:resume" -> {
                val state = timerManager.resumeSession()
                val res = JSONObject().apply {
                    put("success", state != null)
                    put("status", state?.status?.name ?: "IDLE")
                }
                bridgeSuccess(res)
            }
            "timer:addMinutes" -> {
                val delta = params.optInt("minutes", 5)
                val state = timerManager.addMinutes(delta)
                val res = JSONObject().apply {
                    put("success", state != null)
                    if (state != null) {
                        put("remaining_ms", state.getRemainingMs())
                        put("formatted", state.getFormattedTime())
                    }
                }
                bridgeSuccess(res)
            }
            "timer:finish" -> {
                val notes = params.optString("notes", "")
                val session = timerManager.finishSession(notes.ifBlank { null })
                if (session != null) {
                    val now = session.endTime ?: System.currentTimeMillis()
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                    val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                    val durSec = session.durationSec
                    val mins = (durSec / 60L).coerceAtLeast(1L)
                    val ev = V3Event(
                        id = session.id,
                        category = "session",
                        date = date,
                        ts = ts,
                        timestamp = now,
                        title = session.activity,
                        content = "${mins}m focus session${if (!session.notes.isNullOrBlank()) " • ${session.notes}" else ""}",
                        metadataJson = JSONObject().put("durationSec", durSec).put("activity", session.activity).toString()
                    )
                    repository.insertEvent(ev)
                }
                val res = JSONObject().apply {
                    put("success", session != null)
                    if (session != null) put("id", session.id)
                }
                bridgeSuccess(res)
            }
            "timer:getState" -> {
                val state = timerManager.timerState.value
                val res = JSONObject().apply {
                    put("status", state.status.name)
                    put("topic", state.topic)
                    put("category", state.category)
                    put("mode", state.mode.name)
                    put("elapsed_ms", state.getElapsedMs())
                    put("remaining_ms", state.getRemainingMs())
                    put("formatted", state.getFormattedTime())
                }
                bridgeSuccess(res)
            }
            "timer:getNotificationConfig" -> {
                val config = FocusNotificationConfig.getInstance(context)
                bridgeSuccess(config.toJsonObject())
            }
            "timer:saveNotificationConfig" -> {
                val config = FocusNotificationConfig.getInstance(context)
                config.updateFromJson(params)
                FocusTimerService.updateNotification(context)
                bridgeSuccess(JSONObject().put("success", true).put("config", config.toJsonObject()))
            }
            "day:timeline" -> {
                val dateStr = params.optString("date", "").ifBlank {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }
                val timeline = repository.getDayTimeline(dateStr)
                val evArr = JSONArray()
                timeline.events.forEach { e ->
                    evArr.put(JSONObject().apply {
                        put("id", e.id)
                        put("timestamp", e.timestamp)
                        put("timeFormatted", e.timeFormatted)
                        put("category", e.category)
                        put("title", e.title)
                        put("subtitle", e.subtitle)
                        e.durationSec?.let { put("durationSec", it) }
                        put("isHighlight", e.isHighlight)
                    })
                }
                val res = JSONObject().apply {
                    put("date", timeline.date)
                    put("totalFocusSec", timeline.totalFocusSec)
                    put("sessionCount", timeline.sessionCount)
                    put("tasksCompletedCount", timeline.tasksCompletedCount)
                    put("thoughtsCount", timeline.thoughtsCount)
                    put("totalScreenTimeMs", timeline.totalScreenTimeMs)
                    put("events", evArr)
                }
                bridgeSuccess(res)
            }
            "sessions:list", "sessions:get" -> {
                val dateStr = params.optString("date", "").ifBlank {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }
                val timeline = repository.getDayTimeline(dateStr)
                val arr = JSONArray()
                timeline.events.filter { it.category == "sessions" || it.category == "session" }.forEach { s ->
                    arr.put(JSONObject().apply {
                        put("id", s.id)
                        put("title", s.title)
                        put("subtitle", s.subtitle)
                        put("timeFormatted", s.timeFormatted)
                        put("durationSec", s.durationSec ?: 0)
                        put("timestamp", s.timestamp)
                    })
                }
                bridgeSuccess(JSONObject().put("sessions", arr))
            }
            "sessions:add" -> {
                val act = params.optString("activity", "Deep Work")
                val durSec = params.optLong("durationSec", 1500L)
                val notes = params.optString("notes", "")
                val now = System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                val id = "ses_" + UUID.randomUUID().toString().take(8)
                val session = V3Session(
                    id = id,
                    activity = act,
                    startTime = now - (durSec * 1000),
                    endTime = now,
                    durationSec = durSec,
                    notes = notes
                )
                repository.insertSession(session)
                val event = V3Event(
                    id = id,
                    category = "session",
                    date = date,
                    ts = ts,
                    timestamp = now,
                    title = act,
                    content = "${durSec / 60}m focus session${if (notes.isNotBlank()) " • $notes" else ""}",
                    metadataJson = JSONObject().put("durationSec", durSec).put("activity", act).toString()
                )
                repository.insertEvent(event)
                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", true).put("id", id))
            }
            "sessions:delete", "session:delete" -> {
                val id = params.optString("id", "")
                val ok = repository.recycleItem(id, "session", "{}")
                if (ok && id.isNotBlank()) {
                    syncManager.deleteRemoteEvent(id)
                }
                bridgeSuccess(JSONObject().put("success", ok))
            }
            else -> null
        }
    }
}
