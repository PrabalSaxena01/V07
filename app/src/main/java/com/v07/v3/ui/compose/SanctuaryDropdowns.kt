package com.v07.v3.ui.compose

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.core.data.V07Database

/**
 * SanctuaryFilterDropdown
 *
 * Highly tactile, compact pill dropdown selector for category filters, status filters,
 * sort order, and week selection across V3 screens.
 */
@Composable
fun <T> SanctuaryFilterDropdown(
    selectedItem: T,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    itemLabel: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)
                .clickable { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                if (!label.isNullOrBlank()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Text(
                    text = itemLabel(selectedItem),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(item),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * SanctuaryFormDropdown
 *
 * Form-field style dropdown input for dialogs, modals, and settings.
 * Includes a subtle label, outlined container, and selection dropdown menu.
 */
@Composable
fun <T> SanctuaryFormDropdown(
    label: String,
    selectedItem: T,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    itemLabel: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { expanded = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = itemLabel(selectedItem),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                items.forEach { item ->
                    val isSelected = item == selectedItem
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemLabel(item),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

/**
 * SanctuaryProfileDropdownMenu
 *
 * Dropdown menu for the top bar profile avatar, faithful to the profile switcher in the old APK.
 */
@Composable
fun SanctuaryProfileDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onProfileSwitched: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("v07_profiles", Context.MODE_PRIVATE) }
    var activeProfileId by remember(expanded) {
        mutableStateOf(prefs.getString("active_profile_id", "guest") ?: "guest")
    }

    // Dynamic profiles available in Sanctuary SQLite
    val profiles = remember(expanded) {
        (listOf(V07Database.DEFAULT_PROFILE_ID) + V07Database.listProfiles(context)).distinct()
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .width(240.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        // Header info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "SANCTUARY PROFILE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = "Encrypted Local Space",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = SanctuaryPalette.AccentHabits
                )
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Profiles list
        if (false) profiles.forEach { profile ->
            val isSelected = profile.equals(activeProfileId, ignoreCase = true)
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = profile,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (isSelected) {
                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    activeProfileId = profile
                    V07Database.switchProfile(context, profile)
                    onProfileSwitched(profile)
                    onDismissRequest()
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Settings / Cloud Sync action
        DropdownMenuItem(
            text = {
                Text(
                    text = "Profile Settings & Cloud Sync",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            },
            onClick = {
                onDismissRequest()
                onNavigateToSettings()
            }
        )
    }
}
