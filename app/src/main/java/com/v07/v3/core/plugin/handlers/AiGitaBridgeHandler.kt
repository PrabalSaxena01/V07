package com.v07.v3.core.plugin.handlers

import android.content.Context
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.GitaProvider
import com.v07.v3.core.data.V3Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AiGitaBridgeHandler(
    private val context: Context,
    private val repository: EventRepository
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("ai:") || method.startsWith("gita:")
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "ai:status" -> {
                com.v07.dashboard.GeminiConfig.init(context)
                val configured = com.v07.dashboard.GeminiConfig.isConfigured
                val res = JSONObject().apply {
                    put("configured", configured)
                    put("defaultModel", com.v07.dashboard.GeminiConfig.defaultModel)
                    put("provider", com.v07.dashboard.GeminiConfig.provider)
                    put("maskedKey", com.v07.dashboard.GeminiConfig.maskedKey())
                    put("models", JSONArray(com.v07.dashboard.GeminiConfig.AVAILABLE_MODELS))
                }
                bridgeSuccess(res)
            }
            "ai:generate" -> {
                com.v07.dashboard.GeminiConfig.init(context)
                val prompt = params.optString("prompt").trim()
                if (prompt.isBlank()) return bridgeError("Missing required 'prompt'")
                val sysInst = params.optString("systemInstruction").ifBlank { null }
                val model = params.optString("model").ifBlank { null }
                val temp = if (params.has("temperature")) params.optDouble("temperature").toFloat() else null
                val maxTok = if (params.has("maxTokens")) params.optInt("maxTokens") else null
                val format = params.optString("responseFormat", "text")
                val schema = params.optJSONObject("jsonSchema")?.let { s ->
                    val map = mutableMapOf<String, Any?>()
                    s.keys().forEach { k -> map[k] = s.get(k) }
                    map
                }

                val result = runBlocking(Dispatchers.IO) {
                    com.v07.dashboard.GeminiClient.generate(
                        com.v07.dashboard.GeminiClient.GenerateOptions(
                            prompt = prompt,
                            systemInstruction = sysInst,
                            model = model,
                            temperature = temp,
                            maxTokens = maxTok,
                            responseFormat = format,
                            responseSchema = schema
                        )
                    )
                }
                if (result.ok) {
                    val promptTok = (result.usage["promptTokens"] as? Number)?.toInt() ?: 0
                    val candTok = (result.usage["candidatesTokens"] as? Number)?.toInt() ?: 0
                    com.v07.dashboard.GeminiManager.recordUsage(promptTok, candTok)
                }
                val res = JSONObject().apply {
                    put("ok", result.ok)
                    put("text", result.text ?: "")
                    put("model", result.model)
                    put("latencyMs", result.latencyMs)
                    if (result.error != null) put("error", result.error)
                }
                if (result.ok) bridgeSuccess(res) else bridgeError(result.error ?: "AI generation failed")
            }
            "ai:chat" -> {
                com.v07.dashboard.GeminiConfig.init(context)
                val rawMessages = params.optJSONArray("messages") ?: JSONArray()
                val messages = mutableListOf<Map<String, String>>()
                for (i in 0 until rawMessages.length()) {
                    val m = rawMessages.optJSONObject(i) ?: continue
                    messages.add(mapOf(
                        "role" to m.optString("role", "user"),
                        "content" to m.optString("content", m.optString("text", ""))
                    ))
                }
                val sysInst = params.optString("systemInstruction").ifBlank { null }
                val model = params.optString("model").ifBlank { null }
                val temp = if (params.has("temperature")) params.optDouble("temperature").toFloat() else null

                val result = runBlocking(Dispatchers.IO) {
                    com.v07.dashboard.GeminiClient.chat(messages, sysInst, model, temp)
                }
                if (result.ok) {
                    val promptTok = (result.usage["promptTokens"] as? Number)?.toInt() ?: 0
                    val candTok = (result.usage["candidatesTokens"] as? Number)?.toInt() ?: 0
                    com.v07.dashboard.GeminiManager.recordUsage(promptTok, candTok)
                }
                val res = JSONObject().apply {
                    put("ok", result.ok)
                    put("text", result.text ?: "")
                    put("model", result.model)
                    put("latencyMs", result.latencyMs)
                    if (result.error != null) put("error", result.error)
                }
                if (result.ok) bridgeSuccess(res) else bridgeError(result.error ?: "AI chat failed")
            }
            "gita:getVerse", "gita:getDaily" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 0)
                val vs = params.optInt("verse", 0)
                val verseObj = if (ch > 0 && vs > 0) {
                    gitaProvider.getVerse(ch, vs)
                } else {
                    gitaProvider.getDailyVerse()
                }
                if (verseObj != null) {
                    bridgeSuccess(verseObj)
                } else {
                    bridgeError("Verse not found")
                }
            }
            "gita:nextVerse" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 2)
                val vs = params.optInt("verse", 47)
                val verseObj = gitaProvider.getNextVerse(ch, vs)
                if (verseObj != null) bridgeSuccess(verseObj) else bridgeError("Next verse not found")
            }
            "gita:prevVerse" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 2)
                val vs = params.optInt("verse", 47)
                val verseObj = gitaProvider.getPrevVerse(ch, vs)
                if (verseObj != null) bridgeSuccess(verseObj) else bridgeError("Prev verse not found")
            }
            "gita:randomVerse" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val verseObj = gitaProvider.getRandomVerse()
                if (verseObj != null) bridgeSuccess(verseObj) else bridgeError("Random verse not found")
            }
            "gita:markRead" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 0)
                val vs = params.optInt("verse", 0)
                val isRead = params.optBoolean("is_read", true)
                if (ch > 0 && vs > 0) {
                    val progress = gitaProvider.markVerseRead(ch, vs, isRead)
                    bridgeSuccess(JSONObject().put("success", true).put("progress", progress))
                } else {
                    bridgeError("Missing chapter or verse")
                }
            }
            "gita:unmarkRead" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 0)
                val vs = params.optInt("verse", 0)
                val progress = gitaProvider.markVerseRead(ch, vs, false)
                bridgeSuccess(JSONObject().put("success", true).put("progress", progress))
            }
            "gita:getProgress" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                bridgeSuccess(gitaProvider.getProgress())
            }
            "gita:getNextUnread" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 1)
                val vs = params.optInt("verse", 1)
                val unread = gitaProvider.getNextUnreadVerse(ch, vs)
                if (unread != null) bridgeSuccess(unread) else bridgeError("All verses read!")
            }
            "gita:saveReflection", "gita:saveReview" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 0)
                val vs = params.optInt("verse", 0)
                val text = params.optString("text", "").trim()
                val rating = params.optInt("rating", 3)
                if (ch > 0 && vs > 0 && text.isNotBlank()) {
                    val entry = gitaProvider.saveReflection(ch, vs, text, rating)
                    try {
                        val now = System.currentTimeMillis()
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
                        val tsStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(now))
                        repository.insertEvent(
                            V3Event(
                                id = "gv_" + UUID.randomUUID().toString().take(8),
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
                    } catch (_: Exception) {}
                    bridgeSuccess(JSONObject().put("success", true).put("entry", entry))
                } else {
                    bridgeError("Missing chapter, verse, or reflection text")
                }
            }
            "gita:getReflections", "gita:getReviews" -> {
                val gitaProvider = GitaProvider.getInstance(context)
                val ch = params.optInt("chapter", 0)
                val vs = params.optInt("verse", 0)
                val list = gitaProvider.getReflectionsForVerse(ch, vs)
                bridgeSuccess(JSONObject().put("reflections", list))
            }
            else -> null
        }
    }
}
