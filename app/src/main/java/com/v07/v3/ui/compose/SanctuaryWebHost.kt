package com.v07.v3.ui.compose

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Sandboxed WebView Host for Tools, Ledger, Day, Settings, and Dynamic Plugins
 * Reuses the single high-performance WebView instance across tabs without reload flicker.
 */
@Composable
fun SanctuaryWebHost(
    webView: WebView,
    currentTab: SanctuaryTab,
    isPluginFullscreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Notify the underlying web application of tab transitions
    LaunchedEffect(currentTab) {
        val targetTab = when (currentTab) {
            SanctuaryTab.TOOLS -> "tools"
            SanctuaryTab.JOURNAL -> "journal"
            SanctuaryTab.DAY -> "day"
            SanctuaryTab.SETTINGS -> "settings"
            SanctuaryTab.HOME -> "home"
        }
        webView.evaluateJavascript(
            """
            (function() {
                if (typeof switchTab === 'function') {
                    switchTab('$targetTab');
                } else if (typeof window.switchTab === 'function') {
                    window.switchTab('$targetTab');
                }
            })();
            """.trimIndent(),
            null
        )
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.visibility = View.VISIBLE
            webView
        },
        update = { view ->
            view.visibility = View.VISIBLE
        }
    )
}
