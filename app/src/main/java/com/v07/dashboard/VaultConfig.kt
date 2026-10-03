package com.v07.dashboard

import android.content.Context
import java.io.File

object VaultConfig {
    private const val PREFS_NAME = "v07_vault_prefs"
    private const val KEY_PATH = "vault_path"
    private const val KEY_CONFIGURED = "vault_configured"
    private const val DEFAULT_PATH = "/storage/emulated/0/V07Data"

    private var context: Context? = null
    val appContext: Context? get() = context
    private var cachedPath: String? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
        ensureGitaJson()
    }

    fun isVaultConfigured(): Boolean {
        val ctx = context ?: return false
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_CONFIGURED, false)
    }

    fun setVaultConfigured(configured: Boolean) {
        val ctx = context ?: return
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_CONFIGURED, configured)
            .apply()
    }

    fun ensureGitaJson() {
        val ctx = context ?: return
        val target = File(gitaDir, "gita.json")
        if (!target.exists()) {
            try {
                ctx.assets.open("gita.json").use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                android.util.Log.i("V07Vault", "Copied gita.json to ${target.absolutePath}")
            } catch (e: Exception) {
                android.util.Log.e("V07Vault", "Failed to copy gita.json to ${target.absolutePath}: ${e.message}")
            }
        }
    }

    private fun prefs() = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getVaultPath(): String {
        cachedPath?.let { return it }
        val ctx = context ?: return DEFAULT_PATH
        val saved = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PATH, null)
        if (saved != null) {
            cachedPath = saved
            return saved
        }
        cachedPath = DEFAULT_PATH
        return DEFAULT_PATH
    }

    fun setVaultPath(path: String) {
        cachedPath = null
        val ctx = context ?: return
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_PATH, path)
            .apply()
        cachedPath = path
    }

    val vaultDir: File get() = File(getVaultPath())

    val vaultSystemDir: File get() = File(vaultDir, "08 - System")

    val eventsDir: File get() = File(vaultSystemDir, "Data/events").also { it.mkdirs() }

    val gitaDir: File get() = File(vaultSystemDir, "Data/gita").also { it.mkdirs() }

    val appUsageDir: File get() = File(vaultSystemDir, "app_usage").also { it.mkdirs() }

    val mediaDir: File get() = File(vaultDir, "media").also { it.mkdirs() }

    val dayTrackerFile: File get() = File(vaultSystemDir, "day_tracker.jsonl")

    val filesDir: File get() = context?.filesDir ?: File("/data/data/com.v07.dashboard/files")

    fun catFile(category: String): File = File(eventsDir, "$category.jsonl")

    fun recycleFile(): File = File(eventsDir, "recycle_bin.jsonl")

    fun recycleLock(): File = File(eventsDir, "recycle_bin.lock")

    fun catLock(category: String): File = File(eventsDir, "$category.lock")

    fun gitaReadFile(): File = File(gitaDir, "gita_read.jsonl")

    fun gitaReviewsFile(): File = File(gitaDir, "gita_reviews.jsonl")

    fun appUsageFile(date: String): File = File(appUsageDir, "$date.json")

    val tagsFile: File get() = File(vaultDir, "tags.json")
}
