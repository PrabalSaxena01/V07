package com.v07.v3.ui.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.V3PlatformHost
import com.v07.v3.app.overlay.V3BubbleOverlayService
import com.v07.v3.core.data.GitaProvider
import com.v07.v3.core.data.V07Database
import com.v07.v3.core.data.V3Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class SanctuaryQuote(
    val quote: String,
    val author: String
)

private val SANCTUARY_QUOTES = listOf(
    SanctuaryQuote(
        "Almost everything will work again if you unplug it for a few minutes, including you.",
        "Anne Lamott"
    ),
    SanctuaryQuote(
        "You have power over your mind, not outside events. Realize this, and you will find strength.",
        "Marcus Aurelius"
    ),
    SanctuaryQuote(
        "Smile, breathe, and go slowly. There is no need to hurry. Have patience with all things.",
        "Thich Nhat Hanh"
    ),
    SanctuaryQuote(
        "Nature does not hurry, yet everything is accomplished.",
        "Lao Tzu"
    ),
    SanctuaryQuote(
        "Within you, there is a stillness and a sanctuary to which you can retreat at any time.",
        "Hermann Hesse"
    ),
    SanctuaryQuote(
        "Adopt the pace of nature: her secret is patience.",
        "Ralph Waldo Emerson"
    ),
    SanctuaryQuote(
        "Silence is not the absence of sound, but the stillness of the mind.",
        "Jiddu Krishnamurti"
    ),
    SanctuaryQuote(
        "In the midst of movement and chaos, keep stillness inside of you.",
        "Deepak Chopra"
    )
)

/**
 * Native Jetpack Compose Sanctuary Home Screen
 * Matches the calm, serene aesthetic with real dynamic telemetry and live persistence:
 * - Editorial greeting dynamically welcoming user based on active profile and time of day
 * - "The morning flow" snapshot card with real screen time, focus time, and habits telemetry
 * - Unhurried steps card with live SQLite V3Task persistence and zero mock data
 * - "Currently reading" companion card with live Bhagavad Gita daily wisdom from gita.json
 * - Serene daily reflection quote card rotating each day
 * - Focus Flow companion card
 */
