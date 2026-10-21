package com.v07.v3.ui.compose

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Task
import com.v07.v3.core.sync.V3CompanionSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SovereignTasksScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { EventRepository.getInstance(context) }
    val tracker = remember { ScreenTimeTracker(context, repository) }
    val syncManager = remember { V3CompanionSyncManager.getInstance(context, repository, tracker) }
    val scope = rememberCoroutineScope()

    var tasks by remember { mutableStateOf<List<V3Task>>(emptyList()) }
    var activeFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, HIGH, COMPLETED
    var newTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("MEDIUM") } // HIGH, MEDIUM, LOW
    var deadlineDate by remember { mutableStateOf<String?>(null) } // "yyyy-MM-dd"
    var deadlineTime by remember { mutableStateOf<String?>(null) } // "HH:mm"
    var isDeadlineBoxOpen by remember { mutableStateOf(false) }

    fun refreshTasks() {
        scope.launch(Dispatchers.IO) {
            val list = repository.getTasks(null)
            withContext(Dispatchers.Main) {
                tasks = list
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshTasks()
    }

    fun addTask() {
        val title = newTitle.trim()
        if (title.isBlank()) return

        val deadlineIso = if (!deadlineDate.isNullOrBlank()) {
            val timePart = deadlineTime ?: "23:59"
            "${deadlineDate}T${timePart}:00"
        } else null

        val task = V3Task(
            id = UUID.randomUUID().toString(),
            title = title,
            priority = selectedPriority,
            status = "pending",
            createdAt = System.currentTimeMillis(),
            deadline = deadlineIso
        )

        scope.launch(Dispatchers.IO) {
            val ok = repository.insertTask(task)
            if (ok) {
                withContext(Dispatchers.Main) {
                    newTitle = ""
                    deadlineDate = null
                    deadlineTime = null
                    isDeadlineBoxOpen = false
                    refreshTasks()
                    Toast.makeText(context, "Directive defined", Toast.LENGTH_SHORT).show()
                }
                try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
            }
        }
    }

    fun toggleTask(taskId: String) {
        scope.launch(Dispatchers.IO) {
            repository.toggleTaskStatus(taskId)
            withContext(Dispatchers.Main) {
                refreshTasks()
            }
            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
        }
    }

    fun deleteTask(taskId: String) {
        scope.launch(Dispatchers.IO) {
            repository.recycleItem(taskId, "task", "{}")
            withContext(Dispatchers.Main) {
                refreshTasks()
                Toast.makeText(context, "Directive moved to recycle bin", Toast.LENGTH_SHORT).show()
            }
            try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
        }
    }

    // Stats Calculations
    val totalCount = tasks.size
    val doneCount = tasks.count { it.status.equals("completed", ignoreCase = true) || it.status.equals("done", ignoreCase = true) }
    val pendingCount = totalCount - doneCount
    val highPriorityPending = tasks.count { it.priority.equals("HIGH", ignoreCase = true) && !it.status.equals("completed", ignoreCase = true) && !it.status.equals("done", ignoreCase = true) }
    val completionPct = if (totalCount > 0) ((doneCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 0

    val filteredTasks = remember(tasks, activeFilter) {
        val sorted = tasks.sortedWith(
            compareBy<V3Task> {
                it.status.equals("completed", ignoreCase = true) || it.status.equals("done", ignoreCase = true)
            }.thenByDescending {
                it.priority.equals("HIGH", ignoreCase = true)
            }.thenByDescending {
                it.createdAt
            }
        )
        when (activeFilter) {
            "PENDING" -> sorted.filter { !it.status.equals("completed", ignoreCase = true) && !it.status.equals("done", ignoreCase = true) }
            "HIGH" -> sorted.filter { it.priority.equals("HIGH", ignoreCase = true) && !it.status.equals("completed", ignoreCase = true) && !it.status.equals("done", ignoreCase = true) }
            "COMPLETED" -> sorted.filter { it.status.equals("completed", ignoreCase = true) || it.status.equals("done", ignoreCase = true) }
            else -> sorted
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier.padding(start = 2.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Sanctuary Tasks",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Offline directives, priority matrix & quiet discipline",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "CORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- Stats HUD ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskStatCard(
                        label = "PENDING",
                        value = "$pendingCount",
                        sub = "Active directives",
                        accentColor = Color(0xFF5CE1E6),
                        modifier = Modifier.weight(1f)
                    )
                    TaskStatCard(
                        label = "HIGH PRIORITY",
                        value = "$highPriorityPending",
                        sub = if (highPriorityPending > 0) "Needs focus" else "All clear",
                        accentColor = Color(0xFFFF5370),
                        modifier = Modifier.weight(1f)
                    )
                    TaskStatCard(
                        label = "COMPLETED",
                        value = "$doneCount",
                        sub = "Accomplished",
                        accentColor = Color(0xFF4DE086),
                        modifier = Modifier.weight(1f)
                    )
                    TaskStatCard(
                        label = "MOMENTUM",
                        value = "$completionPct%",
                        sub = "Execution pace",
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- Execution Momentum Progress Bar ---
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXECUTION MOMENTUM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "$doneCount / $totalCount ($completionPct%)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) (doneCount.toFloat() / totalCount.toFloat()) else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF10B981),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // --- Add Directive Input Card ---
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                placeholder = {
                                    Text(
                                        text = "Define high-impact directive...",
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                        )
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            // Deadline toggle button
                            IconButton(
                                onClick = { isDeadlineBoxOpen = !isDeadlineBoxOpen },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDeadlineBoxOpen || deadlineDate != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .border(1.dp, if (isDeadlineBoxOpen || deadlineDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = "Deadline",
                                    tint = if (isDeadlineBoxOpen || deadlineDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Add button
                            Button(
                                onClick = { addTask() },
                                shape = RoundedCornerShape(10.dp),
                                enabled = newTitle.isNotBlank(),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("+ ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Expandable Deadline & Alarm Options
                        AnimatedVisibility(visible = isDeadlineBoxOpen) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Date picker trigger
                                    OutlinedButton(
                                        onClick = {
                                            val c = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, day ->
                                                    deadlineDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                                },
                                                c.get(Calendar.YEAR),
                                                c.get(Calendar.MONTH),
                                                c.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(deadlineDate ?: "Select Date", fontSize = 11.5.sp)
                                    }

                                    // Time picker trigger
                                    OutlinedButton(
                                        onClick = {
                                            val c = Calendar.getInstance()
                                            TimePickerDialog(
                                                context,
                                                { _, hour, minute ->
                                                    deadlineTime = String.format(Locale.US, "%02d:%02d", hour, minute)
                                                },
                                                c.get(Calendar.HOUR_OF_DAY),
                                                c.get(Calendar.MINUTE),
                                                true
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(deadlineTime ?: "Select Time", fontSize = 11.5.sp)
                                    }

                                    if (deadlineDate != null) {
                                        IconButton(
                                            onClick = {
                                                deadlineDate = null
                                                deadlineTime = null
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Outlined.Close, "Clear deadline", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Audible alarm scheduled at due time with 1-hour advance reminder",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // Priority Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PRIORITY:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            listOf("HIGH" to Color(0xFFFF5370), "MEDIUM" to Color(0xFFFFB84D), "LOW" to Color(0xFF10B981)).forEach { (prio, prioColor) ->
                                val isSel = selectedPriority == prio
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) prioColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isSel) prioColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier.clickable { selectedPriority = prio }
                                ) {
                                    Text(
                                        text = prio,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) prioColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Filter Chips Row ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to totalCount,
                        "PENDING" to pendingCount,
                        "HIGH" to highPriorityPending,
                        "COMPLETED" to doneCount
                    ).forEach { (fName, count) ->
                        val isSel = activeFilter == fName
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { activeFilter = fName }
                        ) {
                            Text(
                                text = "$fName ($count)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // --- Tasks List / Empty State ---
            if (filteredTasks.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "All clear in Sanctuary Tasks",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (activeFilter == "COMPLETED") "No directives marked as completed yet." else "No pending directives. Enjoy the calm presence or define a new goal.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { toggleTask(task.id) },
                        onDelete = { deleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskStatCard(
    label: String,
    value: String,
    sub: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.4.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = accentColor
                )
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: V3Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.status.equals("completed", ignoreCase = true) || task.status.equals("done", ignoreCase = true)
    val prioColor = when (task.priority.uppercase()) {
        "HIGH" -> Color(0xFFFF5370)
        "LOW" -> Color(0xFF10B981)
        else -> Color(0xFFFFB84D)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDone) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox completion toggle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(1.5.dp, if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Task content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggle() },
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority Pill
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = prioColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, prioColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = task.priority.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = prioColor
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    // Deadline pill if exists
                    if (!task.deadline.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = formatDeadline(task.deadline),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Delete Action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete directive",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatDeadline(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val d = parser.parse(iso) ?: return iso
        val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.US)
        formatter.format(d)
    } catch (_: Exception) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val d = parser.parse(iso) ?: return iso
            val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.US)
            formatter.format(d)
        } catch (_: Exception) {
            iso
        }
    }
}
