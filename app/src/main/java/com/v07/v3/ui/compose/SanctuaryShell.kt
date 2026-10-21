package com.v07.v3.ui.compose

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Root Sanctuary Hybrid Shell faithful to V07 Sanctuary:
 * - 5 native Compose core tabs: HOME, TOOLS, LEDGER, DAY, SETTINGS
 * - Dedicated sub-views: FOCUS (Focus Sanctuary), SCREENTIME (Digital Stillness), DATACENTER (Data Center)
 * - Dedicated sandboxed plugin host: PLUGIN (PluginHostScreen)
 * - Native Compose Top Bar (V07 SANCTUARY)
 * - Floating Bottom Dock (Home, Tools, Ledger, Day, Settings)
 * - Background WebView Host for plugin extension execution
 */
@Composable
fun SanctuaryShell(
    webView: WebView,
    isLight: Boolean = false,
    onToggleTheme: () -> Unit = {},
    isPluginFullscreen: Boolean = false,
    onSetFullscreen: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onImportPluginZip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(SanctuaryTab.HOME) }
    var activeSubView by remember { mutableStateOf(SanctuarySubView.NONE) }
    var activePlugin by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // Intercept back presses: exit plugin first, then sub-view, or return to Home tab
    BackHandler(enabled = activePlugin != null || activeSubView != SanctuarySubView.NONE || currentTab != SanctuaryTab.HOME) {
        if (activePlugin != null) {
            activePlugin = null
        } else if (activeSubView != SanctuarySubView.NONE) {
            activeSubView = SanctuarySubView.NONE
        } else if (currentTab != SanctuaryTab.HOME) {
            currentTab = SanctuaryTab.HOME
        }
    }

    val isSubViewActive = activeSubView != SanctuarySubView.NONE || activePlugin != null
    val showScaffoldBars = !isPluginFullscreen && !isSubViewActive

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (showScaffoldBars) {
                SanctuaryTopBar(
                    isLight = isLight,
                    onToggleTheme = onToggleTheme,
                    onProfileClick = {
                        currentTab = SanctuaryTab.SETTINGS
                        onProfileClick()
                    }
                )
            }
        },
        bottomBar = {
            if (showScaffoldBars) {
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
                .padding(
                    top = if (showScaffoldBars) innerPadding.calculateTopPadding() else 0.dp,
                    bottom = if (showScaffoldBars) innerPadding.calculateBottomPadding() else 0.dp
                )
        ) {
            // Keep SanctuaryWebHost active so WebView process and plugins stay synchronized
            SanctuaryWebHost(
                webView = webView,
                currentTab = currentTab,
                isPluginFullscreen = isPluginFullscreen,
                modifier = Modifier.fillMaxSize()
            )

            // When not in external plugin fullscreen, render native Compose Sanctuary screens
            if (!isPluginFullscreen) {
                if (activePlugin != null) {
                    val p = activePlugin!!
                    PluginHostScreen(
                        pluginId = p.first,
                        pluginTitle = p.second,
                        pluginSubtitle = p.third,
                        isLight = isLight,
                        onBack = { activePlugin = null },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (activeSubView != SanctuarySubView.NONE) {
                    when (activeSubView) {
                        SanctuarySubView.FOCUS -> {
                            FocusSanctuaryScreen(
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuarySubView.SCREENTIME -> {
                            ScreenTimeScreen(
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuarySubView.DATACENTER -> {
                            DataCenterScreen(
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuarySubView.READING -> {
                            PluginHostScreen(
                                pluginId = "com.v07.reader",
                                pluginTitle = "Sovereign Shelf",
                                pluginSubtitle = "Kindle & physical book reading progress",
                                isLight = isLight,
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuarySubView.GITA -> {
                            PluginHostScreen(
                                pluginId = "com.v07.gita",
                                pluginTitle = "Bhagavad Gita",
                                pluginSubtitle = "Sacred verses, Sanskrit & commentaries",
                                isLight = isLight,
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuarySubView.HABITS -> {
                            PluginHostScreen(
                                pluginId = "com.v07.habits",
                                pluginTitle = "Gentle Habits",
                                pluginSubtitle = "Rhythm, cadence & consistency tracking",
                                isLight = isLight,
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuarySubView.TASKS -> {
                            PluginHostScreen(
                                pluginId = "com.v07.tasks",
                                pluginTitle = "Sovereign Tasks",
                                pluginSubtitle = "Daily directives, quiet priorities & deadlines",
                                isLight = isLight,
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuarySubView.MOOD -> {
                            PluginHostScreen(
                                pluginId = "com.v07.vitals",
                                pluginTitle = "Mood & Energy",
                                pluginSubtitle = "Emotional equilibrium & wellness trends",
                                isLight = isLight,
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            FocusSanctuaryScreen(
                                onBack = { activeSubView = SanctuarySubView.NONE },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    when (currentTab) {
                        SanctuaryTab.HOME -> {
                            HomeScreen(
                                onNavigateToSubView = { subView -> activeSubView = subView },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuaryTab.TOOLS -> {
                            ToolsScreen(
                                onNavigateToSubView = { subView -> activeSubView = subView },
                                onNavigateToTab = { tab -> currentTab = tab },
                                onOpenPlugin = { pid, title, subtitle ->
                                    activePlugin = Triple(pid, title, subtitle)
                                },
                                onImportPluginZip = onImportPluginZip,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuaryTab.LEDGER -> {
                            LedgerScreen(
                                onNavigateToDatacenter = { activeSubView = SanctuarySubView.DATACENTER },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuaryTab.DAY -> {
                            DayScreen(
                                onNavigateToScreentime = { activeSubView = SanctuarySubView.SCREENTIME },
                                onNavigateToFocus = { activeSubView = SanctuarySubView.FOCUS },
                                onNavigateToHabits = { currentTab = SanctuaryTab.HOME },
                                onNavigateToJournal = { currentTab = SanctuaryTab.LEDGER },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        SanctuaryTab.SETTINGS -> {
                            SettingsScreen(
                                isLight = isLight,
                                onToggleTheme = onToggleTheme,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
