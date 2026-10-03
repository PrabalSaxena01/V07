package com.v07.v3.app

import android.content.Context
import android.util.Log
import com.v07.v3.app.server.LoopbackServer
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.core.data.DatabaseManager
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.plugin.PlatformJsBridge
import com.v07.v3.core.plugin.PluginRegistry

/**
 * V3PlatformHost
 *
 * Central coordinator and lifecycle manager for all V3 native subsystems:
 * - SQLite storage engine and EventRepository
 * - Database admin and root-free snapshot backup utility
 * - Focus Timer state machine and foreground ticker
 * - Screen Time passive telemetry watcher
 * - Authenticated HTTP Loopback Server
 */
object V3PlatformHost {
    private const val TAG = "V3PlatformHost"

    @Volatile
    private var initialized = false

    lateinit var repository: EventRepository
        private set
    lateinit var databaseManager: DatabaseManager
        private set
    lateinit var timerManager: TimerManager
        private set
    lateinit var screenTimeTracker: ScreenTimeTracker
        private set
    lateinit var loopbackServer: LoopbackServer
        private set
    lateinit var pluginRegistry: PluginRegistry
        private set
    lateinit var syncManager: com.v07.v3.core.sync.V3CompanionSyncManager
        private set
    lateinit var platformJsBridge: PlatformJsBridge
        private set

    val isInitialized: Boolean get() = initialized
    val baseUrl: String get() = if (initialized) loopbackServer.baseUrl else ""
    val sessionToken: String get() = if (initialized) loopbackServer.sessionToken else ""

    /**
     * Initializes all V3 platform components. Safe to call multiple times.
     */
    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        val appCtx = context.applicationContext

        Log.i(TAG, "Initializing V3PlatformHost...")
        try {
            com.google.firebase.FirebaseApp.initializeApp(appCtx)
            Log.i(TAG, "FirebaseApp initialized for V3")
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp init skipped or failed: ${e.message}")
        }
        repository = EventRepository.getInstance(appCtx)
        databaseManager = DatabaseManager.getInstance(appCtx)
        timerManager = TimerManager.getInstance(appCtx, repository)
        screenTimeTracker = ScreenTimeTracker(appCtx, repository)
        pluginRegistry = PluginRegistry.getInstance(appCtx)
        syncManager = com.v07.v3.core.sync.V3CompanionSyncManager.getInstance(appCtx, repository, screenTimeTracker)
        platformJsBridge = PlatformJsBridge(appCtx, repository, timerManager, screenTimeTracker, pluginRegistry, syncManager)
        loopbackServer = LoopbackServer(appCtx, repository, databaseManager, timerManager, screenTimeTracker, pluginRegistry, syncManager)

        // Pre-warm GitaProvider in background to maximize startup speed
        Thread {
            try {
                com.v07.v3.core.data.GitaProvider.getInstance(appCtx).ensureLoaded()
            } catch (e: Exception) {
                Log.w(TAG, "Gita background pre-warm error: ${e.message}")
            }
        }.apply {
            name = "V3-StartupPrewarm"
            priority = Thread.MIN_PRIORITY
            start()
        }

        initialized = true
        Log.i(TAG, "V3PlatformHost initialized successfully")
    }

    /**
     * Starts background services: Loopback Server and Screen Time telemetry.
     */
    @Synchronized
    fun startServices(preferredPort: Int = 8000): String {
        check(initialized) { "V3PlatformHost must be initialized before starting services" }

        loopbackServer.start(preferredPort)
        screenTimeTracker.start()

        // Start persistent Firestore realtime listener and initial cloud sync
        try {
            syncManager.startRealtimeSync()
            syncManager.syncNow { ok, err ->
                Log.i(TAG, "Startup cloud sync result: $ok (err: $err)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Startup cloud sync exception: ${e.message}")
        }

        Log.i(TAG, "V3PlatformHost services active on $baseUrl with token length ${sessionToken.length}")
        return baseUrl
    }

    /**
     * Stops background services cleanly.
     */
    @Synchronized
    fun stopServices() {
        if (!initialized) return
        try {
            syncManager.stopRealtimeSync()
        } catch (_: Exception) {}
        loopbackServer.stop()
        screenTimeTracker.stop()
        Log.i(TAG, "V3PlatformHost services stopped")
    }
}
