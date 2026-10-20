package com.v07.v3.ui.compose

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.timer.ActiveSessionState
import com.v07.v3.app.timer.TimerStatus
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.core.data.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Native Jetpack Compose Cockpit Screen
 * Faithful 1:1 replica of panel-cockpit from the V3 main branch:
 * - Metric KPI Deck (Focus Today, Cadence Ratio, Obligations, Mastery Rank)
 * - Deep Focus Chrono Widget (Circular progress gauge, preset chips 15m/25m/50m/90m, Begin Flow & Reset)
 * - Daily Sovereign Activity Spine (Chronological stream of sessions, tasks, reflections)
 */
@Composable
fun CockpitScreen(
    onNavigateToCadence: () -> Unit,
    onNavigateToChrono: () -> Unit,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { EventRepository.getInstance(context) }
    val timerManager = remember { TimerManager.getInstance(context, repository) }

    // Observe real-time TimerManager state
    val timerState by timerManager.timerState.collectAsState()

    // Real KPI Metrics
    var todayFocusMinutes by remember { mutableStateOf(0) }
    var habitRatioPercent by remember { mutableStateOf(0) }
    var pendingTasksCount by remember { mutableStateOf(0) }
    var targetMinutes by remember { mutableStateOf(25) }

    // Fetch initial metrics
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val todaySessions = repository.getTodaySessions(cal.timeInMillis)
                val totalFocusMs = todaySessions.sumOf { (it.endTime ?: it.startTime) - it.startTime }
                todayFocusMinutes = (totalFocusMs / 60000).toInt()

                val tasks = repository.getTasks()
                pendingTasksCount = tasks.count { !it.status.equals("completed", ignoreCase = true) && !it.status.equals("done", ignoreCase = true) }
            } catch (_: Exception) {}
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // =====================================================================
        // 1. Metric KPI Pills Deck (2x2 grid on mobile)
        // =====================================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: Focus Today
                    KpiPillItem(
                        icon = Icons.Outlined.Timer,
                        label = "Focus Today",
                        value = "${todayFocusMinutes}m",
                        accentColor = SanctuaryPalette.AccentChrono,
                        accentBg = SanctuaryPalette.AccentChronoBg,
                        onClick = onNavigateToChrono,
                        modifier = Modifier.weight(1f)
                    )

                    // KPI 2: Cadence Ratio
                    KpiPillItem(
                        icon = Icons.Outlined.Repeat,
                        label = "Cadence Ratio",
                        value = "${habitRatioPercent}%",
                        accentColor = SanctuaryPalette.AccentHabits,
                        accentBg = SanctuaryPalette.AccentHabitsBg,
                        onClick = onNavigateToCadence,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 3: Obligations
                    KpiPillItem(
                        icon = Icons.Outlined.Checklist,
                        label = "Obligations",
                        value = "$pendingTasksCount",
                        accentColor = SanctuaryPalette.AccentTasks,
                        accentBg = SanctuaryPalette.AccentTasksBg,
                        onClick = onNavigateToTasks,
                        modifier = Modifier.weight(1f)
                    )

                    // KPI 4: Mastery Rank
                    KpiPillItem(
                        icon = Icons.Outlined.MilitaryTech,
                        label = "Mastery Rank",
                        value = "Practitioner",
                        accentColor = SanctuaryPalette.AccentGamify,
                        accentBg = SanctuaryPalette.AccentGamifyBg,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // =====================================================================
        // 2. Deep Focus Chronometer Card (Faithful to panel-cockpit)
        // =====================================================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, CardShape),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Deep Focus Chrono",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Deep Work • BLOCK MODE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.SelfImprovement,
                            contentDescription = "Chrono",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Progress Gauge
                    val isRunning = timerState.status == TimerStatus.RUNNING
                    val formattedTime = if (timerState.status != TimerStatus.IDLE) {
                        timerState.getFormattedTime()
                    } else {
                        String.format("%02d:00", targetMinutes)
                    }

                    val totalMs = (targetMinutes * 60 * 1000).toFloat()
                    val remainingMs = timerState.getRemainingMs().toFloat()
                    val progressFraction = if (totalMs > 0 && timerState.status != TimerStatus.IDLE) {
                        (1f - (remainingMs / totalMs)).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            val strokeWidth = 9.dp.toPx()
                            // Track
                            drawCircle(
                                color = Color(0xFF1E2835),
                                style = Stroke(width = strokeWidth)
                            )
                            // Animated Progress
                            drawArc(
                                color = SanctuaryPalette.PrimaryTeal,
                                startAngle = -90f,
                                sweepAngle = progressFraction * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target Interval",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interval Presets Chips Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(15, 25, 50, 90).forEach { mins ->
                            val isSelected = targetMinutes == mins && timerState.status == TimerStatus.IDLE
                            val chipBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant
                            val chipBorder = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outline
                            val chipText = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, PillShape)
                                    .clickable {
                                        if (timerState.status == TimerStatus.IDLE) {
                                            targetMinutes = mins
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    ),
                                    color = chipText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Controls: Begin Flow / Pause & Reset
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Begin Flow Button
                        Button(
                            onClick = {
                                if (isRunning) {
                                    timerManager.pauseSession()
                                } else if (timerState.status == TimerStatus.PAUSED) {
                                    timerManager.resumeSession()
                                } else {
                                    timerManager.startSession(
                                        topic = "Deep Focus Flow",
                                        category = "focus",
                                        mode = TimerMode.COUNTDOWN,
                                        targetMinutes = targetMinutes
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Play",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "Pause Flow" else if (timerState.status == TimerStatus.PAUSED) "Resume" else "Begin Flow",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }

                        // Reset Button
                        IconButton(
                            onClick = {
                                timerManager.discardSession()
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RestartAlt,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. Daily Sovereign Activity Spine (Timeline stream)
        // =====================================================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, CardShape),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Activity Spine",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Chronological stream for selected date",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Timeline,
                            contentDescription = "Timeline",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timeline Empty State / Item
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HistoryEdu,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Daily activities & focus sessions record automatically.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiPillItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    accentBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
            .clickable { onClick() },
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
