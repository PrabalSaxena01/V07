package com.v07.dashboard

import kotlinx.serialization.json.*
import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

fun getGitaVerses(): List<Map<String, Any?>> {
    val f = File(gitaDir(), "gita.json")
    if (!f.exists()) return listOf()
    return try {
        json.decodeFromString<JsonArray>(f.readText())
            .map { it.jsonObject.entries.associate { (k, v) -> k to jsonElementToAny(v) } }
    } catch (_: Exception) { listOf() }
}

private fun jsonElementToAny(el: JsonElement): Any? = when (el) {
    is JsonPrimitive -> {
        val s = el.content
        s.toIntOrNull() ?: s.toDoubleOrNull() ?: s.toBooleanStrictOrNull() ?: s
    }
    is JsonNull -> null
    is JsonObject -> el.entries.associate { (k, v) -> k to jsonElementToAny(v) }
    is JsonArray -> el.map { jsonElementToAny(it) }
}

fun getReadIds(): Set<String> {
    val f = gitaReadFile()
    if (!f.exists()) return setOf()
    return f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
        try { json.decodeFromString<Map<String, String>>(line)["verse_id"] } catch (_: Exception) { null }
    }.toSet()
}

fun getSequentialVerse(): Map<String, Any?>? {
    val all = getGitaVerses()
    if (all.isEmpty()) return null
    val readIds = getReadIds()
    val total = all.size
    val read = readIds.size
    for (v in all) {
        val id = v["id"]?.toString() ?: ""
        if (id !in readIds) {
            val result = v.toMutableMap()
            result["id"] = id
            result["total_verses"] = total
            result["read_count"] = read
            result["unread_count"] = total - read
            result["already_read"] = false
            return result
        }
    }
    return null
}

fun getVerseById(id: String): Map<String, Any?>? {
    val all = getGitaVerses()
    val readIds = getReadIds()
    val total = all.size
    val read = readIds.size
    val v = all.find { it["id"]?.toString() == id } ?: return null
    val result = v.toMutableMap()
    result["id"] = id
    result["total_verses"] = total
    result["read_count"] = read
    result["unread_count"] = total - read
    result["already_read"] = id in readIds
    return result
}

fun getVerseIndex(id: String): Int {
    val all = getGitaVerses()
    return all.indexOfFirst { it["id"]?.toString() == id }
}

fun getNextVerse(id: String): Map<String, Any?>? {
    val all = getGitaVerses()
    if (all.isEmpty()) return null
    val idx = getVerseIndex(id)
    val nextIdx = if (idx < 0) 0 else (idx + 1) % all.size
    val readIds = getReadIds()
    val total = all.size
    val read = readIds.size
    val vid = all[nextIdx]["id"]?.toString() ?: ""
    val result = all[nextIdx].toMutableMap()
    result["id"] = vid
    result["total_verses"] = total
    result["read_count"] = read
    result["unread_count"] = total - read
    result["already_read"] = vid in readIds
    return result
}

fun getPrevVerse(id: String): Map<String, Any?>? {
    val all = getGitaVerses()
    if (all.isEmpty()) return null
    val idx = getVerseIndex(id)
    val prevIdx = if (idx < 0) all.size - 1 else (idx - 1 + all.size) % all.size
    val readIds = getReadIds()
    val total = all.size
    val read = readIds.size
    val vid = all[prevIdx]["id"]?.toString() ?: ""
    val result = all[prevIdx].toMutableMap()
    result["id"] = vid
    result["total_verses"] = total
    result["read_count"] = read
    result["unread_count"] = total - read
    result["already_read"] = vid in readIds
    return result
}

fun markVerseRead(vid: String) {
    gitaDir().mkdirs()
    val entry = mapOf(
        "id" to "gr_${Random.nextLong().toString(36).take(8)}",
        "date" to LocalDate.now().toString(),
        "ts" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        "verse_id" to vid
    )
    gitaReadFile().appendText(toJson(entry) + "\n")
}

fun isVerseRead(vid: String): Boolean = vid in getReadIds()

fun unmarkVerseRead(vid: String) {
    val f = gitaReadFile()
    if (!f.exists()) return
    val lines = f.readLines()
    val remaining = lines.filter { line ->
        try { json.decodeFromString<Map<String, String>>(line)["verse_id"] != vid }
        catch (_: Exception) { true }
    }
    f.writeText(remaining.joinToString("\n") + if (remaining.isNotEmpty()) "\n" else "")
}

