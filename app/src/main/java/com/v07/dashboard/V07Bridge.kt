package com.v07.dashboard

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.net.HttpURLConnection
import java.net.URL

/**
 * JS bridge exposed to plugins as `AndroidV07`. Every method is synchronous and
 * returns a JSON string; plugin code calls V07.* (injected bootstrap) which parses it.
 *
 * Reads/writes are proxied to the local V07 server on 127.0.0.1:8000. Permission
 * checks mirror the plugin manifest whitelist. The `Android` (existing) interface
 * is untouched — this is a separate interface object.
 */
class V07Bridge(private val context: Context) {

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)

    // ─── Generic dispatch ──────────────────────────────────────────────────
    @android.webkit.JavascriptInterface
    fun call(method: String, argJson: String?): String {
        val arg = argJson?.let { runCatching { jsonToMap(it) }.getOrNull() } ?: emptyMap<String, Any?>()
        val result: Any? = try {
            when (method) {
                "readEvents" -> readEvents(arg)
                "readEventsAll" -> readEventsAll(arg)
                "writeEvent" -> writeEvent(arg)
                "updateEvent" -> updateEvent(arg)
                "deleteEvent" -> deleteEvent(arg)
                "readChecklist" -> readChecklist(arg)
                "readSessions" -> readSessions(arg)
                "readUsage" -> readUsage(arg)
                "getTheme" -> mapOf("theme" to prefs.getString("theme", "dark"))
                "toast" -> { toast(arg); mapOf("ok" to true) }
                "importPlugin" -> PluginZipImporter.pickAndInstall()
                "api" -> api(arg)
                "canUseReminders" -> mapOf("allowed" to PluginManager.canUseNotifications(arg["pluginId"] as? String ?: ""))
                "scheduleReminder" -> scheduleReminder(arg)
                "cancelReminder" -> cancelReminder(arg)
                "aiStatus" -> mapOf("configured" to GeminiConfig.isConfigured, "defaultModel" to GeminiConfig.defaultModel)
                "canUseAI" -> mapOf("allowed" to GeminiManager.canPluginAccessAI(arg["pluginId"] as? String))
                "pinHomeScreenWidget" -> pinHomeScreenWidget(arg, argJson)
                "openWidgetChooser" -> openWidgetChooser()
                "getHomeScreenWidgetType" -> mapOf("type" to (prefs.getString("default_widget_type", "weekly") ?: "weekly"))
                "setHomeScreenWidgetType" -> setHomeScreenWidgetType(arg, argJson)
                else -> mapOf("error" to "unknown method: $method")
            }
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "exception"))
        }
        return toJson(result)
    }

    private fun setHomeScreenWidgetType(arg: Map<String, Any?>, rawArg: String?): Map<String, Any?> {
        val type = (arg["widgetType"] as? String)?.takeIf { it.isNotBlank() }
            ?: rawArg?.trim()?.trim('"')?.takeIf { !it.startsWith("{") && it.isNotBlank() }
            ?: "weekly"
        prefs.edit().putString("default_widget_type", type).apply()
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val allIds = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, V07DashboardWidget::class.java))
        for (id in allIds) {
            prefs.edit().putString("widget_type_$id", type).apply()
            V07DashboardWidget.updateWidget(context, appWidgetManager, id)
        }
        return mapOf("ok" to true, "type" to type, "updatedCount" to allIds.size)
    }

    private fun pinHomeScreenWidget(arg: Map<String, Any?>, rawArg: String?): Map<String, Any?> {
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val type = (arg["widgetType"] as? String)?.takeIf { it.isNotBlank() }
            ?: rawArg?.trim()?.trim('"')?.takeIf { !it.startsWith("{") && it.isNotBlank() }

        if (type.isNullOrBlank()) {
            openWidgetChooser()
            return mapOf("ok" to true, "openedChooser" to true)
        }

        prefs.edit().putString("default_widget_type", type).apply()
        val allIds = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, V07DashboardWidget::class.java))
        for (id in allIds) {
            prefs.edit().putString("widget_type_$id", type).apply()
            V07DashboardWidget.updateWidget(context, appWidgetManager, id)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            val componentName = android.content.ComponentName(context, V07DashboardWidget::class.java)
            val successIntent = Intent(context, V07DashboardWidget::class.java).apply {
                action = "com.v07.dashboard.PINNED_WIDGET_CONFIG"
                putExtra("preferred_widget_type", type)
            }
            val successCallback = PendingIntent.getBroadcast(
                context, 0, successIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val requested = appWidgetManager.requestPinAppWidget(componentName, null, successCallback)
            return mapOf("ok" to requested, "supported" to true)
        } else {
            openWidgetChooser()
            return mapOf("ok" to true, "supported" to false, "message" to "Chooser opened")
        }
    }

    private fun openWidgetChooser(): Map<String, Any?> {
        val intent = Intent(context, V07WidgetConfigActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return mapOf("ok" to true)
    }

    // ─── Data access (proxied to local server) ────────────────────────────

    private fun readEvents(arg: Map<String, Any?>): Any? {
        val cat = arg["category"] as? String ?: return mapOf("error" to "missing category")
        val date = arg["date"] as? String
        val path = if (date.isNullOrEmpty()) "/events/$cat" else "/event/$cat/range?start=$date&end=$date"
        return serverGet(path)
    }

    private fun readEventsAll(arg: Map<String, Any?>): Any? {
        val cat = arg["category"] as? String ?: return mapOf("error" to "missing category")
        return serverGet("/events/$cat")
    }

    private fun writeEvent(arg: Map<String, Any?>): Any? {
        val cat = arg["category"] as? String ?: return mapOf("ok" to false, "error" to "missing category")
        val data = arg["data"] as? Map<String, Any?> ?: emptyMap()
        return serverPost("/event/$cat", mapOf("data" to data))
    }

    private fun updateEvent(arg: Map<String, Any?>): Any? {
        val cat = arg["category"] as? String ?: return mapOf("ok" to false, "error" to "missing category")
        val id = arg["id"] as? String ?: return mapOf("ok" to false, "error" to "missing id")
        val fields = arg["fields"] as? Map<String, Any?> ?: emptyMap()
        return serverPost("/event/$cat/$id/edit", fields)
    }

    private fun deleteEvent(arg: Map<String, Any?>): Any? {
        val cat = arg["category"] as? String ?: return mapOf("ok" to false, "error" to "missing category")
        val id = arg["id"] as? String ?: return mapOf("ok" to false, "error" to "missing id")
        return serverDelete("/event/$cat/$id")
    }

    private fun readChecklist(arg: Map<String, Any?>): Any? {
        val date = arg["date"] as? String
        return serverGet(if (date.isNullOrEmpty()) "/checklist/today" else "/checklist/today")
    }

    private fun readSessions(arg: Map<String, Any?>): Any? {
        val date = arg["date"] as? String
        return serverGet(if (date.isNullOrEmpty()) "/sessions/history" else "/sessions/history")
    }

    private fun readUsage(arg: Map<String, Any?>): Any? {
        val date = arg["date"] as? String
        return serverGet("/app-usage?date=${date ?: ""}")
    }

    private fun toast(arg: Map<String, Any?>) {
        val msg = arg["message"] as? String ?: ""
        val handler = android.os.Handler(context.mainLooper)
        handler.post {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /** Permission-gated proxy to arbitrary local server path ("server" permission). */
    private fun api(arg: Map<String, Any?>): Any? {
        val method = (arg["method"] as? String)?.uppercase() ?: "GET"
        val path = arg["path"] as? String ?: return mapOf("error" to "missing path")
        val data = arg["data"] as? Map<String, Any?>?
        return when (method) {
            "GET" -> serverGet(path)
            "POST" -> serverPost(path, data ?: emptyMap())
            "DELETE" -> serverDelete(path)
            else -> mapOf("error" to "unsupported method")
        }
    }

    // ─── Reminder Timers / Notifications ───────────────────────────────────

    private fun scheduleReminder(arg: Map<String, Any?>): Map<String, Any?> {
        val pluginId = arg["pluginId"] as? String ?: return mapOf("ok" to false, "error" to "missing pluginId")
        if (!PluginManager.canUseNotifications(pluginId)) {
            return mapOf(
                "ok" to false,
                "error" to "Plugin does not have 'notification' permission or notifications are disabled in settings"
            )
        }

        val title = (arg["title"] as? String)?.ifBlank { "Reminder" } ?: "Reminder"
        val body = (arg["body"] as? String)
            ?: (arg["message"] as? String)
            ?: (arg["text"] as? String)
            ?: ""
        val delayMs = (arg["delayMs"] as? Number)?.toLong()
            ?: (arg["delay"] as? Number)?.toLong()
            ?: (arg["delayMs"] as? String)?.toLongOrNull()
            ?: (arg["delay"] as? String)?.toLongOrNull()
            ?: 0L
        val atTime = (arg["atTime"] as? Number)?.toLong()
            ?: (arg["atTime"] as? String)?.toLongOrNull()
            ?: 0L
        val id = arg["id"]?.toString() ?: "${System.currentTimeMillis()}"

        PluginReminderReceiver.ensureNotificationChannel(context)

        val triggerAtMs = when {
            delayMs > 0 -> System.currentTimeMillis() + delayMs
            atTime > System.currentTimeMillis() -> atTime
            else -> 0L
        }

        val notifId = (pluginId + id).hashCode() and 0x7FFFFFFF

        if (triggerAtMs <= System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return mapOf("ok" to false, "error" to "POST_NOTIFICATIONS permission not granted. Please enable notifications in App Settings.")
                }
            }
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("openPlugin", pluginId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, V07ServerService.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            return try {
                NotificationManagerCompat.from(context).notify(notifId, notification)
                mapOf("ok" to true, "id" to id, "immediate" to true)
            } catch (e: Exception) {
                mapOf("ok" to false, "error" to (e.message ?: "Failed to fire notification"))
            }
        } else {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, PluginReminderReceiver::class.java).apply {
                action = "com.v07.dashboard.ACTION_PLUGIN_REMINDER"
                putExtra("pluginId", pluginId)
                putExtra("id", id)
                putExtra("title", title)
                putExtra("body", body)
                putExtra("repeating", id == "daily" || (arg["repeating"] as? Boolean) == true)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager?.canScheduleExactAlarms() == true) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                    } else {
                        alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                } else {
                    alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                }
            } catch (_: SecurityException) {
                alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
            return mapOf("ok" to true, "id" to id, "triggerAt" to triggerAtMs)
        }
    }

    private fun cancelReminder(arg: Map<String, Any?>): Map<String, Any?> {
        val pluginId = arg["pluginId"] as? String ?: return mapOf("ok" to false, "error" to "missing pluginId")
        val id = arg["id"]?.toString() ?: return mapOf("ok" to false, "error" to "missing id")
        val notifId = (pluginId + id).hashCode() and 0x7FFFFFFF

        val intent = Intent(context, PluginReminderReceiver::class.java).apply {
            action = "com.v07.dashboard.ACTION_PLUGIN_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        NotificationManagerCompat.from(context).cancel(notifId)
        return mapOf("ok" to true, "id" to id)
    }

    // ─── HTTP helpers (blocking, called from WebView JS thread) ───────────

    private fun serverGet(path: String): Any? =
        http(path, "GET", null)

    private fun serverPost(path: String, data: Map<String, Any?>): Any? =
        http(path, "POST", data)

    private fun serverDelete(path: String): Any? =
        http(path, "DELETE", null)

    private fun http(path: String, method: String, data: Map<String, Any?>?): Any? {
        return try {
            val url = URL("${V07ServerConfig.baseUrl}$path")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = method
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Accept", "application/json")
                if (data != null) {
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.outputStream.use { it.write(toJson(data).toByteArray(Charsets.UTF_8)) }
                }
                val code = conn.responseCode
                val resp = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = resp?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
                runCatching { jsonToMap(text) }.getOrNull() ?: mapOf("raw" to text)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            mapOf("ok" to false, "error" to (e.message ?: "network error"))
        }
    }
}
