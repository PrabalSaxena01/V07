package com.v07.v3.ui.compose

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.v07.v3.app.V3PlatformHost
import com.v07.v3.core.data.DatabaseManager
import com.v07.v3.core.data.V3DatabaseStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ColumnSchema(
    val name: String,
    val type: String,
    val notNull: Boolean,
    val isPk: Boolean,
    val defaultValue: String?
)

data class QuickEditFieldData(
    val tableName: String,
    val colName: String,
    val currentVal: String,
    val pkCol: String,
    val pkVal: String
)

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

    // Table Explorer & CRUD State
    var tableNames by remember { mutableStateOf<List<String>>(listOf("events", "tasks", "thoughts", "sessions", "day_tracker", "app_usage", "plugin_storage")) }
    var selectedTable by remember { mutableStateOf("events") }
    var selectedLimit by remember { mutableStateOf(25) }
    val limitOptions = remember { listOf(25, 50, 100, 200) }
    var tableSearchQuery by remember { mutableStateOf("") }
    var tableRows by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var totalTableRows by remember { mutableStateOf(0) }
    var isLoadingTable by remember { mutableStateOf(false) }
    var currentTableSchema by remember { mutableStateOf<List<ColumnSchema>>(emptyList()) }

    // Dialog States
    var showAddRowDialog by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<Map<String, String>?>(null) }
    var deletingRow by remember { mutableStateOf<Map<String, String>?>(null) }
    var quickEditField by remember { mutableStateOf<QuickEditFieldData?>(null) }

    // SQLite Studio / Mode: 0 = Table Explorer & CRUD, 1 = SQL Script & Console Runner
    var dbMode by remember { mutableStateOf(0) }

    // SQL Console State
    var sqlQueryText by remember { mutableStateOf("SELECT * FROM events ORDER BY timestamp DESC LIMIT 10;") }
    var isExecutingSql by remember { mutableStateOf(false) }
    var sqlResult by remember { mutableStateOf<JSONObject?>(null) }
    var sqlError by remember { mutableStateOf<String?>(null) }
    var sqlViewMode by remember { mutableStateOf(0) } // 0 = Table Grid, 1 = Raw JSON

    // Detected table from SQL Query for direct row editing
    val detectedTable = remember(sqlQueryText, tableNames) {
        val match = Regex("""\bFROM\s+["`]?([a-zA-Z0-9_]+)["`]?\b""", RegexOption.IGNORE_CASE).find(sqlQueryText)
        val candidate = match?.groupValues?.get(1)
        if (candidate != null && tableNames.any { it.equals(candidate, ignoreCase = true) }) {
            tableNames.first { it.equals(candidate, ignoreCase = true) }
        } else null
    }

    fun loadTableSchema(table: String, onLoaded: ((List<ColumnSchema>) -> Unit)? = null) {
        coroutineScope.launch(Dispatchers.IO) {
            val mgr = DatabaseManager.getInstance(context)
            val schemaArray = mgr.getTableSchema(table)
            val list = mutableListOf<ColumnSchema>()
            for (i in 0 until schemaArray.length()) {
                val obj = schemaArray.optJSONObject(i) ?: continue
                list.add(
                    ColumnSchema(
                        name = obj.optString("name"),
                        type = obj.optString("type", "TEXT").uppercase(),
                        notNull = obj.optBoolean("notnull", false),
                        isPk = obj.optBoolean("pk", false),
                        defaultValue = if (obj.isNull("dflt_value")) null else obj.optString("dflt_value")
                    )
                )
            }
            withContext(Dispatchers.Main) {
                currentTableSchema = list
                onLoaded?.invoke(list)
            }
        }
    }

    fun loadTableRecords(table: String, limit: Int, search: String = "") {
        coroutineScope.launch(Dispatchers.IO) {
            isLoadingTable = true
            val mgr = DatabaseManager.getInstance(context)
            val names = mgr.getAllTableNames()
            val result = mgr.getTableData(
                tableName = table,
                limit = limit,
                offset = 0,
                search = if (search.isBlank()) null else search
            )
            val total = result.optInt("total_count", result.optInt("total", 0))
            val dataArray = result.optJSONArray("rows") ?: result.optJSONArray("data") ?: JSONArray()
            val list = mutableListOf<Map<String, String>>()
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.optJSONObject(i) ?: continue
                val map = mutableMapOf<String, String>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = if (obj.isNull(k)) "null" else obj.optString(k, "")
                }
                list.add(map)
            }
            withContext(Dispatchers.Main) {
                if (names.isNotEmpty()) tableNames = names
                totalTableRows = total
                tableRows = list
                isLoadingTable = false
            }
        }
    }

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

    fun runSqlQuery(sql: String) {
        val trimmed = sql.trim()
        if (trimmed.isBlank()) {
            Toast.makeText(context, "Please enter a SQL query", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch(Dispatchers.IO) {
            isExecutingSql = true
            sqlError = null
            sqlResult = null
            val mgr = DatabaseManager.getInstance(context)
            val res = mgr.executeSql(trimmed)
            withContext(Dispatchers.Main) {
                isExecutingSql = false
                if (res.optBoolean("ok", false)) {
                    sqlResult = res
                    if (!res.optBoolean("is_query", false)) {
                        refreshStats()
                        loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
                    }
                } else {
                    sqlError = res.optString("error", "Execution failed")
                }
            }
        }
    }

    fun findPrimaryKeyCol(schema: List<ColumnSchema>, row: Map<String, String>): String {
        val pkFromSchema = schema.firstOrNull { it.isPk }?.name
        if (pkFromSchema != null && pkFromSchema in row) return pkFromSchema
        if ("id" in row) return "id"
        if ("key" in row) return "key"
        if ("plugin_id" in row) return "plugin_id"
        return row.keys.firstOrNull() ?: "id"
    }

    fun insertRecord(values: Map<String, String>, schema: List<ColumnSchema>) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val mgr = DatabaseManager.getInstance(context)
                val json = JSONObject()
                for ((colName, strVal) in values) {
                    val colDef = schema.firstOrNull { it.name == colName }
                    val trimmed = strVal.trim()
                    if (trimmed.isEmpty()) {
                        if (colDef?.isPk == true) {
                            // Let SQLite autoincrement PK
                            continue
                        }
                        if (colDef?.notNull == true) {
                            if (colDef.type.contains("INT") || colDef.type.contains("REAL")) {
                                continue
                            } else {
                                json.put(colName, "")
                            }
                        } else {
                            json.put(colName, JSONObject.NULL)
                        }
                    } else {
                        val colType = colDef?.type ?: "TEXT"
                        when {
                            colType.contains("INT") -> {
                                val num = trimmed.toLongOrNull()
                                if (num != null) json.put(colName, num) else json.put(colName, trimmed)
                            }
                            colType.contains("REAL") || colType.contains("FLOAT") || colType.contains("DOUBLE") -> {
                                val num = trimmed.toDoubleOrNull()
                                if (num != null) json.put(colName, num) else json.put(colName, trimmed)
                            }
                            colType.contains("BOOL") -> {
                                val boolVal = trimmed.equals("true", true) || trimmed == "1"
                                json.put(colName, if (boolVal) 1 else 0)
                            }
                            else -> {
                                json.put(colName, trimmed)
                            }
                        }
                    }
                }
                val ok = mgr.insertTableRow(selectedTable, json)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        Toast.makeText(context, "Row inserted into $selectedTable", Toast.LENGTH_SHORT).show()
                        showAddRowDialog = false
                        loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
                        refreshStats()
                    } else {
                        Toast.makeText(context, "Failed to insert row", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Insert error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun updateRecord(pkCol: String, pkVal: String, values: Map<String, String>, schema: List<ColumnSchema>) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val mgr = DatabaseManager.getInstance(context)
                val json = JSONObject()
                for ((colName, strVal) in values) {
                    if (colName == pkCol) continue
                    val colDef = schema.firstOrNull { it.name == colName }
                    val trimmed = strVal.trim()
                    if (trimmed.isEmpty()) {
                        if (colDef?.notNull == true) {
                            if (colDef.type.contains("INT") || colDef.type.contains("REAL")) {
                                continue
                            } else {
                                json.put(colName, "")
                            }
                        } else {
                            json.put(colName, JSONObject.NULL)
                        }
                    } else {
                        val colType = colDef?.type ?: "TEXT"
                        when {
                            colType.contains("INT") -> {
                                val num = trimmed.toLongOrNull()
                                if (num != null) json.put(colName, num) else json.put(colName, trimmed)
                            }
                            colType.contains("REAL") || colType.contains("FLOAT") || colType.contains("DOUBLE") -> {
                                val num = trimmed.toDoubleOrNull()
                                if (num != null) json.put(colName, num) else json.put(colName, trimmed)
                            }
                            colType.contains("BOOL") -> {
                                val boolVal = trimmed.equals("true", true) || trimmed == "1"
                                json.put(colName, if (boolVal) 1 else 0)
                            }
                            else -> {
                                json.put(colName, trimmed)
                            }
                        }
                    }
                }
                val ok = mgr.updateTableRow(selectedTable, pkCol, pkVal, json)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        Toast.makeText(context, "Row updated in $selectedTable", Toast.LENGTH_SHORT).show()
                        editingRow = null
                        loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
                        refreshStats()
                        if (sqlQueryText.isNotBlank()) {
                            runSqlQuery(sqlQueryText)
                        }
                    } else {
                        Toast.makeText(context, "Failed to update row", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Update error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun updateSingleField(data: QuickEditFieldData, newVal: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val mgr = DatabaseManager.getInstance(context)
                val json = JSONObject().apply {
                    if (newVal.trim().isEmpty()) {
                        put(data.colName, JSONObject.NULL)
                    } else {
                        val colDef = currentTableSchema.firstOrNull { it.name == data.colName }
                        val colType = colDef?.type ?: "TEXT"
                        val trimmed = newVal.trim()
                        when {
                            colType.contains("INT") -> {
                                val num = trimmed.toLongOrNull()
                                if (num != null) put(data.colName, num) else put(data.colName, trimmed)
                            }
                            colType.contains("REAL") || colType.contains("FLOAT") || colType.contains("DOUBLE") -> {
                                val num = trimmed.toDoubleOrNull()
                                if (num != null) put(data.colName, num) else put(data.colName, trimmed)
                            }
                            colType.contains("BOOL") -> {
                                val boolVal = trimmed.equals("true", true) || trimmed == "1"
                                put(data.colName, if (boolVal) 1 else 0)
                            }
                            else -> put(data.colName, trimmed)
                        }
                    }
                }
                val ok = mgr.updateTableRow(data.tableName, data.pkCol, data.pkVal, json)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        Toast.makeText(context, "${data.colName} updated in ${data.tableName}", Toast.LENGTH_SHORT).show()
                        quickEditField = null
                        loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
                        refreshStats()
                        if (sqlQueryText.isNotBlank()) {
                            runSqlQuery(sqlQueryText)
                        }
                    } else {
                        Toast.makeText(context, "Failed to update ${data.colName}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Update error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun deleteRecord(pkCol: String, pkVal: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val mgr = DatabaseManager.getInstance(context)
                val ok = mgr.deleteTableRow(selectedTable, pkCol, pkVal)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        Toast.makeText(context, "Row deleted from $selectedTable", Toast.LENGTH_SHORT).show()
                        deletingRow = null
                        loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
                        refreshStats()
                        if (sqlQueryText.isNotBlank()) {
                            runSqlQuery(sqlQueryText)
                        }
                    } else {
                        Toast.makeText(context, "Failed to delete row", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Delete error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        refreshStats()
        loadTableSchema(selectedTable)
        loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
    }

    LaunchedEffect(selectedTable, selectedLimit) {
        loadTableSchema(selectedTable)
        loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
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
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Local JSON, SQLite & vault sync",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            refreshStats()
                            loadTableRecords(selectedTable, selectedLimit, tableSearchQuery)
                        },
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
                                    text = "Local SQLite First",
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
                            text = "Your records reside exclusively in your local SQLite database. Telemetry and insights remain on-device without third-party surveillance.",
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
                        Text("Vault Entity Counts", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

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
                            Text("Directives & Tasks", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["tasks"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ritual & Core Events", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["events"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("App Usage Telemetry Rows", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["app_usage"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recycle Bin Soft-Deleted Items", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["recycle_bin"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B)))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plugin Key-Value Store", style = MaterialTheme.typography.bodySmall)
                            Text("${counts["plugin_storage"] ?: 0}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }

            // SQLite Studio & Database Editor Card
            item {
                Surface(
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header: Title and records count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "SQLite Studio & Editor",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Text(
                                text = "$totalTableRows records",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Segmented Mode Switcher: Records CRUD vs SQL Console
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                onClick = { dbMode = 0 },
                                shape = RoundedCornerShape(8.dp),
                                color = if (dbMode == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                                tonalElevation = if (dbMode == 0) 2.dp else 0.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.TableChart,
                                        contentDescription = null,
                                        tint = if (dbMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Table Explorer (CRUD)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (dbMode == 0) FontWeight.Bold else FontWeight.Medium,
                                            color = if (dbMode == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Surface(
                                onClick = { dbMode = 1 },
                                shape = RoundedCornerShape(8.dp),
                                color = if (dbMode == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                                tonalElevation = if (dbMode == 1) 2.dp else 0.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Terminal,
                                        contentDescription = null,
                                        tint = if (dbMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SQL Console (Runner)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (dbMode == 1) FontWeight.Bold else FontWeight.Medium,
                                            color = if (dbMode == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        if (dbMode == 0) {
                            // MODE 0: Table Explorer & CRUD Records Editor
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Select Table & Select Limit Dropdowns
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SanctuaryFilterDropdown(
                                        label = "Table:",
                                        selectedItem = selectedTable,
                                        items = tableNames,
                                        onItemSelected = { selectedTable = it },
                                        leadingIcon = Icons.Outlined.TableChart,
                                        modifier = Modifier.weight(1f)
                                    )

                                    SanctuaryFilterDropdown(
                                        label = "Limit:",
                                        selectedItem = "${selectedLimit} rows",
                                        items = limitOptions.map { "$it rows" },
                                        onItemSelected = { selectedLimit = it.removeSuffix(" rows").toInt() },
                                        leadingIcon = Icons.Outlined.FormatListNumbered
                                    )
                                }

                                // Search Field & + Add Row Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = tableSearchQuery,
                                        onValueChange = {
                                            tableSearchQuery = it
                                            loadTableRecords(selectedTable, selectedLimit, it)
                                        },
                                        placeholder = { Text("Search records in $selectedTable...", fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        trailingIcon = {
                                            if (tableSearchQuery.isNotBlank()) {
                                                IconButton(onClick = {
                                                    tableSearchQuery = ""
                                                    loadTableRecords(selectedTable, selectedLimit, "")
                                                }) {
                                                    Icon(
                                                        Icons.Outlined.Clear,
                                                        contentDescription = "Clear",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp)
                                    )

                                    Button(
                                        onClick = {
                                            loadTableSchema(selectedTable) {
                                                showAddRowDialog = true
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Row", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                if (isLoadingTable) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    }
                                } else if (tableRows.isEmpty()) {
                                    Text(
                                        text = if (tableSearchQuery.isNotBlank())
                                            "No rows matched '$tableSearchQuery' in '$selectedTable'."
                                        else
                                            "Table '$selectedTable' contains no rows.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        tableRows.forEachIndexed { idx, row ->
                                            val pkCol = findPrimaryKeyCol(currentTableSchema, row)
                                            val pkVal = row[pkCol] ?: "#$idx"

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        loadTableSchema(selectedTable) {
                                                            editingRow = row
                                                        }
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                                modifier = Modifier.weight(1f, fill = false)
                                                            ) {
                                                                Text(
                                                                    text = "$pkCol: $pkVal",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        color = MaterialTheme.colorScheme.primary,
                                                                        fontSize = 11.sp
                                                                    ),
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                            Text(
                                                                text = "#${idx + 1} of ${tableRows.size}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    fontSize = 10.sp
                                                                )
                                                            )
                                                        }

                                                        // Action Buttons: Edit and Delete
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            IconButton(
                                                                onClick = {
                                                                    loadTableSchema(selectedTable) {
                                                                        editingRow = row
                                                                    }
                                                                },
                                                                modifier = Modifier.size(32.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Outlined.Edit,
                                                                    contentDescription = "Edit Row",
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    loadTableSchema(selectedTable) {
                                                                        deletingRow = row
                                                                    }
                                                                },
                                                                modifier = Modifier.size(32.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Outlined.DeleteOutline,
                                                                    contentDescription = "Delete Row",
                                                                    tint = MaterialTheme.colorScheme.error,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    // Column preview values (tap any field to quick-edit directly)
                                                    row.entries.filter { it.key != pkCol }.take(8).forEach { (k, v) ->
                                                        val isNull = v == "null"
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color.Transparent,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    quickEditField = QuickEditFieldData(
                                                                        tableName = selectedTable,
                                                                        colName = k,
                                                                        currentVal = if (isNull) "" else v,
                                                                        pkCol = pkCol,
                                                                        pkVal = pkVal
                                                                    )
                                                                }
                                                                .padding(vertical = 1.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "$k:",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                        fontSize = 11.sp
                                                                    )
                                                                )
                                                                Text(
                                                                    text = v,
                                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                                        fontSize = 11.5.sp,
                                                                        fontStyle = if (isNull) FontStyle.Italic else FontStyle.Normal,
                                                                        color = if (isNull) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                                                else MaterialTheme.colorScheme.onSurface
                                                                    ),
                                                                    maxLines = 2,
                                                                    overflow = TextOverflow.Ellipsis,
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                                Icon(
                                                                    imageVector = Icons.Outlined.Edit,
                                                                    contentDescription = "Quick edit $k",
                                                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // MODE 1: Interactive SQL Console & Script Runner
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Execute queries, PRAGMA checks, or SQL statements directly against SQLite:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )

                                // Preset SQL Chips
                                Text(
                                    text = "Quick Presets:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                val sqlPresets = remember {
                                    listOf(
                                        "SELECT * FROM events ORDER BY timestamp DESC LIMIT 10;",
                                        "SELECT * FROM tasks WHERE completed = 0;",
                                        "SELECT * FROM thoughts ORDER BY id DESC LIMIT 10;",
                                        "SELECT * FROM sessions ORDER BY start_time DESC LIMIT 10;",
                                        "SELECT name, type FROM sqlite_master WHERE type='table';",
                                        "PRAGMA table_info(events);",
                                        "PRAGMA integrity_check;",
                                        "SELECT COUNT(*) AS total_rows FROM app_usage;",
                                        "VACUUM;"
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    sqlPresets.forEach { preset ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                            modifier = Modifier.clickable { sqlQueryText = preset }
                                        ) {
                                            Text(
                                                text = preset.take(30) + if (preset.length > 30) "..." else "",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // SQL Input Text Field
                                OutlinedTextField(
                                    value = sqlQueryText,
                                    onValueChange = { sqlQueryText = it },
                                    placeholder = {
                                        Text(
                                            text = "Enter SQL statement (e.g. SELECT * FROM events;)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                        )
                                    },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    trailingIcon = {
                                        if (sqlQueryText.isNotBlank()) {
                                            IconButton(onClick = { sqlQueryText = "" }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Clear,
                                                    contentDescription = "Clear",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    minLines = 4,
                                    maxLines = 8,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Execute & Reset Controls Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { runSqlQuery(sqlQueryText) },
                                        enabled = !isExecutingSql && sqlQueryText.isNotBlank(),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        if (isExecutingSql) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Executing...")
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Run SQL Query", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            sqlQueryText = ""
                                            sqlResult = null
                                            sqlError = null
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset")
                                    }
                                }

                                // Error Banner if execution failed
                                if (sqlError != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ErrorOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = sqlError!!,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Results Grid & JSON display
                                if (sqlResult != null) {
                                    val result = sqlResult!!
                                    val isQuery = result.optBoolean("is_query", false)
                                    val elapsedMs = result.optLong("execution_time_ms", 0L)

                                    if (!isQuery) {
                                        val affected = result.optInt("affected_rows", 0)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "Query executed in $elapsedMs ms • Rows affected: $affected",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        val rowCount = result.optInt("row_count", 0)
                                        val columnsArray = result.optJSONArray("columns") ?: JSONArray()
                                        val rowsArray = result.optJSONArray("rows") ?: JSONArray()
                                        val columns = remember(result) {
                                            (0 until columnsArray.length()).map { columnsArray.getString(it) }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$rowCount rows in $elapsedMs ms",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                )

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    FilterChip(
                                                        selected = sqlViewMode == 0,
                                                        onClick = { sqlViewMode = 0 },
                                                        label = { Text("Grid", fontSize = 11.sp) },
                                                        modifier = Modifier.height(28.dp)
                                                    )
                                                    FilterChip(
                                                        selected = sqlViewMode == 1,
                                                        onClick = { sqlViewMode = 1 },
                                                        label = { Text("JSON", fontSize = 11.sp) },
                                                        modifier = Modifier.height(28.dp)
                                                    )
                                                }
                                            }

                                            if (rowCount == 0) {
                                                Text(
                                                    text = "Query returned 0 rows.",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            } else if (sqlViewMode == 0) {
                                                if (detectedTable != null) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Edit,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Text(
                                                            text = "Table '$detectedTable' recognized · Click any row or edit icon to edit entry directly",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        )
                                                    }
                                                }

                                                // 2D Scrollable Table Grid
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(max = 380.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .horizontalScroll(rememberScrollState())
                                                            .verticalScroll(rememberScrollState())
                                                    ) {
                                                        Column {
                                                            // Header Row
                                                            Row(
                                                                modifier = Modifier
                                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                if (detectedTable != null) {
                                                                    Text(
                                                                        text = "ACTION",
                                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            color = MaterialTheme.colorScheme.primary,
                                                                            fontSize = 11.sp
                                                                        ),
                                                                        modifier = Modifier
                                                                            .width(60.dp)
                                                                            .padding(horizontal = 4.dp)
                                                                    )
                                                                }
                                                                columns.forEach { colName ->
                                                                    Text(
                                                                        text = colName,
                                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            color = MaterialTheme.colorScheme.primary
                                                                        ),
                                                                        modifier = Modifier
                                                                            .widthIn(min = 100.dp, max = 220.dp)
                                                                            .padding(horizontal = 8.dp)
                                                                    )
                                                                }
                                                            }

                                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                                                            // Data Rows
                                                            for (i in 0 until rowsArray.length()) {
                                                                val rowObj = rowsArray.optJSONObject(i) ?: JSONObject()
                                                                val isEven = i % 2 == 0
                                                                val rowMap = remember(rowObj) {
                                                                    val map = mutableMapOf<String, String>()
                                                                    val keys = rowObj.keys()
                                                                    while (keys.hasNext()) {
                                                                        val k = keys.next()
                                                                        map[k] = if (rowObj.isNull(k)) "null" else rowObj.optString(k, "")
                                                                    }
                                                                    map
                                                                }

                                                                Row(
                                                                    modifier = Modifier
                                                                        .background(
                                                                            if (isEven) Color.Transparent
                                                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                                                        )
                                                                        .clickable(enabled = detectedTable != null) {
                                                                            if (detectedTable != null) {
                                                                                selectedTable = detectedTable
                                                                                loadTableSchema(detectedTable) {
                                                                                    editingRow = rowMap
                                                                                }
                                                                            }
                                                                        }
                                                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    if (detectedTable != null) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .width(60.dp)
                                                                                .padding(horizontal = 4.dp),
                                                                            contentAlignment = Alignment.CenterStart
                                                                        ) {
                                                                            IconButton(
                                                                                onClick = {
                                                                                    selectedTable = detectedTable
                                                                                    loadTableSchema(detectedTable) {
                                                                                        editingRow = rowMap
                                                                                    }
                                                                                },
                                                                                modifier = Modifier.size(28.dp)
                                                                            ) {
                                                                                Icon(
                                                                                    imageVector = Icons.Outlined.Edit,
                                                                                    contentDescription = "Edit Row in $detectedTable",
                                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                                    modifier = Modifier.size(15.dp)
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                    columns.forEach { colName ->
                                                                        val cellVal = if (rowObj.isNull(colName)) "null" else rowObj.optString(colName, "")
                                                                        val isNull = rowObj.isNull(colName)
                                                                        Text(
                                                                            text = cellVal,
                                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                                fontFamily = FontFamily.Monospace,
                                                                                fontSize = 11.5.sp,
                                                                                fontStyle = if (isNull) FontStyle.Italic else FontStyle.Normal,
                                                                                color = if (isNull) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                                                        else MaterialTheme.colorScheme.onSurface
                                                                            ),
                                                                            maxLines = 3,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                            modifier = Modifier
                                                                                .widthIn(min = 100.dp, max = 220.dp)
                                                                                .padding(horizontal = 8.dp)
                                                                        )
                                                                    }
                                                                }
                                                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                // JSON Mode
                                                val clipboardManager = LocalClipboardManager.current
                                                val jsonStr = remember(rowsArray) { rowsArray.toString(2) }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End
                                                        ) {
                                                            OutlinedButton(
                                                                onClick = {
                                                                    clipboardManager.setText(AnnotatedString(jsonStr))
                                                                    Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                                                },
                                                                modifier = Modifier.height(30.dp),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Copy JSON", fontSize = 11.sp)
                                                            }
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .heightIn(max = 280.dp)
                                                                .verticalScroll(rememberScrollState())
                                                        ) {
                                                            Text(
                                                                text = jsonStr,
                                                                style = MaterialTheme.typography.bodySmall.copy(
                                                                    fontFamily = FontFamily.Monospace,
                                                                    fontSize = 11.sp,
                                                                    color = MaterialTheme.colorScheme.onSurface
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

                    // Export Vault JSON Archive
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
                                        val shareIntent = Intent.createChooser(sendIntent, "Export Vault JSON")
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
                        Text("Export Vault JSON Archive")
                    }
                }
            }
        }
    }

    // Dialog: Insert Record
    if (showAddRowDialog) {
        SqliteAddRowDialog(
            tableName = selectedTable,
            schema = currentTableSchema,
            onDismiss = { showAddRowDialog = false },
            onInsert = { values -> insertRecord(values, currentTableSchema) }
        )
    }

    // Dialog: Edit Record
    if (editingRow != null) {
        SqliteEditRowDialog(
            tableName = selectedTable,
            schema = currentTableSchema,
            row = editingRow!!,
            onDismiss = { editingRow = null },
            onUpdate = { pkCol, pkVal, values -> updateRecord(pkCol, pkVal, values, currentTableSchema) }
        )
    }

    // Dialog: Delete Record Confirmation
    if (deletingRow != null) {
        val pkCol = remember(deletingRow, currentTableSchema) {
            findPrimaryKeyCol(currentTableSchema, deletingRow!!)
        }
        val pkVal = deletingRow!![pkCol] ?: ""
        SqliteDeleteConfirmDialog(
            tableName = selectedTable,
            pkCol = pkCol,
            pkVal = pkVal,
            onDismiss = { deletingRow = null },
            onConfirm = { deleteRecord(pkCol, pkVal) }
        )
    }

    // Dialog: Quick Edit Single Field
    if (quickEditField != null) {
        SqliteQuickEditFieldDialog(
            data = quickEditField!!,
            onDismiss = { quickEditField = null },
            onSave = { newVal -> updateSingleField(quickEditField!!, newVal) }
        )
    }
}

@Composable
fun SqliteAddRowDialog(
    tableName: String,
    schema: List<ColumnSchema>,
    onDismiss: () -> Unit,
    onInsert: (Map<String, String>) -> Unit
) {
    val fieldValues = remember {
        mutableStateMapOf<String, String>().apply {
            schema.forEach { col ->
                this[col.name] = ""
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "New Record in $tableName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Fill column values or leave blank for auto/defaults",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.5.sp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    schema.forEach { col ->
                        val isInt = col.type.contains("INT")
                        val isNum = isInt || col.type.contains("REAL") || col.type.contains("FLOAT")
                        val isBool = col.type.contains("BOOL")

                        OutlinedTextField(
                            value = fieldValues[col.name] ?: "",
                            onValueChange = { fieldValues[col.name] = it },
                            label = {
                                Text(
                                    text = buildString {
                                        append(col.name)
                                        append(" (${col.type})")
                                        if (col.isPk) append(" • PK")
                                        if (col.notNull) append(" • Req")
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            placeholder = {
                                Text(
                                    text = when {
                                        col.isPk && isInt -> "Auto-increment ID if empty"
                                        col.defaultValue != null -> "Default: ${col.defaultValue}"
                                        isBool -> "1/0 or true/false"
                                        else -> "Enter ${col.name}"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (isNum) KeyboardType.Number else KeyboardType.Text
                            ),
                            singleLine = !col.type.contains("TEXT") && !col.type.contains("VARCHAR"),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onInsert(fieldValues) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Insert Row", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun SqliteEditRowDialog(
    tableName: String,
    schema: List<ColumnSchema>,
    row: Map<String, String>,
    onDismiss: () -> Unit,
    onUpdate: (pkCol: String, pkVal: String, values: Map<String, String>) -> Unit
) {
    val pkCol = remember(schema, row) {
        val pkFromSchema = schema.firstOrNull { it.isPk }?.name
        if (pkFromSchema != null && pkFromSchema in row) pkFromSchema
        else if ("id" in row) "id"
        else if ("key" in row) "key"
        else if ("plugin_id" in row) "plugin_id"
        else row.keys.firstOrNull() ?: "id"
    }
    val pkVal = remember(row, pkCol) { row[pkCol] ?: "" }

    val fieldValues = remember(row) {
        mutableStateMapOf<String, String>().apply {
            putAll(row)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Edit Record in $tableName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Primary Key: $pkCol = $pkVal",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    schema.forEach { col ->
                        val isPkField = col.name == pkCol
                        val isNum = col.type.contains("INT") || col.type.contains("REAL") || col.type.contains("FLOAT")

                        OutlinedTextField(
                            value = fieldValues[col.name] ?: "",
                            onValueChange = { if (!isPkField) fieldValues[col.name] = it },
                            enabled = !isPkField,
                            label = {
                                Text(
                                    text = buildString {
                                        append(col.name)
                                        append(" (${col.type})")
                                        if (isPkField) append(" [Locked PK]")
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (isNum) KeyboardType.Number else KeyboardType.Text
                            ),
                            singleLine = !col.type.contains("TEXT") && !col.type.contains("VARCHAR"),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onUpdate(pkCol, pkVal, fieldValues) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun SqliteDeleteConfirmDialog(
    tableName: String,
    pkCol: String,
    pkVal: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Delete Record?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "Are you sure you want to permanently delete this row from '$tableName'?\n\nCondition: $pkCol = $pkVal\n\nThis change takes effect immediately in SQLite.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Record", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun SqliteQuickEditFieldDialog(
    data: QuickEditFieldData,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textVal by remember(data) { mutableStateOf(data.currentVal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Quick Edit: ${data.colName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${data.tableName} · ${data.pkCol} = ${data.pkVal}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = textVal,
                    onValueChange = { textVal = it },
                    label = { Text(data.colName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4
                )
                Text(
                    text = "Changes take effect immediately in SQLite on save.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(textVal) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Update Field", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
