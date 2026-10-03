package com.v07.dashboard

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File
import java.time.OffsetDateTime

class V07WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val widgetId = intent.getIntExtra("widget_id", -1)

        Log.i("V07Widget", "onReceive action=$action widgetId=$widgetId")

        when (action) {
            "TOGGLE_CHECKLIST" -> {
                val itemId = intent.getStringExtra("item_id") ?: return
                toggleChecklistItem(context, itemId)
                refreshChecklistWidget(context)
            }
            "SESSION_PAUSE" -> {
                sessionAction(context, "pause")
            }
            "SESSION_RESUME" -> {
                sessionAction(context, "resume")
            }
            "SESSION_STOP" -> {
                sessionAction(context, "end")
            }
        }
    }

    private fun toggleChecklistItem(context: Context, itemId: String) {
        val file = VaultConfig.catFile("checklist")
        if (!file.exists()) {
            Log.e("V07Widget", "checklist.jsonl not found at ${file.absolutePath}")
            return
        }
        try {
            val lines = file.readLines().toMutableList()
            var found = false
            for (i in lines.indices) {
                if (lines[i].isBlank()) continue
                try {
                    val map = jsonToMap(lines[i])
                    if (map["id"] == itemId) {
                        val oldDone = map["done"]
                        map["done"] = map["done"] != true
                        lines[i] = toJson(map)
                        found = true
                        Log.i("V07Widget", "Toggled item $itemId: done $oldDone -> ${map["done"]}")
                        break
                    }
                } catch (_: Exception) {}
            }
            if (!found) {
                Log.w("V07Widget", "Item $itemId not found in ${file.absolutePath}")
                return
            }
            file.writeText(lines.joinToString("\n") + "\n")
        } catch (e: Exception) {
            Log.e("V07Widget", "Toggle checklist failed: ${e.message}")
        }
    }

    private fun sessionAction(context: Context, action: String) {
        try {
            when (action) {
                "pause" -> {
                    val active = getActiveSession() ?: return
                    val startDt = parseTs(active.ts ?: "") ?: return
                    val duration = java.time.Duration.between(startDt, OffsetDateTime.now()).toMinutes().toInt()
                    appendEvent("sessions", mapOf("type" to "end", "activity" to (active.activity ?: ""), "start_ts" to (active.ts ?: ""), "duration" to duration, "paused" to true))
                }
                "resume" -> {
                    val paused = getPausedSession() ?: return
                    val sessions = readEventsAll("sessions").sortedBy { ev -> parseTs(ev.ts ?: "")?.toInstant()?.toEpochMilli() ?: 0L }
                    val pausedEnd = sessions.lastOrNull { it.type == "end" && it.paused == true && it.start_ts == paused.ts }
                    if (pausedEnd != null) deleteEvent("sessions", pausedEnd.id)
                }
                "end" -> {
                    val active = getActiveSession() ?: return
                    val startDt = parseTs(active.ts ?: "") ?: return
                    val duration = java.time.Duration.between(startDt, OffsetDateTime.now()).toMinutes().toInt()
                    appendEvent("sessions", mapOf("type" to "end", "activity" to (active.activity ?: ""), "start_ts" to (active.ts ?: ""), "duration" to duration, "paused" to false))
                }
            }
            Log.i("V07Widget", "Session action $action completed")
            broadcastSessionChanged()
        } catch (e: Exception) {
            Log.e("V07Widget", "Session action $action failed: ${e.message}")
        }
    }

    private fun refreshChecklistWidget(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, V07ChecklistWidget::class.java))
        for (id in ids) {
            mgr.notifyAppWidgetViewDataChanged(id, R.id.widgetChecklistList)
            V07ChecklistWidget.updateWidget(context, mgr, id)
        }
    }
}
