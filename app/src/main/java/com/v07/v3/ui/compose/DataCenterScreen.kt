package com.v07.v3.ui.compose

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.V3PlatformHost
import com.v07.v3.core.data.DatabaseManager
import com.v07.v3.core.data.V3DatabaseStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun DataCenterScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var dbStats by remember { mutableStateOf<V3DatabaseStats?>(null) }
    var isVacuuming by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var integrityStatus by remember { mutableStateOf("Checking...") }

    fun refreshStats() {
        coroutineScope.launch(Dispatchers.IO) {
            val mgr = DatabaseManager.getInstance(context)
            val stats = mgr.getStats()
            val integrity = mgr.checkIntegrity()
            withContext(Dispatchers.Main) {
                dbStats = stats
                integrityStatus = integrity
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier.padding(start = 2.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Data Center",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Sovereign JSON, local SQLite & vault sync",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = { refreshStats() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 48.dp)
        ) {
            // Storage Engine Status Card
            item {
                Surface(
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Storage Architecture", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Surface(shape = PillShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                                Text(
                                    text = "100% Local SQLite First",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Your records reside exclusively in your sovereign local SQLite database. Telemetry and insights remain on-device without third-party surveillance.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Database Integrity", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(integrityStatus, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (integrityStatus == "ok") Color(0xFF10B981) else MaterialTheme.colorScheme.error))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Database File Size", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            val sizeKb = (dbStats?.fileSizeBytes ?: 0L) / 1024
                            Text("${sizeKb} KB", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Real-Time Table Records
            item {
                Surface(
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Sovereign Entity Counts", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                        val counts = dbStats?.tableCounts ?: emptyMap()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Journal Reflections & Notes", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["thoughts"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Deep Focus Sessions", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["sessions"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sovereign Directives / Tasks", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["tasks"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gentle Habit Logs", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["day_tracker"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("App Usage Telemetry Rows", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["app_usage"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Vault Operations", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    // Sync Now
                    Button(
                        onClick = {
                            if (isSyncing) return@Button
                            isSyncing = true
                            syncMessage = "Syncing with Companion Vault..."
                            if (V3PlatformHost.isInitialized) {
                                V3PlatformHost.syncManager.syncNow { success, err ->
                                    coroutineScope.launch(Dispatchers.Main) {
                                        isSyncing = false
                                        syncMessage = if (success) "Sync complete: Vault updated" else "Sync failed: $err"
                                        refreshStats()
                                    }
                                }
                            } else {
                                isSyncing = false
                                syncMessage = "Platform host not ready"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSyncing) "Syncing..." else "Sync with Companion Vault Now", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    if (syncMessage != null) {
                        Text(
                            text = syncMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // VACUUM & Optimize
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                isVacuuming = true
                                val ok = DatabaseManager.getInstance(context).vacuum()
                                withContext(Dispatchers.Main) {
                                    isVacuuming = false
                                    Toast.makeText(context, if (ok) "VACUUM optimization complete" else "VACUUM failed", Toast.LENGTH_SHORT).show()
                                    refreshStats()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isVacuuming) "Optimizing..." else "Run VACUUM & Defragment Pages")
                    }

                    // Create Local Backup
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val file = DatabaseManager.getInstance(context).createBackup()
                                withContext(Dispatchers.Main) {
                                    if (file != null) {
                                        Toast.makeText(context, "Snapshot saved to: ${file.name}", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Backup creation failed", Toast.LENGTH_SHORT).show()
                                    }
                                    refreshStats()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Snapshot Backup to Storage")
                    }

                    // Export Sovereign JSON
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val dbMgr = DatabaseManager.getInstance(context)
                                    val exportObj = JSONObject()
                                    exportObj.put("export_version", 3)
                                    exportObj.put("exported_at", System.currentTimeMillis())
                                    exportObj.put("thoughts", dbMgr.getTableData("thoughts", 1000).getJSONArray("rows"))
                                    exportObj.put("sessions", dbMgr.getTableData("sessions", 1000).getJSONArray("rows"))
                                    exportObj.put("tasks", dbMgr.getTableData("tasks", 1000).getJSONArray("rows"))

                                    withContext(Dispatchers.Main) {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, exportObj.toString(2))
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Export Sovereign JSON")
                                        context.startActivity(shareIntent)
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Sovereign JSON Archive")
                    }
                }
            }
        }
    }
}
