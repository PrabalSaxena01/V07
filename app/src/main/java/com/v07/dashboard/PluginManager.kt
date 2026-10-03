package com.v07.dashboard

import java.io.File
import java.util.zip.ZipInputStream

/**
 * Manager for the in-app plugin system (Chrome-extensions style).
 *
 * Plugins are .v07zip bundles (a ZIP containing manifest.json + index.html + plugin files)
 * that live in the app's internal storage and are served to the WebView via the local
 * HTTP server on 127.0.0.1:8000. They can be installed, enabled, disabled, and removed
 * at runtime without reinstalling the APK.
 *
 * Storage layout:
 *   {filesDir}/v07-plugins/
 *     registry.json                 <- enabled/disabled/version of every plugin
 *     installed/{pluginId}/         <- extracted bundle per plugin
 *       manifest.json
 *       index.html
 *       plugin.js, plugin.css, assets/...
 */
object PluginManager {

    private const val REGISTRY_FILE = "registry.json"
    private const val INSTALLED_DIR = "installed"

    // Whitelist of permissions a plugin is allowed to declare in its manifest.
    val ALLOWED_PERMISSIONS = setOf(
        "events:read",
        "events:write",
        "sessions:read",
        "sessions:write",
        "notification",
        "server",
        "ai"
    )

    private const val MAX_BUNDLE_BYTES = 2 * 1024 * 1024 // 2 MB
    private const val MAX_PLUGIN_FILES = 200

    /** A single metadata object describing a plugin. */
    data class PluginMeta(
        val id: String,
        val name: String,
        val version: String,
        val author: String,
        val description: String,
        val permissions: List<String> = emptyList(),
        val nav: Map<String, String> = emptyMap(),
        val fab: List<Map<String, Any?>> = emptyList(),
        val widgets: List<Map<String, Any?>> = emptyList(),
        val builtin: Boolean
    )

    // ─── Paths ─────────────────────────────────────────────────────────────

    private fun pluginsRoot(): File = File(VaultConfig.filesDir, "v07-plugins").also { it.mkdirs() }

    private fun installedDir(): File = File(pluginsRoot(), INSTALLED_DIR).also { it.mkdirs() }

    fun pluginDir(id: String): File = File(installedDir(), sanitizeId(id))

    private fun registryFile(): File = File(pluginsRoot(), REGISTRY_FILE)

    val BUILTIN_PLUGIN_IDS = listOf(
        "com.v07.chronosynth",
        "com.v07.gamify",
        "com.v07.habits",
        "com.v07.reader",
        "com.v07.tasks",
        "com.v07.timetracker",
        "com.v07.vitals"
    )

    /** Sanitize a plugin id so it can never escape the installed directory. */
    fun sanitizeId(id: String): String =
        id.replace(Regex("[^a-zA-Z0-9._-]"), "_")

    // ─── Registry ──────────────────────────────────────────────────────────

    /** Current registry as a map of pluginId -> map of fields. */
    fun readRegistry(): Map<String, Map<String, Any?>> {
        val f = registryFile()
        val reg = if (!f.exists()) {
            mutableMapOf<String, Map<String, Any?>>()
        } else {
            try {
                val raw = f.readText()
                @Suppress("UNCHECKED_CAST")
                ((jsonToMap(raw)["plugins"] as? Map<String, Any?>)?.entries
                    ?.associate { (k, v) -> k to (v as? Map<String, Any?> ?: emptyMap()) }
                    ?: emptyMap()).toMutableMap()
            } catch (_: Exception) { mutableMapOf<String, Map<String, Any?>>() }
        }

        var changed = false
        for (bid in BUILTIN_PLUGIN_IDS) {
            if (!reg.containsKey(bid)) {
                reg[bid] = mapOf(
                    "id" to bid,
                    "enabled" to true,
                    "builtin" to true
                )
                changed = true
            }
        }
        if (changed) {
            try { writeRegistry(reg) } catch (_: Exception) {}
        }
        return reg
    }

    private fun writeRegistry(registry: Map<String, Map<String, Any?>>) {
        registryFile().writeText(toJson(mapOf("plugins" to registry)))
    }

    private fun updateRegistry(id: String, mutate: (Map<String, Any?>) -> Map<String, Any?>) {
        val reg = readRegistry().toMutableMap()
        val entry = reg[id] ?: mapOf("id" to id)
        reg[id] = mutate(entry)
        writeRegistry(reg)
    }

    // ─── Manifest I/O ──────────────────────────────────────────────────────

