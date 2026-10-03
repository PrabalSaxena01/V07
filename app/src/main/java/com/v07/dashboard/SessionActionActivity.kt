package com.v07.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

class SessionActionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action
        Log.i("V07Server", "SessionActionActivity action=$action")
        if (action != null && action.startsWith("SESSION_")) {
            try {
                val serviceIntent = Intent(this, V07ServerService::class.java).apply {
                    this.action = action
                }
                startForegroundService(serviceIntent)
            } catch (e: Exception) {
                Log.e("V07Server", "SessionActionActivity failed to start service: ${e.message}")
            }
        }
        finish()
    }
}
