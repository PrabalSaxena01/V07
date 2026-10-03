package com.v07.v3.app.telemetry

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3AppUsage
import kotlinx.coroutines.*
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * ScreenTimeTracker
 *
 * Native telemetry collector for app usage.
 * Uses Android's UsageStatsManager to extract foreground transitions and active screen time,
 * aggregating app usage into structured records and persisting directly to the V3 SQLite database.
 *
 * Runs asynchronously via Coroutines with zero file I/O overhead.
 */
class ScreenTimeTracker(
    private val context: Context,
    private val repository: EventRepository
) {
    companion object {
        private const val TAG = "V3ScreenTimeTracker"
        private const val DEFAULT_POLL_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

        private val IGNORED_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.launcher",
            "com.android.launcher3",
            "com.coloros.launcher",
            "com.google.android.apps.nexuslauncher",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.google.android.inputmethod.latin",
            "com.android.inputmethod",
            "com.android.shell",
            "com.android.documentsui",
            "com.android.providers.downloads"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    private val appLabelCache = ConcurrentHashMap<String, String>()

    data class ScreenTimeSummary(
        val date: String,
        val totalDurationMs: Long,
        val appCount: Int,
        val topApps: List<AppUsageStat> = emptyList(),
        val hourlyMinutes: List<Int> = List(24) { 0 }
    )

    data class AppUsageStat(
        val packageName: String,
        val appName: String,
        val durationMs: Long,
        val launchCount: Int,
        val category: String,
        val percentage: Float
    )

    /**
     * Checks if PACKAGE_USAGE_STATS permission is granted to this application.
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Generates intent to open Settings where user can grant usage access.
     */
    fun createPermissionIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Starts periodic telemetry polling in the background.
     */
    fun start(intervalMs: Long = DEFAULT_POLL_INTERVAL_MS) {
        if (!hasUsageStatsPermission()) {
            Log.w(TAG, "Cannot start telemetry polling: Usage stats permission not granted.")
            return
        }

        pollingJob?.cancel()
        pollingJob = scope.launch {
            Log.i(TAG, "ScreenTimeTracker polling started (interval: ${intervalMs / 1000}s)")
            while (isActive) {
                try {
                    collectAndPersistToday()
                } catch (e: Exception) {
                    Log.e(TAG, "Error collecting usage stats: ${e.message}", e)
                }
                delay(intervalMs)
            }
        }
    }

    /**
     * Stops background polling.
     */
    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        Log.i(TAG, "ScreenTimeTracker polling stopped")
    }

    /**
     * Immediately triggers collection and persistence for today.
     */
    suspend fun collectNow(): ScreenTimeSummary = withContext(Dispatchers.IO) {
        collectAndPersistToday()
    }

    /**
     * Queries and computes usage statistics for a specific target date (yyyy-MM-dd).
     */
    fun collectForDate(dateStr: String): ScreenTimeSummary {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayStr = sdf.format(java.util.Date())
        if (dateStr == todayStr || dateStr.isBlank()) {
            return collectAndPersistToday()
        }
        val cal = Calendar.getInstance().apply {
            try {
                time = sdf.parse(dateStr) ?: java.util.Date()
            } catch (_: Exception) {}
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMs = cal.timeInMillis
        val endMs = startMs + 86400000L - 1L

        // 1. First, check SQLite database for historical/imported data
        val dbUsages = repository.getAppUsageForDate(dateStr, startMs, endMs)
        val summaryEvent = repository.getEventById("st_$dateStr")

        if (dbUsages.isNotEmpty() || summaryEvent != null) {
            val statsList = mutableListOf<AppUsageStat>()
            var totalDurationMs = 0L
            val hourlyMins = MutableList(24) { 0 }

            if (summaryEvent != null && !summaryEvent.metadataJson.isNullOrBlank()) {
                try {
                    val meta = org.json.JSONObject(summaryEvent.metadataJson)
                    val hourlyArr = meta.optJSONArray("hourly_usage")
                    if (hourlyArr != null) {
                        for (h in 0 until minOf(24, hourlyArr.length())) {
                            val sec = hourlyArr.optLong(h, 0L)
                            hourlyMins[h] = (sec / 60).toInt().coerceIn(0, 60)
                        }
                    }
                    val totSec = meta.optLong("total_duration_sec", 0L)
                    if (totSec in 1..86400L) {
                        totalDurationMs = totSec * 1000L
                    }
                } catch (_: Exception) {}
            }

            if (dbUsages.isNotEmpty()) {
                val dbTotal = dbUsages.sumOf { it.durationMs }.coerceAtMost(86400000L)
                if (totalDurationMs == 0L) totalDurationMs = dbTotal

                dbUsages.forEach { u ->
                    val pct = if (totalDurationMs > 0) ((u.durationMs.toFloat() / totalDurationMs) * 100f).coerceIn(0f, 100f) else 0f
                    statsList.add(
                        AppUsageStat(
                            packageName = u.packageName,
                            appName = u.appName.ifBlank { resolveAppName(u.packageName) },
                            durationMs = u.durationMs,
                            launchCount = 1,
                            category = u.category.ifBlank { resolveAppCategory(u.packageName) },
                            percentage = pct
                        )
                    )
                }
                statsList.sortByDescending { it.durationMs }
            } else if (summaryEvent != null && !summaryEvent.metadataJson.isNullOrBlank()) {
                try {
                    val meta = org.json.JSONObject(summaryEvent.metadataJson)
                    val appsArr = meta.optJSONArray("all_apps") ?: meta.optJSONArray("top_apps")
                    if (appsArr != null) {
                        for (i in 0 until appsArr.length()) {
                            val a = appsArr.getJSONObject(i)
                            val pkg = a.optString("package_name", "unknown")
                            val name = a.optString("app_name", "Unknown")
                            val dur = a.optLong("duration_ms", 0L).coerceIn(0L, 86400000L)
                            val cat = a.optString("category", "Productivity")
                            val pct = if (totalDurationMs > 0) ((dur.toFloat() / totalDurationMs) * 100f).coerceIn(0f, 100f) else 0f
                            statsList.add(
                                AppUsageStat(
                                    packageName = pkg,
                                    appName = name,
                                    durationMs = dur,
                                    launchCount = 1,
                                    category = cat,
                                    percentage = pct
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}
            }

            if (statsList.isNotEmpty() || totalDurationMs > 0L) {
                return ScreenTimeSummary(
                    date = dateStr,
                    totalDurationMs = totalDurationMs,
                    appCount = statsList.size,
                    topApps = statsList,
                    hourlyMinutes = hourlyMins
                )
            }
        }

        return collectForCalendar(cal, isToday = false)
    }

    /**
     * Queries and computes today's usage statistics from midnight to now.
     */
    private fun collectAndPersistToday(): ScreenTimeSummary {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return collectForCalendar(cal, isToday = true)
    }

    /**
     * Core collection and persistence engine for any given calendar date.
     */
    private fun collectForCalendar(cal: Calendar, isToday: Boolean): ScreenTimeSummary {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return ScreenTimeSummary("", 0L, 0, emptyList(), List(24) { 0 })

        val startMs = cal.timeInMillis
        val endMs = if (isToday) System.currentTimeMillis() else (startMs + 86400000L - 1L)
        val dateStr = String.format("%tF", cal.time)

        val events = try {
            usm.queryEvents(startMs, endMs)
        } catch (e: Exception) {
            Log.w(TAG, "usm.queryEvents failed: ${e.message}")
            null
        }
        val event = UsageEvents.Event()

        // Accumulate active foreground intervals per package
        val packageDurations = mutableMapOf<String, Long>()
        val packageLaunches = mutableMapOf<String, Int>()
        val packageFirstMs = mutableMapOf<String, Long>()
        val packageLastMs = mutableMapOf<String, Long>()
        val hourlyDurationMs = LongArray(24)

        fun recordSession(pkg: String, start: Long, end: Long) {
            val dur = (end - start).coerceAtLeast(0L)
            if (dur > 500L && !IGNORED_PACKAGES.contains(pkg)) {
                packageDurations[pkg] = (packageDurations[pkg] ?: 0L) + dur
                packageLastMs[pkg] = end
                // Accumulate non-overlapping hourly distribution strictly capped to 60m per hour
                for (h in 0..23) {
                    val hStart = startMs + h * 3600_000L
                    val hEnd = hStart + 3600_000L
                    val oStart = maxOf(start, hStart)
                    val oEnd = minOf(end, hEnd)
                    if (oEnd > oStart) {
                        hourlyDurationMs[h] += (oEnd - oStart)
                    }
                }
            }
        }

        var currentPkg: String? = null
        var currentStartMs = 0L

        if (events != null) {
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                val time = event.timeStamp
                val evType = event.eventType

                // Screen turn-off / keyguard (SCREEN_NON_INTERACTIVE = 16, KEYGUARD_SHOWN = 18)
                if (evType == 16 || evType == 18) {
                    if (currentPkg != null && currentStartMs > 0L) {
                        recordSession(currentPkg!!, currentStartMs, time)
                        currentPkg = null
                        currentStartMs = 0L
                    }
                    continue
                }

                // Ignored packages (launchers, system UI, keyboards)
                if (IGNORED_PACKAGES.contains(pkg)) {
                    if (evType == UsageEvents.Event.ACTIVITY_RESUMED && currentPkg != null && currentStartMs > 0L) {
                        recordSession(currentPkg!!, currentStartMs, time)
                        currentPkg = null
                        currentStartMs = 0L
                    }
                    continue
                }

                when (evType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        if (currentPkg != null && currentPkg != pkg && currentStartMs > 0L) {
                            recordSession(currentPkg!!, currentStartMs, time)
                        }
                        currentPkg = pkg
                        currentStartMs = time
                        packageLaunches[pkg] = (packageLaunches[pkg] ?: 0) + 1
                        if (!packageFirstMs.containsKey(pkg)) {
                            packageFirstMs[pkg] = time
                        }
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        if (currentPkg == pkg && currentStartMs > 0L) {
                            recordSession(pkg, currentStartMs, time)
                            currentPkg = null
                            currentStartMs = 0L
                        }
                    }
                }
            }
        }

        // Account for any app currently in foreground only if screen is on (for today only)
        if (isToday) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val isScreenOn = powerManager?.isInteractive ?: false
            if (currentPkg != null && currentStartMs > 0L && isScreenOn && !IGNORED_PACKAGES.contains(currentPkg)) {
                val cappedEnd = minOf(endMs, currentStartMs + 3600_000L)
                recordSession(currentPkg!!, currentStartMs, cappedEnd)
            }
        }

        // If queryEvents returned zero data, try reading already persisted data or queryUsageStats fallback
        if (packageDurations.isEmpty()) {
            try {
                val dbUsages = repository.getAppUsageForDate(dateStr, startMs, endMs)
                if (dbUsages.isNotEmpty()) {
                    dbUsages.forEach { u ->
                        packageDurations[u.packageName] = u.durationMs
                        packageFirstMs[u.packageName] = u.startTime
                        packageLastMs[u.packageName] = u.endTime
                    }
                } else if (hasUsageStatsPermission()) {
                    val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs - 3600_000L, endMs + 3600_000L)
                    if (!stats.isNullOrEmpty()) {
                        for (s in stats) {
                            val pkg = s.packageName ?: continue
                            if (IGNORED_PACKAGES.contains(pkg)) continue
                            if (s.totalTimeInForeground >= 1000L && s.lastTimeUsed >= startMs) {
                                packageDurations[pkg] = s.totalTimeInForeground
                                packageFirstMs[pkg] = s.firstTimeStamp
                                packageLastMs[pkg] = s.lastTimeStamp
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Fallback usage resolution error: ${e.message}")
            }
        }

        val totalDuration = packageDurations.values.sum()
        val statsList = mutableListOf<AppUsageStat>()

        // Persist to EventRepository and assemble stats
        packageDurations.forEach { (pkg, duration) ->
            if (duration >= 1000L) { // Only log apps with at least 1 second usage
                val appName = resolveAppName(pkg)
                val category = resolveAppCategory(pkg)
                val launches = packageLaunches[pkg] ?: 1
                val firstTime = packageFirstMs[pkg] ?: startMs
                val lastTime = packageLastMs[pkg] ?: endMs
                val percentage = if (totalDuration > 0) (duration.toFloat() / totalDuration) * 100f else 0f

                statsList.add(
                    AppUsageStat(
                        packageName = pkg,
                        appName = appName,
                        durationMs = duration,
                        launchCount = launches,
                        category = category,
                        percentage = percentage
                    )
                )

                // Save or update in database
                val v3Usage = V3AppUsage(
                    id = "${dateStr}_$pkg",
                    packageName = pkg,
                    appName = appName,
                    startTime = firstTime,
                    endTime = lastTime,
                    durationMs = duration,
                    category = category
                )
                try {
                    repository.insertAppUsage(v3Usage)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to insert app usage into database: ${e.message}")
                }
            }
        }

        statsList.sortByDescending { it.durationMs }

        // Each hour's minutes are strictly clamped between 0 and 60 minutes
        val hourlyMins = hourlyDurationMs.map { durMs ->
            (durMs / 60000L).toInt().coerceIn(0, 60)
        }

        return ScreenTimeSummary(
            date = dateStr,
            totalDurationMs = totalDuration,
            appCount = statsList.size,
            topApps = statsList,
            hourlyMinutes = hourlyMins
        )
    }

    /**
     * Resolves human-friendly display label for a package.
     */
    private fun resolveAppName(packageName: String): String {
        return appLabelCache.getOrPut(packageName) {
            try {
                val pm = context.packageManager
                val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getApplicationInfo(packageName, 0)
                }
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                // Return clean name from package
                packageName.substringAfterLast('.')
            }
        }
    }

    /**
     * Categorizes the app based on Android ApplicationInfo category or heuristics.
     */
    private fun resolveAppCategory(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_GAME -> "Games"
                    ApplicationInfo.CATEGORY_AUDIO -> "Audio & Music"
                    ApplicationInfo.CATEGORY_VIDEO -> "Video & Media"
                    ApplicationInfo.CATEGORY_IMAGE -> "Photography"
                    ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                    ApplicationInfo.CATEGORY_NEWS -> "News & Information"
                    ApplicationInfo.CATEGORY_MAPS -> "Navigation"
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                    ApplicationInfo.CATEGORY_ACCESSIBILITY -> "Accessibility"
                    else -> "Productivity"
                }
            } else {
                "Productivity"
            }
        } catch (_: Exception) {
            "Other"
        }
    }

    data class DayScreenTime(
        val date: String,
        val dayOfWeek: String,
        val totalDurationMs: Long,
        val formatted: String,
        val isToday: Boolean
    )

    /**
     * Retrieves genuine screen time for each of the last 7 calendar days.
     */
    fun getWeeklyHistory(): List<DayScreenTime> {
        val days = mutableListOf<DayScreenTime>()
        val dayFmt = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
        val dateFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dateStr = dateFmt.format(c.time)
            val dayName = dayFmt.format(c.time)

            val totalMs = if (i == 0) {
                collectAndPersistToday().totalDurationMs
            } else {
                val dbTimeline = repository.getDayTimeline(dateStr)
                if (dbTimeline.totalScreenTimeMs > 0L) {
                    dbTimeline.totalScreenTimeMs
                } else {
                    val summary = collectForCalendar(c, isToday = false)
                    summary.totalDurationMs
                }
            }

            val totalSec = totalMs / 1000
            val hrs = totalSec / 3600
            val mins = (totalSec % 3600) / 60
            val formatted = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"

            days.add(
                DayScreenTime(
                    date = dateStr,
                    dayOfWeek = dayName,
                    totalDurationMs = totalMs,
                    formatted = formatted,
                    isToday = (i == 0)
                )
            )
        }
        return days
    }
}
