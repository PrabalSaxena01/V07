package com.v07.dashboard

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import com.v07.v3.core.data.EventRepository

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var bubbleView: View? = null
    private var expandedView: View? = null
    private var thinkInputView: View? = null
    private var checklistOverlayView: View? = null
    private var shareOverlayView: View? = null
    private var isExpanded = false
    private var isThinkInputOpen = false
    private var isChecklistOpen = false
    private var isShareOpen = false
    private var selectedThinkTopic: String = "quick"

    private var currentAppName = ""
    private var currentResumeMs = 0L
    private var lastPollMs = 0L
    private var currentAppTotalMs = 0L
    private var currentAppDate = java.time.LocalDate.now().toString()
    private var appLimits = mutableMapOf<String, Int>()
    private val DEFAULT_LIMIT_MIN = 30
    private var prefsRestored = false

    private val dayChangeReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            Log.i("OverlayService", "Received system broadcast: $action")
            if (action == Intent.ACTION_DATE_CHANGED ||
                action == Intent.ACTION_TIME_CHANGED ||
                action == Intent.ACTION_TIMEZONE_CHANGED
            ) {
                val now = System.currentTimeMillis()
                val today = java.time.LocalDate.now().toString()
                handleDayChange(now, today)
            }
        }
    }

    private fun handleDayChange(now: Long, today: String) {
        if (currentAppDate == today && currentAppTotalMs == 0L) return
        Log.i("OverlayService", "Day change reset: transitioned from '$currentAppDate' to '$today'")
        currentAppDate = today
        currentAppTotalMs = 0L
        currentResumeMs = now
        trackedAppTotalMs = 0L
        saveTrackedState()
        handler.post {
            updateTimerDisplay()
            updateNotification()
        }
    }

    private fun saveTrackedState() {
        getSharedPreferences("v07_prefs", MODE_PRIVATE).edit()
            .putString("tracked_app_name", currentAppName)
            .putLong("tracked_app_total_ms", currentAppTotalMs)
            .putString("tracked_app_date", currentAppDate)
            .putLong("tracked_app_resume_ms", currentResumeMs)
            .apply()
    }

    private fun restoreTrackedState(): Triple<String, Long, String> {
        val prefs = getSharedPreferences("v07_prefs", MODE_PRIVATE)
        val name = prefs.getString("tracked_app_name", "") ?: ""
        val ms = prefs.getLong("tracked_app_total_ms", 0L)
        val date = prefs.getString("tracked_app_date", "") ?: ""
        return Triple(name, ms, date)
    }

    private val ignoredPackages = setOf(
        "android", "com.android.systemui", "com.android.launcher",
        "com.android.launcher3", "com.coloros.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod", "com.android.shell",
        "com.android.documentsui", "com.android.providers.downloads",
        "com.v07.dashboard",
        "com.v07.dashboard.v2",
        "com.v07.dashboard.test",
        "com.v07.dashboard.v3"
    )

    private var limitsLastLoadedMs = 0L
    private val timerRunnable = object : Runnable {
        override fun run() {
            Thread {
                pollForegroundApp()
                handler.post {
                    updateTimerDisplay()
                    updateNotification()
                }
            }.start()
            val now = System.currentTimeMillis()
            if (now - limitsLastLoadedMs > 300_000L) {
                loadAppLimits()
                limitsLastLoadedMs = now
            }
            handler.postDelayed(this, 3_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Start foreground FIRST (Android 12+ requires this before any stopSelf)
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, OVERLAY_CHANNEL_ID)
            .setContentTitle("V07 Overlay")
            .setContentText("App timer active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .setOngoing(true)
            .build()
        startForeground(FOREGROUND_NOTIF_ID, notification)

        if (!Settings.canDrawOverlays(this)) {
            Log.w("OverlayService", "Overlay permission not granted, stopping")
            stopSelf()
            return
        }

        loadAppLimits()
        val filter = android.content.IntentFilter().apply {
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
        } catch (_: Exception) {
            try { registerReceiver(dayChangeReceiver, filter) } catch (_: Exception) {}
        }
        showBubble()
        handler.postDelayed(timerRunnable, 10_000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent?.action) {
            "ACTION_THINK" -> handler.postDelayed({ showThinkInput() }, 300)
            "ACTION_MOOD" -> handler.postDelayed({ sendQuickLog("mood") }, 300)
            "ACTION_CHECKLIST" -> handler.postDelayed({ showChecklistOverlay() }, 300)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try { unregisterReceiver(dayChangeReceiver) } catch (_: Exception) {}
        handler.removeCallbacks(timerRunnable)
        removeViews()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            OVERLAY_CHANNEL_ID, "V07 Overlay",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun removeViews() {
        try { bubbleView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { expandedView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { thinkInputView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { checklistOverlayView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        try { shareOverlayView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        bubbleView = null
        expandedView = null
        thinkInputView = null
        checklistOverlayView = null
        shareOverlayView = null
    }

    private fun showBubble() {
        if (bubbleView != null) return

        val inflater = LayoutInflater.from(this)
        bubbleView = inflater.inflate(R.layout.overlay_bubble, null)

        val timeText = bubbleView!!.findViewById<TextView>(R.id.overlay_time)
        timeText.text = formatElapsed(0)

        val params = createBubbleParams()
        setupDrag(bubbleView!!, params)

        bubbleView!!.setOnClickListener {
            if (!isExpanded) showExpanded()
            else collapseAll()
        }

        windowManager.addView(bubbleView, params)

        Thread {
            pollForegroundApp()
            handler.post {
                updateTimerDisplay()
                updateNotification()
            }
        }.start()
    }

    private fun showExpanded() {
        if (expandedView != null) return
        collapseAll()
        isExpanded = true

        val inflater = LayoutInflater.from(this)
        showBubbleActions(inflater)
    }

    private fun showBubbleActions(inflater: LayoutInflater) {
        expandedView = inflater.inflate(R.layout.overlay_bubble_actions, null)
        val params = createActionsNearBubbleParams()
        windowManager.addView(expandedView, params)

        // Close button
        expandedView!!.findViewById<View>(R.id.ba_close).setOnClickListener {
            collapseExpanded()
        }

        // OFF button
        expandedView!!.findViewById<View>(R.id.ba_off).setOnClickListener {
            collapseExpanded()
            NotificationPreferences.setOverlayEnabled(false)
            Toast.makeText(this@OverlayService, "Overlay turned off", Toast.LENGTH_SHORT).show()
            stopSelf()
        }

        // Button clicks
        expandedView!!.findViewById<View>(R.id.ba_think).setOnClickListener {
            collapseExpanded(); showThinkInput()
        }
        expandedView!!.findViewById<View>(R.id.ba_share).setOnClickListener {
            collapseExpanded(); showShareOverlay()
        }
        expandedView!!.findViewById<View>(R.id.ba_checklist).setOnClickListener {
            collapseExpanded(); showChecklistOverlay()
        }

        // Stagger animation
        handler.post {
            val buttons = listOf(
                expandedView!!.findViewById<View>(R.id.ba_think),
                expandedView!!.findViewById<View>(R.id.ba_share),
                expandedView!!.findViewById<View>(R.id.ba_checklist),
                expandedView!!.findViewById<View>(R.id.ba_off),
                expandedView!!.findViewById<View>(R.id.ba_close)
            )
            for (i in buttons.indices) {
                val btn = buttons[i]
                btn.scaleX = 0.7f; btn.scaleY = 0.7f; btn.alpha = 0f
                btn.animate()
                    .scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(200L)
                    .setStartDelay(i * 40L)
                    .setInterpolator(android.view.animation.OvershootInterpolator(1.3f))
                    .start()
            }
        }
    }


    private fun showPaletteLauncher(inflater: LayoutInflater) {
        expandedView = inflater.inflate(R.layout.overlay_palette_launcher, null)
        val params = createBackdropParams()
        windowManager.addView(expandedView, params)

        val panel = expandedView!!.findViewById<View>(R.id.palette_panel)
        val page1 = expandedView!!.findViewById<ViewGroup>(R.id.palette_page1)
        val page2 = expandedView!!.findViewById<ViewGroup>(R.id.palette_page2)
        val fab = expandedView!!.findViewById<View>(R.id.palette_fab)

        // Backdrop dismiss
        expandedView!!.findViewById<View>(R.id.palette_backdrop).setOnClickListener {
            collapseExpanded()
        }
        fab.setOnClickListener { collapseExpanded() }

        // Page 1 buttons
        expandedView!!.findViewById<View>(R.id.pbtn_think).setOnClickListener {
            collapseExpanded(); showThinkInput()
        }
        expandedView!!.findViewById<View>(R.id.pbtn_mood).setOnClickListener {
            collapseExpanded(); sendQuickLog("mood")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_energy).setOnClickListener {
            collapseExpanded(); sendQuickLog("energy")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_run).setOnClickListener {
            collapseExpanded(); sendQuickLog("run")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_more).setOnClickListener {
            // Slide to page 2
            page1.visibility = View.GONE
            page2.visibility = View.VISIBLE
            // Stagger page 2 rows
            for (i in 0 until page2.childCount) {
                val child = page2.getChildAt(i)
                child.alpha = 0f; child.translationX = 40f
                child.animate().alpha(1f).translationX(0f)
                    .setDuration(200L).setStartDelay(i * 25L)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        }

        // Page 2 buttons
        expandedView!!.findViewById<View>(R.id.pbtn_back).setOnClickListener {
            page2.visibility = View.GONE
            page1.visibility = View.VISIBLE
            for (i in 0 until page1.childCount) {
                val child = page1.getChildAt(i)
                child.alpha = 0f; child.translationX = -40f
                child.animate().alpha(1f).translationX(0f)
                    .setDuration(200L).setStartDelay(i * 25L)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        }
        expandedView!!.findViewById<View>(R.id.pbtn_win).setOnClickListener {
            collapseExpanded(); sendQuickLog("win")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_habit).setOnClickListener {
            collapseExpanded(); sendQuickLog("habit")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_dsa).setOnClickListener {
            collapseExpanded(); sendQuickLog("dsa")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_chess).setOnClickListener {
            collapseExpanded(); sendQuickLog("chess")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_pushups).setOnClickListener {
            collapseExpanded(); sendQuickLog("pushups")
        }
        expandedView!!.findViewById<View>(R.id.pbtn_panic).setOnClickListener {
            collapseExpanded(); sendQuickLog("panic")
        }

        // Apply theme-aware label colors for palette
        val isLightPalette = getSharedPreferences("v07_prefs", MODE_PRIVATE).getString("theme", "dark") == "light"
        val labelColorPalette = if (isLightPalette) Color.parseColor("#1a1a2e") else Color.parseColor("#e1e2ea")
        val btnIds = intArrayOf(
            R.id.pbtn_think, R.id.pbtn_mood, R.id.pbtn_energy, R.id.pbtn_run, R.id.pbtn_more,
            R.id.pbtn_back, R.id.pbtn_win, R.id.pbtn_habit, R.id.pbtn_dsa, R.id.pbtn_chess, R.id.pbtn_pushups, R.id.pbtn_panic
        )
        for (id in btnIds) {
            val btn = expandedView!!.findViewById<View>(id)
            if (btn is ViewGroup) {
                for (i in 0 until btn.childCount) {
                    val child = btn.getChildAt(i)
                    if (child is TextView && child != btn.getChildAt(0)) {
                        child.setTextColor(labelColorPalette)
                    }
                }
            }
        }

        // Animate panel in
        handler.post {
            panel.scaleX = 0.95f; panel.scaleY = 0.95f; panel.alpha = 0f
            panel.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(250L)
                .setInterpolator(android.view.animation.OvershootInterpolator(1.1f))
                .start()
            // Stagger page 1 rows
            for (i in 0 until page1.childCount) {
                val child = page1.getChildAt(i)
                child.alpha = 0f
                child.translationY = 12f
                child.animate().alpha(1f).translationY(0f)
                    .setDuration(180L).setStartDelay(60L + i * 25L)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun sendQuickLog(type: String) {
        Thread {
            try {
                val url = URL("http://127.0.0.1:8000/log/quick?type=$type")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }.start()
        handler.post { Toast.makeText(this, "$type logged", Toast.LENGTH_SHORT).show() }
    }

    private fun showThinkInput() {
        if (isThinkInputOpen) return
        collapseAll()
        isThinkInputOpen = true
        selectedThinkTopic = "quick"

        val inflater = LayoutInflater.from(this)
        thinkInputView = inflater.inflate(R.layout.overlay_think_input, null)

        val params = createBackdropParams(focusable = true)
        val input = thinkInputView!!.findViewById<EditText>(R.id.think_input)
        val saveBtn = thinkInputView!!.findViewById<TextView>(R.id.btn_save)
        val cancelBtn = thinkInputView!!.findViewById<TextView>(R.id.btn_cancel)
        val chipContainer = thinkInputView!!.findViewById<LinearLayout>(R.id.think_topic_chips)
        val customTopicInput = thinkInputView!!.findViewById<EditText>(R.id.think_custom_topic)
        var cachedTopics = emptyList<String>()

        fun refreshChips() {
            renderThinkTopicChips(chipContainer, cachedTopics) { topic ->
                if (topic.isEmpty()) {
                    customTopicInput.visibility = android.view.View.VISIBLE
                    customTopicInput.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showSoftInput(customTopicInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                } else {
                    customTopicInput.visibility = android.view.View.GONE
                    customTopicInput.setText("")
                }
                refreshChips()
            }
        }

        fetchThinkTopics { topics ->
            cachedTopics = topics
            handler.post { refreshChips() }
        }

        input.requestFocus()
        input.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        saveBtn.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                val topic = if (selectedThinkTopic.isEmpty()) {
                    customTopicInput.text.toString().trim().ifEmpty { "quick" }
                } else selectedThinkTopic
                postThought(text, topic)
                Toast.makeText(this, "Thought saved", Toast.LENGTH_SHORT).show()
            }
            collapseThinkInput()
        }

        cancelBtn.setOnClickListener {
            collapseThinkInput()
        }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                saveBtn.performClick()
                true
            } else false
        }

        windowManager.addView(thinkInputView, params)

        thinkInputView!!.findViewById<View>(R.id.think_backdrop)?.setOnClickListener {
            collapseThinkInput()
        }
    }

    // --- Checklist Overlay ---

    private fun showChecklistOverlay() {
        if (isChecklistOpen) return
        collapseAll()
        isChecklistOpen = true

        val inflater = LayoutInflater.from(this)
        checklistOverlayView = inflater.inflate(R.layout.overlay_checklist, null)

        val params = createBackdropParams()
        val container = checklistOverlayView!!.findViewById<LinearLayout>(R.id.checklist_items_container)
        val emptyView = checklistOverlayView!!.findViewById<TextView>(R.id.checklist_empty)
        val loadingView = checklistOverlayView!!.findViewById<TextView>(R.id.checklist_loading)
        val closeBtn = checklistOverlayView!!.findViewById<TextView>(R.id.checklist_close)

        closeBtn.setOnClickListener { collapseChecklist() }

        windowManager.addView(checklistOverlayView, params)

        checklistOverlayView!!.findViewById<View>(R.id.checklist_backdrop)?.setOnClickListener {
            collapseChecklist()
        }

        fetchChecklist(container, emptyView, loadingView)
    }

    private fun fetchChecklist(
        container: LinearLayout,
        emptyView: TextView,
        loadingView: TextView
    ) {
        Thread {
            try {
                val tasks = EventRepository.getInstance(this@OverlayService).getTasks()

                handler.post {
                    loadingView.visibility = View.GONE
                    container.removeAllViews()

                    if (tasks.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        return@post
                    }
                    emptyView.visibility = View.GONE

                    val inflater = LayoutInflater.from(this@OverlayService)
                    for (task in tasks) {
                        val id = task.id
                        val label = task.title
                        val done = task.status.equals("completed", ignoreCase = true) || task.status.equals("done", ignoreCase = true)

                        val row = inflater.inflate(R.layout.overlay_checklist_item, container, false)
                        val circle = row.findViewById<TextView>(R.id.check_circle)
                        val labelView = row.findViewById<TextView>(R.id.check_label)

                        labelView.text = label
                        if (done) {
                            circle.text = "\u2713"
                            circle.setTextSize(9f)
                            circle.setTextColor(0xFF003A73.toInt())
                            circle.setBackgroundResource(R.drawable.overlay_check_circle_checked)
                            circle.alpha = 1.0f
                            labelView.setTextColor(0xFF4DE086.toInt())
                            labelView.alpha = 0.8f
                            labelView.paint.isStrikeThruText = true
                        } else {
                            circle.text = ""
                            circle.setBackgroundResource(R.drawable.overlay_check_circle)
                            circle.alpha = 0.6f
                            labelView.setTextColor(0xFFC1C6D4.toInt())
                            labelView.alpha = 1.0f
                            labelView.paint.isStrikeThruText = false
                        }

                        val clickListener = View.OnClickListener { toggleChecklistItem(id, row) }
                        circle.setOnClickListener(clickListener)
                        labelView.setOnClickListener(clickListener)
                        row.setOnClickListener(clickListener)

                        container.addView(row)
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    loadingView.text = "Error: ${e.message}"
                }
            }
        }.start()
    }

    private fun toggleChecklistItem(id: String, row: View) {
        Thread {
            try {
                val repo = EventRepository.getInstance(this@OverlayService)
                val success = repo.toggleTaskStatus(id)
                if (success) {
                    val updatedTasks = repo.getTasks()
                    val task = updatedTasks.find { it.id == id }
                    val newDone = task?.let { it.status.equals("completed", ignoreCase = true) || it.status.equals("done", ignoreCase = true) } ?: false

                    handler.post {
                        val circle = row.findViewById<TextView>(R.id.check_circle)
                        val labelView = row.findViewById<TextView>(R.id.check_label)
                        if (newDone) {
                            circle.text = "\u2713"
                            circle.setTextSize(9f)
                            circle.setTextColor(0xFF003A73.toInt())
                            circle.setBackgroundResource(R.drawable.overlay_check_circle_checked)
                            circle.alpha = 1.0f
                            labelView.setTextColor(0xFF4DE086.toInt())
                            labelView.alpha = 0.8f
                            labelView.paint.isStrikeThruText = true
                        } else {
                            circle.text = ""
                            circle.setBackgroundResource(R.drawable.overlay_check_circle)
                            circle.alpha = 0.6f
                            labelView.setTextColor(0xFFC1C6D4.toInt())
                            labelView.alpha = 1.0f
                            labelView.paint.isStrikeThruText = false
                        }
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(this@OverlayService, "Toggle failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun collapseChecklist() {
        if (checklistOverlayView != null) {
            try { windowManager.removeView(checklistOverlayView) } catch (_: Exception) {}
            checklistOverlayView = null
        }
        isChecklistOpen = false
    }

    // --- Share Overlay ---

    private var availableTags: List<String> = emptyList()
    private var selectedTags: MutableSet<String> = mutableSetOf()

    private fun showShareOverlay() {
        if (isShareOpen) return
        collapseAll()
        isShareOpen = true
        selectedTags.clear()

        val inflater = LayoutInflater.from(this)
        shareOverlayView = inflater.inflate(R.layout.overlay_share_input, null)

        val params = createBackdropParams(focusable = true)

        shareOverlayView!!.findViewById<View>(R.id.share_backdrop).setOnClickListener {
            collapseShare()
        }

        val contentPreview = shareOverlayView!!.findViewById<TextView>(R.id.share_content_preview)
        val titleInput = shareOverlayView!!.findViewById<EditText>(R.id.share_title)
        val infoInput = shareOverlayView!!.findViewById<EditText>(R.id.share_info)
        val tagsContainer = shareOverlayView!!.findViewById<LinearLayout>(R.id.share_tags_container)
        val saveBtn = shareOverlayView!!.findViewById<TextView>(R.id.btn_save)
        val cancelBtn = shareOverlayView!!.findViewById<TextView>(R.id.btn_cancel)

        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        var clipText: String? = null
        if (clip != null && clip.itemCount > 0) {
            clipText = clip.getItemAt(0).text?.toString()
        }

        if (clipText != null && clipText.isNotEmpty()) {
            contentPreview.visibility = View.VISIBLE
            contentPreview.text = clipText
        }

        val pasteBtn = shareOverlayView!!.findViewById<ImageButton>(R.id.btn_paste_clipboard)
        pasteBtn.setOnClickListener {
            try {
                val cm2 = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip2 = cm2.primaryClip
                if (clip2 != null && clip2.itemCount > 0) {
                    val text = clip2.getItemAt(0).text?.toString()
                    if (text != null && text.isNotEmpty()) {
                        infoInput.setText(text)
                        infoInput.setSelection(text.length)
                        if (titleInput.text.isNullOrEmpty()) {
                            titleInput.setText(if (text.length > 60) text.substring(0, 60) + "..." else text)
                        }
                        Toast.makeText(this, "Pasted", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) {}
        }

        fetchTags { tags ->
            availableTags = tags
            handler.post { renderTagChips(tagsContainer) }
        }

        titleInput.requestFocus()
        titleInput.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(titleInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        cancelBtn.setOnClickListener { collapseShare() }

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val info = infoInput.text.toString().trim()
            val type = if (clipText != null && (clipText.startsWith("http://") || clipText.startsWith("https://"))) "url" else "text"
            postShare(title, info, type, clipText, selectedTags.joinToString(","))
            Toast.makeText(this, "Saved to vault", Toast.LENGTH_SHORT).show()
            collapseShare()
        }

        windowManager.addView(shareOverlayView, params)
    }

    private fun renderTagChips(container: LinearLayout) {
        container.removeAllViews()
        for (tag in availableTags) {
            val chip = TextView(this).apply {
                text = tag
                textSize = 11f
                typeface = android.graphics.Typeface.MONOSPACE
                setTextColor(if (selectedTags.contains(tag)) 0xFF8D6CFF.toInt() else 0xFFC1C6D4.toInt())
                background = getDrawable(if (selectedTags.contains(tag)) R.drawable.overlay_tag_selected_bg else R.drawable.overlay_tag_bg)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                setOnClickListener {
                    if (selectedTags.contains(tag)) selectedTags.remove(tag) else selectedTags.add(tag)
                    renderTagChips(container)
                }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6); bottomMargin = dp(4) }
            container.addView(chip, lp)
        }
    }

    private fun fetchTags(callback: (List<String>) -> Unit) {
        Thread {
            try {
                val url = URL("http://127.0.0.1:8000/tags")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val json = org.json.JSONObject(body)
                    val arr = json.getJSONArray("tags")
                    val tags = (0 until arr.length()).map { arr.getJSONObject(it).getString("name") }
                    callback(tags)
                } else {
                    conn.disconnect()
                    callback(emptyList())
                }
            } catch (_: Exception) {
                callback(emptyList())
            }
        }.start()
    }

    private fun postShare(title: String, info: String, type: String, content: String?, tags: String) {
        Thread {
            try {
                val params = mutableListOf("title=$title", "type=$type")
                if (info.isNotEmpty()) params.add("info=$info")
                if (content != null) params.add("content=${URLEncoder.encode(content, "UTF-8")}")
                if (tags.isNotEmpty()) params.add("tags=${URLEncoder.encode(tags, "UTF-8")}")
                val now = LocalDateTime.now()
                params.add("captured_time=${URLEncoder.encode(now.format(DateTimeFormatter.ofPattern("HH:mm")), "UTF-8")}")
                val loc = getLocationString()
                if (loc.isNotEmpty()) params.add("location=${URLEncoder.encode(loc, "UTF-8")}")

                val url = URL("http://127.0.0.1:8000/log/share?${params.joinToString("&")}")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }.start()
    }

    @Suppress("MissingPermission")
    private fun getLocationString(): String {
        return try {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) String.format("%.4f, %.4f", loc.latitude, loc.longitude) else ""
        } catch (_: Exception) { "" }
    }

    private fun collapseShare() {
        if (shareOverlayView != null) {
            try { windowManager.removeView(shareOverlayView) } catch (_: Exception) {}
            shareOverlayView = null
        }
        isShareOpen = false
    }

    private fun fetchThinkTopics(callback: (List<String>) -> Unit) {
        Thread {
            try {
                val url = URL("http://127.0.0.1:8000/meta/think-topics")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val arr = org.json.JSONArray(body)
                    val topics = (0 until arr.length()).map { arr.getString(it) }
                    callback(topics)
                } else {
                    conn.disconnect()
                    callback(emptyList())
                }
            } catch (_: Exception) {
                callback(emptyList())
            }
        }.start()
    }

    private fun renderThinkTopicChips(container: LinearLayout, topics: List<String>, onSelect: (String) -> Unit) {
        container.removeAllViews()
        val allTopics = topics.toMutableList()
        if (!allTopics.contains("Quick")) allTopics.add(0, "Quick")

        fun truncate(s: String): String {
            val words = s.split("\\s+".toRegex())
            return if (words.size > 20) words.take(20).joinToString(" ") + "…" else s
        }

        val density = resources.displayMetrics.density
        val displayW = resources.displayMetrics.widthPixels
        val wrapperPad = dp(20)
        val margin = dp(32)
        val containerWidth = ((container.parent as? View)?.width ?: (displayW - wrapperPad - margin)).coerceAtLeast(100)
        val availableWidth = containerWidth - dp(12)

        data class ChipDef(val text: String, val isSelected: Boolean, val onClick: () -> Unit)

        val chips = allTopics.map { topic ->
            val isSel = selectedThinkTopic == topic
            ChipDef(truncate(topic), isSel, {
                selectedThinkTopic = if (isSel) "" else topic
                onSelect(selectedThinkTopic)
            })
        } + ChipDef("Other\u2026", selectedThinkTopic.isEmpty(), {
            selectedThinkTopic = ""
            onSelect("")
        })

        val paint = android.graphics.Paint().apply {
            textSize = 11f * density
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val padH = dp(10) + dp(10)
        val marginGap = dp(6) + dp(3)

        var currentRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        var rowWidth = 0

        for (chipDef in chips) {
            val textW = (paint.measureText(chipDef.text) + padH + dp(2)).toInt()
            val chipW = textW + marginGap
            if (rowWidth + chipW > availableWidth && rowWidth > 0) {
                container.addView(currentRow)
                currentRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                rowWidth = 0
            }
            val chip = TextView(this).apply {
                text = chipDef.text
                textSize = 11f
                typeface = android.graphics.Typeface.MONOSPACE
                setTextColor(if (chipDef.isSelected) 0xFF8D6CFF.toInt() else 0xFFC1C6D4.toInt())
                background = getDrawable(if (chipDef.isSelected) R.drawable.overlay_tag_selected_bg else R.drawable.overlay_tag_bg)
                setPadding(dp(10), dp(5), dp(10), dp(5))
                setOnClickListener { chipDef.onClick() }
            }
            currentRow.addView(chip, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6); bottomMargin = dp(3) })
            rowWidth += chipW
        }
        if (currentRow.childCount > 0) container.addView(currentRow)
    }

    private fun postThought(text: String, topic: String = "quick") {
        Thread {
            try {
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val encodedTopic = URLEncoder.encode(topic, "UTF-8")
                val url = URL("http://127.0.0.1:8000/log/think?text=$encodedText&topic=$encodedTopic")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }.start()
    }

    private fun collapseExpanded() {
        if (expandedView != null) {
            try { windowManager.removeView(expandedView) } catch (_: Exception) {}
            expandedView = null
        }
        isExpanded = false
    }

    private fun collapseThinkInput() {
        if (thinkInputView != null) {
            try { windowManager.removeView(thinkInputView) } catch (_: Exception) {}
            thinkInputView = null
        }
        isThinkInputOpen = false
    }

    private fun collapseAll() {
        collapseExpanded()
        collapseThinkInput()
        collapseChecklist()
        collapseShare()
    }

    // --- App Usage Tracking ---

    private fun loadAppLimits() {
        try {
            val prefs = getSharedPreferences("v07_prefs", MODE_PRIVATE)
            val json = prefs.getString("app_limits", null)
            if (json != null) {
                val obj = JSONObject(json)
                appLimits.clear()
                for (key in obj.keys()) {
                    appLimits[key] = obj.getInt(key)
                }
            }
        } catch (_: Exception) {}
    }

    // --- Timer ---

    private fun calculateUsageSinceMidnight(
        usm: UsageStatsManager,
        targetPkg: String,
        midnight: Long,
        now: Long
    ): Long {
        if (targetPkg.isEmpty() || ignoredPackages.contains(targetPkg) || now <= midnight) {
            return 0L
        }
        val maxPossible = (now - midnight).coerceAtLeast(0L)
        var totalMs = 0L

        try {
            val events = usm.queryEvents(midnight, now)
            val event = android.app.usage.UsageEvents.Event()
            var sessionStart = -1L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                val time = event.timeStamp
                val type = event.eventType

                if (type == 16 || type == 18) { // Screen off or keyguard shown
                    if (sessionStart != -1L) {
                        totalMs += (time - sessionStart).coerceAtLeast(0L)
                        sessionStart = -1L
                    }
                    continue
                }

                if (pkg == targetPkg) {
                    when (type) {
                        android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED -> {
                            if (sessionStart == -1L) {
                                sessionStart = time
                            }
                        }
                        android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                        android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED -> {
                            if (sessionStart != -1L) {
                                totalMs += (time - sessionStart).coerceAtLeast(0L)
                                sessionStart = -1L
                            }
                        }
                    }
                } else {
                    if (type == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED &&
                        !ignoredPackages.contains(pkg)) {
                        if (sessionStart != -1L) {
                            totalMs += (time - sessionStart).coerceAtLeast(0L)
                            sessionStart = -1L
                        }
                    }
                }
            }

            if (sessionStart != -1L) {
                totalMs += (now - sessionStart).coerceAtLeast(0L)
            }
        } catch (e: Exception) {
            Log.w("OverlayService", "calculateUsageSinceMidnight error: ${e.message}")
        }

        return totalMs.coerceIn(0L, maxPossible)
    }

    private fun pollForegroundApp() {
        try {
            val now = System.currentTimeMillis()
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val isScreenOn = powerManager?.isInteractive ?: true
            if (!isScreenOn) {
                lastPollMs = now
                return
            }

            val today = java.time.LocalDate.now().toString()
            if (today != currentAppDate) {
                handleDayChange(now, today)
            }

            val elapsed = if (lastPollMs > 0) now - lastPollMs else 0L

            if (!prefsRestored && lastPollMs == 0L) {
                val saved = restoreTrackedState()
                if (saved.first.isNotEmpty() && saved.third == today) {
                    currentAppName = saved.first
                    currentAppTotalMs = saved.second
                    currentAppDate = saved.third
                } else {
                    currentAppName = ""
                    currentAppTotalMs = 0L
                    currentAppDate = today
                }
                prefsRestored = true
            }

            var pkg = ""

            if (ForegroundAppTracker.isConnected && ForegroundAppTracker.currentForegroundPackage.isNotEmpty()) {
                val trackerPkg = ForegroundAppTracker.currentForegroundPackage
                if (!ignoredPackages.contains(trackerPkg)) {
                    pkg = trackerPkg
                }
            }

            if (pkg.isEmpty()) {
                getForegroundAppViaEvents()?.let {
                    if (!ignoredPackages.contains(it)) {
                        pkg = it
                    }
                }
            }

            if (pkg.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = am.runningAppProcesses
                if (tasks != null) {
                    for (task in tasks) {
                        if (task.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            || task.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                            if (!ignoredPackages.contains(task.processName)) {
                                pkg = task.processName
                                break
                            }
                        }
                    }
                }
            }

            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val midnight = cal.timeInMillis

            if (pkg.isNotEmpty()) {
                if (pkg != currentAppName) {
                    currentAppName = pkg
                    currentAppTotalMs = calculateUsageSinceMidnight(usm, pkg, midnight, now)
                    currentResumeMs = now
                } else {
                    if (elapsed in 1..4999) {
                        currentAppTotalMs += elapsed
                    }
                    if (now - currentResumeMs > 15_000L) {
                        val computed = calculateUsageSinceMidnight(usm, pkg, midnight, now)
                        if (computed > 0L) {
                            currentAppTotalMs = computed
                        }
                    }
                }
            } else {
                if (currentAppName.isNotEmpty()) {
                    currentAppName = ""
                    currentAppTotalMs = 0L
                    currentResumeMs = 0L
                }
            }

            trackedAppName = currentAppName
            trackedAppTotalMs = currentAppTotalMs
            saveTrackedState()
        } catch (e: Exception) {
            Log.e("OverlayService", "pollForegroundApp failed: ${e.message}")
        }
        lastPollMs = System.currentTimeMillis()
    }

    private fun getForegroundAppViaEvents(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val events = usm.queryEvents(now - 30000, now)
            val event = android.app.usage.UsageEvents.Event()
            var currentPkg: String? = null
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED -> {
                        if (!ignoredPackages.contains(event.packageName)) {
                            currentPkg = event.packageName
                        }
                    }
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                    android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED -> {
                        if (currentPkg == event.packageName) {
                            currentPkg = null
                        }
                    }
                }
            }
            currentPkg
        } catch (_: Exception) { null }
    }

    private fun updateTimerDisplay() {
        val totalMs = if (currentAppName.isNotEmpty()) currentAppTotalMs else 0L
        val timeText = bubbleView?.findViewById<TextView>(R.id.overlay_time)
        timeText?.text = formatElapsed(totalMs)

        val limitMin = appLimits[currentAppName] ?: DEFAULT_LIMIT_MIN
        val totalMin = totalMs / 60_000
        val ratio = totalMin.toFloat() / limitMin
        val color = when {
            ratio >= 1.0f -> Color.parseColor("#FF6464")
            ratio >= 0.5f -> Color.parseColor("#F4D35E")
            else -> Color.parseColor("#4de086")
        }
        timeText?.setTextColor(color)
    }

    private fun formatElapsed(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        return when {
            h > 0 -> "${h}h ${m}m"
            m > 0 -> "${m}m"
            else -> "<1m"
        }
    }

    private fun getAppIconBitmap(pkg: String): Bitmap? {
        return try {
            val drawable = packageManager.getApplicationIcon(pkg)
            if (drawable is android.graphics.drawable.BitmapDrawable) {
                drawable.bitmap
            } else {
                val w = drawable.intrinsicWidth.coerceAtLeast(1)
                val h = drawable.intrinsicHeight.coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (_: Exception) { null }
    }

    private fun updateNotification() {
        val hasApp = currentAppName.isNotEmpty()
        val totalMs = if (hasApp) currentAppTotalMs else 0L
        val text = if (hasApp) "${formatElapsed(totalMs)} today" else "App timer active"
        val openIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, OVERLAY_CHANNEL_ID)
            .setContentTitle("")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openIntent)
            .setOngoing(true)
        if (hasApp) {
            getAppIconBitmap(currentAppName)?.let { builder.setLargeIcon(it) }
        }
        startForeground(FOREGROUND_NOTIF_ID, builder.build())
    }

    // --- Drag ---

    private fun setupDrag(view: View, params: WindowManager.LayoutParams) {
        var startX = 0f
        var startY = 0f
        var startParamsX = 0
        var startParamsY = 0
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    startParamsX = params.x
                    startParamsY = params.y
                    isDragging = false
                    view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startX
                    val dy = event.rawY - startY
                    if (kotlin.math.abs(dx) > 5 || kotlin.math.abs(dy) > 5) {
                        isDragging = true
                    }
                    params.x = startParamsX + dx.toInt()
                    params.y = startParamsY + dy.toInt()
                    try {
                        windowManager.updateViewLayout(view, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                    if (!isDragging && event.action == MotionEvent.ACTION_UP) {
                        view.performClick()
                    } else {
                        snapToEdge(view, params)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToEdge(view: View, params: WindowManager.LayoutParams) {
        val screenW = resources.displayMetrics.widthPixels
        val viewW = if (view.measuredWidth > 0) view.measuredWidth else 120
        val centerX = params.x + viewW / 2
        val snapX = if (centerX < screenW / 2) 0 else screenW - viewW
        val startX = params.x
        val animator = android.animation.ValueAnimator.ofInt(startX, snapX).apply {
            duration = 240
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { va ->
                params.x = va.animatedValue as Int
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (_: Exception) {}
            }
        }
        animator.start()
    }

    // --- Layout Params ---

    private fun createBubbleParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = resources.displayMetrics.widthPixels - dp(100)
            y = 200
        }
    }

    private fun createExpandedParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val dm = resources.displayMetrics
        val w = (dm.widthPixels * 0.80).toInt()
        val h = (dm.heightPixels * 0.80).toInt()

        return WindowManager.LayoutParams(
            w, h,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = (dm.widthPixels - w) / 2
            y = (dm.heightPixels - h) / 2
        }
    }

    private fun createCenteredParams(w: Int, h: Int, focusable: Boolean = false): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val dm = resources.displayMetrics
        val flags = if (focusable) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }

        return WindowManager.LayoutParams(
            w, h,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = (dm.widthPixels - w) / 2
            y = (dm.heightPixels - h) / 2
        }
    }

    private fun createBackdropParams(focusable: Boolean = false): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val dm = resources.displayMetrics
        val flags = if (focusable) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }

        return WindowManager.LayoutParams(
            dm.widthPixels,
            dm.heightPixels,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = 0
            y = 0
        }
    }

    private fun createActionsNearBubbleParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val bubbleParams = bubbleView?.layoutParams as? WindowManager.LayoutParams
        val bx = bubbleParams?.x ?: (resources.displayMetrics.widthPixels - dp(100))
        val by = bubbleParams?.y ?: 200
        val bubbleW = bubbleView?.measuredWidth ?: dp(42)

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = bx + bubbleW + dp(4)
            y = by
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val OVERLAY_CHANNEL_ID = "v07_overlay_channel"
        private const val FOREGROUND_NOTIF_ID = 3000

        var trackedAppName = ""
        var trackedAppTotalMs = 0L

        fun formatAppTime(ms: Long): String {
            val totalSec = ms / 1000
            val h = totalSec / 3600
            val m = (totalSec % 3600) / 60
            return when {
                h > 0 -> "${h}h ${m}m"
                m > 0 -> "${m}m"
                else -> "<1m"
            }
        }

        var savedProjection: android.media.projection.MediaProjection? = null
        var savedProjectionData: Intent? = null

        fun start(context: Context) {
            com.v07.v3.app.overlay.V3BubbleOverlayService.start(context)
        }

        fun stop(context: Context) {
            com.v07.v3.app.overlay.V3BubbleOverlayService.stop(context)
            try {
                context.stopService(Intent(context, OverlayService::class.java))
            } catch (_: Exception) {}
        }
    }
}
