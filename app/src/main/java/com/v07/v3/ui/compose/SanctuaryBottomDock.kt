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
    COCKPIT("cockpit", "Cockpit", Icons.Outlined.Dashboard),
    CADENCE("habits", "Cadence", Icons.Outlined.Repeat),
    FOCUS("chrono", "Focus", Icons.Outlined.Timer),
    TASKS("tasks", "Tasks", Icons.Outlined.Checklist),
    JOURNAL("journal", "Journal", Icons.Outlined.EditNote)
}

/**
 * Mobile Bottom Navigation Dock faithful to the V3 main branch:
 * - 5 core tabs: Cockpit, Cadence, Focus, Tasks, Journal
 * - Floating rounded pill dock with elevation and subtle border
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
                    elevation = 10.dp,
                    shape = PillShape,
                    spotColor = Color(0x33000000),
                    ambientColor = Color(0x22000000)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = PillShape
                ),
            shape = PillShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 6.dp)
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
                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
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
