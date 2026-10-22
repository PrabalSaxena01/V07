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
import com.v07.v3.core.data.EventRepository
import java.util.Locale

class RunWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, RunWidget::class.java))
        for (id in ids) updateWidget(context, mgr, id)
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_run)

            // Tapping widget opens V3 Athletics plugin directly
            val launchIntent = Intent(context, V3Activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("openPlugin", "com.v07.running")
            }
            val launchPending = PendingIntent.getActivity(
                context, widgetId + 30000, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRunRoot, launchPending)

            try {
                val repository = EventRepository.getInstance(context)
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

                views.setTextViewText(R.id.widgetRunDistance, roundedKm)
                views.setTextColor(R.id.widgetRunDistance, Color.parseColor("#4DE086"))

                views.setTextViewText(R.id.widgetRunPushups, "$totalPushups")
                views.setTextColor(R.id.widgetRunPushups, Color.parseColor("#FFB84D"))

                views.setTextViewText(R.id.widgetRunSessions, "$workoutsCount")
                views.setTextColor(R.id.widgetRunSessions, Color.parseColor("#68A6FF"))

                views.setTextViewText(R.id.widgetRunLast, "${String.format(Locale.US, "%.1f", lastKm)}k")
                views.setTextColor(R.id.widgetRunLast, Color.parseColor("#2DD4BF"))

                views.setTextViewText(
                    R.id.widgetRunStatus,
                    "Total $roundedKm run · $totalPushups pushups · $workoutsCount workouts"
                )
            } catch (e: Exception) {
                views.setTextViewText(R.id.widgetRunStatus, "Tap to open Sanctuary Athletics")
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
