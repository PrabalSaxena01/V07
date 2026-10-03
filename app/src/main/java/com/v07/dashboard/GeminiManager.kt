package com.v07.dashboard

import android.content.Context
import java.util.concurrent.atomic.AtomicLong

/**
 * High-level coordinator for the Gemini Core Integration Layer.
 * Manages lifecycle, security permission validation for plugins, and telemetry.
 */
object GeminiManager {

    private val totalPromptTokens = AtomicLong(0L)
    private val totalCandidatesTokens = AtomicLong(0L)

    fun init(context: Context) {
        GeminiConfig.init(context)
    }

    /**
     * Checks if a caller has access to the AI layer.
     * Internal calls (from the host app/dashboard) have null or empty pluginId and are always permitted.
     * Plugin callers must have declared "ai" permission in manifest.json and not have it disabled in user settings.
     */
    fun canPluginAccessAI(pluginId: String?): Boolean {
        if (pluginId.isNullOrBlank()) return true // Internal app or settings caller
        return PluginManager.canUseAI(pluginId)
    }

    fun recordUsage(prompt: Int, candidates: Int) {
        totalPromptTokens.addAndGet(prompt.toLong())
        totalCandidatesTokens.addAndGet(candidates.toLong())
    }

    fun getUsageStats(): Map<String, Long> = mapOf(
        "promptTokens" to totalPromptTokens.get(),
        "candidatesTokens" to totalCandidatesTokens.get(),
        "totalTokens" to (totalPromptTokens.get() + totalCandidatesTokens.get())
    )
}
