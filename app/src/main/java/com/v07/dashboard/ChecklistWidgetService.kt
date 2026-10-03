package com.v07.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import java.time.LocalDate

class ChecklistWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ChecklistFactory(applicationContext)
    }

    class ChecklistFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
        private var items = mutableListOf<Map<String, Any?>>()

        override fun onCreate() {
            Log.i("V07CLService", "ChecklistFactory created")
        }

        override fun onDestroy() {
            items.clear()
        }

        override fun getCount() = items.size

        override fun getLoadingView(): RemoteViews? {
            return RemoteViews(context.packageName, R.layout.widget_checklist_item).apply {
                setTextViewText(R.id.widgetChecklistLabel, "Loading...")
                setTextViewText(R.id.widgetChecklistCheck, "")
            }
        }

        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = position.toLong()
        override fun hasStableIds() = false

        override fun onDataSetChanged() {
            items.clear()
            try {
                val file = VaultConfig.catFile("checklist")
                Log.i("V07CLService", "Reading checklist from: ${file.absolutePath}, exists=${file.exists()}, length=${if (file.exists()) file.length() else 0}")
                if (!file.exists()) return

                val today = LocalDate.now().toString()
                var totalLines = 0
                var todayCount = 0

                file.readLines().forEach { line ->
                    if (line.isBlank()) return@forEach
                    totalLines++
                    try {
                        val map = jsonToMap(line)
                        val date = map["date"] as? String
                        Log.d("V07CLService", "  line date=$date, today=$today, label=${map["label"]}, done=${map["done"]}")
                        if (date == today) {
                            todayCount++
                            items.add(map)
                        }
                    } catch (e: Exception) {
                        Log.e("V07CLService", "  Failed to parse line: ${e.message}")
                    }
                }
                Log.i("V07CLService", "Loaded ${items.size} items for today ($today) from $totalLines total lines ($todayCount today)")
            } catch (e: Exception) {
                Log.e("V07CLService", "onDataSetChanged failed: ${e.message}", e)
            }
        }

        override fun getViewAt(position: Int): RemoteViews {
            val item = items[position]
            val done = item["done"] == true || item["done"] == "true"
            val label = item["label"] as? String ?: ""
            val id = item["id"] as? String ?: ""

            return RemoteViews(context.packageName, R.layout.widget_checklist_item).apply {
                setTextViewText(R.id.widgetChecklistLabel, label)
                setTextViewText(R.id.widgetChecklistCheck, if (done) "✓" else "○")
                setTextColor(R.id.widgetChecklistCheck, ContextCompat.getColor(context, if (done) R.color.accent else R.color.text_secondary))
                setInt(R.id.widgetChecklistLabel, "setPaintFlags", if (done) Paint.STRIKE_THRU_TEXT_FLAG else 0)

                val fillInIntent = Intent().apply {
                    putExtra("item_id", id)
                }
                setOnClickFillInIntent(R.id.widgetChecklistItemRoot, fillInIntent)
            }
        }
    }
}
