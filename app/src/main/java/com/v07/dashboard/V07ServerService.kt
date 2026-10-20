package com.v07.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import android.util.Log
import android.widget.RemoteViews
import java.io.File
import java.net.ServerSocket
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime

class V07ServerService : Service() {
    private var server: ServerSocket? = null
    private var usageCollector: UsageStatsCollector? = null
    private var notifyJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        VaultConfig.init(this)
        NotificationPreferences.init(this)
        SessionManager.init(this)
        GeminiManager.init(this)
        createNotificationChannel()

        server = startServer(8000, this)
        try {
            startForeground(FOREGROUND_NOTIF_ID, buildServerNotification())
        } catch (e: Exception) {
            android.util.Log.w("V07ServerService", "Legacy startForeground skipped/limited: ${e.message}")
        }

        usageCollector = UsageStatsCollector(this)
        usageCollector?.startPolling()

        startNotificationChecker()
        try { seedChecklistToday() } catch (_: Exception) {}
    }

    private var sessionNotifActive = false

    private fun buildServerNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("V07 Dashboard")
            .setContentText("Server running on port ${V07ServerConfig.port}")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.i("V07Server", "onStartCommand action=$action")
        if (action != null) {
            when (action) {
                "SESSION_PAUSE" -> SessionManager.pause()
                "SESSION_RESUME" -> SessionManager.resume()
                "SESSION_STOP" -> {
                    SessionManager.stop()
                    dismissSessionNotif()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        server?.close()
        usageCollector?.stop()
        notifyJob?.cancel()
        scope.cancel()
        instance = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val serverChan = NotificationChannel(CHANNEL_ID, "V07 Server", NotificationManager.IMPORTANCE_LOW)
        val sessionChan = NotificationChannel(SESSION_CHANNEL_ID, "V07 Session", NotificationManager.IMPORTANCE_HIGH)
            .apply { setShowBadge(false); enableVibration(false); setSound(null, null) }
        val alertChan = NotificationChannel(ALERT_CHANNEL_ID, "V07 Alerts", NotificationManager.IMPORTANCE_HIGH)
        val reminderChan = NotificationChannel(REMINDER_CHANNEL_ID, "V07 Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        val nm = getSystemService(NotificationManager::class.java)
        nm?.createNotificationChannel(serverChan)
        nm?.createNotificationChannel(sessionChan)
        nm?.createNotificationChannel(alertChan)
        nm?.createNotificationChannel(reminderChan)
    }

    private var lastChecklistSeedDate = ""

    private fun startNotificationChecker() {
        notifyJob = scope.launch {
            launch {
                while (isActive) {
                    try {
                        updateSessionNotification()
                    } catch (_: Exception) {}
                    delay(1000L)
                }
            }
            launch {
                while (isActive) {
                    try {
                        checkAndNotify()
                        checkCountdownSession()
                        checkChecklistSeed()
                    } catch (_: Exception) {}
                    delay(10 * 1000L)
                }
            }
        }
    }

    private fun checkChecklistSeed() {
        val today = LocalDate.now().toString()
        if (today == lastChecklistSeedDate) return
        val count = seedChecklistToday()
        lastChecklistSeedDate = today
        if (count > 0) {
            Log.i("V07Server", "Seeded $count checklist items for $today")
        }
    }

    private fun isAppInForeground(): Boolean {
        return MainActivity.isInForeground
    }

    private fun updateSessionNotification() {
        try {
            val s = SessionManager.getSession()
            if (s == null) {
                if (sessionNotifActive) {
                    dismissSessionNotif()
                }
                return
            }

            val openIntent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val elapsed = SessionManager.getElapsed()
            val elapsedSec = (elapsed / 1000).toInt()

            val timeText = when {
                s.mode == "countdown" && s.targetDuration > 0 -> {
                    val remaining = SessionManager.getRemaining()
                    val remSec = (remaining / 1000).toInt()
                    String.format("%02d:%02d", remSec / 60, remSec % 60)
                }
                else -> {
                    val h = elapsedSec / 3600
                    val m = (elapsedSec % 3600) / 60
                    val sec = elapsedSec % 60
                    String.format("%02d:%02d:%02d", h, m, sec)
                }
            }
            val activityLabel = s.customLabel ?: s.activity

            val rv = RemoteViews(packageName, R.layout.notification_session)
            rv.setTextViewText(R.id.notif_activity, activityLabel)
            rv.setTextViewText(R.id.notif_timer, timeText)
            rv.setTextViewText(R.id.btn_pause, if (s.status == "paused") "▶" else "⏸")

            val pausePending = PendingIntent.getActivity(this, 2001,
                Intent(this, SessionActionActivity::class.java).apply {
                    action = if (s.status == "paused") "SESSION_RESUME" else "SESSION_PAUSE"
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val stopPending = PendingIntent.getActivity(this, 2002,
                Intent(this, SessionActionActivity::class.java).apply { action = "SESSION_STOP" },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            rv.setOnClickPendingIntent(R.id.btn_pause, pausePending)
            rv.setOnClickPendingIntent(R.id.btn_stop, stopPending)
            rv.setOnClickPendingIntent(R.id.notif_activity, openIntent)

            val notifIcon = if (s.status == "paused") android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause
            val notification = NotificationCompat.Builder(this, SESSION_CHANNEL_ID)
                .setSmallIcon(notifIcon)
                .setContentTitle("")
                .setContentText("Session active")
                .setCustomContentView(rv)
                .setCustomBigContentView(rv)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(openIntent)
                .build()

            NotificationManagerCompat.from(this).notify(SESSION_NOTIF_ID, notification)
            sessionNotifActive = true
            SessionManager.checkpoint()
        } catch (e: Exception) {
            Log.e("V07Server", "updateSessionNotification failed: ${e.message}")
        }
    }

    fun dismissSessionNotif() {
        sessionNotifActive = false
        try {
            NotificationManagerCompat.from(this).cancel(SESSION_NOTIF_ID)
            startForeground(FOREGROUND_NOTIF_ID, buildServerNotification())
        } catch (e: Exception) {
            Log.e("V07Server", "dismissSessionNotif failed: ${e.message}")
        }
    }

    private fun checkCountdownSession() {
        try {
            val s = SessionManager.getSession() ?: return
            if (s.status != "running") return
            if (s.mode != "countdown" || s.targetDuration <= 0L) return
            val elapsed = SessionManager.getElapsed()
            if (elapsed >= s.targetDuration) {
                SessionManager.stop()
                dismissSessionNotif()
                sendAlert("Session Complete", "${s.activity} timer done! Great work.", isEssential = true)
            }
        } catch (_: Exception) {}
    }

    private fun checkAndNotify() {
        val status = getNotifyStatus()
        val today = LocalDate.now().toString()


        if (NotificationPreferences.isOverdueAlertEnabled()) {
            val deadlines = status["deadlines"] as? Map<*, *>
            val overdueCount = (deadlines?.get("overdue_count") as? Number)?.toInt() ?: 0
            if (overdueCount > 0) {
                sendAlert("Overdue Tasks", "$overdueCount overdue task(s). Check your tasks!", isEssential = true)
            }
        }

        if (NotificationPreferences.isDueTodayAlertEnabled()) {
            val deadlines = status["deadlines"] as? Map<*, *>
            val dueTodayCount = (deadlines?.get("due_today_count") as? Number)?.toInt() ?: 0
            if (dueTodayCount > 0) {
                val nowHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                if (nowHour >= 18) {
                    sendAlert("Tasks Due Today", "$dueTodayCount task(s) still due today!", isEssential = true)
                }
            }
        }

        if (NotificationPreferences.isTimePriorityAlertEnabled()) {
            val timePriority = status["time_priority"] as? List<*> ?: emptyList<Any?>()
            for (item in timePriority) {
                val task = item as? Map<*, *> ?: continue
                val title = task["title"] as? String ?: continue
                val taskTime = task["time"] as? String ?: continue
                val trigger = task["trigger"] as? String ?: "1min"
                if (trigger == "60min") sendReminder("Task Due Soon", "Task '$title' is at $taskTime (1 hour)", isEssential = true)
                else sendAlert("Task Due Now", "Task '$title' is due at $taskTime!", isEssential = true)
            }
        }

        if (NotificationPreferences.isChecklistReminderEnabled()) {
            val nowHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            if (nowHour == 21) {
                val nowMs = System.currentTimeMillis()
                val lastSent = NotificationPreferences.getLastChecklistReminderSent()
                if (nowMs - lastSent > 60 * 60 * 1000) {
                    try {
                        val f = File(VaultConfig.eventsDir, "checklist.jsonl")
                        if (f.exists()) {
                            val today = LocalDate.now().toString()
                            val items = f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
                                try { jsonToMap(line) } catch (_: Exception) { null }
                            }.filter { it["date"] == today }
                            val total = items.size
                            val done = items.count { it["done"] == true }
                            if (total > 0 && done < total) {
                                sendReminder("Checklist Incomplete", "${total - done} of $total items not done today!")
                                NotificationPreferences.setLastChecklistReminderSent(nowMs)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        if (NotificationPreferences.isGitaDailyEnabled()) {
            val timeStr = NotificationPreferences.getGitaDailyTime()
            if (timeStr.isNotEmpty()) {
                val now = java.time.LocalTime.now()
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val targetHour = parts[0].toIntOrNull() ?: -1
                    val targetMin = parts[1].toIntOrNull() ?: -1
                    if (targetHour >= 0 && targetMin >= 0) {
                        val target = java.time.LocalTime.of(targetHour, targetMin)
                        val diff = java.time.Duration.between(now, target).toMinutes()
                        if (diff in -5..15) {
                            val verse = getSequentialVerse()
                            if (verse != null && verse["already_read"] == false) {
                                val verseId = verse["id"] as? String ?: "?"
                                val unread = verse["unread_count"] as? Int ?: 0
                                sendGitaNotification(verseId, unread)
                            }
                        }
                    }
                }
            }
        }
    }

    private var lastSentAlerts = mutableMapOf<String, Long>()

    private fun dedupeAlert(key: String, cooldownMin: Long = 60): Boolean {
        val now = System.currentTimeMillis()
        val last = lastSentAlerts[key]
        return if (last != null && (now - last) < cooldownMin * 60 * 1000) false
        else { lastSentAlerts[key] = now; true }
    }

    private fun sendAlert(title: String, text: String, isEssential: Boolean = false) {
        if (!isEssential && (isAppInForeground() || System.currentTimeMillis() - lastRequestTime < 60_000)) return
        if (!dedupeAlert("alert:$title", 60)) return
        val nm = NotificationManagerCompat.from(this)
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .build()
        try { nm.notify((title.hashCode() and 0x7fffffff) % 10000, notification) } catch (_: Exception) {}
    }

    private fun sendReminder(title: String, text: String, isEssential: Boolean = false) {
        if (!isEssential && (isAppInForeground() || System.currentTimeMillis() - lastRequestTime < 60_000)) return
        if (!dedupeAlert("rem:$title", 120)) return
        val nm = NotificationManagerCompat.from(this)
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .build()
        try { nm.notify((title.hashCode() and 0x7fffffff) % 10000 + 5000, notification) } catch (_: Exception) {}
    }

    private fun sendGitaNotification(verseId: String, unreadCount: Int) {
        if (!dedupeAlert("gita:daily:notify", 1440)) return
        val nm = NotificationManagerCompat.from(this)
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("action", "open_gita")
            putExtra("verse_id", verseId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 9998, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val text = "Verse $verseId \u00b7 $unreadCount unread remaining"
        val notification = NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("\uD83D\uDCD6 Daily Gita Quote")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Read today's Bhagavad Gita verse $verseId\n$unreadCount unread remaining"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        try { nm.notify(8888, notification) } catch (_: Exception) {}
    }

    private fun sendTestNotification() {
        val nm = NotificationManagerCompat.from(this)
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Test Notification")
            .setContentText("V07 notifications are working!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("V07 notifications are working!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .build()
        try { nm.notify(9999, notification) } catch (_: Exception) {}
    }

    companion object {
        const val CHANNEL_ID = "v07_server_channel"
        const val SESSION_CHANNEL_ID = "v07_session_channel"
        const val ALERT_CHANNEL_ID = "v07_alert_channel"
        const val REMINDER_CHANNEL_ID = "v07_reminder_channel"
        const val SESSION_NOTIF_ID = 2001
        const val FOREGROUND_NOTIF_ID = 1
        private var instance: V07ServerService? = null
        fun dismissSessionNotification() {
            instance?.dismissSessionNotif()
        }
        fun sendTestNotificationFromServer() {
            instance?.sendTestNotification()
        }
        fun refreshUsageStats(force: Boolean = false) {
            val now = System.currentTimeMillis()
            if (!force && now - lastRefreshMs < 5 * 60 * 1000) return
            lastRefreshMs = now
            Thread {
                try {
                    instance?.usageCollector?.collectAndSave()
                } catch (e: Exception) {
                    Log.e("V07Screen", "refreshUsageStats background failed", e)
                }
            }.start()
        }
        fun collectNow() {
            lastRefreshMs = System.currentTimeMillis()
            instance?.usageCollector?.collectAndSave()
        }
        private var lastRefreshMs = 0L
    }
}