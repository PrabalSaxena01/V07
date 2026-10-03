package com.v07.dashboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Universal AI client supporting Claude 3.5+, Gemini 2.0/1.5, OpenRouter, and OpenAI-compatible gateways.
 * Executes on background I/O dispatcher and handles model-specific formats automatically.
 */
object GeminiClient {

    data class GenerateOptions(
        val prompt: String,
        val systemInstruction: String? = null,
        val model: String? = null,
        val apiKey: String? = null,
        val temperature: Float? = null,
        val maxTokens: Int? = null,
        val responseFormat: String = "text", // "text" or "json"
        val responseSchema: Map<String, Any?>? = null
    )

    data class GenerateResult(
        val ok: Boolean,
        val text: String? = null,
        val data: Any? = null,
        val model: String = "",
        val usage: Map<String, Int> = emptyMap(),
        val error: String? = null,
        val latencyMs: Long = 0L
    )

    /**
     * Executes a prompt completion using the effective provider (Anthropic, OpenRouter, OpenAI, or Gemini).
     */
    suspend fun generate(options: GenerateOptions): GenerateResult = withContext(Dispatchers.IO) {
        val apiKey = options.apiKey?.ifBlank { null } ?: GeminiConfig.apiKey
        if (apiKey.isBlank()) {
            return@withContext GenerateResult(
                ok = false,
                error = "API key is not configured. Please set your key in Settings -> AI / Models."
            )
        }

        val rawModel = options.model?.ifBlank { null } ?: GeminiConfig.defaultModel
        val provider = GeminiConfig.detectEffectiveProvider(rawModel, explicitKey = apiKey)
        val targetModel = GeminiConfig.sanitizeModel(rawModel, provider)
        val startTime = System.currentTimeMillis()

        try {
            val (result, effectiveModel) = executeWithGeminiFallback(
                provider = provider,
                initialModel = targetModel,
                apiKey = apiKey,
                baseUrl = GeminiConfig.baseUrl,
                startTime = startTime,
                payloadBuilder = { m -> buildPayload(provider, m, options) }
            )
            val latency = System.currentTimeMillis() - startTime

            if (!result.isSuccess) {
                return@withContext GenerateResult(
                    ok = false,
                    error = result.errorMessage,
                    latencyMs = latency
                )
            }

            parseResponse(result.body, effectiveModel, latency, options.responseFormat, options.responseSchema != null)
        } catch (e: Exception) {
            GenerateResult(
                ok = false,
                error = e.message ?: "Network error connecting to AI API",
                latencyMs = System.currentTimeMillis() - startTime
            )
        }
    }

