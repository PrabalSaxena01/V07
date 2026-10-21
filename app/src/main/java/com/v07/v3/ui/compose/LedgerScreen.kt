package com.v07.v3.ui.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Thought
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

enum class LedgerDateFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    WEEK("7d")
}

data class CategoryMeta(val id: String, val label: String, val icon: String, val color: Color)

val LEDGER_CATEGORIES = listOf(
    CategoryMeta("quick", "Quick", "⚡", Color(0xFF006C50)),
    CategoryMeta("win", "Win", "🏆", Color(0xFF10B981)),
    CategoryMeta("lesson", "Lesson", "💡", Color(0xFF06B6D4)),
    CategoryMeta("checkin", "Check-in", "🌅", Color(0xFFF59E0B)),
    CategoryMeta("checkout", "Checkout", "🌙", Color(0xFF8B5CF6)),
    CategoryMeta("session", "Focus", "⏱️", Color(0xFF3B82F6)),
    CategoryMeta("note", "Note", "📝", Color(0xFF6B7280))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    onNavigateToDatacenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { EventRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var thoughts by remember { mutableStateOf<List<V3Thought>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("all") }
    var selectedDateFilter by remember { mutableStateOf(LedgerDateFilter.ALL) }

    // Quick Capture state
    var captureText by remember { mutableStateOf("") }
    var captureCategory by remember { mutableStateOf("quick") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showFilterDropdown by remember { mutableStateOf(false) }

    val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val todayStr = remember { sdfDate.format(Date()) }
    val yesterdayStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        sdfDate.format(cal.time)
    }

    fun loadThoughts() {
        scope.launch(Dispatchers.IO) {
            val list = repository.getRecentThoughts(100)
            withContext(Dispatchers.Main) {
                thoughts = list
            }
        }
    }

    LaunchedEffect(Unit) {
        loadThoughts()
    }

    fun handleRecord() {
        val text = captureText.trim()
        if (text.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val thought = V3Thought(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                date = sdfDate.format(Date(now)),
                topic = captureCategory.replaceFirstChar { it.uppercase() },
                content = text,
                pinned = false,
                tags = captureCategory
            )
            repository.insertThought(thought)
            withContext(Dispatchers.Main) {
                captureText = ""
                loadThoughts()
                Toast.makeText(context, "Entry recorded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleDelete(id: String) {
        scope.launch(Dispatchers.IO) {
            repository.deleteThought(id)
            withContext(Dispatchers.Main) {
                loadThoughts()
                Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Ledger Entry", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    // Filter logic
    val filteredThoughts = remember(thoughts, searchQuery, selectedCategoryFilter, selectedDateFilter) {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L

        thoughts.filter { item ->
            // Category filter
            val itemCat = (item.tags ?: item.topic ?: "quick").lowercase()
            val matchesCategory = if (selectedCategoryFilter == "all") true
            else if (selectedCategoryFilter == "quick") (itemCat == "quick" || itemCat == "thought" || itemCat.isEmpty())
            else itemCat.contains(selectedCategoryFilter.lowercase())

            // Date filter
            val matchesDate = when (selectedDateFilter) {
                LedgerDateFilter.ALL -> true
                LedgerDateFilter.TODAY -> item.date == todayStr
                LedgerDateFilter.YESTERDAY -> item.date == yesterdayStr
                LedgerDateFilter.WEEK -> item.timestamp >= sevenDaysAgo
            }

            // Search query
            val matchesSearch = if (searchQuery.isBlank()) true
            else {
                val q = searchQuery.trim().lowercase()
                (item.content.lowercase().contains(q) ||
                 (item.topic ?: "").lowercase().contains(q) ||
                 (item.tags ?: "").lowercase().contains(q))
            }

            matchesCategory && matchesDate && matchesSearch
        }
    }

    val cardShape = RoundedCornerShape(16.dp)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Ledger Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sovereign Journal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sanctuary Ledger",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your personal log of thoughts, reflections, notes & entries.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onNavigateToDatacenter,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(text = "Data Center", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = { handleRecord() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Write", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Write", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // --- 2. In-Stream Quick Capture Card ---
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
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = captureText,
                        onValueChange = { captureText = it },
                        placeholder = {
                            Text(
                                text = "What's on your mind? Capture a thought, note or idea...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 5
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Selector Dropdown Button
                        Box {
                            val activeCatMeta = LEDGER_CATEGORIES.find { it.id == captureCategory } ?: LEDGER_CATEGORIES[0]
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { showCategoryDropdown = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(activeCatMeta.color)
                                    )
                                    Text(
                                        text = "${activeCatMeta.icon} ${activeCatMeta.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                LEDGER_CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(text = cat.icon)
                                                Text(text = cat.label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                            }
                                        },
                                        onClick = {
                                            captureCategory = cat.id
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Submit Record Button
                        Button(
                            onClick = { handleRecord() },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Outlined.Send, contentDescription = "Record", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Record", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // --- 3. Filter, Search & Categories Controls ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search entries by keyword, note, or tag...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                // Filter Category Dropdown & Date Range Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Filter Button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { showFilterDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val filterLabel = if (selectedCategoryFilter == "all") "All Categories"
                                else (LEDGER_CATEGORIES.find { it.id == selectedCategoryFilter }?.let { "${it.icon} ${it.label}" } ?: selectedCategoryFilter)

                                Text(
                                    text = "Filter:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = filterLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFilterDropdown,
                            onDismissRequest = { showFilterDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "All Categories", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)) },
                                onClick = {
                                    selectedCategoryFilter = "all"
                                    showFilterDropdown = false
                                }
                            )
                            LEDGER_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = cat.icon)
                                            Text(text = cat.label, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        selectedCategoryFilter = cat.id
                                        showFilterDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Date range chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LedgerDateFilter.values().forEach { df ->
                            val isSelected = selectedDateFilter == df
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier.clickable { selectedDateFilter = df }
                            ) {
                                Text(
                                    text = df.label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                // Summary info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val catName = if (selectedCategoryFilter == "all") "All Categories" else (LEDGER_CATEGORIES.find { it.id == selectedCategoryFilter }?.label ?: selectedCategoryFilter)
                    Text(
                        text = "Viewing: $catName",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = "${filteredThoughts.size} ${if (filteredThoughts.size == 1) "entry" else "entries"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }

        // --- 4. Unified Entries Stream ---
        if (filteredThoughts.isEmpty()) {
            item {
                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HistoryEdu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Your Ledger is Quiet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "No entries match the selected filter. Type a note in the box above to capture your thoughts.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredThoughts, key = { it.id }) { item ->
                val cat = (item.tags ?: item.topic ?: "quick").lowercase()
                val meta = LEDGER_CATEGORIES.find { it.id == cat } ?: LEDGER_CATEGORIES[0]

                // Time format
                val dateFormatted = when (item.date) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> item.date
                }
                val timeStr = sdfTime.format(Date(item.timestamp))
                val timeDisplay = "$dateFormatted · $timeStr"

                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = meta.color.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, meta.color.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = meta.icon, fontSize = 11.sp)
                                        Text(
                                            text = meta.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = meta.color,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = timeDisplay,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Actions
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { copyToClipboard(item.content) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { handleDelete(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Content
                        if (!item.topic.isNullOrBlank() && item.topic != meta.label && item.topic != "Quick") {
                            Text(
                                text = item.topic ?: "",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            )
                        }

                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
