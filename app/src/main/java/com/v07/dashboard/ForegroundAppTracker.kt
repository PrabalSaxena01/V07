package com.v07.dashboard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ForegroundAppTracker : AccessibilityService() {
    companion object {
        var currentForegroundPackage: String = ""
            private set
        var isConnected: Boolean = false
            private set
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            || event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            event.packageName?.toString()?.let {
                currentForegroundPackage = it
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        isConnected = true
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        isConnected = false
        currentForegroundPackage = ""
        return super.onUnbind(intent)
    }
}