    /**
     * Executes a multi-turn chat conversation across supported providers.
     */
    suspend fun chat(
        messages: List<Map<String, String>>,
        systemInstruction: String? = null,
        model: String? = null,
        temperature: Float? = null
    ): GenerateResult = withContext(Dispatchers.IO) {
        val apiKey = GeminiConfig.apiKey
        if (apiKey.isBlank()) {
            return@withContext GenerateResult(
                ok = false,
                error = "API key is not configured. Please set your key in Settings -> AI / Models."
            )
        }

        val rawModel = model?.ifBlank { null } ?: GeminiConfig.defaultModel
        val provider = GeminiConfig.detectEffectiveProvider(rawModel, explicitKey = apiKey)
        val targetModel = GeminiConfig.sanitizeModel(rawModel, provider)
        val startTime = System.currentTimeMillis()

        try {
            val temp = temperature ?: GeminiConfig.temperature

            fun makePayload(m: String): JSONObject {
                val payload = JSONObject()
                when (provider) {
                    "anthropic" -> {
                        payload.put("model", m)
                        payload.put("max_tokens", GeminiConfig.maxOutputTokens)
                        systemInstruction?.takeIf { it.isNotBlank() }?.let { payload.put("system", it) }
                        val msgArray = JSONArray()
                        for (msg in messages) {
                            val role = if (msg["role"] == "assistant" || msg["role"] == "model") "assistant" else "user"
                            msgArray.put(JSONObject().apply {
                                put("role", role)
                                put("content", msg["content"] ?: msg["text"] ?: "")
                            })
                        }
                        payload.put("messages", msgArray)
                        payload.put("temperature", temp)
                    }
                    "openrouter", "openai", "groq" -> {
                        payload.put("model", m)
                        payload.put("max_tokens", GeminiConfig.maxOutputTokens)
                        payload.put("temperature", temp)
                        val msgArray = JSONArray()
                        systemInstruction?.takeIf { it.isNotBlank() }?.let { sys ->
                            msgArray.put(JSONObject().apply {
                                put("role", "system")
                                put("content", sys)
                            })
                        }
                        for (msg in messages) {
                            val role = if (msg["role"] == "assistant" || msg["role"] == "model") "assistant" else "user"
                            msgArray.put(JSONObject().apply {
                                put("role", role)
                                put("content", msg["content"] ?: msg["text"] ?: "")
                            })
                        }
                        payload.put("messages", msgArray)
                    }
                    else -> { // "gemini"
                        val contents = JSONArray()
                        for (msg in messages) {
                            val role = if (msg["role"] == "model" || msg["role"] == "assistant") "model" else "user"
                            val text = msg["content"] ?: msg["text"] ?: ""
                            contents.put(JSONObject().apply {
                                put("role", role)
                                put("parts", JSONArray().put(JSONObject().put("text", text)))
                            })
                        }
                        payload.put("contents", contents)
                        systemInstruction?.takeIf { it.isNotBlank() }?.let { sys ->
                            payload.put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", sys))))
                        }
                        val genConfig = JSONObject().apply {
                            put("temperature", temp)
                            put("maxOutputTokens", GeminiConfig.maxOutputTokens)
                        }
                        payload.put("generationConfig", genConfig)
                    }
                }
                return payload
            }

            val (result, effectiveModel) = executeWithGeminiFallback(
                provider = provider,
                initialModel = targetModel,
                apiKey = apiKey,
                baseUrl = GeminiConfig.baseUrl,
                startTime = startTime,
                payloadBuilder = { m -> makePayload(m) }
            )
            val latency = System.currentTimeMillis() - startTime

            if (!result.isSuccess) {
                return@withContext GenerateResult(
                    ok = false,
                    error = result.errorMessage,
                    latencyMs = latency
                )
            }

            parseResponse(result.body, effectiveModel, latency, "text", false)
        } catch (e: Exception) {
            GenerateResult(
                ok = false,
                error = e.message ?: "Network error connecting to AI API",
                latencyMs = System.currentTimeMillis() - startTime
            )
        }
    }

    /**
     * Executes a lightweight ping to verify credentials, model, and connectivity.
     */
    suspend fun testConnection(model: String? = null, apiKey: String? = null): GenerateResult {
        val effectiveKey = apiKey?.ifBlank { null } ?: GeminiConfig.apiKey
        val effectiveModel = model?.ifBlank { null } ?: GeminiConfig.defaultModel
        val provider = GeminiConfig.detectEffectiveProvider(effectiveModel, explicitKey = effectiveKey)
        val targetModel = GeminiConfig.sanitizeModel(effectiveModel, provider)

        return generate(
            GenerateOptions(
                prompt = "Respond with 'pong' if you receive this ping.",
                model = targetModel,
                apiKey = effectiveKey,
                maxTokens = 16,
                temperature = 0.1f
            )
        )
    }

