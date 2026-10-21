package com.v07.v3.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.V3PlatformHost
import com.v07.v3.core.plugin.PluginRecord
import com.v07.v3.core.plugin.PluginRegistry
import com.v07.v3.core.plugin.PluginState
import com.v07.v3.core.plugin.V3PluginManifest

enum class SanctuaryToolType {
    CORE,     // Sovereign foundational tools: Habits, Focus, Screentime, Ledger, Gita, Data Center
    PLUGIN    // Modular platform plugins: Tasks, Reading, Mood, Gamify, Digest, Running, etc.
}

data class SanctuaryTool(
    val id: String,
    val pluginId: String?,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val accentBg: Color,
    val statusPreview: String,
    val subView: SanctuarySubView = SanctuarySubView.NONE,
    val type: SanctuaryToolType = SanctuaryToolType.CORE
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToSubView: (SanctuarySubView) -> Unit,
    onNavigateToTab: (SanctuaryTab) -> Unit,
    onOpenPlugin: (pluginId: String, title: String, subtitle: String) -> Unit,
    onImportPluginZip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val toolPrefs = remember { context.getSharedPreferences("v07_tool_states", Context.MODE_PRIVATE) }

    var searchQuery by remember { mutableStateOf("") }
    var isManageMode by remember { mutableStateOf(false) }
    var isInactiveExpanded by remember { mutableStateOf(false) }
    var showAddCatalogSheet by remember { mutableStateOf(false) }
    var selectedToolForSettings by remember { mutableStateOf<SanctuaryTool?>(null) }
    var selectedPluginForSheet by remember { mutableStateOf<PluginRecord?>(null) }
    var toolTypeFilter by remember { mutableStateOf("All") } // "All", "Core", "Plugins"

    // Master catalog cleanly separating CORE foundation tools from MODULAR plugins (Habits is explicitly CORE)
    val defaultTools = remember {
        listOf(
            // =================================================================
            // 1. CORE SANCTUARY TOOLS (Permanent Sovereign Substrate)
            // =================================================================
            SanctuaryTool(
                id = "habits",
                pluginId = null,
                title = "Gentle Habits",
                category = "Daily Rhythms",
                description = "Rhythm, cadence, 7-day habit matrix & consistency tracking",
                icon = Icons.Outlined.Repeat,
                accentColor = SanctuaryPalette.AccentHabits,
                accentBg = SanctuaryPalette.AccentHabitsBg,
                statusPreview = "7-Day Cadence Active • Local SQLite",
                subView = SanctuarySubView.HABITS,
                type = SanctuaryToolType.CORE
            ),
            SanctuaryTool(
                id = "focus",
                pluginId = null,
                title = "Focus Sanctuary",
                category = "Presence & Rhythm",
                description = "Deep work interval timer & calm focus",
                icon = Icons.Outlined.HourglassTop,
                accentColor = SanctuaryPalette.AccentChrono,
                accentBg = SanctuaryPalette.AccentChronoBg,
                statusPreview = "∞ Open Flow • Ready",
                subView = SanctuarySubView.FOCUS,
                type = SanctuaryToolType.CORE
            ),
            SanctuaryTool(
                id = "screentime",
                pluginId = null,
                title = "Digital Stillness",
                category = "Telemetry & Limits",
                description = "Daily app attention, 24h & 7d usage bar graphs & mindful limits",
                icon = Icons.Outlined.ScreenLockPortrait,
                accentColor = Color(0xFFF59E0B),
                accentBg = Color(0x1AF59E0B),
                statusPreview = "24h & 7d Bargraphs Active",
                subView = SanctuarySubView.SCREENTIME,
                type = SanctuaryToolType.CORE
            ),
            SanctuaryTool(
                id = "ledger",
                pluginId = null,
                title = "Sanctuary Ledger",
                category = "Reflection & Log",
                description = "Reflections, personal log & insights",
                icon = Icons.Outlined.HistoryEdu,
                accentColor = Color(0xFF8B5CF6),
                accentBg = Color(0x1A8B5CF6),
                statusPreview = "Live reflections & capture",
                subView = SanctuarySubView.NONE,
                type = SanctuaryToolType.CORE
            ),
            SanctuaryTool(
                id = "gita",
                pluginId = null,
                title = "Bhagavad Gita",
                category = "Wisdom & Philosophy",
                description = "Sacred verses, Sanskrit, Hindi commentary & English translations",
                icon = Icons.Outlined.MenuBook,
                accentColor = Color(0xFFEAB308),
                accentBg = Color(0x1AEAB308),
                statusPreview = "Chapter 2, Verse 47",
                subView = SanctuarySubView.GITA,
                type = SanctuaryToolType.CORE
            ),
            SanctuaryTool(
                id = "datacenter",
                pluginId = null,
                title = "Data Center",
                category = "Sovereign Console",
                description = "Sovereign JSON export, import, DB explorer & vault sync",
                icon = Icons.Outlined.Storage,
                accentColor = Color(0xFF0284C7),
                accentBg = Color(0x1A0284C7),
                statusPreview = "100% Local SQLite First",
                subView = SanctuarySubView.DATACENTER,
                type = SanctuaryToolType.CORE
            ),

            // =================================================================
            // 2. MODULAR PLUGINS & EXTENSIONS (Sandboxed Extensible Platform)
            // =================================================================
            SanctuaryTool(
                id = "tasks",
                pluginId = "com.v07.tasks",
                title = "Sovereign Tasks",
                category = "Action Items",
                description = "Daily directives, quiet priorities & deadlines",
                icon = Icons.Outlined.Checklist,
                accentColor = SanctuaryPalette.AccentTasks,
                accentBg = SanctuaryPalette.AccentTasksBg,
                statusPreview = "Directives & focus list",
                type = SanctuaryToolType.PLUGIN
            ),
            SanctuaryTool(
                id = "reading",
                pluginId = "com.v07.reader",
                title = "Sovereign Shelf",
                category = "Literary Shelf",
                description = "Kindle & physical book reading progress",
                icon = Icons.Outlined.AutoStories,
                accentColor = Color(0xFFEC4899),
                accentBg = Color(0x1AEC4899),
                statusPreview = "Reading journey & book notes",
                type = SanctuaryToolType.PLUGIN
            ),
            SanctuaryTool(
                id = "mood",
                pluginId = "com.v07.vitals",
                title = "Mood & Energy",
                category = "Sovereign Check-in",
                description = "Emotional equilibrium & wellness trends",
                icon = Icons.Outlined.Spa,
                accentColor = Color(0xFF10B981),
                accentBg = Color(0x1A10B981),
                statusPreview = "Grounded • Calm energy",
                type = SanctuaryToolType.PLUGIN
            ),
            SanctuaryTool(
                id = "gamify",
                pluginId = "com.v07.gamify",
                title = "Gamify Quests",
                category = "Sanctuary Progression",
                description = "Mindful XP rewards, streak protection & levels",
                icon = Icons.Outlined.MilitaryTech,
                accentColor = Color(0xFF06B6D4),
                accentBg = Color(0x1A06B6D4),
                statusPreview = "Level 7 • 1,420 XP",
                type = SanctuaryToolType.PLUGIN
            ),
            SanctuaryTool(
                id = "chronosynth",
                pluginId = "com.v07.chronosynth",
                title = "Weekly Digest",
                category = "Executive Synthesis",
                description = "AI weekly intelligence & cadence synthesis",
                icon = Icons.Outlined.Insights,
                accentColor = Color(0xFF6366F1),
                accentBg = Color(0x1A6366F1),
                statusPreview = "Weekly Sunday briefing",
                type = SanctuaryToolType.PLUGIN
            ),
            SanctuaryTool(
                id = "running",
                pluginId = "com.v07.running",
                title = "Runtime Tracker",
                category = "Health & Cadence",
                description = "Athletic conditioning, cadence & pace logs",
                icon = Icons.Outlined.DirectionsRun,
                accentColor = Color(0xFF10B981),
                accentBg = Color(0x1A10B981),
                statusPreview = "Pace & cardiovascular log",
                type = SanctuaryToolType.PLUGIN
            ),
            SanctuaryTool(
                id = "timetracker",
                pluginId = "com.v07.timetracker",
                title = "Pomodoro Companion",
                category = "Companion Rhythm",
                description = "Companion web interval timer",
                icon = Icons.Outlined.Timer,
                accentColor = Color(0xFFF97316),
                accentBg = Color(0x1AF97316),
                statusPreview = "Modular interval timer",
                type = SanctuaryToolType.PLUGIN
            )
        )
    }

    // Dynamic state of which tools are enabled vs inactive
    val enabledToolMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            defaultTools.forEach { tool ->
                val isExplicitlyDisabled = toolPrefs.getString(tool.id, "ENABLED") == "DISABLED"
                this[tool.id] = !isExplicitlyDisabled
            }
        }
    }

    fun setToolEnabled(toolId: String, enabled: Boolean) {
        enabledToolMap[toolId] = enabled
        toolPrefs.edit().putString(toolId, if (enabled) "ENABLED" else "DISABLED").apply()

        // Sync with PluginRegistry if applicable
        val tool = defaultTools.find { it.id == toolId }
        val pluginId = tool?.pluginId
        if (pluginId != null && V3PlatformHost.isInitialized) {
            if (enabled) {
                V3PlatformHost.pluginRegistry.enablePlugin(pluginId)
            } else {
                V3PlatformHost.pluginRegistry.disablePlugin(pluginId)
            }
        }

        val name = tool?.title ?: toolId
        val msg = if (enabled) "$name enabled in Kit" else "$name moved to Inactive Companions"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun openTool(tool: SanctuaryTool) {
        when {
            tool.id == "focus" -> onNavigateToSubView(SanctuarySubView.FOCUS)
            tool.id == "screentime" -> onNavigateToSubView(SanctuarySubView.SCREENTIME)
            tool.id == "habits" -> onNavigateToSubView(SanctuarySubView.HABITS)
            tool.id == "ledger" -> onNavigateToTab(SanctuaryTab.LEDGER)
            tool.id == "datacenter" -> onNavigateToSubView(SanctuarySubView.DATACENTER)
            tool.id == "gita" -> onNavigateToSubView(SanctuarySubView.GITA)
            tool.pluginId != null -> onOpenPlugin(tool.pluginId, tool.title, tool.description)
            else -> onNavigateToSubView(tool.subView)
        }
    }

    fun openPluginSettings(tool: SanctuaryTool) {
        val pluginId = tool.pluginId ?: return
        val reg = if (V3PlatformHost.isInitialized) V3PlatformHost.pluginRegistry else PluginRegistry.getInstance(context)
        val existingRecord = reg.getPlugin(pluginId)

        if (existingRecord != null) {
            selectedPluginForSheet = existingRecord
        } else {
            // Synthesize record from tool metadata
            val synthManifest = V3PluginManifest(
                id = pluginId,
                name = tool.title,
                version = "3.0.0",
                description = tool.description,
                publisher = "V07 Core Team",
                permissions = setOf("storage:isolated", "events:read", "events:write", "telemetry:read", "notifications")
            )
            val synthRecord = PluginRecord(
                manifest = synthManifest,
                state = if (enabledToolMap[tool.id] ?: true) PluginState.ENABLED else PluginState.DISABLED,
                isBuiltIn = true
            )
            selectedPluginForSheet = synthRecord
        }
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Default") }

    val categories = remember(defaultTools) {
        listOf("All") + defaultTools.map { it.category }.distinct()
    }
    val sortOptions = remember {
        listOf("Default", "Name (A-Z)", "Active First")
    }

    val activeTools = remember(enabledToolMap.toMap(), searchQuery, selectedCategory, selectedSort, toolTypeFilter) {
        val filtered = defaultTools.filter { tool ->
            val matchesType = when (toolTypeFilter) {
                "Core" -> tool.type == SanctuaryToolType.CORE
                "Plugins" -> tool.type == SanctuaryToolType.PLUGIN
                else -> true
            }
            matchesType &&
                    (enabledToolMap[tool.id] ?: true) &&
                    (selectedCategory == "All" || tool.category.equals(selectedCategory, ignoreCase = true)) &&
                    (searchQuery.isBlank() || tool.title.contains(searchQuery, ignoreCase = true) || tool.description.contains(searchQuery, ignoreCase = true))
        }
        when (selectedSort) {
            "Name (A-Z)" -> filtered.sortedBy { it.title }
            else -> filtered
        }
    }

    val coreTools = remember(activeTools) { activeTools.filter { it.type == SanctuaryToolType.CORE } }
    val pluginTools = remember(activeTools) { activeTools.filter { it.type == SanctuaryToolType.PLUGIN } }

    val inactiveTools = remember(enabledToolMap.toMap(), searchQuery, selectedCategory, toolTypeFilter) {
        defaultTools.filter { tool ->
            val matchesType = when (toolTypeFilter) {
                "Core" -> tool.type == SanctuaryToolType.CORE
                "Plugins" -> tool.type == SanctuaryToolType.PLUGIN
                else -> true
            }
            matchesType &&
                    !(enabledToolMap[tool.id] ?: true) &&
                    (selectedCategory == "All" || tool.category.equals(selectedCategory, ignoreCase = true)) &&
                    (searchQuery.isBlank() || tool.title.contains(searchQuery, ignoreCase = true) || tool.description.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
    ) {
        // =====================================================================
        // 1. Kit Header with Add & Manage Actions
        // =====================================================================
        item {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tool Kit",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Surface(shape = PillShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                            Text(
                                text = "${activeTools.size} active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { isManageMode = !isManageMode },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isManageMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isManageMode) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isManageMode) Icons.Outlined.Check else Icons.Outlined.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isManageMode) "Done" else "Manage", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = { showAddCatalogSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Core sovereign foundation tools and extensible companion plugins.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // =====================================================================
        // 2. Segmented Scope Filter (All / Core Tools / Modular Plugins)
        // =====================================================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filterOptions = listOf(
                    Triple("All", "All (${activeTools.size})", Icons.Outlined.Dashboard),
                    Triple("Core", "Core Tools (${coreTools.size})", Icons.Outlined.Stars),
                    Triple("Plugins", "Modular Plugins (${pluginTools.size})", Icons.Outlined.Extension)
                )

                filterOptions.forEach { (key, label, icon) ->
                    val isSelected = toolTypeFilter == key
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { toolTypeFilter = key }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. Search Bar
        // =====================================================================
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tools, habits, or plugins...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // =====================================================================
        // 4. Category & Sort Selectors
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SanctuaryFilterDropdown(
                    label = "Category:",
                    selectedItem = selectedCategory,
                    items = categories,
                    onItemSelected = { selectedCategory = it },
                    leadingIcon = Icons.Outlined.Category,
                    modifier = Modifier.weight(1f)
                )

                SanctuaryFilterDropdown(
                    label = "Sort:",
                    selectedItem = selectedSort,
                    items = sortOptions,
                    onItemSelected = { selectedSort = it },
                    leadingIcon = Icons.Outlined.Sort
                )
            }
        }

        // =====================================================================
        // SECTION A: CORE SANCTUARY TOOLS (Permanent Foundation)
        // =====================================================================
        if (coreTools.isNotEmpty() && (toolTypeFilter == "All" || toolTypeFilter == "Core")) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CORE SANCTUARY TOOLS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x203B82F6)
                        ) {
                            Text(
                                text = "CORE SYSTEM • SQLite",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            items(coreTools, key = { it.id }) { tool ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                        .clickable {
                            if (!isManageMode) openTool(tool)
                        },
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tool.accentBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.title,
                                        tint = tool.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = tool.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x1A3B82F6)
                                        ) {
                                            Text(
                                                text = "CORE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF60A5FA)
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tool.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            if (isManageMode) {
                                Switch(
                                    checked = enabledToolMap[tool.id] ?: true,
                                    onCheckedChange = { checked ->
                                        setToolEnabled(tool.id, checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(tool.accentColor)
                                )
                                Text(
                                    text = tool.statusPreview,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { selectedToolForSettings = tool },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Tune,
                                        contentDescription = "Preferences",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Button(
                                    onClick = { openTool(tool) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Open", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // SECTION B: MODULAR PLUGINS & EXTENSIONS (Extensible Platform)
        // =====================================================================
        if (pluginTools.isNotEmpty() && (toolTypeFilter == "All" || toolTypeFilter == "Plugins")) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "MODULAR PLUGINS & EXTENSIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x2010B981)
                        ) {
                            Text(
                                text = "MODULAR • SANDBOXED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            items(pluginTools, key = { it.id }) { tool ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                        .clickable {
                            if (!isManageMode) openTool(tool)
                        },
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tool.accentBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.title,
                                        tint = tool.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = tool.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x1A10B981)
                                        ) {
                                            Text(
                                                text = "PLUGIN",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tool.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            if (isManageMode) {
                                Switch(
                                    checked = enabledToolMap[tool.id] ?: true,
                                    onCheckedChange = { checked ->
                                        setToolEnabled(tool.id, checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(tool.accentColor)
                                )
                                Text(
                                    text = tool.statusPreview,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Direct Plugin Settings Gear Button (fulfills "implement setting for all plugins")
                                IconButton(
                                    onClick = { openPluginSettings(tool) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = "Plugin Settings",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Button(
                                    onClick = { openTool(tool) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Launch", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 5. Inactive / Disabled Companions Accordion
        // =====================================================================
        if (inactiveTools.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isInactiveExpanded = !isInactiveExpanded },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "INACTIVE COMPANIONS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Surface(
                                    shape = PillShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${inactiveTools.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isInactiveExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (isInactiveExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            inactiveTools.forEach { tool ->
                                Surface(
                                    shape = CardShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(tool.accentBg.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(tool.icon, contentDescription = null, tint = tool.accentColor.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                            }
                                            Column {
                                                Text(tool.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp))
                                                Text("${if (tool.type == SanctuaryToolType.CORE) "Core System" else "Plugin"} • Inactive", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                            }
                                        }

                                        Button(
                                            onClick = { setToolEnabled(tool.id, true) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Enable", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
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

    // =========================================================================
    // 6. Universal Plugin Settings Sheet (Settings for all plugins)
    // =========================================================================
    selectedPluginForSheet?.let { record ->
        PluginSettingsSheet(
            pluginRecord = record,
            onDismiss = { selectedPluginForSheet = null },
            onStateChanged = { enabled ->
                val tool = defaultTools.find { it.pluginId == record.manifest.id }
                if (tool != null) {
                    setToolEnabled(tool.id, enabled)
                }
            }
        )
    }

    // =========================================================================
    // 7. Add Tool & Plugin Catalog Bottom Sheet
    // =========================================================================
    if (showAddCatalogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCatalogSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Add Tools & Companions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Enable modular plugins or import custom packages", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    IconButton(onClick = { showAddCatalogSheet = false }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                // Import ZIP button
                Surface(
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAddCatalogSheet = false
                            onImportPluginZip()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Import Plugin (.zip)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Install an offline V3 plugin bundle from local storage", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                }

                if (inactiveTools.isNotEmpty()) {
                    Text("Inactive Starter Companions", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        inactiveTools.forEach { tool ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(tool.icon, contentDescription = null, tint = tool.accentColor, modifier = Modifier.size(18.dp))
                                    Text(tool.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Button(
                                    onClick = { setToolEnabled(tool.id, true) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Add to Kit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // =========================================================================
    // 8. Core Tool Preferences Dialog
    // =========================================================================
    selectedToolForSettings?.let { tool ->
        AlertDialog(
            onDismissRequest = { selectedToolForSettings = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(tool.icon, contentDescription = null, tint = tool.accentColor)
                    Text("${tool.title} Preferences", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (tool.type == SanctuaryToolType.CORE) Color(0x203B82F6) else Color(0x2010B981)
                        ) {
                            Text(
                                text = if (tool.type == SanctuaryToolType.CORE) "CORE SYSTEM TOOL" else "MODULAR PLUGIN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tool.type == SanctuaryToolType.CORE) Color(0xFF60A5FA) else Color(0xFF34D399)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text("Category: ${tool.category}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Text(tool.description, style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Status: ${tool.statusPreview}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    Text("Module Substrate: ${if (tool.type == SanctuaryToolType.CORE) "Native Jetpack Compose • Local SQLite" else (tool.pluginId ?: "Modular Sandbox")}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp))
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedToolForSettings = null }) {
                    Text("Done")
                }
            }
        )
    }
}
