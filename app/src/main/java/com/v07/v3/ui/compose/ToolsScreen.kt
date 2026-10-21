package com.v07.v3.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.V3PlatformHost
import com.v07.v3.core.plugin.PluginRegistry
import com.v07.v3.core.plugin.PluginState

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
    val subView: SanctuarySubView = SanctuarySubView.NONE
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

    // Master list of all 12 sanctuary tools & companions
    val defaultTools = remember {
        listOf(
            SanctuaryTool(
                id = "focus",
                pluginId = "com.v07.timetracker",
                title = "Focus Sanctuary",
                category = "Presence & Rhythm",
                description = "Deep work interval timer & calm focus",
                icon = Icons.Outlined.HourglassTop,
                accentColor = SanctuaryPalette.AccentChrono,
                accentBg = SanctuaryPalette.AccentChronoBg,
                statusPreview = "∞ Open Flow • Ready",
                subView = SanctuarySubView.FOCUS
            ),
            SanctuaryTool(
                id = "screentime",
                pluginId = null,
                title = "Digital Stillness",
                category = "Telemetry & Limits",
                description = "Daily app attention & mindful limits",
                icon = Icons.Outlined.ScreenLockPortrait,
                accentColor = Color(0xFFF59E0B),
                accentBg = Color(0x1AF59E0B),
                statusPreview = "Mindful usage boundaries",
                subView = SanctuarySubView.SCREENTIME
            ),
            SanctuaryTool(
                id = "habits",
                pluginId = "com.v07.habits",
                title = "Gentle Habits",
                category = "Daily Rhythms",
                description = "Rhythm, cadence & consistency tracking",
                icon = Icons.Outlined.Repeat,
                accentColor = SanctuaryPalette.AccentHabits,
                accentBg = SanctuaryPalette.AccentHabitsBg,
                statusPreview = "140-day cadence active"
            ),
            SanctuaryTool(
                id = "tasks",
                pluginId = "com.v07.tasks",
                title = "Sovereign Tasks",
                category = "Action Items",
                description = "Daily directives, quiet priorities & deadlines",
                icon = Icons.Outlined.Checklist,
                accentColor = SanctuaryPalette.AccentTasks,
                accentBg = SanctuaryPalette.AccentTasksBg,
                statusPreview = "Directives & focus list"
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
                subView = SanctuarySubView.NONE
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
                statusPreview = "Reading journey & book notes"
            ),
            SanctuaryTool(
                id = "gita",
                pluginId = "com.v07.gita",
                title = "Bhagavad Gita",
                category = "Wisdom & Philosophy",
                description = "Sacred verses, Sanskrit & commentaries",
                icon = Icons.Outlined.MenuBook,
                accentColor = Color(0xFFEAB308),
                accentBg = Color(0x1AEAB308),
                statusPreview = "Chapter 2, Verse 47"
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
                statusPreview = "Grounded • Calm energy"
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
                statusPreview = "Level 7 • 1,420 XP"
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
                statusPreview = "Weekly Sunday briefing"
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
                statusPreview = "Pace & cardiovascular log"
            ),
            SanctuaryTool(
                id = "datacenter",
                pluginId = null,
                title = "Data Center",
                category = "Sovereign Console",
                description = "Sovereign JSON export, import & vault sync",
                icon = Icons.Outlined.Storage,
                accentColor = Color(0xFF0284C7),
                accentBg = Color(0x1A0284C7),
                statusPreview = "100% Local SQLite First",
                subView = SanctuarySubView.DATACENTER
            )
        )
    }

    // Dynamic state of which tools are enabled vs inactive
    val enabledToolMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            defaultTools.forEach { tool ->
                // Check SharedPreferences (default true, except optional starter tools default true)
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
            tool.id == "ledger" -> onNavigateToTab(SanctuaryTab.LEDGER)
            tool.id == "datacenter" -> onNavigateToSubView(SanctuarySubView.DATACENTER)
            tool.pluginId != null -> onOpenPlugin(tool.pluginId, tool.title, tool.description)
            else -> onNavigateToSubView(tool.subView)
        }
    }

    val activeTools = remember(enabledToolMap.toMap(), searchQuery) {
        defaultTools.filter { tool ->
            (enabledToolMap[tool.id] ?: true) &&
                    (searchQuery.isBlank() || tool.title.contains(searchQuery, ignoreCase = true) || tool.description.contains(searchQuery, ignoreCase = true))
        }
    }

    val inactiveTools = remember(enabledToolMap.toMap(), searchQuery) {
        defaultTools.filter { tool ->
            !(enabledToolMap[tool.id] ?: true) &&
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
                    text = "Calm modules for reflection, focus, and mindful presence.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // =====================================================================
        // 2. Manage Kit Info Banner (Shown only when in Manage Mode)
        // =====================================================================
        if (isManageMode) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Toggle switches to enable or hide tools from your sanctuary kit.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Button(
                            onClick = { isManageMode = false },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Done", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
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
                placeholder = { Text("Search companions or modules...", fontSize = 12.sp) },
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
        // 4. Active Tools Cards
        // =====================================================================
        if (activeTools.isEmpty()) {
            item {
                Surface(
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                        }
                        Text("Your Kit is Serene & Empty", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "No tools are currently active in this view. Enable tools below or import custom companions.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(activeTools, key = { it.id }) { tool ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                        .clickable {
                            if (!isManageMode) {
                                openTool(tool)
                            }
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
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = tool.category,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

                        // Bottom status bar & actions
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
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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
                                        contentDescription = "Customize",
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
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
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
                                                Text("${tool.category} • Inactive", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
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
    // 6. Add Tool & Plugin Catalog Bottom Sheet
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
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
    // 7. Contextual Tool Settings Dialog
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
                    Text("Category: ${tool.category}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text(tool.description, style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Status: ${tool.statusPreview}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    Text("Plugin Package: ${tool.pluginId ?: "Native Sanctuary Core Module"}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp))
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
