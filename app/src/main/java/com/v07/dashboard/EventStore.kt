package com.v07.dashboard

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

// ─── Data types ─────────────────────────────────────────────────────────────

val VAULT_DIR: String get() = VaultConfig.vaultSystemDir.path
val EVENTS_DIR: String get() = VaultConfig.eventsDir.path
val GITA_DIR: String get() = VaultConfig.gitaDir.path
val APP_USAGE_DIR: String get() = VaultConfig.appUsageDir.path

fun eventsDir() = VaultConfig.eventsDir
fun gitaDir() = VaultConfig.gitaDir
fun appUsageDir() = VaultConfig.appUsageDir

fun catFile(category: String) = VaultConfig.catFile(category)
fun recycleFile() = VaultConfig.recycleFile()
fun recycleLock() = VaultConfig.recycleLock()
fun catLock(category: String) = VaultConfig.catLock(category)
fun gitaReadFile() = VaultConfig.gitaReadFile()
fun gitaReviewsFile() = VaultConfig.gitaReviewsFile()
fun appUsageFile(date: String) = VaultConfig.appUsageFile(date)

@Serializable
data class Event(
    val id: String = "",
    val date: String = LocalDate.now().toString(),
    val ts: String = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    val kind: String? = null,
    val action: String? = null,
    val title: String? = null,
    val deadline: String? = null,
    val time: String? = null,
    val priority: String? = null,
    val task_id: String? = null,
    val activity: String? = null,
    val on: Boolean? = null,
    val type: String? = null,
    val mood: String? = null,
    val energy: String? = null,
    val mood_score: Int? = null,
    val energy_score: Int? = null,
    val note: String? = null,
    val text: String? = null,
    val topic: String? = null,
    val subtype: String? = null,
    val doom_scroll: Int? = null,
    val pushups: Int? = null,
    val minutes: Int? = null,
    val distance_km: Double? = null,
    val duration_min: Int? = null,
    val run_type: String? = null,
    val notes: String? = null,
    val custom_label: String? = null,
    val start_ts: String? = null,
    val duration: Int? = null,
    val paused: Boolean? = null,
    val problem: String? = null,
    val platform: String? = null,
    val difficulty: String? = null,
    val description: String? = null,
    val approach: String? = null,
    val opponent: String? = null,
    val result: String? = null,
    val color: String? = null,
    val trigger: String? = null,
    val time_since_last: String? = null,
    val time_since_last_sec: Int? = null,
    val completed: Boolean? = null,
    val status: String? = "pending",
    val verse_id: String? = null,
    val rating: Int? = null,
    val review_type: String? = null,
    val visited: Boolean? = null,
    val thought_id: String? = null,
    val recycle_id: String? = null,
    val category: String? = null,
    val data: Map<String, String>? = null,
    val deleted_at: String? = null,
    val timer_mode: String? = null,
    val timer_seconds: Int? = null,
    val pinned: Boolean? = null,
    val tags: String? = null,
    val info: String? = null,
    val file_path: String? = null,
    val location: String? = null,
    val captured_time: String? = null,
    val label: String? = null,
    val done: Boolean? = null,
    val frequency: String? = null,
    val recurring: Boolean? = null,
    val hour: Int? = null,
    val habit: String? = null,
    val habits: kotlinx.serialization.json.JsonElement? = null
)

