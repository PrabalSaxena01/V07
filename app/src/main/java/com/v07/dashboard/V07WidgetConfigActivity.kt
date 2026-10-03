package com.v07.dashboard

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

/**
 * Configuration Activity for V07 Dashboard Home Screen Widget.
 * Shows a real-time live preview of the widget and lets the user choose
 * which V07 module to render:
 * Weekly Digest, Tasks, Time Tracker, Day Tracker, Checklist, Run, Habits, Gamify, Vitals, Reader.
 */
class V07WidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedType = "weekly"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_widget_config)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val prefs = getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)
        selectedType = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getString("widget_type_$appWidgetId", null)
                ?: prefs.getString("default_widget_type", "weekly") ?: "weekly"
        } else {
            prefs.getString("default_widget_type", "weekly") ?: "weekly"
        }

        setupChoice(R.id.choiceWeeklyDigest, "weekly")
        setupChoice(R.id.choiceTasks, "tasks")
        setupChoice(R.id.choiceTimeTracker, "timetracker")
        setupChoice(R.id.choiceDayTracker, "daytracker")
        setupChoice(R.id.choiceChecklist, "checklist")
        setupChoice(R.id.choiceFocus, "timetracker")
        setupChoice(R.id.choiceRun, "run")
        setupChoice(R.id.choiceHabits, "habits")
        setupChoice(R.id.choiceGamify, "gamify")
        setupChoice(R.id.choiceVitals, "vitals")
        setupChoice(R.id.choiceReader, "reader")

        findViewById<Button>(R.id.btnApplySelectedWidget)?.setOnClickListener {
            commitWidgetType(selectedType)
        }

        findViewById<View>(R.id.btnCancelConfig)?.setOnClickListener {
            finish()
        }

        updatePreview(selectedType)
    }

    private fun setupChoice(viewId: Int, widgetType: String) {
        findViewById<View>(viewId)?.setOnClickListener {
            selectedType = widgetType
            updatePreview(widgetType)
        }
    }

    private fun updatePreview(type: String) {
        val root = findViewById<View>(R.id.previewWidgetLayout) ?: return
        val icon = root.findViewById<TextView>(R.id.widgetDashboardIcon)
        val title = root.findViewById<TextView>(R.id.widgetDashboardTitle)
        val sub = root.findViewById<TextView>(R.id.widgetDashboardSubtitle)
        val t1V = root.findViewById<TextView>(R.id.widgetTile1Val)
        val t1L = root.findViewById<TextView>(R.id.widgetTile1Lbl)
        val t2V = root.findViewById<TextView>(R.id.widgetTile2Val)
        val t2L = root.findViewById<TextView>(R.id.widgetTile2Lbl)
        val t3V = root.findViewById<TextView>(R.id.widgetTile3Val)
        val t3L = root.findViewById<TextView>(R.id.widgetTile3Lbl)
        val t4V = root.findViewById<TextView>(R.id.widgetTile4Val)
        val t4L = root.findViewById<TextView>(R.id.widgetTile4Lbl)
        val status = root.findViewById<TextView>(R.id.widgetDashboardStatus)

        when (type) {
            "tasks" -> {
                icon?.text = "📋"
                title?.text = "TASKS & PRIORITIES"
                sub?.text = "V07 · EXECUTION"
                t1V?.text = "4"; t1L?.text = "PENDING"; t1V?.setTextColor(Color.parseColor("#68A6FF"))
                t2V?.text = "2"; t2L?.text = "HIGH PRIO"; t2V?.setTextColor(Color.parseColor("#FF5370"))
                t3V?.text = "8"; t3L?.text = "DONE"; t3V?.setTextColor(Color.parseColor("#4DE086"))
                t4V?.text = "80%"; t4L?.text = "RATE"; t4V?.setTextColor(Color.parseColor("#FFB84D"))
                status?.text = "Top: Review architecture & deployment"
            }
            "timetracker", "focus" -> {
                icon?.text = "⏱️"
                title?.text = "TIME TRACKER"
                sub?.text = "V07 · FOCUS CADENCE"
                t1V?.text = "3.2h"; t1L?.text = "TODAY"; t1V?.setTextColor(Color.parseColor("#8D6CFF"))
                t2V?.text = "4"; t2L?.text = "SESSIONS"; t2V?.setTextColor(Color.parseColor("#68A6FF"))
                t3V?.text = "ACTIVE"; t3L?.text = "STATE"; t3V?.setTextColor(Color.parseColor("#4DE086"))
                t4V?.text = "4.0h"; t4L?.text = "GOAL"; t4V?.setTextColor(Color.parseColor("#FFB84D"))
                status?.text = "Focus: Deep Work (35m elapsed)"
            }
            "habits" -> {
                icon?.text = "🔥"
                title?.text = "HABITS MATRIX"
                sub?.text = "V07 · DISCIPLINE"
                t1V?.text = "5/6"; t1L?.text = "TODAY"; t1V?.setTextColor(Color.parseColor("#FFB84D"))
                t2V?.text = "7d"; t2L?.text = "STREAK"; t2V?.setTextColor(Color.parseColor("#4DE086"))
                t3V?.text = "83%"; t3L?.text = "RATE"; t3V?.setTextColor(Color.parseColor("#68A6FF"))
                t4V?.text = "6"; t4L?.text = "ACTIVE"; t4V?.setTextColor(Color.parseColor("#A78BFA"))
                status?.text = "Streak: 7 days unbroken · Tap to check in"
            }
            "gamify" -> {
                icon?.text = "🎮"
                title?.text = "GAMIFY & RANKS"
                sub?.text = "V07 · LEVEL & XP"
                t1V?.text = "LVL 5"; t1L?.text = "LEVEL"; t1V?.setTextColor(Color.parseColor("#4DE086"))
                t2V?.text = "1,450"; t2L?.text = "TOTAL XP"; t2V?.setTextColor(Color.parseColor("#FFB84D"))
                t3V?.text = "VETERAN"; t3L?.text = "RANK"; t3V?.setTextColor(Color.parseColor("#68A6FF"))
                t4V?.text = "3/4"; t4L?.text = "QUESTS"; t4V?.setTextColor(Color.parseColor("#A78BFA"))
                status?.text = "Rank: Veteran · 550 XP to Centurion"
            }
            "vitals" -> {
                icon?.text = "🧠"
                title?.text = "MOOD & ENERGY"
                sub?.text = "V07 · WELLNESS"
                t1V?.text = "4/5"; t1L?.text = "MOOD"; t1V?.setTextColor(Color.parseColor("#4DE086"))
                t2V?.text = "4/5"; t2L?.text = "ENERGY"; t2V?.setTextColor(Color.parseColor("#FFB84D"))
                t3V?.text = "3"; t3L?.text = "CHECKINS"; t3V?.setTextColor(Color.parseColor("#68A6FF"))
                t4V?.text = "88%"; t4L?.text = "EQUIL."; t4V?.setTextColor(Color.parseColor("#A78BFA"))
                status?.text = "Latest: Mood Steady · Energy Balanced"
            }
            "reader" -> {
                icon?.text = "📖"
                title?.text = "BOOK COLLECTION"
                sub?.text = "V07 · LIBRARY"
                t1V?.text = "ACTIVE"; t1L?.text = "READING"; t1V?.setTextColor(Color.parseColor("#8D6CFF"))
                t2V?.text = "12"; t2L?.text = "SHELVES"; t2V?.setTextColor(Color.parseColor("#68A6FF"))
                t3V?.text = "24p"; t3L?.text = "TODAY"; t3V?.setTextColor(Color.parseColor("#FFB84D"))
                t4V?.text = "5"; t4L?.text = "FINISHED"; t4V?.setTextColor(Color.parseColor("#4DE086"))
                status?.text = "Tap to track daily reading & explore library"
            }
            "run" -> {
                icon?.text = "🏃"
                title?.text = "RUN & ATHLETICS"
                sub?.text = "FITNESS · CADENCE"
                t1V?.text = "12.4k"; t1L?.text = "TOTAL KM"; t1V?.setTextColor(Color.parseColor("#4DE086"))
                t2V?.text = "120"; t2L?.text = "PUSHUPS"; t2V?.setTextColor(Color.parseColor("#FFB84D"))
                t3V?.text = "4"; t3L?.text = "RUNS"; t3V?.setTextColor(Color.parseColor("#68A6FF"))
                t4V?.text = "3.4k"; t4L?.text = "LAST RUN"; t4V?.setTextColor(Color.parseColor("#A78BFA"))
                status?.text = "Tap to log run or view workout telemetry"
            }
            "daytracker" -> {
                icon?.text = "📅"
                title?.text = "DAY TRACKER"
                sub?.text = "TIMELINE · DATES"
                t1V?.text = "TODAY"; t1L?.text = "DATE"; t1V?.setTextColor(Color.parseColor("#68A6FF"))
                t2V?.text = "PINNED"; t2L?.text = "EVENTS"; t2V?.setTextColor(Color.parseColor("#FFB84D"))
                t3V?.text = "NEXT"; t3L?.text = "UPCOMING"; t3V?.setTextColor(Color.parseColor("#4DE086"))
                t4V?.text = "DAYS"; t4L?.text = "COUNT"; t4V?.setTextColor(Color.parseColor("#A78BFA"))
                status?.text = "Milestones & countdowns active"
            }
            "checklist" -> {
                icon?.text = "📝"
                title?.text = "DAILY CHECKLIST"
                sub?.text = "DISCIPLINE · CHECKS"
                t1V?.text = "6/8"; t1L?.text = "DONE"; t1V?.setTextColor(Color.parseColor("#4DE086"))
                t2V?.text = "2"; t2L?.text = "REMAINING"; t2V?.setTextColor(Color.parseColor("#FF5370"))
                t3V?.text = "75%"; t3L?.text = "RATE"; t3V?.setTextColor(Color.parseColor("#68A6FF"))
                t4V?.text = "TODAY"; t4L?.text = "TARGET"; t4V?.setTextColor(Color.parseColor("#FFB84D"))
                status?.text = "Tap to open and manage checklist items"
            }
            else -> {
                icon?.text = "⚡"
                title?.text = "WEEKLY DIGEST"
                sub?.text = "V07 · AI SUMMARY"
                t1V?.text = "18.4h"; t1L?.text = "FOCUS"; t1V?.setTextColor(Color.parseColor("#8D6CFF"))
                t2V?.text = "24"; t2L?.text = "TASKS"; t2V?.setTextColor(Color.parseColor("#68A6FF"))
                t3V?.text = "88%"; t3L?.text = "HABITS"; t3V?.setTextColor(Color.parseColor("#4DE086"))
                t4V?.text = "14.2h"; t4L?.text = "SCREEN"; t4V?.setTextColor(Color.parseColor("#FFB84D"))
                status?.text = "Weekly focus hours, habits, tasks & screen time"
            }
        }
    }

    private fun commitWidgetType(type: String) {
        val prefs = getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.edit().putString("widget_type_$appWidgetId", type).putString("default_widget_type", type).apply()
            val mgr = AppWidgetManager.getInstance(this)
            V07DashboardWidget.updateWidget(this, mgr, appWidgetId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
        } else {
            // Pin/type requested from within app without existing id
            prefs.edit().putString("default_widget_type", type).apply()
            setResult(RESULT_OK)

            val mgr = AppWidgetManager.getInstance(this)
            val allIds = mgr.getAppWidgetIds(android.content.ComponentName(this, V07DashboardWidget::class.java))
            for (id in allIds) {
                prefs.edit().putString("widget_type_$id", type).apply()
                V07DashboardWidget.updateWidget(this, mgr, id)
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && mgr.isRequestPinAppWidgetSupported) {
                val componentName = android.content.ComponentName(this, V07DashboardWidget::class.java)
                val successIntent = Intent(this, V07DashboardWidget::class.java).apply {
                    action = "com.v07.dashboard.PINNED_WIDGET_CONFIG"
                    putExtra("preferred_widget_type", type)
                }
                val successCallback = android.app.PendingIntent.getBroadcast(
                    this, 0, successIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
                )
                mgr.requestPinAppWidget(componentName, null, successCallback)
            }
        }
        finish()
    }
}
