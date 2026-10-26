package com.v07.v3.app.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.v07.dashboard.R
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Task

class V3TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return V3TasksRemoteViewsFactory(applicationContext)
    }
}

class V3TasksRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var tasks: List<V3Task> = emptyList()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            val repository = EventRepository.getInstance(context)
            tasks = repository.getTasks("pending").take(20)
        } catch (_: Exception) {
            tasks = emptyList()
        }
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= tasks.size) return null
        val task = tasks[position]

        val views = RemoteViews(context.packageName, R.layout.v3_widget_tasks_item)
        val isDone = task.status.equals("completed", ignoreCase = true)
        views.setTextViewText(R.id.btn_v3_task_toggle, if (isDone) "✓" else "○")
        views.setTextColor(R.id.btn_v3_task_toggle, if (isDone) Color.parseColor("#10B981") else Color.parseColor("#94A3B8"))
        views.setTextViewText(R.id.tv_v3_task_title, task.title)

        if (!task.deadline.isNullOrBlank()) {
            views.setTextViewText(R.id.tv_v3_task_deadline, "Due ${task.deadline}")
            views.setViewVisibility(R.id.tv_v3_task_deadline, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.tv_v3_task_deadline, android.view.View.GONE)
        }

        val priColor = when (task.priority.uppercase()) {
            "HIGH" -> Color.parseColor("#EF4444")
            "LOW" -> Color.parseColor("#94A3B8")
            else -> Color.parseColor("#38BDF8")
        }
        views.setTextViewText(R.id.tv_v3_task_priority, task.priority.uppercase())
        views.setTextColor(R.id.tv_v3_task_priority, priColor)

        // Set fill-in intent for item tap / toggle
        val fillInIntent = Intent().apply {
            putExtra(V3WidgetActionReceiver.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.btn_v3_task_toggle, fillInIntent)
        views.setOnClickFillInIntent(R.id.v3_task_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
