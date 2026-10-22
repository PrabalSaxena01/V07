package com.v07.v3.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.notification.NotificationPriority
import com.v07.v3.app.notification.SanctuaryNotificationItem
import com.v07.v3.app.notification.SanctuaryNotificationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryNotificationsSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    notificationManager: SanctuaryNotificationManager,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onNavigateToCadence: () -> Unit = {}
) {
    if (!isOpen) return

    val notifications by notificationManager.notifications.collectAsState()
    val unreadCount by notificationManager.unreadCount.collectAsState()
    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to "All",
        "tasks" to "Directives",
        "focus" to "Focus",
        "cadence" to "Cadence",
        "screentime" to "Stillness"
    )

    val filteredList = remember(notifications, selectedCategory) {
        if (selectedCategory == "all") {
            notifications
        } else {
            notifications.filter { it.category == selectedCategory }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sanctuary Feed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (unreadCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 1.dp)
                                ) {
                                    Text(
                                        text = "$unreadCount NEW",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Directives, daily cadence & wellbeing alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Action Icons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = { notificationManager.markAllAsRead() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all read",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = { notificationManager.clearAll() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catId, catLabel) ->
                    val isSelected = selectedCategory == catId
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = catId }
                    ) {
                        Text(
                            text = catLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Notifications List / Empty State
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = "Quiet Sanctuary",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Quiet Sanctuary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "All directives and check-ins are clear.\nEnjoy undisturbed calm and focused flow.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            onMarkRead = { notificationManager.markAsRead(item.id) },
                            onDismiss = { notificationManager.dismiss(item.id) },
                            onAction = {
                                notificationManager.markAsRead(item.id)
                                onDismiss()
                                when (item.actionType) {
                                    "COMPLETE_TASK" -> onNavigateToTasks()
                                    "START_FOCUS" -> onNavigateToFocus()
                                    "CHECK_IN", "CHECK_OUT" -> onNavigateToCadence()
                                    else -> {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: SanctuaryNotificationItem,
    onMarkRead: () -> Unit,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    val (icon, iconColor) = when (item.category) {
        "tasks" -> Icons.Outlined.CheckCircle to Color(0xFFF59E0B) // Amber
        "focus" -> Icons.Outlined.HourglassTop to Color(0xFF14B8A6) // Teal
        "cadence" -> Icons.Outlined.Spa to Color(0xFFA855F7) // Purple
        "screentime" -> Icons.Outlined.Smartphone to Color(0xFF3B82F6) // Blue
        else -> Icons.Outlined.Info to Color(0xFF10B981)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (item.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(
            1.dp,
            if (!item.isRead) iconColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onMarkRead() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f))
                    .border(1.dp, iconColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.category,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Message Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(iconColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Optional Action Pill
                if (item.actionType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = iconColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAction() }
                    ) {
                        Text(
                            text = when (item.actionType) {
                                "COMPLETE_TASK" -> "Execute Task →"
                                "START_FOCUS" -> "Begin Flow →"
                                "CHECK_IN" -> "Morning Check-In →"
                                "CHECK_OUT" -> "Evening Reflection →"
                                else -> "Open Action →"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Dismiss Button (44px target)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
