package com.v07.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SessionActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("V07Server", "SessionActionReceiver: $action")
        try {
            val serviceIntent = Intent(context, V07ServerService::class.java).apply {
                this.action = action
            }
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e("V07Server", "Failed to start service with action $action: ${e.message}")
            context.getSharedPreferences("session_actions", Context.MODE_PRIVATE)
                .edit()
                .putString("pending_action", action)
                .putLong("action_ts", System.currentTimeMillis())
                .apply()
        }
    }
}