fun addVerseReview(vid: String, rating: Int, text: String, type: String) {
    gitaDir().mkdirs()
    val entry = mapOf(
        "id" to "gv_${Random.nextLong().toString(36).take(8)}",
        "date" to LocalDate.now().toString(),
        "ts" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        "verse_id" to vid, "rating" to rating, "text" to text, "type" to type
    )
    gitaReviewsFile().appendText(toJson(entry) + "\n")
}

fun getVerseReviews(): List<Map<String, Any?>> {
    val f = gitaReviewsFile()
    if (!f.exists()) return listOf()
    return f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
        try { json.decodeFromString<Map<String, Any?>>(line) } catch (_: Exception) { null }
    }
}

fun getReadHistory(): List<Map<String, Any?>> {
    val f = gitaReadFile()
    if (!f.exists()) return listOf()
    return f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
        try { json.decodeFromString<Map<String, Any?>>(line) } catch (_: Exception) { null }
    }
}

fun getAllVerses(): Map<String, Any?> {
    val all = getGitaVerses()
    val readIds = getReadIds()
    val entries = all.map { v ->
        val shlokaObj = v["shloka"] as? Map<*, *>
        val shlokaText = shlokaObj?.get("text") as? String ?: ""
        val hindiObj = v["hindi_translation"] as? Map<*, *>
        val hindiText = hindiObj?.get("text") as? String ?: ""
        mapOf(
            "id" to (v["id"]?.toString() ?: ""),
            "chapter" to (v["chapter"] ?: 1),
            "verse" to (v["verse"] ?: 1),
            "read" to ((v["id"]?.toString() ?: "") in readIds),
            "shloka_preview" to if (shlokaText.length > 80) shlokaText.take(80) + "..." else shlokaText,
            "hindi_preview" to if (hindiText.length > 80) hindiText.take(80) + "..." else hindiText
        )
    }
    val chapterMap = linkedMapOf<Int, MutableList<Map<String, Any?>>>()
    entries.forEach { e ->
        val ch = (e["chapter"] as? Number)?.toInt() ?: 1
        chapterMap.getOrPut(ch) { mutableListOf() }.add(e)
    }
    val chapters = chapterMap.map { (ch, verses) ->
        val chRead = verses.count { it["read"] == true }
        mapOf(
            "number" to ch,
            "name" to (CHAPTER_NAMES[ch] ?: "Chapter $ch"),
            "total" to verses.size,
            "read" to chRead,
            "verses" to verses
        )
    }
    return mapOf(
        "verses" to entries,
        "chapters" to chapters,
        "total" to all.size,
        "read_count" to readIds.size
    )
}

private val CHAPTER_NAMES = mapOf(
    1 to "Arjuna Vishada", 2 to "Sankhya", 3 to "Karma", 4 to "Jnana",
    5 to "Karma Vairagya", 6 to "Dhyana", 7 to "Jnana Vijnana", 8 to "Aksara Brahma",
    9 to "Raja Vidya", 10 to "Vibhuti", 11 to "Visvarupa", 12 to "Bhakti",
    13 to "Prakrti Purusha", 14 to "Gunatraya", 15 to "Purushottama", 16 to "Daivasura",
    17 to "Sraddhatraya", 18 to "Mokshasannyasa"
)

fun getWeeklyVerseStats(): Map<String, Any?> {
    val today = LocalDate.now()
    val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    var read = 0
    var totalRating = 0
    var reviewCount = 0
    var reviewEntries = 0
    for (entry in getReadHistory()) {
        val ed = try { LocalDate.parse(entry["date"] as? String ?: "") } catch (_: Exception) { continue }
        if (ed >= monday) read++
    }
    for (entry in getVerseReviews()) {
        val ed = try { LocalDate.parse(entry["date"] as? String ?: "") } catch (_: Exception) { continue }
        if (ed >= monday) {
            reviewCount++
            val r = entry["rating"] as? Int ?: 0
            if (r > 0) { totalRating += r; reviewEntries++ }
        }
    }
    val avg = if (reviewEntries > 0) {
        (totalRating / reviewEntries.toDouble()).let { String.format("%.1f", it).toDouble() }
    } else null
    return mapOf("week_start" to monday.toString(), "read_count" to read, "review_count" to reviewCount, "average_rating" to avg)
}

