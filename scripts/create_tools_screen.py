code = """package com.v07.v3.ui.compose

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SanctuaryTool(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val accentBg: Color,
    val subView: SanctuarySubView
)

@Composable
fun ToolsScreen(
    onNavigateToSubView: (SanctuarySubView) -> Unit,
    onNavigateToTab: (SanctuaryTab) -> Unit,
    onImportPluginZip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isManageMode by remember { mutableStateOf(false) }

    val allTools = remember {
        listOf(
            SanctuaryTool("focus", "Focus Sanctuary", "Deep work interval timer & calm focus", Icons.Outlined.HourglassTop, SanctuaryPalette.AccentChrono, SanctuaryPalette.AccentChronoBg, SanctuarySubView.FOCUS),
            SanctuaryTool("screentime", "Digital Stillness", "Daily app attention & mindful limits", Icons.Outlined.ScreenLockPortrait, Color(0xFFF59E0B), Color(0x1AF59E0B), SanctuarySubView.SCREENTIME),
            SanctuaryTool("habits", "Gentle Habits", "Rhythm, cadence & consistency tracking", Icons.Outlined.Repeat, SanctuaryPalette.AccentHabits, SanctuaryPalette.AccentHabitsBg, SanctuarySubView.HABITS),
            SanctuaryTool("tasks", "Sovereign Tasks", "Daily directives, quiet priorities & deadlines", Icons.Outlined.Checklist, SanctuaryPalette.AccentTasks, SanctuaryPalette.AccentTasksBg, SanctuarySubView.TASKS),
            SanctuaryTool("ledger", "Sanctuary Ledger", "Reflections, personal log & insights", Icons.Outlined.HistoryEdu, Color(0xFF8B5CF6), Color(0x1A8B5CF6), SanctuarySubView.NONE),
            SanctuaryTool("reading", "Sovereign Shelf", "Kindle & physical book reading progress", Icons.Outlined.AutoStories, Color(0xFFEC4899), Color(0x1AEC4899), SanctuarySubView.READING),
            SanctuaryTool("gita", "Bhagavad Gita", "Sacred verses, Sanskrit & commentaries", Icons.Outlined.MenuBook, Color(0xFFEAB308), Color(0x1AEAB308), SanctuarySubView.GITA),
            SanctuaryTool("mood", "Mood & Energy", "Emotional equilibrium & wellness trends", Icons.Outlined.Spa, Color(0xFF10B981), Color(0x1A10B981), SanctuarySubView.MOOD),
            SanctuaryTool("datacenter", "Data Center", "Sovereign JSON export, import & vault sync", Icons.Outlined.Storage, Color(0xFF0284C7), Color(0x1A0284C7), SanctuarySubView.DATACENTER)
        )
    }

    val filteredTools = remember(searchQuery) {
        if (searchQuery.isBlank()) allTools
        else allTools.filter { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
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
                                text = "${allTools.size} active",
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
                            Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isManageMode) "Done" else "Manage", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = { onImportPluginZip() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Outlined.FolderZip, contentDescription = null, modifier = Modifier.size(15.dp))
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
        // 2. Search Bar
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
        // 3. Tools Grid / Cards
        // =====================================================================
        items(filteredTools) { tool ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                    .clickable {
                        if (tool.id == "ledger") {
                            onNavigateToTab(SanctuaryTab.LEDGER)
                        } else {
                            onNavigateToSubView(tool.subView)
                        }
                    },
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
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

                        Column {
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tool.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
"""

with open(r"D:/software/V07-Android/app/src/main/java/com/v07/v3/ui/compose/ToolsScreen.kt", "w", encoding="utf-8") as f:
    f.write(code)

print("Created ToolsScreen.kt successfully.")
