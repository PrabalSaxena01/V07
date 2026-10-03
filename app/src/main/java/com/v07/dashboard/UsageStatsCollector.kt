package com.v07.dashboard

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsageStatsCollector(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    private val ignoredPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.launcher",
        "com.android.launcher3",
        "com.coloros.launcher",
        "com.android.settings",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod",
        "com.android.shell",
        "com.android.documentsui",
        "com.android.providers.downloads"
    )

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                try {
                    collectAndSave()
                } catch (e: Exception) {
                    Log.e("V07Screen", "startPolling collectAndSave failed", e)
                }
                delay(5 * 60 * 1000L)
            }
        }
    }

    fun stop() {
        pollingJob?.cancel()
        scope.cancel()
    }

    @Synchronized
    fun collectAndSave() {
        val executionStart = System.currentTimeMillis()
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.packageManager

        Log.i("V07Screen", "collectAndSave() starting")

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()

        val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timelineSdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val events = usm.queryEvents(start, end)
        val event = UsageEvents.Event()
        Log.i("V07Screen", "queryEvents returned for range $start - $end")

        var currentActivePkg: String? = null
        var currentActiveStartMs = 0L
        val sessionList = mutableListOf<JSONObject>()
        val timelineList = mutableListOf<JSONObject>()
        val appTotalDurations = mutableMapOf<String, Long>()
        val appLaunchCounts = mutableMapOf<String, Int>()
        val appFirstLaunchMs = mutableMapOf<String, Long>()
        val appLastLaunchMs = mutableMapOf<String, Long>()

        var totalAppSwitches = 0
        var ignoredCount = 0
        var lastActivePackage: String? = null
        var firstActivityMs = Long.MAX_VALUE
        var lastActivityMs = Long.MIN_VALUE
        var totalSessionsCount = 0

        fun closeActiveSession(endMs: Long, isRunningAtEnd: Boolean = false) {
            val pkg = currentActivePkg ?: return
            val startTime = currentActiveStartMs
            currentActivePkg = null
            if (endMs <= startTime) return
            val durationSec = (endMs - startTime) / 1000L
            if (durationSec < 2L) return

            val name = appName(pkg)
            sessionList.add(sessionJson(name, pkg, startTime, endMs, durationSec, active = isRunningAtEnd, timeSdf = timeSdf))
            totalSessionsCount++
            appTotalDurations[pkg] = (appTotalDurations[pkg] ?: 0L) + durationSec

            timelineList.add(JSONObject().apply {
                put("time", timelineSdf.format(Date(endMs)))
                put("event", if (isRunningAtEnd) "RUNNING" else "END")
                put("app", name)
                put("package", pkg)
                put("timestamp_ms", endMs)
            })
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val type = event.eventType
            val eventTime = event.timeStamp
            if (eventTime < firstActivityMs) firstActivityMs = eventTime
            if (eventTime > lastActivityMs) lastActivityMs = eventTime

            // Screen turn-off / keyguard (SCREEN_NON_INTERACTIVE = 16, KEYGUARD_SHOWN = 18)
            if (type == 16 || type == 18) {
                closeActiveSession(eventTime)
                continue
            }

            // Ignored packages (launchers, system UI, keyboards)
            if (ignoredPackages.contains(pkg)) {
                ignoredCount++
                if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                    closeActiveSession(eventTime)
                }
                continue
            }

            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (currentActivePkg != null && currentActivePkg != pkg) {
                    closeActiveSession(eventTime)
                }

                appLaunchCounts[pkg] = (appLaunchCounts[pkg] ?: 0) + 1
                appFirstLaunchMs.putIfAbsent(pkg, eventTime)
                appLastLaunchMs[pkg] = eventTime

                if (lastActivePackage != null && lastActivePackage != pkg) totalAppSwitches++
                lastActivePackage = pkg
                currentActivePkg = pkg
                currentActiveStartMs = eventTime

                timelineList.add(JSONObject().apply {
                    put("time", timelineSdf.format(Date(eventTime)))
                    put("event", "START")
                    put("app", appName(pkg))
                    put("package", pkg)
                    put("timestamp_ms", eventTime)
                })
            } else if (type == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (currentActivePkg == pkg) {
                    closeActiveSession(eventTime)
                }
            }
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        val isScreenOn = powerManager?.isInteractive ?: false
        if (currentActivePkg != null && isScreenOn) {
            val cappedEnd = minOf(end, currentActiveStartMs + 7200_000L)
            closeActiveSession(cappedEnd, isRunningAtEnd = true)
        } else {
            currentActivePkg = null
        }

        sessionList.sortBy { it.optLong("start_ms") }
        timelineList.sortBy { it.optLong("timestamp_ms") }

        val sessionsArray = JSONArray().also { arr -> sessionList.forEach { arr.put(it) } }
        val timelineArray = JSONArray().also { arr ->
            timelineList.forEach {
                arr.put(it)
            }
        }

        val totalScreenTimeSec = appTotalDurations.values.sum()
        var topAppPkg = "None"
        var topAppDuration = 0L

        val usageList = appTotalDurations.map { (pkg, duration) ->
            if (duration > topAppDuration) {
                topAppDuration = duration
                topAppPkg = pkg
            }

            val launches = appLaunchCounts[pkg] ?: 0
            val percentage = if (totalScreenTimeSec > 0) {
                Math.round((duration.toDouble() / totalScreenTimeSec.toDouble() * 100.0) * 10.0) / 10.0
            } else 0.0

            JSONObject().apply {
                put("app", appName(pkg))
                put("package", pkg)
                put("total_duration_sec", duration)
                put("total_duration_formatted", formatHms(duration))
                put("launch_count", launches)
                put("average_session_sec", if (launches > 0) duration / launches else 0)
                put("percentage", percentage)
                put("first_launch", appFirstLaunchMs[pkg]?.let { timeSdf.format(Date(it)) } ?: JSONObject.NULL)
                put("last_launch", appLastLaunchMs[pkg]?.let { timeSdf.format(Date(it)) } ?: JSONObject.NULL)
            }
        }.sortedByDescending { it.optLong("total_duration_sec") }

        val sortedUsageArray = JSONArray().also { arr -> usageList.forEach { arr.put(it) } }

        val hourlyDistribution = MutableList(24) { 0L }
        var longestFocusSec = 0L
        var longestSessionObj = JSONObject()
        for (session in sessionList) {
            val duration = session.optLong("duration_sec")
            if (duration > longestFocusSec) {
                longestFocusSec = duration
                longestSessionObj = JSONObject(session.toString())
            }

            val sessionStart = session.optLong("start_ms")
            val sessionEnd = session.optLong("end_ms")

            for (h in 0..23) {
                val hourStart = start + h * 3600_000L
                val hourEnd = hourStart + 3600_000L
                val overlap = maxOf(0L, minOf(sessionEnd, hourEnd) - maxOf(sessionStart, hourStart))
                if (overlap > 0) {
                    hourlyDistribution[h] += (overlap / 1000L)
                }
            }
        }

        // Clamp each hour strictly to at most 3600 seconds (60 mins)
        for (h in 0..23) {
            if (hourlyDistribution[h] > 3600L) {
                hourlyDistribution[h] = 3600L
            }
        }

        val hourlyUsageArray = JSONArray().also { arr ->
            for (hour in 0..23) {
                val seconds = hourlyDistribution[hour]
                arr.put(JSONObject().apply {
                    put("hour", hour)
                    put("duration_sec", seconds)
                    put("duration_min", Math.min(60L, Math.round(seconds.toDouble() / 60.0)))
                })
            }
        }

        val topLaunches = appLaunchCounts[topAppPkg] ?: 0
        val topPercentage = if (totalScreenTimeSec > 0) {
            Math.round((topAppDuration.toDouble() / totalScreenTimeSec.toDouble() * 100.0) * 10.0) / 10.0
        } else 0.0

        val root = JSONObject().apply {
            put("date", dateSdf.format(Date()))
            put("generated_at", end)
            put("collector", JSONObject().apply {
                put("version", "1.2.3")
                put("collector", "Android UsageEvents")
                put("schema_version", "1.1")
                put("generated_by", "V07 Android Kotlin")
            })
            put("metadata", JSONObject().apply {
                put("manufacturer", Build.MANUFACTURER)
                put("device", Build.DEVICE)
                put("device_model", Build.MODEL)
                put("android_version", Build.VERSION.RELEASE)
                put("sdk", Build.VERSION.SDK_INT)
            })
            put("summary", JSONObject().apply {
                put("collection_start_ms", start)
                put("collection_end_ms", end)
                put("total_screen_time_sec", totalScreenTimeSec)
                put("total_screen_time_formatted", formatHms(totalScreenTimeSec))
                put("session_count", totalSessionsCount)
                put("average_session_sec", if (totalSessionsCount == 0) 0 else totalScreenTimeSec / totalSessionsCount)
                put("unique_apps_used", appTotalDurations.size)
                put("first_activity", if (firstActivityMs == Long.MAX_VALUE) JSONObject.NULL else timeSdf.format(Date(firstActivityMs)))
                put("last_activity", if (lastActivityMs == Long.MIN_VALUE) JSONObject.NULL else timeSdf.format(Date(lastActivityMs)))
                put("app_switches", totalAppSwitches)
                put("ignored_packages", ignoredCount)
                put("top_app", JSONObject().apply {
                    put("app", appName(topAppPkg))
                    put("package", topAppPkg)
                    put("duration_sec", topAppDuration)
                    put("launch_count", topLaunches)
                    put("percentage", topPercentage)
                })
                put("longest_session", longestSessionObj)
                put("generation_time_ms", System.currentTimeMillis() - executionStart)
            })
            put("usage", sortedUsageArray)
            put("hourly_usage", hourlyUsageArray)
            put("timeline", timelineArray)
            put("sessions", sessionsArray)
        }

        val outFile = VaultConfig.appUsageFile(dateSdf.format(Date()))
        outFile.parentFile?.mkdirs()
        FileWriter(outFile).use { writer -> writer.write(root.toString(2)) }
        Log.i("V07Screen", "collectAndSave() done in ${System.currentTimeMillis() - executionStart}ms, wrote ${outFile.length()} bytes to ${outFile.name}")
    }

    private fun sessionJson(
        appName: String,
        packageName: String,
        startMs: Long,
        endMs: Long,
        durationSec: Long,
        active: Boolean,
        timeSdf: SimpleDateFormat
    ): JSONObject {
        return JSONObject().apply {
            put("app", appName)
            put("package", packageName)
            put("start_ms", startMs)
            put("end_ms", endMs)
            put("start", timeSdf.format(Date(startMs)))
            put("end", timeSdf.format(Date(endMs)))
            put("duration_sec", durationSec)
            put("duration_formatted", formatHms(durationSec))
            put("focus", durationSec > 1500L)
            put("active", active)
            put("status", if (active) "running" else "completed")
        }
    }

    private fun appName(packageName: String): String {
        return try {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(ai).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast(".")
        }
    }

    private fun formatHms(seconds: Long): String {
        val h = seconds / 3600L
        val m = (seconds % 3600L) / 60L
        val s = seconds % 60L
        return "${h}h ${m}m ${s}s"
    }
}
