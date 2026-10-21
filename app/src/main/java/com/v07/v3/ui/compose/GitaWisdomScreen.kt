package com.v07.v3.ui.compose

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.GitaProvider
import com.v07.v3.core.data.V3Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// Verse count for chapters 1 through 18
val GITA_CHAPTER_VERSE_COUNTS = listOf(
    47, 72, 43, 42, 29, 47, 30, 28, 34, 42, 55, 20, 35, 27, 20, 24, 28, 78
)

private fun extractGitaText(obj: JSONObject?, key: String): String {
    if (obj == null) return ""
    val sub = obj.optJSONObject(key)
    if (sub != null) {
        val txt = sub.optString("text", "")
        if (txt.isNotBlank()) return txt
    }
    return obj.optString(key, "")
}

private fun extractGitaHeading(obj: JSONObject?, key: String, fallback: String): String {
    if (obj == null) return fallback
    val sub = obj.optJSONObject(key)
    val h = sub?.optString("heading", "") ?: ""
    return if (h.isNotBlank()) h else fallback
}

val GITA_CHAPTER_NAMES = listOf(
    "1. Arjuna Vishada Yoga",
    "2. Sankhya Yoga",
    "3. Karma Yoga",
    "4. Jnana Karma Sanyasa",
    "5. Karma Sanyasa Yoga",
    "6. Dhyana Yoga",
    "7. Jnana Vijnana Yoga",
    "8. Akshara Brahma Yoga",
    "9. Raja Vidya Yoga",
    "10. Vibhuti Yoga",
    "11. Vishwarupa Darshana",
    "12. Bhakti Yoga",
    "13. Kshetra Vibhaga Yoga",
    "14. Gunatraya Vibhaga",
    "15. Purushottama Yoga",
    "16. Daivasura Sampad",
    "17. Shraddhatraya Vibhaga",
    "18. Moksha Sanyasa Yoga"
)

/**
 * GitaWisdomScreen
 *
 * Sovereign, native Jetpack Compose Bhagavad Gita wisdom reader.
 * Faithfully incorporates the Chapter & Verse dropdown selectors from the legacy APK.
 */
