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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.feedback.SanctuaryFeedbackHelper
import com.v07.v3.ui.compose.SanctuaryPalette
import com.v07.v3.ui.compose.SanctuaryTheme

/**
 * Impeccable Jetpack Compose Configuration & Preview Activity
 * for V07 Sanctuary Home Screen Widgets.
 *
 * Supports module switching, live telemetry preview, and dedicated
 * widget-specific API key & endpoint configuration.
 */
class V07WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    data class WidgetModuleItem(
        val id: String,
        val icon: String,
        val title: String,
        val category: String,
        val description: String,
        val t1Val: String, val t1Lbl: String, val t1Color: Color,
        val t2Val: String, val t2Lbl: String, val t2Color: Color,
        val t3Val: String, val t3Lbl: String, val t3Color: Color,
        val t4Val: String, val t4Lbl: String, val t4Color: Color,
        val statusText: String
    )

    private val moduleCatalog = listOf(
        WidgetModuleItem(
            id = "weekly",
            icon = "⚡",
            title = "WEEKLY DIGEST",
            category = "SYNTHESIS & CADENCE",
            description = "Rollup of weekly focus hours, completed tasks, ritual consistency & screen time.",
            t1Val = "18.4h", t1Lbl = "FOCUS", t1Color = Color(0xFF2DD4BF),
            t2Val = "24", t2Lbl = "TASKS", t2Color = Color(0xFF4DE086),
            t3Val = "88%", t3Lbl = "RITUALS", t3Color = Color(0xFF68A6FF),
            t4Val = "14.2h", t4Lbl = "SCREEN", t4Color = Color(0xFFFFB84D),
            statusText = "Today: 3.2h focus · 6 tasks done · 5/6 rituals"
        ),
        WidgetModuleItem(
            id = "tasks",
            icon = "📋",
            title = "TASKS & PRIORITIES",
            category = "EXECUTION ENGINE",
            description = "High-priority directives, pending obligations, and daily execution velocity.",
            t1Val = "4", t1Lbl = "PENDING", t1Color = Color(0xFF68A6FF),
            t2Val = "2", t2Lbl = "HIGH PRIO", t2Color = Color(0xFFFF5370),
            t3Val = "8", t3Lbl = "DONE", t3Color = Color(0xFF4DE086),
            t4Val = "80%", t4Lbl = "RATE", t4Color = Color(0xFFFFB84D),
            statusText = "Top: Review architecture & deployment"
        ),
        WidgetModuleItem(
            id = "timetracker",
            icon = "⏱️",
            title = "FOCUS SANCTUARY",
            category = "DEEP WORK CADENCE",
            description = "Live focus session status, today's deep work hours, and cadence target.",
            t1Val = "3.2h", t1Lbl = "TODAY", t1Color = Color(0xFF2DD4BF),
            t2Val = "4", t2Lbl = "SESSIONS", t2Color = Color(0xFF68A6FF),
            t3Val = "ACTIVE", t3Lbl = "STATE", t3Color = Color(0xFF4DE086),
            t4Val = "4.0h", t4Lbl = "GOAL", t4Color = Color(0xFFFFB84D),
            statusText = "Focus: Deep Work (35m elapsed) · Tap to resume"
        ),
        WidgetModuleItem(
            id = "habits",
            icon = "🔥",
            title = "HABITS MATRIX",
            category = "DAILY DISCIPLINE",
            description = "Ritual consistency, active day streaks, and daily completion ratios.",
            t1Val = "5/6", t1Lbl = "TODAY", t1Color = Color(0xFFFFB84D),
            t2Val = "7d", t2Lbl = "STREAK", t2Color = Color(0xFF4DE086),
            t3Val = "83%", t3Lbl = "RATE", t3Color = Color(0xFF68A6FF),
            t4Val = "6", t4Lbl = "ACTIVE", t4Color = Color(0xFF2DD4BF),
            statusText = "Streak: 7 days unbroken · 1 ritual pending"
        ),
        WidgetModuleItem(
            id = "gamify",
            icon = "🎮",
            title = "MASTERY & RANKS",
            category = "LEVEL & REPUTATION",
            description = "Gamified progression, experience points, player rank title, and quest status.",
            t1Val = "LVL 5", t1Lbl = "LEVEL", t1Color = Color(0xFF4DE086),
            t2Val = "1,450", t2Lbl = "TOTAL XP", t2Color = Color(0xFFFFB84D),
            t3Val = "VETERAN", t3Lbl = "RANK", t3Color = Color(0xFF68A6FF),
            t4Val = "3/4", t4Lbl = "QUESTS", t4Color = Color(0xFF2DD4BF),
            statusText = "Rank: Veteran · 550 XP to Centurion"
        ),
        WidgetModuleItem(
            id = "vitals",
            icon = "🧠",
            title = "MOOD & ENERGY",
            category = "SANCTUARY WELLNESS",
            description = "Mental equilibrium, mood stability, energy balance, and daily check-ins.",
            t1Val = "4/5", t1Lbl = "MOOD", t1Color = Color(0xFF4DE086),
            t2Val = "4/5", t2Lbl = "ENERGY", t2Color = Color(0xFFFFB84D),
            t3Val = "3", t3Lbl = "CHECKINS", t3Color = Color(0xFF68A6FF),
            t4Val = "88%", t4Lbl = "EQUIL.", t4Color = Color(0xFF2DD4BF),
            statusText = "Latest: Mood Steady · Energy Balanced"
        ),
        WidgetModuleItem(
            id = "reader",
            icon = "📖",
            title = "SANCTUARY LIBRARY",
            category = "KNOWLEDGE & BOOKS",
            description = "Active book shelves, daily reading pages, and finished literary collection.",
            t1Val = "ACTIVE", t1Lbl = "READING", t1Color = Color(0xFF2DD4BF),
            t2Val = "12", t2Lbl = "SHELVES", t2Color = Color(0xFF68A6FF),
            t3Val = "24p", t3Lbl = "TODAY", t3Color = Color(0xFFFFB84D),
            t4Val = "5", t4Lbl = "FINISHED", t4Color = Color(0xFF4DE086),
            statusText = "Active: Clean Architecture · 24 pages read today"
        ),
        WidgetModuleItem(
            id = "run",
            icon = "🏃",
            title = "RUN & ATHLETICS",
            category = "ATHLETIC TELEMETRY",
            description = "Workout cadence, total kilometers run, pushup tally, and latest session pace.",
            t1Val = "12.4k", t1Lbl = "TOTAL KM", t1Color = Color(0xFF4DE086),
            t2Val = "120", t2Lbl = "PUSHUPS", t2Color = Color(0xFFFFB84D),
            t3Val = "4", t3Lbl = "WORKOUTS", t3Color = Color(0xFF68A6FF),
            t4Val = "3.4k", t4Lbl = "LAST RUN", t4Color = Color(0xFF2DD4BF),
            statusText = "Total 12.4k run · 120 pushups logged"
        ),
        WidgetModuleItem(
            id = "daytracker",
            icon = "📅",
            title = "DAY MILESTONES",
            category = "TIMELINE & DATES",
            description = "Milestone countdowns, pinned events, and upcoming date targets.",
            t1Val = "TODAY", t1Lbl = "DATE", t1Color = Color(0xFF68A6FF),
            t2Val = "4", t2Lbl = "PINNED", t2Color = Color(0xFFFFB84D),
            t3Val = "3", t3Lbl = "UPCOMING", t3Color = Color(0xFF4DE086),
            t4Val = "12d", t4Lbl = "COUNT", t4Color = Color(0xFF2DD4BF),
            statusText = "Milestones & countdowns active"
        ),
        WidgetModuleItem(
            id = "checklist",
            icon = "📝",
            title = "DAILY CHECKLIST",
            category = "ACTION CHECKLIST",
            description = "High-cadence checklist of urgent items to check off today.",
            t1Val = "6/8", t1Lbl = "DONE", t1Color = Color(0xFF4DE086),
            t2Val = "2", t2Lbl = "REMAINING", t2Color = Color(0xFFFF5370),
            t3Val = "75%", t3Lbl = "RATE", t3Color = Color(0xFF68A6FF),
            t4Val = "TODAY", t4Lbl = "TARGET", t4Color = Color(0xFFFFB84D),
            statusText = "6 of 8 items checked · Tap to open checklist"
        ),
        WidgetModuleItem(
            id = "gita",
            icon = "🕉️",
            title = "BHAGAVAD GITA",
            category = "SACRED INSIGHT",
            description = "Timeless Sanskrit verse reflections, Karma Yoga principles, and daily peace.",
            t1Val = "2.47", t1Lbl = "VERSE", t1Color = Color(0xFFFFB84D),
            t2Val = "KARMA", t2Lbl = "PATH", t2Color = Color(0xFF2DD4BF),
            t3Val = "ACTIVE", t3Lbl = "INSIGHT", t3Color = Color(0xFF4DE086),
            t4Val = "DEEP", t4Lbl = "PEACE", t4Color = Color(0xFF68A6FF),
            statusText = "Karmanye Vadhikaraste Ma Phaleshu Kadachana"
        )
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

        val initialApiKey = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getString("widget_api_key_$appWidgetId", "") ?: ""
        } else {
            prefs.getString("default_widget_api_key", "") ?: ""
        }

        val initialApiEndpoint = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            prefs.getString("widget_api_endpoint_$appWidgetId", "") ?: ""
        } else {
            prefs.getString("default_widget_api_endpoint", "") ?: ""
        }

        setContent {
            SanctuaryTheme(darkTheme = true) {
                WidgetConfigScreen(
                    initialType = initialType,
                    initialApiKey = initialApiKey,
                    initialApiEndpoint = initialApiEndpoint,
                    onApply = { chosenType, chosenApiKey, chosenApiEndpoint ->
                        commitWidgetSettings(chosenType, chosenApiKey, chosenApiEndpoint)
                    },
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun WidgetConfigScreen(
        initialType: String,
        initialApiKey: String,
        initialApiEndpoint: String,
        onApply: (type: String, apiKey: String, endpoint: String) -> Unit,
        onDismiss: () -> Unit
    ) {
        var selectedType by remember { mutableStateOf(initialType) }
        var widgetApiKeyInput by remember { mutableStateOf(initialApiKey) }
        var widgetApiEndpointInput by remember { mutableStateOf(initialApiEndpoint) }
        var isApiKeyVisible by remember { mutableStateOf(false) }

        val activeModule = remember(selectedType) {
            moduleCatalog.find { it.id.equals(selectedType, ignoreCase = true) }
                ?: moduleCatalog.first()
        }

        Scaffold(
            containerColor = SanctuaryPalette.DarkCanvas,
            topBar = {
                Surface(
                    color = SanctuaryPalette.DarkCanvas,
                    border = BorderStroke(0.dp, Color.Transparent),
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
                                        .background(SanctuaryPalette.PrimaryTeal)
                                )
                                Text(
                                    text = "WIDGET ARCHITECTURE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = SanctuaryPalette.PrimaryTeal,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Text(
                                text = "Sanctuary Widget",
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
                                onApply(selectedType, widgetApiKeyInput, widgetApiEndpointInput)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SanctuaryPalette.PrimaryTeal
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "APPLY WIDGET TO HOME SCREEN",
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // --- 1. Live Interactive Preview ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE TELEMETRY PREVIEW",
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
                                color = SanctuaryPalette.PrimaryTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "4x2 HOME TILE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SanctuaryPalette.PrimaryTeal
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Preview Box Matching widget_dashboard.xml
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SanctuaryPalette.DarkCard,
                            border = BorderStroke(1.dp, SanctuaryPalette.DarkBorder),
                            shadowElevation = 4.dp,
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
                                        Text(text = activeModule.icon, fontSize = 16.sp)
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
                                    Text(
                                        text = activeModule.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.5.sp,
                                            color = SanctuaryPalette.DarkTextMuted
                                        )
                                    )
                                }

                                // 4-Tile Stat Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StatTile(activeModule.t1Val, activeModule.t1Lbl, activeModule.t1Color, Modifier.weight(1f))
                                    StatTile(activeModule.t2Val, activeModule.t2Lbl, activeModule.t2Color, Modifier.weight(1f))
                                    StatTile(activeModule.t3Val, activeModule.t3Lbl, activeModule.t3Color, Modifier.weight(1f))
                                    StatTile(activeModule.t4Val, activeModule.t4Lbl, activeModule.t4Color, Modifier.weight(1f))
                                }

                                // Status Footer
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

                // --- 2. Widget-Specific API Key & Endpoint Section ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WIDGET INTELLIGENCE & API KEY",
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
                                color = if (widgetApiKeyInput.isNotBlank()) Color(0xFF10B981).copy(alpha = 0.15f) else SanctuaryPalette.DarkCardAlt
                            ) {
                                Text(
                                    text = if (widgetApiKeyInput.isNotBlank()) "OVERRIDE ACTIVE" else "INHERITING GLOBAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (widgetApiKeyInput.isNotBlank()) Color(0xFF10B981) else SanctuaryPalette.DarkTextMuted
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SanctuaryPalette.DarkCard,
                            border = BorderStroke(1.dp, SanctuaryPalette.DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Key,
                                        contentDescription = null,
                                        tint = SanctuaryPalette.PrimaryTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "SPECIFIC API KEY & ENDPOINT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = SanctuaryPalette.DarkTextPrimary
                                        )
                                    )
                                }

                                Text(
                                    text = "Assign a dedicated API key and endpoint for home screen widgets and background data processing, overriding the global sanctuary key.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SanctuaryPalette.DarkTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                )

                                // Specific API Endpoint
                                OutlinedTextField(
                                    value = widgetApiEndpointInput,
                                    onValueChange = { widgetApiEndpointInput = it },
                                    label = { Text("Widget Endpoint / Point (Optional)") },
                                    placeholder = { Text("https://generativelanguage.googleapis.com") },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SanctuaryPalette.PrimaryTeal,
                                        unfocusedBorderColor = SanctuaryPalette.DarkBorder,
                                        focusedLabelColor = SanctuaryPalette.PrimaryTeal
                                    )
                                )

                                // Specific API Key
                                OutlinedTextField(
                                    value = widgetApiKeyInput,
                                    onValueChange = { widgetApiKeyInput = it },
                                    label = { Text("Widget Specific API Key (Optional)") },
                                    placeholder = { Text("Paste dedicated API key (AIza..., sk-...)") },
                                    visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                            Icon(
                                                imageVector = if (isApiKeyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                contentDescription = "Toggle Visibility",
                                                tint = SanctuaryPalette.DarkTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SanctuaryPalette.PrimaryTeal,
                                        unfocusedBorderColor = SanctuaryPalette.DarkBorder,
                                        focusedLabelColor = SanctuaryPalette.PrimaryTeal
                                    )
                                )
                            }
                        }
                    }
                }

                // --- 3. Module Selector List ---
                item {
                    Text(
                        text = "SELECT WIDGET MODULE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SanctuaryPalette.DarkTextSecondary,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(moduleCatalog, key = { it.id }) { module ->
                    val isSelected = selectedType.equals(module.id, ignoreCase = true)
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) SanctuaryPalette.PrimaryTeal else SanctuaryPalette.DarkBorder,
                        label = "moduleBorder"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) SanctuaryPalette.PrimaryTeal.copy(alpha = 0.08f) else SanctuaryPalette.DarkCard,
                        label = "moduleBg"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SanctuaryFeedbackHelper.performClick(this@V07WidgetConfigActivity)
                                selectedType = module.id
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SanctuaryPalette.PrimaryTeal.copy(alpha = 0.2f) else SanctuaryPalette.DarkCardAlt),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = module.icon, fontSize = 18.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = module.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = SanctuaryPalette.DarkTextPrimary
                                        )
                                    )
                                    Text(
                                        text = "· ${module.category}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = SanctuaryPalette.DarkTextMuted
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = module.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SanctuaryPalette.DarkTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 5.dp else 1.5.dp,
                                        color = if (isSelected) SanctuaryPalette.PrimaryTeal else SanctuaryPalette.DarkBorder,
                                        shape = CircleShape
                                    )
                                    .background(if (isSelected) SanctuaryPalette.PrimaryTeal else Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun StatTile(valText: String, lblText: String, color: Color, modifier: Modifier) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SanctuaryPalette.DarkCardAlt,
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

    private fun commitWidgetSettings(type: String, apiKey: String, apiEndpoint: String) {
        val prefs = getSharedPreferences("v07_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("default_widget_type", type)
        editor.putString("default_widget_api_key", apiKey.trim())
        editor.putString("default_widget_api_endpoint", apiEndpoint.trim())

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            editor.putString("widget_type_$appWidgetId", type)
            editor.putString("widget_api_key_$appWidgetId", apiKey.trim())
            editor.putString("widget_api_endpoint_$appWidgetId", apiEndpoint.trim())
            editor.apply()

            val mgr = AppWidgetManager.getInstance(this)
            V07DashboardWidget.updateWidget(this, mgr, appWidgetId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            Toast.makeText(this, "Sanctuary Widget updated with dedicated preferences", Toast.LENGTH_SHORT).show()
        } else {
            editor.apply()
            setResult(RESULT_OK)

            val mgr = AppWidgetManager.getInstance(this)
            val allIds = mgr.getAppWidgetIds(ComponentName(this, V07DashboardWidget::class.java))
            for (id in allIds) {
                editor.putString("widget_type_$id", type).apply()
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
            Toast.makeText(this, "Default Sanctuary Widget configured", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
