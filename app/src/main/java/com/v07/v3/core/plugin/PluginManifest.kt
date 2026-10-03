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
    val publisher: String = "V07 Core",
    val permissions: Set<String> = emptySet(),
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
        // Legacy permission aliases
        if (permission == "storage:isolated" && (permissions.contains("events:write") || permissions.contains("events:read"))) return true
        if (permission == "telemetry:read" && permissions.contains("server")) return true
        return false
    }
}

/**
 * PluginManifestParser
 *
 * Robust, resilient parser for V3 machine-readable plugin contracts.
 * Handles both the modern V3 schema and legacy plugin manifests seamlessly.
 */
object PluginManifestParser {

    fun parse(jsonStr: String): V3PluginManifest? {
        return try {
            val json = JSONObject(jsonStr)
            val id = json.optString("id").trim()
            if (id.isBlank()) return null

            val name = json.optString("name", id)
            val version = json.optString("version", "1.0.0")
            val apiVersion = json.optString("apiVersion", "3.0")
            val description = json.optString("description", "")
            val publisher = if (json.has("publisher")) {
                val pubObj = json.optJSONObject("publisher")
                pubObj?.optString("name", "V07 Core") ?: json.optString("publisher", "V07 Core")
            } else {
                json.optString("author", "V07 Core")
            }

            // Permissions
            val permSet = mutableSetOf<String>()
            val permArr = json.optJSONArray("permissions")
            if (permArr != null) {
                for (i in 0 until permArr.length()) {
                    permSet.add(permArr.getString(i).trim())
                }
            }

            // Navigation & Kit Tile
            val navObj = json.optJSONObject("nav")
            val navIcon = navObj?.optString("icon") ?: "extension"
            val navLabel = navObj?.optString("label") ?: name.uppercase()
            val navColor = navObj?.optString("color") ?: "#4DE086"

            // Entry points
            var entry = json.optString("entry", "index.html")
            var settingsEntry = json.optString("settings", "").ifBlank { null }

            val extObj = json.optJSONObject("extensionPoints")
            if (extObj != null) {
                val pageObj = extObj.optJSONObject("page")
                if (pageObj != null && pageObj.has("entry")) {
                    entry = pageObj.getString("entry")
                }
                val setObj = extObj.optJSONObject("settings")
                if (setObj != null && setObj.has("schema")) {
                    settingsEntry = setObj.getString("schema")
                }
            }

            // Widgets
            val widgetList = mutableListOf<V3WidgetInfo>()
            val widgetsArr = json.optJSONArray("widgets")
            if (widgetsArr != null) {
                for (i in 0 until widgetsArr.length()) {
                    val w = widgetsArr.optJSONObject(i) ?: continue
                    val wId = w.optString("id", "${id}_w$i")
                    val wTitle = w.optString("title", name)
                    val wPath = w.optString("path", w.optString("entry", "widget.html"))
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
