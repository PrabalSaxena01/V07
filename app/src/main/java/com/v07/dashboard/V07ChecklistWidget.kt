package com.v07.dashboard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class V07ChecklistWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(android.content.ComponentName(context, V07ChecklistWidget::class.java))
        for (id in ids) updateWidget(context, mgr, id)
    }

    companion object {
        const val ACTION_REFRESH = "com.v07.dashboard.REFRESH_CHECKLIST"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val serviceIntent = Intent(context, ChecklistWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val views = RemoteViews(context.packageName, R.layout.widget_checklist)

            try {
                views.setRemoteAdapter(R.id.widgetChecklistList, serviceIntent)
            } catch (e: Exception) {
                android.util.Log.e("V07ChecklistWidget", "setRemoteAdapter failed: ${e.message}")
            }

            val toggleIntent = Intent(context, V07WidgetActionReceiver::class.java).apply {
                action = "TOGGLE_CHECKLIST"
            }
            val toggleTemplate = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widgetChecklistList, toggleTemplate)

            val addIntent = Intent(context, V07ChecklistInputActivity::class.java).apply {
                putExtra("widget_id", widgetId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val addPending = PendingIntent.getActivity(
                context, widgetId, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetChecklistAdd, addPending)

            val launchIntent = Intent(context, MainActivity::class.java)
            val launchPending = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetChecklistRoot, launchPending)

            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widgetChecklistList)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
