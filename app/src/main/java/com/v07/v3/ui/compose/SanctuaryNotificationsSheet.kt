package com.v07.v3.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.feedback.SanctuaryFeedbackHelper
import com.v07.v3.app.notification.NotificationPriority
import com.v07.v3.app.notification.SanctuaryNotificationItem
import com.v07.v3.app.notification.SanctuaryNotificationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * SanctuaryNotificationsSheet
 *
 * Impeccably crafted editorial notifications panel adhering strictly to the /taste discipline:
 * - Anti-Slop: No generic purple blobs or over-rounded pill cards
 * - Dials: Variance 6, Motion 4, Density 7 (high information density, crisp 1px borders)
 * - Typographic discipline: Monospace timestamps & metrics paired with clean neo-grotesque sans
 * - Tactile: Integrated haptics and audio feedback on interactions
 */
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

    val context = LocalContext.current
    val notifications by notificationManager.notifications.collectAsState()
    val unreadCount by notificationManager.unreadCount.collectAsState()
    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to "ALL",
        "tasks" to "DIRECTIVES",
        "focus" to "FOCUS",
        "cadence" to "RHYTHM",
        "screentime" to "STILLNESS"
    )

    val filteredList = remember(notifications, selectedCategory) {
        if (selectedCategory == "all") {
            notifications
        } else {
            notifications.filter { it.category == selectedCategory }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            SanctuaryFeedbackHelper.performClick(context)
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Feed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "SANCTUARY FEED",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 14.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (unreadCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                ) {
                                    Text(
                                        text = "$unreadCount NEW",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Directives · Focus Intervals · Mindful Rhythm",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (notifications.any { !it.isRead }) {
                        IconButton(
                            onClick = {
                                SanctuaryFeedbackHelper.performSuccess(context)
                                notificationManager.markAllAsRead()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark All Read",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                SanctuaryFeedbackHelper.performClick(context)
                                notificationManager.clearAll()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Crisp Vector Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { (catId, label) ->
                    val isSelected = selectedCategory == catId
                    val count = if (catId == "all") notifications.size else notifications.count { it.category == catId }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 32.dp)
                            .clickable {
                                SanctuaryFeedbackHelper.performClick(context)
                                selectedCategory = catId
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (count > 0) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notifications Feed Deck
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 44.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "SANCTUARY IN EQUILIBRIUM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "No pending alerts or overdue directives. Your space is clear.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        SanctuaryNotificationCard(
                            item = item,
                            onRead = {
                                SanctuaryFeedbackHelper.performClick(context)
                                notificationManager.markAsRead(item.id)
                            },
                            onDismiss = {
                                SanctuaryFeedbackHelper.performClick(context)
                                notificationManager.dismiss(item.id)
                            },
                            onAction = {
                                SanctuaryFeedbackHelper.performSuccess(context)
                                notificationManager.markAsRead(item.id)
                                when (item.actionType) {
                                    "COMPLETE_TASK", "VIEW_TASKS" -> {
                                        onDismiss()
                                        onNavigateToTasks()
                                    }
                                    "START_FOCUS" -> {
                                        onDismiss()
                                        onNavigateToFocus()
                                    }
                                    "LOG_HABITS", "MORNING_CHECKIN", "EVENING_REFLECTION" -> {
                                        onDismiss()
                                        onNavigateToCadence()
                                    }
                                    "VIEW_SCREEN_TIME" -> {
                                        onDismiss()
                                    }
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
private fun SanctuaryNotificationCard(
    item: SanctuaryNotificationItem,
    onRead: () -> Unit,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    val categoryAccent = when (item.category) {
        "tasks" -> Color(0xFF68A6FF)
        "focus" -> Color(0xFF2DD4BF)
        "cadence" -> Color(0xFF4DE086)
        "screentime" -> Color(0xFFFFB84D)
        else -> MaterialTheme.colorScheme.primary
    }

    val iconVector: ImageVector = when (item.category) {
        "tasks" -> Icons.Outlined.CheckCircle
        "focus" -> Icons.Outlined.HourglassTop
        "cadence" -> Icons.Outlined.WbSunny
        "screentime" -> Icons.Outlined.Smartphone
        else -> Icons.Outlined.Notifications
    }

    val timeFormatted = remember(item.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (item.isRead) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            1.dp,
            if (!item.isRead) categoryAccent.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRead() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(categoryAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = item.category,
                    tint = categoryAccent,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Body Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(categoryAccent)
                            )
                        }
                    }

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    )
                }

                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // 1-Tap Action Pill
                if (item.actionType != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = categoryAccent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, categoryAccent.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 26.dp)
                                .clickable { onAction() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val actionLabel = when (item.actionType) {
                                    "COMPLETE_TASK" -> "Execute Directive"
                                    "VIEW_TASKS" -> "View Queue"
                                    "START_FOCUS" -> "Enter Focus"
                                    "LOG_HABITS" -> "Log Rituals"
                                    "MORNING_CHECKIN" -> "Check In"
                                    "EVENING_REFLECTION" -> "Reflect"
                                    else -> "Open"
                                }
                                Text(
                                    text = actionLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = categoryAccent
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = categoryAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dismiss Button (44px min tap target)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