fun Event.toEventMap(): Map<String, Any?> = mapOf(
    "id" to id, "date" to date, "ts" to ts, "kind" to kind, "action" to action,
    "title" to title, "deadline" to deadline, "time" to time, "priority" to priority,
    "task_id" to task_id, "activity" to activity, "on" to on, "type" to type,
    "mood" to mood, "energy" to energy, "mood_score" to mood_score, "energy_score" to energy_score, "note" to note, "text" to text,
    "topic" to topic, "subtype" to subtype, "doom_scroll" to doom_scroll,
    "pushups" to pushups, "minutes" to minutes, "distance_km" to distance_km,
    "duration_min" to duration_min, "run_type" to run_type, "notes" to notes,
    "custom_label" to custom_label, "start_ts" to start_ts, "duration" to duration,
    "paused" to paused, "problem" to problem, "platform" to platform,
    "difficulty" to difficulty, "description" to description, "approach" to approach,
    "opponent" to opponent, "result" to result, "color" to color, "trigger" to trigger,
    "time_since_last" to time_since_last, "time_since_last_sec" to time_since_last_sec,
    "completed" to completed, "status" to status, "verse_id" to verse_id,
    "rating" to rating, "review_type" to review_type, "visited" to visited,
    "thought_id" to thought_id, "recycle_id" to recycle_id, "category" to category,
    "data" to data, "deleted_at" to deleted_at,
    "timer_mode" to timer_mode, "timer_seconds" to timer_seconds,
    "pinned" to pinned, "tags" to tags, "info" to info, "file_path" to file_path,
    "location" to location, "captured_time" to captured_time,
    "label" to label, "done" to done, "frequency" to frequency,
    "recurring" to recurring, "hour" to hour,
    "habit" to habit,
    "habits" to (habits?.let { jsonElementToMap(it) })
).filterValues { it != null }

fun mapToEvent(m: Map<String, Any?>): Event {
    fun toInt(v: Any?): Int? = when (v) {
        is Number -> v.toInt()
        is String -> v.toIntOrNull() ?: v.toDoubleOrNull()?.toInt()
        else -> null
    }
    fun toDouble(v: Any?): Double? = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull()
        else -> null
    }
    fun toBoolean(v: Any?): Boolean? = when (v) {
        is Boolean -> v
        is String -> v.toBooleanStrictOrNull()
        else -> null
    }
    val id = (m["id"] as? String) ?: ""
    val ts = (m["ts"] as? String) ?: ""
    val date = (m["date"] as? String)?.takeIf { it.isNotBlank() } ?: ts.take(10).takeIf { it.length == 10 } ?: LocalDate.now().toString()
    return Event(
        id = id,
        date = date,
        ts = ts,
        kind = m["kind"] as? String,
        action = m["action"] as? String,
        title = m["title"] as? String,
        deadline = m["deadline"] as? String,
        time = m["time"] as? String,
        priority = m["priority"] as? String,
        task_id = m["task_id"] as? String,
        activity = m["activity"] as? String,
        on = toBoolean(m["on"]),
        type = m["type"] as? String,
        mood = m["mood"] as? String,
        energy = m["energy"] as? String,
        mood_score = toInt(m["mood_score"]),
        energy_score = toInt(m["energy_score"]),
        note = m["note"] as? String,
        text = m["text"] as? String,
        topic = m["topic"] as? String,
        subtype = m["subtype"] as? String,
        doom_scroll = toInt(m["doom_scroll"]),
        pushups = toInt(m["pushups"]),
        minutes = toInt(m["minutes"]),
        distance_km = toDouble(m["distance_km"]),
        duration_min = toInt(m["duration_min"]),
        run_type = m["run_type"] as? String,
        notes = m["notes"] as? String,
        custom_label = m["custom_label"] as? String,
        start_ts = m["start_ts"] as? String,
        duration = toInt(m["duration"]),
        paused = toBoolean(m["paused"]),
        problem = m["problem"] as? String,
        platform = m["platform"] as? String,
        difficulty = m["difficulty"] as? String,
        description = m["description"] as? String,
        approach = m["approach"] as? String,
        opponent = m["opponent"] as? String,
        result = m["result"] as? String,
        color = m["color"] as? String,
        trigger = m["trigger"] as? String,
        time_since_last = m["time_since_last"] as? String,
        time_since_last_sec = toInt(m["time_since_last_sec"]),
        completed = toBoolean(m["completed"]),
        status = m["status"] as? String ?: "pending",
        verse_id = m["verse_id"] as? String,
        rating = toInt(m["rating"]),
        review_type = m["review_type"] as? String,
        visited = toBoolean(m["visited"]),
        thought_id = m["thought_id"] as? String,
        recycle_id = m["recycle_id"] as? String,
        category = m["category"] as? String,
        deleted_at = m["deleted_at"] as? String,
        timer_mode = m["timer_mode"] as? String,
        timer_seconds = toInt(m["timer_seconds"]),
        pinned = toBoolean(m["pinned"]),
        tags = m["tags"] as? String,
        info = m["info"] as? String,
        file_path = m["file_path"] as? String,
        location = m["location"] as? String,
        captured_time = m["captured_time"] as? String,
        label = m["label"] as? String,
        done = toBoolean(m["done"]),
        frequency = m["frequency"] as? String,
        recurring = toBoolean(m["recurring"]),
        hour = toInt(m["hour"]),
        habit = m["habit"] as? String
    )
}

