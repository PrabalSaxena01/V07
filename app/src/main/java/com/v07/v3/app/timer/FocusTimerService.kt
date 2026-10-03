package com.v07.v3.app.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.v07.v3.core.data.EventRepository
import kotlinx.coroutines.*

/**
 * FocusTimerService
 *
 * Native Android Foreground Service that maintains active timer sessions reliably in background.
 * Presents a persistent, customized notification with real-time progress bar,
 * live countdown/stopwatch formatting, motivational subtitles, and instant interactive action controls
 * (Pause, Resume, Reset, Finish, +5m) routed via FocusTimerActionReceiver.
 */
class FocusTimerService : Service() {

    companion object {
        private const val TAG = "V3FocusTimerService"
        const val CHANNEL_ID = "v3_focus_timer_channel"
        const val NOTIFICATION_ID = 1073
        const val COMPLETION_NOTIFICATION_ID = 1074

        const val ACTION_START = "com.v07.v3.timer.START"
        const val ACTION_PAUSE = "com.v07.v3.timer.PAUSE"
        const val ACTION_RESUME = "com.v07.v3.timer.RESUME"
        const val ACTION_RESET = "com.v07.v3.timer.RESET"
        const val ACTION_DISCARD = "com.v07.v3.timer.DISCARD"
        const val ACTION_FINISH = "com.v07.v3.timer.FINISH"
        const val ACTION_ADD_5M = "com.v07.v3.timer.ADD_5M"
        const val ACTION_STOP = "com.v07.v3.timer.STOP"
        const val ACTION_UPDATE = "com.v07.v3.timer.UPDATE"

        @Volatile
        var activeService: FocusTimerService? = null
            private set

        fun start(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updateNotification(context: Context) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val repository = EventRepository.getInstance(context)
            val timerManager = TimerManager.getInstance(context, repository)
            val state = timerManager.timerState.value

            if (state.status != TimerStatus.IDLE) {
                val service = activeService
                if (service != null && nm != null) {
                    nm.notify(NOTIFICATION_ID, service.buildNotification(state))
                } else {
                    start(context)
                }
            } else {
                stop(context)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null
    private lateinit var timerManager: TimerManager

    override fun onCreate() {
        super.onCreate()
        activeService = this
        timerManager = TimerManager.getInstance(this, EventRepository.getInstance(this))
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        Log.d(TAG, "onStartCommand action: $action")

        when (action) {
            ACTION_PAUSE -> timerManager.pauseSession()
            ACTION_RESUME -> timerManager.resumeSession()
            ACTION_RESET, ACTION_DISCARD -> {
                timerManager.discardSession()
                stopForegroundSafely()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_FINISH -> {
                timerManager.finishSession("Completed via timer service")
                stopForegroundSafely()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_ADD_5M -> timerManager.addMinutes(5)
            ACTION_STOP -> {
                stopForegroundSafely()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val state = timerManager.timerState.value
        if (state.status == TimerStatus.IDLE) {
            stopForegroundSafely()
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification(state))
        startTicker()

        return START_STICKY
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                val state = timerManager.timerState.value
                if (state.status == TimerStatus.IDLE) {
                    stopForegroundSafely()
                    stopSelf()
                    break
                }

                // Check countdown completion
                if (state.mode == TimerMode.COUNTDOWN && state.getRemainingMs() <= 0L && state.status == TimerStatus.RUNNING) {
                    Log.i(TAG, "Countdown reached 00:00! Completing session.")
                    timerManager.finishSession("Completed countdown interval")
                    notifySessionCompletion(state)
                    stopForegroundSafely()
                    stopSelf()
                    break
                }

                if (state.status == TimerStatus.RUNNING) {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, buildNotification(state))
                }
                delay(1000L)
            }
        }
    }

    fun buildNotification(state: ActiveSessionState): Notification {
        val config = FocusNotificationConfig.getInstance(this)
        val formattedTime = state.getFormattedTime()

        // 1. Dynamic or Custom Title
        val title = when (config.titleMode) {
            "sanctuary" -> "Focus Sanctuary"
            "custom" -> config.customTitle.ifBlank { "Focus Sanctuary" }
            else -> if (state.topic.isNotBlank()) "Focus: ${state.topic}" else "Focus Session"
        }

        // 2. Subtitle & Motivational Theme
        val statusText = if (state.status == TimerStatus.PAUSED) "Paused" else "Running"
        val subtitle = when (config.subtitleMode) {
            "gita" -> "$formattedTime • Karmanye Vadhikaraste"
            "mindful" -> "$formattedTime • Stay centered & present"
            "custom" -> if (config.customSubtitle.isNotBlank()) "$formattedTime • ${config.customSubtitle}" else "$formattedTime • $statusText"
            else -> "$formattedTime • ${state.category} ($statusText)"
        }

        // Tap notification to open main app
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, com.v07.v3.app.V3Activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("v3_open_tab", "timer")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(com.v07.dashboard.R.drawable.ic_notification)
            .setColor(com.v07.dashboard.PluginReminderReceiver.SANCTUARY_ACCENT_COLOR)
            .setContentIntent(contentIntent)
            .setOngoing(state.status == TimerStatus.RUNNING || state.status == TimerStatus.PAUSED)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // 3. Native Progress Bar for Countdown Mode
        if (config.showProgressBar && state.mode == TimerMode.COUNTDOWN && state.targetDurationMs > 0) {
            val elapsed = state.getElapsedMs()
            val target = state.targetDurationMs
            val progressPercent = ((elapsed.toDouble() / target.toDouble()) * 100).toInt().coerceIn(0, 100)
            builder.setProgress(100, progressPercent, false)
        }

        // 4. Action Buttons (Routed to FocusTimerActionReceiver for zero-latency execution)
        if (config.showPauseAction) {
            if (state.status == TimerStatus.RUNNING) {
                val pauseIntent = PendingIntent.getBroadcast(
                    this, 101,
                    Intent(this, FocusTimerActionReceiver::class.java).apply { action = ACTION_PAUSE },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_media_pause, "Pause", pauseIntent)
            } else if (state.status == TimerStatus.PAUSED) {
                val resumeIntent = PendingIntent.getBroadcast(
                    this, 102,
                    Intent(this, FocusTimerActionReceiver::class.java).apply { action = ACTION_RESUME },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_media_play, "Resume", resumeIntent)
            }
        }

        if (config.showResetAction) {
            val resetIntent = PendingIntent.getBroadcast(
                this, 103,
                Intent(this, FocusTimerActionReceiver::class.java).apply { action = ACTION_RESET },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_revert, "Reset", resetIntent)
        }

        if (config.showFinishAction) {
            val finishIntent = PendingIntent.getBroadcast(
                this, 104,
                Intent(this, FocusTimerActionReceiver::class.java).apply { action = ACTION_FINISH },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.checkbox_on_background, "Finish", finishIntent)
        }

        if (config.showAdd5mAction && state.mode == TimerMode.COUNTDOWN) {
            val add5mIntent = PendingIntent.getBroadcast(
                this, 105,
                Intent(this, FocusTimerActionReceiver::class.java).apply { action = ACTION_ADD_5M },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_add, "+5m", add5mIntent)
        }

        return builder.build()
    }

    private fun notifySessionCompletion(state: ActiveSessionState) {
        val config = FocusNotificationConfig.getInstance(this)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openIntent = PendingIntent.getActivity(
            this,
            10,
            Intent(this, com.v07.v3.app.V3Activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("v3_open_tab", "timer")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (config.chimeOnComplete) {
            android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        } else null

        val completeBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Interval Complete! 🎯")
            .setContentText("Well done! Completed ${state.topic.ifBlank { "Focus session" }}. Time to recharge.")
            .setSmallIcon(com.v07.dashboard.R.drawable.ic_notification)
            .setColor(com.v07.dashboard.PluginReminderReceiver.SANCTUARY_ACCENT_COLOR)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (config.vibrateOnComplete) {
            completeBuilder.setVibrate(longArrayOf(0, 300, 150, 300))
        }
        if (soundUri != null) {
            completeBuilder.setSound(soundUri)
        }

        nm.notify(COMPLETION_NOTIFICATION_ID, completeBuilder.build())
    }

    private fun stopForegroundSafely() {
        tickerJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows live status and controls for active focus timer sessions"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        if (activeService == this) {
            activeService = null
        }
        tickerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
