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
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.v07.dashboard.ForegroundAppTracker
import com.v07.dashboard.R
import com.v07.v3.app.V3Activity
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Event
import com.v07.v3.core.data.V3Task
import com.v07.v3.core.data.V3Thought
import com.v07.v3.ui.compose.FloatingBubblePillCompose
import com.v07.v3.ui.compose.FloatingOverlayMenuModal
import com.v07.v3.ui.compose.OverlayServiceLifecycleOwner
import com.v07.v3.ui.compose.bindViewToOverlayLifecycle
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
            return if (prefs.contains(PREF_KEY_ENABLED)) {
                prefs.getBoolean(PREF_KEY_ENABLED, false)
            } else {
                com.v07.dashboard.NotificationPreferences.isOverlayEnabled()
            }
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

    // Overlay lifecycle & Compose views
    private val serviceLifecycleOwner = OverlayServiceLifecycleOwner()
    private var bubbleView: View? = null
    private var menuView: View? = null

    // Compose reactive state for overlay telemetry
    private val activeAppIconState = mutableStateOf<Drawable?>(null)
    private val activeAppLabelState = mutableStateOf("V07 Sanctuary")
    private val activeAppUsageMsState = mutableStateOf(0L)
    private val formattedUsageState = mutableStateOf("0m")
    private val tasksState = mutableStateOf<List<V3Task>>(emptyList())

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

        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

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
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        serviceLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceLifecycleOwner.clear()
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

    private fun initializeBubble() {
        if (bubbleView != null) return

        val dm = resources.displayMetrics
        val minY = dpToPx(56)
        val maxY = dm.heightPixels - dpToPx(110)

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (dm.widthPixels - dpToPx(110)).coerceAtLeast(0)
            y = dpToPx(160).coerceIn(minY, maxY)
        }

        val composeView = ComposeView(this).apply {
            bindViewToOverlayLifecycle(this, serviceLifecycleOwner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                FloatingBubblePillCompose(
                    appIcon = activeAppIconState.value,
                    formattedUsage = formattedUsageState.value,
                    onClick = { toggleMenu() },
                    onDrag = { dx, dy ->
                        val dm = resources.displayMetrics
                        val minY = dpToPx(56)
                        val maxY = dm.heightPixels - dpToPx(110)
                        bubbleParams.x += dx.toInt()
                        bubbleParams.y = (bubbleParams.y + dy.toInt()).coerceIn(minY, maxY)
                        try {
                            windowManager.updateViewLayout(bubbleView, bubbleParams)
                        } catch (_: Exception) {}
                    },
                    onDragEnd = {
                        bubbleView?.let { snapBubbleToEdge(it, bubbleParams) }
                    }
                )
            }
        }

        bubbleView = composeView

        try {
            windowManager.addView(bubbleView, bubbleParams)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding bubble view to window manager: ${e.message}", e)
        }

        // Initialize immediately with current telemetry
        val currentIcon = if (activePackage.isNotBlank() && activePackage != packageName && !ignoredPackages.contains(activePackage)) {
            getAppIcon(activePackage)
        } else null
        updateBubbleUi(currentIcon, formatUsageDuration(activeAppUsageMs))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleTouch(view: View, params: WindowManager.LayoutParams) {
        var startX = 0f
        var startY = 0f
        var initialParamsX = 0
        var initialParamsY = 0
        var isDragging = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop.coerceAtLeast(dpToPx(12))

        view.setOnTouchListener { _, event ->
            val dm = resources.displayMetrics
            val minY = dpToPx(56)
            val maxY = dm.heightPixels - dpToPx(110)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    initialParamsX = params.x
                    initialParamsY = params.y
                    isDragging = false
                    view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY
                    if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = (initialParamsX + dx.toInt())
                        params.y = (initialParamsY + dy.toInt()).coerceIn(minY, maxY)
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (_: Exception) {}
                    }
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

        val minY = dpToPx(56)
        val maxY = dm.heightPixels - dpToPx(110)
        params.y = params.y.coerceIn(minY, maxY)

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
        val displayIcon = if (icon != null && activePackage.isNotBlank() && activePackage != packageName && !ignoredPackages.contains(activePackage)) {
            icon
        } else null
        activeAppIconState.value = displayIcon
        formattedUsageState.value = formattedUsage
    }

    private fun updateMenuUi(icon: Drawable?, appName: String, formattedUsage: String) {
        val displayIcon = if (icon != null && activePackage.isNotBlank() && activePackage != packageName && !ignoredPackages.contains(activePackage)) {
            icon
        } else null
        activeAppIconState.value = displayIcon
        activeAppLabelState.value = appName
        formattedUsageState.value = formattedUsage
    }

    private fun formatUsageDuration(ms: Long): String {
        val totalMins = (ms / 1000L) / 60
        val hrs = totalMins / 60
        val remMins = totalMins % 60
        return when {
            hrs > 0 -> "${hrs}h ${remMins}m"
            remMins > 0 -> "${remMins}m"
            ms > 0L -> "<1m"
            else -> "0m"
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
    // Editorial Action Menu & Compose Modal
    // =========================================================================

    private fun toggleMenu() {
        if (menuView != null) {
            collapseMenu()
        } else {
            showMenu()
        }
    }

    private fun showMenu() {
        if (menuView != null) return

        refreshTasks()

        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayWindowType(),
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    @Suppress("DEPRECATION") WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            x = 0
            y = 0
        }

        val composeView = ComposeView(this).apply {
            bindViewToOverlayLifecycle(this, serviceLifecycleOwner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                FloatingOverlayMenuModal(
                    appIcon = activeAppIconState.value,
                    appName = activeAppLabelState.value,
                    formattedUsage = formattedUsageState.value,
                    tasks = tasksState.value,
                    onClose = { collapseMenu() },
                    onStartFocus = {
                        collapseMenu()
                        startQuickFocusSession()
                    },
                    onOpenSanctuary = {
                        collapseMenu()
                        openSanctuaryApp()
                    },
                    onTurnOffOverlay = {
                        collapseMenu()
                        turnOffOverlay()
                    },
                    onSaveThought = { thoughtText ->
                        saveThoughtAtomic(thoughtText)
                        Toast.makeText(this@V3BubbleOverlayService, "Reflection recorded in Sanctuary", Toast.LENGTH_SHORT).show()
                    },
                    onToggleTask = { task ->
                        toggleTaskAtomic(task)
                    }
                )
            }
        }

        menuView = composeView

        try {
            windowManager.addView(menuView, menuParams)
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

    private fun refreshTasks() {
        serviceScope.launch(Dispatchers.IO) {
            val tasks = try {
                repository.getTasks()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks: ${e.message}")
                emptyList()
            }
            withContext(Dispatchers.Main) {
                tasksState.value = tasks
            }
        }
    }

    private fun toggleTaskAtomic(task: V3Task) {
        serviceScope.launch(Dispatchers.IO) {
            val isDone = task.status.equals("completed", ignoreCase = true) ||
                    task.status.equals("done", ignoreCase = true)
            repository.toggleTaskStatus(task.id)
            val updated = repository.getTasks()
            withContext(Dispatchers.Main) {
                tasksState.value = updated
                Toast.makeText(
                    this@V3BubbleOverlayService,
                    if (isDone) "Task reopened" else "Task completed ✓",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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

    private fun removeAllViews() {
        collapseMenu()
        bubbleView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        bubbleView = null
    }
}
