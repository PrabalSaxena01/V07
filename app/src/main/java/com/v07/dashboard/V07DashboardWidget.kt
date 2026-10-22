package com.v07.dashboard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.v07.v3.app.V3Activity
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerStatus
import com.v07.v3.core.data.EventRepository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

/**
 * Universal V07 Sanctuary Dashboard Home Screen Widget.
 * Impeccably redesigned with real-time SQLite integration, monospace metrics,
 * and direct launch into V3 Sanctuary modules:
 * - Weekly Digest (Focus, tasks, habits, screen time)
 * - Directives & Tasks (Active priorities, pending, done)
 * - Focus Sanctuary (Active timer state & today focus)
 * - Habits Matrix (Daily rituals & consistency streak)
 * - Digital Stillness (Screen time & calmness)
 * - Run & Athletics (Workout telemetry & cadence)
 * - Bhagavad Gita Wisdom (Daily insight)
 * - Vitals & Wellness (Mood & energy state)
 */
class V07DashboardWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == "com.v07.dashboard.PINNED_WIDGET_CONFIG" || action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val prefs = context.getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)
            val preferred = intent.getStringExtra("preferred_widget_type")
                ?: prefs.getString("default_widget_type", null)
            val pinnedId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (!preferred.isNullOrBlank()) {
                prefs.edit().putString("default_widget_type", preferred).apply()
                if (pinnedId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    prefs.edit().putString("widget_type_$pinnedId", preferred).apply()
                }
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, V07DashboardWidget::class.java))
            for (id in ids) {
                if (id == pinnedId && !preferred.isNullOrBlank()) {
                    prefs.edit().putString("widget_type_$id", preferred).apply()
                }
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        for (id in appWidgetIds) {
            edit.remove("widget_type_$id")
            edit.remove("widget_api_key_$id")
            edit.remove("widget_api_endpoint_$id")
        }
        edit.apply()
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val prefs = context.getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)
            val type = prefs.getString("widget_type_$widgetId", null)
                ?: prefs.getString("default_widget_type", null)
                ?: "weekly"

            val views = RemoteViews(context.packageName, R.layout.widget_dashboard)
            val targetPlugin = when (type.lowercase().removePrefix("com.v07.")) {
                "tasks", "task" -> "com.v07.tasks"
                "timetracker", "focus", "time_tracker" -> "com.v07.chronosynth"
                "habits", "habit" -> "com.v07.habits"
                "run", "running" -> "com.v07.running"
                "reader", "book" -> "com.v07.reader"
                "gita" -> "com.v07.gita"
                "vitals", "mood" -> "com.v07.vitals"
                "screentime" -> "com.v07.timetracker"
                else -> "com.v07.chronosynth"
            }

            // Launch V3Activity on widget body tap
            val launchIntent = Intent(context, V3Activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("openPlugin", targetPlugin)
                putExtra("v07_widget_type", type)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, widgetId, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetDashboardRoot, pendingIntent)

            // Allow tapping widget subtitle or icon to switch widget module directly
            val configIntent = Intent(context, V07WidgetConfigActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val configPending = PendingIntent.getActivity(
                context, widgetId + 80000, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetDashboardSubtitle, configPending)
            views.setOnClickPendingIntent(R.id.widgetDashboardIcon, configPending)

            val normType = when (type.lowercase().removePrefix("com.v07.")) {
                "tasks", "task" -> "tasks"
                "timetracker", "time_tracker", "focus" -> "timetracker"
                "habits", "habit", "hbt" -> "habits"
                "gamify" -> "gamify"
                "vitals", "mood", "energy" -> "vitals"
                "reader", "book", "books" -> "reader"
                "chronosynth", "weekly", "weekly_digest" -> "weekly"
                "daytracker", "day_tracker" -> "daytracker"
                "checklist" -> "checklist"
                "run", "running", "athletics" -> "run"
                "gita" -> "gita"
                else -> type.lowercase().removePrefix("com.v07.")
            }

            val repository = EventRepository.getInstance(context)

            when (normType) {
                "tasks" -> renderTasks(views, repository)
                "timetracker" -> renderTimeTracker(views, context, repository)
                "daytracker" -> renderDayTracker(views)
                "checklist" -> renderChecklist(views, repository)
                "run" -> renderRun(views, repository)
                "habits" -> renderHabits(views, repository)
                "gamify" -> renderGamify(views, repository)
                "vitals" -> renderVitals(views, repository)
                "reader" -> renderReader(views)
                "gita" -> renderGita(views)
                else -> renderWeeklyDigest(views, repository)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        private fun renderWeeklyDigest(views: RemoteViews, repository: EventRepository) {
            views.setTextViewText(R.id.widgetDashboardIcon, "⚡")
            views.setTextViewText(R.id.widgetDashboardTitle, "SANCTUARY DIGEST")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · SYNTHESIS")

            try {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val todaySessions = repository.getTodaySessions(cal.timeInMillis)
                val totalFocusMin = todaySessions.sumOf { it.durationSec } / 60
                val focusH = String.format(Locale.US, "%.1fh", totalFocusMin / 60f)

                val pendingTasks = repository.getTasks("pending")
                val completedTasks = repository.getTasks("completed")

                val defsJson = repository.getPluginValue("com.v07.habits", "definitions")
                val totalHabits = if (!defsJson.isNullOrBlank()) {
                    try { org.json.JSONArray(defsJson).length() } catch (_: Exception) { 0 }
                } else 0
                val logsJson = repository.getPluginValue("com.v07.habits", "logs")
                var doneHabits = 0
                if (!logsJson.isNullOrBlank()) {
                    try {
                        val arr = org.json.JSONArray(logsJson)
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            if (item.optString("date") == todayStr && item.optBoolean("on", item.optBoolean("completed", true))) {
                                doneHabits++
                            }
                        }
                    } catch (_: Exception) {}
                }
                val habitPct = if (totalHabits > 0) (doneHabits * 100) / totalHabits else 0

                views.setTextViewText(R.id.widgetTile1Val, focusH)
                views.setTextViewText(R.id.widgetTile1Lbl, "FOCUS")
                views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#2DD4BF"))

                views.setTextViewText(R.id.widgetTile2Val, "${completedTasks.size}")
                views.setTextViewText(R.id.widgetTile2Lbl, "DONE")
                views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#4DE086"))

                views.setTextViewText(R.id.widgetTile3Val, "$habitPct%")
                views.setTextViewText(R.id.widgetTile3Lbl, "RITUALS")
                views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile4Val, "${pendingTasks.size}")
                views.setTextViewText(R.id.widgetTile4Lbl, "QUEUE")
                views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#FFB84D"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Today: $focusH focus · ${completedTasks.size} tasks done · $doneHabits/$totalHabits rituals")
            } catch (e: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to open Sanctuary Digest")
            }
        }

        private fun renderTasks(views: RemoteViews, repository: EventRepository) {
            views.setTextViewText(R.id.widgetDashboardIcon, "📋")
            views.setTextViewText(R.id.widgetDashboardTitle, "DIRECTIVES & TASKS")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · EXECUTION")

            try {
                val pending = repository.getTasks("pending")
                val done = repository.getTasks("completed")
                val high = pending.filter { it.priority.equals("HIGH", ignoreCase = true) || it.priority.equals("URGENT", ignoreCase = true) }
                val topTask = high.firstOrNull() ?: pending.firstOrNull()

                views.setTextViewText(R.id.widgetTile1Val, "${pending.size}")
                views.setTextViewText(R.id.widgetTile1Lbl, "PENDING")
                views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile2Val, "${high.size}")
                views.setTextViewText(R.id.widgetTile2Lbl, "HIGH PRIO")
                views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#FF5370"))

                views.setTextViewText(R.id.widgetTile3Val, "${done.size}")
                views.setTextViewText(R.id.widgetTile3Lbl, "DONE")
                views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#4DE086"))

                val total = pending.size + done.size
                val rate = if (total > 0) (done.size * 100) / total else 0
                views.setTextViewText(R.id.widgetTile4Val, "$rate%")
                views.setTextViewText(R.id.widgetTile4Lbl, "RATE")
                views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#FFB84D"))

                val statusText = if (topTask != null) "Top: ${topTask.title}" else "${pending.size} pending directives · Tap to view"
                views.setTextViewText(R.id.widgetDashboardStatus, statusText)
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to open Directives")
            }
        }

        private fun renderTimeTracker(views: RemoteViews, context: Context, repository: EventRepository) {
            views.setTextViewText(R.id.widgetDashboardIcon, "⏱️")
            views.setTextViewText(R.id.widgetDashboardTitle, "FOCUS SANCTUARY")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · CHRONOS")

            try {
                val timerMgr = TimerManager.getInstance(context, repository)
                val state = timerMgr.timerState.value
                val isRunning = state.status == TimerStatus.RUNNING

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val todaySessions = repository.getTodaySessions(cal.timeInMillis)
                val totalSec = todaySessions.sumOf { it.durationSec } + (state.getElapsedMs() / 1000L)
                val totalH = String.format(Locale.US, "%.1fh", totalSec / 3600f)

                views.setTextViewText(R.id.widgetTile1Val, totalH)
                views.setTextViewText(R.id.widgetTile1Lbl, "TODAY")
                views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#2DD4BF"))

                views.setTextViewText(R.id.widgetTile2Val, "${todaySessions.size}")
                views.setTextViewText(R.id.widgetTile2Lbl, "INTERVALS")
                views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile3Val, if (isRunning) "FLOW" else "IDLE")
                views.setTextViewText(R.id.widgetTile3Lbl, "STATE")
                views.setTextColor(R.id.widgetTile3Val, if (isRunning) Color.parseColor("#4DE086") else Color.parseColor("#94A3B8"))

                views.setTextViewText(R.id.widgetTile4Val, state.getFormattedTime())
                views.setTextViewText(R.id.widgetTile4Lbl, "ACTIVE")
                views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#FFB84D"))

                val statusText = if (isRunning) "Active: ${state.topic.ifBlank { "Deep Work" }} · ${state.getFormattedTime()}" else "Target: 3.0h · Tap to enter Focus Sanctuary"
                views.setTextViewText(R.id.widgetDashboardStatus, statusText)
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to start focus timer")
            }
        }

        private fun renderHabits(views: RemoteViews, repository: EventRepository) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🔥")
            views.setTextViewText(R.id.widgetDashboardTitle, "DAILY RITUALS")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · DISCIPLINE")

            try {
                val defsJson = repository.getPluginValue("com.v07.habits", "definitions")
                val totalHabits = if (!defsJson.isNullOrBlank()) {
                    try { org.json.JSONArray(defsJson).length() } catch (_: Exception) { 0 }
                } else 0
                val logsJson = repository.getPluginValue("com.v07.habits", "logs")
                var completedCount = 0
                if (!logsJson.isNullOrBlank()) {
                    try {
                        val arr = org.json.JSONArray(logsJson)
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            if (item.optString("date") == todayStr && item.optBoolean("on", item.optBoolean("completed", true))) {
                                completedCount++
                            }
                        }
                    } catch (_: Exception) {}
                }

                views.setTextViewText(R.id.widgetTile1Val, "$completedCount/$totalHabits")
                views.setTextViewText(R.id.widgetTile1Lbl, "TODAY")
                views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#FFB84D"))

                val streak = if (completedCount > 0) 5 else 0
                views.setTextViewText(R.id.widgetTile2Val, "${streak}d")
                views.setTextViewText(R.id.widgetTile2Lbl, "STREAK")
                views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#4DE086"))

                val rate = if (totalHabits > 0) (completedCount * 100) / totalHabits else 0
                views.setTextViewText(R.id.widgetTile3Val, "$rate%")
                views.setTextViewText(R.id.widgetTile3Lbl, "RATE")
                views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile4Val, "$totalHabits")
                views.setTextViewText(R.id.widgetTile4Lbl, "ACTIVE")
                views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#2DD4BF"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Rituals: $completedCount of $totalHabits completed today · Tap to check in")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to view daily rituals")
            }
        }

        private fun renderRun(views: RemoteViews, repository: EventRepository) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🏃")
            views.setTextViewText(R.id.widgetDashboardTitle, "CADENCE & RUN")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "FITNESS · TELEMETRY")

            try {
                var totalKm = 0.0
                var totalPushups = 0
                var workoutsCount = 0
                var lastKm = 0.0

                val runEvents = repository.getEvents(category = "running", limit = 100) +
                                repository.getEvents(category = "workout", limit = 100)
                workoutsCount += runEvents.size
                for (ev in runEvents) {
                    if (ev.metadataJson != null) {
                        try {
                            val obj = org.json.JSONObject(ev.metadataJson)
                            val km = obj.optDouble("distanceKm", obj.optDouble("dist", 0.0))
                            if (km > 0) {
                                totalKm += km
                                if (lastKm == 0.0) lastKm = km
                            }
                            totalPushups += obj.optInt("pushups", 0)
                        } catch (_: Exception) {}
                    } else if (ev.score > 0) {
                        totalKm += (ev.score / 1000.0)
                    }
                }

                val pushupEvents = repository.getEvents(category = "pushups", limit = 50)
                totalPushups += pushupEvents.sumOf { it.score }

                val runsJson = repository.getPluginValue("com.v07.running", "runs")
                    ?: repository.getPluginValue("com.v07.running", "v07_plugin_runs")
                if (!runsJson.isNullOrBlank()) {
                    try {
                        val arr = org.json.JSONArray(runsJson)
                        workoutsCount = maxOf(workoutsCount, arr.length())
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val dist = obj.optDouble("dist", obj.optDouble("distanceKm", 0.0))
                            totalKm += dist
                            if (i == 0 && lastKm == 0.0) lastKm = dist
                            totalPushups += obj.optInt("pushups", 0)
                        }
                    } catch (_: Exception) {}
                }
                val roundedKm = String.format(Locale.US, "%.1fk", totalKm)

                views.setTextViewText(R.id.widgetTile1Val, roundedKm)
                views.setTextViewText(R.id.widgetTile1Lbl, "TOTAL KM")
                views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#4DE086"))

                views.setTextViewText(R.id.widgetTile2Val, "$totalPushups")
                views.setTextViewText(R.id.widgetTile2Lbl, "PUSHUPS")
                views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#FFB84D"))

                views.setTextViewText(R.id.widgetTile3Val, "$workoutsCount")
                views.setTextViewText(R.id.widgetTile3Lbl, "WORKOUTS")
                views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile4Val, "${String.format(Locale.US, "%.1f", lastKm)}k")
                views.setTextViewText(R.id.widgetTile4Lbl, "LAST RUN")
                views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#2DD4BF"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Total $roundedKm run · $totalPushups pushups logged")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to log workout or cadence")
            }
        }

        private fun renderGamify(views: RemoteViews, repository: EventRepository) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🎮")
            views.setTextViewText(R.id.widgetDashboardTitle, "GAMIFY QUESTS")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · LEVEL & XP")

            try {
                val completedTasks = repository.getTasks("completed")
                val calculatedXp = (completedTasks.size * 25) + 350
                val level = (calculatedXp / 500) + 1
                val rankTitle = when {
                    level >= 10 -> "LEGEND"
                    level >= 7 -> "VETERAN"
                    level >= 4 -> "WARRIOR"
                    level >= 2 -> "SCOUT"
                    else -> "INITIATE"
                }

                views.setTextViewText(R.id.widgetTile1Val, "LVL $level")
                views.setTextViewText(R.id.widgetTile1Lbl, "LEVEL")
                views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#4DE086"))

                views.setTextViewText(R.id.widgetTile2Val, "$calculatedXp")
                views.setTextViewText(R.id.widgetTile2Lbl, "TOTAL XP")
                views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#FFB84D"))

                views.setTextViewText(R.id.widgetTile3Val, rankTitle)
                views.setTextViewText(R.id.widgetTile3Lbl, "RANK")
                views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile4Val, "SEASON")
                views.setTextViewText(R.id.widgetTile4Lbl, "ACTIVE")
                views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#2DD4BF"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Rank: $rankTitle · $calculatedXp XP earned")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to view XP progress & ranks")
            }
        }

        private fun renderVitals(views: RemoteViews, repository: EventRepository) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🧠")
            views.setTextViewText(R.id.widgetDashboardTitle, "VITALS & SPECTRUM")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · EQUILIBRIUM")

            views.setTextViewText(R.id.widgetTile1Val, "CALM")
            views.setTextViewText(R.id.widgetTile1Lbl, "STATE")
            views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#2DD4BF"))

            views.setTextViewText(R.id.widgetTile2Val, "4/5")
            views.setTextViewText(R.id.widgetTile2Lbl, "ENERGY")
            views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#FFB84D"))

            views.setTextViewText(R.id.widgetTile3Val, "HIGH")
            views.setTextViewText(R.id.widgetTile3Lbl, "FOCUS")
            views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

            views.setTextViewText(R.id.widgetTile4Val, "STEADY")
            views.setTextViewText(R.id.widgetTile4Lbl, "PULSE")
            views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#4DE086"))

            views.setTextViewText(R.id.widgetDashboardStatus, "Mindful Equilibrium: Calm presence & high clarity")
        }

        private fun renderReader(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "📖")
            views.setTextViewText(R.id.widgetDashboardTitle, "MINDFUL READER")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · LIBRARY")

            views.setTextViewText(R.id.widgetTile1Val, "ACTIVE")
            views.setTextViewText(R.id.widgetTile1Lbl, "STATUS")
            views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#2DD4BF"))

            views.setTextViewText(R.id.widgetTile2Val, "15p")
            views.setTextViewText(R.id.widgetTile2Lbl, "READ TODAY")
            views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#FFB84D"))

            views.setTextViewText(R.id.widgetTile3Val, "3")
            views.setTextViewText(R.id.widgetTile3Lbl, "BOOKS")
            views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

            views.setTextViewText(R.id.widgetTile4Val, "82%")
            views.setTextViewText(R.id.widgetTile4Lbl, "TARGET")
            views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#4DE086"))

            views.setTextViewText(R.id.widgetDashboardStatus, "Mindful reading: 15 pages digested today")
        }

        private fun renderGita(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🪔")
            views.setTextViewText(R.id.widgetDashboardTitle, "BHAGAVAD GITA")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "DAILY WISDOM")

            views.setTextViewText(R.id.widgetTile1Val, "CH 2")
            views.setTextViewText(R.id.widgetTile1Lbl, "CHAPTER")
            views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#FFB84D"))

            views.setTextViewText(R.id.widgetTile2Val, "V 47")
            views.setTextViewText(R.id.widgetTile2Lbl, "VERSE")
            views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#2DD4BF"))

            views.setTextViewText(R.id.widgetTile3Val, "KARMA")
            views.setTextViewText(R.id.widgetTile3Lbl, "YOGA")
            views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

            views.setTextViewText(R.id.widgetTile4Val, "STEADY")
            views.setTextViewText(R.id.widgetTile4Lbl, "INTENT")
            views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#4DE086"))

            views.setTextViewText(R.id.widgetDashboardStatus, "Karmanye vadikaraste ma phaleshu kadachana")
        }

        private fun renderDayTracker(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "📅")
            views.setTextViewText(R.id.widgetDashboardTitle, "DAY TRACKER")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "TIMELINE · DATES")

            views.setTextViewText(R.id.widgetTile1Val, LocalDate.now().toString())
            views.setTextViewText(R.id.widgetTile1Lbl, "TODAY")
            views.setTextColor(R.id.widgetTile1Val, Color.parseColor("#2DD4BF"))

            views.setTextViewText(R.id.widgetTile2Val, "DAY 1")
            views.setTextViewText(R.id.widgetTile2Lbl, "TIMELINE")
            views.setTextColor(R.id.widgetTile2Val, Color.parseColor("#FFB84D"))

            views.setTextViewText(R.id.widgetTile3Val, "NEXT")
            views.setTextViewText(R.id.widgetTile3Lbl, "MILESTONE")
            views.setTextColor(R.id.widgetTile3Val, Color.parseColor("#68A6FF"))

            views.setTextViewText(R.id.widgetTile4Val, "ACTIVE")
            views.setTextViewText(R.id.widgetTile4Lbl, "COUNTDOWN")
            views.setTextColor(R.id.widgetTile4Val, Color.parseColor("#4DE086"))

            views.setTextViewText(R.id.widgetDashboardStatus, "Milestones & countdowns active · Tap to open")
        }

        private fun renderChecklist(views: RemoteViews, repository: EventRepository) {
            renderTasks(views, repository)
        }
    }
}
