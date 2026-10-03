package com.v07.dashboard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class V07DayTrackerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(android.content.ComponentName(context, V07DayTrackerWidget::class.java))
        for (id in ids) updateWidget(context, mgr, id)
    }

    companion object {
        const val ACTION_REFRESH = "com.v07.dashboard.REFRESH_DAY_TRACKER"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val serviceIntent = Intent(context, DayTrackerWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val views = RemoteViews(context.packageName, R.layout.widget_daytracker)

            try {
                views.setRemoteAdapter(R.id.widgetDayTrackerList, serviceIntent)
            } catch (e: Exception) {
                android.util.Log.e("V07DTWidget", "setRemoteAdapter failed: ${e.message}")
            }

            val launchIntent = Intent(context, MainActivity::class.java)
            val launchPending = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetDaytrackerRoot, launchPending)

            views.setEmptyView(R.id.widgetDayTrackerList, R.id.widgetDayTrackerEmpty)

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widgetDayTrackerList)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
