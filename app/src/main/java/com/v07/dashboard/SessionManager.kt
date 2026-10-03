package com.v07.dashboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object SessionManager {

    private const val TAG = "SessionManager"

    private var context: Context? = null
    private var activeSession: ActiveSession? = null
    private var lastCheckpointMs = 0L

    private const val PREFS_NAME = "session_prefs"
    private const val KEY_DAILY_TARGET = "daily_target_minutes"
    private const val DEFAULT_DAILY_TARGET = 150

    private fun prefs(): SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDailyTarget(): Int {
        return prefs()?.getInt(KEY_DAILY_TARGET, DEFAULT_DAILY_TARGET) ?: DEFAULT_DAILY_TARGET
    }

    fun setDailyTarget(minutes: Int) {
        prefs()?.edit()?.putInt(KEY_DAILY_TARGET, minutes)?.apply()
    }

    // ─── Lifecycle ──────────────────────────────────────────────────────────

    fun init(ctx: Context) {
        context = ctx.applicationContext
        recoverSession()
    }

    private fun recoverSession() {
        val saved = SessionStorage.loadActiveSession()
        if (saved != null) {
            val elapsed = elapsedAt(saved, now())
            if (elapsed < 24 * 3600_000L) {
                activeSession = saved
                Log.i(TAG, "Recovered session: ${saved.activity} (${elapsed / 60000}m)")
            } else {
                SessionStorage.deleteActiveSession()
                Log.i(TAG, "Expired session deleted (>24h)")
            }
        }
    }

    // ─── API ────────────────────────────────────────────────────────────────

    fun start(activity: String, mode: String = "countdown", targetMinutes: Int = 0, customLabel: String? = null): ActiveSession {
        if (activeSession != null && activeSession?.status == "running") {
            throw IllegalStateException("Already active")
        }
        val dailyTarget = getDailyTarget()
        val dur = if (mode == "countdown" && targetMinutes > 0) targetMinutes * 60_000L else 0L
        val session = ActiveSession(
            id = UUID.randomUUID().toString(),
            activity = activity,
            customLabel = customLabel,
            mode = mode,
            status = "running",
            startedAt = now(),
            pauseStartedAt = null,
            pausedDuration = 0L,
            targetDuration = dur,
            dailyTarget = dailyTarget
        )
        activeSession = session
        SessionStorage.saveActiveSession(session)
        try {
            appendEvent("sessions", mapOf(
                "type" to "start",
                "activity" to activity,
                "custom_label" to customLabel,
                "mode" to mode,
                "timer_mode" to mode,
                "timer_seconds" to (session.targetDuration / 1000).toInt()
            ))
        } catch (e: Exception) { Log.e(TAG, "EventStore write failed: ${e.message}") }
        broadcastChanged()
        Log.i(TAG, "Started: $activity")
        return session
    }

    fun pause() {
        val s = activeSession ?: return
        if (s.status != "running") return
        activeSession = s.copy(
            status = "paused",
            pauseStartedAt = now()
        )
        SessionStorage.saveActiveSession(activeSession!!)
        broadcastChanged()
        Log.i(TAG, "Paused: ${s.activity}")
    }

    fun resume() {
        val s = activeSession ?: return
        if (s.status != "paused" || s.pauseStartedAt == null) return
        val addedPaused = now() - s.pauseStartedAt
        activeSession = s.copy(
            status = "running",
            pauseStartedAt = null,
            pausedDuration = s.pausedDuration + addedPaused
        )
        SessionStorage.saveActiveSession(activeSession!!)
        broadcastChanged()
        Log.i(TAG, "Resumed: ${s.activity}")
    }

    fun stop(): CompletedSession? {
        val s = activeSession ?: return null
        val endTime = now()
        val workedMs = elapsedAt(s, endTime)
        val today = java.time.LocalDate.now().toString()

        val completed = CompletedSession(
            id = s.id,
            activity = s.activity,
            customLabel = s.customLabel,
            startedAt = s.startedAt,
            endedAt = endTime,
            workedMs = workedMs,
            pausedMs = s.pausedDuration,
            targetDuration = s.targetDuration,
            date = today
        )

        SessionStorage.appendSession(completed)
        SessionStorage.recordSessionComplete(completed)
        SessionStorage.deleteActiveSession()
        try {
            val workedMin = (workedMs / 60000).toInt()
            appendEvent("sessions", mapOf(
                "type" to "end",
                "activity" to s.activity,
                "custom_label" to s.customLabel,
                "duration" to workedMin,
                "timer_mode" to s.mode,
                "timer_seconds" to (s.targetDuration / 1000).toInt(),
                "started_at" to s.startedAt,
                "ended_at" to endTime
            ))
        } catch (e: Exception) { Log.e(TAG, "EventStore write failed: ${e.message}") }
        activeSession = null
        broadcastChanged()
        Log.i(TAG, "Stopped: ${s.activity} (${workedMs / 60000}m worked)")
        try {
            V07ServerService.dismissSessionNotification()
        } catch (_: Exception) {}
        return completed
    }

    // ─── Queries ────────────────────────────────────────────────────────────

    fun getSession(): ActiveSession? = activeSession

    fun isRunning(): Boolean = activeSession?.status == "running"

    fun isPaused(): Boolean = activeSession?.status == "paused"

    fun getElapsed(): Long {
        val s = activeSession ?: return 0L
        if (s.status == "paused") {
            val pausedAt = s.pauseStartedAt ?: return now() - s.startedAt - s.pausedDuration
            return pausedAt - s.startedAt - s.pausedDuration
        }
        return now() - s.startedAt - s.pausedDuration
    }

    fun getRemaining(): Long {
        val s = activeSession ?: return 0L
        if (s.targetDuration <= 0) return 0L
        return (s.targetDuration - getElapsed()).coerceAtLeast(0)
    }

    fun getProgress(): Float {
        val s = activeSession ?: return 0f
        if (s.targetDuration <= 0) return 0f
        return (getElapsed().toFloat() / s.targetDuration).coerceIn(0f, 1f)
    }

    fun getElapsedFormatted(): String = formatMs(getElapsed())

    fun getRemainingFormatted(): String = formatMs(getRemaining())

    fun getTodayWorkedMinutes(): Int {
        val s = activeSession ?: return SessionStorage.getTodayMinutesForActivity("")
        val completed = SessionStorage.getTodayMinutesForActivity(s.activity)
        val currentMs = if (s.status == "running") getElapsed() else 0L
        return completed + (currentMs / 60000).toInt()
    }

    fun getTodayProgress(): Float {
        val target = getDailyTarget()
        if (target <= 0) return 0f
        return (getTodayWorkedMinutes().toFloat() / target).coerceIn(0f, 1f)
    }

    // ─── Checkpoint (periodic save) ─────────────────────────────────────────

    fun checkpoint() {
        val s = activeSession ?: return
        val nowMs = now()
        if (nowMs - lastCheckpointMs < 30_000L) return
        lastCheckpointMs = nowMs
        SessionStorage.saveActiveSession(s)
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private fun now() = System.currentTimeMillis()

    private fun elapsedAt(s: ActiveSession, at: Long): Long {
        val pausedAt = if (s.status == "paused") s.pauseStartedAt else null
        val realAt = pausedAt ?: at
        return (realAt - s.startedAt - s.pausedDuration).coerceAtLeast(0L)
    }

    private fun broadcastChanged() {
        try {
            val ctx = context ?: return
            ctx.sendBroadcast(Intent("com.v07.dashboard.SESSION_CHANGED"))
        } catch (_: Exception) {}
    }

    private fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return when {
            h > 0 -> "${h}h ${m}m"
            m > 0 -> "${m}m ${s}s"
            else -> "${s}s"
        }
    }

    private fun formatTime(ms: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
    }

    // ─── Serialize for API ──────────────────────────────────────────────────

    fun toApiMap(): Map<String, Any?> {
        val s = activeSession
        return mapOf(
            "active" to (s != null && s.status == "running"),
            "paused" to (s != null && s.status == "paused"),
            "session" to s?.let {
                mapOf(
                    "id" to it.id,
                    "activity" to it.activity,
                    "custom_label" to it.customLabel,
                    "mode" to it.mode,
                    "status" to it.status,
                    "started_at" to it.startedAt,
                    "elapsed_ms" to getElapsed(),
                    "elapsed_minutes" to (getElapsed() / 60000).toInt(),
                    "remaining_ms" to getRemaining(),
                    "target_duration" to it.targetDuration,
                    "daily_target" to getDailyTarget(),
                    "progress" to getProgress()
                )
            },
            "today_minutes" to getTodayWorkedMinutes(),
            "today_progress" to getTodayProgress(),
            "daily_target" to getDailyTarget()
        )
    }

    fun toTodayMap(): Map<String, Any?> {
        val s = activeSession
        val completedToday = if (s != null) SessionStorage.getTodayMinutesForActivity(s.activity) else 0
        val activeMinutes = if (s != null && s.status == "running") (getElapsed() / 60000).toInt() else 0
        return mapOf(
            "total_minutes" to completedToday,
            "active" to (s != null && s.status == "running"),
            "active_minutes" to activeMinutes
        )
    }
}
