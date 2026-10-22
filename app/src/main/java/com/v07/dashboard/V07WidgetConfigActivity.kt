package com.v07.dashboard

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.feedback.SanctuaryFeedbackHelper
import com.v07.v3.ui.compose.SanctuaryPalette
import com.v07.v3.ui.compose.SanctuaryTheme

/**
 * Deep Widget Customization & Configuration Activity for V07 Sanctuary Widgets.
 *
 * Provides extensive personalization:
 * 1. Module / Telemetry Source (11 Modules)
 * 2. Accent Color Themes (Teal, Emerald, Sky, Amber, Purple, Rose, Slate)
 * 3. Background Opacity & Glass Surfaces (Solid, Frosted 80%, Subtle 40%, Transparent)
 * 4. Display Elements Toggles (Footer Status, Category Tag, Leading Icon)
 * 5. Widget Tap Action Routing (Direct Module, Home Cockpit, Reconfigure)
 */
class V07WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    data class WidgetModuleItem(
        val id: String,
        val icon: String,
        val title: String,
        val category: String,
        val description: String,
        val t1Val: String, val t1Lbl: String,
        val t2Val: String, val t2Lbl: String,
        val t3Val: String, val t3Lbl: String,
        val t4Val: String, val t4Lbl: String,
        val statusText: String
    )

    data class AccentOption(
        val name: String,
        val hex: String,
        val color: Color
    )

    data class OpacityOption(
        val name: String,
        val alpha: Int,
        val description: String
    )

    private val moduleCatalog = listOf(
        WidgetModuleItem(
            id = "weekly",
            icon = "⚡",
            title = "WEEKLY DIGEST",
            category = "SYNTHESIS & CADENCE",
            description = "Rollup of weekly focus hours, completed tasks, ritual consistency & screen time.",
            t1Val = "18.4h", t1Lbl = "FOCUS",
            t2Val = "24", t2Lbl = "TASKS",
            t3Val = "88%", t3Lbl = "RITUALS",
            t4Val = "14.2h", t4Lbl = "SCREEN",
            statusText = "Today: 3.2h focus · 6 tasks done · 5/6 rituals"
        ),
        WidgetModuleItem(
            id = "tasks",
            icon = "📋",
            title = "TASKS & PRIORITIES",
            category = "EXECUTION ENGINE",
            description = "High-priority directives, pending obligations, and daily execution velocity.",
            t1Val = "4", t1Lbl = "PENDING",
            t2Val = "2", t2Lbl = "HIGH PRIO",
            t3Val = "8", t3Lbl = "DONE",
            t4Val = "80%", t4Lbl = "RATE",
            statusText = "Top: Review architecture & deployment"
        ),
        WidgetModuleItem(
            id = "timetracker",
            icon = "⏱️",
            title = "FOCUS SANCTUARY",
            category = "DEEP WORK CADENCE",
            description = "Live focus session status, today's deep work hours, and cadence target.",
            t1Val = "3.2h", t1Lbl = "TODAY",
            t2Val = "4", t2Lbl = "SESSIONS",
            t3Val = "ACTIVE", t3Lbl = "STATE",
            t4Val = "4.0h", t4Lbl = "GOAL",
            statusText = "Focus: Deep Work (35m elapsed) · Tap to resume"
        ),
        WidgetModuleItem(
            id = "habits",
            icon = "🔥",
            title = "HABITS MATRIX",
            category = "DAILY DISCIPLINE",
            description = "Ritual consistency, active day streaks, and daily completion ratios.",
            t1Val = "5/6", t1Lbl = "TODAY",
            t2Val = "7d", t2Lbl = "STREAK",
            t3Val = "83%", t3Lbl = "RATE",
            t4Val = "6", t4Lbl = "ACTIVE",
            statusText = "Streak: 7 days unbroken · 1 ritual pending"
        ),
        WidgetModuleItem(
            id = "gamify",
            icon = "🎮",
            title = "MASTERY & RANKS",
            category = "LEVEL & REPUTATION",
            description = "Gamified progression, experience points, player rank title, and quest status.",
            t1Val = "LVL 5", t1Lbl = "LEVEL",
            t2Val = "1,450", t2Lbl = "TOTAL XP",
            t3Val = "VETERAN", t3Lbl = "RANK",
            t4Val = "3/4", t4Lbl = "QUESTS",
            statusText = "Rank: Veteran · 550 XP to Centurion"
        ),
        WidgetModuleItem(
            id = "vitals",
            icon = "🧠",
            title = "MOOD & ENERGY",
            category = "SANCTUARY WELLNESS",
            description = "Mental equilibrium, mood stability, energy balance, and daily check-ins.",
            t1Val = "4/5", t1Lbl = "MOOD",
            t2Val = "4/5", t2Lbl = "ENERGY",
            t3Val = "3", t3Lbl = "CHECKINS",
            t4Val = "88%", t4Lbl = "EQUIL.",
            statusText = "Latest: Mood Steady · Energy Balanced"
        ),
        WidgetModuleItem(
            id = "reader",
            icon = "📖",
            title = "SANCTUARY LIBRARY",
            category = "KNOWLEDGE & BOOKS",
            description = "Active book shelves, daily reading pages, and finished literary collection.",
            t1Val = "ACTIVE", t1Lbl = "READING",
            t2Val = "12", t2Lbl = "SHELVES",
            t3Val = "24p", t3Lbl = "TODAY",
            t4Val = "5", t4Lbl = "FINISHED",
            statusText = "Active: Clean Architecture · 24 pages read today"
        ),
        WidgetModuleItem(
            id = "run",
            icon = "🏃",
            title = "RUN & ATHLETICS",
            category = "ATHLETIC TELEMETRY",
            description = "Workout cadence, total kilometers run, pushup tally, and latest session pace.",
            t1Val = "12.4k", t1Lbl = "TOTAL KM",
            t2Val = "120", t2Lbl = "PUSHUPS",
            t3Val = "4", t3Lbl = "WORKOUTS",
            t4Val = "3.4k", t4Lbl = "LAST RUN",
            statusText = "Total 12.4k run · 120 pushups logged"
        ),
        WidgetModuleItem(
            id = "daytracker",
            icon = "📅",
            title = "DAY MILESTONES",
            category = "TIMELINE & DATES",
            description = "Milestone countdowns, pinned events, and upcoming date targets.",
            t1Val = "TODAY", t1Lbl = "DATE",
            t2Val = "4", t2Lbl = "PINNED",
            t3Val = "3", t3Lbl = "UPCOMING",
            t4Val = "12d", t4Lbl = "COUNT",
            statusText = "Milestones & countdowns active"
        ),
        WidgetModuleItem(
            id = "checklist",
            icon = "📝",
            title = "DAILY CHECKLIST",
            category = "ACTION CHECKS",
            description = "High-cadence checklist of urgent items to check off today.",
            t1Val = "6/8", t1Lbl = "DONE",
            t2Val = "2", t2Lbl = "REMAINING",
            t3Val = "75%", t3Lbl = "RATE",
            t4Val = "TODAY", t4Lbl = "TARGET",
            statusText = "6 of 8 items checked · Tap to open checklist"
        ),
        WidgetModuleItem(
            id = "gita",
            icon = "🕉️",
            title = "BHAGAVAD GITA",
            category = "SACRED INSIGHT",
            description = "Timeless Sanskrit verse reflections, Karma Yoga principles, and daily peace.",
            t1Val = "2.47", t1Lbl = "VERSE",
            t2Val = "KARMA", t2Lbl = "PATH",
            t3Val = "ACTIVE", t3Lbl = "INSIGHT",
            t4Val = "DEEP", t4Lbl = "PEACE",
            statusText = "Karmanye Vadhikaraste Ma Phaleshu Kadachana"
        )
    )

    private val accentOptions = listOf(
        AccentOption("Sanctuary Teal", "#2DD4BF", Color(0xFF2DD4BF)),
        AccentOption("Emerald Green", "#10B981", Color(0xFF10B981)),
        AccentOption("Ocean Sky", "#38BDF8", Color(0xFF38BDF8)),
        AccentOption("Cyber Amber", "#F59E0B", Color(0xFFF59E0B)),
        AccentOption("Royal Purple", "#A78BFA", Color(0xFFA78BFA)),
        AccentOption("Coral Rose", "#F43F5E", Color(0xFFF43F5E)),
        AccentOption("Crisp Slate", "#E2E8F0", Color(0xFFE2E8F0))
    )

    private val opacityOptions = listOf(
        OpacityOption("Solid Dark", 255, "100% Opaque (#0E1217)"),
        OpacityOption("Frosted Glass", 204, "80% Translucent Surface"),
        OpacityOption("Subtle Tint", 102, "40% Smoked Glass"),
        OpacityOption("Pure Transparent", 0, "See-Through Floating Text")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val prefs = getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)

        val initialType = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getString("widget_type_$appWidgetId", null)
                ?: prefs.getString("default_widget_type", "weekly") ?: "weekly"
        } else {
            prefs.getString("default_widget_type", "weekly") ?: "weekly"
        }

        val initialAccent = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getString("widget_accent_$appWidgetId", null)
                ?: prefs.getString("default_widget_accent", "#2DD4BF") ?: "#2DD4BF"
        } else {
            prefs.getString("default_widget_accent", "#2DD4BF") ?: "#2DD4BF"
        }

        val initialAlpha = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getInt("widget_bg_alpha_$appWidgetId", prefs.getInt("default_widget_bg_alpha", 255))
        } else {
            prefs.getInt("default_widget_bg_alpha", 255)
        }

        val initialShowFooter = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getBoolean("widget_show_footer_$appWidgetId", prefs.getBoolean("default_widget_show_footer", true))
        } else {
            prefs.getBoolean("default_widget_show_footer", true)
        }

        val initialShowSub = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getBoolean("widget_show_sub_$appWidgetId", prefs.getBoolean("default_widget_show_sub", true))
        } else {
            prefs.getBoolean("default_widget_show_sub", true)
        }

        val initialShowIcon = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getBoolean("widget_show_icon_$appWidgetId", prefs.getBoolean("default_widget_show_icon", true))
        } else {
            prefs.getBoolean("default_widget_show_icon", true)
        }

        val initialTapAction = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getString("widget_tap_action_$appWidgetId", prefs.getString("default_widget_tap_action", "OPEN_MODULE")) ?: "OPEN_MODULE"
        } else {
            prefs.getString("default_widget_tap_action", "OPEN_MODULE") ?: "OPEN_MODULE"
        }

        setContent {
            SanctuaryTheme(darkTheme = true) {
                WidgetConfigScreen(
                    initialType = initialType,
                    initialAccent = initialAccent,
                    initialAlpha = initialAlpha,
                    initialShowFooter = initialShowFooter,
                    initialShowSub = initialShowSub,
                    initialShowIcon = initialShowIcon,
                    initialTapAction = initialTapAction,
                    onApply = { type, accent, alpha, showFooter, showSub, showIcon, tapAction ->
                        commitCustomizations(type, accent, alpha, showFooter, showSub, showIcon, tapAction)
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }

    @Composable
    private fun WidgetConfigScreen(
        initialType: String,
        initialAccent: String,
        initialAlpha: Int,
        initialShowFooter: Boolean,
        initialShowSub: Boolean,
        initialShowIcon: Boolean,
        initialTapAction: String,
        onApply: (type: String, accent: String, alpha: Int, showFooter: Boolean, showSub: Boolean, showIcon: Boolean, tapAction: String) -> Unit,
        onDismiss: () -> Unit
    ) {
        var selectedType by remember { mutableStateOf(initialType) }
        var selectedAccentHex by remember { mutableStateOf(initialAccent) }
        var selectedAlpha by remember { mutableIntStateOf(initialAlpha) }
        var showFooter by remember { mutableStateOf(initialShowFooter) }
        var showSub by remember { mutableStateOf(initialShowSub) }
        var showIcon by remember { mutableStateOf(initialShowIcon) }
        var selectedTapAction by remember { mutableStateOf(initialTapAction) }

        val activeModule = remember(selectedType) {
            moduleCatalog.find { it.id.equals(selectedType, ignoreCase = true) }
                ?: moduleCatalog.first()
        }

        val activeAccentColor = remember(selectedAccentHex) {
            try {
                Color(android.graphics.Color.parseColor(selectedAccentHex))
            } catch (_: Exception) {
                Color(0xFF2DD4BF)
            }
        }

        Scaffold(
            containerColor = SanctuaryPalette.DarkCanvas,
            topBar = {
                Surface(
                    color = SanctuaryPalette.DarkCanvas,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(activeAccentColor)
                                )
                                Text(
                                    text = "HOME WIDGET STUDIO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = activeAccentColor,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Text(
                                text = "Customize Widget",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SanctuaryPalette.DarkTextPrimary
                                )
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SanctuaryPalette.DarkCardAlt)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = SanctuaryPalette.DarkTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = SanctuaryPalette.DarkCard,
                    border = BorderStroke(1.dp, SanctuaryPalette.DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                SanctuaryFeedbackHelper.performSuccess(this@V07WidgetConfigActivity)
                                onApply(
                                    selectedType,
                                    selectedAccentHex,
                                    selectedAlpha,
                                    showFooter,
                                    showSub,
                                    showIcon,
                                    selectedTapAction
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeAccentColor,
                                contentColor = if (selectedAccentHex == "#E2E8F0") Color(0xFF0F172A) else Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "APPLY WIDGET CUSTOMIZATIONS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text(
                                text = "Dismiss without saving",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SanctuaryPalette.DarkTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // --- 1. Real-Time Dynamic Live Preview ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE WIDGET PREVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = SanctuaryPalette.DarkTextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = activeAccentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "4x2 HOME TILE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeAccentColor
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Widget Card Preview (Reflects chosen opacity, accent color, and toggles)
                        val previewBg = if (selectedAlpha == 0) {
                            Color(0x15FFFFFF) // Subtle outline for transparent preview visibility
                        } else {
                            Color(0xFF0E1217).copy(alpha = selectedAlpha / 255f)
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = previewBg,
                            border = BorderStroke(1.dp, if (selectedAlpha == 0) activeAccentColor.copy(alpha = 0.4f) else SanctuaryPalette.DarkBorder),
                            shadowElevation = if (selectedAlpha > 150) 4.dp else 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (showIcon) {
                                            Text(text = activeModule.icon, fontSize = 16.sp)
                                        }
                                        Text(
                                            text = activeModule.title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = SanctuaryPalette.DarkTextPrimary,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                    if (showSub) {
                                        Text(
                                            text = activeModule.category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.5.sp,
                                                color = SanctuaryPalette.DarkTextMuted
                                            )
                                        )
                                    }
                                }

                                // 4-Tile Stat Grid (Tile 1 uses selected accent color)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CustomStatTile(activeModule.t1Val, activeModule.t1Lbl, activeAccentColor, selectedAlpha, Modifier.weight(1f))
                                    CustomStatTile(activeModule.t2Val, activeModule.t2Lbl, Color(0xFF4DE086), selectedAlpha, Modifier.weight(1f))
                                    CustomStatTile(activeModule.t3Val, activeModule.t3Lbl, Color(0xFF68A6FF), selectedAlpha, Modifier.weight(1f))
                                    CustomStatTile(activeModule.t4Val, activeModule.t4Lbl, Color(0xFFFFB84D), selectedAlpha, Modifier.weight(1f))
                                }

                                // Status Footer
                                if (showFooter) {
                                    Text(
                                        text = activeModule.statusText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = SanctuaryPalette.DarkTextSecondary
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 2. Module Selector (11 Options) ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1. SELECT WIDGET MODULE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = SanctuaryPalette.DarkTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(moduleCatalog, key = { it.id }) { mod ->
                                val isSelected = selectedType.equals(mod.id, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) activeAccentColor.copy(alpha = 0.15f) else SanctuaryPalette.DarkCard,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) activeAccentColor else SanctuaryPalette.DarkBorder),
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clickable {
                                            SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                            selectedType = mod.id
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = mod.icon, fontSize = 20.sp)
                                        Text(
                                            text = mod.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isSelected) activeAccentColor else SanctuaryPalette.DarkTextPrimary,
                                                fontSize = 10.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = mod.category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                color = SanctuaryPalette.DarkTextMuted
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 3. Accent Color Customization ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "2. ACCENT COLOR THEME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = SanctuaryPalette.DarkTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            accentOptions.forEach { opt ->
                                val isSelected = selectedAccentHex.equals(opt.hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else SanctuaryPalette.DarkBorder,
                                            shape = CircleShape
                                        )
                                        .background(opt.color)
                                        .clickable {
                                            SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                            selectedAccentHex = opt.hex
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = if (opt.hex == "#E2E8F0") Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 4. Background Surface & Transparency ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "3. BACKGROUND SURFACE & OPACITY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = SanctuaryPalette.DarkTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            opacityOptions.forEach { opt ->
                                val isSelected = selectedAlpha == opt.alpha
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) activeAccentColor.copy(alpha = 0.12f) else SanctuaryPalette.DarkCard,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) activeAccentColor else SanctuaryPalette.DarkBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                            selectedAlpha = opt.alpha
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = opt.name,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isSelected) activeAccentColor else SanctuaryPalette.DarkTextPrimary
                                                )
                                            )
                                            Text(
                                                text = opt.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    color = SanctuaryPalette.DarkTextSecondary
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (isSelected) 4.5.dp else 1.5.dp,
                                                    color = if (isSelected) activeAccentColor else SanctuaryPalette.DarkBorder,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 5. Element Visibility Toggles ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "4. DISPLAY ELEMENTS & VISIBILITY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = SanctuaryPalette.DarkTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SanctuaryPalette.DarkCard,
                            border = BorderStroke(1.dp, SanctuaryPalette.DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                // Toggle: Footer Status
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Status Footer Bar",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = SanctuaryPalette.DarkTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Show contextual telemetry summary at the bottom of widget.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = SanctuaryPalette.DarkTextSecondary
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = showFooter,
                                        onCheckedChange = {
                                            SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                            showFooter = it
                                        }
                                    )
                                }

                                Divider(color = SanctuaryPalette.DarkBorder, thickness = 1.dp)

                                // Toggle: Category Subtitle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Category Tag & Subtitle",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = SanctuaryPalette.DarkTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Show domain tag (e.g. 'V07 · EXECUTION') in the top right.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = SanctuaryPalette.DarkTextSecondary
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = showSub,
                                        onCheckedChange = {
                                            SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                            showSub = it
                                        }
                                    )
                                }

                                Divider(color = SanctuaryPalette.DarkBorder, thickness = 1.dp)

                                // Toggle: Leading Icon
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Leading Module Icon",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = SanctuaryPalette.DarkTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Display the module emoji glyph next to the title.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = SanctuaryPalette.DarkTextSecondary
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = showIcon,
                                        onCheckedChange = {
                                            SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                            showIcon = it
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 6. Widget Tap Action Routing ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "5. ON-TAP INTERACTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = SanctuaryPalette.DarkTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )

                        val tapOptions = listOf(
                            Triple("OPEN_MODULE", "Open Dedicated Module Cockpit", "Tapping widget opens straight into this specific plugin"),
                            Triple("OPEN_HOME", "Open Main Sanctuary Cockpit", "Tapping widget brings up the full overview dashboard"),
                            Triple("OPEN_CONFIG", "Reconfigure Widget Settings", "Tapping widget opens this customization screen directly")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            tapOptions.forEach { (actionKey, label, desc) ->
                                val isSelected = selectedTapAction == actionKey
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) activeAccentColor.copy(alpha = 0.12f) else SanctuaryPalette.DarkCard,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) activeAccentColor else SanctuaryPalette.DarkBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                            selectedTapAction = actionKey
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isSelected) activeAccentColor else SanctuaryPalette.DarkTextPrimary
                                                )
                                            )
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    color = SanctuaryPalette.DarkTextSecondary
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (isSelected) 4.5.dp else 1.5.dp,
                                                    color = if (isSelected) activeAccentColor else SanctuaryPalette.DarkBorder,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CustomStatTile(valText: String, lblText: String, color: Color, alpha: Int, modifier: Modifier) {
        val tileBg = if (alpha == 0) Color(0x10FFFFFF) else SanctuaryPalette.DarkCardAlt.copy(alpha = (alpha / 255f).coerceAtLeast(0.2f))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = tileBg,
            border = BorderStroke(1.dp, SanctuaryPalette.DarkBorder.copy(alpha = (alpha / 255f).coerceAtLeast(0.3f))),
            modifier = modifier.height(52.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = valText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontSize = 13.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = lblText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = SanctuaryPalette.DarkTextMuted,
                        fontSize = 8.sp,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }

    private fun commitCustomizations(
        type: String,
        accent: String,
        alpha: Int,
        showFooter: Boolean,
        showSub: Boolean,
        showIcon: Boolean,
        tapAction: String
    ) {
        val prefs = getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("default_widget_type", type)
        editor.putString("default_widget_accent", accent)
        editor.putInt("default_widget_bg_alpha", alpha)
        editor.putBoolean("default_widget_show_footer", showFooter)
        editor.putBoolean("default_widget_show_sub", showSub)
        editor.putBoolean("default_widget_show_icon", showIcon)
        editor.putString("default_widget_tap_action", tapAction)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            editor.putString("widget_type_$appWidgetId", type)
            editor.putString("widget_accent_$appWidgetId", accent)
            editor.putInt("widget_bg_alpha_$appWidgetId", alpha)
            editor.putBoolean("widget_show_footer_$appWidgetId", showFooter)
            editor.putBoolean("widget_show_sub_$appWidgetId", showSub)
            editor.putBoolean("widget_show_icon_$appWidgetId", showIcon)
            editor.putString("widget_tap_action_$appWidgetId", tapAction)
            editor.apply()

            val mgr = AppWidgetManager.getInstance(this)
            V07DashboardWidget.updateWidget(this, mgr, appWidgetId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            Toast.makeText(this, "Sanctuary Widget customized successfully", Toast.LENGTH_SHORT).show()
        } else {
            editor.apply()
            setResult(RESULT_OK)

            val mgr = AppWidgetManager.getInstance(this)
            val allIds = mgr.getAppWidgetIds(ComponentName(this, V07DashboardWidget::class.java))
            for (id in allIds) {
                editor.putString("widget_type_$id", type)
                editor.putString("widget_accent_$id", accent)
                editor.putInt("widget_bg_alpha_$id", alpha)
                editor.putBoolean("widget_show_footer_$id", showFooter)
                editor.putBoolean("widget_show_sub_$id", showSub)
                editor.putBoolean("widget_show_icon_$id", showIcon)
                editor.putString("widget_tap_action_$id", tapAction)
                editor.apply()
                V07DashboardWidget.updateWidget(this, mgr, id)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mgr.isRequestPinAppWidgetSupported) {
                val componentName = ComponentName(this, V07DashboardWidget::class.java)
                val successIntent = Intent(this, V07DashboardWidget::class.java).apply {
                    action = "com.v07.dashboard.PINNED_WIDGET_CONFIG"
                    putExtra("preferred_widget_type", type)
                }
                val successCallback = android.app.PendingIntent.getBroadcast(
                    this, 0, successIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
                )
                mgr.requestPinAppWidget(componentName, null, successCallback)
            }
            Toast.makeText(this, "Default Sanctuary Widget settings saved", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