    fun readManifest(id: String): Map<String, Any?>? {
        val cleanId = sanitizeId(id)
        if (BUILTIN_PLUGIN_IDS.contains(cleanId)) {
            val assetManifest = try {
                val stream = VaultConfig.appContext?.assets?.open("plugins/$cleanId/manifest.json")
                val text = stream?.bufferedReader()?.use { it.readText() }
                if (text != null) jsonToMap(text) else null
            } catch (_: Exception) { null }
            if (assetManifest != null) {
                try {
                    val mf = File(pluginDir(cleanId), "manifest.json")
                    mf.parentFile?.mkdirs()
                    mf.writeText(toJson(assetManifest))
                } catch (_: Exception) {}
                return assetManifest
            }
        }
        val mf = File(pluginDir(cleanId), "manifest.json")
        if (mf.exists()) {
            val res = try { jsonToMap(mf.readText()) } catch (_: Exception) { null }
            if (res != null) return res
        }
        return try {
            val stream = VaultConfig.appContext?.assets?.open("plugins/$cleanId/manifest.json") ?: return null
            val text = stream.bufferedReader().use { it.readText() }
            jsonToMap(text)
        } catch (_: Exception) { null }
    }

    fun toMeta(id: String, manifest: Map<String, Any?>, builtin: Boolean): PluginMeta {
        @Suppress("UNCHECKED_CAST")
        val perms = manifest["permissions"] as? List<Any?> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val nav = manifest["nav"] as? Map<String, Any?> ?: emptyMap()
        @Suppress("UNCHECKED_CAST")
        val fab = manifest["fab"] as? List<Map<String, Any?>> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val widgets = manifest["widgets"] as? List<Map<String, Any?>> ?: emptyList()
        return PluginMeta(
            id = id,
            name = manifest["name"] as? String ?: id,
            version = manifest["version"] as? String ?: "0.0.0",
            author = manifest["author"] as? String ?: "",
            description = manifest["description"] as? String ?: "",
            permissions = perms.mapNotNull { it as? String },
            nav = nav.mapNotNull { (k, v) -> v?.let { k to it.toString() } }.toMap(),
            fab = fab,
            widgets = widgets,
            builtin = builtin
        )
    }

    // ─── Plugin-Side JS access ─────────────────────────────────────────────

    fun isEnabled(id: String): Boolean {
        val reg = readRegistry()
        return reg[id]?.get("enabled") == true
    }

    /** Returns the metadata list for every enabled plugin (used by dashboard nav injection). */
    fun listEnabled(): List<PluginMeta> {
        val reg = readRegistry()
        return reg.mapNotNull { (id, entry) ->
            if (entry["enabled"] == true) {
                val manifest = readManifest(id)
                if (manifest != null) toMeta(id, manifest, entry["builtin"] == true) else null
            } else null
        }
    }

    /** Returns metadata + enabled + builtin for every installed plugin. */
    fun listInstalled(): List<Map<String, Any?>> {
        val reg = readRegistry()
        return reg.mapNotNull { (id, entry) ->
            val manifest = readManifest(id) ?: return@mapNotNull null
            val meta = toMeta(id, manifest, entry["builtin"] == true)
            mutableMapOf(
                "id" to meta.id,
                "name" to meta.name,
                "version" to meta.version,
                "author" to meta.author,
                "description" to meta.description,
                "permissions" to meta.permissions,
                "nav" to meta.nav,
                "fab" to meta.fab,
                "widgets" to meta.widgets,
                "features" to featureList(meta, manifest, entry),
                "enabled" to (entry["enabled"] == true),
                "builtin" to meta.builtin,
                "has_bundle" to pluginDir(id).exists()
            )
        }
    }

