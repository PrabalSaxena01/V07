package com.v07.dashboard

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBw_23Rap_Ja9-CKfDEce6F8ZYgBgjNgsc")
                    .setApplicationId("1:689369760342:android:1f03ec126b581e8916f1f7")
                    .setProjectId("v07-dashboard")
                    .setStorageBucket("v07-dashboard.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                android.util.Log.i("App", "FirebaseApp initialized with FirebaseOptions in App.onCreate")
            }
        } catch (e: Exception) {
            android.util.Log.w("App", "FirebaseApp init error: ${e.message}")
        }
        VaultConfig.init(this)
        NotificationPreferences.init(this)
        PluginReminderReceiver.ensureNotificationChannel(this)
        Thread { migrateRunningDurations() }.start()
        try {
            val intent = Intent(this, V07ServerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    startForegroundService(intent)
                } catch (e: Exception) {
                    android.util.Log.w("App", "Legacy V07ServerService start deferred: ${e.message}")
                }
            } else {
                startService(intent)
            }
            if (NotificationPreferences.isOverlayEnabled() && Settings.canDrawOverlays(this)) {
                try {
                    OverlayService.start(this)
                } catch (e: Exception) {
                    android.util.Log.w("App", "OverlayService start deferred: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("App", "App init background tasks deferred: ${e.message}")
        }
    }
}