@Serializable
data class RecycleEntry(
    val recycle_id: String = "",
    val category: String = "",
    val data: Map<String, String> = emptyMap(),
    val deleted_at: String = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
)

val ACTIVITIES = emptyList<String>()
val ACTIVITY_LABELS = emptyMap<String, String>()
val MOODS = listOf(
    "happy" to "😊 Happy",
    "energetic" to "⚡ Energetic",
    "motivated" to "🚀 Motivated",
    "stressed" to "😰 Stressed",
    "anxious" to "😟 Anxious",
    "tired" to "😴 Tired",
    "confused" to "😕 Confused",
    "relaxed" to "😌 Relaxed",
    "frustrated" to "😤 Frustrated",
    "hopeful" to "🌟 Hopeful"
)
val MOOD_SCORES = mapOf(
    "happy" to 9, "energetic" to 8, "motivated" to 8,
    "relaxed" to 7, "hopeful" to 7,
    "confused" to 4, "tired" to 3, "stressed" to 3,
    "anxious" to 3, "frustrated" to 2
)
val ENERGIES = listOf(
    "high" to "🔥 High",
    "moderate" to "⚡ Moderate",
    "balanced" to "⚖️ Balanced",
    "low" to "🪫 Low",
    "exhausted" to "💀 Exhausted"
)
val ENERGY_SCORES = mapOf(
    "high" to 5, "moderate" to 4, "balanced" to 3,
    "low" to 2, "exhausted" to 1
)
val DSA_TOPICS = listOf(
    "Arrays", "Strings", "Linked Lists", "Stacks", "Queues",
    "Trees", "Graphs", "Hashing", "Heaps", "Recursion",
    "Dynamic Programming", "Greedy", "Sorting", "Searching",
    "Sliding Window", "Two Pointers", "Bit Manipulation", "Tries"
)

// ─── File Locking ──────────────────────────────────────────────────────────

fun <T> withLock(lockFile: File, block: () -> T): T {
    val start = System.currentTimeMillis()
    while (true) {
        if (lockFile.createNewFile()) {
            lockFile.deleteOnExit()
            try {
                return block()
            } finally {
                lockFile.delete()
            }
        }
        if (System.currentTimeMillis() - start > 10000) {
            lockFile.delete()
            continue
        }
        Thread.sleep(20)
    }
}

fun <T> withDoubleLock(catLock: File, rLock: File, block: () -> T): T {
    val start = System.currentTimeMillis()
    while (true) {
        val catOk = catLock.createNewFile()
        val rOk = rLock.createNewFile()
        if (catOk && rOk) {
            catLock.deleteOnExit(); rLock.deleteOnExit()
            try { return block() }
            finally { catLock.delete(); rLock.delete() }
        }
        if (catOk) catLock.delete()
        if (rOk) rLock.delete()
        if (System.currentTimeMillis() - start > 10000) {
            catLock.delete(); rLock.delete()
            continue
        }
        Thread.sleep(20)
    }
}

// ─── Event I/O ─────────────────────────────────────────────────────────────

fun appendEvent(category: String, data: Map<String, Any?>): Event {
    val id = "${category[0]}_${Random.nextLong().toString(36).take(8)}"
    val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
    val record = mutableMapOf<String, Any?>(
        "id" to id,
        "date" to LocalDate.now().toString(),
        "ts" to timestamp
    )
    for ((k, v) in data) {
        if (v != null) record[k] = v
    }
    val jsonStr = toJson(record)
    val f = catFile(category)
    f.parentFile.mkdirs()
    withLock(catLock(category)) {
        f.appendText(jsonStr + "\n")
    }
    return Event(id = id, date = record["date"] as? String ?: "", ts = timestamp)
}

