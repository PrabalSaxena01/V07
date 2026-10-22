package com.v07.v3.core.plugin.handlers

import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Event
import com.v07.v3.core.data.V3Session
import com.v07.v3.core.data.V3Task
import com.v07.v3.core.data.V3Thought
import com.v07.v3.core.sync.V3CompanionSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CadenceLedgerBridgeHandler(
    private val repository: EventRepository,
    private val syncManager: V3CompanionSyncManager
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("cadence:") ||
                method.startsWith("ledger:") ||
                method.startsWith("capture:") ||
                method.startsWith("entries:")
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "cadence:checkin" -> {
                val mood = params.optInt("mood", 3)
                val energy = params.optInt("energy", 3)
                val focus = params.optInt("focus", 3)
                val intention = params.optString("intention", "").trim()
                val now = System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                val meta = JSONObject().apply {
                    put("mood", mood)
                    put("energy", energy)
                    put("focus", focus)
                    put("intention", intention)
                }.toString()

                val event = V3Event(
                    id = "ci_" + UUID.randomUUID().toString().take(8),
                    category = "checkin",
                    date = date,
                    ts = ts,
                    timestamp = now,
                    title = "Morning Check-In",
                    content = intention,
                    score = mood,
                    metadataJson = meta
                )
                val ok = repository.insertEvent(event)
                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", event.id))
            }
            "cadence:checkout" -> {
                val wins = params.optString("wins", "").trim()
                val lessons = params.optString("lessons", "").trim()
                val reflection = params.optString("reflection", "").trim()
                val score = params.optInt("score", 4)
                val now = System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                val meta = JSONObject().apply {
                    put("wins", wins)
                    put("lessons", lessons)
                    put("reflection", reflection)
                    put("score", score)
                }.toString()

                val event = V3Event(
                    id = "co_" + UUID.randomUUID().toString().take(8),
                    category = "checkout",
                    date = date,
                    ts = ts,
                    timestamp = now,
                    title = "Evening Check-Out",
                    content = reflection,
                    score = score,
                    metadataJson = meta
                )
                repository.insertEvent(event)

                if (wins.isNotBlank()) {
                    repository.insertEvent(
                        V3Event(
                            id = "win_" + UUID.randomUUID().toString().take(8),
                            category = "win",
                            date = date,
                            ts = ts,
                            timestamp = now + 1,
                            title = "Daily Win",
                            content = wins
                        )
                    )
                }
                if (lessons.isNotBlank()) {
                    repository.insertEvent(
                        V3Event(
                            id = "les_" + UUID.randomUUID().toString().take(8),
                            category = "lesson",
                            date = date,
                            ts = ts,
                            timestamp = now + 2,
                            title = "Daily Lesson",
                            content = lessons
                        )
                    )
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", true).put("id", event.id))
            }
            "cadence:getStatus" -> {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val events = repository.getEvents(date = today)
                val ci = events.find { it.category == "checkin" }
                val co = events.find { it.category == "checkout" }
                val res = JSONObject().apply {
                    put("hasCheckin", ci != null)
                    put("hasCheckout", co != null)
                    if (ci != null) {
                        put("checkin", JSONObject().apply {
                            put("id", ci.id)
                            put("intention", ci.content ?: "")
                            put("score", ci.score)
                            put("meta", if (!ci.metadataJson.isNullOrBlank()) JSONObject(ci.metadataJson) else JSONObject())
                        })
                    }
                    if (co != null) {
                        put("checkout", JSONObject().apply {
                            put("id", co.id)
                            put("reflection", co.content ?: "")
                            put("score", co.score)
                            put("meta", if (!co.metadataJson.isNullOrBlank()) JSONObject(co.metadataJson) else JSONObject())
                        })
                    }
                }
                bridgeSuccess(res)
            }
            "ledger:save", "ledger:add" -> {
                val rawCat = params.optString("category", "quick").trim().lowercase()
                val cat = if (rawCat.isBlank()) "quick" else rawCat
                val content = (if (params.has("content")) params.optString("content", "")
                else if (params.has("note")) params.optString("note", "")
                else params.optString("text", "")).trim()
                val title = params.optString("title", "").trim()
                val score = params.optInt("score", 0)
                val now = if (params.has("timestamp") && params.optLong("timestamp") > 0L) params.optLong("timestamp") else System.currentTimeMillis()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = params.optString("date", sdf.format(Date(now)))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date(now))
                val id = params.optString("id", "${cat}_" + UUID.randomUUID().toString().take(10))
                val meta = if (params.has("metadata")) {
                    params.optJSONObject("metadata")?.toString() ?: "{}"
                } else params.optString("metadataJson", "{}")

                val event = V3Event(
                    id = id,
                    category = cat,
                    date = date,
                    ts = ts,
                    timestamp = now,
                    title = if (title.isNotBlank()) title else cat.replaceFirstChar { c -> c.uppercase() },
                    content = content,
                    score = score,
                    metadataJson = meta
                )
                val ok = repository.insertEvent(event)
                if (cat == "thought" || cat == "quick") {
                    try {
                        repository.insertThought(
                            V3Thought(
                                id = event.id,
                                timestamp = now,
                                date = date,
                                topic = event.title ?: "Quick Entry",
                                content = content
                            )
                        )
                    } catch (_: Exception) {}
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                val res = JSONObject().apply {
                    put("success", ok)
                    put("id", event.id)
                    put("category", event.category)
                    put("title", event.title)
                    put("content", event.content)
                    put("date", event.date)
                    put("timeFormatted", timeFmt.format(Date(now)))
                    put("timestamp", event.timestamp)
                    put("score", event.score)
                    put("metadataJson", event.metadataJson)
                }
                bridgeSuccess(res)
            }
            "ledger:query", "ledger:list" -> {
                val cat = params.optString("category", "").ifBlank { null }
                val start = params.optString("startDate", "").ifBlank { null }
                val end = params.optString("endDate", "").ifBlank { null }
                val q = params.optString("query", "").ifBlank { null }
                val limit = params.optInt("limit", 200)

                val events = repository.queryLedgerEntries(cat, start, end, q, limit)
                val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                val arr = JSONArray()
                events.forEach { e ->
                    arr.put(JSONObject().apply {
                        put("id", e.id)
                        put("category", e.category)
                        put("displayCategory", when (e.category) {
                            "quick" -> "Quick"
                            "thought" -> "Quick"
                            "win" -> "Daily Win"
                            "lesson" -> "Daily Lesson"
                            "pushups" -> "Physical"
                            else -> e.category.replaceFirstChar { c -> c.uppercase() }
                        })
                        put("title", e.title ?: "")
                        put("content", e.content ?: "")
                        put("date", e.date)
                        put("timeFormatted", timeFmt.format(Date(e.timestamp)))
                        put("timestamp", e.timestamp)
                        put("score", e.score)
                        put("metadataJson", e.metadataJson ?: "")
                    })
                }
                bridgeSuccess(JSONObject().put("entries", arr))
            }
            "ledger:delete" -> {
                val id = params.optString("id", "")
                val cat = params.optString("category", "quick")
                val title = params.optString("title", "Ledger Entry")
                val content = params.optString("content", "")
                val timestamp = params.optLong("timestamp", System.currentTimeMillis())
                val dataJson = JSONObject().apply {
                    put("id", id)
                    put("title", title)
                    put("content", content)
                    put("category", cat)
                    put("timestamp", timestamp)
                    put("deletedAt", System.currentTimeMillis())
                }.toString()
                val ok = repository.recycleItem(id, cat, dataJson)
                if (ok && id.isNotBlank()) {
                    syncManager.deleteRemoteEvent(id)
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", id))
            }
            "ledger:update" -> {
                val id = params.optString("id", "")
                if (id.isBlank()) {
                    return bridgeError("Entry ID cannot be blank")
                }
                val title = if (params.has("title")) params.optString("title") else null
                val content = if (params.has("content")) params.optString("content") else (if (params.has("note")) params.optString("note") else null)
                val cat = if (params.has("category")) params.optString("category") else null
                val meta = if (params.has("metadata")) {
                    params.optJSONObject("metadata")?.toString()
                } else if (params.has("metadataJson")) {
                    params.optString("metadataJson")
                } else null
                val ok = repository.updateEvent(id, title, content, cat, meta)
                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", id))
            }
            "capture:quick" -> {
                val rawCat = params.optString("category", "quick").trim().lowercase()
                val customCat = params.optString("customCategory", "").trim().lowercase()
                val cat = if (customCat.isNotBlank()) customCat else (if (rawCat.isNotBlank()) rawCat else "quick")
                val noteStr = if (params.has("note")) params.optString("note", "") else (if (params.has("content")) params.optString("content", "") else params.optString("text", ""))
                val content = noteStr.trim()
                val defaultTitle = if (customCat.isNotBlank()) customCat.replaceFirstChar { c -> c.uppercase() } else cat.replaceFirstChar { c -> c.uppercase() }
                val title = params.optString("title", "").ifBlank { defaultTitle }.trim()
                val score = params.optInt("score", 0)
                val count = if (params.has("count")) params.optInt("count", 0) else params.optInt("value", 0)
                val now = System.currentTimeMillis()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                val ts = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))

                val meta = JSONObject().apply {
                    if (count > 0) put("count", count)
                    if (score > 0) put("score", score)
                    if (params.has("metadata")) {
                        val customMeta = params.optJSONObject("metadata")
                        customMeta?.keys()?.forEach { k ->
                            put(k, customMeta.get(k))
                        }
                    } else if (params.has("metadataJson")) {
                        try {
                            val customMeta = JSONObject(params.optString("metadataJson"))
                            customMeta.keys().forEach { k ->
                                put(k, customMeta.get(k))
                            }
                        } catch (_: Exception) {}
                    }
                }.toString()

                val event = V3Event(
                    id = "${cat}_" + UUID.randomUUID().toString().take(8),
                    category = cat,
                    date = date,
                    ts = ts,
                    timestamp = now,
                    title = if (title.isNotBlank()) title else cat.replaceFirstChar { c -> c.uppercase() },
                    content = if (content.isNotBlank()) content else if (count > 0) "$count reps" else "",
                    score = score,
                    metadataJson = meta
                )
                val ok = repository.insertEvent(event)

                if ((cat == "thought" || cat == "quick") && content.isNotBlank()) {
                    repository.insertThought(
                        V3Thought(
                            id = event.id,
                            timestamp = now,
                            date = date,
                            topic = title.ifBlank { "Quick Capture" },
                            content = content
                        )
                    )
                } else if (cat == "session" || cat == "sessions") {
                    val durSec = if (params.has("durationSec")) params.optLong("durationSec", 0L) else (params.optLong("duration", 0L) * 60)
                    val session = V3Session(
                        id = event.id,
                        activity = if (title.isNotBlank()) title else "Focus Session",
                        startTime = now - (durSec * 1000),
                        endTime = now,
                        durationSec = durSec,
                        notes = content
                    )
                    repository.insertSession(session)
                } else if (cat == "task" || cat == "tasks") {
                    val task = V3Task(
                        id = event.id,
                        title = if (title.isNotBlank()) title else (if (content.isNotBlank()) content else "New Task"),
                        priority = params.optString("priority", "MEDIUM").uppercase(),
                        status = "pending",
                        createdAt = now,
                        deadline = params.optString("deadline", "").ifBlank { null }
                    )
                    repository.insertTask(task)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", event.id))
            }
            "entries:query" -> {
                val cat = params.optString("category", "").ifBlank { null }
                val start = params.optString("startDate", "").ifBlank { null }
                val end = params.optString("endDate", "").ifBlank { null }
                val q = params.optString("query", "").ifBlank { null }
                val limit = params.optInt("limit", 200)

                val list = repository.queryUnifiedEntries(
                    category = cat,
                    startDate = start,
                    endDate = end,
                    query = q,
                    limit = limit
                )
                val arr = JSONArray()
                list.forEach { u ->
                    arr.put(JSONObject().apply {
                        put("id", u.id)
                        put("category", u.category)
                        put("displayCategory", u.displayCategory)
                        put("title", u.title)
                        put("subtitle", u.subtitle)
                        put("content", u.content)
                        put("date", u.date)
                        put("timeFormatted", u.timeFormatted)
                        put("timestamp", u.timestamp)
                        u.score?.let { put("score", it) }
                        u.durationSec?.let { put("durationSec", it) }
                        u.tags?.let { put("tags", it) }
                        put("rawJson", u.rawJson)
                    })
                }
                bridgeSuccess(JSONObject().put("entries", arr))
            }
            "entries:delete" -> {
                val id = params.optString("id", "")
                val cat = params.optString("category", "").lowercase()
                val ok = repository.deleteEntityEverywhere(id, cat)
                if (ok && id.isNotBlank()) {
                    syncManager.deleteRemoteEvent(id)
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", id))
            }
            "entries:recycle" -> {
                val originalId = params.optString("originalId", params.optString("id", ""))
                val category = params.optString("category", "general")
                val dataJson = params.optString("dataJson", "{}")
                val ok = repository.recycleItem(originalId, category, dataJson)
                if (ok && originalId.isNotBlank()) {
                    syncManager.deleteRemoteEvent(originalId)
                }
                bridgeSuccess(JSONObject().put("success", ok).put("originalId", originalId))
            }
            "entries:batchDelete" -> {
                val idsArr = params.optJSONArray("ids")
                val idList = mutableListOf<String>()
                if (idsArr != null) {
                    for (i in 0 until idsArr.length()) {
                        val id = idsArr.optString(i, "")
                        if (id.isNotBlank()) idList.add(id)
                    }
                }
                val count = repository.batchDeleteEntities(idList)
                CoroutineScope(Dispatchers.IO).launch {
                    idList.forEach { syncManager.deleteRemoteEvent(it) }
                }
                bridgeSuccess(JSONObject().put("success", true).put("deletedCount", count))
            }
            "entries:batchRecycle" -> {
                val itemsArr = params.optJSONArray("items")
                val tripleList = mutableListOf<Triple<String, String, String>>()
                if (itemsArr != null) {
                    for (i in 0 until itemsArr.length()) {
                        val itm = itemsArr.optJSONObject(i)
                        if (itm != null) {
                            val oid = itm.optString("originalId", itm.optString("id", ""))
                            val cat = itm.optString("category", "general")
                            val data = itm.optString("dataJson", "{}")
                            if (oid.isNotBlank()) {
                                tripleList.add(Triple(oid, cat, data))
                            }
                        }
                    }
                }
                val count = repository.batchRecycleEntities(tripleList)
                CoroutineScope(Dispatchers.IO).launch {
                    tripleList.forEach { syncManager.deleteRemoteEvent(it.first) }
                }
                bridgeSuccess(JSONObject().put("success", true).put("recycledCount", count))
            }
            "entries:update" -> {
                val id = params.optString("id", "")
                if (id.isBlank()) {
                    return bridgeError("Entry ID cannot be blank")
                }
                val title = if (params.has("title")) params.optString("title") else null
                val content = if (params.has("content")) params.optString("content") else (if (params.has("note")) params.optString("note") else null)
                val cat = if (params.has("category")) params.optString("category") else null
                val meta = if (params.has("metadata")) {
                    params.optJSONObject("metadata")?.toString()
                } else if (params.has("metadataJson")) {
                    params.optString("metadataJson")
                } else null

                val ok = repository.updateEvent(id, title, content, cat, meta)
                CoroutineScope(Dispatchers.IO).launch {
                    try { syncManager.syncNow { _, _ -> } } catch (_: Exception) {}
                }
                bridgeSuccess(JSONObject().put("success", ok).put("id", id))
            }
            else -> null
        }
    }
}
