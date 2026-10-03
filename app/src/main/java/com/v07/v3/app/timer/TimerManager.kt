package com.v07.v3.app.timer

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

enum class TimerMode {
    STOPWATCH,
    COUNTDOWN
}

data class ActiveSessionState(
    val id: String = UUID.randomUUID().toString(),
    val topic: String = "",
    val category: String = "Work",
    val mode: TimerMode = TimerMode.STOPWATCH,
    val status: TimerStatus = TimerStatus.IDLE,
    val startTimestampMs: Long = 0L,
    val pauseTimestampMs: Long = 0L,
    val pausedDurationMs: Long = 0L,
    val targetDurationMs: Long = 0L,
    val notes: String = ""
) {
    /**
     * Calculates active elapsed time in milliseconds.
     */
    fun getElapsedMs(now: Long = System.currentTimeMillis()): Long {
        if (startTimestampMs <= 0L) return 0L
        return when (status) {
            TimerStatus.IDLE -> 0L
            TimerStatus.RUNNING -> (now - startTimestampMs - pausedDurationMs).coerceAtLeast(0L)
            TimerStatus.PAUSED, TimerStatus.COMPLETED -> (pauseTimestampMs - startTimestampMs - pausedDurationMs).coerceAtLeast(0L)
        }
    }

    /**
     * Calculates remaining time in milliseconds (for COUNTDOWN mode).
     */
    fun getRemainingMs(now: Long = System.currentTimeMillis()): Long {
        if (mode != TimerMode.COUNTDOWN) return 0L
        val elapsed = getElapsedMs(now)
        return (targetDurationMs - elapsed).coerceAtLeast(0L)
    }

    /**
     * Formats duration as MM:SS or HH:MM:SS.
     */
    fun getFormattedTime(now: Long = System.currentTimeMillis()): String {
        val ms = if (mode == TimerMode.COUNTDOWN) getRemainingMs(now) else getElapsedMs(now)
        val totalSeconds = ms / 1000L
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

/**
 * TimerManager
 *
 * Thread-safe singleton state manager for the V3 Session Timer.
 * Maintains active session state, persists to SharedPreferences for recovery,
 * updates EventRepository upon completion, and emits updates via Kotlin StateFlow.
 */
class TimerManager private constructor(
    private val context: Context,
    private val repository: EventRepository
) {
    companion object {
        private const val TAG = "V3TimerManager"
        private const val PREFS_NAME = "v3_timer_state"

        @Volatile
        private var instance: TimerManager? = null

        fun getInstance(context: Context, repository: EventRepository): TimerManager {
            return instance ?: synchronized(this) {
                instance ?: TimerManager(context.applicationContext, repository).also {
                    instance = it
                    it.restoreActiveSession()
                }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _timerState = MutableStateFlow(ActiveSessionState())
    val timerState: StateFlow<ActiveSessionState> = _timerState.asStateFlow()

    /**
     * Starts a new session or restarts.
     */
    @Synchronized
    fun startSession(
        topic: String,
        category: String = "Work",
        mode: TimerMode = TimerMode.STOPWATCH,
        targetMinutes: Int = 0,
        notes: String = ""
    ): ActiveSessionState {
        val now = System.currentTimeMillis()
        val targetMs = if (mode == TimerMode.COUNTDOWN && targetMinutes > 0) targetMinutes * 60_000L else 0L

        val state = ActiveSessionState(
            id = UUID.randomUUID().toString(),
            topic = topic.ifBlank { "Focus Session" },
            category = category.ifBlank { "Work" },
            mode = mode,
            status = TimerStatus.RUNNING,
            startTimestampMs = now,
            pauseTimestampMs = 0L,
            pausedDurationMs = 0L,
            targetDurationMs = targetMs,
            notes = notes
        )

        _timerState.value = state
        persistState(state)
        FocusTimerService.start(context)
        Log.i(TAG, "Started session: ${state.topic} (${state.mode})")
        return state
    }

    /**
     * Pauses currently running session.
     */
    @Synchronized
    fun pauseSession(): ActiveSessionState? {
        val current = _timerState.value
        if (current.status != TimerStatus.RUNNING) return null

        val now = System.currentTimeMillis()
        val updated = current.copy(
            status = TimerStatus.PAUSED,
            pauseTimestampMs = now
        )

        _timerState.value = updated
        persistState(updated)
        FocusTimerService.updateNotification(context)
        Log.i(TAG, "Paused session: ${updated.topic}")
        return updated
    }

    /**
     * Resumes paused session.
     */
    @Synchronized
    fun resumeSession(): ActiveSessionState? {
        val current = _timerState.value
        if (current.status != TimerStatus.PAUSED) return null

        val now = System.currentTimeMillis()
        val additionalPauseMs = (now - current.pauseTimestampMs).coerceAtLeast(0L)
        val updated = current.copy(
            status = TimerStatus.RUNNING,
            pausedDurationMs = current.pausedDurationMs + additionalPauseMs,
            pauseTimestampMs = 0L
        )

        _timerState.value = updated
        persistState(updated)
        FocusTimerService.updateNotification(context)
        Log.i(TAG, "Resumed session: ${updated.topic}")
        return updated
    }

    /**
     * Finishes current session, commits to SQLite EventRepository, and clears active session.
     */
    @Synchronized
    fun finishSession(finalNotes: String? = null): V3Session? {
        val current = _timerState.value
        if (current.status == TimerStatus.IDLE) return null

        val now = System.currentTimeMillis()
        val elapsedMs = current.getElapsedMs(now)
        val durationSec = (elapsedMs / 1000L).coerceAtLeast(1L)
        val mergedNotes = finalNotes?.ifBlank { null } ?: current.notes.ifBlank { null }

        val sessionRecord = V3Session(
            id = current.id,
            activity = current.topic,
            startTime = current.startTimestampMs,
            endTime = now,
            durationSec = durationSec,
            notes = mergedNotes
        )

        repository.insertSession(sessionRecord)
        Log.i(TAG, "Committed completed session ${sessionRecord.id} ($durationSec seconds) to repository")

        val resetState = ActiveSessionState(status = TimerStatus.IDLE)
        _timerState.value = resetState
        clearPersistedState()
        FocusTimerService.stop(context)
        return sessionRecord
    }

    /**
     * Discards active session without saving to database.
     */
    @Synchronized
    fun discardSession() {
        Log.i(TAG, "Discarded session: ${_timerState.value.topic}")
        _timerState.value = ActiveSessionState(status = TimerStatus.IDLE)
        clearPersistedState()
        FocusTimerService.stop(context)
    }

    /**
     * Extends or adjusts active session duration by deltaMinutes (e.g. +5m extension).
     */
    @Synchronized
    fun addMinutes(deltaMinutes: Int): ActiveSessionState? {
        val current = _timerState.value
        if (current.status == TimerStatus.IDLE) return null
        val deltaMs = deltaMinutes * 60_000L
        val updated = if (current.mode == TimerMode.COUNTDOWN) {
            current.copy(targetDurationMs = (current.targetDurationMs + deltaMs).coerceAtLeast(60_000L))
        } else {
            current
        }
        _timerState.value = updated
        persistState(updated)
        FocusTimerService.updateNotification(context)
        Log.i(TAG, "Extended session duration by $deltaMinutes min")
        return updated
    }

    /**
     * Restores session state from SharedPreferences upon process restart.
     */
    private fun restoreActiveSession() {
        val statusStr = prefs.getString("status", null) ?: return
        try {
            val status = TimerStatus.valueOf(statusStr)
            if (status == TimerStatus.IDLE || status == TimerStatus.COMPLETED) {
                clearPersistedState()
                return
            }

            val startMs = prefs.getLong("startTimestampMs", 0L)
            val now = System.currentTimeMillis()
            // If session is older than 24 hours, discard
            if (now - startMs > 24 * 3600_000L) {
                Log.w(TAG, "Expiring stale active session (> 24 hours)")
                clearPersistedState()
                return
            }

            val restored = ActiveSessionState(
                id = prefs.getString("id", UUID.randomUUID().toString())!!,
                topic = prefs.getString("topic", "Focus Session")!!,
                category = prefs.getString("category", "Work")!!,
                mode = TimerMode.valueOf(prefs.getString("mode", TimerMode.STOPWATCH.name)!!),
                status = status,
                startTimestampMs = startMs,
                pauseTimestampMs = prefs.getLong("pauseTimestampMs", 0L),
                pausedDurationMs = prefs.getLong("pausedDurationMs", 0L),
                targetDurationMs = prefs.getLong("targetDurationMs", 0L),
                notes = prefs.getString("notes", "")!!
            )

            _timerState.value = restored
            Log.i(TAG, "Restored active session: ${restored.topic} (${restored.status})")
            if (restored.status == TimerStatus.RUNNING || restored.status == TimerStatus.PAUSED) {
                FocusTimerService.start(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore session state: ${e.message}", e)
            clearPersistedState()
        }
    }

    private fun persistState(state: ActiveSessionState) {
        prefs.edit().apply {
            putString("id", state.id)
            putString("topic", state.topic)
            putString("category", state.category)
            putString("mode", state.mode.name)
            putString("status", state.status.name)
            putLong("startTimestampMs", state.startTimestampMs)
            putLong("pauseTimestampMs", state.pauseTimestampMs)
            putLong("pausedDurationMs", state.pausedDurationMs)
            putLong("targetDurationMs", state.targetDurationMs)
            putString("notes", state.notes)
            apply()
        }
    }

    private fun clearPersistedState() {
        prefs.edit().clear().apply()
    }
}
