package com.v07.v3.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.v07.dashboard.R
import com.v07.v3.app.V3Activity
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerStatus
import com.v07.v3.core.data.EventRepository

class V3FocusWidget : AppWidgetProvider() {

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, V3FocusWidget::class.java))
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repository = EventRepository.getInstance(context)
            val timerManager = TimerManager.getInstance(context, repository)
            val timer = timerManager.timerState.value

            val views = RemoteViews(context.packageName, R.layout.v3_widget_focus)

            // Tap root to open V3Activity
            val appIntent = Intent(context, V3Activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(
                context, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.v3_focus_root, appPendingIntent)

            when (timer.status) {
                TimerStatus.RUNNING -> {
                    views.setTextViewText(R.id.tv_v3_focus_status, "RUNNING")
                    views.setTextColor(R.id.tv_v3_focus_status, Color.parseColor("#10B981"))
                    views.setInt(R.id.tv_v3_focus_status, "setBackgroundResource", R.drawable.v3_widget_pill_emerald)

                    views.setTextViewText(R.id.tv_v3_focus_topic, timer.topic.ifBlank { timer.category })
                    views.setTextViewText(R.id.tv_v3_focus_timer, timer.getFormattedTime())

                    views.setViewVisibility(R.id.layout_v3_focus_controls_running, View.VISIBLE)
                    views.setViewVisibility(R.id.layout_v3_focus_controls_paused, View.GONE)
                    views.setViewVisibility(R.id.layout_v3_focus_controls_idle, View.GONE)

                    views.setOnClickPendingIntent(
                        R.id.btn_v3_focus_pause,
                        V3WidgetActionReceiver.createActionIntent(context, V3WidgetActionReceiver.ACTION_PAUSE_TIMER)
                    )
                    views.setOnClickPendingIntent(
                        R.id.btn_v3_focus_stop,
                        V3WidgetActionReceiver.createActionIntent(context, V3WidgetActionReceiver.ACTION_STOP_TIMER)
                    )
                }
                TimerStatus.PAUSED -> {
                    views.setTextViewText(R.id.tv_v3_focus_status, "PAUSED")
                    views.setTextColor(R.id.tv_v3_focus_status, Color.parseColor("#F59E0B"))
                    views.setInt(R.id.tv_v3_focus_status, "setBackgroundResource", R.drawable.v3_widget_pill_amber)

                    views.setTextViewText(R.id.tv_v3_focus_topic, timer.topic.ifBlank { timer.category })
                    views.setTextViewText(R.id.tv_v3_focus_timer, timer.getFormattedTime())

                    views.setViewVisibility(R.id.layout_v3_focus_controls_running, View.GONE)
                    views.setViewVisibility(R.id.layout_v3_focus_controls_paused, View.VISIBLE)
                    views.setViewVisibility(R.id.layout_v3_focus_controls_idle, View.GONE)

                    views.setOnClickPendingIntent(
                        R.id.btn_v3_focus_resume,
                        V3WidgetActionReceiver.createActionIntent(context, V3WidgetActionReceiver.ACTION_RESUME_TIMER)
                    )
                    views.setOnClickPendingIntent(
                        R.id.btn_v3_focus_stop_paused,
                        V3WidgetActionReceiver.createActionIntent(context, V3WidgetActionReceiver.ACTION_STOP_TIMER)
                    )
                }
                else -> {
                    views.setTextViewText(R.id.tv_v3_focus_status, "IDLE")
                    views.setTextColor(R.id.tv_v3_focus_status, Color.parseColor("#94A3B8"))
                    views.setInt(R.id.tv_v3_focus_status, "setBackgroundResource", R.drawable.v3_widget_pill_slate)

                    views.setTextViewText(R.id.tv_v3_focus_topic, "Ready to focus")
                    views.setTextViewText(R.id.tv_v3_focus_timer, "00:00")

                    views.setViewVisibility(R.id.layout_v3_focus_controls_running, View.GONE)
                    views.setViewVisibility(R.id.layout_v3_focus_controls_paused, View.GONE)
                    views.setViewVisibility(R.id.layout_v3_focus_controls_idle, View.VISIBLE)

                    views.setOnClickPendingIntent(
                        R.id.btn_v3_focus_start_25,
                        V3WidgetActionReceiver.createActionIntent(context, V3WidgetActionReceiver.ACTION_START_FOCUS_25)
                    )
                    views.setOnClickPendingIntent(
                        R.id.btn_v3_focus_start_stopwatch,
                        V3WidgetActionReceiver.createActionIntent(context, V3WidgetActionReceiver.ACTION_START_STOPWATCH)
                    )
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
