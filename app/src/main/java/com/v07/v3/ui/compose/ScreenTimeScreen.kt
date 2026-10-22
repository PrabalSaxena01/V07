package com.v07.v3.ui.compose

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.core.data.EventRepository
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class DayTrendPoint(
    val dateStr: String,
    val dayLabel: String,
    val dayNumber: String,
    val durationMs: Long,
    val isSelected: Boolean
)

enum class ScreenTimeChartMode {
    HOURLY_RHYTHM,
    WEEKLY_TREND
}

@Composable
fun ScreenTimeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { EventRepository.getInstance(context) }
    val tracker = remember { ScreenTimeTracker(context, repository) }
    val scope = rememberCoroutineScope()

    val sdfIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sdfFriendly = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val todayDateStr = remember { sdfIso.format(Date()) }

    var selectedDateStr by remember { mutableStateOf(todayDateStr) }
    var hasPermission by remember { mutableStateOf(tracker.hasUsageStatsPermission()) }
    var appUsages by remember { mutableStateOf<List<ScreenTimeTracker.AppUsageStat>>(emptyList()) }
    var hourlyMinutes by remember { mutableStateOf(List(24) { 0 }) }
    var weeklyTrend by remember { mutableStateOf<List<DayTrendPoint>>(emptyList()) }

    var chartMode by remember { mutableStateOf(ScreenTimeChartMode.HOURLY_RHYTHM) }
    var selectedHourIndex by remember { mutableStateOf<Int?>(null) }

    fun loadData(dateStr: String) {
        hasPermission = tracker.hasUsageStatsPermission()
        if (!hasPermission) {
            appUsages = emptyList()
            hourlyMinutes = List(24) { 0 }
            weeklyTrend = emptyList()
            return
        }

        scope.launch(Dispatchers.IO) {
            val summary = tracker.collectForDate(dateStr)
            val sortedApps = summary.topApps.sortedByDescending { it.durationMs }
            val hours = summary.hourlyMinutes

            // Compute 7-day trend
            val trend = mutableListOf<DayTrendPoint>()
            try {
                val cal = Calendar.getInstance()
                val parsed = sdfIso.parse(dateStr) ?: Date()
                cal.time = parsed

                for (i in 6 downTo 0) {
                    val dayCal = Calendar.getInstance().apply {
                        time = parsed
                        add(Calendar.DAY_OF_YEAR, -i)
                    }
                    val dStr = sdfIso.format(dayCal.time)
                    val daySum = if (dStr == dateStr) summary else tracker.collectForDate(dStr)
                    val label = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time)
                    val num = SimpleDateFormat("d", Locale.getDefault()).format(dayCal.time)

                    trend.add(
                        DayTrendPoint(
                            dateStr = dStr,
                            dayLabel = label,
                            dayNumber = num,
                            durationMs = daySum.totalDurationMs,
                            isSelected = (dStr == dateStr)
                        )
                    )
                }
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                appUsages = sortedApps
                hourlyMinutes = hours
                weeklyTrend = trend
            }
        }
    }

    LaunchedEffect(selectedDateStr) {
        selectedHourIndex = null
        loadData(selectedDateStr)
    }

    fun navigateDate(deltaDays: Int) {
        try {
            val dateObj = sdfIso.parse(selectedDateStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = dateObj
            cal.add(Calendar.DAY_OF_YEAR, deltaDays)
            selectedDateStr = sdfIso.format(cal.time)
        } catch (_: Exception) {}
    }

    val totalMs = appUsages.sumOf { it.durationMs }
    val totalMins = totalMs / 60000L
    val totalHrs = totalMins / 60L
    val remMins = totalMins % 60L
    val totalDisplay = if (totalHrs > 0) "${totalHrs}h ${remMins}m" else "${remMins}m"

    val isToday = selectedDateStr == todayDateStr
    val cardShape = RoundedCornerShape(16.dp)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Top Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Digital Stillness",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x26F59E0B)
                            ) {
                                Text(
                                    text = "CORE TOOL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Telemetry & mindful attention boundaries",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(onClick = { loadData(selectedDateStr) }) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- 2. Permission Banner if missing ---
        if (!hasPermission) {
            item {
                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Usage Access Permission Required",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                        Text(
                            text = "To view detailed app telemetry from your device, please grant Android usage stats access.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        Button(
                            onClick = {
                                val intent = tracker.createPermissionIntent()
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                contentColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(text = "Grant Permission in Settings", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // --- 3. Date Navigation Bar ---
        item {
            Surface(
                shape = cardShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { navigateDate(-1) },
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Day", modifier = Modifier.size(18.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            if (!isToday) selectedDateStr = todayDateStr
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isToday) "Today" else {
                                    val d = try { sdfIso.parse(selectedDateStr) } catch (_: Exception) { Date() }
                                    sdfFriendly.format(d ?: Date())
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                        Text(
                            text = if (isToday) "Daily Overview" else selectedDateStr,
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { navigateDate(1) },
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Day", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // --- 4. Total Screen Time Hero Card ---
        item {
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TOTAL SCREEN ATTENTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = if (hasPermission) totalDisplay else "--",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${appUsages.size} apps active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        val peakHour = hourlyMinutes.indices.maxByOrNull { hourlyMinutes[it] } ?: 0
                        val peakMinutes = hourlyMinutes.getOrElse(peakHour) { 0 }
                        if (peakMinutes > 0) {
                            val peakLabel = when {
                                peakHour == 0 -> "12 AM"
                                peakHour < 12 -> "${peakHour} AM"
                                peakHour == 12 -> "12 PM"
                                else -> "${peakHour - 12} PM"
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0x26F59E0B)
                            ) {
                                Text(
                                    text = "Peak: $peakLabel ($peakMinutes m)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // --- 5. TACTILE BARGRAPH REPRESENTATION (Hourly & 7-Day Trend) ---
        // =====================================================================
        item {
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
                    // Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ATTENTION BARGRAPH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = if (chartMode == ScreenTimeChartMode.HOURLY_RHYTHM) "24-Hour Rhythm Distribution" else "7-Day Daily Attention Comparison",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Full-width Tactile Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (chartMode == ScreenTimeChartMode.HOURLY_RHYTHM) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { chartMode = ScreenTimeChartMode.HOURLY_RHYTHM },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "24-Hour Rhythm",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (chartMode == ScreenTimeChartMode.HOURLY_RHYTHM) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (chartMode == ScreenTimeChartMode.WEEKLY_TREND) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { chartMode = ScreenTimeChartMode.WEEKLY_TREND },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "7-Day Trend",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (chartMode == ScreenTimeChartMode.WEEKLY_TREND) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    if (chartMode == ScreenTimeChartMode.HOURLY_RHYTHM) {
                        // -------------------------------------------------------------
                        // Mode A: 24-Hour Hourly Rhythm Bar Chart
                        // -------------------------------------------------------------
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Selected hour callout
                            if (selectedHourIndex != null) {
                                val idx = selectedHourIndex!!
                                val mins = hourlyMinutes.getOrElse(idx) { 0 }
                                val hourTitle = when {
                                    idx == 0 -> "12:00 AM – 1:00 AM"
                                    idx < 12 -> "$idx:00 AM – ${idx + 1}:00 AM"
                                    idx == 12 -> "12:00 PM – 1:00 PM"
                                    else -> "${idx - 12}:00 PM – ${idx - 11}:00 PM"
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = hourTitle,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "$mins min active",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }

                            // 24 Bar chart representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    for (hour in 0..23) {
                                        val mins = hourlyMinutes.getOrElse(hour) { 0 }
                                        val heightRatio = (mins.toFloat() / 60f).coerceIn(0f, 1f)
                                        val isSelected = (selectedHourIndex == hour)

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                                .clickable {
                                                    selectedHourIndex = if (selectedHourIndex == hour) null else hour
                                                }
                                                .padding(horizontal = 1.dp)
                                        ) {
                                            // Top pill indicator if selected
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }

                                            // Bar body
                                            val barColor = when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                mins >= 40 -> Color(0xFFEF4444) // Intense screen time
                                                mins >= 20 -> Color(0xFFF59E0B) // Moderate
                                                mins > 0 -> Color(0xFF10B981)   // Light / Mindful
                                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(fraction = if (mins > 0) heightRatio.coerceAtLeast(0.08f) else 0.04f)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(barColor)
                                            )
                                        }
                                    }
                                }
                            }

                            // X-axis markings (12a, 3a, 6a, 9a, 12p, 3p, 6p, 9p)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val labels = listOf("12a", "3a", "6a", "9a", "12p", "3p", "6p", "9p", "11p")
                                labels.forEach { lbl ->
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // -------------------------------------------------------------
                        // Mode B: 7-Day Trend Bar Chart
                        // -------------------------------------------------------------
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val maxTrendMs = weeklyTrend.maxOfOrNull { it.durationMs }?.coerceAtLeast(3600_000L) ?: 3600_000L
                            val avgTrendMs = if (weeklyTrend.isNotEmpty()) weeklyTrend.sumOf { it.durationMs } / weeklyTrend.size else 0L
                            val avgHrs = (avgTrendMs / 3600000L)
                            val avgMins = ((avgTrendMs % 3600000L) / 60000L)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "7-Day Daily Average: ${avgHrs}h ${avgMins}m",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "Tap a bar to inspect",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            // 7 Bar flex layout
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    weeklyTrend.forEach { point ->
                                        val fraction = (point.durationMs.toFloat() / maxTrendMs.toFloat()).coerceIn(0f, 1f)
                                        val dayMins = point.durationMs / 60000L
                                        val dayHrs = dayMins / 60L
                                        val remM = dayMins % 60L
                                        val timeStr = if (dayHrs > 0) "${dayHrs}h" else "${remM}m"

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(36.dp)
                                                .clickable {
                                                    selectedDateStr = point.dateStr
                                                }
                                        ) {
                                            // Value label above bar
                                            if (point.durationMs > 0) {
                                                Text(
                                                    text = timeStr,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = if (point.isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (point.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }

                                            // Bar body
                                            val barColor = when {
                                                point.isSelected -> MaterialTheme.colorScheme.primary
                                                point.durationMs > 14400000L -> Color(0xFFEF4444) // > 4h
                                                point.durationMs > 0 -> Color(0xFFF59E0B)
                                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .width(22.dp)
                                                    .fillMaxHeight(fraction = if (point.durationMs > 0) fraction.coerceAtLeast(0.1f) else 0.05f)
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(barColor)
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Day label
                                            Text(
                                                text = point.dayLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = if (point.isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (point.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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

        // --- 6. Application Usage Breakdown ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APPLICATION TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "${appUsages.size} apps logged",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (appUsages.isEmpty()) {
                    Surface(
                        shape = cardShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (hasPermission) "No application telemetry found for this day." else "Permission required to view application breakdown.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                } else {
                    appUsages.forEach { usage ->
                        val durationMins = usage.durationMs / 60000L
                        val appHrs = durationMins / 60L
                        val appRemMins = durationMins % 60L
                        val durationText = if (appHrs > 0) "${appHrs}h ${appRemMins}m" else "${appRemMins}m"
                        val fraction = if (totalMs > 0) (usage.durationMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f) else 0f

                        Surface(
                            shape = cardShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                        AppLogoIcon(
                                            packageName = usage.packageName,
                                            appName = usage.appName,
                                            modifier = Modifier.size(36.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = usage.appName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = usage.packageName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = durationText,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = String.format("%.1f%%", usage.percentage),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }

                                // Proportional progress bar
                                LinearProgressIndicator(
                                    progress = fraction,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Resilient, cached application icon loader for Digital Stillness.
 * Extracts actual package logo directly from PackageManager with fallback to styled initial.
 */
@Composable
fun AppLogoIcon(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var iconBitmap by remember(packageName) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val drawable = pm.getApplicationIcon(packageName)
                val width = drawable.intrinsicWidth.coerceAtLeast(72)
                val height = drawable.intrinsicHeight.coerceAtLeast(72)
                val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                    drawable.bitmap
                } else {
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bmp
                }
                iconBitmap = bitmap.asImageBitmap()
            } catch (_: Exception) {
                iconBitmap = null
            }
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap!!,
            contentDescription = appName,
            modifier = modifier.clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