@Composable
fun GitaWisdomScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gitaProvider = remember { GitaProvider.getInstance(context) }
    val repository = remember { EventRepository.getInstance(context) }

    var selectedChapter by remember { mutableStateOf(2) }
    var selectedVerse by remember { mutableStateOf(47) }
    var currentVerseObj by remember { mutableStateOf<JSONObject?>(null) }
    var isReadToday by remember { mutableStateOf(false) }
    var readCount by remember { mutableStateOf(0) }
    var reflectionText by remember { mutableStateOf("") }
    var showReflectionInput by remember { mutableStateOf(false) }

    val chapterOptions = remember {
        (1..18).map { "Chapter $it" }
    }

    val maxVersesForCurrentChapter = GITA_CHAPTER_VERSE_COUNTS.getOrElse(selectedChapter - 1) { 72 }
    val verseOptions = remember(selectedChapter) {
        (1..maxVersesForCurrentChapter).map { "Verse $it" }
    }

    fun loadVerse(chapter: Int, verse: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            val v = gitaProvider.getVerse(chapter, verse)
            val progress = gitaProvider.getProgress()
            withContext(Dispatchers.Main) {
                currentVerseObj = v
                isReadToday = v?.optBoolean("already_read", false) ?: false
                readCount = progress.optInt("read_count", 0)
            }
        }
    }

    LaunchedEffect(selectedChapter, selectedVerse) {
        loadVerse(selectedChapter, selectedVerse)
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text(
                                text = "Shrimad Bhagavad Gita",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = GITA_CHAPTER_NAMES.getOrElse(selectedChapter - 1) { "Wisdom & Presence" },
                                style = MaterialTheme.typography.labelSmall,
                                color = SanctuaryPalette.AccentHabits,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "$readCount / 701",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =================================================================
            // 1. Chapter & Verse Dropdown Selectors (Faithful to legacy index.html)
            // =================================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCRIPTURE NAVIGATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Random Shloka Button
                                OutlinedButton(
                                    onClick = {
                                        val rand = gitaProvider.getRandomVerse()
                                        if (rand != null) {
                                            selectedChapter = rand.optInt("chapter", 2)
                                            selectedVerse = rand.optInt("verse", 47)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("🎲 Random", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                                }

                                // Daily Shloka Button
                                OutlinedButton(
                                    onClick = {
                                        val daily = gitaProvider.getDailyVerse()
                                        if (daily != null) {
                                            selectedChapter = daily.optInt("chapter", 2)
                                            selectedVerse = daily.optInt("verse", 47)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("☀️ Daily", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                                }
                            }
                        }

                        // Dropdowns Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SanctuaryFilterDropdown(
                                label = "Chapter:",
                                selectedItem = "Chapter $selectedChapter",
                                items = chapterOptions,
                                onItemSelected = {
                                    val ch = it.removePrefix("Chapter ").toIntOrNull() ?: 2
                                    selectedChapter = ch
                                    selectedVerse = 1
                                },
                                leadingIcon = Icons.Outlined.MenuBook,
                                modifier = Modifier.weight(1.2f)
                            )

                            SanctuaryFilterDropdown(
                                label = "Verse:",
                                selectedItem = "Verse $selectedVerse",
                                items = verseOptions,
                                onItemSelected = {
                                    val vs = it.removePrefix("Verse ").toIntOrNull() ?: 1
                                    selectedVerse = vs
                                },
                                leadingIcon = Icons.Outlined.FormatQuote,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // =================================================================
            // 2. Sanskrit Original Shloka Card
            // =================================================================
            item {
                val shlokaText = extractGitaText(currentVerseObj, "shloka").ifBlank {
                    "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥"
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SanctuaryPalette.AccentHabits.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = PillShape,
                            color = SanctuaryPalette.AccentHabits.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "CHAPTER $selectedChapter · SHLOKA $selectedVerse",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = SanctuaryPalette.AccentHabits,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = shlokaText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                lineHeight = 30.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // =================================================================
            // 3. Hindi Translation & Commentary
            // =================================================================
            item {
                val hindiText = extractGitaText(currentVerseObj, "hindi_translation")
                val hindiHeading = extractGitaHeading(currentVerseObj, "hindi_translation", "हिन्दी अनुवाद (Hindi Translation)")
                val hindiComm = extractGitaText(currentVerseObj, "hindi_commentary")

                if (hindiText.isNotBlank() || hindiComm.isNotBlank()) {
                    var showFullComm by remember(selectedChapter, selectedVerse) { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(hindiHeading, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            if (hindiText.isNotBlank()) {
                                Text(
                                    text = hindiText,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (hindiComm.isNotBlank() && hindiComm != hindiText) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "विस्तृत व्याख्या (Commentary)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SanctuaryPalette.AccentHabits,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    TextButton(
                                        onClick = { showFullComm = !showFullComm },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (showFullComm) "Hide" else "Read", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                AnimatedVisibility(visible = showFullComm) {
                                    Text(
                                        text = hindiComm,
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // 4. English Translation (Swami Sivananda & Gambirananda)
            // =================================================================
            item {
                val engSivananda = extractGitaText(currentVerseObj, "english_sivananda")
                val engSivaHeading = extractGitaHeading(currentVerseObj, "english_sivananda", "English Commentary (Swami Sivananda)")
                val engGambira = extractGitaText(currentVerseObj, "english_gambirananda")
                val engGambHeading = extractGitaHeading(currentVerseObj, "english_gambirananda", "English Commentary (Swami Gambirananda)")

                val engText = if (engSivananda.isNotBlank()) engSivananda else engGambira
                val heading = if (engSivananda.isNotBlank()) engSivaHeading else engGambHeading

                if (engText.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(heading, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            Text(
                                text = engText,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // =================================================================
            // 5. Actions & Navigation
            // =================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (selectedVerse > 1) {
                                selectedVerse--
                            } else if (selectedChapter > 1) {
                                selectedChapter--
                                selectedVerse = GITA_CHAPTER_VERSE_COUNTS.getOrElse(selectedChapter - 1) { 1 }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val nextState = !isReadToday
                                gitaProvider.markVerseRead(selectedChapter, selectedVerse, nextState)
                                val progress = gitaProvider.getProgress()

                                if (nextState) {
                                    val now = System.currentTimeMillis()
                                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                                    val ev = V3Event(
                                        id = "gita_${selectedChapter}_${selectedVerse}_${UUID.randomUUID().toString().take(6)}",
                                        category = "gita",
                                        date = dateStr,
                                        ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now)),
                                        timestamp = now,
                                        title = "Bhagavad Gita $selectedChapter.$selectedVerse",
                                        content = "Contemplated Shloka $selectedChapter.$selectedVerse",
                                        score = 1
                                    )
                                    repository.insertEvent(ev)
                                }

                                withContext(Dispatchers.Main) {
                                    isReadToday = nextState
                                    readCount = progress.optInt("read_count", readCount)
                                    Toast.makeText(
                                        context,
                                        if (nextState) "Shloka $selectedChapter.$selectedVerse marked as contemplated ✓" else "Removed from contemplated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReadToday) SanctuaryPalette.AccentHabits else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isReadToday) Icons.Outlined.CheckCircle else Icons.Outlined.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isReadToday) "Contemplated ✓" else "Mark Contemplated")
                    }

                    OutlinedButton(
                        onClick = {
                            val max = GITA_CHAPTER_VERSE_COUNTS.getOrElse(selectedChapter - 1) { 72 }
                            if (selectedVerse < max) {
                                selectedVerse++
                            } else if (selectedChapter < 18) {
                                selectedChapter++
                                selectedVerse = 1
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
