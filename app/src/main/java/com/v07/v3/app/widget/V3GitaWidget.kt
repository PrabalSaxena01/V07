package com.v07.v3.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.v07.dashboard.R
import com.v07.v3.app.V3Activity
import com.v07.v3.core.data.GitaProvider

class V3GitaWidget : AppWidgetProvider() {

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, V3GitaWidget::class.java))
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val gitaProvider = GitaProvider.getInstance(context)
            val verse = try { gitaProvider.getDailyVerse() } catch (_: Exception) { null }

            val views = RemoteViews(context.packageName, R.layout.v3_widget_gita)

            // Tap root to open Gita in V3Activity
            val appIntent = Intent(context, V3Activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAV_TAB", "gita")
            }
            val appPendingIntent = PendingIntent.getActivity(
                context, 201, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.v3_gita_root, appPendingIntent)

            if (verse != null) {
                val ch = verse.optInt("chapter", 2)
                val vs = verse.optInt("verse", 47)
                val vid = "$ch.$vs"
                views.setTextViewText(R.id.tv_v3_gita_citation, "BG $vid")

                val sanskrit = verse.optString("text", verse.optString("shloka", "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन..."))
                views.setTextViewText(R.id.tv_v3_gita_sanskrit, sanskrit)

                val meaning = verse.optString("meaning", verse.optString("translation", "Perform your duty with dedication, without clinging to results."))
                views.setTextViewText(R.id.tv_v3_gita_meaning, meaning)

                val isRead = verse.optBoolean("already_read", false)
                views.setTextViewText(R.id.btn_v3_gita_read, if (isRead) "Read ✓" else "Mark Read")

                val progress = gitaProvider.getProgress()
                val readCount = progress.optInt("read_count", 0)
                views.setTextViewText(R.id.tv_v3_gita_progress, "$readCount / 701 Verses Read")

                // PendingIntent for Next Verse
                views.setOnClickPendingIntent(
                    R.id.btn_v3_gita_next,
                    V3WidgetActionReceiver.createActionIntent(context, V3WidgetActionReceiver.ACTION_NEXT_GITA_VERSE)
                )

                // PendingIntent for Mark Read
                val readIntent = Intent(context, V3WidgetActionReceiver::class.java).apply {
                    action = V3WidgetActionReceiver.ACTION_MARK_GITA_READ
                    putExtra(V3WidgetActionReceiver.EXTRA_VERSE_CHAPTER, ch)
                    putExtra(V3WidgetActionReceiver.EXTRA_VERSE_NUMBER, vs)
                }
                views.setOnClickPendingIntent(
                    R.id.btn_v3_gita_read,
                    PendingIntent.getBroadcast(
                        context, 202, readIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
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
