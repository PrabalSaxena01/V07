package com.v07.v3.ui.compose

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data representation of a tracked habit ritual.
 */
data class HabitItemModel(
    val id: String,
    val name: String,
    val icon: String,
    val hexColor: String = "#10B981",
    val targetDays: Int = 7
)

/**
 * Day info within the active week matrix.
 */
data class MatrixDay(
    val dateStr: String,     // "yyyy-MM-dd"
    val dayInitial: String,  // "M", "T", "W"...
    val dayNumber: String,   // "21"
    val isToday: Boolean
)

/**
 * Native Jetpack Compose Habit Tracker Screen (Gentle Habits).
 * Faithful implementation of the canonical V07 Habit Matrix and Habit Streak Cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { EventRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayDateStr = remember { dateFormat.format(Date()) }

    var weekOffset by remember { mutableIntStateOf(0) } // 0 = current week, -1 = last week, etc.

    // Calculate the 7 days of the active week (Mon - Sun)
    val weekDays = remember(weekOffset) {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val days = mutableListOf<MatrixDay>()
        val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")

        for (i in 0 until 7) {
            val dStr = dateFormat.format(cal.time)
            val dNum = cal.get(Calendar.DAY_OF_MONTH).toString()
            days.add(
                MatrixDay(
                    dateStr = dStr,
                    dayInitial = dayLetters[i],
                    dayNumber = dNum,
                    isToday = dStr == todayDateStr
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        days
    }

    // Default Canonical Habits
    val defaultHabits = remember {
        listOf(
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
    }

    var habits by remember { mutableStateOf(defaultHabits) }
    // Completed map: "habitId:yyyy-MM-dd" -> Boolean
    var completions by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitItemModel?>(null) }

    // Load definitions and logs from EventRepository
    fun loadHabitData() {
        scope.launch(Dispatchers.IO) {
            try {
                // 1. Load custom definitions if any
                val defsJson = repository.getPluginValue("com.v07.habits", "definitions")
                val loadedHabits = mutableListOf<HabitItemModel>()
                if (!defsJson.isNullOrBlank()) {
                    try {
                        val arr = JSONArray(defsJson)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val id = obj.optString("id", UUID.randomUUID().toString())
                            val name = obj.optString("name", "Habit")
                            val icon = obj.optString("icon", "🌱")
                            val color = obj.optString("color", "#10B981")
                            loadedHabits.add(HabitItemModel(id, name, icon, color))
                        }
                    } catch (_: Exception) {}
                }

                val finalHabits = if (loadedHabits.isNotEmpty()) loadedHabits else defaultHabits

                // 2. Load completions
                val compMap = mutableMapOf<String, Boolean>()

                // Check stored logs in plugin_storage
                val logsJson = repository.getPluginValue("com.v07.habits", "logs")
                if (!logsJson.isNullOrBlank()) {
                    try {
                        val arr = JSONArray(logsJson)
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            val hid = item.optString("habit", item.optString("habit_id", ""))
                            val d = item.optString("date", "")
                            val on = item.optBoolean("on", item.optBoolean("completed", true))
                            if (hid.isNotBlank() && d.isNotBlank()) {
                                compMap["$hid:$d"] = on
                            }
                        }
                    } catch (_: Exception) {}
                }

                // Also query V3Event table for category 'habit' or 'hbt'
                val habitEvents = repository.getEvents(category = "habit", limit = 1000) +
                        repository.getEvents(category = "hbt", limit = 1000)

                for (ev in habitEvents) {
                    val d = ev.date
                    if (d.isNotBlank()) {
                        // Check if metadata contains habit_id
                        var foundHid: String? = null
                        if (!ev.metadataJson.isNullOrBlank()) {
                            try {
                                val meta = JSONObject(ev.metadataJson)
                                if (meta.has("habit_id")) {
                                    foundHid = meta.getString("habit_id")
                                }
                            } catch (_: Exception) {}
                        }
                        if (foundHid == null) {
                            // Match by habit title or id in event title
                            val matched = finalHabits.firstOrNull { ev.title?.contains(it.name, ignoreCase = true) == true }
                            if (matched != null) foundHid = matched.id
                        }
                        if (foundHid != null) {
                            compMap["$foundHid:$d"] = true
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    habits = finalHabits
                    completions = compMap
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadHabitData()
    }

    // Toggle a habit on a specific date
    fun toggleHabit(habitId: String, dateStr: String) {
        val key = "$habitId:$dateStr"
        val current = completions[key] ?: false
        val newStatus = !current

        val updatedMap = completions.toMutableMap()
        if (newStatus) {
            updatedMap[key] = true
        } else {
            updatedMap.remove(key)
        }
        completions = updatedMap

        scope.launch(Dispatchers.IO) {
            val habit = habits.firstOrNull { it.id == habitId } ?: return@launch
            val now = System.currentTimeMillis()

            // 1. Insert/update core V3Event
            if (newStatus) {
                val event = V3Event(
                    id = "hbt_${habitId}_$dateStr",
                    category = "habit",
                    date = dateStr,
                    ts = "${dateStr}T12:00:00Z",
                    timestamp = now,
                    title = "${habit.icon} ${habit.name}",
                    content = "Completed ${habit.name}",
                    score = 10,
                    metadataJson = "{\"habit_id\":\"$habitId\",\"completed\":true}"
                )
                repository.insertEvent(event)
            } else {
                repository.deleteEvent("hbt_${habitId}_$dateStr")
            }

            // 2. Sync to plugin_storage for habits
            try {
                val currentLogsStr = repository.getPluginValue("com.v07.habits", "logs")
                val logsArr = if (!currentLogsStr.isNullOrBlank()) JSONArray(currentLogsStr) else JSONArray()
                val newArr = JSONArray()

                // Filter out previous entry for same habit and date
                for (i in 0 until logsArr.length()) {
                    val item = logsArr.getJSONObject(i)
                    val hid = item.optString("habit", item.optString("habit_id", ""))
                    val d = item.optString("date", "")
                    if (!(hid == habitId && d == dateStr)) {
                        newArr.put(item)
                    }
                }
                if (newStatus) {
                    newArr.put(JSONObject().apply {
                        put("habit", habitId)
                        put("date", dateStr)
                        put("on", true)
                        put("created", now)
                    })
                }
                repository.setPluginValue("com.v07.habits", "logs", newArr.toString())
            } catch (_: Exception) {}
        }
    }

    // Mark all habits done for today
    fun logAllToday() {
        val updatedMap = completions.toMutableMap()
        habits.forEach { habit ->
            updatedMap["${habit.id}:$todayDateStr"] = true
        }
        completions = updatedMap

        scope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            habits.forEach { habit ->
                val event = V3Event(
                    id = "hbt_${habit.id}_$todayDateStr",
                    category = "habit",
                    date = todayDateStr,
                    ts = "${todayDateStr}T12:00:00Z",
                    timestamp = now,
                    title = "${habit.icon} ${habit.name}",
                    content = "Completed ${habit.name}",
                    score = 10,
                    metadataJson = "{\"habit_id\":\"${habit.id}\",\"completed\":true}"
                )
                repository.insertEvent(event)
            }

            // Update plugin_storage
            try {
                val currentLogsStr = repository.getPluginValue("com.v07.habits", "logs")
                val logsArr = if (!currentLogsStr.isNullOrBlank()) JSONArray(currentLogsStr) else JSONArray()
                val existingKeys = mutableSetOf<String>()
                val newArr = JSONArray()

                for (i in 0 until logsArr.length()) {
                    val item = logsArr.getJSONObject(i)
                    val hid = item.optString("habit", item.optString("habit_id", ""))
                    val d = item.optString("date", "")
                    if (d == todayDateStr) {
                        existingKeys.add(hid)
                    }
                    newArr.put(item)
                }

                habits.forEach { habit ->
                    if (!existingKeys.contains(habit.id)) {
                        newArr.put(JSONObject().apply {
                            put("habit", habit.id)
                            put("date", todayDateStr)
                            put("on", true)
                            put("created", now)
                        })
                    }
                }
                repository.setPluginValue("com.v07.habits", "logs", newArr.toString())
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "All rituals logged for today! 🌿", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Calculate current streak for a habit
    fun calculateStreak(habitId: String): Int {
        var streak = 0
        val cal = Calendar.getInstance()

        // Check today first; if not done, check if yesterday was done
        val isDoneToday = completions["$habitId:$todayDateStr"] ?: false
        if (isDoneToday) streak++

        cal.add(Calendar.DAY_OF_MONTH, -1)
        while (true) {
            val dStr = dateFormat.format(cal.time)
            val isDone = completions["$habitId:$dStr"] ?: false
            if (isDone) {
                streak++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
            if (streak > 365) break // safety limit
        }
        return streak
    }

    // Add a new habit
    fun addHabit(name: String, icon: String, colorHex: String) {
        val newHabit = HabitItemModel(
            id = "hbt_${System.currentTimeMillis()}",
            name = name,
            icon = icon,
            hexColor = colorHex
        )
        val updatedList = habits + newHabit
        habits = updatedList

        scope.launch(Dispatchers.IO) {
            try {
                val arr = JSONArray()
                updatedList.forEach { h ->
                    arr.put(JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("icon", h.icon)
                        put("color", h.hexColor)
                    })
                }
                repository.setPluginValue("com.v07.habits", "definitions", arr.toString())
            } catch (_: Exception) {}
        }
    }

    // Remove an existing habit
    fun removeHabit(habitId: String) {
        val updatedList = habits.filter { it.id != habitId }
        habits = updatedList

        scope.launch(Dispatchers.IO) {
            try {
                val arr = JSONArray()
                updatedList.forEach { h ->
                    arr.put(JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("icon", h.icon)
                        put("color", h.hexColor)
                    })
                }
                repository.setPluginValue("com.v07.habits", "definitions", arr.toString())
            } catch (_: Exception) {}
        }
    }

    val completedTodayCount = habits.count { completions["${it.id}:$todayDateStr"] == true }

    var streakFilter by remember { mutableStateOf("All") }
    val streakFilterOptions = remember { listOf("All", "Pending", "Completed") }

    val weekOptions = remember { listOf("This Week", "Last Week", "2 Weeks Ago", "3 Weeks Ago") }
    val selectedWeekLabel = when (weekOffset) {
        0 -> "This Week"
        -1 -> "Last Week"
        -2 -> "2 Weeks Ago"
        -3 -> "3 Weeks Ago"
        else -> "$weekOffset wks"
    }

    val filteredHabits = remember(habits, streakFilter, completions, todayDateStr) {
        when (streakFilter) {
            "Pending" -> habits.filter { (completions["${it.id}:$todayDateStr"] ?: false).not() }
            "Completed" -> habits.filter { completions["${it.id}:$todayDateStr"] == true }
            else -> habits
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Gentle Habits",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Cadence, rhythm & daily rituals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { loadHabitData() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanctuaryPalette.AccentHabits,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Metric Overview Pill Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = PillShape,
                            color = SanctuaryPalette.AccentHabitsBg,
                            border = BorderStroke(1.dp, SanctuaryPalette.AccentHabits.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "🔥 $completedTodayCount/${habits.size} TODAY",
                                color = SanctuaryPalette.AccentHabits,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${habits.size} ACTIVE",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { logAllToday() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanctuaryPalette.AccentHabits.copy(alpha = 0.15f),
                            contentColor = SanctuaryPalette.AccentHabits
                        ),
                        border = BorderStroke(1.dp, SanctuaryPalette.AccentHabits.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "LOG ALL TODAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Weekly Habit Matrix Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Section Header & Week Navigation with Dropdown Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. HABIT MATRIX",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                SanctuaryFilterDropdown(
                                    selectedItem = selectedWeekLabel,
                                    items = weekOptions,
                                    onItemSelected = { label ->
                                        weekOffset = when (label) {
                                            "This Week" -> 0
                                            "Last Week" -> -1
                                            "2 Weeks Ago" -> -2
                                            "3 Weeks Ago" -> -3
                                            else -> 0
                                        }
                                    },
                                    leadingIcon = Icons.Outlined.DateRange
                                )

                                IconButton(
                                    onClick = { weekOffset-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronLeft,
                                        contentDescription = "Previous Week",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { weekOffset++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = "Next Week",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Matrix Header: Habit label on left, 7 day columns on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HABIT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                weekDays.forEach { day ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (day.isToday) SanctuaryPalette.AccentHabits.copy(alpha = 0.15f) else Color.Transparent)
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = day.dayInitial,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (day.isToday) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = day.dayNumber,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = if (day.isToday) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Matrix Rows
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            habits.forEach { habit ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Habit Icon + Title
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = habit.icon,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                lineHeight = 13.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // 7 Day Toggle Cells
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        weekDays.forEach { day ->
                                            val isDone = completions["${habit.id}:${day.dateStr}"] ?: false
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isDone) SanctuaryPalette.AccentHabits.copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isDone) SanctuaryPalette.AccentHabits
                                                        else if (day.isToday) SanctuaryPalette.AccentHabits.copy(alpha = 0.4f)
                                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { toggleHabit(habit.id, day.dateStr) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isDone) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Check,
                                                        contentDescription = "Done",
                                                        tint = SanctuaryPalette.AccentHabits,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                } else if (day.isToday) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(SanctuaryPalette.AccentHabits.copy(alpha = 0.5f))
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
            }

            // 3. Habit Streak Cards Section with Filter Dropdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "2. 🔥 HABIT STREAK CARDS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${filteredHabits.size}/${habits.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SanctuaryFilterDropdown(
                        selectedItem = streakFilter,
                        items = streakFilterOptions,
                        onItemSelected = { streakFilter = it },
                        leadingIcon = Icons.Outlined.FilterList
                    )
                }
            }

            // Streak Cards List
            items(filteredHabits) { habit ->
                val isDoneToday = completions["${habit.id}:$todayDateStr"] ?: false
                val streak = calculateStreak(habit.id)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isDoneToday) SanctuaryPalette.AccentHabits.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = habit.icon, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isDoneToday) "Completed for today" else "Pending daily rhythm",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDoneToday) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Quick Streak Badge
                                Surface(
                                    shape = PillShape,
                                    color = if (streak > 0) Color(0x1AFFB454) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = if (streak > 0) BorderStroke(1.dp, Color(0x60FFB454)) else null
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "🔥 $streak d",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (streak > 0) Color(0xFFFFB454) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { habitToDelete = habit },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Remove Habit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Completion Toggle Button
                        Button(
                            onClick = { toggleHabit(habit.id, todayDateStr) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDoneToday) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = if (isDoneToday) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isDoneToday) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDoneToday) "COMPLETED TODAY" else "MARK DONE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddDialog) {
        var habitNameInput by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("🌱") }
        var selectedFrequency by remember { mutableStateOf("Every day (7d)") }
        var selectedCategory by remember { mutableStateOf("Mindfulness 🧘") }

        val frequencyOptions = listOf("Every day (7d)", "Weekdays (5d)", "Weekends (2d)", "3 times / week (3d)", "4 times / week (4d)")
        val categoryOptions = listOf("Mindfulness 🧘", "Physical / Fitness 💪", "Deep Work ⚡", "Knowledge 📚", "Discipline ⚔️", "Health 🌿")
        val iconsList = listOf("🌱", "🏃", "💪", "🧘", "📚", "💻", "✍️", "⚡", "🎯", "✨", "💧", "♟️", "⚔️")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "New Gentle Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Establish a gentle cadence for your daily presence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = habitNameInput,
                        onValueChange = { habitNameInput = it },
                        label = { Text("Habit Name") },
                        placeholder = { Text("e.g. Sanskrit Chant, 30m Run") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SanctuaryFormDropdown(
                        label = "Cadence Frequency",
                        selectedItem = selectedFrequency,
                        items = frequencyOptions,
                        onItemSelected = { selectedFrequency = it },
                        leadingIcon = Icons.Outlined.Repeat
                    )

                    SanctuaryFormDropdown(
                        label = "Domain / Category",
                        selectedItem = selectedCategory,
                        items = categoryOptions,
                        onItemSelected = { selectedCategory = it },
                        leadingIcon = Icons.Outlined.Category
                    )

                    Text(
                        text = "Choose Icon",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconsList.forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedIcon == icon) SanctuaryPalette.AccentHabits.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = if (selectedIcon == icon) 2.dp else 1.dp,
                                        color = if (selectedIcon == icon) SanctuaryPalette.AccentHabits else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 20.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = habitNameInput.trim()
                        if (trimmed.isNotBlank()) {
                            addHabit(trimmed, selectedIcon, "#10B981")
                            showAddDialog = false
                            Toast.makeText(context, "Added ritual: $selectedIcon $trimmed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SanctuaryPalette.AccentHabits),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add Ritual", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Habit Confirmation Dialog
    if (habitToDelete != null) {
        val habit = habitToDelete!!
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
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
                    text = "Are you sure you want to remove \"${habit.icon} ${habit.name}\" from your daily rituals? Your historical logs will remain safely stored.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        removeHabit(habit.id)
                        Toast.makeText(context, "Removed ritual: ${habit.name}", Toast.LENGTH_SHORT).show()
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
