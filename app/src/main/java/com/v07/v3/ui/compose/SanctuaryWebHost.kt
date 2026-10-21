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
 * Sandboxed WebView Host for Cadence, Focus, Tasks, and Journal
 * Synchronizes tab switching with window.SanctuaryApp.switchTab()
 */
@Composable
fun SanctuaryWebHost(
    webView: WebView,
    currentTab: SanctuaryTab,
    isPluginFullscreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(currentTab) {
        val targetTab = currentTab.id
        webView.evaluateJavascript(
            """
            (function() {
                if (window.SanctuaryApp && typeof window.SanctuaryApp.switchTab === 'function') {
                    window.SanctuaryApp.switchTab('$targetTab');
                } else if (typeof switchTab === 'function') {
                    switchTab('$targetTab');
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
            val shouldShow = isPluginFullscreen
            webView.visibility = if (shouldShow) View.VISIBLE else View.INVISIBLE
            webView
        },
        update = { view ->
            val shouldShow = isPluginFullscreen
            view.visibility = if (shouldShow) View.VISIBLE else View.INVISIBLE
        }
    )
}
