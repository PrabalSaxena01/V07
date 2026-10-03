package com.v07.dashboard

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Client for the plugin store. Fetches the store index (a JSON document listing
 * available plugins) and downloads .v07zip bundle files, typically hosted on GitHub
 * (plugins.json served as a raw URL, bundles as GitHub Release assets).
 */
object PluginStoreClient {

    /** Default index URL — points at a GitHub repo hosting plugins.json. */
    const val DEFAULT_INDEX_URL =
        "https://raw.githubusercontent.com/prabalv07/v07-plugins/main/plugins.json"

    private const val TIMEOUT_MS = 15000

    /** Fetch the store index JSON text from [url] (or default). */
    fun fetchIndex(url: String = DEFAULT_INDEX_URL): String {
        return httpGet(url) ?: ""
    }

    /** Download a plugin bundle from [bundleUrl], returning raw bytes, or null on failure. */
    fun downloadBundle(bundleUrl: String): ByteArray? {
        return httpGetBytes(bundleUrl)
    }

    private fun httpGet(urlString: String): String? {
        val bytes = httpGetBytes(urlString) ?: return null
        return String(bytes, Charsets.UTF_8)
    }

    private fun httpGetBytes(urlString: String): ByteArray? {
        return try {
            val conn = URL(urlString).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.connectTimeout = TIMEOUT_MS
                conn.readTimeout = TIMEOUT_MS
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("Accept", "application/octet-stream")
                val code = conn.responseCode
                if (code !in 200..299) return null
                val input = conn.inputStream
                val out = ByteArrayOutputStream()
                input.use { it.copyTo(out) }
                out.toByteArray()
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) { null }
    }
}
