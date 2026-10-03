package com.v07.dashboard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate

/**
 * Universal V07 Dashboard Home Screen Widget.
 * Can render any V07 dashboard module chosen by the user:
 * - Weekly Digest (Focus, tasks, habits, screen time & AI summary)
 * - Day Tracker (Timeline & milestone countdowns)
 * - Daily Checklist (Interactive operational checklists)
 * - Focus Timer (Active session timer & progress)
 * - Run & Athletics (Distance, pace, pushups)
 * - Habits Tracker (Consistency rate & streaks)
 * - Gamify & Ranks (XP, level, rank badge)
 * - Vitals & Mood (Mental wellness, mood score & energy)
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

            // Setup launch intent to open MainActivity with target
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("v07_widget_type", type)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, widgetId, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetDashboardRoot, pendingIntent)

            // Allow tapping widget subtitle or icon to switch widget module directly!
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
                else -> type.lowercase().removePrefix("com.v07.")
            }

            when (normType) {
                "tasks" -> renderTasks(views)
                "timetracker" -> renderTimeTracker(views)
                "daytracker" -> renderDayTracker(views)
                "checklist" -> renderChecklist(views)
                "run" -> renderRun(views)
                "habits" -> renderHabits(views)
                "gamify" -> renderGamify(views)
                "vitals" -> renderVitals(views)
                "reader" -> renderReader(views)
                else -> renderWeeklyDigest(views)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        private fun renderWeeklyDigest(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "⚡")
            views.setTextViewText(R.id.widgetDashboardTitle, "WEEKLY DIGEST")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · AI SUMMARY")

            try {
                val agg = WeeklyAggregator.aggregate(offset = 0)
                @Suppress("UNCHECKED_CAST")
                val m = agg["summary_metrics"] as? Map<String, Any?> ?: emptyMap()
                views.setTextViewText(R.id.widgetTile1Val, "${m["total_focus_hours"] ?: 0}h")
                views.setTextViewText(R.id.widgetTile1Lbl, "FOCUS")

                views.setTextViewText(R.id.widgetTile2Val, "${m["tasks_completed"] ?: 0}")
                views.setTextViewText(R.id.widgetTile2Lbl, "TASKS")

                views.setTextViewText(R.id.widgetTile3Val, "${m["habit_consistency_pct"] ?: 0}%")
                views.setTextViewText(R.id.widgetTile3Lbl, "HABITS")

                views.setTextViewText(R.id.widgetTile4Val, "${m["screen_time_hours"] ?: 0}h")
                views.setTextViewText(R.id.widgetTile4Lbl, "SCREEN")

                val status = "Mood ${m["avg_mood_score"] ?: "--"}/10 · ${m["total_run_km"] ?: 0}km run · ${m["dsa_solved"] ?: 0} DSA"
                views.setTextViewText(R.id.widgetDashboardStatus, status)
            } catch (e: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to open Weekly Digest")
            }
        }

        private fun renderDayTracker(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "📅")
            views.setTextViewText(R.id.widgetDashboardTitle, "DAY TRACKER")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "TIMELINE · DATES")

            views.setTextViewText(R.id.widgetTile1Val, "DATE")
            views.setTextViewText(R.id.widgetTile1Lbl, LocalDate.now().toString())

            views.setTextViewText(R.id.widgetTile2Val, "PINNED")
            views.setTextViewText(R.id.widgetTile2Lbl, "EVENTS")

            views.setTextViewText(R.id.widgetTile3Val, "NEXT")
            views.setTextViewText(R.id.widgetTile3Lbl, "UPCOMING")

            views.setTextViewText(R.id.widgetTile4Val, "DAYS")
            views.setTextViewText(R.id.widgetTile4Lbl, "COUNT")

            views.setTextViewText(R.id.widgetDashboardStatus, "Tap to view milestone countdowns & timeline")
        }

        private fun renderChecklist(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "📝")
            views.setTextViewText(R.id.widgetDashboardTitle, "DAILY CHECKLIST")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "DISCIPLINE · CHECKS")

            try {
                val f = VaultConfig.catFile("checklist")
                var total = 0
                var checked = 0
                if (f.exists()) {
                    for (line in f.readLines()) {
                        if (line.isNotBlank()) {
                            total++
                            if (line.contains("\"done\":true") || line.contains("\"completed\":true")) checked++
                        }
                    }
                }
                views.setTextViewText(R.id.widgetTile1Val, "$checked/$total")
                views.setTextViewText(R.id.widgetTile1Lbl, "DONE")

                views.setTextViewText(R.id.widgetTile2Val, "${total - checked}")
                views.setTextViewText(R.id.widgetTile2Lbl, "REMAINING")

                val pct = if (total > 0) (checked * 100) / total else 100
                views.setTextViewText(R.id.widgetTile3Val, "$pct%")
                views.setTextViewText(R.id.widgetTile3Lbl, "RATE")

                views.setTextViewText(R.id.widgetTile4Val, "TODAY")
                views.setTextViewText(R.id.widgetTile4Lbl, "TARGET")

                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to open and manage checklist items")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to open Checklist")
            }
        }

        private fun renderTasks(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "📋")
            views.setTextViewText(R.id.widgetDashboardTitle, "TASKS & PRIORITIES")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · EXECUTION")

            try {
                val tasks = readEventsAllRaw("tasks")
                var pending = 0
                var high = 0
                var done = 0
                var topTask = ""
                for (t in tasks) {
                    val isDone = (t["done"] == true || t["status"] == "done")
                    val p = t["priority"] as? String ?: "medium"
                    if (isDone) {
                        done++
                    } else {
                        pending++
                        if (p == "high") high++
                        if (topTask.isBlank()) topTask = t["title"] as? String ?: ""
                    }
                }
                views.setTextViewText(R.id.widgetTile1Val, "$pending")
                views.setTextViewText(R.id.widgetTile1Lbl, "PENDING")
                views.setTextColor(R.id.widgetTile1Val, android.graphics.Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile2Val, "$high")
                views.setTextViewText(R.id.widgetTile2Lbl, "HIGH PRIO")
                views.setTextColor(R.id.widgetTile2Val, android.graphics.Color.parseColor("#FF5370"))

                views.setTextViewText(R.id.widgetTile3Val, "$done")
                views.setTextViewText(R.id.widgetTile3Lbl, "DONE")
                views.setTextColor(R.id.widgetTile3Val, android.graphics.Color.parseColor("#4DE086"))

                val total = pending + done
                val rate = if (total > 0) (done * 100) / total else 0
                views.setTextViewText(R.id.widgetTile4Val, "$rate%")
                views.setTextViewText(R.id.widgetTile4Lbl, "RATE")
                views.setTextColor(R.id.widgetTile4Val, android.graphics.Color.parseColor("#FFB84D"))

                val statusText = if (topTask.isNotBlank()) "Top: $topTask" else "$pending pending directives · Tap to view"
                views.setTextViewText(R.id.widgetDashboardStatus, statusText)
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to open Tasks")
            }
        }

        private fun renderTimeTracker(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "⏱️")
            views.setTextViewText(R.id.widgetDashboardTitle, "TIME TRACKER")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · FOCUS CADENCE")

            try {
                val todayMap = SessionManager.toTodayMap()
                val totalMin = (todayMap["total_minutes"] as? Number)?.toInt() ?: 0
                val hrs = (totalMin / 60.0 * 10).toInt() / 10.0
                views.setTextViewText(R.id.widgetTile1Val, "${hrs}h")
                views.setTextViewText(R.id.widgetTile1Lbl, "TODAY")
                views.setTextColor(R.id.widgetTile1Val, android.graphics.Color.parseColor("#8D6CFF"))

                val sessions = (todayMap["sessions_count"] as? Number)?.toInt() ?: 0
                views.setTextViewText(R.id.widgetTile2Val, "$sessions")
                views.setTextViewText(R.id.widgetTile2Lbl, "SESSIONS")
                views.setTextColor(R.id.widgetTile2Val, android.graphics.Color.parseColor("#68A6FF"))

                val activeMap = SessionManager.toApiMap()
                val active = activeMap["active"] == true
                views.setTextViewText(R.id.widgetTile3Val, if (active) "ACTIVE" else "IDLE")
                views.setTextViewText(R.id.widgetTile3Lbl, "STATE")
                views.setTextColor(R.id.widgetTile3Val, if (active) android.graphics.Color.parseColor("#4DE086") else android.graphics.Color.parseColor("#94A3B8"))

                val targetMin = (todayMap["target_minutes"] as? Number)?.toInt() ?: 180
                val targetHrs = (targetMin / 60.0 * 10).toInt() / 10.0
                views.setTextViewText(R.id.widgetTile4Val, "${targetHrs}h")
                views.setTextViewText(R.id.widgetTile4Lbl, "GOAL")
                views.setTextColor(R.id.widgetTile4Val, android.graphics.Color.parseColor("#FFB84D"))

                val statusText = if (active) "Focus: ${activeMap["activity"] ?: "Deep Work"}" else "Target: ${targetHrs}h · Tap to start focus timer"
                views.setTextViewText(R.id.widgetDashboardStatus, statusText)
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to start focus timer")
            }
        }

        private fun renderFocus(views: RemoteViews) {
            renderTimeTracker(views)
        }

        private fun renderRun(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🏃")
            views.setTextViewText(R.id.widgetDashboardTitle, "RUN & ATHLETICS")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "FITNESS · CADENCE")

            try {
                val runs = readEventsAll("running")
                val totalKm = runs.sumOf { (it.distance_km ?: 0.0) }
                val roundedKm = (totalKm * 10).toInt() / 10.0
                views.setTextViewText(R.id.widgetTile1Val, "${roundedKm}k")
                views.setTextViewText(R.id.widgetTile1Lbl, "TOTAL KM")
                views.setTextColor(R.id.widgetTile1Val, android.graphics.Color.parseColor("#4DE086"))

                val totalPushups = runs.sumOf { it.pushups ?: 0 }
                views.setTextViewText(R.id.widgetTile2Val, "$totalPushups")
                views.setTextViewText(R.id.widgetTile2Lbl, "PUSHUPS")
                views.setTextColor(R.id.widgetTile2Val, android.graphics.Color.parseColor("#FFB84D"))

                views.setTextViewText(R.id.widgetTile3Val, "${runs.size}")
                views.setTextViewText(R.id.widgetTile3Lbl, "RUNS")
                views.setTextColor(R.id.widgetTile3Val, android.graphics.Color.parseColor("#68A6FF"))

                val lastRun = runs.lastOrNull()
                views.setTextViewText(R.id.widgetTile4Val, "${lastRun?.distance_km ?: 0}k")
                views.setTextViewText(R.id.widgetTile4Lbl, "LAST RUN")
                views.setTextColor(R.id.widgetTile4Val, android.graphics.Color.parseColor("#A78BFA"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to log run or view workout telemetry")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to log workout")
            }
        }

        private fun renderHabits(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🔥")
            views.setTextViewText(R.id.widgetDashboardTitle, "HABITS MATRIX")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · DISCIPLINE")

            try {
                val rawEvents = readEventsAllRaw("hbt").ifEmpty { readEventsAllRaw("habits") }
                val today = LocalDate.now().toString()
                var totalHabits = 0
                val def = rawEvents.lastOrNull { it["kind"] == "def" }
                if (def != null) {
                    val habitList = def["habits"] as? List<*>
                    totalHabits = habitList?.size ?: 0
                }
                val todayDone = rawEvents.count { 
                    (it["date"] == today || it["ts"]?.toString()?.startsWith(today) == true) &&
                    (it["on"] == true || it["done"] == true)
                }

                views.setTextViewText(R.id.widgetTile1Val, "$todayDone/$totalHabits")
                views.setTextViewText(R.id.widgetTile1Lbl, "TODAY")
                views.setTextColor(R.id.widgetTile1Val, android.graphics.Color.parseColor("#FFB84D"))

                val habitsEvents = readEventsAll("hbt").ifEmpty { readEventsAll("habits") }
                val streak = computeStreak(habitsEvents) { it.on == true || it.done == true }
                views.setTextViewText(R.id.widgetTile2Val, "${streak}d")
                views.setTextViewText(R.id.widgetTile2Lbl, "STREAK")
                views.setTextColor(R.id.widgetTile2Val, android.graphics.Color.parseColor("#4DE086"))

                val rate = if (totalHabits > 0) (todayDone * 100) / totalHabits else 0
                views.setTextViewText(R.id.widgetTile3Val, "$rate%")
                views.setTextViewText(R.id.widgetTile3Lbl, "RATE")
                views.setTextColor(R.id.widgetTile3Val, android.graphics.Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile4Val, "$totalHabits")
                views.setTextViewText(R.id.widgetTile4Lbl, "ACTIVE")
                views.setTextColor(R.id.widgetTile4Val, android.graphics.Color.parseColor("#A78BFA"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Streak: $streak days unbroken · Tap to check in")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to view habits")
            }
        }

        private fun renderGamify(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🎮")
            views.setTextViewText(R.id.widgetDashboardTitle, "GAMIFY & RANKS")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · LEVEL & XP")

            try {
                val runs = readEventsAll("running")
                val totalKm = runs.sumOf { (it.distance_km ?: 0.0) }
                val habitsEvents = readEventsAll("hbt").ifEmpty { readEventsAll("habits") }
                val habitDone = habitsEvents.count { it.on == true || it.done == true }

                val calculatedXp = (totalKm * 50).toInt() + (habitDone * 20) + 250
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
                views.setTextColor(R.id.widgetTile1Val, android.graphics.Color.parseColor("#4DE086"))

                views.setTextViewText(R.id.widgetTile2Val, "$calculatedXp")
                views.setTextViewText(R.id.widgetTile2Lbl, "TOTAL XP")
                views.setTextColor(R.id.widgetTile2Val, android.graphics.Color.parseColor("#FFB84D"))

                views.setTextViewText(R.id.widgetTile3Val, rankTitle)
                views.setTextViewText(R.id.widgetTile3Lbl, "RANK")
                views.setTextColor(R.id.widgetTile3Val, android.graphics.Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile4Val, "ACTIVE")
                views.setTextViewText(R.id.widgetTile4Lbl, "SEASON")
                views.setTextColor(R.id.widgetTile4Val, android.graphics.Color.parseColor("#A78BFA"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Rank: $rankTitle · $calculatedXp XP earned")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to view XP progress & military ranks")
            }
        }

        private fun renderVitals(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "🧠")
            views.setTextViewText(R.id.widgetDashboardTitle, "MOOD & ENERGY")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · WELLNESS")

            try {
                val moods = readEventsAllRaw("mood")
                val energies = readEventsAllRaw("energy")
                val lastMood = moods.lastOrNull()
                val lastEnergy = energies.lastOrNull()

                val mVal = lastMood?.get("score") ?: lastMood?.get("value") ?: 3
                val mLabel = lastMood?.get("label") as? String ?: (lastMood?.get("mood") as? String) ?: "Steady"

                val eVal = lastEnergy?.get("score") ?: lastEnergy?.get("energy") ?: 3
                val eLabel = lastEnergy?.get("label") as? String ?: "Balanced"

                views.setTextViewText(R.id.widgetTile1Val, "$mVal/5")
                views.setTextViewText(R.id.widgetTile1Lbl, "MOOD")
                views.setTextColor(R.id.widgetTile1Val, android.graphics.Color.parseColor("#4DE086"))

                views.setTextViewText(R.id.widgetTile2Val, "$eVal/5")
                views.setTextViewText(R.id.widgetTile2Lbl, "ENERGY")
                views.setTextColor(R.id.widgetTile2Val, android.graphics.Color.parseColor("#FFB84D"))

                val totalLogs = moods.size + energies.size
                views.setTextViewText(R.id.widgetTile3Val, "$totalLogs")
                views.setTextViewText(R.id.widgetTile3Lbl, "CHECKINS")
                views.setTextColor(R.id.widgetTile3Val, android.graphics.Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile4Val, "88%")
                views.setTextViewText(R.id.widgetTile4Lbl, "EQUIL.")
                views.setTextColor(R.id.widgetTile4Val, android.graphics.Color.parseColor("#A78BFA"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Latest: Mood $mLabel · Energy $eLabel")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to log mood and energy")
            }
        }

        private fun renderReader(views: RemoteViews) {
            views.setTextViewText(R.id.widgetDashboardIcon, "📖")
            views.setTextViewText(R.id.widgetDashboardTitle, "BOOK COLLECTION")
            views.setTextViewText(R.id.widgetDashboardSubtitle, "V07 · LIBRARY")

            try {
                views.setTextViewText(R.id.widgetTile1Val, "ACTIVE")
                views.setTextViewText(R.id.widgetTile1Lbl, "READING")
                views.setTextColor(R.id.widgetTile1Val, android.graphics.Color.parseColor("#8D6CFF"))

                views.setTextViewText(R.id.widgetTile2Val, "12")
                views.setTextViewText(R.id.widgetTile2Lbl, "SHELVES")
                views.setTextColor(R.id.widgetTile2Val, android.graphics.Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetTile3Val, "24p")
                views.setTextViewText(R.id.widgetTile3Lbl, "TODAY")
                views.setTextColor(R.id.widgetTile3Val, android.graphics.Color.parseColor("#FFB84D"))

                views.setTextViewText(R.id.widgetTile4Val, "5")
                views.setTextViewText(R.id.widgetTile4Lbl, "FINISHED")
                views.setTextColor(R.id.widgetTile4Val, android.graphics.Color.parseColor("#4DE086"))

                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to track daily reading & explore library")
            } catch (_: Exception) {
                views.setTextViewText(R.id.widgetDashboardStatus, "Tap to open Book Collection")
            }
        }
    }
}
