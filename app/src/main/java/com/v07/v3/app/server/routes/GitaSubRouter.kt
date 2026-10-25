package com.v07.v3.app.server.routes

import android.content.Context
import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.GitaProvider
import com.v07.v3.core.data.V3Event
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GitaSubRouter(
    private val context: Context,
    private val repository: EventRepository
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/gita") || path.startsWith("/api/v3/gita")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        val clean = path.removePrefix("/api/v3")
        val gitaProvider = GitaProvider.getInstance(context)

        return when {
            clean == "/gita/today" && method == "GET" -> {
                val v = gitaProvider.getDailyVerse()
                if (v != null) HttpResponse.json(v.toString())
                else HttpResponse.error("Daily verse not available", 404)
            }
            clean.startsWith("/gita/verse") && method == "GET" -> {
                val verseId = clean.removePrefix("/gita/verse/").removePrefix("/gita/verse").trim().ifBlank {
                    request.queryParams["id"] ?: request.queryParams["verse_id"] ?: ""
                }
                val parts = verseId.split(".")
                val ch = parts.getOrNull(0)?.toIntOrNull() ?: 1
                val vs = parts.getOrNull(1)?.toIntOrNull() ?: 1
                val v = gitaProvider.getVerse(ch, vs)
                if (v != null) HttpResponse.json(v.toString())
                else HttpResponse.error("Verse $verseId not found", 404)
            }
            clean == "/gita/all" && method == "GET" -> {
                val data = gitaProvider.getTrackerData()
                HttpResponse.json(data.toString())
            }
            clean == "/gita/read" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val verseId = body.optString("verse_id", request.queryParams["verse_id"] ?: "")
                val parts = verseId.split(".")
                val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val vs = parts.getOrNull(1)?.toIntOrNull() ?: 0
                if (ch > 0 && vs > 0) {
                    val progress = gitaProvider.markVerseRead(ch, vs, true)
                    HttpResponse.json(JSONObject().put("ok", true).put("progress", progress).toString())
                } else {
                    HttpResponse.error("Missing or invalid verse_id", 400)
                }
            }
            clean == "/gita/unread" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val verseId = body.optString("verse_id", request.queryParams["verse_id"] ?: "")
                val parts = verseId.split(".")
                val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val vs = parts.getOrNull(1)?.toIntOrNull() ?: 0
                if (ch > 0 && vs > 0) {
                    val progress = gitaProvider.markVerseRead(ch, vs, false)
                    HttpResponse.json(JSONObject().put("ok", true).put("progress", progress).toString())
                } else {
                    HttpResponse.error("Missing or invalid verse_id", 400)
                }
            }
            clean == "/gita/review" && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val verseId = body.optString("verse_id", "")
                val text = body.optString("text", "").trim()
                val rating = body.optInt("rating", 3)
                val parts = verseId.split(".")
                val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val vs = parts.getOrNull(1)?.toIntOrNull() ?: 0

                if (ch > 0 && vs > 0 && text.isNotBlank()) {
                    val entry = gitaProvider.saveReflection(ch, vs, text, rating)
                    val now = System.currentTimeMillis()
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                    val tsStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                    val id = "gv_" + UUID.randomUUID().toString().take(8)

                    repository.insertEvent(
                        V3Event(
                            id = id,
                            category = "gita",
                            date = dateStr,
                            ts = tsStr,
                            timestamp = now,
                            title = "Gita Reflection · BG $ch.$vs",
                            content = text,
                            score = rating,
                            metadataJson = entry.toString()
                        )
                    )
                    HttpResponse.json(JSONObject().put("ok", true).put("entry", entry).toString(), 201)
                } else {
                    HttpResponse.error("Missing verse_id or text", 400)
                }
            }
            clean.startsWith("/gita/thoughts") && method == "GET" -> {
                val verseId = request.queryParams["verse_id"] ?: ""
                val parts = verseId.split(".")
                val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val vs = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val reflections = if (ch > 0 && vs > 0) gitaProvider.getReflectionsForVerse(ch, vs) else JSONArray()
                val thoughtsArr = JSONArray()
                for (i in 0 until reflections.length()) {
                    val r = reflections.getJSONObject(i)
                    thoughtsArr.put(JSONObject().apply {
                        put("id", r.optString("id"))
                        put("ts", r.optString("ts"))
                        put("text", r.optString("text"))
                        put("verse_id", r.optString("verse_id"))
                    })
                }
                HttpResponse.json(JSONObject().put("ok", true).put("thoughts", thoughtsArr).toString())
            }
            else -> HttpResponse.error("Gita endpoint not found: $path", 404)
        }
    }
}
