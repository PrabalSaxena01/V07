package com.v07.dashboard

import android.content.Context
import android.content.SharedPreferences

/**
 * Secure persistent configuration for the AI & Model Integration Layer.
 * Supports Claude 3.5+, Gemini 2.0/1.5, OpenRouter, and custom gateways.
 * Credentials are stored in Android private preferences and never exposed to plugins.
 */
object GeminiConfig {

    private const val PREFS_NAME = "v07_prefs"
    private const val KEY_API_KEY = "gemini_api_key"
    private const val KEY_BASE_URL = "gemini_base_url"
    private const val KEY_DEFAULT_MODEL = "gemini_default_model"
    private const val KEY_PROVIDER = "gemini_provider"
    private const val KEY_TEMPERATURE = "gemini_temperature"
    private const val KEY_MAX_OUTPUT_TOKENS = "gemini_max_output_tokens"

    const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    // Free & High-Performance Models Catalog
    const val MODEL_GEMINI_1_5_FLASH = "gemini-1.5-flash"
    const val MODEL_GEMINI_2_0_FLASH = "gemini-2.0-flash"
    const val MODEL_GEMINI_1_5_PRO = "gemini-1.5-pro"
    const val MODEL_GEMINI_2_5_FLASH = "gemini-2.5-flash"
    const val MODEL_GROQ_LLAMA_3_3 = "llama-3.3-70b-versatile"
    const val MODEL_GROQ_DEEPSEEK_R1 = "deepseek-r1-distill-llama-70b"
    const val MODEL_OPENROUTER_LLAMA_FREE = "meta-llama/llama-3.3-70b-instruct:free"
    const val MODEL_OPENROUTER_GEMINI_FREE = "google/gemini-2.0-flash-exp:free"

    const val MODEL_CLAUDE_3_5_SONNET = "claude-3-5-sonnet-20241022"
    const val MODEL_CLAUDE_3_5_HAIKU = "claude-3-5-haiku-20241022"
    const val MODEL_OPENROUTER_CLAUDE_3_5_SONNET = "anthropic/claude-3.5-sonnet"
    const val MODEL_OPENROUTER_CLAUDE_3_5_HAIKU = "anthropic/claude-3.5-haiku"
    const val MODEL_GPT_4O = "gpt-4o"
    const val MODEL_GPT_4O_MINI = "gpt-4o-mini"

    val AVAILABLE_MODELS = listOf(
        mapOf(
            "id" to MODEL_GEMINI_1_5_FLASH,
            "label" to "Gemini 1.5 Flash (Google · Free Tier 15 RPM - Recommended)",
            "provider" to "gemini",
            "speed" to "Fastest",
            "context" to "1M"
        ),
        mapOf(
            "id" to MODEL_GEMINI_2_0_FLASH,
            "label" to "Gemini 2.0 Flash (Google · Fast Multimodal)",
            "provider" to "gemini",
            "speed" to "Ultra-Fast",
            "context" to "1M"
        ),
        mapOf(
            "id" to MODEL_GEMINI_1_5_PRO,
            "label" to "Gemini 1.5 Pro (Google · Requires Tier 1 / Paid Key)",
            "provider" to "gemini",
            "speed" to "Deep Reasoning",
            "context" to "2M"
        ),
        mapOf(
            "id" to MODEL_GROQ_LLAMA_3_3,
            "label" to "Llama 3.3 70B (Groq · Free & Ultra-Fast)",
            "provider" to "groq",
            "speed" to "Instant (~300 t/s)",
            "context" to "128k"
        ),
        mapOf(
            "id" to MODEL_GROQ_DEEPSEEK_R1,
            "label" to "DeepSeek R1 Distill 70B (Groq · Free Reasoning)",
            "provider" to "groq",
            "speed" to "Fast",
            "context" to "128k"
        ),
        mapOf(
            "id" to MODEL_OPENROUTER_LLAMA_FREE,
            "label" to "Llama 3.3 70B Free (OpenRouter)",
            "provider" to "openrouter",
            "speed" to "Fast",
            "context" to "128k"
        ),
        mapOf(
            "id" to MODEL_OPENROUTER_GEMINI_FREE,
            "label" to "Gemini 2.0 Flash Exp Free (OpenRouter)",
            "provider" to "openrouter",
            "speed" to "Fastest",
            "context" to "1M"
        ),
        mapOf(
            "id" to MODEL_GEMINI_2_5_FLASH,
            "label" to "Gemini 2.5 Flash (Google · High Performance)",
            "provider" to "gemini",
            "speed" to "Fastest",
            "context" to "1M"
        ),
        mapOf(
            "id" to MODEL_GEMINI_1_5_PRO,
            "label" to "Gemini 1.5 Pro (Google · Deep Reasoning)",
            "provider" to "gemini",
            "speed" to "Standard",
            "context" to "2M"
        ),
        mapOf(
            "id" to MODEL_GEMINI_1_5_FLASH,
            "label" to "Gemini 1.5 Flash (Google · Stable & Fast)",
            "provider" to "gemini",
            "speed" to "Fastest",
            "context" to "1M"
        ),
        mapOf(
            "id" to MODEL_GEMINI_1_5_PRO,
            "label" to "Gemini 1.5 Pro (Google · Deep Reasoning)",
            "provider" to "gemini",
            "speed" to "Standard",
            "context" to "2M"
        ),
        mapOf(
            "id" to MODEL_CLAUDE_3_5_SONNET,
            "label" to "Claude 3.5 Sonnet (Anthropic · State-of-the-Art 3.5)",
            "provider" to "anthropic",
            "speed" to "Fast",
            "context" to "200k"
        ),
        mapOf(
            "id" to MODEL_CLAUDE_3_5_HAIKU,
            "label" to "Claude 3.5 Haiku (Anthropic · Ultra-Fast 3.5)",
            "provider" to "anthropic",
            "speed" to "Fastest",
            "context" to "200k"
        ),
        mapOf(
            "id" to MODEL_OPENROUTER_CLAUDE_3_5_SONNET,
            "label" to "Claude 3.5 Sonnet (via OpenRouter)",
            "provider" to "openrouter",
            "speed" to "Fast",
            "context" to "200k"
        ),
        mapOf(
            "id" to MODEL_OPENROUTER_CLAUDE_3_5_HAIKU,
            "label" to "Claude 3.5 Haiku (via OpenRouter)",
            "provider" to "openrouter",
            "speed" to "Fastest",
            "context" to "200k"
        ),
        mapOf(
            "id" to MODEL_GPT_4O,
            "label" to "GPT-4o (OpenAI · Multimodal Flagship)",
            "provider" to "openai",
            "speed" to "Fast",
            "context" to "128k"
        ),
        mapOf(
            "id" to MODEL_GPT_4O_MINI,
            "label" to "GPT-4o Mini (OpenAI · Fast & Efficient)",
            "provider" to "openai",
            "speed" to "Fastest",
            "context" to "128k"
        )
    )

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var apiKey: String
        get() = prefs?.getString(KEY_API_KEY, "") ?: ""
        set(value) {
            prefs?.edit()?.putString(KEY_API_KEY, value.trim())?.apply()
        }