    private fun executeWithGeminiFallback(
        provider: String,
        initialModel: String,
        apiKey: String,
        baseUrl: String,
        startTime: Long,
        payloadBuilder: (model: String) -> JSONObject
    ): Pair<HttpResponse, String> {
        var currentModel = initialModel
        var (endpointUrl, customHeaders) = resolveEndpointAndHeaders(provider, currentModel, apiKey, baseUrl)
        var payload = payloadBuilder(currentModel)
        var result = executePost(endpointUrl, payload.toString(), customHeaders)

        if (!result.isSuccess && provider == "gemini") {
            val isNotFound = result.code == 404 ||
                result.errorMessage.contains("not found for API version", ignoreCase = true) ||
                result.errorMessage.contains("not supported for generateContent", ignoreCase = true)

            if (isNotFound) {
                // 1. Try v1 if v1beta failed
                if (endpointUrl.contains("/v1beta/")) {
                    val v1Endpoint = endpointUrl.replace("/v1beta/", "/v1/")
                    val v1Result = executePost(v1Endpoint, payload.toString(), customHeaders)
                    if (v1Result.isSuccess) {
                        return v1Result to currentModel
                    }
                }
                // 2. If model is not gemini-1.5-flash, fallback to gemini-1.5-flash
                if (currentModel != GeminiConfig.MODEL_GEMINI_1_5_FLASH) {
                    currentModel = GeminiConfig.MODEL_GEMINI_1_5_FLASH
                    val (fallbackUrl, fallbackHeaders) = resolveEndpointAndHeaders(provider, currentModel, apiKey, baseUrl)
                    val fallbackPayload = payloadBuilder(currentModel)
                    val fallbackResult = executePost(fallbackUrl, fallbackPayload.toString(), fallbackHeaders)
                    if (fallbackResult.isSuccess) {
                        GeminiConfig.defaultModel = GeminiConfig.MODEL_GEMINI_1_5_FLASH
                        return fallbackResult to currentModel
                    }
                }
                // 3. Try gemini-2.0-flash as secondary fallback
                if (currentModel != GeminiConfig.MODEL_GEMINI_2_0_FLASH) {
                    val altModel = GeminiConfig.MODEL_GEMINI_2_0_FLASH
                    val (altUrl, altHeaders) = resolveEndpointAndHeaders(provider, altModel, apiKey, baseUrl)
                    val altPayload = payloadBuilder(altModel)
                    val altResult = executePost(altUrl, altPayload.toString(), altHeaders)
                    if (altResult.isSuccess) {
                        GeminiConfig.defaultModel = altModel
                        return altResult to altModel
                    }
                }
            }
        }

        return result to currentModel
    }

    private fun resolveEndpointAndHeaders(
        provider: String,
        targetModel: String,
        apiKey: String,
        baseUrl: String
    ): Pair<String, Map<String, String>> {
        return when (provider) {
            "groq" -> {
                val url = when {
                    baseUrl.endsWith("/chat/completions") -> baseUrl
                    baseUrl.contains("groq.com") -> "https://api.groq.com/openai/v1/chat/completions"
                    baseUrl != GeminiConfig.DEFAULT_BASE_URL -> if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
                    else -> "https://api.groq.com/openai/v1/chat/completions"
                }
                val headers = mapOf(
                    "Authorization" to "Bearer $apiKey",
                    "Content-Type" to "application/json"
                )
                url to headers
            }
            "openrouter" -> {
                val url = when {
                    baseUrl.endsWith("/chat/completions") -> baseUrl
                    baseUrl.contains("openrouter.ai") -> "https://openrouter.ai/api/v1/chat/completions"
                    baseUrl.endsWith("/v1") -> "$baseUrl/chat/completions"
                    baseUrl != GeminiConfig.DEFAULT_BASE_URL -> if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
                    else -> "https://openrouter.ai/api/v1/chat/completions"
                }
                val headers = mapOf(
                    "Authorization" to "Bearer $apiKey",
                    "HTTP-Referer" to "https://v07.app",
                    "X-Title" to "V07 Dashboard"
                )
                url to headers
            }
            "anthropic" -> {
                val url = when {
                    baseUrl.endsWith("/messages") -> baseUrl
                    baseUrl.contains("anthropic.com") -> "https://api.anthropic.com/v1/messages"
                    baseUrl.endsWith("/v1") -> "$baseUrl/messages"
                    baseUrl != GeminiConfig.DEFAULT_BASE_URL -> if (baseUrl.endsWith("/messages")) baseUrl else "$baseUrl/messages"
                    else -> "https://api.anthropic.com/v1/messages"
                }
                val headers = mapOf(
                    "x-api-key" to apiKey,
                    "anthropic-version" to "2023-06-01"
                )
                url to headers
            }
            "openai" -> {
                val url = when {
                    baseUrl.endsWith("/chat/completions") -> baseUrl
                    baseUrl.contains("openai.com") -> "https://api.openai.com/v1/chat/completions"
                    baseUrl.endsWith("/v1") -> "$baseUrl/chat/completions"
                    baseUrl != GeminiConfig.DEFAULT_BASE_URL -> if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
                    else -> "https://api.openai.com/v1/chat/completions"
                }
                val headers = mapOf(
                    "Authorization" to "Bearer $apiKey"
                )
                url to headers
            }
            else -> { // "gemini"
                val url = when {
                    baseUrl.contains("?") -> "$baseUrl/$targetModel:generateContent&key=$apiKey"
                    baseUrl.endsWith("/models") -> "$baseUrl/$targetModel:generateContent?key=$apiKey"
                    baseUrl != GeminiConfig.DEFAULT_BASE_URL && !baseUrl.contains("generativelanguage") -> {
                        if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
                    }
                    else -> "${GeminiConfig.DEFAULT_BASE_URL}/$targetModel:generateContent?key=$apiKey"
                }
                val headers = emptyMap<String, String>()
                url to headers
            }
        }
    }

