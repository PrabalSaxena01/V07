package com.v07.v3.core.plugin

import org.json.JSONArray
import org.json.JSONObject

data class V3WidgetInfo(
    val id: String,
    val title: String,
    val entry: String,
    val icon: String? = null,
    val color: String? = null,
    val supportedSizes: List<String> = listOf("compact", "small", "medium", "large"),
    val defaultSize: String = "medium"
)

data class V3PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: String = "3.0",
    val description: String = "",
    val publisher: String = "V07 Core Team",
    val permissions: Set<String> = emptySet(),
    val capabilities: Set<String> = emptySet(),
    val dependencies: Map<String, String> = emptyMap(),
    val entry: String = "index.html",
    val settingsEntry: String? = null,
    val navIcon: String = "extension",
    val navLabel: String = "",
    val navColor: String = "#4DE086",
    val widgets: List<V3WidgetInfo> = emptyList()
) {
    fun hasPermission(permission: String): Boolean {
        // Universal permission matching
        if (permissions.contains("*")) return true
        if (permissions.contains(permission)) return true
        // Legacy and cross-spec permission aliases
        if (permission == "storage:isolated" && (permissions.contains("events:write") || permissions.contains("events:read"))) return true
        if (permission == "events:listen" && (permissions.contains("events:read") || permissions.contains("events:listen"))) return true
        if (permission == "events:emit" && (permissions.contains("events:write") || permissions.contains("events:emit"))) return true
        if (permission == "telemetry:read" && (permissions.contains("server") || permissions.contains("telemetry:read") || permissions.contains("sessions:read"))) return true
        if (permission == "notifications:schedule" && (permissions.contains("notification") || permissions.contains("notifications:schedule"))) return true
        return false
    }
}

/**
 * PluginManifestParser
 *
 * Robust, resilient parser for V3 machine-readable plugin contracts conforming to PLUGIN_SPECIFICATION.md.
 * Handles modern extensionPoints (widgets, kitTile, page, settings), capabilities, permissions,
 * and legacy plugin manifests seamlessly.
 */
object PluginManifestParser {

    fun parse(jsonStr: String): V3PluginManifest? {
        return try {
            val json = JSONObject(jsonStr)
            val id = json.optString("id").trim()
            if (id.isBlank()) return null

            val name = json.optString("name", id)
            val version = json.optString("version", "3.0.0")
            val apiVersion = json.optString("apiVersion", "3.0")
            val description = json.optString("description", "")
            val publisher = if (json.has("publisher")) {
                val pubObj = json.optJSONObject("publisher")
                pubObj?.optString("name", "V07 Core Team") ?: json.optString("publisher", "V07 Core Team")
            } else {
                json.optString("author", "V07 Core Team")
            }

            // Permissions
            val permSet = mutableSetOf<String>()
            val permArr = json.optJSONArray("permissions")
            if (permArr != null) {
                for (i in 0 until permArr.length()) {
                    permSet.add(permArr.getString(i).trim())
                }
            }

            // Capabilities
            val capSet = mutableSetOf<String>()
            val capArr = json.optJSONArray("capabilities")
            if (capArr != null) {
                for (i in 0 until capArr.length()) {
                    capSet.add(capArr.getString(i).trim())
                }
            }

            // Dependencies
            val depMap = mutableMapOf<String, String>()
            val depObj = json.optJSONObject("dependencies")
            if (depObj != null) {
                val keys = depObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    depMap[k] = depObj.optString(k, "")
                }
            }

            // Extension Points
            val extObj = json.optJSONObject("extensionPoints")

            // Navigation & Kit Tile
            val kitTileObj = extObj?.optJSONObject("kitTile") ?: json.optJSONObject("nav")
            val navIcon = kitTileObj?.optString("icon") ?: "extension"
            val navLabel = kitTileObj?.optString("label") ?: name.uppercase()
            val navColor = kitTileObj?.optString("color") ?: "#4DE086"

            // Entry points (Page & Settings)
            var entry = json.optString("entry", "index.html")
            var settingsEntry = json.optString("settings", "").ifBlank { null }

            if (extObj != null) {
                val pageObj = extObj.optJSONObject("page")
                if (pageObj != null && pageObj.has("entry")) {
                    entry = pageObj.getString("entry")
                }
                val setObj = extObj.optJSONObject("settings")
                if (setObj != null) {
                    settingsEntry = setObj.optString("schema", setObj.optString("entry", ""))
                    if (settingsEntry.isBlank()) settingsEntry = null
                }
            }

            // Widgets (from extensionPoints.widgets or root widgets)
            val widgetList = mutableListOf<V3WidgetInfo>()
            val widgetsArr = extObj?.optJSONArray("widgets") ?: json.optJSONArray("widgets")
            if (widgetsArr != null) {
                for (i in 0 until widgetsArr.length()) {
                    val w = widgetsArr.optJSONObject(i) ?: continue
                    val wId = w.optString("id", "${id}_w$i")
                    val wTitle = w.optString("title", name)
                    val wPath = w.optString("entry", w.optString("path", "widget.html"))
                    val wIcon = w.optString("icon", navIcon)
                    val wColor = w.optString("color", navColor)
                    val defSize = w.optString("defaultSize", "medium")

                    val sizes = mutableListOf<String>()
                    val sizesArr = w.optJSONArray("supportedSizes")
                    if (sizesArr != null) {
                        for (s in 0 until sizesArr.length()) sizes.add(sizesArr.getString(s))
                    } else {
                        sizes.addAll(listOf("compact", "small", "medium", "large"))
                    }

                    widgetList.add(
                        V3WidgetInfo(
                            id = wId,
                            title = wTitle,
                            entry = wPath,
                            icon = wIcon,
                            color = wColor,
                            supportedSizes = sizes,
                            defaultSize = defSize
                        )
                    )
                }
            }

            V3PluginManifest(
                id = id,
                name = name,
                version = version,
                apiVersion = apiVersion,
                description = description,
                publisher = publisher,
                permissions = permSet,
                capabilities = capSet,
                dependencies = depMap,
                entry = entry,
                settingsEntry = settingsEntry,
                navIcon = navIcon,
                navLabel = navLabel,
                navColor = navColor,
                widgets = widgetList
            )
        } catch (_: Exception) {
            null
        }
    }

    fun validate(manifest: V3PluginManifest): List<String> {
        val errors = mutableListOf<String>()
        if (manifest.id.isBlank()) errors.add("Plugin ID is required")
        if (!manifest.id.contains('.')) errors.add("Plugin ID must be namespaced (e.g., com.v07.habits)")
        if (manifest.name.isBlank()) errors.add("Plugin Name is required")
        if (manifest.entry.isBlank()) errors.add("Plugin Entry HTML file is required")
        return errors
    }
}