    var baseUrl: String
        get() = prefs?.getString(KEY_BASE_URL, DEFAULT_BASE_URL)?.ifBlank { DEFAULT_BASE_URL } ?: DEFAULT_BASE_URL
        set(value) {
            val clean = value.trim().removeSuffix("/")
            prefs?.edit()?.putString(KEY_BASE_URL, if (clean.isEmpty()) DEFAULT_BASE_URL else clean)?.apply()
        }

    var defaultModel: String
        get() {
            return prefs?.getString(KEY_DEFAULT_MODEL, MODEL_GEMINI_1_5_FLASH)?.ifBlank { MODEL_GEMINI_1_5_FLASH } ?: MODEL_GEMINI_1_5_FLASH
        }
        set(value) {
            val clean = value.trim()
            prefs?.edit()?.putString(KEY_DEFAULT_MODEL, if (clean.isEmpty()) MODEL_GEMINI_1_5_FLASH else clean)?.apply()
        }

    var provider: String
        get() = prefs?.getString(KEY_PROVIDER, "auto") ?: "auto"
        set(value) {
            prefs?.edit()?.putString(KEY_PROVIDER, value.trim())?.apply()
        }

    var temperature: Float
        get() = prefs?.getFloat(KEY_TEMPERATURE, 0.7f) ?: 0.7f
        set(value) {
            prefs?.edit()?.putFloat(KEY_TEMPERATURE, value)?.apply()
        }

    var maxOutputTokens: Int
        get() = prefs?.getInt(KEY_MAX_OUTPUT_TOKENS, 4096) ?: 4096
        set(value) {
            prefs?.edit()?.putInt(KEY_MAX_OUTPUT_TOKENS, value)?.apply()
        }

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    fun maskedKey(): String {
        val key = apiKey
        return when {
            key.isEmpty() -> ""
            key.length <= 8 -> "••••••••"
            else -> "${key.take(4)}...${key.takeLast(4)}"
        }
    }

    /**
     * Sanitizes the model identifier against deprecated versions and provider mismatches.
     */
    fun sanitizeModel(modelId: String, effectiveProvider: String): String {
        var m = modelId.trim()
        if (m.isEmpty()) {
            m = MODEL_GEMINI_1_5_FLASH
        }
        return when (effectiveProvider) {
            "gemini" -> if (m.startsWith("gemini-")) m else MODEL_GEMINI_1_5_FLASH
            "groq" -> if (m.startsWith("llama") || m.startsWith("deepseek") || m.startsWith("gemma")) m else MODEL_GROQ_LLAMA_3_3
            "anthropic" -> if (m.startsWith("claude-")) m else MODEL_CLAUDE_3_5_SONNET
            "openrouter" -> if (m.contains("/")) m else MODEL_OPENROUTER_LLAMA_FREE
            "openai" -> if (m.startsWith("gpt-")) m else MODEL_GPT_4O
            else -> m
        }
    }

    /**
     * Detects whether to format payloads for Anthropic, OpenRouter, OpenAI, Groq, or Google Gemini.
     * Prioritizes API key signature before model names to prevent provider cross-calls.
     */
    fun detectEffectiveProvider(model: String = defaultModel, explicitKey: String? = null): String {
        val explicit = provider.lowercase().trim()
        if (explicit.isNotEmpty() && explicit != "auto") return explicit

        val key = (explicitKey ?: apiKey).trim()
        val url = baseUrl.lowercase().trim()

        return when {
            key.startsWith("gsk_") || url.contains("groq.com") -> "groq"
            key.startsWith("AIza") || url.contains("generativelanguage.googleapis.com") -> "gemini"
            key.startsWith("sk-ant-") || url.contains("anthropic.com") -> "anthropic"
            key.startsWith("sk-or-") || url.contains("openrouter.ai") -> "openrouter"
            key.startsWith("sk-") || url.contains("openai.com") -> "openai"
            model.startsWith("llama") || model.startsWith("deepseek") -> "groq"
            model.startsWith("gemini-") -> "gemini"
            model.startsWith("claude-") -> "anthropic"
            model.contains("/") -> "openrouter"
            model.startsWith("gpt-") -> "openai"
            else -> "gemini"
        }
    }
}
