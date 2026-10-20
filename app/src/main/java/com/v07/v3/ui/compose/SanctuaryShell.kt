package com.v07.v3.ui.compose

import android.webkit.WebView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Root Sanctuary Hybrid Shell
 * Coordinates the Compose Native App Bar, Compose Native Home Screen,
 * Floating Pill Bottom Navigation Dock, and embedded WebView Plugin Host.
 */
@Composable
fun SanctuaryShell(
    webView: WebView,
    isPluginFullscreen: Boolean = false,
    onSetFullscreen: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(SanctuaryTab.HOME) }

    val screenTitle = when (currentTab) {
        SanctuaryTab.HOME -> "Home"
        SanctuaryTab.TOOLS -> "Tools"
        SanctuaryTab.JOURNAL -> "Journal"
        SanctuaryTab.DAY -> "Day"
        SanctuaryTab.SETTINGS -> "Settings"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (!isPluginFullscreen) {
                SanctuaryTopBar(
                    title = screenTitle,
                    onProfileClick = {
                        currentTab = SanctuaryTab.SETTINGS
                        webView.evaluateJavascript(
                            """
                            (function() {
                                if (typeof switchTab === 'function') switchTab('settings');
                                else if (typeof window.switchTab === 'function') window.switchTab('settings');
                            })();
                            """.trimIndent(),
                            null
                        )
                        onProfileClick()
                    }
                )
            }
        },
        bottomBar = {
            if (!isPluginFullscreen) {
                SanctuaryBottomDock(
                    currentTab = currentTab,
                    onTabSelected = { selected ->
                        currentTab = selected
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (!isPluginFullscreen) innerPadding.calculateTopPadding() else 0.dp)
        ) {
            // Keep SanctuaryWebHost ALWAYS composed so the WebView process is warmed up,
            // preserves its internal DOM state, and transitions with zero reload flicker.
            SanctuaryWebHost(
                webView = webView,
                currentTab = currentTab,
                isPluginFullscreen = isPluginFullscreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (!isPluginFullscreen) innerPadding.calculateBottomPadding() else 0.dp)
            )

            // When HOME tab is active and not fullscreen, show the native Compose Sanctuary Home Screen on top
            if (currentTab == SanctuaryTab.HOME && !isPluginFullscreen) {
                HomeScreen(
                    onOpenGitaCompanion = {
                        onSetFullscreen(true)
                        webView.evaluateJavascript(
                            """
                            (function() {
                                if (typeof openGitaModal === 'function') openGitaModal();
                                else if (typeof openPlugin === 'function') openPlugin('com.v07.gita');
                            })();
                            """.trimIndent(),
                            null
                        )
                    },
                    onOpenFocusChrono = {
                        onSetFullscreen(true)
                        webView.evaluateJavascript(
                            """
                            (function() {
                                if (typeof openFocusModal === 'function') openFocusModal();
                                else if (typeof openPlugin === 'function') openPlugin('com.v07.chronosynth');
                            })();
                            """.trimIndent(),
                            null
                        )
                    },
                    onOpenScreenTime = {
                        onSetFullscreen(true)
                        webView.evaluateJavascript(
                            """
                            (function() {
                                if (typeof openScreenTimeModal === 'function') openScreenTimeModal();
                                else if (typeof openPlugin === 'function') openPlugin('com.v07.timetracker');
                            })();
                            """.trimIndent(),
                            null
                        )
                    },
                    onOpenHabits = {
                        onSetFullscreen(true)
                        webView.evaluateJavascript(
                            """
                            (function() {
                                if (typeof openHabitsModal === 'function') openHabitsModal();
                                else if (typeof openPlugin === 'function') openPlugin('com.v07.habits');
                            })();
                            """.trimIndent(),
                            null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
