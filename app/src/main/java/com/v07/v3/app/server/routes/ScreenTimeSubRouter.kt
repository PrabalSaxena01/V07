package com.v07.v3.app.server.routes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.app.telemetry.ScreenTimeTracker
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ScreenTimeSubRouter(
    private val context: Context,
    private val screenTimeTracker: ScreenTimeTracker
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/v3/screentime") ||
                path.startsWith("/api/v3/apps/icon") ||
                path.startsWith("/api/apps/icon") ||
                path.startsWith("/api/v3/screentime/icon") ||
                path.startsWith("/app-limits") ||
                path.startsWith("/app-usage")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/api/v3/apps/icon") || path.startsWith("/api/apps/icon") || path.startsWith("/api/v3/screentime/icon") ->
                handleAppIcon(method, path, request)
            path.startsWith("/app-limits") -> handleAppLimits(method, request)
            else -> handleScreenTime(method, path)
        }
    }

    private fun handleAppLimits(method: String, request: HttpRequest): HttpResponse {
        return when (method) {
            "GET" -> HttpResponse.json(JSONObject().put("ok", true).put("limits", JSONArray()).toString())
            "POST" -> HttpResponse.json(JSONObject().put("ok", true).toString())
            else -> HttpResponse.error("Method $method not allowed for app-limits", 405)
        }
    }

    private fun handleAppIcon(method: String, path: String, request: HttpRequest): HttpResponse {
        if (method != "GET") {
            return HttpResponse.error("Method $method not allowed for app icon", 405)
        }
        val queryPkg = request.queryParams["package"] ?: request.queryParams["pkg"]
        val pathPkg = path.substringAfterLast("/").takeIf { it != "icon" && it.isNotBlank() }
        val packageName = (queryPkg ?: pathPkg)?.trim() ?: ""

        if (packageName.isBlank()) {
            return HttpResponse.error("Missing 'package' parameter", 400)
        }

        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val width = drawable.intrinsicWidth.coerceAtLeast(72)
            val height = drawable.intrinsicHeight.coerceAtLeast(72)
            val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
            val bytes = stream.toByteArray()
            HttpResponse.bytes(
                data = bytes,
                mimeType = "image/png",
                headers = mapOf(
                    "Cache-Control" to "public, max-age=86400",
                    "Access-Control-Allow-Origin" to "*"
                )
            )
        } catch (e: Exception) {
            HttpResponse.error("App icon not found for package: $packageName", 404)
        }
    }

    private suspend fun handleScreenTime(method: String, path: String): HttpResponse {
        if (!screenTimeTracker.hasUsageStatsPermission()) {
            return HttpResponse.json(JSONObject().apply {
                put("ok", false)
                put("exists", false)
                put("permission_granted", false)
                put("message", "PACKAGE_USAGE_STATS permission not granted. Please enable in Android Settings.")
            }.toString(), 403)
        }

        return when (method) {
            "GET" -> {
                val summary = screenTimeTracker.collectNow()
                val arr = JSONArray()
                val usageArr = JSONArray()
                summary.topApps.forEach { app ->
                    val appObj = JSONObject().apply {
                        put("package_name", app.packageName)
                        put("app_name", app.appName)
                        put("duration_ms", app.durationMs)
                        put("duration_sec", app.durationMs / 1000)
                        put("duration_formatted", formatSeconds(app.durationMs / 1000))
                        put("launch_count", app.launchCount)
                        put("category", app.category)
                        put("percentage", app.percentage)
                        put("icon_url", "/api/v3/apps/icon?package=" + app.packageName)
                    }
                    arr.put(appObj)
                    usageArr.put(appObj)
                }

                val formattedTotal = formatSeconds(summary.totalDurationMs / 1000)
                val summaryObj = JSONObject().apply {
                    put("total_screen_time_formatted", formattedTotal)
                    put("total_duration_ms", summary.totalDurationMs)
                    put("total_duration_sec", summary.totalDurationMs / 1000)
                    put("app_count", summary.appCount)
                }

                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("exists", true)
                    put("date", summary.date)
                    put("total_duration_ms", summary.totalDurationMs)
                    put("total_duration_sec", summary.totalDurationMs / 1000)
                    put("total_duration_formatted", formattedTotal)
                    put("app_count", summary.appCount)
                    put("apps", arr)
                    put("usage", usageArr)
                    put("summary", summaryObj)
                    put("hourly_usage", JSONArray())
                }.toString())
            }
            "POST" -> {
                val summary = screenTimeTracker.collectNow()
                HttpResponse.json(JSONObject().apply {
                    put("ok", true)
                    put("synced", true)
                    put("total_duration_ms", summary.totalDurationMs)
                    put("app_count", summary.appCount)
                }.toString())
            }
            else -> HttpResponse.error("Method $method not allowed for screentime", 405)
        }
    }

    private fun formatSeconds(totalSec: Long): String {
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return when {
            h > 0 -> String.format(java.util.Locale.US, "%dh %dm", h, m)
            m > 0 -> String.format(java.util.Locale.US, "%dm %ds", m, s)
            else -> String.format(java.util.Locale.US, "%ds", s)
        }
    }
}