fun appendEventWithTs(category: String, data: Map<String, Any?>, customTs: String? = null, customDate: String? = null): Event {
    val id = "${category[0]}_${Random.nextLong().toString(36).take(8)}"
    val timestamp = customTs ?: OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
    val date = customDate ?: LocalDate.now().toString()
    val record = mutableMapOf<String, Any?>(
        "id" to id,
        "date" to date,
        "ts" to timestamp
    )
    for ((k, v) in data) {
        if (v != null) record[k] = v
    }
    val jsonStr = toJson(record)
    val f = catFile(category)
    f.parentFile.mkdirs()
    withLock(catLock(category)) {
        f.appendText(jsonStr + "\n")
    }
    return Event(id = id, date = date, ts = timestamp)
}

fun appendRawEvent(category: String, data: String) {
    val f = catFile(category)
    f.parentFile.mkdirs()
    withLock(catLock(category)) {
        f.appendText(data + "\n")
    }
}

fun deleteEvent(category: String, eventId: String) {
    val f = catFile(category)
    if (!f.exists()) return
    withLock(catLock(category)) {
        val lines = f.readLines().filter { line ->
            if (line.isBlank()) return@filter false
            try {
                val m = jsonToMap(line)
                val id = m["id"] as? String ?: ""
                id != eventId
            } catch (_: Exception) { true }
        }
        f.writeText(lines.joinToString("\n") + if (lines.isNotEmpty()) "\n" else "")
    }
}

fun updateEventField(category: String, eventId: String, fields: Map<String, Any?>) {
    val f = catFile(category)
    if (!f.exists()) return
    withLock(catLock(category)) {
        val lines = f.readLines().map { line ->
            if (line.isBlank()) return@map line
            try {
                val m = jsonToMap(line)
                if (m["id"] == eventId) {
                    for ((k, v) in fields) m[k] = v
                    m["id"] = eventId
                    toJson(m)
                } else line
            } catch (_: Exception) { line }
        }
        f.writeText(lines.joinToString("\n") + if (lines.isNotEmpty()) "\n" else "")
    }
}

fun readEventsRaw(category: String): List<Map<String, Any?>> {
    val f = catFile(category)
    if (!f.exists()) return emptyList()
    return try {
        f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
            try { jsonToMap(line) } catch (_: Exception) { null }
        }
    } catch (e: Exception) {
        android.util.Log.e("V07EventStore", "readEventsRaw($category): failed to read ${f.absolutePath}: ${e.message}")
        emptyList()
    }
}

