package com.v07.v3.app.server

import android.content.Context
import android.util.Log
import com.v07.v3.app.server.routes.*
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.core.data.DatabaseManager
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.plugin.PluginRegistry
import com.v07.v3.core.sync.V3CompanionSyncManager

/**
 * ApiRouter
 *
 * Coordinates V3 REST endpoint routing.
 * Delegates to modular SubRouters while enforcing authentication and CORS.
 */
class ApiRouter(
    private val context: Context,
    private val repository: EventRepository,
    private val databaseManager: DatabaseManager,
    private val timerManager: TimerManager,
    private val screenTimeTracker: ScreenTimeTracker,
    private val pluginRegistry: PluginRegistry,
    private val syncManager: V3CompanionSyncManager,
    private val sessionToken: String
) {
    companion object {
        private const val TAG = "V3ApiRouter"
    }

    private val subRouters: List<SubRouter> = listOf(
        StatusSubRouter(timerManager, databaseManager, screenTimeTracker),
        SessionSubRouter(repository, timerManager, syncManager),
        TaskSubRouter(repository, syncManager),
        ThoughtSubRouter(repository, syncManager),
        ScreenTimeSubRouter(context, screenTimeTracker),
        CadenceSubRouter(repository, syncManager),
        PluginSubRouter(repository, pluginRegistry),
        AdminSubRouter(context, repository, databaseManager, syncManager),
        AiSubRouter(context, repository)
    )

    suspend fun handle(request: HttpRequest): HttpResponse {
        val path = request.path
        val method = request.method.uppercase()

        // Handle CORS preflight
        if (method == "OPTIONS") {
            return HttpResponse(statusCode = 204, statusText = "No Content")
        }

        // Threat Hardening: Authenticate data-bearing endpoints
        val isExempt = (path == "/api/v3/status" && method == "GET") ||
                path.startsWith("/ai") ||
                path.startsWith("/events") || path.startsWith("/event") ||
                path.startsWith("/daytracker") || path.startsWith("/day-tracker") ||
                path.startsWith("/checklist") || path.startsWith("/tasks") ||
                path.startsWith("/task/") ||
                path.startsWith("/api/weekly-summary")
        if (!isExempt) {
            val tokenHeader = request.getHeader("X-V07-Token") ?: request.queryParams["token"]
            if (tokenHeader != sessionToken) {
                return HttpResponse.error("Unauthorized: Invalid or missing X-V07-Token", 401)
            }
        }

        return try {
            for (router in subRouters) {
                if (router.matches(path)) {
                    return router.handle(method, path, request)
                }
            }
            HttpResponse.error("Endpoint not found: $path", 404)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling $method $path: ${e.message}", e)
            HttpResponse.error("Internal Server Error: ${e.message}", 500)
        }
    }
}
