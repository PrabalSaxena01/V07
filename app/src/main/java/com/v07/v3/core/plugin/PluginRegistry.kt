package com.v07.v3.core.plugin

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import com.v07.v3.core.data.V07Database
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

enum class PluginState {
    DISCOVERED,
    VALIDATED,
    INSTALLED,
    ENABLED,
    RUNNING,
    DISABLED,
    BROKEN
}

data class PluginRecord(
    val manifest: V3PluginManifest,
    var state: PluginState,
    val sourceDir: File? = null,
    val isBuiltIn: Boolean = true,
    val validationErrors: List<String> = emptyList()
)

/**
 * PluginRegistry
 *
 * Central registry and lifecycle manager for all V3 plugins.
 * Discovers built-in starter plugins from assets and external plugins from public storage.
 * Maintains isolated plugin state and dependency resolution.
 */
class PluginRegistry private constructor(private val context: Context) {

    companion object {
        private const val TAG = "V3PluginRegistry"
        private const val PREFS_NAME = "v3_plugin_states"
        private const val PLUGINS_DIR_NAME = "plugins"

        @Volatile
        private var instance: PluginRegistry? = null

        fun getInstance(context: Context): PluginRegistry {
            return instance ?: synchronized(this) {
                instance ?: PluginRegistry(context.applicationContext).also {
                    instance = it
                    it.discoverAll()
                }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val plugins = ConcurrentHashMap<String, PluginRecord>()

    /**
     * Discovers all plugins from both internal assets and external storage.
     */
    @Synchronized
    fun discoverAll(): List<PluginRecord> {
        plugins.clear()
        discoverBuiltInPlugins()
        discoverExternalPlugins()
        Log.i(TAG, "Plugin discovery complete. Total plugins registered: ${plugins.size}")
        return plugins.values.toList()
    }

    fun getAllPlugins(): List<PluginRecord> = plugins.values.toList()

    fun getEnabledPlugins(): List<PluginRecord> {
        return plugins.values.filter { it.state == PluginState.ENABLED || it.state == PluginState.RUNNING }
    }

    fun getPlugin(id: String): PluginRecord? = plugins[id]

    @Synchronized
    fun enablePlugin(id: String): Boolean {
        val record = plugins[id] ?: return false
        record.state = PluginState.ENABLED
        prefs.edit().putString(id, "ENABLED").apply()
        Log.i(TAG, "Enabled plugin: $id")
        return true
    }

    @Synchronized
    fun disablePlugin(id: String): Boolean {
        val record = plugins[id] ?: return false
        record.state = PluginState.DISABLED
        prefs.edit().putString(id, "DISABLED").apply()
        Log.i(TAG, "Disabled plugin: $id")
        return true
    }

    @Synchronized
    fun registerCustomPlugin(manifest: V3PluginManifest): Boolean {
        val errors = PluginManifestParser.validate(manifest)
        val state = if (errors.isNotEmpty()) PluginState.BROKEN else PluginState.ENABLED
        plugins[manifest.id] = PluginRecord(
            manifest = manifest,
            state = state,
            sourceDir = null,
            isBuiltIn = false,
            validationErrors = errors
        )
        prefs.edit().putString(manifest.id, state.name).apply()
        Log.i(TAG, "Registered custom plugin: ${manifest.id} (state: ${state.name})")
        return errors.isEmpty()
    }

    @Synchronized
    fun unregisterPlugin(id: String): Boolean {
        val removed = plugins.remove(id)
        prefs.edit().remove(id).apply()
        Log.i(TAG, "Unregistered plugin: $id")
        return removed != null
    }

    /**
     * Collects all widgets from all currently enabled plugins.
     */
    fun getActiveWidgets(): List<Pair<PluginRecord, V3WidgetInfo>> {
        val list = mutableListOf<Pair<PluginRecord, V3WidgetInfo>>()
        for (plugin in getEnabledPlugins()) {
            for (w in plugin.manifest.widgets) {
                list.add(Pair(plugin, w))
            }
        }
        return list
    }

    private fun discoverBuiltInPlugins() {
        try {
            val assetManager = context.assets
            val pluginDirs = assetManager.list(PLUGINS_DIR_NAME) ?: emptyArray()

            for (dirName in pluginDirs) {
                if (dirName.startsWith("_") || dirName.endsWith("-template") || dirName == "v07-template") continue // Skip templates

                val manifestPath = "$PLUGINS_DIR_NAME/$dirName/manifest.json"
                try {
                    val jsonStr = assetManager.open(manifestPath).use {
                        it.bufferedReader(StandardCharsets.UTF_8).readText()
                    }
                    val manifest = PluginManifestParser.parse(jsonStr)
                    if (manifest != null) {
                        val errors = PluginManifestParser.validate(manifest)
                        val isExplicitlyDisabled = prefs.getString(manifest.id, "ENABLED") == "DISABLED"
                        val state = if (errors.isNotEmpty()) {
                            PluginState.BROKEN
                        } else if (isExplicitlyDisabled) {
                            PluginState.DISABLED
                        } else {
                            PluginState.ENABLED
                        }

                        plugins[manifest.id] = PluginRecord(
                            manifest = manifest,
                            state = state,
                            sourceDir = null,
                            isBuiltIn = true,
                            validationErrors = errors
                        )
                        Log.d(TAG, "Registered built-in plugin: ${manifest.id} (${state.name})")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read manifest for built-in plugin $dirName: ${e.message}")
                }
            }

            // Register Core Companion Plugins: Gita Wisdom & Runtime Tracker
            if (!plugins.containsKey("com.v07.gita")) {
                val gitaManifest = V3PluginManifest(
                    id = "com.v07.gita",
                    name = "Gita Wisdom",
                    version = "3.0.0",
                    description = "Contemplative verses and philosophical clarity for daily life.",
                    publisher = "V07 Sanctuary",
                    permissions = setOf("storage:isolated"),
                    navIcon = "auto_stories",
                    navLabel = "GITA",
                    navColor = "#FF9F43"
                )
                val state = if (prefs.getString("com.v07.gita", "ENABLED") == "DISABLED") PluginState.DISABLED else PluginState.ENABLED
                plugins["com.v07.gita"] = PluginRecord(manifest = gitaManifest, state = state, isBuiltIn = true)
            }

            if (!plugins.containsKey("com.v07.running")) {
                val runManifest = V3PluginManifest(
                    id = "com.v07.running",
                    name = "Runtime Tracker",
                    version = "3.0.0",
                    description = "Log running sessions, track pace, and visual progression.",
                    publisher = "V07 Sanctuary",
                    permissions = setOf("storage:isolated"),
                    navIcon = "sprint",
                    navLabel = "RUNNING",
                    navColor = "#10B981"
                )
                val state = if (prefs.getString("com.v07.running", "ENABLED") == "DISABLED") PluginState.DISABLED else PluginState.ENABLED
                plugins["com.v07.running"] = PluginRecord(manifest = runManifest, state = state, isBuiltIn = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering built-in plugins: ${e.message}", e)
        }
    }

    private fun discoverExternalPlugins() {
        val externalDirs = listOf(
            File(V07Database.resolveDatabaseFile(context).parentFile, PLUGINS_DIR_NAME),
            File(context.getExternalFilesDir(null), PLUGINS_DIR_NAME),
            File(context.filesDir, PLUGINS_DIR_NAME)
        )

        for (dir in externalDirs) {
            if (!dir.exists() || !dir.isDirectory) continue

            val subDirs = dir.listFiles() ?: continue
            for (pluginDir in subDirs) {
                if (!pluginDir.isDirectory) continue

                val manifestFile = File(pluginDir, "manifest.json")
                if (manifestFile.exists()) {
                    try {
                        val jsonStr = manifestFile.readText(StandardCharsets.UTF_8)
                        val manifest = PluginManifestParser.parse(jsonStr)
                        if (manifest != null) {
                            val errors = PluginManifestParser.validate(manifest)
                            val isExplicitlyDisabled = prefs.getString(manifest.id, "ENABLED") == "DISABLED"
                            val state = if (errors.isNotEmpty()) {
                                PluginState.BROKEN
                            } else if (isExplicitlyDisabled) {
                                PluginState.DISABLED
                            } else {
                                PluginState.ENABLED
                            }

                            plugins[manifest.id] = PluginRecord(
                                manifest = manifest,
                                state = state,
                                sourceDir = pluginDir,
                                isBuiltIn = false,
                                validationErrors = errors
                            )
                            Log.d(TAG, "Registered external plugin: ${manifest.id} from ${pluginDir.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error loading external plugin from ${pluginDir.name}: ${e.message}")
                    }
                }
            }
        }
    }
}
