package com.v07.v3.app.widget

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.GitaProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class V3WidgetActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "V3WidgetActionReceiver"

        const val ACTION_START_FOCUS_25 = "com.v07.v3.widget.ACTION_START_FOCUS_25"
        const val ACTION_START_STOPWATCH = "com.v07.v3.widget.ACTION_START_STOPWATCH"
        const val ACTION_PAUSE_TIMER = "com.v07.v3.widget.ACTION_PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.v07.v3.widget.ACTION_RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.v07.v3.widget.ACTION_STOP_TIMER"

        const val ACTION_TOGGLE_TASK = "com.v07.v3.widget.ACTION_TOGGLE_TASK"
        const val EXTRA_TASK_ID = "extra_task_id"

        const val ACTION_NEXT_GITA_VERSE = "com.v07.v3.widget.ACTION_NEXT_GITA_VERSE"
        const val ACTION_MARK_GITA_READ = "com.v07.v3.widget.ACTION_MARK_GITA_READ"
        const val EXTRA_VERSE_CHAPTER = "extra_verse_chapter"
        const val EXTRA_VERSE_NUMBER = "extra_verse_number"

        fun createActionIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, V3WidgetActionReceiver::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "Received widget action: $action")

        val pendingResult = goAsync()
        val repository = EventRepository.getInstance(context)
        val timerManager = TimerManager.getInstance(context, repository)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_START_FOCUS_25 -> {
                        timerManager.startSession(
                            topic = "Deep Focus",
                            category = "Work",
                            mode = TimerMode.COUNTDOWN,
                            targetMinutes = 25
                        )
                        V3FocusWidget.updateAll(context)
                    }
                    ACTION_START_STOPWATCH -> {
                        timerManager.startSession(
                            topic = "Focus Session",
                            category = "Work",
                            mode = TimerMode.STOPWATCH,
                            targetMinutes = 0
                        )
                        V3FocusWidget.updateAll(context)
                    }
                    ACTION_PAUSE_TIMER -> {
                        timerManager.pauseSession()
                        V3FocusWidget.updateAll(context)
                    }
                    ACTION_RESUME_TIMER -> {
                        timerManager.resumeSession()
                        V3FocusWidget.updateAll(context)
                    }
                    ACTION_STOP_TIMER -> {
                        timerManager.finishSession()
                        V3FocusWidget.updateAll(context)
                    }
                    ACTION_TOGGLE_TASK -> {
                        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                        if (!taskId.isNullOrBlank()) {
                            repository.toggleTaskStatus(taskId)
                            V3TasksWidget.updateAll(context)
                        }
                    }
                    ACTION_NEXT_GITA_VERSE -> {
                        val gitaProvider = GitaProvider.getInstance(context)
                        gitaProvider.getRandomVerse()
                        V3GitaWidget.updateAll(context)
                    }
                    ACTION_MARK_GITA_READ -> {
                        val ch = intent.getIntExtra(EXTRA_VERSE_CHAPTER, 2)
                        val vs = intent.getIntExtra(EXTRA_VERSE_NUMBER, 47)
                        val gitaProvider = GitaProvider.getInstance(context)
                        gitaProvider.markVerseRead(ch, vs, true)
                        V3GitaWidget.updateAll(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling action $action: ${e.message}", e)
            } finally {
                try {
                    pendingResult.finish()
                } catch (_: Exception) {}
            }
        }
    }
}
