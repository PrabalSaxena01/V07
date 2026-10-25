package com.v07.v3.app.server

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.v07.v3.app.telemetry.ScreenTimeTracker
import com.v07.v3.app.timer.TimerManager
import com.v07.v3.core.data.DatabaseManager
import com.v07.v3.core.data.EventRepository
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * LoopbackServer
 *
 * High-performance, lightweight HTTP loopback server strictly bound to 127.0.0.1.
 * Serves the V3 UI shell assets and handles REST API requests via ApiRouter.
 * Guarantees zero external network exposure and enforces session token authentication.
 */
class LoopbackServer(
    private val context: Context,
    private val repository: EventRepository,
    private val databaseManager: DatabaseManager,
    private val timerManager: TimerManager,
    private val screenTimeTracker: ScreenTimeTracker,
    private val pluginRegistry: com.v07.v3.core.plugin.PluginRegistry,
    private val syncManager: com.v07.v3.core.sync.V3CompanionSyncManager
) {
    companion object {
        private const val TAG = "V3LoopbackServer"
        private const val PREFS_NAME = "v3_server_security"
        private const val KEY_SESSION_TOKEN = "session_token"
        val CANDIDATE_PORTS = listOf(8000, 8008, 8080, 8888)
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val sessionToken: String = getOrCreateSessionToken()
    private val router: ApiRouter = ApiRouter(context, repository, databaseManager, timerManager, screenTimeTracker, pluginRegistry, syncManager, sessionToken)

    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null
    var boundPort: Int = 0
        private set

    val baseUrl: String
        get() = "http://127.0.0.1:$boundPort"

    private fun getOrCreateSessionToken(): String {
        var token = prefs.getString(KEY_SESSION_TOKEN, null)
        if (token.isNullOrBlank()) {
            token = UUID.randomUUID().toString().replace("-", "")
            prefs.edit().putString(KEY_SESSION_TOKEN, token).apply()
        }
        return token
    }

    /**
     * Starts the loopback HTTP server.
     */
    @Synchronized
    fun start(preferredPort: Int = 8000): Int {
        if (serverSocket != null && !serverSocket!!.isClosed) {
            return boundPort
        }

        val ports = if (preferredPort in CANDIDATE_PORTS) {
            listOf(preferredPort) + CANDIDATE_PORTS.filter { it != preferredPort }
        } else {
            listOf(preferredPort) + CANDIDATE_PORTS
        }

        var socket: ServerSocket? = null
        for (port in ports) {
            try {
                socket = ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))
                boundPort = port
                break
            } catch (e: Exception) {
                Log.w(TAG, "Port $port unavailable: ${e.message}")
            }
        }

        var bound = socket
        if (bound == null) {
            try {
                bound = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
                boundPort = bound.localPort
                Log.w(TAG, "Candidate ports unavailable. Dynamically bound to loopback port: $boundPort")
            } catch (e: Exception) {
                throw IllegalStateException("Failed to bind any loopback port from $ports and dynamic port failed: ${e.message}", e)
            }
        }
        serverSocket = bound
        Log.i(TAG, "V3 LoopbackServer listening on http://127.0.0.1:$boundPort")

        serverScope.launch {
            while (isActive && !bound.isClosed) {
                try {
                    val client = bound.accept()
                    // Strict loopback verification
                    if (!client.inetAddress.isLoopbackAddress) {
                        Log.w(TAG, "Rejected non-loopback connection from ${client.inetAddress}")
                        client.close()
                        continue
                    }
                    launch(Dispatchers.IO) {
                        handleClient(client)
                    }
                } catch (e: Exception) {
                    if (!bound.isClosed) {
                        Log.w(TAG, "Accept loop error: ${e.message}")
                    }
                    break
                }
            }
        }

        return boundPort
    }

    /**
     * Stops the loopback server cleanly.
     */
    @Synchronized
    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        serverScope.cancel()
        Log.i(TAG, "V3 LoopbackServer stopped")
    }

    private suspend fun handleClient(client: Socket) {
        try {
            client.soTimeout = 10000 // 10s timeout
            val input = client.getInputStream()
            val output = client.getOutputStream()

            val request = parseRequest(input)
            if (request == null) {
                HttpResponse.error("Bad Request", 400).writeTo(output)
                return
            }

            val isApi = request.path.startsWith("/api/") ||
                        request.path.startsWith("/events") ||
                        request.path.startsWith("/event") ||
                        request.path.startsWith("/ai/") ||
                        request.path.startsWith("/session") ||
                        request.path.startsWith("/sessions") ||
                        request.path.startsWith("/tasks") ||
                        request.path.startsWith("/task") ||
                        request.path.startsWith("/checklist") ||
                        request.path.startsWith("/thoughts") ||
                        request.path.startsWith("/thought") ||
                        request.path.startsWith("/daytracker") ||
                        request.path.startsWith("/day-tracker") ||
                        request.path.startsWith("/day/") ||
                        request.path == "/day" ||
                        request.path == "/timeline" ||
                        request.path.startsWith("/timeline") ||
                        request.path.startsWith("/recycle-bin") ||
                        request.path.startsWith("/recyclebin") ||
                        request.path.startsWith("/database") ||
                        request.path.startsWith("/plugins") ||
                        request.path.startsWith("/app-limits") ||
                        request.path.startsWith("/app-usage") ||
                        request.path.startsWith("/gita") ||
                        request.path.startsWith("/meta/") ||
                        request.path == "/client/log" ||
                        request.path.startsWith("/edit")

            val response = if (isApi) {
                router.handle(request)
            } else {
                serveStaticAsset(request.path)
            }

            response.writeTo(output, request.getHeader("Origin"))
        } catch (e: Exception) {
            Log.e(TAG, "Client handling exception: ${e.message}")
        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    private fun parseRequest(input: InputStream): HttpRequest? {
        val reader = input.bufferedReader(StandardCharsets.UTF_8)
        val requestLine = reader.readLine() ?: return null
        val parts = requestLine.split(" ")
        if (parts.size < 2) return null

        val method = parts[0].uppercase()
        val rawUri = parts[1]
        val path = rawUri.substringBefore('?')
        val queryString = if (rawUri.contains('?')) rawUri.substringAfter('?') else ""

        val queryParams = mutableMapOf<String, String>()
        if (queryString.isNotBlank()) {
            for (param in queryString.split('&')) {
                val kv = param.split('=', limit = 2)
                val k = URLDecoder.decode(kv[0], "UTF-8")
                val v = if (kv.size > 1) URLDecoder.decode(kv[1], "UTF-8") else ""
                queryParams[k] = v
            }
        }

        val headers = mutableMapOf<String, String>()
        var contentLength = 0
        while (true) {
            val headerLine = reader.readLine() ?: break
            if (headerLine.isEmpty()) break
            val colonIdx = headerLine.indexOf(':')
            if (colonIdx > 0) {
                val headerName = headerLine.substring(0, colonIdx).trim()
                val headerVal = headerLine.substring(colonIdx + 1).trim()
                headers[headerName] = headerVal
                if (headerName.equals("Content-Length", ignoreCase = true)) {
                    contentLength = headerVal.toIntOrNull() ?: 0
                }
            }
        }

        val body = if (contentLength > 0) {
            val buffer = CharArray(contentLength)
            var read = 0
            while (read < contentLength) {
                val count = reader.read(buffer, read, contentLength - read)
                if (count == -1) break
                read += count
            }
            String(buffer, 0, read)
        } else {
            ""
        }

        return HttpRequest(
            method = method,
            path = path,
            queryParams = queryParams,
            headers = headers,
            body = body
        )
    }

    private val assetCache = java.util.concurrent.ConcurrentHashMap<String, HttpResponse>()

    fun clearAssetCache() {
        assetCache.clear()
    }

    private fun serveStaticAsset(requestedPath: String): HttpResponse {
        val clean = requestedPath.removePrefix("/").substringBefore('?')
        // Threat 1 Hardening: Reject path traversal attempts
        if (clean.contains("..") || clean.contains('\\') || clean.contains("%2e", ignoreCase = true) || clean.contains('\u0000')) {
            return HttpResponse.error("Forbidden: Invalid asset path", 403)
        }
        val cacheKey = clean.ifEmpty { "index.html" }
        val cached = assetCache[cacheKey]
        if (cached != null) return cached

        val candidates = when (clean) {
            "", "index.html" -> listOf("v3/index.html")
            "admin", "admin.html" -> listOf("v3/admin.html", "admin.html")
            "companion", "companion.html" -> listOf("v3/companion.html", "companion.html")
            else -> if (clean.startsWith("v3/")) listOf(clean, clean.removePrefix("v3/")) else listOf("v3/$clean", clean)
        }

        // Try Android assets first
        for (cand in candidates) {
            try {
                val assetBytes = context.assets.open(cand).use { it.readBytes() }
                val resp = formatAssetResponse(cand, assetBytes)
                assetCache[cacheKey] = resp
                return resp
            } catch (_: Exception) {}
        }

        // Try external storage (e.g. plugins directory) with strict canonical directory containment
        val extBases = listOfNotNull(
            com.v07.v3.core.data.V07Database.resolveDatabaseFile(context).parentFile,
            context.getExternalFilesDir(null),
            context.filesDir
        )
        for (baseDir in extBases) {
            try {
                val candidate = java.io.File(baseDir, clean).canonicalFile
                val canonicalBase = baseDir.canonicalFile
                val isContained = candidate.path.startsWith(canonicalBase.path + java.io.File.separator) || candidate.path == canonicalBase.path
                if (isContained && candidate.exists() && candidate.isFile) {
                    val bytes = candidate.readBytes()
                    val resp = formatAssetResponse(clean, bytes)
                    assetCache[cacheKey] = resp
                    return resp
                }
            } catch (_: Exception) {}
        }

        return HttpResponse.error("Asset not found: $requestedPath", 404)
    }

    private fun formatAssetResponse(assetPath: String, rawBytes: ByteArray): HttpResponse {
        val mime = getMimeType(assetPath)
        val headers = mapOf(
            "Cache-Control" to "public, max-age=86400",
            "ETag" to "\"v3-${assetPath.hashCode()}\""
        )
        if (!assetPath.endsWith(".html")) {
            return HttpResponse.bytes(rawBytes, mime, headers = headers)
        }

        // HTML asset: inject platform & compatibility scripts
        try {
            val htmlStr = String(rawBytes, StandardCharsets.UTF_8)
            val script = if (assetPath.contains("plugins/")) {
                val segs = assetPath.substringAfter("plugins/").split('/')
                val pluginId = if (segs.isNotEmpty()) segs[0] else "unknown"
                "<link rel=\"stylesheet\" href=\"$baseUrl/vector_plugins.css\">\n" +
                "<script>\n" + com.v07.v3.core.plugin.PlatformJsBridge.generatePluginCompatibilityScript(pluginId, baseUrl, sessionToken) + "\n</script>"
            } else {
                "<script>\n" + com.v07.v3.core.plugin.PlatformJsBridge.generateInjectionScript("com.v07.core", baseUrl, sessionToken) + "\n</script>"
            }

            val modifiedHtml = if (htmlStr.contains("<head>", ignoreCase = true)) {
                htmlStr.replaceFirst("(?i)<head>".toRegex(), "<head>\n$script\n")
            } else {
                "$script\n$htmlStr"
            }
            return HttpResponse.bytes(modifiedHtml.toByteArray(StandardCharsets.UTF_8), mime, headers = headers)
        } catch (e: Exception) {
            return HttpResponse.bytes(rawBytes, mime, headers = headers)
        }
    }

    private fun getMimeType(path: String): String {
        return when {
            path.endsWith(".html") -> "text/html; charset=utf-8"
            path.endsWith(".css") -> "text/css; charset=utf-8"
            path.endsWith(".js") -> "application/javascript; charset=utf-8"
            path.endsWith(".json") -> "application/json; charset=utf-8"
            path.endsWith(".svg") -> "image/svg+xml"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
            path.endsWith(".ico") -> "image/x-icon"
            path.endsWith(".woff2") -> "font/woff2"
            path.endsWith(".woff") -> "font/woff"
            path.endsWith(".ttf") -> "font/ttf"
            else -> "application/octet-stream"
        }
    }
}
