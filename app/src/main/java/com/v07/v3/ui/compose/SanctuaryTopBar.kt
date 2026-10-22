package com.v07.v3.ui.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.feedback.SanctuaryFeedbackHelper
import com.v07.v3.core.data.V07Database

/**
 * Top App Bar faithful to the V3 main branch:
 * - Brand Box with 'spa' icon in primary teal
 * - "V07 SANCTUARY" bold headline & "v3.0 Calm Sanctuary" subtitle
 * - Notification trigger, Theme Switcher & Profile modal avatar button
 */
@Composable
fun SanctuaryTopBar(
    isLight: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var profileDropdownExpanded by remember { mutableStateOf(false) }

    var profileChangeCounter by remember { mutableStateOf(0) }

    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("v07_profiles", Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            profileChangeCounter++
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val profileInitial = remember(profileDropdownExpanded, profileChangeCounter) {
        val prefs = context.getSharedPreferences("v07_profiles", Context.MODE_PRIVATE)
        prefs.getString("profile_avatar_symbol", null)?.ifBlank { null }
            ?: prefs.getString("profile_display_name", null)?.trim()?.firstOrNull()?.uppercase()
            ?: run {
                val active = prefs.getString("active_profile_id", null) ?: V07Database.getCurrentProfileId()
                if (active.isNotBlank() && active != "guest" && active != "default") {
                    active.replace("_", " ").trim().firstOrNull()?.uppercase()
                } else null
            }
    }

    var activeProfileName by remember(profileDropdownExpanded, profileChangeCounter) {
        val prefs = context.getSharedPreferences("v07_profiles", Context.MODE_PRIVATE)
        mutableStateOf(prefs.getString("active_profile_id", "guest") ?: "guest")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Title & Sanctuary Pill (Faithful to main index.html)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Spa,
                        contentDescription = "V07 Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "V07 SANCTUARY",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "v3.0 Calm Sanctuary",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Actions: Notification + Profile Modal Trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val notificationManager = remember { com.v07.v3.app.notification.SanctuaryNotificationManager.getInstance(context) }
                val unreadCount by notificationManager.unreadCount.collectAsState()

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable {
                            SanctuaryFeedbackHelper.performClick(context)
                            onNotificationClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                // Theme Switcher Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable {
                            SanctuaryFeedbackHelper.performClick(context)
                            onToggleTheme()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLight) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                SanctuaryFeedbackHelper.performClick(context)
                                onProfileClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileInitial != null) {
                            Text(
                                text = profileInitial,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        // Presence dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SanctuaryPalette.AccentHabits)
                        )
                    }

                    /* Single profile: direct navigation to settings */
                    if (false) SanctuaryProfileDropdownMenu(
                        expanded = profileDropdownExpanded,
                        onDismissRequest = { profileDropdownExpanded = false },
                        onNavigateToSettings = {
                            profileDropdownExpanded = false
                            onProfileClick()
                        },
                        onProfileSwitched = { newProfile ->
                            activeProfileName = newProfile
                            android.widget.Toast.makeText(context, "Switched to profile: $newProfile", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
