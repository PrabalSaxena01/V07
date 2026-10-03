package com.v07.dashboard

/**
 * Bridges a V07 bridge call to Android's async SAF file picker WITHOUT blocking
 * the WebView renderer thread. The bridge method returns immediately; when
 * MainActivity's activity-result callback finishes installing the bundle, the
 * result JSON is pushed back into the page via [poster].
 */
object PluginZipImporter {

    @Volatile private var launcher: (() -> Unit)? = null
    @Volatile private var poster: ((String) -> Unit)? = null

    /** MainActivity registers its SAF launcher here once (setupWebView). */
    @Synchronized fun bind(launch: () -> Unit) { launcher = launch }

    /** MainActivity registers the JS push channel (webView.evaluateJavascript). */
    @Synchronized fun bindPoster(push: (String) -> Unit) { poster = push }

    /** Called from the V07 bridge (JS thread). Launches the picker, returns at once. */
    fun pickAndInstall(): Map<String, Any?> {
        val fn = launcher
        if (fn == null) return mapOf("ok" to false, "error" to "picker unavailable")
        return try {
            fn()
            mapOf("ok" to true, "pending" to true)
        } catch (e: Exception) {
            mapOf("ok" to false, "error" to (e.message ?: "failed to open picker"))
        }
    }

    /** MainActivity's activity-result callback publishes the install outcome to JS. */
    fun complete(result: Map<String, Any?>) {
        poster?.invoke(toJson(result))
    }
}