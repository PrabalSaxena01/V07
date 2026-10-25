package com.v07.v3.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.v07.dashboard.R
import com.v07.v3.app.V3Activity
import com.v07.v3.core.data.EventRepository

class V3TasksWidget : AppWidgetProvider() {

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, V3TasksWidget::class.java))
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repository = EventRepository.getInstance(context)
            val pendingTasks = try { repository.getTasks("pending") } catch (_: Exception) { emptyList() }

            val views = RemoteViews(context.packageName, R.layout.v3_widget_tasks)
            views.setTextViewText(R.id.tv_v3_tasks_count, pendingTasks.size.toString())

            if (pendingTasks.isEmpty()) {
                views.setViewVisibility(R.id.layout_v3_tasks_empty, View.VISIBLE)
                views.setViewVisibility(R.id.lv_v3_tasks_list, View.GONE)
            } else {
                views.setViewVisibility(R.id.layout_v3_tasks_empty, View.GONE)
                views.setViewVisibility(R.id.lv_v3_tasks_list, View.VISIBLE)
            }

            // Set up RemoteViewsService intent for list
            val serviceIntent = Intent(context, V3TasksWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.lv_v3_tasks_list, serviceIntent)
            views.setEmptyView(R.id.lv_v3_tasks_list, R.id.layout_v3_tasks_empty)

            // PendingIntent template for task item toggles
            val toggleIntent = Intent(context, V3WidgetActionReceiver::class.java).apply {
                action = V3WidgetActionReceiver.ACTION_TOGGLE_TASK
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.lv_v3_tasks_list, togglePendingIntent)

            // Tap + button or header to open Tasks in V3Activity
            val appIntent = Intent(context, V3Activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAV_TAB", "tasks")
            }
            val appPendingIntent = PendingIntent.getActivity(
                context, 101, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_v3_tasks_add, appPendingIntent)

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_v3_tasks_list)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
