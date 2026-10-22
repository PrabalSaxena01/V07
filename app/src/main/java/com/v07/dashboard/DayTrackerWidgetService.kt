package com.v07.dashboard

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DayTrackerWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DayTrackerFactory(applicationContext)
    }

    class DayTrackerFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
        private var items = mutableListOf<Map<String, Any?>>()

        override fun onCreate() {
            Log.i("V07DTService", "DayTrackerFactory created")
        }

        override fun onDestroy() {
            items.clear()
        }

        override fun getCount() = items.size

        override fun getLoadingView(): RemoteViews? {
            return RemoteViews(context.packageName, R.layout.widget_daytracker_item).apply {
                setTextViewText(R.id.widgetItemTitle, "Loading...")
                setTextViewText(R.id.widgetItemLabel, "")
                setTextViewText(R.id.widgetItemDateTime, "")
            }
        }

        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = position.toLong()
        override fun hasStableIds() = false

        override fun onDataSetChanged() {
            items.clear()
            try {
                val file = VaultConfig.dayTrackerFile
                Log.i("V07DTService", "Reading day_tracker from: ${file.absolutePath}, exists=${file.exists()}, length=${if (file.exists()) file.length() else 0}")
                if (!file.exists()) return

                var totalLines = 0
                var pinnedCount = 0

                file.readLines().forEach { line ->
                    if (line.isBlank()) return@forEach
                    totalLines++
                    try {
                        val map = jsonToMap(line)
                        val pinned = map["pinned"]
                        val isPinned = pinned == true || pinned == "true" || pinned == "1"
                        Log.d("V07DTService", "  line pinned=$pinned, isPinned=$isPinned, title=${map["title"]}, date=${map["date"]}")
                        if (isPinned) {
                            pinnedCount++
                            val dateStr = map["date"] as? String ?: return@forEach
                            val targetDate = try {
                                LocalDate.parse(dateStr)
                            } catch (_: Exception) {
                                Log.w("V07DTService", "  Invalid date: $dateStr")
                                return@forEach
                            }
                            val hourVal = (map["hour"] as? Number)?.toInt() ?: 0
                            val targetDateTime = LocalDateTime.of(targetDate, LocalTime.of(hourVal, 0))
                            val nowDateTime = LocalDateTime.now()
                            val totalSecs = Duration.between(nowDateTime, targetDateTime).seconds
                            val absSecs = Math.abs(totalSecs)
                            val sign = if (totalSecs >= 0) 1 else -1
                            val diffDays = (absSecs / 86400).toInt()
                            val remHours = ((absSecs % 86400) / 3600).toInt()
                            val totalHours = diffDays * 24 + remHours
                            val title = map["title"] as? String ?: dateStr
                            val hourStr = if (hourVal > 0) String.format("%02d:00", hourVal) else "00:00"
                            val dateTimeStr = "$dateStr  $hourStr"
                            val label = when {
                                sign > 0 && diffDays > 0 -> "${diffDays}d ${remHours}h left"
                                sign > 0 && totalHours > 0 -> "${totalHours}h left"
                                sign > 0 -> "Now"
                                sign < 0 && diffDays > 0 -> "${diffDays}d ${remHours}h ago"
                                sign < 0 && totalHours > 0 -> "${totalHours}h ago"
                                else -> "Today"
                            }
                            items.add(mapOf("title" to title, "label" to label, "date" to dateStr, "dateTime" to dateTimeStr))
                        }
                    } catch (e: Exception) {
                        Log.e("V07DTService", "  Failed to parse line: ${e.message}")
                    }
                }
                items.sortBy { it["date"] as? String ?: "" }
                Log.i("V07DTService", "Loaded ${items.size} pinned items from $totalLines total lines ($pinnedCount pinned)")
            } catch (e: Exception) {
                Log.e("V07DTService", "onDataSetChanged failed: ${e.message}", e)
            }

            if (items.isEmpty()) {
                try {
                    val repo = com.v07.v3.core.data.EventRepository.getInstance(context)
                    val dbItems = repo.getAllDayTrackerItems().filter { it.pinned }
                    val nowDateTime = LocalDateTime.now()
                    for (item in dbItems) {
                        val dateStr = item.targetDate
                        val targetDate = try { LocalDate.parse(dateStr) } catch (_: Exception) { continue }
                        val hourVal = item.hour
                        val targetDateTime = LocalDateTime.of(targetDate, LocalTime.of(hourVal.coerceIn(0, 23), 0))
                        val totalSecs = Duration.between(nowDateTime, targetDateTime).seconds
                        val absSecs = Math.abs(totalSecs)
                        val sign = if (totalSecs >= 0) 1 else -1
                        val diffDays = (absSecs / 86400).toInt()
                        val remHours = ((absSecs % 86400) / 3600).toInt()
                        val totalHours = diffDays * 24 + remHours
                        val hourStr = if (hourVal > 0) String.format("%02d:00", hourVal) else "00:00"
                        val dateTimeStr = "$dateStr  $hourStr"
                        val label = when {
                            sign > 0 && diffDays > 0 -> "${diffDays}d ${remHours}h left"
                            sign > 0 && totalHours > 0 -> "${totalHours}h left"
                            sign > 0 -> "Now"
                            sign < 0 && diffDays > 0 -> "${diffDays}d ${remHours}h ago"
                            sign < 0 && totalHours > 0 -> "${totalHours}h ago"
                            else -> "Today"
                        }
                        items.add(mapOf("title" to item.title, "label" to label, "date" to dateStr, "dateTime" to dateTimeStr))
                    }
                    items.sortBy { it["date"] as? String ?: "" }
                } catch (_: Exception) {}
            }
        }

        override fun getViewAt(position: Int): RemoteViews {
            val item = items[position]
            return RemoteViews(context.packageName, R.layout.widget_daytracker_item).apply {
                setTextViewText(R.id.widgetItemTitle, item["title"] as? String ?: "")
                setTextViewText(R.id.widgetItemLabel, item["label"] as? String ?: "")
                setTextViewText(R.id.widgetItemDateTime, item["dateTime"] as? String ?: "")
            }
        }
    }
}
