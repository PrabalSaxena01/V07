package com.v07.v3.core.plugin.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.core.data.EventRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TelemetryOverlayBridgeHandler(
    private val context: Context,
    private val screenTimeTracker: ScreenTimeTracker,
    private val repository: EventRepository,
    private val mainHandler: Handler,
    private val activityProvider: () -> com.v07.v3.app.V3Activity?
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("telemetry:") ||
                method == "apps:getIcon" ||
                method.startsWith("overlay:") ||
                method.startsWith("notifications:") ||
                method.startsWith("alarms:") ||
                method.startsWith("reminders:") ||
                method == "tasks:cancelReminder" ||
                method == "tasks:scheduleReminder" ||
                method == "ui:toast"
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "telemetry:getScreenTime" -> {
                val requestedDate = params.optString("date", "").ifBlank { null }
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val (dateStr, totalMs, appCount) = if (requestedDate != null && requestedDate != todayStr) {
                    val tl = repository.getDayTimeline(requestedDate)
                    Triple(requestedDate, tl.totalScreenTimeMs, 0)
                } else {
                    val summary = runBlocking { screenTimeTracker.collectNow() }
                    Triple(summary.date, summary.totalDurationMs, summary.appCount)
                }
                val res = JSONObject().apply {
                    put("date", dateStr)
                    put("total_duration_ms", totalMs)
                    put("total_duration_sec", totalMs / 1000)
                    put("app_count", appCount)
                }
                bridgeSuccess(res)
            }
            "telemetry:getDetailedScreenTime", "telemetry:getSummary" -> {
                val requestedDate = params.optString("date", "").ifBlank { null }
                val summary = runBlocking {
                    if (requestedDate != null) screenTimeTracker.collectForDate(requestedDate)
                    else screenTimeTracker.collectNow()
                }
                val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val hasPerm = screenTimeTracker.hasUsageStatsPermission() || summary.totalDurationMs > 0L || (requestedDate != null && requestedDate != todayDateStr)
                val appsArr = JSONArray()
                summary.topApps.take(15).forEach { a ->
                    val totalSec = a.durationMs / 1000
                    val hrs = totalSec / 3600
                    val mins = (totalSec % 3600) / 60
                    val formatted = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                    appsArr.put(JSONObject().apply {
                        put("packageName", a.packageName)
                        put("appName", a.appName)
                        put("durationMs", a.durationMs)
                        put("durationSec", totalSec)
                        put("formatted", formatted)
                        put("durationFormatted", formatted)
                        put("category", a.category)
                        put("percentage", a.percentage)
                        put("percentOfTotal", a.percentage)
                        put("launchCount", a.launchCount)
                        put("iconUrl", "/api/v3/apps/icon?package=" + a.packageName)
                    })
                }
                val totalSec = summary.totalDurationMs / 1000
                val totalHrs = totalSec / 3600
                val totalMins = (totalSec % 3600) / 60
                val formattedTotal = if (totalHrs > 0) "${totalHrs}h ${totalMins}m" else "${totalMins}m"

                val weeklyArr = JSONArray()
                val weekly = screenTimeTracker.getWeeklyHistory()
                weekly.forEach { w ->
                    weeklyArr.put(JSONObject().apply {
                        put("date", w.date)
                        put("dayOfWeek", w.dayOfWeek)
                        put("totalDurationMs", w.totalDurationMs)
                        put("formatted", w.formatted)
                        put("isToday", w.isToday)
                    })
                }

                val hourlyArr = JSONArray()
                summary.hourlyMinutes.forEach { hourlyArr.put(it) }

                val res = JSONObject().apply {
                    put("hasPermission", hasPerm)
                    put("totalDurationMs", summary.totalDurationMs)
                    put("totalDurationSec", totalSec)
                    put("formattedTotal", formattedTotal)
                    put("totalFormatted", formattedTotal)
                    put("appCount", summary.appCount)
                    put("topApps", appsArr)
                    put("weeklyHistory", weeklyArr)
                    put("hourlyMinutes", hourlyArr)
                }
                bridgeSuccess(res)
            }
            "telemetry:getAppIcon", "apps:getIcon" -> {
                val pkg = params.optString("package").ifBlank { params.optString("packageName") }
                if (pkg.isBlank()) return bridgeError("Missing 'package' parameter")
                try {
                    val pm = context.packageManager
                    val drawable = pm.getApplicationIcon(pkg)
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
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    val b64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    bridgeSuccess(JSONObject().apply {
                        put("package", pkg)
                        put("dataUrl", "data:image/png;base64,$b64")
                    })
                } catch (e: Exception) {
                    bridgeError("Icon not found for $pkg: ${e.message}")
                }
            }
            "telemetry:getWeekly" -> {
                val weekly = screenTimeTracker.getWeeklyHistory()
                val weeklyArr = JSONArray()
                weekly.forEach { w ->
                    weeklyArr.put(JSONObject().apply {
                        put("date", w.date)
                        put("dayOfWeek", w.dayOfWeek)
                        put("totalDurationMs", w.totalDurationMs)
                        put("formatted", w.formatted)
                        put("isToday", w.isToday)
                    })
                }
                bridgeSuccess(JSONObject().put("days", weeklyArr))
            }
            "telemetry:requestPermission" -> {
                mainHandler.post {
                    try {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", true))
            }
            "telemetry:hasUsagePermission" -> {
                val hasPerm = screenTimeTracker.hasUsageStatsPermission()
                bridgeSuccess(JSONObject().put("hasPermission", hasPerm).put("granted", hasPerm))
            }
            "overlay:getStatus" -> {
                val enabled = com.v07.v3.app.overlay.V3BubbleOverlayService.isOverlayEnabled(context)
                val running = com.v07.v3.app.overlay.V3BubbleOverlayService.isRunning
                val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
                val hasUsage = screenTimeTracker.hasUsageStatsPermission()
                val res = JSONObject().apply {
                    put("enabled", enabled)
                    put("running", running)
                    put("hasOverlayPermission", hasOverlay)
                    put("hasUsagePermission", hasUsage)
                }
                bridgeSuccess(res)
            }
            "overlay:start" -> {
                val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
                if (!hasOverlay) {
                    bridgeError("OVERLAY_PERMISSION_REQUIRED")
                } else {
                    mainHandler.post {
                        com.v07.v3.app.overlay.V3BubbleOverlayService.start(context)
                    }
                    bridgeSuccess(JSONObject().put("success", true).put("running", true))
                }
            }
            "overlay:stop" -> {
                mainHandler.post {
                    com.v07.v3.app.overlay.V3BubbleOverlayService.stop(context)
                }
                bridgeSuccess(JSONObject().put("success", true).put("running", false))
            }
            "overlay:toggle" -> {
                val running = com.v07.v3.app.overlay.V3BubbleOverlayService.isRunning
                val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
                if (!running && !hasOverlay) {
                    bridgeError("OVERLAY_PERMISSION_REQUIRED")
                } else {
                    mainHandler.post {
                        com.v07.v3.app.overlay.V3BubbleOverlayService.toggle(context)
                    }
                    bridgeSuccess(JSONObject().put("success", true).put("running", !running))
                }
            }
            "overlay:requestPermission" -> {
                mainHandler.post {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    } catch (_: Exception) {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
                bridgeSuccess(JSONObject().put("success", true))
            }
            "notifications:requestPermission" -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        mainHandler.post {
                            activityProvider()?.requestNotificationPermission()
                        }
                    }
                    bridgeSuccess(JSONObject().put("requested", true).put("granted", granted))
                } else {
                    val granted = NotificationManagerCompat.from(context).areNotificationsEnabled()
                    bridgeSuccess(JSONObject().put("requested", false).put("granted", granted))
                }
            }
            "notifications:checkPermission", "notifications:hasPermission" -> {
                val granted = if (Build.VERSION.SDK_INT >= 33) {
                    context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else {
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
                bridgeSuccess(JSONObject().put("granted", granted).put("hasPermission", granted))
            }
            "notifications:send", "notifications:post", "notifications:test" -> {
                val title = params.optString("title", "Sanctuary Notification")
                val body = params.optString("body", "Stay centered. A mindful breath restores clear presence.")
                val pId = params.optString("pluginId", "sanctuary")
                val id = params.optString("id", UUID.randomUUID().toString())
                val notifId = com.v07.dashboard.PluginReminderReceiver.postCustomNotification(
                    context, pId, id, title, body
                )
                bridgeSuccess(JSONObject().put("success", notifId != -1).put("notifId", notifId))
            }
            "alarms:schedule", "reminders:schedule", "tasks:scheduleReminder" -> {
                com.v07.dashboard.PluginReminderReceiver.ensureNotificationChannel(context)
                val id = params.optString("id", UUID.randomUUID().toString())
                val title = params.optString("title", "Task Reminder").ifBlank { "Task Reminder" }
                val body = params.optString("body", params.optString("message", "Upcoming task deadline in 1 hour"))
                val triggerAtMs = params.optLong("triggerAtMs", 0L)
                val pId = params.optString("pluginId", "tasks")
                val isAlarm = params.optBoolean("isAlarm", id.endsWith("_due") || pId == "tasks")

                if (triggerAtMs > System.currentTimeMillis()) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                    val notifId = (pId + id).hashCode() and 0x7FFFFFFF
                    val intent = Intent(context, com.v07.dashboard.PluginReminderReceiver::class.java).apply {
                        action = "com.v07.dashboard.ACTION_PLUGIN_REMINDER"
                        putExtra("pluginId", pId)
                        putExtra("id", id)
                        putExtra("title", title)
                        putExtra("body", body)
                        putExtra("isAlarm", isAlarm)
                        putExtra("repeating", false)
                    }
                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                        context,
                        notifId,
                        intent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager?.canScheduleExactAlarms() == true) {
                                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                            } else {
                                alarmManager?.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager?.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                        } else {
                            alarmManager?.set(android.app.AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                        }
                        bridgeSuccess(JSONObject().put("scheduled", true).put("id", id).put("triggerAtMs", triggerAtMs))
                    } catch (e: Exception) {
                        bridgeError("Failed to schedule alarm: ${e.message}")
                    }
                } else {
                    bridgeSuccess(JSONObject().put("scheduled", false).put("reason", "time_in_past"))
                }
            }
            "alarms:cancel", "reminders:cancel", "tasks:cancelReminder" -> {
                val id = params.optString("id", "")
                val pId = params.optString("pluginId", "tasks")
                if (id.isNotBlank()) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                    listOf(id, "${id}_due", "${id}_1h").forEach { subId ->
                        val notifId = (pId + subId).hashCode() and 0x7FFFFFFF
                        val intent = Intent(context, com.v07.dashboard.PluginReminderReceiver::class.java).apply {
                            action = "com.v07.dashboard.ACTION_PLUGIN_REMINDER"
                        }
                        val pendingIntent = android.app.PendingIntent.getBroadcast(
                            context,
                            notifId,
                            intent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        alarmManager?.cancel(pendingIntent)
                        pendingIntent.cancel()
                    }
                }
                bridgeSuccess(JSONObject().put("cancelled", true))
            }
            "ui:toast" -> {
                val msg = params.optString("message", "")
                if (msg.isNotBlank()) {
                    mainHandler.post {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
                bridgeSuccess(JSONObject().put("success", true))
            }
            else -> null
        }
    }
}
