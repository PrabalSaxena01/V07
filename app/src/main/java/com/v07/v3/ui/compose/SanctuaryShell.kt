package com.v07.v3.ui.compose

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Root Sanctuary Hybrid Shell faithful to the V3 main branch:
 * - Native Compose Top Bar (V07 SANCTUARY)
 * - Native Compose Cockpit Dashboard (KPI Deck, Chrono Gauge, Daily Spine)
 * - Floating Bottom Dock (Cockpit, Cadence, Focus, Tasks, Journal)
 * - Seamless WebView Host for subsequent panels
 */
@Composable
fun SanctuaryShell(
    webView: WebView,
    isLight: Boolean = false,
    onToggleTheme: () -> Unit = {},
    isPluginFullscreen: Boolean = false,
    onSetFullscreen: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(SanctuaryTab.COCKPIT) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (!isPluginFullscreen) {
                SanctuaryTopBar(
                    isLight = isLight,
                    onToggleTheme = onToggleTheme,
                    onProfileClick = {
                        onSetFullscreen(true)
                        webView.evaluateJavascript(
                            """
                            (function() {
                                if (window.SanctuaryApp && typeof window.SanctuaryApp.openProfileModal === 'function') {
                                    window.SanctuaryApp.openProfileModal();
                                } else if (window.SanctuaryApp && typeof window.SanctuaryApp.openModal === 'function') {
                                    window.SanctuaryApp.openModal('profileModal');
                                }
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
            // Keep SanctuaryWebHost active so WebView process and tabs stay warmed up
            SanctuaryWebHost(
                webView = webView,
                currentTab = currentTab,
                isPluginFullscreen = isPluginFullscreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (!isPluginFullscreen) innerPadding.calculateBottomPadding() else 0.dp)
            )

            // When COCKPIT tab is active, render the native Jetpack Compose Cockpit Dashboard on top
            if (currentTab == SanctuaryTab.COCKPIT && !isPluginFullscreen) {
                CockpitScreen(
                    onNavigateToCadence = { currentTab = SanctuaryTab.CADENCE },
                    onNavigateToChrono = { currentTab = SanctuaryTab.FOCUS },
                    onNavigateToTasks = { currentTab = SanctuaryTab.TASKS },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