fun readEventsAllRaw(category: String): List<Map<String, Any?>> {
    val seen = mutableSetOf<String>()
    val result = mutableListOf<Map<String, Any?>>()
    for (suffix in listOf("", "_ui")) {
        val f = File(eventsDir(), "${category}${suffix}.jsonl")
        if (!f.exists()) continue
        try {
            for (line in f.readLines().filter { it.isNotBlank() }) {
                try {
                    val m = jsonToMap(line)
                    val id = (m["id"] as? String)?.takeIf { it.isNotBlank() } ?: "gen_${seen.size}"
                    if (seen.add(id)) {
                        result.add(m)
                    }
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            android.util.Log.e("V07EventStore", "readEventsAllRaw($category): failed to read ${f.absolutePath}: ${e.message}")
        }
    }
    return result
}

fun todaysEventsRaw(category: String): List<Map<String, Any?>> {
    val today = LocalDate.now().toString()
    return readEventsAllRaw(category).filter {
        val d = (it["date"] as? String)?.takeIf { s -> s.isNotBlank() }
            ?: (it["ts"] as? String)?.take(10)
            ?: ""
        d == today
    }
}

fun eventsInRangeRaw(category: String, start: String, end: String): List<Map<String, Any?>> {
    return readEventsAllRaw(category).filter {
        val d = (it["date"] as? String)?.takeIf { s -> s.isNotBlank() }
            ?: (it["ts"] as? String)?.take(10)
            ?: ""
        d >= start && d <= end
    }
}

fun readEvents(category: String): List<Event> {
    val f = catFile(category)
    if (!f.exists()) {
        android.util.Log.w("V07EventStore", "readEvents($category): file not found at ${f.absolutePath}")
        return emptyList()
    }
    return try {
        f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
            try {
                json.decodeFromString<Event>(line)
            } catch (_: Exception) {
                try {
                    val m = jsonToMap(line)
                    mapToEvent(m)
                } catch (_: Exception) { null }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("V07EventStore", "readEvents($category): failed to read ${f.absolutePath}: ${e.message}")
        emptyList()
    }
}

fun readEventsAll(category: String): List<Event> {
    val seen = mutableSetOf<String>()
    val result = mutableListOf<Event>()
    for (suffix in listOf("", "_ui")) {
        val f = File(eventsDir(), "${category}${suffix}.jsonl")
        if (!f.exists()) continue
        try {
            for (line in f.readLines().filter { it.isNotBlank() }) {
                try {
                    val e = try {
                        json.decodeFromString<Event>(line)
                    } catch (_: Exception) {
                        val m = jsonToMap(line)
                        mapToEvent(m)
                    }
                    val id = e.id.takeIf { it.isNotEmpty() } ?: "gen_${seen.size}"
                    if (seen.add(id)) {
                        result.add(if (e.id.isEmpty()) e.copy(id = id) else e)
                    }
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            android.util.Log.e("V07EventStore", "readEventsAll($category): failed to read ${f.absolutePath}: ${e.message}")
        }
    }
    return result
}

fun todaysEvents(category: String): List<Event> {
    val today = LocalDate.now().toString()
    return readEventsAll(category).filter {
        val d = it.date.takeIf { s -> s.isNotBlank() } ?: it.ts.take(10)
        d == today
    }
}

fun eventsInRange(category: String, start: String, end: String): List<Event> {
    return readEventsAll(category).filter {
        val d = it.date.takeIf { s -> s.isNotBlank() } ?: it.ts.take(10)
        d >= start && d <= end
    }
}

fun migrateRunningDurations() {
    val f = catFile("running")
    if (!f.exists()) return
    try {
        val lines = f.readLines()
        val migrated = lines.map { line ->
            if (line.isBlank()) return@map line
            try {
                val map = jsonToMap(line)
                val dist = (map["distance_km"] as? Number)?.toDouble() ?: 0.0
                val rawSec = (map["duration_sec"] as? Number)?.toInt()
                val totalSec = if (rawSec != null && rawSec > 0) {
                    rawSec
                } else {
                    val raw = (map["duration_min"] as? Number)?.toDouble()
                        ?: (map["duration"] as? Number)?.toDouble()
                        ?: (map["minutes"] as? Number)?.toDouble()
                        ?: (map["duration_min"] as? String)?.toDoubleOrNull()
                        ?: (map["duration_sec"] as? String)?.toDoubleOrNull()
                        ?: 0.0
                    val isSec = if (dist > 0.0) (raw / dist > 20.0) else (raw >= 100.0)
                    if (isSec) Math.round(raw).toInt() else Math.round(raw * 60.0).toInt()
                }
                if (totalSec <= 0) return@map line
                val mins = Math.round(totalSec / 60.0).toInt()
                val mm = totalSec / 60
                val ss = totalSec % 60
                val formatted = if (ss > 0) "${mm}m ${ss}s" else "${mm}m"

                var modified = false
                if (map["duration_sec"] != totalSec) { map["duration_sec"] = totalSec; modified = true }
                if (map["duration_min"] != mins) { map["duration_min"] = mins; modified = true }
                if (map["duration_minutes"] != mins) { map["duration_minutes"] = mins; modified = true }
                if (map["minutes"] != mins) { map["minutes"] = mins; modified = true }
                if (map["duration_formatted"] != formatted) { map["duration_formatted"] = formatted; modified = true }

                if (modified) toJson(map) else line
            } catch (_: Exception) { line }
        }
        val changed = lines.zip(migrated).any { (o, n) -> o != n }
        if (changed) {
            f.writeText(migrated.joinToString("\n") + "\n")
            android.util.Log.i("V07EventStore", "migrateRunningDurations: normalized running entries")
        }
    } catch (e: Exception) {
        android.util.Log.e("V07EventStore", "migrateRunningDurations failed: ${e.message}")
    }
}

// ─── Streak engine ─────────────────────────────────────────────────────────

fun computeStreak(events: List<Event>, fn: (Event) -> Boolean): Int {
    val today = LocalDate.now()
    val sorted = events.filter(fn).sortedByDescending { it.date }
    var streak = 0
    var cursor = today
    for (e in sorted) {
        if (e.date.isEmpty()) continue
        val ed = try { LocalDate.parse(e.date) } catch (_: Exception) { continue }
        val diff = cursor.toEpochDay() - ed.toEpochDay()
        if (diff.toInt() == 0) {
            streak++
            cursor = cursor.minusDays(1)
        } else if (diff > streak) {
            break
        }
    }
    return streak
}

// ─── Task engine ───────────────────────────────────────────────────────────

data class TaskCacheEntry(val mtime: Long, val tasks: List<Event>)

var taskCache = TaskCacheEntry(0, emptyList())

fun rebuildTaskState(): List<Event> {
    val f = catFile("tasks")
    val mtime = if (f.exists()) f.lastModified() else 0L
    if (mtime == taskCache.mtime) return taskCache.tasks
    val events = readEventsAll("tasks")
    val tasks = mutableMapOf<String, Event>()
    for (e in events) {
        when (e.action) {
            "created" -> tasks[e.id] = e.copy(status = "pending")
            "completed" -> {
                e.task_id?.let { id ->
                    tasks[id]?.let { tasks[id] = it.copy(status = "done") }
                }
            }
            "reopened" -> {
                e.task_id?.let { id ->
                    tasks[id]?.let { tasks[id] = it.copy(status = "pending") }
                }
            }
            "pinned" -> {
                e.task_id?.let { id ->
                    tasks[id]?.let { tasks[id] = it.copy(pinned = e.pinned) }
                }
            }
            "deleted" -> {
                e.task_id?.let { tasks.remove(it) }
            }
        }
    }
    val result = tasks.values.toList()
    taskCache = TaskCacheEntry(mtime, result)
    return result
}

// ─── Recycle bin ───────────────────────────────────────────────────────────

fun readRecycle(): List<RecycleEntry> {
    val f = recycleFile()
    if (!f.exists()) return emptyList()
    return f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
        try { json.decodeFromString<RecycleEntry>(line) } catch (_: Exception) { null }
    }
}

fun writeRecycle(entries: List<RecycleEntry>) {
    recycleFile().parentFile.mkdirs()
    recycleFile().writeText(entries.joinToString("\n") { json.encodeToString(it) } + "\n")
}

fun listRecycleEntries(category: String? = null): List<RecycleEntry> {
    return withLock(recycleLock()) {
        val entries = readRecycle()
        if (category != null) entries.filter { it.category == category } else entries
    }.sortedByDescending { it.deleted_at }
}

fun restoreRecycleEntry(recycleId: String): RecycleEntry? {
    return withLock(recycleLock()) {
        val entries = readRecycle().toMutableList()
        val idx = entries.indexOfFirst { it.recycle_id == recycleId }
        if (idx < 0) return@withLock null
        val entry = entries.removeAt(idx)
        val snapshot = entry.data
        val cat = entry.category
        val rawLine = snapshot["_raw"]
        if (rawLine != null) {
            appendRawEvent(cat, rawLine)
        } else {
            val jsonData = json.encodeToString(Event(id = snapshot["id"] ?: "", date = snapshot["date"] ?: "", ts = snapshot["ts"] ?: ""))
            appendRawEvent(cat, jsonData)
        }
        writeRecycle(entries)
        entry
    }
}

fun clearRecycleBin(category: String? = null) {
    withLock(recycleLock()) {
        if (category != null) {
            val entries = readRecycle().toMutableList()
            val filtered = entries.filter { it.category != category }
            writeRecycle(filtered)
        } else {
            writeRecycle(emptyList())
        }
    }
}
