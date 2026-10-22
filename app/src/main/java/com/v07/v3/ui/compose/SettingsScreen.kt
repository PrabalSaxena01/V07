package com.v07.v3.ui.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.V3PlatformHost
import com.v07.v3.app.overlay.V3BubbleOverlayService
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.plugin.PluginRecord
import com.v07.v3.core.plugin.PluginRegistry
import com.v07.v3.core.plugin.PluginState
import com.v07.v3.core.sync.V3CompanionSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    isLight: Boolean = false,
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { EventRepository.getInstance(context) }
    val tracker = remember { ScreenTimeTracker(context, repository) }
    val syncManager = remember { V3CompanionSyncManager.getInstance(context, repository, tracker) }
    val scope = rememberCoroutineScope()

    var isOverlayEnabled by remember { mutableStateOf(V3BubbleOverlayService.isOverlayEnabled(context)) }
    var hasUsagePermission by remember { mutableStateOf(tracker.hasUsageStatsPermission()) }
    var hasOverlayPermission by remember {
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true)
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var isCloudSyncEnabled by remember { mutableStateOf(syncManager.isCloudSyncEnabled()) }
    var companionUidInput by remember { mutableStateOf(syncManager.getCustomCompanionUid() ?: com.v07.v3.core.sync.V3CompanionSyncManager.DEFAULT_COMPANION_UID) }
    var isSyncingNow by remember { mutableStateOf(false) }
    var isMigratingNow by remember { mutableStateOf(false) }
    var lastSyncFormatted by remember { mutableStateOf(syncManager.getSyncStatus().optString("last_synced_formatted", "Never")) }
    var syncStatusText by remember { mutableStateOf(if (syncManager.isCloudSyncEnabled()) "Ready to sync with cloud" else "Offline Guest Mode") }

    val profilePrefs = remember { context.getSharedPreferences("v07_profiles", Context.MODE_PRIVATE) }
    var activeProfileId by remember { mutableStateOf(profilePrefs.getString("active_profile_id", "guest") ?: "guest") }
    var profileDisplayName by remember { mutableStateOf(profilePrefs.getString("profile_display_name", "Sanctuary Thinker") ?: "Sanctuary Thinker") }
    var profileBio by remember { mutableStateOf(profilePrefs.getString("profile_bio", "Local Vault") ?: "Local Vault") }
    var profileAvatarSymbol by remember { mutableStateOf(profilePrefs.getString("profile_avatar_symbol", "S") ?: "S") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    var showCloudAuthDialog by remember { mutableStateOf(false) }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authCreateMode by remember { mutableStateOf(false) }
    var authErrorMsg by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }
    var authenticatedUser by remember { mutableStateOf(syncManager.getAuthenticatedUser()) }

    fun refreshProfile() {
        activeProfileId = profilePrefs.getString("active_profile_id", "guest") ?: "guest"
        profileDisplayName = profilePrefs.getString("profile_display_name", "Sanctuary Thinker") ?: "Sanctuary Thinker"
        profileBio = profilePrefs.getString("profile_bio", "Local Vault") ?: "Local Vault"
        profileAvatarSymbol = profilePrefs.getString("profile_avatar_symbol", "S") ?: "S"
        authenticatedUser = syncManager.getAuthenticatedUser()
        isCloudSyncEnabled = syncManager.isCloudSyncEnabled()
        companionUidInput = syncManager.getTargetUid()
    }

    var pluginsList by remember { mutableStateOf<List<PluginRecord>>(emptyList()) }
    var selectedPluginForConfig by remember { mutableStateOf<PluginRecord?>(null) }

    fun refreshPlugins() {
        val reg = if (V3PlatformHost.isInitialized) V3PlatformHost.pluginRegistry else PluginRegistry.getInstance(context)
        pluginsList = reg.getAllPlugins().ifEmpty { reg.discoverAll() }
            .filter { !it.manifest.id.contains("template") }
    }

    LaunchedEffect(Unit) {
        refreshPlugins()
    }

    val cardShape = RoundedCornerShape(16.dp)

    fun refreshPermissions() {
        hasUsagePermission = tracker.hasUsageStatsPermission()
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
        isOverlayEnabled = V3BubbleOverlayService.isOverlayEnabled(context)
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun toggleOverlay() {
        if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "Please grant overlay permission", Toast.LENGTH_SHORT).show()
            return
        }
        val newState = !isOverlayEnabled
        V3BubbleOverlayService.setOverlayEnabled(context, newState)
        if (newState) {
            V3BubbleOverlayService.start(context)
            Toast.makeText(context, "Screen Time Bubble enabled", Toast.LENGTH_SHORT).show()
        } else {
            V3BubbleOverlayService.stop(context)
            Toast.makeText(context, "Screen Time Bubble disabled", Toast.LENGTH_SHORT).show()
        }
        isOverlayEnabled = newState
    }

    fun triggerSync() {
        isSyncingNow = true
        syncStatusText = "Syncing with cloud..."
        if (!isCloudSyncEnabled) {
            syncManager.enableCloudSync(companionUidInput)
            isCloudSyncEnabled = true
        }
        scope.launch(Dispatchers.IO) {
            try {
                syncManager.syncNow { success, msg ->
                    scope.launch(Dispatchers.Main) {
                        isSyncingNow = false
                        lastSyncFormatted = syncManager.getSyncStatus().optString("last_synced_formatted", "Just now")
                        syncStatusText = msg ?: if (success) "Synchronized just now" else "Sync failed"
                        Toast.makeText(context, syncStatusText, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSyncingNow = false
                    syncStatusText = "Sync error: ${e.message}"
                    Toast.makeText(context, syncStatusText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun pullCompanionData() {
        isMigratingNow = true
        syncStatusText = "Pulling remote companion data..."
        scope.launch(Dispatchers.IO) {
            try {
                syncManager.migrateWebCompanionData(companionUidInput) { success, msg, _ ->
                    scope.launch(Dispatchers.Main) {
                        isMigratingNow = false
                        lastSyncFormatted = syncManager.getSyncStatus().optString("last_synced_formatted", "Just now")
                        syncStatusText = msg ?: if (success) "Companion data pulled successfully" else "Pull completed"
                        Toast.makeText(context, syncStatusText, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isMigratingNow = false
                    syncStatusText = "Pull error: ${e.message}"
                    Toast.makeText(context, syncStatusText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- Introductory Framing ---
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sanctuary Preferences",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your space",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Make V07 feel like yours.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // --- Section 1: Your profile ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Encrypted & private",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profileAvatarSymbol,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = if (profileAvatarSymbol.length > 1) 22.sp else 24.sp
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = profileDisplayName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.Verified,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = profileBio,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudDone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Active: $activeProfileId · Local Vault",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Edit profile", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            }

                            FilledTonalButton(
                                onClick = { Toast.makeText(context, "AES-GCM encryption enabled", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(imageVector = Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Vault safety", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
            }
        }

        // --- Section 2: Daily rhythm & notifications ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DAILY RHYTHM & NOTIFICATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily sanctuary notifications",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Quiet prompts for morning check-in and evening reflection.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Morning check-in",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                )
                                Text(text = "09:00 AM", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Evening reflection",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                )
                                Text(text = "09:00 PM", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary))
                            }
                        }
                    }
                }
            }
        }

        // --- Section 3: Appearance & Themes ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "APPEARANCE & THEMES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sanctuary Theme Mode",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (isLight) "Light Sanctuary palette active" else "Dark Sanctuary palette active",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        FilledTonalButton(
                            onClick = onToggleTheme,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isLight) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLight) "Dark" else "Light",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // --- Section 4: Floating Bubble Overlay ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FLOATING BUBBLE OVERLAY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Screen Time Floating Bubble",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Live unobtrusive floating pill on top of all apps displaying real-time app usage and quick focus controls.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Switch(
                                checked = isOverlayEnabled,
                                onCheckedChange = { toggleOverlay() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Button(
                                onClick = { toggleOverlay() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Grant Overlay Permission in Android Settings", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // --- Section 5: V07 Companion Sync ---
        item {
            val isAuthed = authenticatedUser != null
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "V07 COMPANION SYNC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isAuthed) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isAuthed) {
                                if (authenticatedUser?.isAnonymous == true) "ONLINE (ANON)" else "ONLINE SYNC"
                            } else "OFFLINE GUEST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isAuthed) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Toggle Cloud Sync Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Firestore Cloud Sync",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (isCloudSyncEnabled) "Realtime bidirectional sync with web companion active" else "Running in offline local mode",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Switch(
                                checked = isCloudSyncEnabled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        syncManager.enableCloudSync(companionUidInput)
                                        isCloudSyncEnabled = true
                                        triggerSync()
                                    } else {
                                        syncManager.disableCloudSync()
                                        isCloudSyncEnabled = false
                                        syncStatusText = "Offline Guest Mode"
                                        Toast.makeText(context, "Cloud sync disabled · Offline mode active", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                        // Cloud Account Identity Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isAuthed) Icons.Outlined.CheckCircle else Icons.Outlined.CloudOff,
                                        contentDescription = null,
                                        tint = if (isAuthed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isAuthed) "Cloud Account Connected" else "Cloud Account Required",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                if (isAuthed) {
                                    TextButton(
                                        onClick = {
                                            syncManager.signOut()
                                            refreshProfile()
                                            Toast.makeText(context, "Signed out of Cloud Account", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Outlined.Logout, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sign out", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (isAuthed) {
                                Text(
                                    text = "User: ${authenticatedUser?.email ?: "Anonymous Cloud Session"}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "UID: ${authenticatedUser?.uid}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            } else {
                                Text(
                                    text = "Firestore security rules require authentication to sync data. Sign in with your web dashboard account or connect anonymously.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            authErrorMsg = null
                                            showCloudAuthDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Icon(Icons.Outlined.Login, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sign In / Register", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            isAuthLoading = true
                                            syncManager.signInAnonymously { success, err ->
                                                isAuthLoading = false
                                                if (success) {
                                                    refreshProfile()
                                                    Toast.makeText(context, "Connected anonymously to cloud", Toast.LENGTH_SHORT).show()
                                                    triggerSync()
                                                } else {
                                                    Toast.makeText(context, err ?: "Anonymous connection failed", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(38.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        if (isAuthLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Outlined.PersonOutline, null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Guest Connect", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                        // Target Firestore Companion UID input
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Firestore Companion Target UID:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = companionUidInput,
                                    onValueChange = { companionUidInput = it.trim() },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 44.dp),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 11.5.sp
                                    ),
                                    placeholder = {
                                        Text(
                                            text = "e.g. 5fwVgpra4pRzB1QqMWb9LSZSqrp2",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                            )
                                        )
                                    }
                                )
                                Button(
                                    onClick = {
                                        syncManager.setTargetUid(companionUidInput)
                                        isCloudSyncEnabled = true
                                        Toast.makeText(context, "Target UID saved: $companionUidInput", Toast.LENGTH_SHORT).show()
                                        triggerSync()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(48.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        companionUidInput = com.v07.v3.core.sync.V3CompanionSyncManager.DEFAULT_COMPANION_UID
                                        syncManager.setTargetUid(companionUidInput)
                                        isCloudSyncEnabled = true
                                        triggerSync()
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Outlined.Restore, null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Restore Default UID (5fwV...)", fontSize = 10.5.sp)
                                }

                                Text(
                                    text = "Last: $lastSyncFormatted",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                        // Action Buttons: Sync Now & Pull Web Companion
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { triggerSync() },
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSyncingNow,
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isSyncingNow) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Syncing...", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync now", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            FilledTonalButton(
                                onClick = { pullCompanionData() },
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isMigratingNow,
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isMigratingNow) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pulling...", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Outlined.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pull cloud", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = "Status: $syncStatusText",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // --- Section 6: Modular Plugins & Extensions ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MODULAR PLUGINS & EXTENSIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    val activeCount = pluginsList.count { it.state == PluginState.ENABLED || it.state == PluginState.RUNNING }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$activeCount / ${pluginsList.size} active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Configure isolated permissions, sandbox storage, and sync behaviors for modular companion plugins.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        if (pluginsList.isEmpty()) {
                            Text(
                                text = "Discovering installed plugins...",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        } else {
                            pluginsList.forEachIndexed { index, plugin ->
                                val manifest = plugin.manifest
                                val isEnabled = plugin.state == PluginState.ENABLED || plugin.state == PluginState.RUNNING

                                if (index > 0) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Extension,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = manifest.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = "v${manifest.version}",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = manifest.id,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { selectedPluginForConfig = plugin },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Settings,
                                                contentDescription = "Configure ${manifest.name}",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Switch(
                                            checked = isEnabled,
                                            onCheckedChange = { checked ->
                                                val reg = if (V3PlatformHost.isInitialized) V3PlatformHost.pluginRegistry else PluginRegistry.getInstance(context)
                                                if (checked) {
                                                    reg.enablePlugin(manifest.id)
                                                } else {
                                                    reg.disablePlugin(manifest.id)
                                                }
                                                refreshPlugins()
                                                Toast.makeText(
                                                    context,
                                                    "${manifest.name} ${if (checked) "enabled" else "disabled"}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Section 7: Android Device & System Permissions ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ANDROID SYSTEM PERMISSIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Usage access permission row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Usage Access (Screen Time)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (hasUsagePermission) "Granted" else "Action required for app telemetry",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (hasUsagePermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                            if (!hasUsagePermission) {
                                FilledTonalButton(
                                    onClick = {
                                        val intent = tracker.createPermissionIntent()
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = "Grant", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            } else {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Granted", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 1.dp)

                        // App Version
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "App Version",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "v3.0.0-compose (v07-sanct)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    selectedPluginForConfig?.let { plugin ->
        PluginSettingsSheet(
            pluginRecord = plugin,
            onDismiss = { selectedPluginForConfig = null },
            onStateChanged = { refreshPlugins() }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(profileDisplayName) }
        var bioInput by remember { mutableStateOf(profileBio) }
        var selectedGlyph by remember { mutableStateOf(profileAvatarSymbol) }
        var selectedProfile by remember { mutableStateOf(activeProfileId) }
        val existingDbProfiles = remember { com.v07.v3.core.data.V07Database.listProfiles(context) }
        var availableProfiles by remember { mutableStateOf((listOf(com.v07.v3.core.data.V07Database.DEFAULT_PROFILE_ID) + existingDbProfiles).distinct()) }
        var newProfileNameInput by remember { mutableStateOf("") }
        var isAddingNewProfile by remember { mutableStateOf(false) }
        val glyphs = listOf("S", "P", "A", "V", "🪔", "🧘", "⚡", "⚔️", "🌿", "📚", "💎", "🎯", "🛡️")

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(text = "Edit Sanctuary Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        placeholder = {
                            Text(
                                text = "e.g. Sanctuary Thinker",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = bioInput,
                        onValueChange = { bioInput = it },
                        label = { Text("Bio & Focus Role") },
                        placeholder = {
                            Text(
                                text = "e.g. Local Vault",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(text = "Avatar Initial / Glyph:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        glyphs.take(6).forEach { glyph ->
                            val isSel = selectedGlyph == glyph
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .clickable { selectedGlyph = glyph },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = glyph,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        glyphs.drop(6).take(6).forEach { glyph ->
                            val isSel = selectedGlyph == glyph
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .clickable { selectedGlyph = glyph },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = glyph,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    Text(text = "Active Database Profile:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableProfiles.forEach { prof ->
                            val isSel = selectedProfile.equals(prof, ignoreCase = true)
                            Surface(
                                shape = PillShape,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { selectedProfile = prof }
                            ) {
                                Text(
                                    text = prof,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = nameInput.trim().ifBlank { "Sanctuary Thinker" }
                        val trimmedBio = bioInput.trim().ifBlank { "Local Vault" }
                        profilePrefs.edit()
                            .putString("profile_display_name", trimmedName)
                            .putString("profile_bio", trimmedBio)
                            .putString("profile_avatar_symbol", selectedGlyph)
                            .putString("active_profile_id", selectedProfile)
                            .apply()

                        if (!selectedProfile.equals(activeProfileId, ignoreCase = true)) {
                            com.v07.v3.core.data.V07Database.switchProfile(context, selectedProfile)
                        }

                        refreshProfile()
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile updated: $trimmedName", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCloudAuthDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isAuthLoading) showCloudAuthDialog = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (authCreateMode) Icons.Outlined.PersonAdd else Icons.Outlined.Login,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (authCreateMode) "Create Cloud Account" else "Sign In to Cloud Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (authCreateMode)
                            "Create an account to synchronize tasks, habits, and focus sessions with the web dashboard."
                        else
                            "Sign in with your email and password to sync with the V07 Companion Dashboard.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    OutlinedTextField(
                        value = authEmail,
                        onValueChange = { authEmail = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("user@example.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = authPassword,
                        onValueChange = { authPassword = it },
                        label = { Text("Password (6+ chars)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!authErrorMsg.isNullOrBlank()) {
                        Text(
                            text = authErrorMsg!!,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    TextButton(
                        onClick = {
                            authCreateMode = !authCreateMode
                            authErrorMsg = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (authCreateMode) "Already have an account? Sign In" else "New to V07 Cloud? Create Account",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanEmail = authEmail.trim()
                        val cleanPass = authPassword
                        if (cleanEmail.isEmpty() || !cleanEmail.contains("@") || cleanPass.length < 6) {
                            authErrorMsg = "Please enter a valid email and 6+ character password"
                            return@Button
                        }
                        isAuthLoading = true
                        authErrorMsg = null
                        syncManager.signIn(cleanEmail, cleanPass, authCreateMode) { success, error ->
                            isAuthLoading = false
                            if (success) {
                                refreshProfile()
                                showCloudAuthDialog = false
                                Toast.makeText(context, "Connected to cloud successfully!", Toast.LENGTH_SHORT).show()
                                triggerSync()
                            } else {
                                authErrorMsg = error ?: "Authentication failed"
                            }
                        }
                    },
                    enabled = !isAuthLoading,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isAuthLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connecting...")
                    } else {
                        Text(if (authCreateMode) "Create Account" else "Sign In")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isAuthLoading) showCloudAuthDialog = false },
                    enabled = !isAuthLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