    private fun buildPayload(provider: String, targetModel: String, options: GenerateOptions): JSONObject {
        return when (provider) {
            "anthropic" -> {
                JSONObject().apply {
                    put("model", targetModel)
                    put("max_tokens", options.maxTokens ?: GeminiConfig.maxOutputTokens)
                    options.systemInstruction?.takeIf { it.isNotBlank() }?.let { put("system", it) }
                    val msgArray = JSONArray()
                    msgArray.put(JSONObject().apply {
                        put("role", "user")
                        put("content", options.prompt)
                    })
                    put("messages", msgArray)
                    options.temperature?.let { put("temperature", it) }
                }
            }
            "openrouter", "openai", "groq" -> {
                JSONObject().apply {
                    put("model", targetModel)
                    options.maxTokens?.let { put("max_tokens", it) }
                    options.temperature?.let { put("temperature", it) }
                    val msgArray = JSONArray()
                    options.systemInstruction?.takeIf { it.isNotBlank() }?.let { sys ->
                        msgArray.put(JSONObject().apply {
                            put("role", "system")
                            put("content", sys)
                        })
                    }
                    msgArray.put(JSONObject().apply {
                        put("role", "user")
                        put("content", options.prompt)
                    })
                    put("messages", msgArray)

                    if (options.responseFormat.equals("json", ignoreCase = true) || options.responseSchema != null) {
                        put("response_format", JSONObject().put("type", "json_object"))
                    }
                }
            }
            else -> { // "gemini"
                JSONObject().apply {
                    val parts = JSONArray().put(JSONObject().put("text", options.prompt))
                    val contentObj = JSONObject().apply {
                        put("role", "user")
                        put("parts", parts)
                    }
                    put("contents", JSONArray().put(contentObj))

                    options.systemInstruction?.takeIf { it.isNotBlank() }?.let { sys ->
                        put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", sys))))
                    }

                    val genConfig = JSONObject().apply {
                        put("temperature", options.temperature ?: GeminiConfig.temperature)
                        put("maxOutputTokens", options.maxTokens ?: GeminiConfig.maxOutputTokens)
                        if (options.responseFormat.equals("json", ignoreCase = true) || options.responseSchema != null) {
                            put("responseMimeType", "application/json")
                            if (options.responseSchema != null) {
                                put("responseSchema", JSONObject(toJson(options.responseSchema)))
                            }
                        }
                    }
                    put("generationConfig", genConfig)
                }
            }
        }
    }

