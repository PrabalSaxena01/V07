package com.v07.v3.core.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * GitaProvider
 *
 * High-performance, cached repository for the 700+ verses of Bhagavad Gita.
 * Prioritizes gita.json from the legacy data folder on device storage,
 * and seamlessly falls back to bundled app assets.
 * Also tracks reading status (gita_read.jsonl) and personal reflections (gita_reviews.jsonl).
 */
class GitaProvider private constructor(private val context: Context) {
    companion object {
        private const val TAG = "GitaProvider"
        const val TOTAL_GITA_VERSES = 701

        @Volatile
        private var instance: GitaProvider? = null

        fun getInstance(context: Context): GitaProvider {
            return instance ?: synchronized(this) {
                instance ?: GitaProvider(context.applicationContext).also {
                    instance = it
                    it.preWarmAsync()
                }
            }
        }
    }

    private var verses: List<JSONObject>? = null
    private val verseMap = ConcurrentHashMap<String, JSONObject>()
    private val readIds = mutableSetOf<String>()
    private val reviewsList = mutableListOf<JSONObject>()
    private val isLoading = AtomicBoolean(false)
    private val isLoaded = AtomicBoolean(false)

    fun preWarmAsync() {
        if (isLoaded.get() || isLoading.get()) return
        Thread {
            ensureLoaded()
        }.apply {
            name = "GitaProvider-Prewarm"
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    @Synchronized
    fun ensureLoaded() {
        if (isLoaded.get()) return
        isLoading.set(true)
        val startTime = System.currentTimeMillis()
        try {
            val (inputStream, sourceDescription) = resolveGitaInputStream()
            Log.i(TAG, "Loading Bhagavad Gita from source: $sourceDescription")

            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val jsonStr = reader.use { it.readText() }
            val jsonArr = JSONArray(jsonStr)
            val list = ArrayList<JSONObject>(jsonArr.length())

            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                list.add(obj)
                val ch = obj.optInt("chapter")
                val vs = obj.optInt("verse")
                val id = obj.optString("id", "$ch.$vs")
                verseMap["$ch.$vs"] = obj
                verseMap[id] = obj
            }
            verses = list

            // Load read history from legacy data folder or local file
            loadReadHistory()
            loadReviewsHistory()

            isLoaded.set(true)
            val elapsed = System.currentTimeMillis() - startTime
            Log.i(TAG, "Bhagavad Gita loaded successfully in ${elapsed}ms (${list.size} verses, ${readIds.size} read)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Bhagavad Gita: ${e.message}", e)
            if (verses == null) verses = emptyList()
        } finally {
            isLoading.set(false)
        }
    }

    /**
     * Resolves gita.json prioritizing the legacy data folder, vault path, external storage, and assets.
     */
    private fun resolveGitaInputStream(): Pair<InputStream, String> {
        val candidates = mutableListOf<File>()

        try {
            // Check legacy data folder in configured vault path
            val vaultPath = com.v07.dashboard.VaultConfig.getVaultPath()
            if (vaultPath.isNotBlank()) {
                candidates.add(File(vaultPath, "Data/gita/gita.json"))
                candidates.add(File(vaultPath, "08 - System/Data/gita/gita.json"))
                candidates.add(File(vaultPath, "gita.json"))
            }
        } catch (_: Throwable) {}

        // Standard Android legacy storage paths
        candidates.add(File("/storage/emulated/0/V07Data/Data/gita/gita.json"))
        candidates.add(File("/storage/emulated/0/V07Data/08 - System/Data/gita/gita.json"))
        candidates.add(File("/storage/emulated/0/V07Data/gita.json"))
        candidates.add(File("/storage/emulated/0/V07Journal/Data/gita/gita.json"))

        // App internal/external file paths
        context.getExternalFilesDir(null)?.let {
            candidates.add(File(it, "Data/gita/gita.json"))
            candidates.add(File(it, "gita.json"))
        }
        candidates.add(File(context.filesDir, "Data/gita/gita.json"))
        candidates.add(File(context.filesDir, "gita.json"))

        for (candidate in candidates) {
            try {
                if (candidate.exists() && candidate.isFile && candidate.canRead() && candidate.length() > 1000) {
                    val stream = candidate.inputStream()
                    return Pair(stream, "File: ${candidate.absolutePath} (${candidate.length()} bytes)")
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Cannot access candidate file ${candidate.absolutePath}: ${e.message}")
            }
        }

        // Bundled APK asset fallback
        return Pair(context.assets.open("gita.json"), "APK Asset: assets/gita.json")
    }

    private fun resolveGitaReadFile(): File? {
        val candidates = mutableListOf<File>()
        try {
            val vaultPath = com.v07.dashboard.VaultConfig.getVaultPath()
            if (vaultPath.isNotBlank()) {
                candidates.add(File(vaultPath, "Data/gita/gita_read.jsonl"))
                candidates.add(File(vaultPath, "08 - System/Data/gita/gita_read.jsonl"))
            }
        } catch (_: Throwable) {}
        candidates.add(File("/storage/emulated/0/V07Data/Data/gita/gita_read.jsonl"))
        candidates.add(File("/storage/emulated/0/V07Data/08 - System/Data/gita/gita_read.jsonl"))
        candidates.add(File(context.filesDir, "gita_read.jsonl"))

        for (f in candidates) {
            try {
                if (f.exists() && f.canRead()) return f
            } catch (_: Throwable) {}
        }
        return File(context.filesDir, "gita_read.jsonl")
    }

    private fun resolveGitaReviewsFile(): File? {
        val candidates = mutableListOf<File>()
        try {
            val vaultPath = com.v07.dashboard.VaultConfig.getVaultPath()
            if (vaultPath.isNotBlank()) {
                candidates.add(File(vaultPath, "Data/gita/gita_reviews.jsonl"))
                candidates.add(File(vaultPath, "08 - System/Data/gita/gita_reviews.jsonl"))
            }
        } catch (_: Throwable) {}
        candidates.add(File("/storage/emulated/0/V07Data/Data/gita/gita_reviews.jsonl"))
        candidates.add(File("/storage/emulated/0/V07Data/08 - System/Data/gita/gita_reviews.jsonl"))
        candidates.add(File(context.filesDir, "gita_reviews.jsonl"))

        for (f in candidates) {
            try {
                if (f.exists() && f.canRead()) return f
            } catch (_: Throwable) {}
        }
        return File(context.filesDir, "gita_reviews.jsonl")
    }

    private fun loadReadHistory() {
        readIds.clear()
        val file = resolveGitaReadFile()
        if (file != null && file.exists()) {
            try {
                file.readLines().forEach { line ->
                    if (line.isNotBlank()) {
                        try {
                            val obj = JSONObject(line)
                            val vid = obj.optString("verse_id")
                            if (vid.isNotBlank()) readIds.add(vid)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error loading gita_read.jsonl: ${e.message}")
            }
        }
        // Also check bundled assets/gita_read.jsonl if readIds is empty
        if (readIds.isEmpty()) {
            try {
                context.assets.open("gita_read.jsonl").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            try {
                                val obj = JSONObject(line)
                                val vid = obj.optString("verse_id")
                                if (vid.isNotBlank()) readIds.add(vid)
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadReviewsHistory() {
        reviewsList.clear()
        val file = resolveGitaReviewsFile()
        if (file != null && file.exists()) {
            try {
                file.readLines().forEach { line ->
                    if (line.isNotBlank()) {
                        try {
                            reviewsList.add(JSONObject(line))
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }
        if (reviewsList.isEmpty()) {
            try {
                context.assets.open("gita_reviews.jsonl").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            try {
                                reviewsList.add(JSONObject(line))
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun enrichVerse(verseObj: JSONObject): JSONObject {
        val enriched = JSONObject(verseObj.toString())
        val ch = enriched.optInt("chapter")
        val vs = enriched.optInt("verse")
        val vid = enriched.optString("id", "$ch.$vs")
        val isRead = readIds.contains(vid) || readIds.contains("$ch.$vs")
        enriched.put("already_read", isRead)
        enriched.put("read_count", readIds.size)
        enriched.put("total_verses", TOTAL_GITA_VERSES)
        enriched.put("unread_count", TOTAL_GITA_VERSES - readIds.size)
        return enriched
    }

    fun getVerse(chapter: Int, verse: Int): JSONObject? {
        ensureLoaded()
        val key = "$chapter.$verse"
        val found = verseMap[key] ?: verses?.find { it.optInt("chapter") == chapter && it.optInt("verse") == verse }
        return found?.let { enrichVerse(it) }
    }

    fun getDailyVerse(): JSONObject? {
        ensureLoaded()
        val all = verses ?: return null
        if (all.isEmpty()) return null
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1) % all.size
        val v = all.getOrNull(index) ?: all.first()
        return enrichVerse(v)
    }

    fun getRandomVerse(): JSONObject? {
        ensureLoaded()
        val all = verses ?: return null
        if (all.isEmpty()) return null
        return enrichVerse(all.random())
    }

    fun getNextVerse(currentChapter: Int, currentVerse: Int): JSONObject? {
        ensureLoaded()
        val all = verses ?: return null
        if (all.isEmpty()) return null
        val idx = all.indexOfFirst { it.optInt("chapter") == currentChapter && it.optInt("verse") == currentVerse }
        val nextIdx = if (idx == -1 || idx == all.lastIndex) 0 else idx + 1
        return enrichVerse(all[nextIdx])
    }

    fun getPrevVerse(currentChapter: Int, currentVerse: Int): JSONObject? {
        ensureLoaded()
        val all = verses ?: return null
        if (all.isEmpty()) return null
        val idx = all.indexOfFirst { it.optInt("chapter") == currentChapter && it.optInt("verse") == currentVerse }
        val prevIdx = if (idx <= 0) all.lastIndex else idx - 1
        return enrichVerse(all[prevIdx])
    }

    fun getNextUnreadVerse(currentChapter: Int = 1, currentVerse: Int = 1): JSONObject? {
        ensureLoaded()
        val all = verses ?: return null
        if (all.isEmpty()) return null

        val startIdx = all.indexOfFirst { it.optInt("chapter") == currentChapter && it.optInt("verse") == currentVerse }
        val searchOrder = if (startIdx >= 0) {
            all.subList(startIdx, all.size) + all.subList(0, startIdx)
        } else {
            all
        }

        for (v in searchOrder) {
            val vid = v.optString("id", "${v.optInt("chapter")}.${v.optInt("verse")}")
            if (!readIds.contains(vid)) {
                return enrichVerse(v)
            }
        }
        return enrichVerse(all.first())
    }

    fun markVerseRead(chapter: Int, verse: Int, isRead: Boolean): JSONObject {
        ensureLoaded()
        val vid = "$chapter.$verse"
        if (isRead) {
            readIds.add(vid)
            try {
                val file = resolveGitaReadFile() ?: File(context.filesDir, "gita_read.jsonl")
                file.parentFile?.mkdirs()
                val entry = JSONObject().apply {
                    put("id", "gr_" + (100000..999999).random().toString(36))
                    put("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
                    put("ts", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date()))
                    put("verse_id", vid)
                }
                file.appendText(entry.toString() + "\n")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist gita_read: ${e.message}")
            }
        } else {
            readIds.remove(vid)
            try {
                val file = resolveGitaReadFile()
                if (file != null && file.exists()) {
                    val remaining = file.readLines().filter { line ->
                        try {
                            JSONObject(line).optString("verse_id") != vid
                        } catch (_: Exception) { true }
                    }
                    file.writeText(remaining.joinToString("\n") + if (remaining.isNotEmpty()) "\n" else "")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update gita_read unmark: ${e.message}")
            }
        }

        return getProgress()
    }

    fun getProgress(): JSONObject {
        ensureLoaded()
        val readCount = readIds.size
        val total = TOTAL_GITA_VERSES
        val pct = if (total > 0) Math.round((readCount.toDouble() / total.toDouble()) * 1000.0) / 10.0 else 0.0
        return JSONObject().apply {
            put("read_count", readCount)
            put("total_verses", total)
            put("unread_count", Math.max(0, total - readCount))
            put("percentage", pct)
        }
    }

    fun saveReflection(chapter: Int, verse: Int, text: String, rating: Int = 3): JSONObject {
        ensureLoaded()
        val vid = "$chapter.$verse"
        val entry = JSONObject().apply {
            put("id", "gv_" + (100000..999999).random().toString(36))
            put("date", SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
            put("ts", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date()))
            put("verse_id", vid)
            put("chapter", chapter)
            put("verse", verse)
            put("rating", rating)
            put("text", text.trim())
            put("type", "reflection")
        }
        reviewsList.add(entry)
        try {
            val file = resolveGitaReviewsFile() ?: File(context.filesDir, "gita_reviews.jsonl")
            file.parentFile?.mkdirs()
            file.appendText(entry.toString() + "\n")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write reflection to reviews file: ${e.message}")
        }
        return entry
    }

    fun getReflectionsForVerse(chapter: Int, verse: Int): JSONArray {
        ensureLoaded()
        val vid = "$chapter.$verse"
        val arr = JSONArray()
        reviewsList.filter { it.optString("verse_id") == vid }.forEach {
            arr.put(it)
        }
        return arr
    }
}
