package com.v07.v3.app.server.routes

import android.content.Context
import android.util.Log
import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse
import com.v07.v3.core.data.EventRepository
import com.v07.v3.core.data.V3Thought
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AiSubRouter(
    private val context: Context,
    private val repository: EventRepository
) : SubRouter {

    override fun matches(path: String): Boolean {
        return path.startsWith("/api/weekly-summary") ||
                path.startsWith("/ai") ||
                path.startsWith("/api/v3/ai")
    }

    override suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/api/weekly-summary") -> handleWeeklySummary(method, path, request)
            else -> handleAiRoutes(method, path, request)
        }
    }

    private suspend fun handleWeeklySummary(method: String, path: String, request: HttpRequest): HttpResponse {
        return when {
            path.startsWith("/api/weekly-summary/aggregate") && method == "GET" -> {
                val start = request.queryParams["start"]
                val end = request.queryParams["end"]
                val offset = request.queryParams["offset"]?.toIntOrNull() ?: 0
                val data = com.v07.dashboard.WeeklyAggregator.aggregate(start, end, offset)
                val json = JSONObject(data)
                HttpResponse.json(json.toString())
            }
            path.startsWith("/api/weekly-summary/generate") && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val offset = body.optInt("offset", 0)
                val start = body.optString("start").ifBlank { null }
                val end = body.optString("end").ifBlank { null }
                val data = com.v07.dashboard.WeeklyAggregator.aggregate(start, end, offset)
                val promptContext = data["prompt_context"] as? String ?: ""

                val rangeMap = data["range"] as? Map<*, *>
                val sDate = rangeMap?.get("start") ?: "Monday"
                val eDate = rangeMap?.get("end") ?: "Sunday"

                val focusHours = (data["focus"] as? Map<*, *>)?.get("total_hours") ?: 0
                val tasksVelocity = (data["tasks"] as? Map<*, *>)?.get("velocity_pct") ?: 0
                val habitAdherence = (data["habits"] as? Map<*, *>)?.get("overall_consistency_pct") ?: 0
                val summaryLead = promptContext.lines().take(15).joinToString("\n")

                val markdown = buildString {
                    appendLine("## 🌿 Weekly Telemetry Synthesis ($sDate to $eDate)")
                    appendLine()
                    appendLine("### Executive Summary")
                    appendLine(summaryLead)
                    appendLine()
                    appendLine("### Key Achievements & Deep Work")
                    appendLine("- **Total Deep Work**: $focusHours hours")
                    appendLine("- **Tasks Velocity**: $tasksVelocity% completed")
                    appendLine("- **Habit Discipline**: $habitAdherence% adherence")
                    appendLine()
                    appendLine("### Sanctuary Mindset Note")
                    appendLine("> \"Action is thy duty, fruit is not thy concern.\" — Shrimad Bhagavad Gita (2.47)")
                }

                HttpResponse.json(JSONObject().put("ok", true).put("markdown", markdown).toString())
            }
            path.startsWith("/api/weekly-summary/save") && method == "POST" -> {
                val body = JSONObject(request.body.ifBlank { "{}" })
                val start = body.optString("start", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                val end = body.optString("end", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                val content = body.optString("content", "")
                val title = body.optString("title", "Weekly-Review-$start-to-$end")

                try {
                    com.v07.dashboard.WeeklyAggregator.saveToVault(start, end, content, title)
                } catch (e: Exception) {
                    Log.w("AiSubRouter", "Vault save fallback: ${e.message}")
                }

                // Also persist into thoughts table for instant access in Sanctuary
                val thought = V3Thought(
                    id = "wr_" + UUID.randomUUID().toString().take(8),
                    timestamp = System.currentTimeMillis(),
                    date = start,
                    topic = "Weekly Review",
                    content = content,
                    pinned = true,
                    tags = "review,weekly,synthesis"
                )
                repository.insertThought(thought)

                HttpResponse.json(JSONObject().put("ok", true).put("saved", true).toString())
            }
            else -> HttpResponse.error("Endpoint not found: $path", 404)
        }
    }

    private suspend fun handleAiRoutes(method: String, path: String, request: HttpRequest): HttpResponse {
        com.v07.dashboard.GeminiConfig.init(context)
        return when {
            path == "/ai/status" && method == "GET" -> {
                val configured = com.v07.dashboard.GeminiConfig.isConfigured
                val json = JSONObject().apply {
                    put("ok", true)
                    put("configured", configured)
                    put("hasAccess", true)
                    put("defaultModel", com.v07.dashboard.GeminiConfig.defaultModel)
                    put("provider", com.v07.dashboard.GeminiConfig.provider)
                    put("maskedKey", com.v07.dashboard.GeminiConfig.maskedKey())
                    put("models", JSONArray(com.v07.dashboard.GeminiConfig.AVAILABLE_MODELS))
                    put("usage", JSONObject(com.v07.dashboard.GeminiManager.getUsageStats()))
                }
                HttpResponse.json(json.toString())
            }
            path == "/ai/models" && method == "GET" -> {
                val json = JSONObject().apply {
                    put("ok", true)
                    put("models", JSONArray(com.v07.dashboard.GeminiConfig.AVAILABLE_MODELS))
                    put("defaultModel", com.v07.dashboard.GeminiConfig.defaultModel)
                }
                HttpResponse.json(json.toString())
            }
            path == "/ai/test" && method == "POST" -> {
                val body = if (request.body.isNotBlank()) JSONObject(request.body) else JSONObject()
                val reqKey = body.optString("apiKey").trim().ifBlank { null }
                val reqModel = body.optString("model").trim().ifBlank { null }
                val result = withContext(Dispatchers.IO) {
                    com.v07.dashboard.GeminiClient.testConnection(model = reqModel, apiKey = reqKey)
                }
                if (result.ok) {
                    if (reqKey != null && (com.v07.dashboard.GeminiConfig.apiKey.isBlank() || com.v07.dashboard.GeminiConfig.apiKey == reqKey)) {
                        com.v07.dashboard.GeminiConfig.apiKey = reqKey
                    }
                    if (reqModel != null && reqModel.isNotBlank()) {
                        com.v07.dashboard.GeminiConfig.defaultModel = reqModel
                    }
                }
                val json = JSONObject().apply {
                    put("ok", result.ok)
                    put("model", result.model)
                    put("latencyMs", result.latencyMs)
                    put("error", result.error ?: "")
                    put("reply", result.text ?: "")
                    put("maskedKey", com.v07.dashboard.GeminiConfig.maskedKey())
                }
                HttpResponse.json(json.toString())
            }
            path == "/ai/generate" && method == "POST" -> {
                val pluginId = request.getHeader("X-Plugin-Id") ?: request.queryParams["pluginId"]
                if (!com.v07.dashboard.GeminiManager.canPluginAccessAI(pluginId)) {
                    return HttpResponse.json(JSONObject().apply {
                        put("ok", false)
                        put("error", "Permission Denied: Plugin '${pluginId ?: "unknown"}' requires 'ai' permission")
                    }.toString(), 403)
                }

                val body = if (request.body.isNotBlank()) JSONObject(request.body) else JSONObject()
                val prompt = body.optString("prompt").trim()
                if (prompt.isBlank()) return HttpResponse.error("Missing required 'prompt'", 400)

                val sysInst = body.optString("systemInstruction").ifBlank { null }
                val model = body.optString("model").ifBlank { null }
                val temp = if (body.has("temperature")) body.optDouble("temperature").toFloat() else null
                val maxTok = if (body.has("maxTokens")) body.optInt("maxTokens") else null
                val format = body.optString("responseFormat", "text")
                val schema = body.optJSONObject("jsonSchema")?.let { s ->
                    val map = mutableMapOf<String, Any?>()
                    s.keys().forEach { k -> map[k] = s.get(k) }
                    map
                }

                val result = withContext(Dispatchers.IO) {
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

                val json = JSONObject().apply {
                    put("ok", result.ok)
                    put("text", result.text ?: "")
                    put("data", JSONObject.wrap(result.data) ?: JSONObject.NULL)
                    put("model", result.model)
                    put("usage", JSONObject(result.usage))
                    put("latencyMs", result.latencyMs)
                    put("error", result.error ?: "")
                }
                HttpResponse.json(json.toString())
            }
            path == "/ai/chat" && method == "POST" -> {
                val pluginId = request.getHeader("X-Plugin-Id") ?: request.queryParams["pluginId"]
                if (!com.v07.dashboard.GeminiManager.canPluginAccessAI(pluginId)) {
                    return HttpResponse.json(JSONObject().apply {
                        put("ok", false)
                        put("error", "Permission Denied: Plugin '${pluginId ?: "unknown"}' requires 'ai' permission")
                    }.toString(), 403)
                }

                val body = if (request.body.isNotBlank()) JSONObject(request.body) else JSONObject()
                val rawMessages = body.optJSONArray("messages") ?: JSONArray()
                val messages = mutableListOf<Map<String, String>>()
                for (i in 0 until rawMessages.length()) {
                    val m = rawMessages.optJSONObject(i) ?: continue
                    messages.add(mapOf(
                        "role" to m.optString("role", "user"),
                        "content" to m.optString("content", m.optString("text", ""))
                    ))
                }
                val sysInst = body.optString("systemInstruction").ifBlank { null }
                val model = body.optString("model").ifBlank { null }
                val temp = if (body.has("temperature")) body.optDouble("temperature").toFloat() else null

                val result = withContext(Dispatchers.IO) {
                    com.v07.dashboard.GeminiClient.chat(messages, sysInst, model, temp)
                }

                if (result.ok) {
                    val promptTok = (result.usage["promptTokens"] as? Number)?.toInt() ?: 0
                    val candTok = (result.usage["candidatesTokens"] as? Number)?.toInt() ?: 0
                    com.v07.dashboard.GeminiManager.recordUsage(promptTok, candTok)
                }

                val json = JSONObject().apply {
                    put("ok", result.ok)
                    put("text", result.text ?: "")
                    put("model", result.model)
                    put("usage", JSONObject(result.usage))
                    put("latencyMs", result.latencyMs)
                    put("error", result.error ?: "")
                }
                HttpResponse.json(json.toString())
            }
            else -> HttpResponse.error("Unknown AI endpoint: $path", 404)
        }
    }
}
