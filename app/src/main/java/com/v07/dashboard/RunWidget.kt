package com.v07.dashboard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class RunWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(android.content.ComponentName(context, RunWidget::class.java))
        for (id in ids) updateWidget(context, mgr, id)
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_run)

            val inputIntent = Intent(context, RunInputActivity::class.java).apply {
                putExtra("widget_id", widgetId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val inputPending = PendingIntent.getActivity(
                context, widgetId, inputIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRunRoot, inputPending)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
