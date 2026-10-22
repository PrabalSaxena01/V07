package com.v07.v3.app.server.routes

import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.core.data.DatabaseManager
import org.json.JSONObject

class StatusSubRouter(
    private val timerManager: TimerManager,
    private val databaseManager: DatabaseManager,
    private val screenTimeTracker: ScreenTimeTracker
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path == "/api/v3/status"
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        val timer = timerManager.timerState.value
        val dbStats = databaseManager.getStats()

        val json = JSONObject().apply {
            put("ok", true)
            put("platform", "V07 V3")
            put("version", "3.0.0")
            // Security: session_token omitted from unauthenticated status
            put("timer", JSONObject().apply {
                put("status", timer.status.name)
                put("topic", timer.topic)
                put("category", timer.category)
                put("mode", timer.mode.name)
                put("elapsed_ms", timer.getElapsedMs())
                put("remaining_ms", timer.getRemainingMs())
                put("formatted", timer.getFormattedTime())
            })
            put("database", JSONObject().apply {
                put("db_size_bytes", dbStats.fileSizeBytes)
                put("db_path", dbStats.dbPath)
                put("is_wal_enabled", dbStats.isWalEnabled)
            })
            put("has_screentime_permission", screenTimeTracker.hasUsageStatsPermission())
        }
        return HttpResponse.json(json.toString())
    }
}