@Composable
fun HomeScreen(
    onOpenGitaCompanion: () -> Unit,
    onOpenFocusChrono: () -> Unit,
    onOpenScreenTime: () -> Unit = {},
    onOpenHabits: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isOverlayRunning by remember { mutableStateOf(V3BubbleOverlayService.isRunning) }

    val toggleOverlay = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
            } catch (_: Exception) {}
        } else {
            V3BubbleOverlayService.toggle(context)
            isOverlayRunning = !isOverlayRunning
        }
    }


    val daySanctuary = remember {
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        "$dayOfWeek sanctuary"
    }

    // Dynamic greeting based on hour of day & active profile
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val timeGreeting = remember(hour) {
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }


    val profileName = remember {
        val prefs = context.getSharedPreferences("v07_profiles", Context.MODE_PRIVATE)
        val active = prefs.getString("active_profile_id", null) ?: V07Database.getCurrentProfileId()
        if (active.isNotBlank() && active != "guest" && active != "default") {
            active.replace("_", " ").split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: ""
        } else {
            val userPrefs = context.getSharedPreferences("v07_user_prefs", Context.MODE_PRIVATE)
            val custom = userPrefs.getString("display_name", "")?.trim() ?: ""
            custom.split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: ""
        }
    }


    val greetingText = remember(timeGreeting, profileName) {
        if (profileName.isNotBlank()) "$timeGreeting, $profileName" else timeGreeting
    }

    // Dynamic Telemetry Snapshot State
    var screenTimeFormatted by remember { mutableStateOf("0m") }
    var focusedTimeFormatted by remember { mutableStateOf("0 min") }
    var ritualsFormatted by remember { mutableStateOf("0 rituals") }
    var snapshotProgress by remember { mutableFloatStateOf(0.15f) }

    // Live Dynamic Tasks
    var tasks by remember { mutableStateOf<List<V3Task>>(emptyList()) }
    var tasksLoaded by remember { mutableStateOf(false) }

    // Live Dynamic Gita Daily Verse
    var dailyVerseObj by remember { mutableStateOf<JSONObject?>(null) }

    // Rotating Daily Quote
    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val currentQuote = remember(dayOfYear) {
        SANCTUARY_QUOTES[dayOfYear % SANCTUARY_QUOTES.size]
    }

    // Add gentle note dialog
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }

    // Reload tasks helper
    val reloadTasks = {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (V3PlatformHost.isInitialized) {
                    val list = V3PlatformHost.repository.getTasks()
                    withContext(Dispatchers.Main) {
                        tasks = list
                        tasksLoaded = true
                    }
                }
            } catch (e: Exception) {
                Log.w("HomeScreen", "Error loading tasks: ${e.message}")
            }
        }
    }


    // Load initial live dynamic data on mount
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                if (V3PlatformHost.isInitialized) {
                    // 1. Live Screen Time Telemetry
                    val todaySummary = V3PlatformHost.screenTimeTracker.collectAndPersistToday(false)
                    val screenMs = todaySummary.totalDurationMs
                    val screenHours = screenMs / (1000 * 60 * 60)
                    val screenMins = (screenMs / (1000 * 60)) % 60
                    screenTimeFormatted = if (screenHours > 0) "${screenHours}h ${screenMins}m" else "${screenMins}m"

                    // 2. Live Focused Sanctuary Time
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val sessions = V3PlatformHost.repository.getTodaySessions(cal.timeInMillis)
                    val focusSec = sessions.sumOf { it.durationSec }
                    val focusMins = focusSec / 60
                    val focusHours = focusMins / 60
                    focusedTimeFormatted = if (focusHours > 0) "${focusHours}h ${focusMins % 60}m" else "$focusMins min"

                    // 3. Live Habits / Rituals Telemetry
                    val defsStr = V3PlatformHost.repository.getPluginValue("com.v07.habits", "definitions")
                    val logsStr = V3PlatformHost.repository.getPluginValue("com.v07.habits", "logs")
                    var totalHabits = 0
                    var completedToday = 0
                    if (!defsStr.isNullOrBlank()) {
                        try {
                            val dArr = JSONArray(defsStr)
                            totalHabits = dArr.length()
                        } catch (_: Exception) {}
                    }
                    if (!logsStr.isNullOrBlank()) {
                        try {
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val lArr = JSONArray(logsStr)
                            val completedIds = mutableSetOf<String>()
                            for (i in 0 until lArr.length()) {
                                val item = lArr.getJSONObject(i)
                                if (item.optString("date") == todayStr && item.optBoolean("completed", true)) {
                                    completedIds.add(item.optString("habit_id"))
                                }
                            }
                            completedToday = completedIds.size
                        } catch (_: Exception) {}
                    }
                    ritualsFormatted = if (totalHabits > 0) "$completedToday of $totalHabits" else if (completedToday > 0) "$completedToday met" else "0 rituals"

                    // 4. Calculated Progress Indicator
                    snapshotProgress = if (totalHabits > 0) {
                        (completedToday.toFloat() / totalHabits.toFloat()).coerceIn(0f, 1f)
                    } else if (focusMins > 0) {
                        (focusMins.toFloat() / 60f).coerceIn(0f, 1f)
                    } else {
                        0.15f
                    }

                    // 5. Live Tasks from SQLite
                    val list = V3PlatformHost.repository.getTasks()
                    withContext(Dispatchers.Main) {
                        tasks = list
                        tasksLoaded = true
                    }
                }

                // 6. Live Gita Wisdom Verse from gita.json
                val provider = GitaProvider.getInstance(context)
                val v = provider.getDailyVerse()
                withContext(Dispatchers.Main) {
                    dailyVerseObj = v
                }
            } catch (e: Exception) {
                Log.w("HomeScreen", "Error loading initial live data: ${e.message}")
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOverlayRunning = V3BubbleOverlayService.isRunning
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        if (V3PlatformHost.isInitialized) {
                            val todaySummary = V3PlatformHost.screenTimeTracker.collectAndPersistToday(true)
                            val screenMs = todaySummary.totalDurationMs
                            val screenHours = screenMs / (1000 * 60 * 60)
                            val screenMins = (screenMs / (1000 * 60)) % 60
                            withContext(Dispatchers.Main) {
                                screenTimeFormatted = if (screenHours > 0) "${screenHours}h ${screenMins}m" else "${screenMins}m"
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text(
                    text = "Add a gentle note",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    placeholder = { Text("What gentle step will you take?") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteText.isNotBlank()) {
                            val textToAdd = newNoteText.trim()
                            newNoteText = ""
                            showAddNoteDialog = false
                            coroutineScope.launch(Dispatchers.IO) {
                                val newTask = V3Task(
                                    id = UUID.randomUUID().toString(),
                                    title = textToAdd,
                                    priority = "MEDIUM",
                                    status = "pending",
                                    createdAt = System.currentTimeMillis()
                                )
                                V3PlatformHost.repository.insertTask(newTask)
                                reloadTasks()
                            }
                        } else {
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = PillShape
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // =====================================================================
        // 1. Warm Dynamic Greeting Header
        // =====================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Spa,
                        contentDescription = "Sanctuary",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = daySanctuary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "A quiet space for today.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // =====================================================================
        // 2. The morning flow (Snapshot Card with Live Telemetry)
        // =====================================================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header row: Pulse dot + The morning flow | HUD Pill action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "The morning flow",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        Surface(
                            onClick = toggleOverlay,
                            shape = PillShape,
                            color = if (isOverlayRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Layers,
                                    contentDescription = "Floating Pill HUD",
                                    tint = if (isOverlayRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isOverlayRunning) "HUD ACTIVE" else "HUD PILL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = if (isOverlayRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 3 Metric Grid with Real Live Telemetry (Clickable Deep Links)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Metric 1: Screen time (Opens Digital Stillness detail)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenScreenTime() },
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = screenTimeFormatted,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "screen time",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Metric 2: Focus sanctuary (Opens Focus Chrono)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenFocusChrono() },
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = focusedTimeFormatted,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "focus sanctuary",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Metric 3: Rituals met (Opens Habits Matrix)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenHabits() },
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = ritualsFormatted,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp
                                ),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "rituals met",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Soft Breath Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(snapshotProgress.coerceIn(0.05f, 1.0f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 3. Take a little time for yourself (Focus Chronometer Card)
        // =====================================================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row with Hourglass badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Take a little time for yourself",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Quiet intervals for calm presence",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onOpenFocusChrono() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HourglassEmpty,
                                contentDescription = "Focus Chrono",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Big Countdown Display (25 : 00)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "25 : 00",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "Foreground focus service ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Action Row: [ > Start focus ] + [ [ ] ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onOpenFocusChrono() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = "Start",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Start focus",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onOpenFocusChrono() },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CropFree,
                                    contentDescription = "Expand",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 4. A few things for today (Live SQLite Persistent Tasks)
        // =====================================================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "A few things for today",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Unrushed steps to take",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Completed Badge
                        val completedCount = tasks.count {
                            it.status.equals("completed", ignoreCase = true) || it.status.equals("done", ignoreCase = true)
                        }
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "$completedCount/${tasks.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Live Task List or Clean Calm Empty State
                    if (tasks.isEmpty() && tasksLoaded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "No tasks scheduled for today.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Take a mindful pause or add a gentle intention below.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            tasks.forEach { task ->
                                val isDone = task.status.equals("completed", ignoreCase = true) || task.status.equals("done", ignoreCase = true)
                                GentleStepItem(
                                    text = task.title,
                                    isDone = isDone,
                                    onToggle = {
                                        val newStatus = if (isDone) "pending" else "completed"
                                        coroutineScope.launch(Dispatchers.IO) {
                                            V3PlatformHost.repository.setTaskStatus(task.id, newStatus)
                                            reloadTasks()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // "+ Add a gentle note" Pill Action Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PillShape)
                            .clickable { showAddNoteDialog = true }
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                shape = PillShape
                            ),
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add a gentle note",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 5. Currently reading (Bhagavad Gita Wisdom Companion)
        // =====================================================================
        item {
            val ch = dailyVerseObj?.optInt("chapter") ?: 2
            val vs = dailyVerseObj?.optInt("verse") ?: 47
            val shlokaText = dailyVerseObj?.optJSONObject("shloka")?.optString("text")
                ?: dailyVerseObj?.optString("slok", "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन। मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि।।")
            val hindiTrans = dailyVerseObj?.optJSONObject("hindi_translation")?.optString("text") ?: ""
            val engSiva = dailyVerseObj?.optJSONObject("english_sivananda")?.optString("text") ?: ""
            val translationText = if (hindiTrans.isNotBlank()) hindiTrans else if (engSiva.isNotBlank()) engSiva else "You have a right to perform your prescribed duty, but not to the fruits of action."
            val readCount = dailyVerseObj?.optInt("read_count", 0) ?: 0
            val totalVerses = dailyVerseObj?.optInt("total_verses", 701) ?: 701
            val progressPercent = if (totalVerses > 0) (readCount * 100) / totalVerses else 0

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = "Reading",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Currently reading",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Wisdom Companion",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Reading Banner / Content Box with Live Verse
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "The Bhagavad Gita",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Chapter $ch • Verse $vs",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"$shlokaText\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.5.sp
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "\"$translationText\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Read progress indicator
                            if (readCount > 0) {
                                Text(
                                    text = "$readCount of $totalVerses verses ($progressPercent% completed)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    // Open Companion Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PillShape)
                            .clickable { onOpenGitaCompanion() },
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open Wisdom Companion",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = "Open",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 6. Serene Atmospheric Daily Quote Card
        // =====================================================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FormatQuote,
                            contentDescription = "Quote",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "\"${currentQuote.quote}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "— ${currentQuote.author}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun GentleStepItem(
    text: String,
    isDone: Boolean,
    onToggle: () -> Unit
) {
    val checkBgColor by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "checkBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .background(checkBgColor),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Done",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