    /** Derived surfaces from the manifest, plus any custom features declared under "features". */
    private fun availableFeatures(meta: PluginMeta, manifest: Map<String, Any?>): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        if (meta.nav.isNotEmpty()) out.add("nav" to "DASHBOARD")
        if (meta.fab.isNotEmpty()) out.add("fab" to "FAB")
        if (meta.widgets.isNotEmpty()) out.add("widgets" to "WIDGETS")
        if (meta.permissions.contains("notification")) out.add("notifications" to "NOTIFICATIONS")
        if (meta.permissions.contains("ai")) out.add("ai" to "AI")
        @Suppress("UNCHECKED_CAST")
        val extras = manifest["features"] as? List<Map<String, Any?>> ?: emptyList()
        extras.forEach { e ->
            val fid = e["id"] as? String ?: return@forEach
            val label = e["label"] as? String ?: fid.uppercase()
            out.add(fid to label)
        }
        return out
    }

    /** Available features with their current enable state, for the settings UI. */
    private fun featureList(meta: PluginMeta, manifest: Map<String, Any?>, entry: Map<String, Any?>): List<Map<String, Any?>> {
        @Suppress("UNCHECKED_CAST")
        val toggles = (entry["features"] as? Map<String, Any?>) ?: emptyMap()
        return availableFeatures(meta, manifest).map { (fid, label) ->
            mapOf(
                "id" to fid,
                "label" to label,
                "enabled" to ((toggles[fid] as? Boolean) ?: true)
            )
        }
    }

    /** Feature enable map (available features only) for the plugin bridge (V07.features). */
    fun featureMap(id: String): Map<String, Any?> {
        val cleanId = sanitizeId(id)
        val entry = readRegistry()[cleanId]
        val manifest = readManifest(cleanId) ?: return emptyMap()
        val isBuiltin = (entry?.get("builtin") as? Boolean) ?: true
        val meta = toMeta(cleanId, manifest, isBuiltin)
        @Suppress("UNCHECKED_CAST")
        val toggles = (entry?.get("features") as? Map<String, Any?>) ?: emptyMap()
        return availableFeatures(meta, manifest)
            .associate { it.first to ((toggles[it.first] as? Boolean) ?: true) }
    }

    // ─── Install / enable / disable / remove ───────────────────────────────

    /** Validate + extract a downloaded .v07zip bundle and register it as installed+enabled. */
    fun install(id: String, zipBytes: ByteArray): Result {
        if (zipBytes.size > MAX_BUNDLE_BYTES) return Result.Fail("Bundle exceeds ${MAX_BUNDLE_BYTES / 1024} KB limit")
        val cleanId = sanitizeId(id)
        if (cleanId != id) return Result.Fail("Plugin id contains invalid characters")

        val extracted = extractBundle(cleanId, zipBytes)
        if (extracted != null) return Result.Fail(extracted)

        val manifest = readManifest(cleanId) ?: return Result.Fail("Missing manifest.json in bundle")
        val manifestId = manifest["id"] as? String
        if (manifestId != cleanId) return Result.Fail("manifest id mismatch")
        val name = manifest["name"] as? String ?: return Result.Fail("manifest missing 'name'")
        val version = manifest["version"] as? String ?: return Result.Fail("manifest missing 'version'")
        @Suppress("UNCHECKED_CAST")
        val perms = manifest["permissions"] as? List<Any?> ?: emptyList()
        for (p in perms) {
            if (p !is String || !ALLOWED_PERMISSIONS.contains(p)) return Result.Fail("Unapproved permission: $p")
        }
        if (!File(pluginDir(cleanId), "index.html").exists())
            return Result.Fail("manifest requires an 'index.html' entry")

        updateRegistry(cleanId) { mapOf(
            "id" to cleanId,
            "enabled" to true,
            "builtin" to false,
            "name" to name,
            "version" to version
        ) }
        return Result.Ok
    }

    /** Install a plugin from a raw zip picked on-device. The plugin id is read from the
     *  bundle's manifest.json (optionally wrapped in a single top-level folder). */
    fun installFromZip(zipBytes: ByteArray): Result {
        if (zipBytes.size > MAX_BUNDLE_BYTES) return Result.Fail("Bundle exceeds ${MAX_BUNDLE_BYTES / 1024} KB limit")
        val id = readZipId(zipBytes) ?: return Result.Fail("manifest.json (with an 'id') not found in bundle")
        val cleanId = sanitizeId(id)
        if (cleanId != id) return Result.Fail("Plugin id contains invalid characters")
        return install(cleanId, zipBytes)
    }

    /** Locate the bundle's manifest.json and return its declared plugin id (or null). */
    private fun readZipId(zipBytes: ByteArray): String? {
        val root = bundleRoot(zipBytes)
        return try {
            ZipInputStream(zipBytes.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    var name = entry.name
                    if (root.isNotEmpty() && name.startsWith("$root/")) name = name.substring(root.length + 1)
                    if (!entry.isDirectory && name == "manifest.json") {
                        val text = zis.readBytes().toString(Charsets.UTF_8)
                        return (jsonToMap(text)["id"]) as? String
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
                null
            }
        } catch (_: Exception) { null }
    }

    /** Detect a single wrapping top-level folder shared by every entry (common when users
     *  zip a folder). Returns "" when the bundle is flat (each -file at the root). */
    private fun bundleRoot(zipBytes: ByteArray): String {
        var root = ""
        var first = true
        try {
            ZipInputStream(zipBytes.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.isNotEmpty() && !entry.name.startsWith("/")) {
                        val candidate = entry.name.substringBefore('/')
                        if (first) { root = candidate; first = false }
                        else if (candidate != root) return ""
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (_: Exception) { return "" }
        return root
    }

    /** Register a pre-bundled plugin (shipped inside the APK's assets). */
    fun registerBuiltin(id: String) {
        val cleanId = sanitizeId(id)
        updateRegistry(cleanId) { it + mapOf(
            "id" to cleanId,
            "enabled" to true,
            "builtin" to true
        ) }
    }

    fun enable(id: String): Boolean {
        if (!isInstalled(id)) return false
        updateRegistry(id) { it + mapOf("enabled" to true) }
        return true
    }

    fun disable(id: String): Boolean {
        if (!isInstalled(id)) return false
        updateRegistry(id) { it + mapOf("enabled" to false) }
        return true
    }

    /** Toggle a single surface/feature (nav, fab, widgets, custom) for an installed plugin. */
    fun setFeature(id: String, feature: String, enabled: Boolean): Boolean {
        if (!isInstalled(id)) return false
        updateRegistry(id) { entry ->
            @Suppress("UNCHECKED_CAST")
            val feats = (entry["features"] as? Map<String, Any?>)?.toMutableMap() ?: mutableMapOf()
            feats[feature] = enabled
            entry + mapOf("features" to feats)
        }
        return true
    }

    fun remove(id: String): Boolean {
        val dir = pluginDir(id)
        if (dir.exists()) dir.deleteRecursively()
        val reg = readRegistry().toMutableMap()
        reg.remove(id)
        writeRegistry(reg)
        return true
    }

    fun isInstalled(id: String): Boolean = readRegistry().containsKey(sanitizeId(id))

    /** Returns true only if the plugin is installed, enabled, has "notification" permission in manifest, and notifications feature is not toggled off. */
    fun canUseNotifications(id: String): Boolean {
        val cleanId = sanitizeId(id)
        val reg = readRegistry()
        val entry = reg[cleanId]
        val isBuiltin = BUILTIN_PLUGIN_IDS.contains(cleanId) || entry?.get("builtin") == true
        if (entry != null) {
            if (entry["enabled"] == false) return false
            val manifest = readManifest(cleanId) ?: return false
            val meta = toMeta(cleanId, manifest, isBuiltin)
            if (!meta.permissions.contains("notification")) return false
            @Suppress("UNCHECKED_CAST")
            val toggles = (entry["features"] as? Map<String, Any?>) ?: emptyMap()
            return (toggles["notifications"] as? Boolean) ?: true
        }
        val manifest = readManifest(cleanId) ?: return false
        val meta = toMeta(cleanId, manifest, isBuiltin)
        return meta.permissions.contains("notification")
    }

    /** Returns true only if the plugin is installed, enabled, has "ai" permission in manifest, and AI feature is not toggled off. */
    fun canUseAI(id: String): Boolean {
        val cleanId = sanitizeId(id)
        val reg = readRegistry()
        val entry = reg[cleanId]
        val isBuiltin = BUILTIN_PLUGIN_IDS.contains(cleanId) || entry?.get("builtin") == true
        if (entry != null) {
            if (entry["enabled"] == false) return false
            val manifest = readManifest(cleanId) ?: return false
            val meta = toMeta(cleanId, manifest, isBuiltin)
            if (!meta.permissions.contains("ai")) return false
            @Suppress("UNCHECKED_CAST")
            val toggles = (entry["features"] as? Map<String, Any?>) ?: emptyMap()
            return (toggles["ai"] as? Boolean) ?: true
        }
        val manifest = readManifest(cleanId) ?: return false
        val meta = toMeta(cleanId, manifest, isBuiltin)
        return meta.permissions.contains("ai")
    }

    // ─── Bundle extraction ─────────────────────────────────────────────────

    private fun extractBundle(id: String, zipBytes: ByteArray): String? {
        val dir = pluginDir(id)
        if (dir.exists()) dir.deleteRecursively()
        dir.mkdirs()
        var count = 0
        val root = bundleRoot(zipBytes)
        try {
            ZipInputStream(zipBytes.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (++count > MAX_PLUGIN_FILES) return "Too many files in bundle"
                    var name = entry.name
                    // Strip a single wrapping folder so "my-plugin/manifest.json" lands at the root.
                    if (root.isNotEmpty() && name.startsWith("$root/")) name = name.substring(root.length + 1)
                    if (name.isEmpty()) {
                        zis.closeEntry()
                        entry = zis.nextEntry
                        continue
                    }
                    // Path traversal guard: reject absolute paths or any "../".
                    if (name.startsWith("/") || name.split('/').any { it == ".." }) {
                        return "Invalid path in bundle: $entry.name"
                    }
                    val target = File(dir, name)
                    if (entry.isDirectory) {
                        target.mkdirs()
                    } else {
                        target.parentFile?.mkdirs()
                        target.outputStream().use { out -> zis.copyTo(out) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            return "Failed to extract bundle: ${e.message}"
        }
        return null
    }

    sealed class Result {
        object Ok : Result()
        data class Fail(val message: String) : Result()
    }
}
