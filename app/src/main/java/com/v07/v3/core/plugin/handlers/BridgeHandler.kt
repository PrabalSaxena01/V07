package com.v07.v3.core.plugin.handlers

import org.json.JSONObject

/**
 * Interface for modular WebView JavaScript bridge command handlers.
 * Decomposes monolithic PlatformJsBridge into testable domain handlers.
 */
interface BridgeHandler {
    /**
     * Checks if this handler can process the given RPC method name.
     */
    fun canHandle(method: String): Boolean

    /**
     * Executes the RPC method and returns a JSON response string.
     */
    fun handle(pluginId: String, method: String, params: JSONObject): String?
}

fun bridgeSuccess(data: JSONObject): String {
    return JSONObject().apply {
        put("ok", true)
        put("status", "success")
        put("result", data)
        put("data", data)
    }.toString()
}

fun bridgeError(errorMessage: String): String {
    return JSONObject().apply {
        put("ok", false)
        put("status", "error")
        put("error", errorMessage)
        put("message", errorMessage)
    }.toString()
}
