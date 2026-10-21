package com.v07.v3.ui.compose

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.ActiveSessionState
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.app.timer.TimerMode
import com.v07.v3.app.timer.TimerStatus
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Event
import com.v07.v3.core.data.V3Task
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToSubView: (SanctuarySubView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { EventRepository.getInstance(context) }
    val timerManager = remember { TimerManager.getInstance(context, repository) }
    val screenTimeTracker = remember { ScreenTimeTracker(context, repository) }
    val timerState by timerManager.timerState.collectAsState()

    // Screen Time permission
    var hasUsagePermission by remember { mutableStateOf(screenTimeTracker.hasUsageStatsPermission()) }
    var todayScreenTimeFormatted by remember { mutableStateOf("--") }

    // Snapshot stats
    var todayFocusMinutes by remember { mutableStateOf(0) }
    var habitsMetCount by remember { mutableStateOf(0) }
    var totalHabitsCount by remember { mutableStateOf(0) }

    // Focus widget state
    var selectedPresetMinutes by remember { mutableStateOf(25) }
    var isOpenStopwatchMode by remember { mutableStateOf(false) }

    // Tasks state
    var tasksList by remember { mutableStateOf<List<V3Task>>(emptyList()) }
    var newTaskTitle by remember { mutableStateOf("") }
    var showDeadlineFields by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf("MEDIUM") }
    var taskFilter by remember { mutableStateOf("Pending") }
    val taskFilterOptions = remember { listOf("Pending", "All", "Completed") }
    val priorityOptions = remember { listOf("LOW", "MEDIUM", "HIGH", "URGENT") }

    val displayedTasks = remember(tasksList, taskFilter) {
        when (taskFilter) {
            "Pending" -> tasksList.filter { !it.status.equals("completed", true) && !it.status.equals("done", true) }
            "Completed" -> tasksList.filter { it.status.equals("completed", true) || it.status.equals("done", true) }
            else -> tasksList
        }
    }

    // Habits state
    var habitsList by remember { mutableStateOf<List<HabitItem>>(emptyList()) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var habitToDeleteOnHome by remember { mutableStateOf<HabitItem?>(null) }

    fun loadRealHabits() {
        val defsJson = repository.getPluginValue("com.v07.habits", "definitions")
        val loaded = mutableListOf<HabitItemModel>()
        if (!defsJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(defsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    loaded.add(HabitItemModel(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", "Habit"),
                        icon = obj.optString("icon", "🌱"),
                        hexColor = obj.optString("color", "#10B981")
                    ))
                }
            } catch (_: Exception) {}
        }
        val actualHabitDefs = if (loaded.isNotEmpty()) loaded else listOf(
            HabitItemModel("hbt_ssb", "SSB Practice", "⚔️", "#FFB454"),
            HabitItemModel("hbt_running", "Running", "🏃", "#4DE086"),
            HabitItemModel("hbt_dsa", "DSA Problem Solving", "💻", "#68A6FF"),
            HabitItemModel("hbt_chess", "Chess Training", "♟️", "#F4D35E"),
            HabitItemModel("hbt_content_creation", "Content Creation", "✍️", "#FF6F61"),
            HabitItemModel("hbt_development", "Development", "⚡", "#63D1F4"),
            HabitItemModel("hbt_meditation", "Meditation", "🧘", "#C792EA"),
            HabitItemModel("hmudx8p39", "Pushups", "💪", "#FF9F45"),
            HabitItemModel("hmud3qf88", "Mindfulness", "🌿", "#80CBC4"),
            HabitItemModel("hmud3qz2i", "Deep Study", "📚", "#A78BFA"),
            HabitItemModel("hmudmiwt3", "Daily Routine", "✨", "#FBBF24")
        )

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdfDate.format(Date())
        val compMap = mutableMapOf<String, Boolean>()
        val logsJson = repository.getPluginValue("com.v07.habits", "logs")
        if (!logsJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(logsJson)
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val hid = item.optString("habit", item.optString("habit_id", ""))
                    val d = item.optString("date", "")
                    val on = item.optBoolean("on", item.optBoolean("completed", true))
                    if (d == todayStr && hid.isNotBlank()) {
                        compMap[hid] = on
                    }
                }
            } catch (_: Exception) {}
        }
        val habitEvents = repository.getEvents(category = "habit", limit = 200)
        for (ev in habitEvents) {
            if (ev.date == todayStr) {
                val hid = actualHabitDefs.firstOrNull { ev.title?.contains(it.name, ignoreCase = true) == true }?.id
                if (hid != null) compMap[hid] = true
            }
        }

        val mappedHabits = actualHabitDefs.map { def ->
            HabitItem(
                id = def.id,
                title = "${def.icon} ${def.name}",
                description = "Daily ritual",
                completed = compMap[def.id] == true,
                streak = if (compMap[def.id] == true) 1 else 0
            )
        }
        habitsList = mappedHabits
    }

    fun toggleHabitOnHome(habit: HabitItem) {
        val newStatus = !habit.completed
        habitsList = habitsList.map { if (it.id == habit.id) it.copy(completed = newStatus) else it }
        coroutineScope.launch(Dispatchers.IO) {
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = sdfDate.format(Date())
            if (newStatus) {
                val event = V3Event(
                    id = "hbt_${habit.id}_$todayStr",
                    category = "habit",
                    date = todayStr,
                    ts = "${todayStr}T12:00:00Z",
                    timestamp = System.currentTimeMillis(),
                    title = habit.title,
                    content = "Completed ${habit.title}",
                    score = 10,
                    metadataJson = "{\"habit_id\":\"${habit.id}\",\"completed\":true}"
                )
                repository.insertEvent(event)
            } else {
                repository.deleteEvent("hbt_${habit.id}_$todayStr")
            }
            try {
                val currentLogsStr = repository.getPluginValue("com.v07.habits", "logs")
                val logsArr = if (!currentLogsStr.isNullOrBlank()) JSONArray(currentLogsStr) else JSONArray()
                val newArr = JSONArray()
                for (i in 0 until logsArr.length()) {
                    val item = logsArr.getJSONObject(i)
                    val hid = item.optString("habit", item.optString("habit_id", ""))
                    val d = item.optString("date", "")
                    if (!(hid == habit.id && d == todayStr)) {
                        newArr.put(item)
                    }
                }
                if (newStatus) {
                    newArr.put(JSONObject().apply {
                        put("habit", habit.id)
                        put("date", todayStr)
                        put("on", true)
                        put("created", System.currentTimeMillis())
                    })
                }
                repository.setPluginValue("com.v07.habits", "logs", newArr.toString())
            } catch (_: Exception) {}
        }
    }

    fun addHabitOnHome(name: String, icon: String) {
        val newId = "hbt_${System.currentTimeMillis()}"
        val newTitle = "$icon $name"
        val updatedList = habitsList + HabitItem(newId, newTitle, "Daily ritual", false, 0)
        habitsList = updatedList
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val defsJson = repository.getPluginValue("com.v07.habits", "definitions")
                val arr = if (!defsJson.isNullOrBlank()) JSONArray(defsJson) else JSONArray()
                arr.put(JSONObject().apply {
                    put("id", newId)
                    put("name", name)
                    put("icon", icon)
                    put("color", "#10B981")
                })
                repository.setPluginValue("com.v07.habits", "definitions", arr.toString())
            } catch (_: Exception) {}
        }
    }

    fun removeHabitOnHome(habitId: String) {
        habitsList = habitsList.filter { it.id != habitId }
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val defsJson = repository.getPluginValue("com.v07.habits", "definitions")
                if (!defsJson.isNullOrBlank()) {
                    val arr = JSONArray(defsJson)
                    val newArr = JSONArray()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        if (obj.optString("id") != habitId) {
                            newArr.put(obj)
                        }
                    }
                    repository.setPluginValue("com.v07.habits", "definitions", newArr.toString())
                }
            } catch (_: Exception) {}
        }
    }

    // Refresh telemetry & repository data
    fun refreshData() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    loadRealHabits()
                    hasUsagePermission = screenTimeTracker.hasUsageStatsPermission()
                    if (hasUsagePermission) {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val summary = screenTimeTracker.collectForDate(sdf.format(java.util.Date()))
                        val totalMs = summary.totalDurationMs
                        val hrs = totalMs / (1000 * 60 * 60)
                        val mins = (totalMs % (1000 * 60 * 60)) / (1000 * 60)
                        todayScreenTimeFormatted = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                    }

                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val sessions = repository.getTodaySessions(cal.timeInMillis)
                    val focusMs = sessions.sumOf { (it.endTime ?: it.startTime) - it.startTime }
                    todayFocusMinutes = (focusMs / 60000).toInt()

                    tasksList = repository.getTasks()
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val greetingTime = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good day"
            in 17..21 -> "Good evening"
            else -> "Peaceful night"
        }
    }

    val todayDateFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        sdf.format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
    ) {
        // =====================================================================
        // 1. Snug Warm Greeting
        // =====================================================================
        item {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                Text(
                    text = "Today's sanctuary · $todayDateFormatted",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = greetingTime,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "A quiet space for today.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        // =====================================================================
        // 2. Android Usage Access Required Banner
        // =====================================================================
        if (!hasUsagePermission) {
            item {
                Surface(
                    shape = CardShape,
                    color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LockClock,
                                contentDescription = "Usage",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Daily Screen Time Permission",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Enable usage stats in Android settings to track mindful attention.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Button(
                            onClick = {
                                try {
                                    val intent = screenTimeTracker.createPermissionIntent()
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD97706),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Enable", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. Today's Quiet Snapshot (Screen time, Focus, Rituals)
        // =====================================================================
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "The morning flow",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Text(
                            text = "Live sync active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Metric 1: Screen Time
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSubView(SanctuarySubView.SCREENTIME) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = todayScreenTimeFormatted,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "screen time",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        // Metric 2: Focus
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSubView(SanctuarySubView.FOCUS) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${todayFocusMinutes} min",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "focus sanctuary",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        // Metric 3: Rituals Met
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSubView(SanctuarySubView.HABITS) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val met = habitsList.count { it.completed }
                            Text(
                                text = "$met / ${habitsList.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                                color = SanctuaryPalette.AccentHabits
                            )
                            Text(
                                text = "rituals met",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val metRatio = if (habitsList.isNotEmpty()) habitsList.count { it.completed }.toFloat() / habitsList.size else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = metRatio.coerceIn(0.1f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 4. Primary Focus Chronometer Card (One-tap launch & Presets)
        // =====================================================================
        item {
            val isRunning = timerState.status == TimerStatus.RUNNING
            val isPaused = timerState.status == TimerStatus.PAUSED
            val isIdle = timerState.status == TimerStatus.IDLE

            val timeDisplay = if (!isIdle) {
                timerState.getFormattedTime()
            } else if (isOpenStopwatchMode) {
                "00:00"
            } else {
                String.format("%02d:00", selectedPresetMinutes)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Take a little time for yourself",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Quiet intervals for calm presence",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .clickable { onNavigateToSubView(SanctuarySubView.FOCUS) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HourglassTop,
                                contentDescription = "Focus",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chrono Digits & Badges
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToSubView(SanctuarySubView.FOCUS) }
                    ) {
                        Text(
                            text = timeDisplay,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 42.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Target Badge
                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = "Target",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isOpenStopwatchMode) "Mode: Stopwatch (0→∞)" else "Target: ${selectedPresetMinutes} min",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            // State Dot
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isRunning) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = if (isRunning) "Flowing" else if (isPaused) "Paused" else "Ready",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Chips Row (15m, 25m, 45m, Open Stopwatch 0->infinity)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(15, 25, 45).forEach { mins ->
                            val isSelected = !isOpenStopwatchMode && selectedPresetMinutes == mins && isIdle
                            val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val chipText = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(chipBg)
                                    .clickable {
                                        if (isIdle) {
                                            isOpenStopwatchMode = false
                                            selectedPresetMinutes = mins
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = chipText
                                    )
                                )
                            }
                        }

                        // Open Stopwatch (0 -> infinity) preset
                        val isOpenSelected = isOpenStopwatchMode && isIdle
                        val openBg = if (isOpenSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val openText = if (isOpenSelected) Color.White else MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(openBg)
                                .clickable {
                                    if (isIdle) {
                                        isOpenStopwatchMode = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.AllInclusive,
                                    contentDescription = "Open",
                                    tint = openText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Open",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isOpenSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = openText
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isRunning) {
                                    timerManager.pauseSession()
                                } else if (isPaused) {
                                    timerManager.resumeSession()
                                } else {
                                    val mode = if (isOpenStopwatchMode) TimerMode.STOPWATCH else TimerMode.COUNTDOWN
                                    timerManager.startSession(
                                        topic = "Deep Focus Flow",
                                        category = "focus",
                                        mode = mode,
                                        targetMinutes = if (isOpenStopwatchMode) 0 else selectedPresetMinutes
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Play",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "Pause flow" else if (isPaused) "Resume flow" else "Start focus",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }

                        // Stop & Log Button (visible when not idle)
                        if (!isIdle) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            timerManager.finishSession()
                                        }
                                        refreshData()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                    contentColor = Color(0xFFD97706)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Outlined.StopCircle, contentDescription = "Stop", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop & Log", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        // Fullscreen Icon Button
                        IconButton(
                            onClick = { onNavigateToSubView(SanctuarySubView.FOCUS) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 5. Gentle Habits Snapshot Card
        // =====================================================================
        item {
            val completedCount = habitsList.count { it.completed }
            val totalHabits = habitsList.size
            val pendingHabits = habitsList.filter { !it.completed }
            val habitSubtitle = when {
                totalHabits == 0 -> "No rituals yet · Tap + to create"
                completedCount == totalHabits -> "All $totalHabits rituals completed today! 🌿"
                completedCount == 0 -> "0 of $totalHabits completed · Start your gentle cadence"
                else -> "$completedCount of $totalHabits met (${((completedCount.toFloat() / totalHabits) * 100).toInt()}%) · Next: ${pendingHabits.firstOrNull()?.title ?: ""}"
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gentle habits",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .clickable { showAddHabitDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Add Habit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                            }
                            TextButton(
                                onClick = { onNavigateToSubView(SanctuarySubView.HABITS) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("View all", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = habitSubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            color = if (completedCount == totalHabits && totalHabits > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (habitsList.isEmpty()) {
                        Text(
                            text = "No rituals recorded yet. Tap + above to add your first daily habit.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            habitsList.take(6).forEach { habit ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { toggleHabitOnHome(habit) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (habit.completed) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (habit.completed) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = habit.title,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    textDecoration = if (habit.completed) TextDecoration.LineThrough else TextDecoration.None
                                                ),
                                                color = if (habit.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (habit.completed) "Completed for today" else habit.description,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                )
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (habit.streak > 0) {
                                            Surface(
                                                shape = PillShape,
                                                color = SanctuaryPalette.AccentHabitsBg
                                            ) {
                                                Text(
                                                    text = "${habit.streak}d streak",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SanctuaryPalette.AccentHabits
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { habitToDeleteOnHome = habit },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteOutline,
                                                contentDescription = "Remove Habit",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                                modifier = Modifier.size(16.dp)
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

        // =====================================================================
        // 6. Sovereign Tasks & Priorities Card
        // =====================================================================
        item {
            val completedCount = tasksList.count { it.status.equals("completed", ignoreCase = true) || it.status.equals("done", ignoreCase = true) }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSubView(SanctuarySubView.TASKS) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Sovereign Tasks",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "Daily directives & priorities · Tap for full view",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$completedCount done",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            SanctuaryFilterDropdown(
                                selectedItem = taskFilter,
                                items = taskFilterOptions,
                                onItemSelected = { taskFilter = it },
                                leadingIcon = Icons.Outlined.FilterList
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Task Add Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("Define a quiet priority...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        IconButton(
                            onClick = { showDeadlineFields = !showDeadlineFields },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Outlined.Event, contentDescription = "Deadline", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }

                        Button(
                            onClick = {
                                val title = newTaskTitle.trim()
                                if (title.isNotBlank()) {
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            val t = V3Task(
                                                id = UUID.randomUUID().toString(),
                                                title = title,
                                                status = "pending",
                                                priority = selectedPriority.lowercase(),
                                                createdAt = System.currentTimeMillis()
                                            )
                                            repository.insertTask(t)
                                        }
                                        newTaskTitle = ""
                                        refreshData()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Add", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    // Optional Priority Row
                    AnimatedVisibility(visible = showDeadlineFields) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SanctuaryFilterDropdown(
                                label = "Priority:",
                                selectedItem = selectedPriority,
                                items = priorityOptions,
                                onItemSelected = { selectedPriority = it },
                                leadingIcon = Icons.Outlined.Flag
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Task List
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        displayedTasks.take(5).forEach { task ->
                            val isDone = task.status.equals("completed", ignoreCase = true) || task.status.equals("done", ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable {
                                        coroutineScope.launch {
                                            withContext(Dispatchers.IO) {
                                                repository.toggleTaskStatus(task.id)
                                            }
                                            refreshData()
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isDone) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Priority pill
                                    val (pColor, pBg) = when (task.priority.lowercase()) {
                                        "high" -> Color(0xFFEF4444) to Color(0x22EF4444)
                                        "low" -> Color(0xFF3B82F6) to Color(0x223B82F6)
                                        else -> Color(0xFFF59E0B) to Color(0x22F59E0B)
                                    }
                                    Surface(shape = PillShape, color = pBg) {
                                        Text(
                                            text = task.priority.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = pColor),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    repository.deleteTask(task.id)
                                                }
                                                refreshData()
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Habit Dialog on Home Screen
    if (showAddHabitDialog) {
        var nameInput by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("🌱") }
        val iconsList = listOf("🌱", "🏃", "💪", "🧘", "📚", "💻", "✍️", "⚡", "🎯", "✨", "💧", "♟️", "⚔️")

        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            title = {
                Text(
                    text = "New Gentle Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Add a sovereign daily cadence to your sanctuary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Habit Name") },
                        placeholder = { Text("e.g. Cold Shower, Reading...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text("Select Ritual Glyph:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconsList.take(6).forEach { icon ->
                            val isSel = selectedIcon == icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 18.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isNotBlank()) {
                            addHabitOnHome(trimmed, selectedIcon)
                            showAddHabitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SanctuaryPalette.AccentHabits),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add Habit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHabitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Habit Confirmation Dialog on Home Screen
    if (habitToDeleteOnHome != null) {
        val habit = habitToDeleteOnHome!!
        AlertDialog(
            onDismissRequest = { habitToDeleteOnHome = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Remove Ritual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${habit.title}\" from your daily rituals? Historical logs are preserved in your local vault.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        removeHabitOnHome(habit.id)
                        habitToDeleteOnHome = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDeleteOnHome = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class HabitItem(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val streak: Int
)
