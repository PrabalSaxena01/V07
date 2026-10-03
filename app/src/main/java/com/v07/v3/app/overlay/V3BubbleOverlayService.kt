package com.v07.v3.app.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.v07.dashboard.ForegroundAppTracker
import com.v07.dashboard.R
import com.v07.v3.app.V3Activity
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Event
import com.v07.v3.core.data.V3Thought
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * V3BubbleOverlayService
 *
 * Modern V3 Floating Bubble Overlay presenting real-time active app usage telemetry,
 * fluid edge snapping, and a quick-action editorial menu for fast sanctuary reflections
 * and focus sessions.
 */
class V3BubbleOverlayService : Service() {

    companion object {
        private const val TAG = "V3BubbleOverlayService"
        const val CHANNEL_ID = "v3_bubble_overlay_channel"
        const val NOTIFICATION_ID = 1074

        const val PREFS_NAME = "v3_overlay_prefs"
        const val PREF_KEY_ENABLED = "v3_overlay_enabled"

        const val ACTION_START = "com.v07.v3.overlay.START"
        const val ACTION_STOP = "com.v07.v3.overlay.STOP"
        const val ACTION_TOGGLE = "com.v07.v3.overlay.TOGGLE"

        @Volatile
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, V3BubbleOverlayService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, V3BubbleOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun toggle(context: Context) {
            if (isRunning) {
                stop(context)
            } else {
                start(context)
            }
        }

        fun isOverlayEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_KEY_ENABLED, false)
        }

        fun setOverlayEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_KEY_ENABLED, enabled).apply()
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var repository: EventRepository
    private lateinit var timerManager: TimerManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollJob: Job? = null

    // Overlay views
    private var bubbleView: View? = null
    private var menuView: View? = null
    private var quickThoughtView: View? = null
    private var checklistView: View? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams

    // Tracked state
    private var activePackage: String = ""
    private var activeAppLabel: String = "V07 Sanctuary"
    private var activeAppUsageMs: Long = 0L
    private var currentSessionStartMs: Long = 0L
    @Volatile
    private var currentTrackedDate: String = ""

    private val dayChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            Log.i(TAG, "Received system broadcast: $action")
            if (action == Intent.ACTION_DATE_CHANGED ||
                action == Intent.ACTION_TIME_CHANGED ||
                action == Intent.ACTION_TIMEZONE_CHANGED
            ) {
                val now = System.currentTimeMillis()
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                handleDayChange(now, todayStr)
            }
        }
    }

    private fun registerDayChangeReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(dayChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(dayChangeReceiver, filter)
            }
            Log.i(TAG, "Day change receiver registered successfully")
        } catch (e: Exception) {
            try {
                registerReceiver(dayChangeReceiver, filter)
            } catch (e2: Exception) {
                Log.w(TAG, "Failed to register day change receiver: ${e2.message}")
            }
        }
    }

    private fun unregisterDayChangeReceiver() {
        try {
            unregisterReceiver(dayChangeReceiver)
        } catch (_: Exception) {}
    }

    @Synchronized
    private fun handleDayChange(nowMs: Long, newDateStr: String) {
        if (currentTrackedDate == newDateStr && activeAppUsageMs == 0L) return
        Log.i(TAG, "Day change reset: transitioned from '$currentTrackedDate' to '$newDateStr'")
        currentTrackedDate = newDateStr
        currentSessionStartMs = nowMs
        activeAppUsageMs = 0L

        val pkg = if (activePackage.isNotBlank()) activePackage else packageName
        val icon = getAppIcon(pkg)
        val label = if (activePackage.isNotBlank()) activeAppLabel else "V07 Sanctuary"
        val formatted = "0m"

        serviceScope.launch(Dispatchers.Main) {
            updateBubbleUi(icon, formatted)
            updateMenuUi(icon, label, formatted)
        }
    }

    private val appLabelCache = ConcurrentHashMap<String, String>()
    private val appIconCache = ConcurrentHashMap<String, Drawable>()

    private val ignoredPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.launcher",
        "com.android.launcher3",
        "com.coloros.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.apps.nexuslauncher.NexusLauncherActivity",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod",
        "com.android.shell",
        "com.android.documentsui",
        "com.android.providers.downloads"
    )

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "V3BubbleOverlayService initializing")

        repository = EventRepository.getInstance(this)
        timerManager = TimerManager.getInstance(this, repository)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        currentTrackedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        registerDayChangeReceiver()

        createNotificationChannel()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    buildForegroundNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, buildForegroundNotification())
            }
        } catch (e: Exception) {
            Log.e(TAG, "startForeground error: ${e.message}", e)
        }
        isRunning = true
        setOverlayEnabled(this, true)

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW permission missing. Stopping service.")
            stopSelf()
            return
        }

        initializeBubble()
        startActiveAppPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        Log.d(TAG, "onStartCommand action: $action")

        when (action) {
            ACTION_STOP -> {
                setOverlayEnabled(this, false)
                stopForegroundSafely()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                setOverlayEnabled(this, true)
                if (bubbleView == null && (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true)) {
                    initializeBubble()
                    startActiveAppPolling()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG, "V3BubbleOverlayService destroying")
        isRunning = false
        unregisterDayChangeReceiver()
        pollJob?.cancel()
        serviceScope.cancel()
        removeAllViews()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "V07 Bubble Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating active app screen time bubble"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, V3Activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sanctuary Bubble Active")
            .setContentText("Monitoring foreground application usage")
            .setSmallIcon(R.drawable.v07_logo)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun stopForegroundSafely() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    // =========================================================================
    // Bubble Initialization & Drag Behavior
    // =========================================================================

    private fun getOverlayWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    @SuppressLint("InflateParams")
    private fun initializeBubble() {
        if (bubbleView != null) return

        val inflater = LayoutInflater.from(this)
        bubbleView = inflater.inflate(R.layout.v3_overlay_bubble, null)

        val dm = resources.displayMetrics
        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (dm.widthPixels - dpToPx(100)).coerceAtLeast(0)
            y = dpToPx(140)
        }

        setupBubbleTouch(bubbleView!!, bubbleParams)

        try {
            windowManager.addView(bubbleView, bubbleParams)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding bubble view to window manager: ${e.message}", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleTouch(view: View, params: WindowManager.LayoutParams) {
        var startX = 0f
        var startY = 0f
        var initialParamsX = 0
        var initialParamsY = 0
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    initialParamsX = params.x
                    initialParamsY = params.y
                    isDragging = false
                    view.animate().scaleX(1.06f).scaleY(1.06f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY
                    if (abs(dx) > 8 || abs(dy) > 8) {
                        isDragging = true
                    }
                    params.x = (initialParamsX + dx.toInt())
                    params.y = (initialParamsY + dy.toInt())
                    try {
                        windowManager.updateViewLayout(view, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                    if (!isDragging && event.action == MotionEvent.ACTION_UP) {
                        toggleMenu()
                    } else {
                        snapBubbleToEdge(view, params)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapBubbleToEdge(view: View, params: WindowManager.LayoutParams) {
        val dm = resources.displayMetrics
        val screenW = dm.widthPixels
        val viewW = if (view.measuredWidth > 0) view.measuredWidth else dpToPx(85)
        val centerX = params.x + viewW / 2
        val margin = dpToPx(12)
        val targetX = if (centerX < screenW / 2) margin else screenW - viewW - margin

        val startX = params.x
        val animator = ValueAnimator.ofInt(startX, targetX).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                params.x = va.animatedValue as Int
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (_: Exception) {}
            }
        }
        animator.start()
    }

    // =========================================================================
    // Foreground Application Telemetry
    // =========================================================================

    private fun startActiveAppPolling() {
        pollJob?.cancel()
        pollJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    pollCurrentForegroundApp()
                } catch (e: Exception) {
                    Log.w(TAG, "Error polling active app: ${e.message}")
                }
                delay(2500L) // Poll every 2.5s for snappy tracking
            }
        }
    }

    private suspend fun pollCurrentForegroundApp() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isScreenOn = powerManager?.isInteractive ?: true
        val now = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))

        // 1. Check for day change
        if (currentTrackedDate.isBlank()) {
            currentTrackedDate = todayStr
        } else if (todayStr != currentTrackedDate) {
            handleDayChange(now, todayStr)
        }

        // If screen is off, do not poll or accumulate
        if (!isScreenOn) {
            return
        }

        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return

        // 2. Compute today's midnight timestamp
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val midnight = cal.timeInMillis

        // 3. Determine active foreground package
        var detectedPkg: String? = null

        if (ForegroundAppTracker.isConnected && ForegroundAppTracker.currentForegroundPackage.isNotBlank()) {
            val trackerPkg = ForegroundAppTracker.currentForegroundPackage
            if (!ignoredPackages.contains(trackerPkg)) {
                detectedPkg = trackerPkg
            }
        } else {
            // Check recent events from the last 30s
            val recentEvents = usm.queryEvents(now - 30_000L, now)
            val event = UsageEvents.Event()
            var lastResumedPkg: String? = null
            var wasLastEventPauseOrStop = false

            while (recentEvents.hasNextEvent()) {
                recentEvents.getNextEvent(event)
                val pkg = event.packageName ?: continue
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        lastResumedPkg = pkg
                        wasLastEventPauseOrStop = false
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        if (pkg == lastResumedPkg || pkg == activePackage) {
                            wasLastEventPauseOrStop = true
                        }
                    }
                }
            }

            if (lastResumedPkg != null) {
                if (!ignoredPackages.contains(lastResumedPkg) && !wasLastEventPauseOrStop) {
                    detectedPkg = lastResumedPkg
                }
            } else {
                // No resume event in last 30s: if activePackage was not paused/stopped, it might still be in foreground
                if (activePackage.isNotBlank() && !ignoredPackages.contains(activePackage) && !wasLastEventPauseOrStop) {
                    detectedPkg = activePackage
                }
            }
        }

        // Target package to display on the bubble:
        val targetPkg = detectedPkg
            ?: (if (activePackage.isNotBlank() && !ignoredPackages.contains(activePackage)) activePackage else packageName)

        val isNewPkg = (targetPkg != activePackage)
        if (isNewPkg) {
            activePackage = targetPkg
            activeAppLabel = getAppLabel(targetPkg)
            currentSessionStartMs = now
        }

        // 4. Calculate today's usage strictly from midnight (00:00:00) to now
        activeAppUsageMs = calculatePackageUsageSinceMidnight(
            usm = usm,
            targetPkg = targetPkg,
            midnightMs = midnight,
            nowMs = now
        )

        val appIcon = getAppIcon(targetPkg)
        val formattedUsage = formatUsageDuration(activeAppUsageMs)

        // 5. Post UI updates to Main thread
        withContext(Dispatchers.Main) {
            updateBubbleUi(appIcon, formattedUsage)
            updateMenuUi(appIcon, activeAppLabel, formattedUsage)
        }
    }

    /**
     * Accurately calculates the active foreground duration (in milliseconds)
     * of [targetPkg] strictly within today's calendar window [midnightMs .. nowMs].
     *
     * Guarantees that usage from prior calendar days is NEVER included.
     */
    private fun calculatePackageUsageSinceMidnight(
        usm: UsageStatsManager,
        targetPkg: String,
        midnightMs: Long,
        nowMs: Long
    ): Long {
        if (targetPkg.isBlank() || ignoredPackages.contains(targetPkg) || nowMs <= midnightMs) {
            return 0L
        }

        val maxPossibleToday = (nowMs - midnightMs).coerceAtLeast(0L)
        var totalMs = 0L

        try {
            val events = usm.queryEvents(midnightMs, nowMs)
            val event = UsageEvents.Event()
            var sessionStart = -1L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                val time = event.timeStamp
                val type = event.eventType

                // Screen turn-off (SCREEN_NON_INTERACTIVE = 16, KEYGUARD_SHOWN = 18)
                if (type == 16 || type == 18) {
                    if (sessionStart != -1L) {
                        totalMs += (time - sessionStart).coerceAtLeast(0L)
                        sessionStart = -1L
                    }
                    continue
                }

                if (pkg == targetPkg) {
                    when (type) {
                        UsageEvents.Event.ACTIVITY_RESUMED -> {
                            if (sessionStart == -1L) {
                                sessionStart = time
                            }
                        }
                        UsageEvents.Event.ACTIVITY_PAUSED,
                        UsageEvents.Event.ACTIVITY_STOPPED -> {
                            if (sessionStart != -1L) {
                                totalMs += (time - sessionStart).coerceAtLeast(0L)
                                sessionStart = -1L
                            }
                        }
                    }
                } else {
                    // Another non-ignored app resumed: targetPkg ceased being in foreground
                    if (type == UsageEvents.Event.ACTIVITY_RESUMED && !ignoredPackages.contains(pkg)) {
                        if (sessionStart != -1L) {
                            totalMs += (time - sessionStart).coerceAtLeast(0L)
                            sessionStart = -1L
                        }
                    }
                }
            }

            // If the app is currently in foreground (resumed and not paused)
            if (sessionStart != -1L) {
                totalMs += (nowMs - sessionStart).coerceAtLeast(0L)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error calculating package usage since midnight: ${e.message}")
        }

        return totalMs.coerceIn(0L, maxPossibleToday)
    }

    private fun updateBubbleUi(icon: Drawable?, formattedUsage: String) {
        val root = bubbleView ?: return
        val iconView = root.findViewById<ImageView>(R.id.v3_bubble_icon)
        val textView = root.findViewById<TextView>(R.id.v3_bubble_text)

        if (icon != null) {
            iconView.setImageDrawable(icon)
        }
        textView.text = formattedUsage
    }

    private fun updateMenuUi(icon: Drawable?, appName: String, formattedUsage: String) {
        val menu = menuView ?: return
        val iconView = menu.findViewById<ImageView>(R.id.v3_menu_app_icon)
        val nameView = menu.findViewById<TextView>(R.id.v3_menu_app_name)
        val usageView = menu.findViewById<TextView>(R.id.v3_menu_app_usage)

        if (icon != null) {
            iconView.setImageDrawable(icon)
        }
        nameView.text = appName
        usageView.text = "$formattedUsage active today"
    }

    private fun formatUsageDuration(ms: Long): String {
        val totalMins = (ms / 1000L) / 60
        val hrs = totalMins / 60
        val remMins = totalMins % 60
        return if (hrs > 0) {
            "${hrs}h ${remMins}m"
        } else {
            "${remMins}m"
        }
    }

    private fun getAppLabel(pkg: String): String {
        if (pkg == packageName) return "V07 Sanctuary"
        return appLabelCache.getOrPut(pkg) {
            try {
                val pm = packageManager
                val ai = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(ai).toString()
            } catch (_: Exception) {
                pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    private fun getAppIcon(pkg: String): Drawable? {
        return appIconCache.getOrPut(pkg) {
            try {
                packageManager.getApplicationIcon(pkg)
            } catch (_: Exception) {
                ContextCompat.getDrawable(this, R.drawable.v07_logo)
                    ?: ColorDrawable(0xFF14171E.toInt())
            }
        }
    }

    // =========================================================================
    // Editorial Action Menu
    // =========================================================================

    private fun toggleMenu() {
        if (menuView != null) {
            collapseMenu()
        } else {
            showMenu()
        }
    }

    @SuppressLint("InflateParams")
    private fun showMenu() {
        if (menuView != null) return
        collapseQuickThought()
        collapseChecklist()

        val inflater = LayoutInflater.from(this)
        menuView = inflater.inflate(R.layout.v3_overlay_menu, null)

        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        // Initialize menu views with latest active app telemetry
        val iconView = menuView!!.findViewById<ImageView>(R.id.v3_menu_app_icon)
        val nameView = menuView!!.findViewById<TextView>(R.id.v3_menu_app_name)
        val usageView = menuView!!.findViewById<TextView>(R.id.v3_menu_app_usage)

        val currentIcon = getAppIcon(activePackage)
        if (currentIcon != null) iconView.setImageDrawable(currentIcon)
        nameView.text = activeAppLabel
        usageView.text = "${formatUsageDuration(activeAppUsageMs)} active today"

        // Backdrop tap to dismiss
        menuView!!.findViewById<View>(R.id.v3_menu_backdrop).setOnClickListener {
            collapseMenu()
        }

        menuView!!.findViewById<View>(R.id.v3_btn_close).setOnClickListener {
            collapseMenu()
        }

        // 1. Quick Thought
        menuView!!.findViewById<View>(R.id.v3_btn_quick_thought).setOnClickListener {
            collapseMenu()
            showQuickThought()
        }

        // 2. Directives & Tasks Checklist
        menuView!!.findViewById<View>(R.id.v3_btn_checklist)?.setOnClickListener {
            collapseMenu()
            showChecklistOverlay()
        }

        // 3. Focus Interval (25 min session)
        menuView!!.findViewById<View>(R.id.v3_btn_focus_timer).setOnClickListener {
            collapseMenu()
            startQuickFocusSession()
        }

        // 3. Open Sanctuary
        menuView!!.findViewById<View>(R.id.v3_btn_open_app).setOnClickListener {
            collapseMenu()
            openSanctuaryApp()
        }

        // 4. Turn Off Overlay
        menuView!!.findViewById<View>(R.id.v3_btn_turn_off).setOnClickListener {
            collapseMenu()
            turnOffOverlay()
        }

        // Animate card entrance
        val card = menuView!!.findViewById<View>(R.id.v3_menu_card)
        card.alpha = 0f
        card.scaleX = 0.92f
        card.scaleY = 0.92f

        try {
            windowManager.addView(menuView, menuParams)
            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180)
                .setInterpolator(DecelerateInterpolator())
                .start()
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying overlay menu: ${e.message}", e)
        }
    }

    private fun collapseMenu() {
        val view = menuView ?: return
        try {
            windowManager.removeView(view)
        } catch (_: Exception) {}
        menuView = null
    }

    private fun startQuickFocusSession() {
        val topic = if (activeAppLabel.isNotBlank()) "Focus ($activeAppLabel)" else "Focus Interval"
        timerManager.startSession(
            topic = topic,
            category = "Work",
            mode = TimerMode.COUNTDOWN,
            targetMinutes = 25,
            notes = "Initiated from floating overlay"
        )
        Toast.makeText(this, "Started 25m Focus Interval", Toast.LENGTH_SHORT).show()
    }

    private fun openSanctuaryApp() {
        val intent = Intent(this, V3Activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun turnOffOverlay() {
        setOverlayEnabled(this, false)
        Toast.makeText(this, "Bubble Overlay turned off", Toast.LENGTH_SHORT).show()
        stopForegroundSafely()
        stopSelf()
    }

    // =========================================================================
    // Floating Quick Thought Dialog
    // =========================================================================

    @SuppressLint("InflateParams")
    private fun showQuickThought() {
        if (quickThoughtView != null) return
        collapseChecklist()

        val inflater = LayoutInflater.from(this)
        quickThoughtView = inflater.inflate(R.layout.v3_overlay_quick_thought, null)

        val thoughtParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            x = 0
            y = 0
        }

        val input = quickThoughtView!!.findViewById<EditText>(R.id.v3_thought_input)
        val closeBtn = quickThoughtView!!.findViewById<View>(R.id.v3_thought_close_btn)
        val cancelBtn = quickThoughtView!!.findViewById<View>(R.id.v3_thought_cancel_btn)
        val saveBtn = quickThoughtView!!.findViewById<View>(R.id.v3_thought_save_btn)
        val backdrop = quickThoughtView!!.findViewById<View>(R.id.v3_thought_backdrop)

        backdrop.setOnClickListener {
            collapseQuickThought()
        }
        closeBtn.setOnClickListener {
            collapseQuickThought()
        }
        cancelBtn.setOnClickListener {
            collapseQuickThought()
        }

        saveBtn.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotBlank()) {
                saveThoughtAtomic(text)
                Toast.makeText(this, "Reflection recorded in Sanctuary", Toast.LENGTH_SHORT).show()
            }
            collapseQuickThought()
        }

        // Entrance animation
        val card = quickThoughtView!!.findViewById<View>(R.id.v3_thought_card)
        card.alpha = 0f
        card.scaleX = 0.92f
        card.scaleY = 0.92f

        try {
            windowManager.addView(quickThoughtView, thoughtParams)
            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .start()

            input.requestFocus()
            input.postDelayed({
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }, 150)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying quick thought view: ${e.message}", e)
        }
    }

    private fun collapseQuickThought() {
        val view = quickThoughtView ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)

        try {
            windowManager.removeView(view)
        } catch (_: Exception) {}
        quickThoughtView = null
    }

    private fun saveThoughtAtomic(content: String) {
        serviceScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val tsFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            val date = dateFmt.format(Date(now))
            val ts = tsFmt.format(Date(now))
            val thoughtId = "thought_" + UUID.randomUUID().toString().take(8)
            val topic = if (activeAppLabel.isNotBlank()) "Thought ($activeAppLabel)" else "Quick Thought"

            val thought = V3Thought(
                id = thoughtId,
                timestamp = now,
                date = date,
                topic = topic,
                content = content,
                tags = "overlay"
            )
            repository.insertThought(thought)

            val event = V3Event(
                id = thoughtId,
                category = "thought",
                date = date,
                ts = ts,
                timestamp = now,
                title = topic,
                content = content,
                score = 0,
                metadataJson = "{\"source\":\"overlay\",\"app\":\"$activePackage\"}"
            )
            repository.insertEvent(event)
        }
    }

    // =========================================================================
    // Floating Directives & Tasks Checklist Overlay
    // =========================================================================

    @SuppressLint("InflateParams")
    private fun showChecklistOverlay() {
        if (checklistView != null) return
        collapseMenu()
        collapseQuickThought()

        val inflater = LayoutInflater.from(this)
        checklistView = inflater.inflate(R.layout.overlay_checklist, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        val container = checklistView!!.findViewById<LinearLayout>(R.id.checklist_items_container)
        val emptyView = checklistView!!.findViewById<TextView>(R.id.checklist_empty)
        val loadingView = checklistView!!.findViewById<TextView>(R.id.checklist_loading)
        val closeBtn = checklistView!!.findViewById<TextView>(R.id.checklist_close)

        closeBtn.setOnClickListener { collapseChecklist() }
        checklistView!!.findViewById<View>(R.id.checklist_backdrop)?.setOnClickListener {
            collapseChecklist()
        }

        try {
            windowManager.addView(checklistView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying checklist overlay: ${e.message}", e)
            return
        }

        renderTasksList(container, emptyView, loadingView)
    }

    private fun renderTasksList(
        container: LinearLayout,
        emptyView: TextView,
        loadingView: TextView
    ) {
        serviceScope.launch(Dispatchers.IO) {
            val tasks = try {
                repository.getTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks: ${e.message}")
                emptyList()
            }

            withContext(Dispatchers.Main) {
                loadingView.visibility = View.GONE
                container.removeAllViews()

                if (tasks.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    return@withContext
                }

                emptyView.visibility = View.GONE
                val inflater = LayoutInflater.from(this@V3BubbleOverlayService)

                tasks.forEach { task ->
                    val row = inflater.inflate(R.layout.overlay_checklist_item, container, false)
                    val circle = row.findViewById<TextView>(R.id.check_circle)
                    val labelView = row.findViewById<TextView>(R.id.check_label)

                    val isDone = task.status.equals("completed", ignoreCase = true) ||
                            task.status.equals("done", ignoreCase = true)

                    val deadlineText = if (!task.deadline.isNullOrBlank()) {
                        " • ${task.deadline.substringAfter('T').take(5)}"
                    } else ""
                    val priorityPrefix = if (task.priority.equals("HIGH", ignoreCase = true)) "⚡ " else ""
                    labelView.text = "$priorityPrefix${task.title}$deadlineText"

                    if (isDone) {
                        circle.text = "\u2713"
                        circle.setTextSize(9f)
                        circle.setTextColor(0xFF003A73.toInt())
                        circle.setBackgroundResource(R.drawable.overlay_check_circle_checked)
                        circle.alpha = 1.0f
                        labelView.setTextColor(0xFF50BF8E.toInt())
                        labelView.alpha = 0.7f
                        labelView.paint.isStrikeThruText = true
                    } else {
                        circle.text = ""
                        circle.setBackgroundResource(R.drawable.overlay_check_circle)
                        circle.alpha = 0.6f
                        labelView.setTextColor(0xFFFFFFFF.toInt())
                        labelView.alpha = 1.0f
                        labelView.paint.isStrikeThruText = false
                    }

                    val clickListener = View.OnClickListener {
                        serviceScope.launch(Dispatchers.IO) {
                            repository.toggleTaskStatus(task.id)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@V3BubbleOverlayService,
                                    if (isDone) "Task reopened" else "Task completed ✓",
                                    Toast.LENGTH_SHORT
                                ).show()
                                renderTasksList(container, emptyView, loadingView)
                            }
                        }
                    }
                    circle.setOnClickListener(clickListener)
                    labelView.setOnClickListener(clickListener)
                    row.setOnClickListener(clickListener)

                    container.addView(row)
                }
            }
        }
    }

    private fun collapseChecklist() {
        val view = checklistView ?: return
        try {
            windowManager.removeView(view)
        } catch (_: Exception) {}
        checklistView = null
    }

    private fun removeAllViews() {
        collapseMenu()
        collapseQuickThought()
        collapseChecklist()
        bubbleView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        bubbleView = null
    }
}
