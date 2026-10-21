package com.v07.v3.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SanctuaryTab(val id: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Outlined.Spa),
    TOOLS("tools", "Tools", Icons.Outlined.GridView),
    LEDGER("journal", "Ledger", Icons.Outlined.HistoryEdu),
    DAY("day", "Day", Icons.Outlined.CalendarToday),
    SETTINGS("settings", "Settings", Icons.Outlined.Settings)
}

enum class SanctuarySubView {
    NONE,
    FOCUS,
    SCREENTIME,
    HABITS,
    TASKS,
    READING,
    GITA,
    MOOD,
    DATACENTER
}

/**
 * Mobile Bottom Navigation Dock faithful to V07 Sanctuary:
 * - 5 core tabs: Home, Tools, Ledger, Day, Settings
 * - Floating rounded pill dock with backdrop elevation and subtle border
 * - Active tab highlighted in primary teal container
 * - Ergonomic touch targets
 */
@Composable
fun SanctuaryBottomDock(
    currentTab: SanctuaryTab,
    onTabSelected: (SanctuaryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = PillShape,
                    spotColor = Color(0x33000000),
                    ambientColor = Color(0x22000000)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = PillShape
                ),
            shape = PillShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SanctuaryTab.values().forEach { tab ->
                    val isActive = currentTab == tab
                    SanctuaryDockItem(
                        tab = tab,
                        isActive = isActive,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SanctuaryDockItem(
    tab: SanctuaryTab,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(targetValue = if (isActive) 1.05f else 1.0f, label = "dockScale")
    val contentColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "dockColor"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(PillShape)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else Color.Transparent
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (isActive) 12.dp else 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.5.sp
                ),
                color = contentColor
            )
        }
    }
}