    private data class HttpResponse(val isSuccess: Boolean, val code: Int, val body: String, val errorMessage: String)

    private fun executePost(urlString: String, jsonBody: String, customHeaders: Map<String, String> = emptyMap()): HttpResponse {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 60000
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            for ((k, v) in customHeaders) {
                conn.setRequestProperty(k, v)
            }
            conn.doOutput = true

            conn.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val respBytes = stream?.use {
                val out = ByteArrayOutputStream()
                it.copyTo(out)
                out.toByteArray()
            } ?: ByteArray(0)
            val respText = String(respBytes, Charsets.UTF_8)

            if (code in 200..299) {
                HttpResponse(true, code, respText, "")
            } else {
                val errorMsg = runCatching {
                    val errJson = JSONObject(respText)
                    val innerErr = errJson.optJSONObject("error")
                    val msg = innerErr?.optString("message") ?: errJson.optString("message")
                    val status = innerErr?.optString("status")
                    when {
                        !msg.isNullOrBlank() -> "AI API Error ($code): $msg"
                        !status.isNullOrBlank() -> "AI API Error: $status (HTTP $code)"
                        else -> "HTTP $code: $respText"
                    }
                }.getOrElse { "HTTP $code error: $respText" }
                HttpResponse(false, code, respText, errorMsg)
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun parseResponse(
        respText: String,
        targetModel: String,
        latencyMs: Long,
        responseFormat: String,
        hasSchema: Boolean
    ): GenerateResult {
        val json = JSONObject(respText)
        var candidateText = ""
        var usage = emptyMap<String, Int>()

        if (json.has("choices")) {
            // OpenAI / OpenRouter format
            val choices = json.optJSONArray("choices")
            val first = choices?.optJSONObject(0)
            val msg = first?.optJSONObject("message")
            candidateText = msg?.optString("content") ?: ""

            val usageMeta = json.optJSONObject("usage")
            usage = mapOf(
                "promptTokens" to (usageMeta?.optInt("prompt_tokens") ?: 0),
                "candidatesTokens" to (usageMeta?.optInt("completion_tokens") ?: 0),
                "totalTokens" to (usageMeta?.optInt("total_tokens") ?: 0)
            )
        } else if (json.has("content") && json.optJSONArray("content") != null) {
            // Anthropic Claude native format
            val contentArr = json.optJSONArray("content")
            candidateText = contentArr?.optJSONObject(0)?.optString("text") ?: ""

            val usageMeta = json.optJSONObject("usage")
            val inTok = usageMeta?.optInt("input_tokens") ?: 0
            val outTok = usageMeta?.optInt("output_tokens") ?: 0
            usage = mapOf(
                "promptTokens" to inTok,
                "candidatesTokens" to outTok,
                "totalTokens" to (inTok + outTok)
            )
        } else if (json.has("candidates")) {
            // Google Gemini format
            val candidates = json.optJSONArray("candidates")
            val first = candidates?.optJSONObject(0)
            val contentObj = first?.optJSONObject("content")
            val partsArr = contentObj?.optJSONArray("parts")
            candidateText = partsArr?.optJSONObject(0)?.optString("text") ?: ""

            val usageMeta = json.optJSONObject("usageMetadata")
            usage = mapOf(
                "promptTokens" to (usageMeta?.optInt("promptTokenCount") ?: 0),
                "candidatesTokens" to (usageMeta?.optInt("candidatesTokenCount") ?: 0),
                "totalTokens" to (usageMeta?.optInt("totalTokenCount") ?: 0)
            )
        }

        var parsedData: Any? = null
        if (responseFormat.equals("json", ignoreCase = true) || hasSchema) {
            parsedData = runCatching { jsonToMap(candidateText) }.getOrNull()
        }

        return GenerateResult(
            ok = true,
            text = candidateText,
            data = parsedData,
            model = targetModel,
            usage = usage,
            latencyMs = latencyMs
        )
    }
}
