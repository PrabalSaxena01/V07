package com.v07.v3.ui.compose

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.timer.ActiveSessionState
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.app.timer.TimerStatus
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

enum class FocusPreset(val label: String, val minutes: Int, val mode: TimerMode) {
    P15("15m", 15, TimerMode.COUNTDOWN),
    P25("25m", 25, TimerMode.COUNTDOWN),
    P45("45m", 45, TimerMode.COUNTDOWN),
    OPEN("Open", 0, TimerMode.STOPWATCH)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSanctuaryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { EventRepository.getInstance(context) }
    val timerManager = remember { TimerManager.getInstance(context, repository) }
    val scope = rememberCoroutineScope()

    val timerState by timerManager.timerState.collectAsState()

    var selectedPreset by remember { mutableStateOf(FocusPreset.P25) }
    var intentionText by remember { mutableStateOf("") }
    var recentSessions by remember { mutableStateOf<List<V3Session>>(emptyList()) }
    var currentDisplayTime by remember { mutableStateOf("25:00") }

    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    fun loadSessions() {
        scope.launch(Dispatchers.IO) {
            val list = repository.getRecentSessions(20)
            withContext(Dispatchers.Main) {
                recentSessions = list
            }
        }
    }

    LaunchedEffect(Unit) {
        loadSessions()
    }

    // Timer ticker loop
    LaunchedEffect(timerState.status, timerState.startTimestampMs, timerState.pauseTimestampMs, selectedPreset) {
        while (true) {
            val now = System.currentTimeMillis()
            if (timerState.status == TimerStatus.RUNNING || timerState.status == TimerStatus.PAUSED) {
                val elapsedSec = timerState.getElapsedMs(now) / 1000L
                if (timerState.mode == TimerMode.STOPWATCH) {
                    val hrs = elapsedSec / 3600
                    val mins = (elapsedSec % 3600) / 60
                    val secs = elapsedSec % 60
                    currentDisplayTime = if (hrs > 0) {
                        String.format("%02d:%02d:%02d", hrs, mins, secs)
                    } else {
                        String.format("%02d:%02d", mins, secs)
                    }
                } else {
                    val targetSec = timerState.targetDurationMs / 1000L
                    val remainSec = (targetSec - elapsedSec).coerceAtLeast(0L)
                    val mins = remainSec / 60
                    val secs = remainSec % 60
                    currentDisplayTime = String.format("%02d:%02d", mins, secs)
                }
            } else {
                // IDLE
                currentDisplayTime = if (selectedPreset.mode == TimerMode.STOPWATCH) "00:00" else "${selectedPreset.minutes}:00"
            }
            delay(500)
        }
    }

    fun handleStart() {
        val topic = if (intentionText.isNotBlank()) intentionText.trim() else "Deep Focus"
        val targetMs = selectedPreset.minutes * 60 * 1000L
        timerManager.startSession(
            topic = topic,
            category = "Focus",
            mode = selectedPreset.mode,
            targetMinutes = selectedPreset.minutes
        )
    }

    fun handleTogglePause() {
        if (timerState.status == TimerStatus.RUNNING) {
            timerManager.pauseSession()
        } else if (timerState.status == TimerStatus.PAUSED) {
            timerManager.resumeSession()
        }
    }

    fun handleStopAndLog() {
        val session = timerManager.finishSession(finalNotes = intentionText)
        if (session != null) {
            Toast.makeText(context, "Logged ${session.durationSec / 60}m focus", Toast.LENGTH_SHORT).show()
        }
        intentionText = ""
        loadSessions()
    }

    fun handleReset() {
        timerManager.discardSession()
    }

    val cardShape = RoundedCornerShape(16.dp)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Top Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    Column {
                        Text(
                            text = "Focus Sanctuary",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Quiet intervals for deep presence",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(onClick = { handleReset() }) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 2. Chronometer Hero Card ---
        item {
            Surface(
                shape = cardShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preset chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FocusPreset.values().forEach { preset ->
                                    val isSelected = (timerState.status == TimerStatus.IDLE && selectedPreset == preset) ||
                                            (timerState.status != TimerStatus.IDLE && timerState.mode == preset.mode &&
                                                    (preset.mode == TimerMode.STOPWATCH || timerState.targetDurationMs == preset.minutes * 60 * 1000L))

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.clickable {
                                            if (timerState.status == TimerStatus.IDLE) {
                                                selectedPreset = preset
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = preset.label,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Large Digital Timer display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = currentDisplayTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 56.sp,
                                letterSpacing = 2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (timerState.mode == TimerMode.STOPWATCH) "Stopwatch (0 -> ∞)" else "Deep Interval",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    // Intention input
                    OutlinedTextField(
                        value = intentionText,
                        onValueChange = { intentionText = it },
                        placeholder = {
                            Text(
                                text = "What is your quiet priority for this session?",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Control buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (timerState.status == TimerStatus.IDLE) {
                            Button(
                                onClick = { handleStart() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Start Focus", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { handleTogglePause() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (timerState.status == TimerStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (timerState.status == TimerStatus.RUNNING) "Pause" else "Resume",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Button(
                                onClick = { handleStopAndLog() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Stop & Log", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Recent Sessions History ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "RECENT INTERVALS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                if (recentSessions.isEmpty()) {
                    Surface(
                        shape = cardShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No intervals recorded yet today.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                } else {
                    recentSessions.forEach { session ->
                        val durationMins = (session.durationSec / 60).coerceAtLeast(1)
                        val startTimeStr = sdfTime.format(Date(session.startTime))

                        Surface(
                            shape = cardShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Timer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = session.activity.ifBlank { "Deep Focus" },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "Started at $startTimeStr",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                Text(
                                    text = "${durationMins}m",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
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
