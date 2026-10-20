package com.v07.dashboard

import org.json.JSONObject
import org.json.JSONArray
import kotlinx.serialization.json.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import java.io.ByteArrayOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.URLDecoder
import java.io.File
import android.provider.Settings
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

var lastRequestTime = 0L

val DB_EVENTS_DIR: File get() = VaultConfig.eventsDir
val DB_GITA_DIR: File get() = VaultConfig.gitaDir
val DB_APP_USAGE_DIR: File get() = VaultConfig.appUsageDir

private val appIconCache = LruCache<String, ByteArray>(150)

fun getAppIconPng(ctx: android.content.Context, packageName: String, targetSize: Int = 64): ByteArray? {
    val cleanPkg = packageName.trim()
    if (cleanPkg.isEmpty()) return null
    val cacheKey = "$cleanPkg:$targetSize"
    val cached = appIconCache.get(cacheKey)
    if (cached != null) return cached

    return try {
        val d: Drawable = if (cleanPkg == ctx.packageName) {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.mipmap.ic_launcher) ?: ctx.packageManager.getApplicationIcon(cleanPkg)
        } else {
            ctx.packageManager.getApplicationIcon(cleanPkg)
        }
        val w = d.intrinsicWidth.coerceAtLeast(targetSize)
        val h = d.intrinsicHeight.coerceAtLeast(targetSize)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        d.setBounds(0, 0, canvas.width, canvas.height)
        d.draw(canvas)

        val scaled = if (w != targetSize || h != targetSize) {
            Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
        } else bitmap

        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 90, baos)
        val bytes = baos.toByteArray()
        appIconCache.put(cacheKey, bytes)
        bytes
    } catch (_: Exception) {
        null
    }
}

private var appContext: android.content.Context? = null

fun broadcastSessionChanged() {
    val ctx = appContext ?: return
    try {
        val intent = android.content.Intent("com.v07.dashboard.SESSION_CHANGED")
        ctx.sendBroadcast(intent)
        android.util.Log.i("V07Server", "Broadcast session changed")
    } catch (e: Exception) {
        android.util.Log.e("V07Server", "broadcastSessionChanged failed: ${e.message}")
    }
}

fun jsonToMap(text: String): MutableMap<String, Any?> {
    val root = try { json.decodeFromString<JsonElement>(text) } catch (_: Exception) { return mutableMapOf() }
    return jsonElementToMap(root) as? MutableMap<String, Any?> ?: mutableMapOf()
}

fun jsonElementToMap(elem: JsonElement): Any? {
    return when (elem) {
        is JsonObject -> {
            val m = mutableMapOf<String, Any?>()
            for ((k, v) in elem) m[k] = jsonElementToMap(v)
            m
        }
        is JsonArray -> elem.map { jsonElementToMap(it) }
        is JsonPrimitive -> {
            if (elem.isString) elem.content
            else {
                val s = elem.content
                when {
                    s == "true" -> true
                    s == "false" -> false
                    else -> s.toIntOrNull() ?: s.toLongOrNull() ?: s.toDoubleOrNull() ?: s
                }
            }
        }
        is JsonNull -> null
    }
}

object V07ServerConfig {
    var port: Int = 8000
    val baseUrl: String get() = "http://127.0.0.1:$port"
}

fun startServer(port: Int = 8000, ctx: android.content.Context? = null): ServerSocket {
    if (ctx != null) appContext = ctx
    val candidatePorts = listOf(port, 8008, 8080, 8081, 8888)
    var socket: ServerSocket? = null
    var selectedPort = port
    for (candidate in candidatePorts) {
        try {
            socket = ServerSocket(candidate, 50, java.net.InetAddress.getByName("127.0.0.1"))
            selectedPort = candidate
            break
        } catch (e: Exception) {
            android.util.Log.w("V07Server", "Port $candidate not available: ${e.message}")
        }
    }
    val server = socket ?: throw IllegalStateException("Failed to bind any port from $candidatePorts")
    V07ServerConfig.port = selectedPort
    android.util.Log.i("V07Server", "Server started successfully on port $selectedPort (loopback only)")
    Thread {
        while (true) {
            try {
                val client = server.accept()
                if (!client.inetAddress.isLoopbackAddress) {
                    android.util.Log.w("V07Server", "Rejecting non-loopback connection from ${client.inetAddress}")
                    try { client.close() } catch (_: Exception) {}
                    continue
                }
                android.util.Log.v("V07Server", "Accepted connection from ${client.inetAddress}:${client.port}")
                Thread { handleClient(client.getInputStream(), client.getOutputStream()) }.start()
            } catch (e: Exception) {
                android.util.Log.w("V07Server", "Accept loop exiting: ${e.message}")
                break
            }
        }
    }.start()
    return server
}

fun toJson(obj: Any?): String = buildString { appendJson(obj) }

private fun StringBuilder.appendJson(obj: Any?) {
    when (obj) {
        null -> append("null")
        is Boolean -> append(obj.toString())
        is Number -> append(obj.toString())
        is String -> {
            append('"')
            obj.forEach { c -> when (c) { '"' -> append("\\\""); '\\' -> append("\\\\"); '\n' -> append("\\n"); '\r' -> append("\\r"); '\t' -> append("\\t"); else -> append(c) } }
            append('"')
        }
        is List<*> -> {
            append('[')
            obj.forEachIndexed { i, v -> if (i > 0) append(','); appendJson(v) }
            append(']')
        }
        is Map<*, *> -> {
            append('{')
            var first = true
            for ((k, v) in obj) {
                if (!first) append(',')
                first = false
                appendJson(k.toString())
                append(':')
                appendJson(v)
            }
            append('}')
        }
        is Event -> appendJson(obj.toEventMap())
        else -> appendJson(obj.toString())
    }
}

private fun handleClient(input: java.io.InputStream, output: java.io.OutputStream) {
    try {
        val bis = java.io.BufferedInputStream(input, 16384)

        val requestLineBuf = StringBuilder()
        while (true) { val b = bis.read(); if (b == -1) return; if (b == '\n'.code) break; if (b != '\r'.code) requestLineBuf.append(b.toChar()) }
        val requestLine = requestLineBuf.toString()
        val parts = requestLine.split(" ")
        if (parts.size < 2) return
        lastRequestTime = System.currentTimeMillis()
        val method = parts[0]
        val fullPath = parts[1]
        val pathQuery = fullPath.split("?", limit = 2)
        val path = URLDecoder.decode(pathQuery[0], "UTF-8")
        val query = pathQuery.getOrNull(1)

        val headers = mutableMapOf<String, String>()
        var contentLength = 0
        while (true) {
            val lineBuf = StringBuilder()
            while (true) { val b = bis.read(); if (b == -1) return; if (b == '\n'.code) break; if (b != '\r'.code) lineBuf.append(b.toChar()) }
            val line = lineBuf.toString()
            if (line.isEmpty()) break
            val colon = line.indexOf(':')
            if (colon > 0) {
                val key = line.substring(0, colon).trim().lowercase()
                val value = line.substring(colon + 1).trim()
                headers[key] = value
                if (key == "content-length") contentLength = value.toIntOrNull() ?: 0
            }
        }

        var body = ""
        if (contentLength > 0) {
            val bodyBytes = bis.readNBytes(contentLength)
            body = String(bodyBytes, Charsets.UTF_8)
        }

        val params = parseQuery(query)

        var status = 200
        var contentType = "application/json"
        var responseBody = ""

        try {
            android.util.Log.i("V07Server", "-> $method $fullPath")
            val p = params
            when {
                path == "/" || path == "/index.html" -> { contentType = "text/html"; responseBody = loadIndexHtml() }
                path == "/companion" || path == "/companion.html" -> { contentType = "text/html"; responseBody = loadAssetHtml("companion.html") }
                path == "/admin" || path == "/admin.html" -> { contentType = "text/html"; responseBody = loadAssetHtml("admin.html") }
                path == "/manifest.json" -> responseBody = toJson(manifestData)
                path == "/sw.js" -> { status = 404; responseBody = "" }
                path.startsWith("/app-icon") -> {
                    val ctx = appContext
                    val pkg = (p["package"] ?: p["pkg"] ?: "").trim()
                    if (ctx != null && pkg.isNotEmpty()) {
                        val size = (p["size"] ?: "64").toIntOrNull()?.coerceIn(24, 256) ?: 64
                        val bytes = getAppIconPng(ctx, pkg, size)
                        if (bytes != null) {
                            output.write("HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nContent-Length: ${bytes.size}\r\nAccess-Control-Allow-Origin: *\r\nCache-Control: public, max-age=604800\r\nConnection: close\r\n\r\n".toByteArray())
                            output.write(bytes)
                            output.flush()
                            return
                        }
                    }
                    status = 404; responseBody = """{"error":"app icon not found"}"""
                }
                path.startsWith("/icon") -> {
                    val ctx = appContext
                    val s = (p["size"] ?: "192").toIntOrNull()?.coerceIn(32, 512) ?: 192
                    val format = p["format"] ?: (if (p.containsKey("png") || path.endsWith(".png")) "png" else "auto")
                    if (ctx != null && format != "svg") {
                        val bytes = getAppIconPng(ctx, ctx.packageName, s)
                        if (bytes != null) {
                            output.write("HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nContent-Length: ${bytes.size}\r\nAccess-Control-Allow-Origin: *\r\nCache-Control: public, max-age=86400\r\nConnection: close\r\n\r\n".toByteArray())
                            output.write(bytes)
                            output.flush()
                            return
                        }
                    }
                    contentType = "image/svg+xml"
                    val r = (s * 0.35).roundToInt()
                    val fs = (s * 0.16).roundToInt()
                    responseBody = """<svg xmlns="http://www.w3.org/2000/svg" width="$s" height="$s" viewBox="0 0 $s $s"><rect width="$s" height="$s" rx="$r" fill="#7c5bf5"/><text x="50%" y="56%" dominant-baseline="middle" text-anchor="middle" fill="white" font-size="${fs}px" font-weight="bold" font-family="-apple-system, sans-serif">V07</text></svg>"""
                }
                path == "/health" -> responseBody = toJson(mapOf("status" to "ok", "timestamp" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                path.startsWith("/app-usage") -> responseBody = handleAppUsage(method, path, p, body)
                path.startsWith("/event/") -> responseBody = handleEventRoute(method, path, p, body)
                path == "/events" || path.startsWith("/events/") -> {
                    val parts = path.split("/").drop(1)
                    if (parts.size >= 2) {
                        val cat = parts[1]
                        if (parts.size >= 3) {
                            val sub = parts[2]
                            responseBody = jsonOk(mapOf("events" to when (sub) {
                                "today" -> todaysEventsRaw(cat)
                                else -> readEventsRaw(cat)
                            }))
                        } else {
                            responseBody = jsonOk(mapOf("events" to readEventsAllRaw(cat)))
                        }
                    } else {
                        responseBody = handleEventsList()
                    }
                }
                path.startsWith("/daily") -> responseBody = handleDaily(method, path, p)
                path.startsWith("/daytracker") || path.startsWith("/day-tracker") -> responseBody = handleDayTrackerRoute(method, path, p, body)
                path.startsWith("/day") -> responseBody = handleDayRoute(path)
                path.startsWith("/streak") -> responseBody = handleStreak()
                path.startsWith("/week") -> responseBody = handleWeek(p)
                path.startsWith("/report") -> responseBody = handleReport(p)
                path.startsWith("/task") || path.startsWith("/tasks") -> responseBody = handleTaskRoute(method, path, p)
                path.startsWith("/session") -> responseBody = handleSessionRoute(method, path, body)
                path.startsWith("/gita") -> responseBody = handleGitaRoute(method, path, p, body)
                path.startsWith("/log") -> responseBody = handleLogRoute(method, path, p)
                path.startsWith("/running/stats") -> responseBody = handleRunningStats(p)
                path.startsWith("/edit") -> responseBody = handleEdit(p)
                path.startsWith("/thought") -> responseBody = handleThoughtRoute(method, path, p)
                path.startsWith("/recycle-bin") -> responseBody = handleRecycleRoute(method, path, p)
                path.startsWith("/meta") -> responseBody = handleMetaRoute(path)
                path == "/mood/prompt" -> responseBody = handleMoodPrompt()
                path.startsWith("/notify-prefs") -> responseBody = handleNotifyPrefsRoute(method, path, p, body)
                path.startsWith("/checklist") -> responseBody = handleChecklistRoute(method, path, p, body)
                path == "/notify/test" && method == "POST" -> { V07ServerService.sendTestNotificationFromServer(); responseBody = jsonOk(mapOf("ok" to true)) }
                path.startsWith("/notify") -> responseBody = handleNotifyRoute(path)
                path.startsWith("/vault/init") && method == "POST" -> responseBody = handleVaultInit()
                path.startsWith("/vault/path") -> responseBody = handleVaultPath(method)
                path == "/network/info" -> responseBody = handleNetworkInfo()
                path == "/sync/trigger" && method == "POST" -> {
                    // Trigger Firestore sync from background
                    try {
                        val activity = appContext as? MainActivity
                        activity?.syncAllToFirestore()
                        responseBody = toJson(mapOf("ok" to true, "message" to "Sync triggered"))
                    } catch (e: Exception) {
                        responseBody = toJson(mapOf("ok" to false, "error" to (e.message ?: "sync failed")))
                    }
                }
                path == "/overlay/status" && method == "GET" -> {
                    val ctx = appContext
                    val canOverlay = ctx != null && Settings.canDrawOverlays(ctx)
                    val enabled = NotificationPreferences.isOverlayEnabled()
                    responseBody = toJson(mapOf("enabled" to enabled, "can_draw_overlays" to canOverlay))
                }
                path == "/overlay/toggle" && method == "POST" -> {
                    val ctx = appContext
                    if (ctx == null) {
                        responseBody = jsonOk(mapOf("ok" to false, "error" to "no context"))
                    } else {
                        val current = NotificationPreferences.isOverlayEnabled()
                        val newState = !current
                        NotificationPreferences.setOverlayEnabled(newState)
                        if (newState && Settings.canDrawOverlays(ctx)) {
                            OverlayService.start(ctx)
                        } else if (!newState) {
                            OverlayService.stop(ctx)
                        }
                        responseBody = jsonOk(mapOf("ok" to true, "enabled" to newState, "can_draw_overlays" to Settings.canDrawOverlays(ctx)))
                    }
                }
                path == "/meta/theme" && method == "POST" -> {
                    val ctx = appContext
                    if (ctx != null && body.isNotEmpty()) {
                        try {
                            val json = org.json.JSONObject(body)
                            val theme = json.optString("theme", "dark")
                            ctx.getSharedPreferences("v07_prefs", android.content.Context.MODE_PRIVATE).edit().putString("theme", theme).apply()
                            responseBody = jsonOk(mapOf("ok" to true, "theme" to theme))
                        } catch (e: Exception) {
                            responseBody = jsonOk(mapOf("ok" to false, "error" to e.message))
                        }
                    } else {
                        responseBody = jsonOk(mapOf("ok" to false, "error" to "no context"))
                    }
                }
                path == "/meta/accessibility" && method == "GET" -> {
                    responseBody = jsonOk(mapOf("connected" to com.v07.dashboard.ForegroundAppTracker.isConnected))
                }
                path == "/meta/open-accessibility-settings" && method == "POST" -> {
                    val ctx = appContext
                    if (ctx != null) {
                        try {
                            ctx.startActivity(android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                            responseBody = jsonOk(mapOf("ok" to true))
                        } catch (e: Exception) {
                            responseBody = jsonOk(mapOf("ok" to false, "error" to e.message))
                        }
                    } else {
                        responseBody = jsonOk(mapOf("ok" to false, "error" to "no context"))
                    }
                }
                path == "/app-limits" && method == "GET" -> {
                    val ctx = appContext
                    if (ctx != null) {
                        val prefs = ctx.getSharedPreferences("v07_prefs", android.content.Context.MODE_PRIVATE)
                        val json = prefs.getString("app_limits", "{}") ?: "{}"
                        responseBody = jsonOk(mapOf("limits" to json))
                    } else {
                        responseBody = jsonOk(mapOf("error" to "no context"))
                    }
                }
                path == "/app-limits" && method == "POST" -> {
                    val ctx = appContext
                    if (ctx != null && body.isNotEmpty()) {
                        try {
                            val jsonBody = org.json.JSONObject(body)
                            val limits = jsonBody.optJSONObject("limits")
                            if (limits != null) {
                                ctx.getSharedPreferences("v07_prefs", android.content.Context.MODE_PRIVATE).edit().putString("app_limits", limits.toString()).apply()
                                responseBody = jsonOk(mapOf("ok" to true))
                            } else {
                                responseBody = jsonOk(mapOf("ok" to false, "error" to "missing limits"))
                            }
                        } catch (e: Exception) {
                            responseBody = jsonOk(mapOf("ok" to false, "error" to e.message))
                        }
                    } else {
                        responseBody = jsonOk(mapOf("ok" to false, "error" to "no context"))
                    }
                }
                path.startsWith("/qr") -> { val qr = handleQR(p); status = qr.first; responseBody = qr.second; contentType = qr.third }
                path.startsWith("/database") -> responseBody = handleDatabase(p)
                path == "/sleep-up" -> responseBody = handleSleepUp()
                path.startsWith("/client/log") -> responseBody = handleClientLog(body)
                path.startsWith("/tags") -> responseBody = handleTagsRoute(method, path, p, body)
                path.startsWith("/shares") -> responseBody = handleSharesRoute(method, path, p)
                path.startsWith("/api/weekly-summary") -> responseBody = handleWeeklySummaryRoute(method, path, p, headers, body)
                path.startsWith("/ai") -> responseBody = handleAIRoute(method, path, headers, body)
                path.startsWith("/plugins") -> {
                    handlePluginRoute(method, path, p, body, output)
                    return
                }
                path.startsWith("/vault-media/") -> {
                    val fileName = path.removePrefix("/vault-media/")
                    val mediaDir = VaultConfig.mediaDir.canonicalFile
                    val file = java.io.File(mediaDir, fileName).canonicalFile
                    if (file.toPath().startsWith(mediaDir.toPath()) && file.exists() && file.canRead()) {
                        try {
                        val mime = when {
                            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
                            fileName.endsWith(".png", true) -> "image/png"
                            fileName.endsWith(".gif", true) -> "image/gif"
                            fileName.endsWith(".webp", true) -> "image/webp"
                            fileName.endsWith(".mp4", true) -> "video/mp4"
                            fileName.endsWith(".webm", true) -> "video/webm"
                            fileName.endsWith(".mkv", true) -> "video/x-matroska"
                            fileName.endsWith(".avi", true) -> "video/x-msvideo"
                            fileName.endsWith(".mov", true) -> "video/quicktime"
                            fileName.endsWith(".mp3", true) -> "audio/mpeg"
                            fileName.endsWith(".ogg", true) || fileName.endsWith(".opus", true) -> "audio/ogg"
                            fileName.endsWith(".wav", true) -> "audio/wav"
                            fileName.endsWith(".flac", true) -> "audio/flac"
                            fileName.endsWith(".aac", true) || fileName.endsWith(".m4a", true) -> "audio/aac"
                            fileName.endsWith(".pdf", true) -> "application/pdf"
                            else -> "application/octet-stream"
                        }
                        val fileSize = file.length()
                        val rangeHeader = headers["range"]
                        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                            val rangeParts = rangeHeader.removePrefix("bytes=").split("-")
                            val start = rangeParts[0].toLongOrNull() ?: 0L
                            val end = if (rangeParts.size > 1 && rangeParts[1].isNotEmpty()) rangeParts[1].toLongOrNull() ?: (fileSize - 1) else (fileSize - 1)
                            val len = end - start + 1
                            output.write("HTTP/1.1 206 Partial Content\r\nContent-Type: $mime\r\nContent-Length: $len\r\nContent-Range: bytes $start-$end/$fileSize\r\nAccept-Ranges: bytes\r\nConnection: close\r\n\r\n".toByteArray())
                            file.inputStream().use { fIn: java.io.InputStream ->
                                fIn.skip(start)
                                val buf = ByteArray(65536)
                                var remaining = len
                                while (remaining > 0) {
                                    val toRead = minOf(buf.size.toLong(), remaining).toInt()
                                    val didRead = fIn.read(buf, 0, toRead)
                                    if (didRead <= 0) break
                                    output.write(buf, 0, didRead)
                                    remaining -= didRead.toLong()
                                }
                            }
                        } else {
                            output.write("HTTP/1.1 200 OK\r\nContent-Type: $mime\r\nContent-Length: $fileSize\r\nAccept-Ranges: bytes\r\nConnection: close\r\n\r\n".toByteArray())
                            file.inputStream().use { fIn: java.io.InputStream ->
                                val buf = ByteArray(65536)
                                var readLen = fIn.read(buf)
                                while (readLen != -1) {
                                    output.write(buf, 0, readLen)
                                    readLen = fIn.read(buf)
                                }
                            }
                        }
                        output.flush()
                        return
                        } catch (e: java.io.IOException) {
                            android.util.Log.w("V07Server", "vault-media stream interrupted: ${e.message}")
                            return
                        }
                    } else {
                        status = 404; responseBody = """{"error":"file not found"}"""
                    }
                }
                else -> {
                    val ctx = appContext
                    if (ctx != null && !path.startsWith("/api")) {
                        try {
                            val assetPath = path.removePrefix("/")
                            val stream = try {
                                ctx.assets.open("v3/$assetPath")
                            } catch (_: Exception) {
                                ctx.assets.open(assetPath)
                            }
                            stream.use { s ->
                                val mime = when {
                                    path.endsWith(".png", true) -> "image/png"
                                    path.endsWith(".jpg", true) || path.endsWith(".jpeg", true) -> "image/jpeg"
                                    path.endsWith(".gif", true) -> "image/gif"
                                    path.endsWith(".webp", true) -> "image/webp"
                                    path.endsWith(".svg", true) -> "image/svg+xml"
                                    path.endsWith(".html", true) -> "text/html"
                                    path.endsWith(".css", true) -> "text/css"
                                    path.endsWith(".js", true) -> "application/javascript"
                                    path.endsWith(".json", true) -> "application/json"
                                    path.endsWith(".woff2", true) -> "font/woff2"
                                    path.endsWith(".woff", true) -> "font/woff"
                                    path.endsWith(".ttf", true) -> "font/ttf"
                                    else -> "application/octet-stream"
                                }
                                contentType = mime
                                val bytes = s.readBytes()
                                output.write("HTTP/1.1 200 OK\r\nContent-Type: $contentType\r\nContent-Length: ${bytes.size}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n".toByteArray())
                                output.write(bytes)
                                output.flush()
                                return
                            }
                        } catch (_: Exception) {
                            status = 404; responseBody = """{"error":"not found"}"""
                        }
                    } else {
                        status = 404; responseBody = """{"error":"not found"}"""
                    }
                }
            }
            } catch (e: Exception) {
                status = 500
                android.util.Log.e("V07Server", "Error handling request: ${e.message}", e)
                responseBody = """{"error":"${e.message?.replace("\"", "\\\"")?.replace("\n", "\\n") ?: "unknown"}","status":500}"""
            }

        respond(output, status, responseBody, contentType)
    } catch (e: Exception) {
        android.util.Log.e("V07Server", "handleClient error: ${e.message}", e)
    } finally {
        try { input.close() } catch (_: Exception) {}
        try { output.close() } catch (_: Exception) {}
    }
}

private fun parseQuery(query: String?): Map<String, String> {
    val map = mutableMapOf<String, String>()
    if (query == null) return map
    for (pair in query.split("&")) {
        val eq = pair.indexOf('=')
        if (eq > 0) {
            map[URLDecoder.decode(pair.substring(0, eq), "UTF-8")] = URLDecoder.decode(pair.substring(eq + 1), "UTF-8")
        }
    }
    return map
}

private fun respond(out: OutputStream, status: Int, body: String, contentType: String) {
    val bytes = body.toByteArray(Charsets.UTF_8)
    val writer = OutputStreamWriter(out, Charsets.UTF_8)
    val reason = when (status) { 200 -> "OK"; 400 -> "Bad Request"; 404 -> "Not Found"; 405 -> "Method Not Allowed"; 500 -> "Internal Server Error"; else -> "Unknown" }
    writer.write("HTTP/1.1 $status $reason\r\n")
    writer.write("Content-Type: $contentType; charset=utf-8\r\n")
    writer.write("Content-Length: ${bytes.size}\r\n")
    writer.write("Access-Control-Allow-Origin: *\r\n")
    writer.write("Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS\r\n")
    writer.write("Access-Control-Allow-Headers: Content-Type\r\n")
    writer.write("Connection: close\r\n")
    writer.write("Cache-Control: no-store, no-cache, must-revalidate\r\n")
    writer.write("\r\n")
    writer.flush()
    out.write(bytes)
    out.flush()
}

private fun jsonOk(data: Map<String, Any?>): String = toJson(data)
private fun jsonOkBoolean(ok: Boolean): String = toJson(mapOf("ok" to ok))

// === App Usage Route ===
private fun handleAppUsage(method: String, path: String, params: Map<String, String>, body: String): String {
    return when (method) {
        "GET" -> {
            val d = params["date"] ?: LocalDate.now().toString()
            val force = params["force"] == "true"
            if (d == LocalDate.now().toString()) {
                if (force) {
                    try { V07ServerService.collectNow() } catch (e: Exception) {
                        android.util.Log.e("V07Server", "collectNow failed", e)
                    }
                } else {
                    V07ServerService.refreshUsageStats(false)
                }
            }
            val f = appUsageFile(d)
            if (!f.exists()) jsonOk(mapOf("date" to d, "exists" to false))
            else {
                try {
                    val raw = jsonToMap(f.readText())
                    raw["exists"] = true
                    if (raw["date"] == null) raw["date"] = d
                    toJson(raw)
                } catch (ex: Exception) {
                    jsonOk(mapOf("date" to d, "exists" to false))
                }
            }
        }
        "POST" -> {
            val jbody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val d = jbody["date"] as? String ?: LocalDate.now().toString()
            appUsageFile(d).writeText(body)
            jsonOk(mapOf("ok" to true, "date" to d))
        }
        else -> jsonOk(mapOf("error" to "method not allowed"))
    }
}

private fun appUsageDuration(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return if (m >= 60) "${m / 60}h ${m % 60}m"
    else if (m > 0) "${m}m ${s}s"
    else "${s}s"
}

// === Event Routes ===
private fun handleEventRoute(method: String, path: String, params: Map<String, String>, body: String): String {
    val parts = path.split("/").drop(1)
    return when (method) {
        "POST" -> {
            val cat = parts.getOrNull(1) ?: return jsonOk(mapOf("ok" to false, "error" to "bad path"))
            val eventId = parts.getOrNull(2)
            // Edit event in-place
            if (eventId != null && parts.size >= 4 && parts.last() == "edit") {
                val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
                if (cat == "sessions") {
                    val f = catFile(cat) ?: return jsonOk(mapOf("ok" to false, "error" to "no file"))
                    if (!f.exists()) return jsonOk(mapOf("ok" to false, "error" to "not found"))
                    val lines = f.readLines().toMutableList()
                    val idx = lines.indexOfFirst { line ->
                        Regex("\"id\"\\s*:\\s*\"${Regex.escape(eventId)}\"").containsMatchIn(line)
                    }
                    if (idx < 0) return jsonOk(mapOf("ok" to false, "error" to "event not found"))
                    val ev = jsonToMap(lines[idx]).toMutableMap()
                    val newActivity = jsonBody["activity"] as? String
                    val newLabel = jsonBody["custom_label"] as? String
                    val newDuration = (jsonBody["duration_minutes"] as? Number)?.toInt()
                    if (newActivity != null) ev["activity"] = newActivity
                    if (newLabel != null) ev["custom_label"] = newLabel
                    if (newDuration != null) {
                        ev["duration"] = newDuration
                        ev["duration_minutes"] = newDuration
                    }
                    // Update the paired start event's activity/label too
                    if (newActivity != null || newLabel != null) {
                        val eDate = ev["date"] as? String ?: ""
                        val eTs = ev["ts"] as? String ?: ""
                        val startIdx = lines.indexOfFirst { line ->
                            try {
                                val item = jsonToMap(line)
                                item["type"] == "start" && item["date"] == eDate &&
                                    (item["activity"] == ev["activity"] || item["activity"] == jsonBody["activity"])
                            } catch (_: Exception) { false }
                        }
                        if (startIdx >= 0) {
                            val startEv = jsonToMap(lines[startIdx]).toMutableMap()
                            if (newActivity != null) startEv["activity"] = newActivity
                            if (newLabel != null) startEv["custom_label"] = newLabel
                            lines[startIdx] = toJson(startEv)
                        }
                    }
                    lines[idx] = toJson(ev)
                    f.writeText(lines.joinToString("\n") + "\n")
                    // Rebuild daily stats
                    val eDate = ev["date"] as? String ?: java.time.LocalDate.now().toString()
                    val allSessions = f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
                        try { jsonToMap(line) } catch (_: Exception) { null }
                    }
                    val dateSessions = allSessions.filter { it["date"] == eDate && it["type"] == "end" }
                    val activityMap = mutableMapOf<String, Int>()
                    for (s in dateSessions) {
                        val a = (s["activity"] as? String) ?: continue
                        val d = (s["duration"] as? Number)?.toInt() ?: 0
                        activityMap[a] = (activityMap[a] ?: 0) + d
                    }
                    SessionStorage.rebuildDailyStatsForDate(eDate, activityMap)
                    return jsonOk(mapOf("ok" to true))
                } else {
                    updateEventField(cat, eventId, jsonBody)
                    return jsonOk(mapOf("ok" to true))
                }
            }
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val data = jsonBody["data"] as? Map<String, Any?> ?: jsonBody
            val ev = appendEvent(cat, data)
            jsonOk(mapOf("ok" to true, "event" to ev))
        }
        "DELETE" -> {
            if (parts.size < 3) return jsonOk(mapOf("ok" to false))
            val cat = parts[1]
            val eventId = parts[2]
            // Read event data before deleting, for stats recalculation
            if (cat == "sessions") {
                val lines = catFile(cat)?.let { f ->
                    if (f.exists()) f.readLines().toMutableList() else null
                }
                if (lines != null) {
                    val idx = lines.indexOfFirst { line ->
                        Regex("\"id\"\\s*:\\s*\"${Regex.escape(eventId)}\"").containsMatchIn(line)
                    }
                    if (idx >= 0) {
                        try {
                            val raw = lines[idx]
                            val ev = jsonToMap(raw)
                            val eType = ev["type"] as? String ?: ""
                            val eActivity = ev["activity"] as? String ?: ""
                            val eDuration = (ev["duration"] as? Number)?.toInt() ?: 0
                            val eDate = ev["date"] as? String ?: java.time.LocalDate.now().toString()
                            if (eType == "end" && eActivity.isNotEmpty() && eDuration > 0) {
                                SessionStorage.removeMinutesFromDailyStats(eActivity, eDuration, eDate)
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
            val found = softDeleteEvent(cat, eventId)
            jsonOk(mapOf("ok" to found, "recycled" to found))
        }
        "GET" -> {
            val cat = parts.getOrNull(1) ?: return jsonOk(mapOf("error" to "missing category"))
            val sub = parts.getOrNull(2)
            val events = when (sub) {
                "today" -> todaysEventsRaw(cat)
                "range" -> {
                    val start = params["start"] ?: ""
                    val end = params["end"] ?: ""
                    eventsInRangeRaw(cat, start, end)
                }
                else -> readEventsRaw(cat)
            }
            jsonOk(mapOf("events" to events))
        }
        else -> jsonOk(mapOf("ok" to false, "error" to "method not allowed"))
    }
}

private fun handleEventsList(): String {
    val cats = listOf("mood", "metrics", "habits", "journal", "dsa", "chess", "running", "panics", "thought", "sessions", "shares")
    return toJson(cats.associateWith { readEvents(it) })
}

private fun handleDaily(method: String, path: String, params: Map<String, String>): String {
    val date = path.split("/").drop(2).firstOrNull() ?: LocalDate.now().toString()
    return jsonOk(mapOf("summary" to formatDayStats(date)))
}

private fun handleDayRoute(path: String): String {
    val date = path.split("/").drop(2).firstOrNull() ?: LocalDate.now().toString()
    return toJson(getDayEvents(date))
}

private fun handleStreak(): String {
    val all = readEventsAll("habits")
    val overall = computeStreak(all) { it.on == true }
    val distinctActs = all.mapNotNull { it.activity }.distinct()
    val perAct = distinctActs.filter { a -> computeStreak(all) { it.activity == a && it.on == true } > 0 }
        .associateWith { activity -> computeStreak(all) { it.activity == activity && it.on == true } }
    return jsonOk(mapOf("overall_streak" to overall, "per_activity" to perAct))
}

private fun handleWeek(params: Map<String, String>): String {
    val base = try { LocalDate.parse(params["target_date"] ?: LocalDate.now().toString()) } catch (_: Exception) { LocalDate.now() }
    val sunday = base.minusDays((base.dayOfWeek.value % 7).toLong())
    val weekDays = (0..6).map { sunday.plusDays(it.toLong()) }
    val allHabits = readEventsAll("habits")
    val result = weekDays.associate { d ->
        val ds = d.toString()
        val acts = allHabits.filter { it.date == ds && it.on == true }.mapNotNull { it.activity }.toSet()
        ds to mapOf("day" to d.dayOfWeek.name, "active_activities" to acts.toList(), "activity_count" to acts.size)
    }
    return jsonOk(mapOf("week" to result, "start" to weekDays.first().toString(), "end" to weekDays.last().toString()))
}

private fun handleMoodPrompt(): String {
    if (!NotificationPreferences.isMoodReminderEnabled()) return jsonOk(mapOf("prompt" to false, "reason" to "disabled"))
    val lastMood = readEventsAll("mood").lastOrNull()
    val shouldPrompt = if (lastMood == null) true else {
        try {
            val lastTs = OffsetDateTime.parse(lastMood.ts)
            Duration.between(lastTs, OffsetDateTime.now()).toHours() >= 8
        } catch (_: Exception) { false }
    }
    return jsonOk(mapOf("prompt" to shouldPrompt))
}

private fun handleReport(params: Map<String, String>): String {
    val period = params["period"] ?: "week"
    val today = LocalDate.now()
    val start = if (period == "week") today.minusDays((today.dayOfWeek.value % 7).toLong()) else today.withDayOfMonth(1)
    val s = start.toString(); val e = today.toString()
    val moods = eventsInRange("mood", s, e)
    val metrics = eventsInRange("metrics", s, e)
    val habits = eventsInRange("habits", s, e)
    val sessions = eventsInRange("sessions", s, e)
    val totalDays = (today.toEpochDay() - start.toEpochDay()).toInt() + 1
    return jsonOk(mapOf(
        "period" to period, "start" to s, "end" to e, "total_days" to totalDays,
        "active_days" to habits.filter { it.on == true }.map { it.date }.distinct().size,
        "mood_count" to moods.size, "sessions_count" to sessions.size,
        "total_pushups" to metrics.sumOf { it.pushups ?: 0 },
        "total_doom" to metrics.sumOf { it.doom_scroll ?: 0 },
        "checkins" to metrics.count { it.type == "checkin" },
        "checkouts" to metrics.count { it.type == "checkout" }
    ))
}

// === Task Routes ===
private fun handleTaskRoute(method: String, path: String, params: Map<String, String>): String {
    val parts = path.split("/").drop(1)
    return when (method) {
        "GET" -> {
            val filter = params["filter"] ?: "all"
            val today = LocalDate.now().toString()
            val tasks = rebuildTaskState()
            val filtered = when (filter) {
                "overdue" -> tasks.filter { it.status != "done" && (it.deadline ?: "9999") < today }
                "today" -> tasks.filter { it.status != "done" && it.deadline == today }
                "upcoming" -> tasks.filter { it.status != "done" && (it.deadline ?: "0000") >= today }
                "done" -> tasks.filter { it.status == "done" }
                else -> tasks
            }
            jsonOk(mapOf("tasks" to filtered))
        }
        "POST" -> {
            when {
                parts.size >= 2 && parts.last() == "done" -> {
                    appendEvent("tasks", mapOf("action" to "completed", "task_id" to parts[1]))
                    jsonOk(mapOf("ok" to true))
                }
                parts.size >= 2 && parts.last() == "reopen" -> {
                    appendEvent("tasks", mapOf("action" to "reopened", "task_id" to parts[1]))
                    jsonOk(mapOf("ok" to true))
                }
                parts.size >= 2 && parts.last() == "pin" -> {
                    val pinVal = params["pinned"]?.toBoolean() ?: true
                    appendEvent("tasks", mapOf("action" to "pinned", "task_id" to parts[1], "pinned" to pinVal))
                    jsonOk(mapOf("ok" to true))
                }
                else -> {
                    val title = params["title"] ?: return jsonOk(mapOf("ok" to false, "detail" to "missing title"))
                    val deadline = params["deadline"] ?: return jsonOk(mapOf("ok" to false, "detail" to "missing deadline"))
                    val time = params["time"] ?: ""
                    val priority = params["priority"] ?: "medium"
                    val ev = appendEvent("tasks", mapOf("action" to "created", "title" to title, "deadline" to deadline, "time" to time, "priority" to priority))
                    jsonOk(mapOf("ok" to true, "task" to ev))
                }
            }
        }
        "DELETE" -> {
            val id = parts.getOrNull(1) ?: return jsonOk(mapOf("ok" to false))
            val tasks = rebuildTaskState()
            val task = tasks.find { it.id == id } ?: return jsonOk(mapOf("ok" to false, "detail" to "task not found"))
            appendEvent("tasks", mapOf("action" to "deleted", "task_id" to id))
            softDeleteTask(id, task)
            jsonOk(mapOf("ok" to true, "recycled" to true))
        }
        else -> jsonOk(mapOf("error" to "method not allowed"))
    }
}

// === Session Routes ===
private fun handleSessionRoute(method: String, path: String, body: String): String {
    val parts = path.split("/").drop(1)
    return when {
        method == "GET" && parts.lastOrNull() == "today" -> {
            jsonOk(SessionManager.toTodayMap())
        }
        method == "GET" && parts.lastOrNull() == "active" -> {
            jsonOk(SessionManager.toApiMap())
        }
        method == "POST" && parts.lastOrNull() == "start" -> {
            try {
                val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
                val activity = jsonBody["activity"] as? String ?: return jsonOk(mapOf("ok" to false, "detail" to "missing activity"))
                val custom = jsonBody["custom_label"] as? String
                val timerMode = jsonBody["timer_mode"] as? String ?: "countdown"
                val timerSeconds = (jsonBody["timer_seconds"] as? Number)?.toInt() ?: 0
                val session = SessionManager.start(activity, timerMode, timerSeconds / 60, custom)
                jsonOk(mapOf("ok" to true, "session" to mapOf(
                    "id" to session.id, "activity" to session.activity,
                    "started_at" to session.startedAt
                )))
            } catch (e: IllegalStateException) {
                jsonOk(mapOf("ok" to false, "detail" to e.message))
            } catch (e: Exception) {
                jsonOk(mapOf("ok" to false, "detail" to e.message))
            }
        }
        method == "POST" && parts.lastOrNull() == "stop" -> {
            val completed = SessionManager.stop() ?: return jsonOk(mapOf("ok" to false, "detail" to "No active session"))
            V07ServerService.dismissSessionNotification()
            jsonOk(mapOf("ok" to true, "duration_minutes" to (completed.workedMs / 60000).toInt()))
        }
        method == "POST" && parts.lastOrNull() == "pause" -> {
            SessionManager.pause()
            jsonOk(mapOf("ok" to true))
        }
        method == "POST" && parts.lastOrNull() == "resume" -> {
            SessionManager.resume()
            jsonOk(mapOf("ok" to true))
        }
        method == "GET" && parts.lastOrNull() == "recent-activities" -> {
            val recent = SessionStorage.getRecentCustomActivities()
            jsonOk(mapOf("ok" to true, "activities" to recent))
        }
        method == "GET" && parts.lastOrNull() == "daily-target" -> {
            jsonOk(mapOf("ok" to true, "daily_target" to SessionManager.getDailyTarget()))
        }
        method == "POST" && parts.lastOrNull() == "daily-target" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val minutes = (jsonBody["minutes"] as? Number)?.toInt()
                ?: return jsonOk(mapOf("ok" to false, "detail" to "missing minutes"))
            SessionManager.setDailyTarget(minutes)
            jsonOk(mapOf("ok" to true))
        }
        method == "POST" && parts.lastOrNull() == "delete-activity" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val activity = jsonBody["activity"] as? String
                ?: return jsonOk(mapOf("ok" to false, "detail" to "missing activity"))
            SessionStorage.deleteRecentActivity(activity)
            jsonOk(mapOf("ok" to true))
        }
        method == "POST" && parts.lastOrNull() == "add-custom-activity" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val activity = jsonBody["activity"] as? String
                ?: return jsonOk(mapOf("ok" to false, "detail" to "missing activity"))
            SessionStorage.addCustomActivity(activity)
            jsonOk(mapOf("ok" to true))
        }
        else -> jsonOk(mapOf("error" to "not found"))
    }
}

// === Checklist Routes ===
private fun checklistFile(): File = File(VaultConfig.eventsDir, "checklist.jsonl")
private fun checklistTombstonesFile(): File = File(VaultConfig.eventsDir, "checklist_tombstones.json")

private fun readChecklistTombstones(): MutableSet<String> {
    val f = checklistTombstonesFile()
    if (!f.exists()) return mutableSetOf()
    return try {
        val content = f.readText().trim()
        if (content.isBlank()) mutableSetOf()
        else {
            val arr = JSONArray(content)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val s = arr.optString(i).trim()
                if (s.isNotEmpty()) set.add(s)
            }
            set
        }
    } catch (_: Exception) { mutableSetOf() }
}

private fun addChecklistTombstone(label: String) {
    if (label.isBlank()) return
    val set = readChecklistTombstones()
    set.add(label.trim())
    try {
        val arr = JSONArray(set.toList())
        checklistTombstonesFile().writeText(arr.toString())
    } catch (_: Exception) {}
}

private fun removeChecklistTombstone(label: String) {
    if (label.isBlank()) return
    val set = readChecklistTombstones()
    if (set.remove(label.trim())) {
        try {
            val arr = JSONArray(set.toList())
            checklistTombstonesFile().writeText(arr.toString())
        } catch (_: Exception) {}
    }
}

private fun readChecklistItems(): List<Map<String, Any?>> {
    val f = checklistFile()
    if (!f.exists()) return emptyList()
    return f.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
        try {
            val m = jsonToMap(line).toMutableMap()
            if (!m.containsKey("frequency")) {
                m["frequency"] = if (m["recurring"] == true) "D" else "S"
            }
            m
        } catch (_: Exception) { null }
    }
}

fun seedChecklistToday(): Int {
    val today = LocalDate.now().toString()
    val sevenDaysAgo = LocalDate.now().minusDays(7).toString()
    val tombstones = readChecklistTombstones()
    val all = readChecklistItems()

    // Clean up expired single-day tasks (older than today) and tombstoned tasks
    val activeItems = all.filter { item ->
        val date = item["date"] as? String ?: ""
        val freq = item["frequency"] as? String ?: "S"
        val label = (item["label"] as? String ?: "").trim()
        if (label.isEmpty() || tombstones.contains(label)) return@filter false
        if (freq == "S" && date < today) return@filter false // Auto-purge expired one-off tasks
        if (freq == "W" && date < sevenDaysAgo && item["recurring"] != true) return@filter false // Expired weekly task
        true
    }

    if (activeItems.size != all.size) {
        withLock(catLock("checklist")) {
            val f = checklistFile()
            f.writeText(activeItems.joinToString("\n") { toJson(it) } + (if (activeItems.isNotEmpty()) "\n" else ""))
        }
    }

    val todayDailyLabels = activeItems.filter { it["date"] == today && it["frequency"] != "W" }
        .mapNotNull { it["label"] as? String }.toSet()

    // Strictly seed recurring DAILY items (do NOT seed weekly items into daily)
    val prevRecurringDaily = activeItems.filter { 
        val freq = it["frequency"] as? String ?: "D"
        val recurring = it["recurring"] == true || freq == "D"
        recurring && freq == "D"
    }
        .sortedByDescending { it["date"] as? String ?: "" }
        .groupBy { (it["label"] as? String ?: "").trim() }
        .mapValues { (_, v) -> v.first() }
        .values

    val seeded = prevRecurringDaily.filter { item ->
        val label = (item["label"] as? String ?: "").trim()
        label.isNotEmpty() && !todayDailyLabels.contains(label) && !tombstones.contains(label)
    }.map { item ->
        val newId = "cl_${Random.nextLong().toString(36).take(8)}"
        mapOf("id" to newId, "date" to today, "label" to item["label"], "done" to false, "recurring" to true, "frequency" to "D", "ts" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }

    if (seeded.isNotEmpty()) {
        withLock(catLock("checklist")) {
            val f = checklistFile()
            f.parentFile.mkdirs()
            for (item in seeded) {
                f.appendText(toJson(item) + "\n")
            }
        }
    }
    return seeded.size
}

private fun handleChecklistRoute(method: String, path: String, params: Map<String, String>, body: String): String {
    val parts = path.split("/").drop(1)
    return when {
        method == "GET" && parts.lastOrNull() == "today" -> {
            val today = LocalDate.now().toString()
            try { seedChecklistToday() } catch (_: Exception) {}
            val todayItems = readChecklistItems().filter { it["date"] == today && it["frequency"] != "W" }
            jsonOk(mapOf("items" to todayItems))
        }
        method == "POST" && parts.lastOrNull() == "today" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val label = jsonBody["label"] as? String ?: return jsonOk(mapOf("ok" to false, "detail" to "missing label"))
            val freq = jsonBody["frequency"] as? String ?: "S"
            val recurring = freq == "D" || freq == "W"
            val today = LocalDate.now().toString()
            val newId = "cl_${Random.nextLong().toString(36).take(8)}"
            val item = mapOf("id" to newId, "date" to today, "label" to label.trim(), "done" to false, "recurring" to recurring, "frequency" to freq, "ts" to OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            removeChecklistTombstone(label)
            withLock(catLock("checklist")) {
                val f = checklistFile()
                f.parentFile.mkdirs()
                f.appendText(toJson(item) + "\n")
            }
            jsonOk(mapOf("ok" to true, "item" to item))
        }
        method == "GET" && parts.lastOrNull() == "week" -> {
            val all = readChecklistItems()
            val sevenDaysAgo = LocalDate.now().minusDays(7).toString()
            val weekItems = all.filter {
                val freq = it["frequency"] as? String ?: if (it["recurring"] == true) "W" else "S"
                freq == "W" && ((it["date"] as? String ?: "") >= sevenDaysAgo)
            }
            jsonOk(mapOf("items" to weekItems))
        }
        method == "POST" && parts.lastOrNull() == "toggle" && parts.size >= 3 -> {
            val id = parts[parts.size - 2]
            val f = checklistFile()
            if (!f.exists()) return jsonOk(mapOf("ok" to false))
            var newDone = false
            withLock(catLock("checklist")) {
                val lines = f.readLines().toMutableList()
                var found = false
                for (i in lines.indices) {
                    if (lines[i].contains("\"$id\"")) {
                        val m = jsonToMap(lines[i])
                        m["done"] = m["done"] != true
                        newDone = m["done"] as Boolean
                        lines[i] = toJson(m)
                        found = true
                        break
                    }
                }
                if (found) f.writeText(lines.joinToString("\n") + "\n")
            }
            jsonOk(mapOf("ok" to true, "done" to newDone))
        }
        method == "POST" && parts.lastOrNull() == "edit" && parts.size >= 3 -> {
            val id = parts[parts.size - 2]
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val label = jsonBody["label"] as? String
            val recurring = jsonBody["recurring"] as? Boolean
            val f = checklistFile()
            if (!f.exists()) return jsonOk(mapOf("ok" to false))
            withLock(catLock("checklist")) {
                val lines = f.readLines().toMutableList()
                for (i in lines.indices) {
                    if (lines[i].contains("\"$id\"")) {
                        val m = jsonToMap(lines[i])
                        if (label != null) m["label"] = label
                        if (recurring != null) m["recurring"] = recurring
                        val freq = jsonBody["frequency"] as? String
                        if (freq != null) m["frequency"] = freq
                        lines[i] = toJson(m)
                        break
                    }
                }
                f.writeText(lines.joinToString("\n") + "\n")
            }
            jsonOk(mapOf("ok" to true))
        }
        method == "DELETE" && parts.size >= 2 -> {
            val id = parts.lastOrNull() ?: return jsonOk(mapOf("ok" to false))
            val f = checklistFile()
            if (!f.exists()) return jsonOk(mapOf("ok" to false))
            withLock(catLock("checklist")) {
                val allLines = f.readLines()
                var deletedLabel: String? = null
                for (line in allLines) {
                    if (line.contains("\"$id\"")) {
                        val m = runCatching { jsonToMap(line) }.getOrNull()
                        deletedLabel = m?.get("label") as? String
                        break
                    }
                }
                if (deletedLabel != null && deletedLabel.isNotBlank()) {
                    addChecklistTombstone(deletedLabel)
                    val lines = allLines.filter { !it.contains("\"$id\"") && !it.contains("\"$deletedLabel\"") }
                    f.writeText(lines.joinToString("\n") + (if (lines.isNotEmpty()) "\n" else ""))
                } else {
                    val lines = allLines.filter { !it.contains("\"$id\"") }
                    f.writeText(lines.joinToString("\n") + (if (lines.isNotEmpty()) "\n" else ""))
                }
            }
            jsonOk(mapOf("ok" to true))
        }
        else -> jsonOk(mapOf("error" to "not found"))
    }
}

// === Day Tracker Routes ===
private fun handleDayTrackerRoute(method: String, path: String, params: Map<String, String>, body: String): String {
    val f = VaultConfig.dayTrackerFile
    return when (method) {
        "GET" -> {
            if (!f.exists()) jsonOk(mapOf("entries" to emptyList<Any>(), "events" to emptyList<Any>()))
            else {
                try {
                    val lines = f.readLines().filter { it.isNotBlank() }
                    val entries = lines.mapNotNull { line ->
                        try { jsonToMap(line) } catch (_: Exception) { null }
                    }
                    val today = LocalDate.now()
                    val events = entries.filter {
                        val p = it["pinned"]
                        p == true || p == "true"
                    }.map { entry ->
                        val dateStr = entry["date"] as? String ?: ""
                        val d = runCatching { LocalDate.parse(dateStr) }.getOrNull()
                        val diffDays = if (d != null) java.time.temporal.ChronoUnit.DAYS.between(today, d) else 0L
                        val isPast = diffDays < 0
                        val diffStr = if (isPast) "${Math.abs(diffDays)}d" else "${diffDays}d"
                        entry.toMutableMap().apply {
                            this["diff_days"] = diffStr
                            this["past"] = isPast
                            this["days"] = diffDays
                        }
                    }.sortedBy { (it["days"] as? Long) ?: 0L }
                    jsonOk(mapOf("entries" to entries, "events" to events))
                } catch (_: Exception) { jsonOk(mapOf("entries" to emptyList<Any>(), "events" to emptyList<Any>())) }
            }
        }
        "POST" -> {
            val jbody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val entry = mutableMapOf<String, Any?>()
            entry["id"] = jbody["id"] ?: (System.currentTimeMillis().toString(36) + Math.random().toString().substring(2, 8))
            entry["date"] = jbody["date"] ?: LocalDate.now().toString()
            entry["title"] = jbody["title"] ?: ""
            entry["category"] = jbody["category"] ?: "others"
            entry["pinned"] = jbody["pinned"] ?: false
            if (jbody.containsKey("hour")) entry["hour"] = jbody["hour"]
            entry["ts"] = jbody["ts"] ?: OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            f.parentFile?.mkdirs()
            f.appendText(toJson(entry) + "\n")
            jsonOk(mapOf("ok" to true, "entry" to entry))
        }
        "PUT" -> {
            val jbody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val id = jbody["id"] as? String ?: return jsonOk(mapOf("ok" to false, "detail" to "missing id"))
            if (!f.exists()) return jsonOk(mapOf("ok" to false))
            val lines = f.readLines().filter { it.isNotBlank() }
            val updated = lines.map { line ->
                try {
                    val map = jsonToMap(line)
                    if (map["id"] == id) {
                        if (jbody.containsKey("title")) map["title"] = jbody["title"]
                        if (jbody.containsKey("date")) map["date"] = jbody["date"]
                        if (jbody.containsKey("hour")) map["hour"] = jbody["hour"]
                        if (jbody.containsKey("pinned")) map["pinned"] = jbody["pinned"]
                        if (jbody.containsKey("category")) map["category"] = jbody["category"]
                    }
                    toJson(map)
                } catch (_: Exception) { line }
            }
            f.writeText(updated.joinToString("\n") + (if (updated.isNotEmpty()) "\n" else ""))
            jsonOk(mapOf("ok" to true))
        }
        "DELETE" -> {
            val id = params["id"] ?: return jsonOk(mapOf("ok" to false, "detail" to "missing id"))
            if (!f.exists()) return jsonOk(mapOf("ok" to false))
            val lines = f.readLines().filter { it.isNotBlank() }
            val filtered = lines.filter { line ->
                try {
                    val map = jsonToMap(line)
                    map["id"] != id
                } catch (_: Exception) { true }
            }
            f.writeText(filtered.joinToString("\n") + (if (filtered.isNotEmpty()) "\n" else ""))
            jsonOk(mapOf("ok" to true))
        }
        else -> jsonOk(mapOf("error" to "method not allowed"))
    }
}

// === Gita Routes ===
private fun handleGitaRoute(method: String, path: String, params: Map<String, String>, body: String): String {
    val parts = path.split("/").drop(1)
    return when {
        method == "GET" && parts.lastOrNull() == "today" -> {
            val verse = getSequentialVerse()
            if (verse == null) jsonOk(mapOf("done" to true, "message" to "All verses read!"))
            else toJson(verse)
        }
        method == "GET" && parts.getOrNull(1) == "verse" && parts.size == 3 -> {
            val verse = getVerseById(parts[2])
            if (verse == null) jsonOk(mapOf("error" to "not found"))
            else toJson(verse)
        }
        method == "GET" && parts.lastOrNull() == "next" -> {
            val id = params["verse_id"] ?: return jsonOk(mapOf("error" to "missing verse_id"))
            val next = getNextVerse(id)
            if (next == null) jsonOk(mapOf("done" to true, "message" to "All remaining verses read!"))
            else toJson(next)
        }
        method == "GET" && parts.lastOrNull() == "prev" -> {
            val id = params["verse_id"] ?: return jsonOk(mapOf("error" to "missing verse_id"))
            val prev = getPrevVerse(id)
            if (prev == null) jsonOk(mapOf("done" to true, "message" to "No earlier unread verses."))
            else toJson(prev)
        }
        method == "POST" && parts.lastOrNull() == "read" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val vid = (jsonBody["verse_id"] as? String) ?: return jsonOk(mapOf("ok" to false, "detail" to "missing verse_id"))
            if (isVerseRead(vid)) jsonOk(mapOf("ok" to true, "already_read" to true))
            else {
                markVerseRead(vid)
                val parts2 = vid.split(".")
                appendEvent("gita", mapOf("verse_id" to vid, "chapter" to (parts2.getOrNull(0) ?: ""), "verse" to (parts2.getOrNull(1) ?: "")))
                jsonOk(mapOf("ok" to true))
            }
        }
        method == "POST" && parts.lastOrNull() == "unread" -> {
            val vid = params["verse_id"] ?: return jsonOk(mapOf("ok" to false))
            unmarkVerseRead(vid); jsonOk(mapOf("ok" to true))
        }
        method == "POST" && parts.lastOrNull() == "review" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val vid = (jsonBody["verse_id"] as? String) ?: (jsonBody["verse_id"] as? Number)?.toString() ?: return jsonOk(mapOf("ok" to false))
            val text = jsonBody["text"] as? String ?: ""
            val rating = (jsonBody["rating"] as? Number)?.toInt() ?: 3
            val type = jsonBody["type"] as? String ?: "reflection"
            addVerseReview(vid, rating, text, type)
            if (text.isNotBlank()) {
                appendEvent("thought", mapOf("topic" to "Geeta", "text" to text, "verse_id" to vid))
            }
            jsonOk(mapOf("ok" to true))
        }
        method == "GET" && parts.lastOrNull() == "thoughts" -> {
            val vid = params["verse_id"] ?: return jsonOk(mapOf("thoughts" to emptyList<Any>()))
            val allThoughts = readEventsAll("thought")
            val verseThoughts = allThoughts.filter { it.verse_id == vid }.map { mapOf<String, Any?>("id" to it.id, "ts" to it.ts, "text" to it.text, "verse_id" to it.verse_id) }
            jsonOk(mapOf("thoughts" to verseThoughts))
        }
        method == "GET" && parts.lastOrNull() == "stats" -> toJson(getWeeklyVerseStats())
        method == "GET" && parts.lastOrNull() == "all" -> toJson(getAllVerses())
        method == "GET" && parts.lastOrNull() == "history" -> jsonOk(mapOf("read_entries" to getReadHistory(), "reviews" to getVerseReviews()))
        else -> jsonOk(mapOf("error" to "not found"))
    }
}

// === Log Routes ===
private fun handleLogRoute(method: String, path: String, params: Map<String, String>): String {
    val cat = path.split("/").drop(2).firstOrNull() ?: return jsonOk(mapOf("error" to "bad path"))
    return when (cat) {
        "mood" -> {
            val moodVal = params["mood"] ?: ""
            val data = mutableMapOf<String, Any>("mood" to moodVal)
            val score = MOOD_SCORES[moodVal]
            if (score != null) data["mood_score"] = score
            val note = params["note"]
            if (note != null && note.isNotEmpty()) data["note"] = note
            appendEvent("mood", data)
            jsonOk(mapOf("ok" to true))
        }
        "energy" -> {
            val energyVal = params["energy"] ?: ""
            val data = mutableMapOf<String, Any>("energy" to energyVal)
            val score = ENERGY_SCORES[energyVal]
            if (score != null) data["energy_score"] = score
            val note = params["note"]
            if (note != null && note.isNotEmpty()) data["note"] = note
            appendEvent("energy", data)
            jsonOk(mapOf("ok" to true))
        }
        "checkin" -> {
            val note = params["note"] ?: ""
            val retroTs = params["ts"] ?: ""
            val retroDate = params["date"] ?: ""
            val timeStr = when {
                retroTs.isNotEmpty() && retroTs.contains("T") -> retroTs.substringAfter("T").take(5)
                retroTs.isNotEmpty() && retroTs.length >= 5 -> retroTs.take(5)
                else -> OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            val data = mutableMapOf<String, Any>("type" to "checkin", "time" to timeStr)
            if (note.isNotEmpty()) data["note"] = note
            if (retroTs.isNotEmpty()) {
                val derivedDate = if (retroDate.isNotEmpty()) retroDate else if (retroTs.contains("T")) retroTs.substringBefore("T") else null
                appendEventWithTs("metrics", data, customTs = retroTs, customDate = derivedDate)
            } else {
                appendEvent("metrics", data)
            }
            jsonOk(mapOf("ok" to true))
        }
        "checkout" -> {
            val note = params["note"] ?: ""
            val retroTime = params["time"] ?: ""
            val retroTs = params["ts"] ?: ""
            val retroDate = params["date"] ?: ""
            val timeStr = when {
                retroTs.isNotEmpty() && retroTs.contains("T") -> retroTs.substringAfter("T").take(5)
                retroTs.isNotEmpty() && retroTs.length >= 5 -> retroTs.take(5)
                retroTime.isNotEmpty() -> retroTime.take(5)
                else -> OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            val data = mutableMapOf<String, Any>("type" to "checkout", "time" to timeStr)
            if (note.isNotEmpty()) data["note"] = note
            if (retroTs.isNotEmpty()) {
                val derivedDate = if (retroDate.isNotEmpty()) retroDate else if (retroTs.contains("T")) retroTs.substringBefore("T") else null
                appendEventWithTs("metrics", data, customTs = retroTs, customDate = derivedDate)
            } else {
                appendEvent("metrics", data)
            }
            jsonOk(mapOf("ok" to true))
        }
        "doom" -> {
            val mins = params["minutes"]?.toIntOrNull() ?: return jsonOk(mapOf("ok" to false))
            appendEvent("metrics", mapOf("doom_scroll" to mins)); jsonOk(mapOf("ok" to true))
        }
        "pushups" -> {
            val count = params["count"]?.toIntOrNull() ?: return jsonOk(mapOf("ok" to false))
            appendEvent("metrics", mapOf("pushups" to count)); jsonOk(mapOf("ok" to true))
        }
        "panic" -> handleLogPanic(params)
        "win" -> {
            val text = params["text"] ?: return jsonOk(mapOf("ok" to false))
            appendEvent("journal", mapOf("subtype" to "win", "text" to text)); jsonOk(mapOf("ok" to true))
        }
        "journal" -> {
            val text = params["text"] ?: return jsonOk(mapOf("ok" to false))
            appendEvent("journal", mapOf("subtype" to "journal", "text" to text)); jsonOk(mapOf("ok" to true))
        }
        "think" -> {
            val text = params["text"] ?: return jsonOk(mapOf("ok" to false))
            val topic = params["topic"] ?: "general"
            appendEvent("thought", mapOf("topic" to topic, "text" to text)); jsonOk(mapOf("ok" to true))
        }
        "share" -> {
            val title = params["title"] ?: return jsonOk(mapOf("ok" to false))
            val type = params["type"] ?: "text"
            val info = params["info"] ?: ""
            val content = params["content"] ?: ""
            val filePath = params["file_path"] ?: ""
            val userTags = params["tags"] ?: ""
            val autoTags = mutableListOf<String>()
            when (type) {
                "url" -> autoTags.add("URL")
                "image" -> autoTags.add("Image")
                "video" -> autoTags.add("Video")
                "audio" -> autoTags.add("Audio")
                "pdf" -> autoTags.add("PDF")
            }
            val ext = filePath.lowercase().substringAfterLast('.', "")
            when {
                ext in listOf("mp4","mkv","avi","mov","webm","3gp") -> if ("Video" !in autoTags) autoTags.add("Video")
                ext in listOf("mp3","wav","ogg","flac","aac","m4a","opus") -> if ("Audio" !in autoTags) autoTags.add("Audio")
                ext in listOf("jpg","jpeg","png","gif","webp","bmp","svg") -> if ("Image" !in autoTags) autoTags.add("Image")
                ext == "pdf" -> if ("PDF" !in autoTags) autoTags.add("PDF")
            }
            val contentLower = content.lowercase().trim()
            val firstLine = contentLower.lines().first().trim()
            if (firstLine.startsWith("http://") || firstLine.startsWith("https://")) {
                val contentExt = firstLine.substringAfterLast('.', "").substringBefore(' ')
                when {
                    contentExt in listOf("mp4","mkv","avi","mov","webm","3gp","mpg","mpeg") -> { if ("Video" !in autoTags) autoTags.add("Video"); if ("URL" !in autoTags) autoTags.add("URL") }
                    contentExt in listOf("mp3","wav","ogg","flac","aac","m4a","opus") -> { if ("Audio" !in autoTags) autoTags.add("Audio"); if ("URL" !in autoTags) autoTags.add("URL") }
                    contentExt in listOf("jpg","jpeg","png","gif","webp","bmp","svg") -> { if ("Image" !in autoTags) autoTags.add("Image"); if ("URL" !in autoTags) autoTags.add("URL") }
                    contentExt == "pdf" -> { if ("PDF" !in autoTags) autoTags.add("PDF"); if ("URL" !in autoTags) autoTags.add("URL") }
                    else -> if ("URL" !in autoTags) autoTags.add("URL")
                }
            }
            val allTags = (autoTags + userTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }).distinct().joinToString(",")
            val data = mutableMapOf<String, Any>("title" to title, "type" to type)
            if (info.isNotEmpty()) data["info"] = info
            if (content.isNotEmpty()) data["text"] = content
            if (filePath.isNotEmpty()) data["file_path"] = filePath
            if (allTags.isNotEmpty()) data["tags"] = allTags
            val loc = params["location"] ?: ""
            if (loc.isNotEmpty()) data["location"] = loc
            val capTime = params["captured_time"] ?: ""
            if (capTime.isNotEmpty()) data["captured_time"] = capTime
            appendEvent("shares", data)
            // Auto-create a thought with hidden type tag for the shared material
            val thoughtText = buildString {
                append(title)
                val c = content.ifEmpty { info }
                if (c.isNotEmpty()) { append(": "); append(c) }
            }
            appendEvent("thought", mapOf("topic" to "quick", "text" to thoughtText, "info" to type))
            jsonOk(mapOf("ok" to true))
        }
        "habit" -> {
            val activity = params["activity"] ?: return jsonOk(mapOf("ok" to false))
            val on = params["on"]?.toBoolean() ?: return jsonOk(mapOf("ok" to false))
            val notes = params["notes"]
            val data = mutableMapOf<String, Any>("activity" to activity, "on" to on)
            if (!notes.isNullOrBlank()) data["notes"] = notes
            appendEvent("habits", data); jsonOk(mapOf("ok" to true))
        }
        "dsa" -> {
            val topic = params["topic"] ?: ""
            val name = params["name"] ?: ""
            val platform = params["platform"] ?: "Other"
            val diff = params["difficulty"] ?: "medium"
            val completed = params["completed"]?.toBoolean() ?: false
            val desc = params["description"] ?: ""
            val approach = params["approach"] ?: ""
            appendEvent("dsa", mapOf("topic" to topic, "problem" to name, "platform" to platform, "difficulty" to diff, "completed" to completed, "description" to desc, "approach" to approach))
            jsonOk(mapOf("ok" to true))
        }
        "chess" -> {
            val opp = params["opponent"] ?: return jsonOk(mapOf("ok" to false))
            val result = params["result"] ?: return jsonOk(mapOf("ok" to false))
            val color = params["color"] ?: "white"
            val notes = params["notes"] ?: ""
            appendEvent("chess", mapOf("opponent" to opp, "result" to result, "color" to color, "notes" to notes))
            jsonOk(mapOf("ok" to true))
        }
        "run" -> handleLogRun(params)
        else -> jsonOk(mapOf("error" to "unknown log type"))
    }
}

private fun handleLogPanic(params: Map<String, String>): String {
    val trigger = params["trigger"] ?: ""
    val now = OffsetDateTime.now()
    val data = mutableMapOf<String, Any>("type" to "panic", "time" to now.format(DateTimeFormatter.ofPattern("HH:mm")))
    if (trigger.isNotEmpty()) data["note"] = trigger
    val panics = readEventsAll("panics")
    val last = panics.lastOrNull { it.type == "panic" }
    if (last != null && last.ts != null) {
        val lastDt = parseTs(last.ts ?: "")
        if (lastDt != null) {
            val secs = java.time.Duration.between(lastDt, now).seconds.toInt()
            data["time_since_last"] = "${secs / 86400}d ${(secs % 86400) / 3600}h ${(secs % 3600) / 60}m"
            data["time_since_last_sec"] = secs
        }
    }
    appendEvent("panics", data)
    return jsonOk(mapOf("ok" to true))
}

private fun handleLogRun(params: Map<String, String>): String {
    val dist = params["distance_km"]?.toDoubleOrNull() ?: return jsonOk(mapOf("ok" to false))
    val durStr = params["duration_min"] ?: return jsonOk(mapOf("ok" to false))
    val runType = params["run_type"] ?: "easy"
    val notes = params["notes"] ?: ""
    val durSecs = if (durStr.contains(":")) {
        val parts = durStr.split(":")
        val m = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val s = parts.getOrNull(1)?.toIntOrNull() ?: 0
        m * 60 + s
    } else {
        (durStr.toIntOrNull() ?: 0) * 60
    }
    val paceSec = if (dist > 0) (durSecs / dist).roundToInt() else 0
    val pace = "${paceSec / 60}:${"%02d".format(paceSec % 60)}"
    appendEvent("running", mapOf("distance_km" to dist, "duration_min" to durSecs, "pace" to pace, "run_type" to runType, "notes" to notes))
    return jsonOk(mapOf("ok" to true))
}

private fun handleRunningStats(params: Map<String, String>): String {
    val weeks = params["weeks"]?.toIntOrNull() ?: 4
    val all = readEvents("running")
    val today = LocalDate.now()
    val sunday = today.minusDays((today.dayOfWeek.value % 7).toLong())
    val start = sunday.minusDays((weeks - 1) * 7L)
    val monthStart = today.withDayOfMonth(1)
    val datedRuns = all.mapNotNull { r ->
        val d = try { LocalDate.parse(r.date ?: "") } catch (_: Exception) { null } ?: return@mapNotNull null
        d to r
    }
    val periodRuns = datedRuns.filter { it.first >= start }.map { it.second }
    val thisWeek = datedRuns.filter { it.first >= sunday }.map { it.second }
    val thisMonth = datedRuns.filter { it.first >= monthStart }.map { it.second }

    fun durationSec(r: Event): Int {
        val raw = r.duration_min ?: 0
        val km = r.distance_km ?: 0.0
        if (raw in 1..99 && (km <= 0 || (raw.toDouble() / km) < 25.0)) {
            return raw * 60
        }
        return raw
    }
    fun km(runs: List<Event>): Double = "%.2f".format(runs.sumOf { it.distance_km ?: 0.0 }).toDouble()
    fun avgPace(runs: List<Event>): String {
        val totalKm = runs.sumOf { it.distance_km ?: 0.0 }
        val totalSec = runs.sumOf { durationSec(it) }
        if (totalKm <= 0.0 || totalSec <= 0) return "--"
        val paceSec = (totalSec / totalKm).roundToInt()
        return "${paceSec / 60}:${"%02d".format(paceSec % 60)}"
    }

    val byType = thisWeek.groupBy { it.run_type ?: "easy" }.mapValues { (_, runs) ->
        mapOf(
            "count" to runs.size,
            "km" to km(runs),
            "seconds" to runs.sumOf { durationSec(it) }
        )
    }

    val daily = (0 until weeks * 7).map { start.plusDays(it.toLong()) }
        .mapNotNull { d ->
            val runs = datedRuns.filter { it.first == d }.map { it.second }
            if (runs.isEmpty()) null else mapOf(
                "date" to d.toString(),
                "count" to runs.size,
                "total_km" to km(runs),
                "total_sec" to runs.sumOf { durationSec(it) },
                "avg_pace" to avgPace(runs)
            )
        }

    val recentRuns = datedRuns.sortedByDescending { it.second.ts ?: "" }.take(10).map { (_, r) ->
        val seconds = durationSec(r)
        val totalKm = r.distance_km ?: 0.0
        val pace = if (totalKm > 0.0 && seconds > 0) {
            val paceSec = (seconds / totalKm).roundToInt()
            "${paceSec / 60}:${"%02d".format(paceSec % 60)}"
        } else "--"
        val minutes = Math.round(seconds / 60.0).toInt()
        val mm = seconds / 60
        val ss = seconds % 60
        mapOf(
            "id" to r.id,
            "date" to r.date,
            "ts" to r.ts,
            "distance_km" to totalKm,
            "duration_sec" to seconds,
            "duration_min" to minutes,
            "minutes" to minutes,
            "duration_formatted" to if (ss > 0) "${mm}m ${ss}s" else "${mm}m",
            "pace" to pace,
            "run_type" to (r.run_type ?: "easy"),
            "notes" to (r.notes ?: "")
        )
    }

    return jsonOk(mapOf(
        "weekly" to mapOf("km" to km(thisWeek), "runs" to thisWeek.size, "seconds" to thisWeek.sumOf { durationSec(it) }),
        "monthly" to mapOf("km" to km(thisMonth), "runs" to thisMonth.size, "seconds" to thisMonth.sumOf { durationSec(it) }),
        "period" to mapOf("km" to km(periodRuns), "runs" to periodRuns.size, "weeks" to weeks, "seconds" to periodRuns.sumOf { durationSec(it) }),
        "by_type" to byType,
        "daily" to daily,
        "recent_runs" to recentRuns,
        "avg_pace" to avgPace(periodRuns)
    ))
}

private fun handleEdit(params: Map<String, String>): String {
    val field = params["field"] ?: return jsonOk(mapOf("ok" to false))
    val value = params["value"] ?: return jsonOk(mapOf("ok" to false))
    val editCats = mapOf("Mood" to Pair("mood", "mood"), "Energy" to Pair("mood", "energy"), "Doom Scroll" to Pair("metrics", "doom_scroll"), "Pushups" to Pair("metrics", "pushups"), "Win" to Pair("journal", "win"), "Lesson" to Pair("journal", "lesson"), "Journal" to Pair("journal", "journal"))
    val (cat, key) = editCats[field] ?: return jsonOk(mapOf("ok" to false, "detail" to "unknown field"))
    val data = if (field in listOf("Doom Scroll", "Pushups")) mapOf(key to (value.toIntOrNull() ?: 0)) else mapOf(key to value)
    appendEvent(cat, data); return jsonOk(mapOf("ok" to true))
}

private fun handleThoughtRoute(method: String, path: String, params: Map<String, String>): String {
    return when {
        path == "/thoughts" && method == "GET" -> {
            val all = readEventsAll("thought")
            val visited = readEventsAll("visited").filter { it.visited == true }.mapNotNull { it.thought_id }.toSet()
            val pinnedIds = run {
                val pinnedFile = File(VaultConfig.eventsDir, "pinned_thoughts.jsonl")
                if (!pinnedFile.exists()) emptySet()
                else try {
                    val lastLine = pinnedFile.readLines().filter { it.isNotBlank() }.lastOrNull()
                    if (lastLine == null) emptySet()
                    else {
                        val parsed = jsonToMap(lastLine)
                        val idsList = parsed["ids"]
                        when (idsList) {
                            is List<*> -> idsList.filterIsInstance<String>().toSet()
                            else -> emptySet()
                        }
                    }
                } catch (_: Exception) { emptySet() }
            }
            val result = all.map { mapOf<String, Any?>("id" to it.id, "ts" to it.ts, "text" to it.text, "topic" to it.topic, "verse_id" to it.verse_id, "visited" to (it.id in visited), "pinned" to (it.id in pinnedIds)) }
                .sortedByDescending { if ((it["pinned"] as? Boolean) == true) "1" else "0" + ((it["ts"] as? String) ?: "") }
            jsonOk(mapOf("thoughts" to result))
        }
        path == "/thought/visit" && method == "POST" -> {
            val tid = params["thought_id"] ?: return jsonOk(mapOf("ok" to false))
            appendEvent("visited", mapOf("thought_id" to tid, "visited" to true)); jsonOk(mapOf("ok" to true))
        }
        path == "/thought/pin" && method == "POST" -> {
            val tid = params["thought_id"] ?: return jsonOk(mapOf("ok" to false))
            val pin = params["pinned"]?.toBoolean() ?: true
            val pinnedFile = File(VaultConfig.eventsDir, "pinned_thoughts.jsonl")
            val current = mutableListOf<String>()
            if (pinnedFile.exists()) {
                try {
                    val lastLine = pinnedFile.readLines().filter { it.isNotBlank() }.lastOrNull()
                    if (lastLine != null) {
                        val parsed = jsonToMap(lastLine)
                        val idsList = parsed["ids"]
                        when (idsList) {
                            is List<*> -> current.addAll(idsList.filterIsInstance<String>())
                        }
                    }
                } catch (_: Exception) {}
            }
            if (pin && tid !in current) current.add(tid)
            else if (!pin) current.remove(tid)
            appendEvent("pinned_thoughts", mapOf("ids" to current))
            jsonOk(mapOf("ok" to true))
        }
        else -> jsonOk(mapOf("error" to "not found"))
    }
}

private fun handleRecycleRoute(method: String, path: String, params: Map<String, String>): String {
    val parts = path.split("/").drop(1)
    return when {
        method == "GET" -> {
            val cat = params["category"]
            val entries = listRecycleEntries(cat)
            val entryMaps = entries.map { e ->
                mapOf(
                    "recycle_id" to e.recycle_id,
                    "category" to e.category,
                    "data" to e.data,
                    "deleted_at" to e.deleted_at
                )
            }
            jsonOk(mapOf("entries" to entryMaps))
        }
        method == "POST" && parts.size >= 3 && parts.last() == "restore" -> {
            val entry = restoreRecycleEntry(parts[1])
            if (entry == null) jsonOk(mapOf("ok" to false, "detail" to "not found"))
            else {
                if (entry.category == "sessions") {
                    try {
                        val rawLine = entry.data["_raw"] as? String ?: ""
                        if (rawLine.isNotEmpty()) {
                            val ev = jsonToMap(rawLine)
                            val eType = ev["type"] as? String ?: ""
                            val eActivity = ev["activity"] as? String ?: ""
                            val eDuration = (ev["duration"] as? Number)?.toInt() ?: 0
                            val eDate = ev["date"] as? String ?: java.time.LocalDate.now().toString()
                            if (eType == "end" && eActivity.isNotEmpty() && eDuration > 0) {
                                SessionStorage.addMinutesToDailyStats(eActivity, eDuration, eDate)
                            }
                        }
                    } catch (_: Exception) {}
                }
                jsonOk(mapOf("ok" to true, "restored" to true))
            }
        }
        method == "POST" && parts.lastOrNull() == "clear" -> {
            val cat = params["category"]
            clearRecycleBin(cat)
            jsonOk(mapOf("ok" to true))
        }
        else -> jsonOk(mapOf("error" to "not found"))
    }
}

private fun handleMetaRoute(path: String): String {
    return when (path) {
        "/meta/moods" -> toJson(MOODS.map { mapOf("value" to it.first, "label" to it.second) })
        "/meta/energies" -> toJson(ENERGIES.map { mapOf("value" to it.first, "label" to it.second) })
        "/meta/activities" -> toJson(SessionStorage.getAllActivities())
        "/meta/dsa-topics" -> toJson(DSA_TOPICS)
        "/meta/think-topics" -> {
            val topics = readEventsAll("thought").mapNotNull { it.topic }.filter { it.isNotBlank() }.distinct().sorted()
            toJson(topics)
        }
        else -> jsonOk(mapOf("error" to "not found"))
    }
}

private fun handleNotifyRoute(path: String): String {
    return when (path) {
        "/notify/status" -> toJson(getNotifyStatus())
        "/notify/daily-verse" -> toJson(getNotifyDailyVerse())
        else -> jsonOk(mapOf("error" to "not found"))
    }
}

private fun handleNotifyPrefsRoute(method: String, path: String, params: Map<String, String>, body: String): String {
    return when {
        method == "GET" -> {
            jsonOk(mapOf(
                "overdue_alert" to NotificationPreferences.isOverdueAlertEnabled(),
                "due_today_alert" to NotificationPreferences.isDueTodayAlertEnabled(),
                "time_priority" to NotificationPreferences.isTimePriorityAlertEnabled(),
                "streak_reminder" to NotificationPreferences.isStreakReminderEnabled(),
                "habits_reminder" to NotificationPreferences.isHabitsReminderEnabled(),
                "mood_reminder" to NotificationPreferences.isMoodReminderEnabled(),
                "gita_daily" to NotificationPreferences.isGitaDailyEnabled(),
                "gita_daily_time" to NotificationPreferences.getGitaDailyTime(),
                "not_logged_in" to NotificationPreferences.isNotLoggedInEnabled(),
                "checklist_reminder" to NotificationPreferences.isChecklistReminderEnabled()
            ))
        }
        method == "POST" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap<String, Any?>()
            jsonBody.forEach { (key, value) ->
                when (key) {
                    "overdue_alert" -> NotificationPreferences.setOverdueAlertEnabled((value as? Boolean) ?: false)
                    "due_today_alert" -> NotificationPreferences.setDueTodayAlertEnabled((value as? Boolean) ?: false)
                    "time_priority" -> NotificationPreferences.setTimePriorityAlertEnabled((value as? Boolean) ?: false)
                    "streak_reminder" -> NotificationPreferences.setStreakReminderEnabled((value as? Boolean) ?: false)
                    "habits_reminder" -> NotificationPreferences.setHabitsReminderEnabled((value as? Boolean) ?: false)
                    "mood_reminder" -> NotificationPreferences.setMoodReminderEnabled((value as? Boolean) ?: false)
                    "gita_daily" -> NotificationPreferences.setGitaDailyEnabled((value as? Boolean) ?: false)
                    "gita_daily_time" -> NotificationPreferences.setGitaDailyTime((value as? String) ?: "")
                    "not_logged_in" -> NotificationPreferences.setNotLoggedInEnabled((value as? Boolean) ?: false)
                    "checklist_reminder" -> NotificationPreferences.setChecklistReminderEnabled((value as? Boolean) ?: false)
                }
            }
            jsonOk(mapOf("ok" to true))
        }
        else -> jsonOk(mapOf("error" to "method not allowed"))
    }
}

private fun handleClientLog(body: String): String {
    val jsonBody = if (body.isNotEmpty()) try { jsonToMap(body) } catch (_: Exception) { emptyMap() } else emptyMap()
    val ts = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val entry = mutableMapOf<String, Any?>("ts" to ts)
    entry.putAll(jsonBody)
    val logDir = File(eventsDir(), "client_logs").also { it.mkdirs() }
    File(logDir, "${LocalDate.now()}.jsonl").appendText(toJson(entry) + "\n")
    return jsonOk(mapOf("ok" to true))
}

private fun handleVaultInit(): String {
    val categories = listOf("mood", "energy", "metrics", "habits", "journal", "thought", "dsa", "chess", "running", "panics", "sessions", "tasks", "shares")
    val created = mutableListOf<String>()
    val skipped = mutableListOf<String>()
    for (cat in categories) {
        val f = VaultConfig.catFile(cat)
        if (!f.exists()) {
            f.createNewFile()
            created.add(cat)
        } else {
            skipped.add(cat)
        }
    }
    VaultConfig.appUsageDir.mkdirs()
    VaultConfig.gitaDir.mkdirs()
    VaultConfig.ensureGitaJson()
    VaultConfig.setVaultPath(VaultConfig.getVaultPath())
    VaultConfig.setVaultConfigured(true)
    return jsonOk(mapOf("created" to created, "skipped" to skipped))
}

private fun handleDatabase(params: Map<String, String>): String {
    val days = params["days"]?.toIntOrNull() ?: 30
    val catFilter = params["category"]?.takeIf { it.isNotBlank() && it != "all" }
    val today = LocalDate.now()
    val start = if (days > 0) today.minusDays(days.toLong() - 1).toString() else "0000-00-00"
    val end = today.toString()

    val standardCats = listOf(
        "sessions", "habits", "metrics", "panics", "journal", "dsa", "chess",
        "tasks", "running", "mood", "energy", "thought", "checklist", "daytracker", "shares"
    )
    val discoveredCats = linkedSetOf<String>()
    discoveredCats.addAll(standardCats)
    val dir = eventsDir()
    if (dir.exists()) {
        dir.listFiles { _, name -> name.endsWith(".jsonl") && !name.endsWith("_ui.jsonl") }?.forEach { f ->
            discoveredCats.add(f.name.removeSuffix(".jsonl"))
        }
    }

    val catsToQuery = if (catFilter != null) listOf(catFilter) else discoveredCats.toList()
    val results = mutableListOf<Map<String, Any?>>()
    val seenEventIds = mutableSetOf<String>()

    for (cat in catsToQuery) {
        if (cat == "daytracker") {
            val f = VaultConfig.dayTrackerFile
            if (f.exists()) {
                for (line in f.readLines().filter { it.isNotBlank() }) {
                    try {
                        val m = jsonToMap(line)
                        val d = (m["date"] as? String)?.takeIf { it.isNotBlank() } ?: (m["ts"] as? String)?.take(10) ?: ""
                        if (days <= 0 || (d >= start && d <= end)) {
                            val id = (m["id"] as? String)?.takeIf { it.isNotBlank() } ?: "dt_${System.identityHashCode(m)}"
                            if (seenEventIds.add(id)) {
                                val row = m.toMutableMap()
                                row["id"] = id
                                row["category"] = "daytracker"
                                if (row["date"] == null || (row["date"] as? String).isNullOrEmpty()) row["date"] = d
                                results.add(row)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
        val events = if (days > 0) eventsInRangeRaw(cat, start, end) else readEventsAllRaw(cat)
        for (raw in events) {
            val m = raw.toMutableMap()
            val id = (m["id"] as? String)?.takeIf { it.isNotBlank() } ?: "${cat}_${System.identityHashCode(raw)}"
            if (seenEventIds.add(id)) {
                m["id"] = id
                m["category"] = cat
                if (m["date"] == null || (m["date"] as? String).isNullOrEmpty()) {
                    m["date"] = (m["ts"] as? String)?.take(10) ?: ""
                }
                if (cat == "running") {
                    val rawSec = (m["duration_sec"] as? Number)?.toInt()
                    val dist = (m["distance_km"] as? Number)?.toDouble() ?: 0.0
                    val sec = if (rawSec != null && rawSec > 0) {
                        rawSec
                    } else {
                        val raw = (m["duration_min"] as? Number)?.toDouble()
                            ?: (m["duration"] as? Number)?.toDouble()
                            ?: (m["minutes"] as? Number)?.toDouble()
                            ?: (m["duration_min"] as? String)?.toDoubleOrNull()
                            ?: (m["duration_sec"] as? String)?.toDoubleOrNull()
                            ?: 0.0
                        val isSec = if (dist > 0.0) (raw / dist > 20.0) else (raw >= 100.0)
                        if (isSec) Math.round(raw).toInt() else Math.round(raw * 60.0).toInt()
                    }
                    val min = Math.round(sec / 60.0).toInt()
                    val mm = sec / 60
                    val ss = sec % 60
                    m["duration_sec"] = sec
                    m["duration_min"] = min
                    m["duration_minutes"] = min
                    m["minutes"] = min
                    m["duration_formatted"] = if (ss > 0) "${mm}m ${ss}s" else "${mm}m"
                }
                results.add(m)
            }
        }
    }
    results.sortByDescending { (it["ts"] as? String) ?: (it["date"] as? String) ?: "" }
    return jsonOk(mapOf("events" to results))
}

private fun handleVaultPath(method: String): String {
    return when (method) {
        "GET" -> jsonOk(mapOf("path" to VaultConfig.getVaultPath()))
        else -> jsonOk(mapOf("error" to "method not allowed"))
    }
}

/**
 * Core AI & Gemini integration routes.
 * Provides endpoints for status, user credentials/model configuration, connection tests,
 * and high-level generation/chat for the app and plugins.
 */
private fun handleAIRoute(
    method: String,
    path: String,
    headers: Map<String, String>,
    body: String
): String {
    return when {
        path == "/ai/status" && method == "GET" -> {
            val pluginId = headers["x-plugin-id"]
            val allowed = GeminiManager.canPluginAccessAI(pluginId)
            jsonOk(mapOf(
                "configured" to GeminiConfig.isConfigured,
                "hasAccess" to allowed,
                "defaultModel" to GeminiConfig.defaultModel,
                "baseUrl" to GeminiConfig.baseUrl,
                "provider" to GeminiConfig.provider,
                "effectiveProvider" to GeminiConfig.detectEffectiveProvider(),
                "availableModels" to GeminiConfig.AVAILABLE_MODELS,
                "usage" to GeminiManager.getUsageStats()
            ))
        }

        path == "/ai/config" && method == "GET" -> {
            jsonOk(mapOf(
                "configured" to GeminiConfig.isConfigured,
                "maskedKey" to GeminiConfig.maskedKey(),
                "hasKey" to GeminiConfig.isConfigured,
                "baseUrl" to GeminiConfig.baseUrl,
                "defaultModel" to GeminiConfig.defaultModel,
                "provider" to GeminiConfig.provider,
                "effectiveProvider" to GeminiConfig.detectEffectiveProvider(),
                "temperature" to GeminiConfig.temperature,
                "maxTokens" to GeminiConfig.maxOutputTokens,
                "availableModels" to GeminiConfig.AVAILABLE_MODELS,
                "usage" to GeminiManager.getUsageStats()
            ))
        }

        path == "/ai/config" && method == "POST" -> {
            try {
                val json = if (body.isNotBlank()) org.json.JSONObject(body) else org.json.JSONObject()
                if (json.has("apiKey")) {
                    val key = json.optString("apiKey", "").trim()
                    GeminiConfig.apiKey = key
                }
                if (json.has("baseUrl")) {
                    val url = json.optString("baseUrl", "").trim()
                    GeminiConfig.baseUrl = url
                }
                if (json.has("defaultModel")) {
                    val m = json.optString("defaultModel", "").trim()
                    if (m.isNotEmpty()) GeminiConfig.defaultModel = m
                }
                if (json.has("provider")) {
                    GeminiConfig.provider = json.optString("provider", "auto").trim()
                }
                if (json.has("temperature")) {
                    GeminiConfig.temperature = json.optDouble("temperature", 0.7).toFloat()
                }
                if (json.has("maxTokens")) {
                    GeminiConfig.maxOutputTokens = json.optInt("maxTokens", 4096)
                }
                jsonOk(mapOf(
                    "ok" to true,
                    "configured" to GeminiConfig.isConfigured,
                    "maskedKey" to GeminiConfig.maskedKey(),
                    "defaultModel" to GeminiConfig.defaultModel,
                    "effectiveProvider" to GeminiConfig.detectEffectiveProvider()
                ))
            } catch (e: Exception) {
                jsonOk(mapOf("ok" to false, "error" to (e.message ?: "Failed to save AI config")))
            }
        }

        path == "/ai/test" && method == "POST" -> {
            val json = runCatching { if (body.isNotBlank()) JSONObject(body) else null }.getOrNull()
            val reqKey = json?.optString("apiKey")?.trim()?.ifBlank { null }
            val reqModel = json?.optString("model")?.trim()?.ifBlank { null }

            val result = kotlinx.coroutines.runBlocking {
                GeminiClient.testConnection(model = reqModel, apiKey = reqKey)
            }

            if (result.ok) {
                if (reqKey != null && (GeminiConfig.apiKey.isBlank() || GeminiConfig.apiKey == reqKey)) {
                    GeminiConfig.apiKey = reqKey
                }
                if (reqModel != null && reqModel.isNotBlank()) {
                    GeminiConfig.defaultModel = reqModel
                }
            }

            toJson(mapOf(
                "ok" to result.ok,
                "model" to result.model,
                "latencyMs" to result.latencyMs,
                "error" to result.error,
                "reply" to result.text,
                "maskedKey" to GeminiConfig.maskedKey()
            ))
        }

        path == "/ai/models" && method == "GET" -> {
            jsonOk(mapOf(
                "models" to GeminiConfig.AVAILABLE_MODELS,
                "defaultModel" to GeminiConfig.defaultModel
            ))
        }

        path == "/ai/generate" && method == "POST" -> {
            val pluginId = headers["x-plugin-id"]
            if (!GeminiManager.canPluginAccessAI(pluginId)) {
                return toJson(mapOf(
                    "ok" to false,
                    "error" to "Permission Denied: Plugin '${pluginId ?: "unknown"}' does not have 'ai' permission or AI access is disabled in settings."
                ))
            }

            try {
                val req = jsonToMap(body)
                val prompt = req["prompt"] as? String ?: return jsonOk(mapOf("ok" to false, "error" to "Missing required 'prompt'"))
                val sysInst = req["systemInstruction"] as? String
                val model = req["model"] as? String
                val temp = (req["temperature"] as? Number)?.toFloat()
                val maxTok = (req["maxTokens"] as? Number)?.toInt()
                val format = (req["responseFormat"] as? String) ?: "text"
                @Suppress("UNCHECKED_CAST")
                val schema = req["jsonSchema"] as? Map<String, Any?>

                val result = kotlinx.coroutines.runBlocking {
                    GeminiClient.generate(
                        GeminiClient.GenerateOptions(
                            prompt = prompt,
                            systemInstruction = sysInst,
                            model = model,
                            temperature = temp,
                            maxTokens = maxTok,
                            responseFormat = format,
                            responseSchema = schema
                        )
                    )
                }

                if (result.ok) {
                    val promptTok = result.usage["promptTokens"] ?: 0
                    val candTok = result.usage["candidatesTokens"] ?: 0
                    GeminiManager.recordUsage(promptTok, candTok)
                }

                toJson(mapOf(
                    "ok" to result.ok,
                    "text" to result.text,
                    "data" to result.data,
                    "model" to result.model,
                    "usage" to result.usage,
                    "latencyMs" to result.latencyMs,
                    "error" to result.error
                ))
            } catch (e: Exception) {
                toJson(mapOf("ok" to false, "error" to (e.message ?: "Server error during AI generation")))
            }
        }

        path == "/ai/chat" && method == "POST" -> {
            val pluginId = headers["x-plugin-id"]
            if (!GeminiManager.canPluginAccessAI(pluginId)) {
                return toJson(mapOf(
                    "ok" to false,
                    "error" to "Permission Denied: Plugin '${pluginId ?: "unknown"}' does not have 'ai' permission or AI access is disabled in settings."
                ))
            }

            try {
                val req = jsonToMap(body)
                @Suppress("UNCHECKED_CAST")
                val rawMessages = req["messages"] as? List<Map<String, Any?>> ?: emptyList()
                val messages = rawMessages.map { m ->
                    mapOf(
                        "role" to (m["role"]?.toString() ?: "user"),
                        "content" to (m["content"]?.toString() ?: m["text"]?.toString() ?: "")
                    )
                }
                val sysInst = req["systemInstruction"] as? String
                val model = req["model"] as? String
                val temp = (req["temperature"] as? Number)?.toFloat()

                val result = kotlinx.coroutines.runBlocking {
                    GeminiClient.chat(messages, sysInst, model, temp)
                }

                if (result.ok) {
                    val promptTok = result.usage["promptTokens"] ?: 0
                    val candTok = result.usage["candidatesTokens"] ?: 0
                    GeminiManager.recordUsage(promptTok, candTok)
                }

                toJson(mapOf(
                    "ok" to result.ok,
                    "text" to result.text,
                    "model" to result.model,
                    "usage" to result.usage,
                    "latencyMs" to result.latencyMs,
                    "error" to result.error
                ))
            } catch (e: Exception) {
                toJson(mapOf("ok" to false, "error" to (e.message ?: "Server error during AI chat")))
            }
        }

        else -> jsonOk(mapOf("error" to "unknown AI endpoint"))
    }
}

/**
 * Route handler for the Weekly Summary Aggregator & AI Synthesizer.
 * GET /api/weekly-summary/aggregate - backend aggregation of all logged categories
 * POST /api/weekly-summary/generate - runs aggregation and synthesizes with AI
 * POST /api/weekly-summary/save - saves the generated markdown into the Obsidian Vault
 */
private fun handleWeeklySummaryRoute(
    method: String,
    path: String,
    params: Map<String, String>,
    headers: Map<String, String>,
    body: String
): String {
    return when {
        path == "/api/weekly-summary/aggregate" && method == "GET" -> {
            val start = params["start"]?.ifBlank { null }
            val end = params["end"]?.ifBlank { null }
            val offset = params["offset"]?.toIntOrNull()
            val agg = WeeklyAggregator.aggregate(start, end, offset)
            toJson(agg)
        }

        path == "/api/weekly-summary/generate" && method == "POST" -> {
            try {
                val req = if (body.isNotBlank()) jsonToMap(body) else emptyMap()
                val pluginId = headers["x-plugin-id"]
                if (!GeminiManager.canPluginAccessAI(pluginId)) {
                    return toJson(mapOf("ok" to false, "error" to "Permission denied: plugin does not have AI access permission"))
                }

                val start = (req["start"] as? String)?.ifBlank { null }
                val end = (req["end"] as? String)?.ifBlank { null }
                val offset = (req["offset"] as? Number)?.toInt()
                val model = (req["model"] as? String)?.ifBlank { null }
                val style = (req["style"] as? String)?.lowercase() ?: "executive"
                val customPrompt = (req["prompt"] as? String)?.ifBlank { null }

                // 1. Run backend aggregation
                val agg = WeeklyAggregator.aggregate(start, end, offset)
                val promptContext = agg["prompt_context"] as? String ?: ""

                // 2. Build system instruction based on selected style
                val systemInstruction = when (style) {
                    "strategic" -> """
                        You are Weekly Digest AI, an elite operational strategist and executive coach for the V07 operating system.
                        Focus on strategic leverage, bottlenecks in Projects/Focus blocks, discipline adherence, and high-impact tactical directives.
                        Keep fluff to zero. Deliver an intense, highly-analytical tactical briefing in Markdown.
                    """.trimIndent()
                    "deep_dive" -> """
                        You are Weekly Digest AI, an exhaustive personal telemetry analyst and cognitive performance auditor for the V07 operating system.
                        Provide a thorough diagnostic covering focus architecture, habit consistency, emotional energy, athletics, and digital attention.
                        Correlate patterns between rest/emotions and work output. Deliver in Markdown.
                    """.trimIndent()
                    else -> """
                        You are Weekly Digest AI, an elite cognitive strategist, executive performance coach, and life telemetry synthesizer for the V07 personal operating system.
                        Synthesize the user's weekly logged data with extreme analytical depth, psychological insight, and operational clarity.
                        Format your response in clean, beautiful GitHub Markdown with:
                        # ⚡ WEEKLY DIGEST // WEEKLY EXECUTIVE SYNTHESIS
                        > **Weekly Velocity & Cadence Rating**: [e.g. 8.6/10 - High Work Velocity, Attention Drag]
                        > **Executive TL;DR**: [2-3 punchy, insightful sentences summarizing the overarching weekly narrative]

                        ## 🏆 Highs, Victories & Completed Missions
                        [Concrete achievements from completed tasks, deep work sessions, reading, and athletic milestones]

                        ## 🧠 Cognitive Capital & Deep Work Architecture
                        [Analysis of focus hours, activity breakdown (Projects, Reading, Notes), peak focus flow days, and velocity]

                        ## 🧬 Discipline, Habits & Athletic Cadence
                        [Habit consistency, running endurance, pushup totals, physical momentum]

                        ## 🔋 Emotional Baseline & Mental Energy Dynamics
                        [Mood and energy trends, journal reflection synthesis, stress/fatigue points, psychological resilience]

                        ## 📱 Digital Attention Audit & Focus Leaks
                        [Screen time analysis, top app distribution, doom scrolling impact, attention leaks vs deep work]

                        ## 💡 Synthesized Ideas & Brain Dump Patterns
                        [Key insights, themes, and takeaways from user thoughts/notes]

                        ## 🎯 Strategic Directives for Next Week
                        [3-4 high-impact, actionable, prioritized operational directives for the upcoming week]
                    """.trimIndent()
                }

                val userPrompt = if (customPrompt != null) {
                    "$promptContext\n\n### ADDITIONAL USER INSTRUCTION:\n$customPrompt"
                } else {
                    "Synthesize the provided weekly telemetry into an executive weekly briefing according to your system instructions:\n\n$promptContext"
                }

                val result = kotlinx.coroutines.runBlocking {
                    GeminiClient.generate(
                        GeminiClient.GenerateOptions(
                            prompt = userPrompt,
                            systemInstruction = systemInstruction,
                            model = model,
                            temperature = 0.4f,
                            maxTokens = 4096
                        )
                    )
                }

                if (result.ok) {
                    val promptTok = result.usage["promptTokens"] ?: 0
                    val candTok = result.usage["candidatesTokens"] ?: 0
                    GeminiManager.recordUsage(promptTok, candTok)
                }

                toJson(mapOf(
                    "ok" to result.ok,
                    "markdown" to result.text,
                    "model" to result.model,
                    "data" to agg,
                    "usage" to result.usage,
                    "latencyMs" to result.latencyMs,
                    "error" to result.error
                ))
            } catch (e: Exception) {
                toJson(mapOf("ok" to false, "error" to (e.message ?: "Failed to generate weekly summary")))
            }
        }

        path == "/api/weekly-summary/save" && method == "POST" -> {
            try {
                val req = if (body.isNotBlank()) jsonToMap(body) else emptyMap()
                val start = req["start"]?.toString() ?: LocalDate.now().minusDays(6).toString()
                val end = req["end"]?.toString() ?: LocalDate.now().toString()
                val content = req["content"]?.toString() ?: ""
                val title = req["title"]?.toString()
                if (content.isBlank()) {
                    return toJson(mapOf("ok" to false, "error" to "Content is empty"))
                }
                val file = WeeklyAggregator.saveToVault(start, end, content, title)
                toJson(mapOf(
                    "ok" to true,
                    "path" to file.absolutePath,
                    "filename" to file.name
                ))
            } catch (e: Exception) {
                toJson(mapOf("ok" to false, "error" to (e.message ?: "Failed to save summary to vault")))
            }
        }

        else -> jsonOk(mapOf("error" to "unknown weekly-summary endpoint"))
    }
}

/**
 * Plugin routes for the in-app plugin system. Serves control endpoints (registry,
 * install, enable/disable/remove, store index) and plugin bundle files (index.html,
 * plugin.js, plugin.css, assets) to the WebView.
 */
private fun handlePluginRoute(
    method: String,
    path: String,
    params: Map<String, String>,
    body: String,
    output: OutputStream
) {
    try {
        // Control endpoints ─────────────────────────────────────────────
        if (path == "/plugins/registry" && method == "GET") {
            val installed = PluginManager.listInstalled()
            respond(output, 200, toJson(mapOf("plugins" to installed)), "application/json")
            return
        }
        if (path == "/plugins/index" && method == "GET") {
            val url = params["url"] ?: PluginStoreClient.DEFAULT_INDEX_URL
            val text = PluginStoreClient.fetchIndex(url)
            if (text.isEmpty()) {
                respond(output, 200, toJson(mapOf("plugins" to emptyList<Any>(), "error" to "unavailable")), "application/json")
            } else {
                output.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${text.toByteArray().size}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n".toByteArray())
                output.write(text.toByteArray())
                output.flush()
            }
            return
        }
        if (path == "/plugins/install" && method == "POST") {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val id = jsonBody["id"] as? String
                ?: return respond(output, 400, toJson(mapOf("ok" to false, "error" to "missing id")), "application/json")
            val bundleUrl = jsonBody["bundle_url"] as? String
                ?: return respond(output, 400, toJson(mapOf("ok" to false, "error" to "missing bundle_url")), "application/json")
            val bytes = PluginStoreClient.downloadBundle(bundleUrl)
                ?: return respond(output, 400, toJson(mapOf("ok" to false, "error" to "download failed")), "application/json")
            val result = PluginManager.install(id, bytes)
            when (result) {
                is PluginManager.Result.Ok ->
                    respond(output, 200, toJson(mapOf("ok" to true)), "application/json")
                is PluginManager.Result.Fail ->
                    respond(output, 400, toJson(mapOf("ok" to false, "error" to result.message)), "application/json")
            }
            return
        }
        if (path.startsWith("/plugins/") && method == "POST") {
            val parts = path.split("/")
            val id = parts.getOrNull(2)
            val action = parts.getOrNull(3)
            if (id != null && action != null) {
                val ok = when (action) {
                    "enable" -> PluginManager.enable(id)
                    "disable" -> PluginManager.disable(id)
                    "remove" -> PluginManager.remove(id)
                    "feature" -> {
                        val f = jsonToMap(body)
                        val feature = f["feature"] as? String
                        val enabled = (f["enabled"] as? Boolean) ?: false
                        if (feature != null) PluginManager.setFeature(id, feature, enabled) else false
                    }
                    else -> false
                }
                respond(output, 200, toJson(mapOf("ok" to ok)), "application/json")
                return
            }
        }

        // Bundle file serving ───────────────────────────────────────────
        // /plugins/{id}/... -> {filesDir}/v07-plugins/installed/{id}/...
        val parts = path.split("/").filter { it.isNotBlank() }
        if (parts.size >= 3 && parts[0] == "plugins") {
            val id = parts[1]
            val rawRelPath = parts.drop(2).joinToString("/")
            val cleanRelPath = try {
                java.net.URLDecoder.decode(rawRelPath.substringBefore('?').substringBefore("%3F"), "UTF-8")
            } catch (_: Exception) {
                rawRelPath.substringBefore('?').substringBefore("%3F")
            }
            val pluginRoot = PluginManager.pluginDir(id).canonicalFile
            val htmlFile = File(pluginRoot, cleanRelPath).canonicalFile

            if (cleanRelPath.endsWith(".html", true)) {
                var html: String? = null
                if (PluginManager.BUILTIN_PLUGIN_IDS.contains(id)) {
                    try {
                        appContext?.assets?.open("plugins/$id/$cleanRelPath")?.use {
                            html = it.bufferedReader().readText()
                        }
                    } catch (_: Exception) {}
                }
                if (html == null && htmlFile.toPath().startsWith(pluginRoot.toPath()) && htmlFile.exists() && htmlFile.isFile) {
                    html = htmlFile.readText()
                } else if (html == null) {
                    try {
                        appContext?.assets?.open("plugins/$id/$cleanRelPath")?.use {
                            html = it.bufferedReader().readText()
                        }
                    } catch (_: Exception) {}
                }

                if (html != null) {
                    val injected = loadPluginHtml(id, html!!)
                    output.write("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: ${injected.toByteArray().size}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n".toByteArray())
                    output.write(injected.toByteArray())
                    output.flush()
                    return
                }
            } else if (cleanRelPath.isNotEmpty()) {
                val file = File(pluginRoot, cleanRelPath).canonicalFile
                var bytes: ByteArray? = null
                if (PluginManager.BUILTIN_PLUGIN_IDS.contains(id)) {
                    try {
                        appContext?.assets?.open("plugins/$id/$cleanRelPath")?.use {
                            bytes = it.readBytes()
                        }
                    } catch (_: Exception) {}
                }
                if (bytes == null && file.toPath().startsWith(pluginRoot.toPath()) && file.exists() && file.isFile) {
                    bytes = file.readBytes()
                } else if (bytes == null) {
                    try {
                        appContext?.assets?.open("plugins/$id/$cleanRelPath")?.use {
                            bytes = it.readBytes()
                        }
                    } catch (_: Exception) {}
                }

                if (bytes != null) {
                    val mime = when {
                        cleanRelPath.endsWith(".png", true) -> "image/png"
                        cleanRelPath.endsWith(".jpg", true) || cleanRelPath.endsWith(".jpeg", true) -> "image/jpeg"
                        cleanRelPath.endsWith(".gif", true) -> "image/gif"
                        cleanRelPath.endsWith(".webp", true) -> "image/webp"
                        cleanRelPath.endsWith(".svg", true) -> "image/svg+xml"
                        cleanRelPath.endsWith(".css", true) -> "text/css"
                        cleanRelPath.endsWith(".js", true) -> "application/javascript"
                        cleanRelPath.endsWith(".json", true) -> "application/json"
                        cleanRelPath.endsWith(".woff2", true) -> "font/woff2"
                        cleanRelPath.endsWith(".ttf", true) -> "font/ttf"
                        else -> "application/octet-stream"
                    }
                    output.write("HTTP/1.1 200 OK\r\nContent-Type: $mime\r\nContent-Length: ${bytes!!.size}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n".toByteArray())
                    output.write(bytes!!)
                    output.flush()
                    return
                }
            }
            respond(output, 404, toJson(mapOf("error" to "not found")), "application/json")
            return
        }

        respond(output, 404, toJson(mapOf("error" to "not found")), "application/json")
    } catch (e: Exception) {
        android.util.Log.e("V07Server", "handlePluginRoute error: ${e.message}", e)
        try {
            respond(output, 500, toJson(mapOf("error" to (e.message ?: "unknown"))), "application/json")
        } catch (_: Exception) {}
    }
}

/** Inject the V07 plugin bridge bootstrap into a plugin's index.html before serving.
 *  Plugins load inside a same-origin iframe. Android's addJavascriptInterface is not
 *  reachable directly inside the iframe, but the parent frame (dashboard) has the
 *  AndroidV07 interface. Since both frames are same-origin (127.0.0.1:8000),
 *  the plugin accesses the interface synchronously via window.parent.AndroidV07. */
private fun loadPluginHtml(id: String, html: String): String {
    val pluginId = toJson(id)
    val featureJson = toJson(PluginManager.featureMap(id)).ifBlank { "{}" }
    val bridgeScript = """
<script>
// V07 plugin bridge (runs inside the plugin iframe).
// The dashboard parent frame owns the AndroidV07 JS interface (added via
// addJavascriptInterface). Both frames are same-origin, so the plugin reaches it
// directly through window.parent.AndroidV07. Calls are synchronous and return
// the JSON result string, which we parse and return as the V07.* return value.
(function(){
  function v07call(method, arg) {
    try {
      var target = window.parent || window;
      var res = target.AndroidV07.call(method, JSON.stringify(arg || null));
      if (res == null || res === '') return null;
      return JSON.parse(res);
    } catch(e) { return null; }
  }
  function v07call2(method, args) {
    // args is already an object to serialize
    try {
      var target = window.parent || window;
      var res = target.AndroidV07.call(method, JSON.stringify(args || null));
      if (res == null || res === '') return null;
      return JSON.parse(res);
    } catch(e) { return null; }
  }
  window.V07 = {
    readEvents:    function(cat, date){ return v07call2('readEvents',    {category:cat, date:date||null}); },
    readEventsAll: function(cat){ return v07call2('readEventsAll', {category:cat}); },
    writeEvent:    function(cat, data){ return v07call2('writeEvent',    {category:cat, data:data||{}}); },
    updateEvent:   function(cat, id, fields){ return v07call2('updateEvent', {category:cat, id:id, fields:fields||{}}); },
    deleteEvent:   function(cat, id){ return v07call2('deleteEvent', {category:cat, id:id}); },
    readChecklist: function(date){ return v07call2('readChecklist', {date:date||null}); },
    readSessions:  function(date){ return v07call2('readSessions',  {date:date||null}); },
    readUsage:     function(date){ return v07call2('readUsage',     {date:date||null}); },
    getTheme:      function(){ return v07call2('getTheme', {}); },
    onThemeChange: function(cb){ window._v07ThemeCb = cb; window._v07ThemeCbWrap = function(payload){ try{ cb(payload&&payload.t?payload.t:null); }catch(e){} }; },
    toast:         function(msg){ v07call2('toast', {message:msg}); },
    api:           function(method2, path, data){ return v07call2('api', {method:method2, path:path, data:data||null}); },
    canUseReminders: function(){ return !!(V07.features && V07.features.notifications === true); },
    scheduleReminder: function(opts){ return v07call2('scheduleReminder', Object.assign({pluginId: V07.pluginId}, opts||{})); },
    cancelReminder:   function(id){ return v07call2('cancelReminder', {pluginId: V07.pluginId, id: id}); },
    canUseAI:         function(){ return !!(V07.features && V07.features.ai !== false); },
    ai: {
      isAvailable: async function(){
        try {
          var r = await fetch('/ai/status', { headers: { 'X-Plugin-Id': ${pluginId} } });
          var j = await r.json();
          return !!(j && j.configured && j.hasAccess !== false);
        } catch(e){ return false; }
      },
      getStatus: async function(){
        try {
          var r = await fetch('/ai/status', { headers: { 'X-Plugin-Id': ${pluginId} } });
          return await r.json();
        } catch(e){ return { ok: false, configured: false, error: e.message }; }
      },
      generateText: async function(opts){
        if (typeof opts === 'string') opts = { prompt: opts };
        opts = opts || {};
        try {
          var res = await fetch('/ai/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-Plugin-Id': ${pluginId} },
            body: JSON.stringify({
              prompt: opts.prompt || '',
              systemInstruction: opts.systemInstruction || null,
              model: opts.model || null,
              temperature: opts.temperature != null ? opts.temperature : null,
              maxTokens: opts.maxTokens != null ? opts.maxTokens : null,
              responseFormat: 'text'
            })
          });
          return await res.json();
        } catch(e){ return { ok: false, error: e.message }; }
      },
      generateJson: async function(opts){
        opts = opts || {};
        try {
          var res = await fetch('/ai/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-Plugin-Id': ${pluginId} },
            body: JSON.stringify({
              prompt: opts.prompt || '',
              systemInstruction: opts.systemInstruction || null,
              model: opts.model || null,
              temperature: opts.temperature != null ? opts.temperature : null,
              maxTokens: opts.maxTokens != null ? opts.maxTokens : null,
              responseFormat: 'json',
              jsonSchema: opts.schema || opts.jsonSchema || null
            })
          });
          return await res.json();
        } catch(e){ return { ok: false, error: e.message }; }
      },
      chat: async function(opts){
        opts = opts || {};
        try {
          var res = await fetch('/ai/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-Plugin-Id': ${pluginId} },
            body: JSON.stringify({
              messages: opts.messages || [],
              systemInstruction: opts.systemInstruction || null,
              model: opts.model || null,
              temperature: opts.temperature != null ? opts.temperature : null
            })
          });
          return await res.json();
        } catch(e){ return { ok: false, error: e.message }; }
      },
      summarize: async function(content, instruction){
        var text = typeof content === 'string' ? content : JSON.stringify(content);
        return this.generateText({
          prompt: "Summarize the following clearly and concisely:\n\n" + text,
          systemInstruction: instruction || "You are an executive assistant creating concise, structured summaries."
        });
      },
      call: async function(path, data){
        var p = (path.startsWith('/') ? '' : '/') + path;
        try {
          var res = await fetch('/ai' + p, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-Plugin-Id': ${pluginId} },
            body: JSON.stringify(data || {})
          });
          return await res.json();
        } catch(e){ return { ok: false, error: e.message }; }
      }
    },
    weekly: {
      aggregate: async function(opts){
        opts = opts || {};
        var q = [];
        if (opts.start) q.push('start=' + encodeURIComponent(opts.start));
        if (opts.end) q.push('end=' + encodeURIComponent(opts.end));
        if (opts.offset != null) q.push('offset=' + encodeURIComponent(opts.offset));
        var url = '/api/weekly-summary/aggregate' + (q.length ? '?' + q.join('&') : '');
        try {
          var r = await fetch(url, { headers: { 'X-Plugin-Id': ${pluginId} } });
          return await r.json();
        } catch(e){ return { ok: false, error: e.message }; }
      },
      generate: async function(opts){
        try {
          var r = await fetch('/api/weekly-summary/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-Plugin-Id': ${pluginId} },
            body: JSON.stringify(opts || {})
          });
          return await r.json();
        } catch(e){ return { ok: false, error: e.message }; }
      },
      saveToVault: async function(opts){
        try {
          var r = await fetch('/api/weekly-summary/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-Plugin-Id': ${pluginId} },
            body: JSON.stringify(opts || {})
          });
          return await r.json();
        } catch(e){ return { ok: false, error: e.message }; }
      }
    },
    pluginId: ${pluginId},
    features:      ${featureJson},
    mode:          (function(){ try{ return (/\bv07mode=(runner|page)/.exec(location.search)||[])[1]||'page'; }catch(e){ return 'page'; } })()
  };
})();
</script>
""".trimIndent()
    val titleTag = html.indexOf("</head>")
    return if (titleTag >= 0) {
        html.substring(0, titleTag) + bridgeScript + "\n" + html.substring(titleTag)
    } else {
        bridgeScript + "\n" + html
    }
}


private fun handleNetworkInfo(): String {
    return try {
        val ips = mutableListOf<String>()
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            val addrs = networkInterface.inetAddresses
            while (addrs.hasMoreElements()) {
                val addr = addrs.nextElement()
                if (addr is java.net.Inet4Address && !addr.isLoopbackAddress) {
                    ips.add(addr.hostAddress ?: "")
                }
            }
        }
        val ip = ips.firstOrNull() ?: "127.0.0.1"
        val url = "http://$ip:8000"
        jsonOk(mapOf("ip" to ip, "url" to url, "ips" to ips))
    } catch (e: Exception) {
        jsonOk(mapOf("ip" to "127.0.0.1", "url" to "http://127.0.0.1:8000", "error" to (e.message ?: "")))
    }
}

private fun handleQR(params: Map<String, String>): Triple<Int, String, String> {
    val text = params["text"] ?: return Triple(400, jsonOk(mapOf("error" to "missing text param")), "application/json")
    return try {
        val matrix = com.google.zxing.qrcode.QRCodeWriter().encode(
            text, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400
        )
        val sb = StringBuilder()
        val w = matrix.width
        val h = matrix.height
        val scale = 8
        val svgW = w * scale
        val svgH = h * scale
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' width='$svgW' height='$svgH' viewBox='0 0 $svgW $svgH'>")
        sb.append("<rect width='$svgW' height='$svgH' fill='white'/>")
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (matrix.get(x, y)) {
                    sb.append("<rect x='${x * scale}' y='${y * scale}' width='$scale' height='$scale' fill='black'/>")
                }
            }
        }
        sb.append("</svg>")
        Triple(200, sb.toString(), "image/svg+xml")
    } catch (e: Exception) {
        Triple(500, jsonOk(mapOf("error" to e.message)), "application/json")
    }
}

internal fun parseTs(ts: String): java.time.OffsetDateTime? {
    return try { java.time.OffsetDateTime.parse(ts) } catch (_: Exception) {
        try { java.time.OffsetDateTime.of(java.time.LocalDateTime.parse(ts, DateTimeFormatter.ISO_LOCAL_DATE_TIME), java.time.ZoneOffset.UTC) } catch (_: Exception) { null }
    }
}

private fun handleSleepUp(): String {
    return try {
        val now = java.time.OffsetDateTime.now()
        val metrics = readEventsAll("metrics")
        val checkins = metrics.filter { it.type == "checkin" }.sortedBy { it.ts }
        val checkouts = metrics.filter { it.type == "checkout" }.sortedBy { it.ts }

        var lastCheckin: Event? = null
        var lastCheckout: Event? = null
        var upMinutes = 0
        var lastSleepMinutes = 0
        var status = "unknown"

        if (checkins.isNotEmpty()) {
            lastCheckin = checkins.last()
            val checkinDt = parseTs(lastCheckin.ts ?: "")
            if (checkinDt != null) {
                val hoursSinceCheckin = java.time.Duration.between(checkinDt, now).toHours()

                if (hoursSinceCheckin > 24) {
                    status = "expired"
                } else {
                    status = "checked_in"
                    val checkoutForPair = checkouts.lastOrNull { entry ->
                        val cdt = parseTs(entry.ts ?: "") ?: return@lastOrNull false
                        cdt.isAfter(checkinDt)
                    }
                    if (checkoutForPair != null) {
                        lastCheckout = checkoutForPair
                        val checkoutDt = parseTs(checkoutForPair.ts ?: "") ?: now
                        upMinutes = java.time.Duration.between(checkinDt, checkoutDt).toMinutes().toInt()
                        status = "checked_out"
                    } else {
                        upMinutes = java.time.Duration.between(checkinDt, now).toMinutes().toInt()
                    }
                }
            }
        }

        // Sleep = time from last checkout to last checkin (person was asleep)
        if (lastCheckin != null) {
            val checkinDt = parseTs(lastCheckin.ts ?: "")
            if (checkinDt != null) {
                val prevCheckout = checkouts.lastOrNull { entry ->
                    val cdt = parseTs(entry.ts ?: "") ?: return@lastOrNull false
                    cdt.isBefore(checkinDt)
                }
                if (prevCheckout != null) {
                    val prevCheckoutDt = parseTs(prevCheckout.ts ?: "")
                if (prevCheckoutDt != null) {
                    val gapMinutes = java.time.Duration.between(prevCheckoutDt, checkinDt).toMinutes().toInt().coerceAtLeast(0)
                    lastSleepMinutes = if (gapMinutes <= 24 * 60) gapMinutes else 0
                }
                }
            }
        }

        val fmtDuration: (Int) -> String = { mins ->
            if (mins >= 60) "${mins / 60}h ${mins % 60}m" else "${mins}m"
        }

        toJson(mapOf(
            "status" to status,
            "checkin_ts" to (lastCheckin?.ts ?: ""),
            "checkout_ts" to (lastCheckout?.ts ?: ""),
            "up_minutes" to upMinutes,
            "up_formatted" to fmtDuration(upMinutes),
            "sleep_minutes" to lastSleepMinutes,
            "sleep_formatted" to fmtDuration(lastSleepMinutes)
        ))
    } catch (e: Exception) {
        toJson(mapOf("status" to "unknown", "up_minutes" to 0, "up_formatted" to "--", "sleep_minutes" to 0, "sleep_formatted" to "--"))
    }
}

// === Original helper functions ===

fun softDeleteEvent(category: String, eventId: String): Boolean {
    val f = catFile(category)
    if (!f.exists()) return false
    return withDoubleLock(catLock(category), recycleLock()) {
        val lines = f.readLines().toMutableList()
        val found = lines.indexOfFirst { line ->
            Regex("\"id\"\\s*:\\s*\"${Regex.escape(eventId)}\"").containsMatchIn(line)
        }
        if (found < 0) return@withDoubleLock false
        val rawLine = lines[found]
        lines.removeAt(found)
        f.writeText(lines.joinToString("\n") + (if (lines.isNotEmpty()) "\n" else ""))
        val recycle = readRecycle().toMutableList()
        recycle.add(RecycleEntry(
            recycle_id = "r_${Random.nextLong().toString(36).take(8)}",
            category = category,
            data = mapOf("_raw" to rawLine, "id" to eventId),
            deleted_at = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        ))
        writeRecycle(recycle)
        true
    }
}

fun softDeleteTask(taskId: String, task: Event) {
    withLock(recycleLock()) {
        val recycle = readRecycle().toMutableList()
        val snapshot = mapOf(
            "id" to task.id, "date" to (task.date ?: ""), "ts" to (task.ts ?: ""),
            "title" to (task.title ?: ""), "text" to (task.text ?: ""),
            "status" to (task.status ?: ""), "type" to (task.type ?: ""),
            "category" to (task.category ?: ""), "activity" to (task.activity ?: ""),
            "on" to (task.on ?: false), "mood" to (task.mood ?: ""), "energy" to (task.energy ?: ""),
            "deadline" to (task.deadline ?: ""), "priority" to (task.priority ?: ""),
            "time" to (task.time ?: ""), "topic" to (task.topic ?: ""),
            "subtype" to (task.subtype ?: ""), "pushups" to (task.pushups ?: 0),
            "doom_scroll" to (task.doom_scroll ?: 0)
        )
        recycle.add(RecycleEntry(
            recycle_id = "r_${Random.nextLong().toString(36).take(8)}",
            category = "tasks",
            data = mapOf("task_id" to taskId, "snapshot" to toJson(snapshot)),
            deleted_at = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        ))
        writeRecycle(recycle)
    }
}

fun getActiveSession(): Event? {
    val sessions = readEventsAll("sessions").sortedBy { ev -> parseTs(ev.ts ?: "")?.toInstant()?.toEpochMilli() ?: 0L }
    var active: Event? = null
    for (e in sessions) {
        when (e.type) {
            "start" -> active = e
            "end" -> active = null
        }
    }
    if (active != null) {
        val startDt = parseTs(active.ts ?: "") ?: return null
        if (java.time.Duration.between(startDt, java.time.OffsetDateTime.now()).toHours() > 24) return null
    }
    return active
}

fun getPausedSession(): Event? {
    val sessions = readEventsAll("sessions").sortedBy { ev -> parseTs(ev.ts ?: "")?.toInstant()?.toEpochMilli() ?: 0L }
    var lastStart: Event? = null
    for (e in sessions) {
        when (e.type) {
            "start" -> lastStart = e
            "end" -> {
                if (e.paused == true && lastStart != null) {
                    val startDt = parseTs(lastStart.ts ?: "")
                    if (startDt != null && java.time.Duration.between(startDt, java.time.OffsetDateTime.now()).toHours() > 24) return null
                    return lastStart
                }
                lastStart = null
            }
        }
    }
    return null
}

fun getDayEvents(dateStr: String): Map<String, Any?> {
    val cats = linkedMapOf(
        "mood" to "Mood", "energy" to "Energy", "metrics" to "Metrics", "habits" to "Habits",
        "journal" to "Journal", "thought" to "Thoughts", "dsa" to "DSA",
        "chess" to "Chess", "running" to "Running", "panics" to "Panics",
        "sessions" to "Sessions", "tasks" to "Tasks",
        "checklist" to "Checklist", "daytracker" to "Time Tracker",
        "shares" to "Time Capsule"
    )
    val dir = eventsDir()
    if (dir.exists()) {
        dir.listFiles { _, name -> name.endsWith(".jsonl") && !name.endsWith("_ui.jsonl") }?.forEach { f ->
            val base = f.name.removeSuffix(".jsonl")
            if (!cats.containsKey(base)) {
                cats[base] = base.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
    val events = mutableListOf<Map<String, Any?>>()
    val seenEventIds = mutableSetOf<String>()

    for ((cat, label) in cats) {
        if (cat == "daytracker") {
            val f = VaultConfig.dayTrackerFile
            if (f.exists()) {
                for (line in f.readLines().filter { it.isNotBlank() }) {
                    try {
                        val map = jsonToMap(line)
                        val d = (map["date"] as? String)?.takeIf { it.isNotBlank() } ?: (map["ts"] as? String)?.take(10) ?: ""
                        if (d != dateStr) continue
                        val id = (map["id"] as? String)?.takeIf { it.isNotBlank() } ?: "dt_${System.identityHashCode(map)}"
                        if (seenEventIds.add(id)) {
                            val m = map.toMutableMap()
                            m["_category"] = cat
                            m["_label"] = label
                            m["id"] = id
                            m["date"] = d
                            m["ts"] = map["ts"] ?: "${dateStr}T12:00:00"
                            m["title"] = map["title"] ?: ""
                            m["hour"] = map["hour"]
                            m["pinned"] = map["pinned"] ?: false
                            m["category"] = map["category"] ?: "others"
                            events.add(m)
                        }
                    } catch (_: Exception) {}
                }
            }
            continue
        }
        for (raw in eventsInRangeRaw(cat, dateStr, dateStr)) {
            val m = raw.toMutableMap()
            val id = (m["id"] as? String)?.takeIf { it.isNotBlank() } ?: "${cat}_${System.identityHashCode(raw)}"
            if (seenEventIds.add(id)) {
                m["_category"] = cat
                m["_label"] = label
                m["id"] = id
                val d = (m["date"] as? String)?.takeIf { it.isNotBlank() } ?: (m["ts"] as? String)?.take(10) ?: dateStr
                m["date"] = d
                if (!m.containsKey("ts") || (m["ts"] as? String).isNullOrEmpty()) {
                    val timeVal = m["time"] as? String
                    m["ts"] = if (!timeVal.isNullOrEmpty() && timeVal.length == 5) "${dateStr}T${timeVal}:00" else "${dateStr}T12:00:00"
                }
                if (cat == "running") {
                    val rawSec = (m["duration_sec"] as? Number)?.toInt()
                    val dist = (m["distance_km"] as? Number)?.toDouble() ?: 0.0
                    val sec = if (rawSec != null && rawSec > 0) {
                        rawSec
                    } else {
                        val raw = (m["duration_min"] as? Number)?.toDouble()
                            ?: (m["duration"] as? Number)?.toDouble()
                            ?: (m["minutes"] as? Number)?.toDouble()
                            ?: (m["duration_min"] as? String)?.toDoubleOrNull()
                            ?: (m["duration_sec"] as? String)?.toDoubleOrNull()
                            ?: 0.0
                        val isSec = if (dist > 0.0) (raw / dist > 20.0) else (raw >= 100.0)
                        if (isSec) Math.round(raw).toInt() else Math.round(raw * 60.0).toInt()
                    }
                    val min = Math.round(sec / 60.0).toInt()
                    val mm = sec / 60
                    val ss = sec % 60
                    m["duration_sec"] = sec
                    m["duration_min"] = min
                    m["duration_minutes"] = min
                    m["minutes"] = min
                    m["duration_formatted"] = if (ss > 0) "${mm}m ${ss}s" else "${mm}m"
                }
                events.add(m)
            }
        }
    }
    events.sortBy { (it["ts"] as? String) ?: "" }
    return mapOf("date" to dateStr, "events" to events)
}

fun formatTodayStats(): String {
    return formatDayStats(LocalDate.now().toString())
}

fun formatDayStats(t: String): String {
    val lines = mutableListOf("*$t*")
    val moodEvents = eventsInRange("mood", t, t)
    val latestMood = moodEvents.lastOrNull { it.mood != null }?.mood ?: "—"
    val latestEnergy = moodEvents.lastOrNull { it.energy != null }?.energy ?: "—"
    lines.add("*Mood:* $latestMood")
    lines.add("*Energy:* $latestEnergy")
    val metrics = eventsInRange("metrics", t, t)
    val pushups = metrics.sumOf { it.pushups ?: 0 }
    val doom = metrics.sumOf { it.doom_scroll ?: 0 }
    if (pushups > 0) lines.add("*Pushups:* $pushups/50")
    if (doom > 0) lines.add("*Doom scroll:* ${doom}m")
    val ci = metrics.lastOrNull { it.type == "checkin" }
    val co = metrics.lastOrNull { it.type == "checkout" }
    if (ci != null) lines.add("*Check-in:* ${ci.time ?: "?"}")
    if (co != null) lines.add("*Check-out:* ${co.time ?: "?"}")
    val habits = eventsInRange("habits", t, t).filter { it.on == true }
    if (habits.isNotEmpty()) {
        lines.add("*Activities:* ${habits.size}")
        for (h in habits) {
            lines.add("  ${ACTIVITY_LABELS[h.activity] ?: h.activity ?: "?"}")
        }
    }
    val journals = eventsInRange("journal", t, t)
    for (j in journals) {
        when (j.subtype) {
            "win" -> lines.add("*Win:* ${j.text ?: ""}")
            "lesson" -> lines.add("*Lesson:* ${j.text ?: ""}")
        }
    }
    return lines.joinToString("\n")
}

fun logQuery(category: String, value: String, vararg extras: Pair<String, String?>): Map<String, Any?> {
    val data = mutableMapOf<String, Any>(category to value)
    for ((k, v) in extras) { if (v != null && v.isNotEmpty()) data[k] = v }
    appendEvent(category, data)
    return mapOf("ok" to true)
}

fun logQuery(category: String, value: String, extras: Map<String, String?>): Map<String, Any?> {
    val data = mutableMapOf<String, Any>(category to value)
    for ((k, v) in extras) { if (v != null && v.isNotEmpty()) data[k] = v }
    appendEvent(category, data)
    return mapOf("ok" to true)
}

fun getNotifyStatus(): Map<String, Any?> {
    val nowMs = System.currentTimeMillis()
    val idle = if (lastRequestTime > 0) {
        val mins = ((nowMs - lastRequestTime) / 60000).toInt()
        mapOf("idle" to (mins > 240), "minutes" to mins, "hours" to "%.1f".format(mins / 60.0))
    } else {
        val now = OffsetDateTime.now()
        val allCats = listOf("mood", "energy", "habits", "journal", "thought", "metrics", "checkin", "sessions", "tasks", "dsa", "chess", "running", "panics", "shares")
        val latestTs = allCats.mapNotNull { cat -> readEventsAll(cat).lastOrNull()?.ts }.maxOrNull()
        if (latestTs != null) {
            val latestDt = parseTs(latestTs)
            if (latestDt != null) {
                val mins = java.time.Duration.between(latestDt, now).toMinutes().toInt()
                mapOf("idle" to (mins > 240), "minutes" to mins, "hours" to "%.1f".format(mins / 60.0))
            } else mapOf("idle" to false, "minutes" to null, "hours" to null)
        } else mapOf("idle" to false, "minutes" to null, "hours" to null)
    }
    val now = OffsetDateTime.now()
    val tasks = rebuildTaskState()
    val today = LocalDate.now().toString()
    val overdue = tasks.filter { it.status != "done" && (it.deadline ?: "9999") < today }
    val dueToday = tasks.filter { it.status != "done" && it.deadline == today }
    val timePriority = tasks.filter { it.status != "done" && it.deadline == today && it.time != null }.mapNotNull { task ->
        try {
            val parts = task.time!!.split(":")
            val taskMins = parts[0].toInt() * 60 + parts[1].toInt()
            val curMins = now.hour * 60 + now.minute
            val diff = taskMins - curMins
            when {
                diff in 55..65 -> mapOf("id" to task.id, "title" to task.title, "time" to task.time, "trigger" to "60min")
                diff in 0..6 -> mapOf("id" to task.id, "title" to task.title, "time" to task.time, "trigger" to "1min")
                else -> null
            }
        } catch (_: Exception) { null }
    }
    val overall = computeStreak(readEventsAll("habits")) { it.on == true }
    return mapOf(
        "idle" to idle,
        "deadlines" to mapOf("overdue_count" to overdue.size, "due_today_count" to dueToday.size),
        "streak" to mapOf("overall" to overall, "milestone" to (overall > 0 && overall % 7 == 0)),
        "time_priority" to timePriority,
        "digest" to mapOf("overdue_count" to overdue.size, "due_today_count" to dueToday.size, "upcoming_count" to tasks.count { it.status != "done" && (it.deadline ?: "0000") > today }, "pending_total" to (overdue.size + dueToday.size))
    )
}

fun getNotifyDailyVerse(): Map<String, Any?> {
    val verse = getSequentialVerse()
    if (verse == null) return mapOf("error" to "No Gita data")
    return mapOf(
        "verse_id" to verse["id"], "chapter" to verse["chapter"],
        "verse" to verse["verse"], "shloka" to (verse["shloka_text"] ?: ""),
        "english" to (verse["english"] ?: ""), "hindi" to (verse["hindi"] ?: ""),
        "already_read" to false
    )
}

private val manifestData = mapOf(
    "name" to "V07 Dashboard", "short_name" to "V07",
    "start_url" to "/", "display" to "standalone",
    "background_color" to "#0e0e10", "theme_color" to "#7c5bf5",
    "description" to "V07 Dashboard",
    "icons" to listOf(
        mapOf("src" to "/icon?size=192", "sizes" to "192x192", "type" to "image/png"),
        mapOf("src" to "/icon?size=512", "sizes" to "512x512", "type" to "image/png")
    )
)

private fun loadIndexHtml(): String {
    try {
        val ctx = appContext
        if (ctx != null) {
            val stream = try { ctx.assets.open("v3/index.html") } catch (_: Exception) { ctx.assets.open("index.html") }
            return String(stream.readBytes())
        }
    } catch (_: Exception) {}
    try {
        val assetFile = File(VaultConfig.filesDir, "index.html")
        if (assetFile.exists()) return assetFile.readText()
    } catch (_: Exception) {}
    return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/><title>V07 Dashboard</title></head><body><h1>V07 Server Running</h1></body></html>"
}

private fun loadAssetHtml(filename: String): String {
    try {
        val ctx = appContext
        if (ctx != null) {
            val stream = try { ctx.assets.open("v3/$filename") } catch (_: Exception) { ctx.assets.open(filename) }
            return String(stream.readBytes())
        }
    } catch (_: Exception) {}
    try {
        val assetFile = File(VaultConfig.filesDir, filename)
        if (assetFile.exists()) return assetFile.readText()
    } catch (_: Exception) {}
    return loadIndexHtml()
}

private val serviceWorkerJs: String = """const CACHE = "v07-v4";
const STATIC_URLS = ["/", "/manifest.json", "/icon?size=192"];
self.addEventListener("install", (e) => {
  self.skipWaiting();
  e.waitUntil(caches.open(CACHE).then(cache => cache.addAll(STATIC_URLS))); });
self.addEventListener("activate", (e) => {
  e.waitUntil(Promise.all([
    caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))),
    self.clients.claim()
  ]));});
self.addEventListener("fetch", (e) => {
  const url = new URL(e.request.url);
  if (url.origin !== location.origin) return;
  if (url.pathname === "/" || url.pathname === "/manifest.json") {
    e.respondWith(
      fetch(e.request).then(response => {
        return caches.open(CACHE).then(cache => {
          cache.put(e.request, response.clone());
          return response;
        });
      }).catch(() => caches.match(e.request))
    );
  } else {
    e.respondWith(fetch(e.request).catch(() => new Response("Offline", {status: 503})));
  }
});"""

private fun readTags(): List<Map<String, Any?>> {
    val f = VaultConfig.tagsFile
    if (!f.exists()) return emptyList()
    return try {
        val body = f.readText().trim()
        if (body.isEmpty()) emptyList()
        else {
            val arr = org.json.JSONArray(body)
            (0 until arr.length()).map { i -> jsonToMap(arr.getJSONObject(i).toString()) }
        }
    } catch (_: Exception) { emptyList() }
}

private fun writeTags(tags: List<Map<String, Any?>>) {
    val arr = org.json.JSONArray()
    for (tag in tags) arr.put(org.json.JSONObject(toJson(tag)))
    VaultConfig.tagsFile.writeText(arr.toString())
}

private fun handleTagsRoute(method: String, path: String, params: Map<String, String>, body: String): String {
    val builtInTags = listOf("URL","Image","Video","Audio","PDF").map { mapOf("id" to "_$it", "name" to it) }
    return when (method) {
        "GET" -> jsonOk(mapOf("tags" to (builtInTags + readTags())))
        "POST" -> {
            val jsonBody = if (body.isNotEmpty()) jsonToMap(body) else emptyMap()
            val name = (jsonBody["name"] as? String)?.trim()?.takeIf { it.isNotEmpty() }
                ?: return jsonOk(mapOf("ok" to false, "error" to "missing name"))
            val tags = readTags().toMutableList()
            if (tags.any { (it["name"] as? String)?.lowercase() == name.lowercase() }) {
                return jsonOk(mapOf("ok" to false, "error" to "tag exists"))
            }
            val id = "tg_${System.currentTimeMillis().toString(36)}"
            val newTag = mapOf("id" to id, "name" to name)
            tags.add(newTag)
            writeTags(tags)
            jsonOk(mapOf("ok" to true, "tag" to newTag))
        }
        "DELETE" -> {
            val id = params["id"] ?: return jsonOk(mapOf("ok" to false, "error" to "missing id"))
            val tags = readTags().toMutableList()
            val removed = tags.removeAll { it["id"] == id }
            if (removed) { writeTags(tags); jsonOk(mapOf("ok" to true)) }
            else jsonOk(mapOf("ok" to false, "error" to "not found"))
        }
        else -> jsonOk(mapOf("error" to "method not allowed"))
    }
}

private fun handleSharesRoute(method: String, path: String, params: Map<String, String>): String {
    val parts = path.split("/").drop(1)
    return when {
        method == "GET" && parts.lastOrNull() == "shares" -> {
            val search = params["search"]?.lowercase() ?: ""
            val tag = params["tag"] ?: ""
            val showDeleted = params["deleted"]?.toBoolean() == true
            var shares: List<Map<String, Any?>>
            if (showDeleted) {
                shares = listRecycleEntries("shares").map { re ->
                    val raw = re.data["_raw"] ?: ""
                    try {
                        val ev = json.decodeFromString<Event>(raw)
                        mapOf(
                            "id" to (ev.id ?: ""), "date" to (ev.date ?: ""), "ts" to (ev.ts ?: ""),
                            "title" to (ev.title ?: ""), "info" to (ev.info ?: ""),
                            "type" to (ev.type ?: ""), "text" to (ev.text ?: ""),
                            "tags" to (ev.tags ?: ""), "file_path" to (ev.file_path ?: ""),
                            "pinned" to (ev.pinned ?: false), "location" to (ev.location ?: ""),
                            "captured_time" to (ev.captured_time ?: ""), "recycle_id" to re.recycle_id
                        )
                    } catch (_: Exception) {
                        mapOf("id" to (re.data["id"] ?: ""), "title" to "(deleted)", "type" to "", "recycle_id" to re.recycle_id)
                    }
                }
            } else {
                shares = readEventsAll("shares").map { e ->
                    mapOf(
                        "id" to (e.id ?: ""), "date" to (e.date ?: ""), "ts" to (e.ts ?: ""),
                        "title" to (e.title ?: ""), "info" to (e.info ?: ""),
                        "type" to (e.type ?: ""), "text" to (e.text ?: ""),
                        "tags" to (e.tags ?: ""), "file_path" to (e.file_path ?: ""),
                        "pinned" to (e.pinned ?: false), "location" to (e.location ?: ""),
                        "captured_time" to (e.captured_time ?: "")
                    )
                }
            }
            if (search.isNotEmpty()) {
                shares = shares.filter {
                    (it["title"] as? String ?: "").lowercase().contains(search) ||
                    (it["info"] as? String ?: "").lowercase().contains(search) ||
                    (it["text"] as? String ?: "").lowercase().contains(search) ||
                    (it["location"] as? String ?: "").lowercase().contains(search)
                }
            }
            if (tag.isNotEmpty()) {
                shares = shares.filter {
                    (it["tags"] as? String ?: "").split(",").map { t -> t.trim() }.contains(tag)
                }
            }
            shares = shares.sortedWith(
                compareByDescending<Map<String, Any?>> { it["pinned"] == true }
                    .thenByDescending { it["ts"] as? String ?: "" }
            )
            jsonOk(mapOf("shares" to shares))
        }
        method == "POST" && parts.lastOrNull() == "pin" -> {
            val id = params["id"] ?: return jsonOk(mapOf("ok" to false, "error" to "missing id"))
            val events = readEventsAll("shares")
            val ev = events.find { it.id == id } ?: return jsonOk(mapOf("ok" to false, "error" to "not found"))
            val newPinned = ev.pinned != true
            updateEventField("shares", id, mapOf("pinned" to newPinned))
            jsonOk(mapOf("ok" to true, "pinned" to newPinned))
        }
        method == "POST" && parts.lastOrNull() == "restore" -> {
            val recycleId = params["id"] ?: return jsonOk(mapOf("ok" to false, "error" to "missing id"))
            val restored = restoreRecycleEntry(recycleId)
            jsonOk(mapOf("ok" to (restored != null)))
        }
        method == "POST" && parts.lastOrNull() == "update" -> {
            val id = params["id"] ?: return jsonOk(mapOf("ok" to false, "error" to "missing id"))
            val fields = mutableMapOf<String, Any?>()
            for (key in listOf("title", "info", "tags", "text", "date", "captured_time", "location")) {
                val v = params[key]
                if (v != null) fields[key] = v
            }
            if (fields.isEmpty()) return jsonOk(mapOf("ok" to false, "error" to "no fields to update"))
            updateEventField("shares", id, fields)
            jsonOk(mapOf("ok" to true))
        }
        method == "DELETE" -> {
            val id = params["id"] ?: return jsonOk(mapOf("ok" to false, "error" to "missing id"))
            val permanent = params["permanent"]?.toBoolean() == true
            if (permanent) {
                deleteEvent("shares", id)
            } else {
                softDeleteEvent("shares", id)
            }
            jsonOk(mapOf("ok" to true))
        }
        method == "DELETE" && parts.lastOrNull() == "permanent" -> {
            val recycleId = params["id"] ?: return jsonOk(mapOf("ok" to false, "error" to "missing id"))
            withLock(recycleLock()) {
                val entries = readRecycle().toMutableList()
                val idx = entries.indexOfFirst { it.recycle_id == recycleId }
                if (idx >= 0) {
                    val removed = entries.removeAt(idx)
                    val raw = removed.data["_raw"]
                    if (raw != null) {
                        try {
                            val ev = json.decodeFromString<Event>(raw)
                            deleteEvent("shares", ev.id)
                        } catch (_: Exception) {}
                    }
                    writeRecycle(entries)
                    jsonOk(mapOf("ok" to true))
                } else {
                    jsonOk(mapOf("ok" to false, "error" to "not found in recycle bin"))
                }
            }
        }
        else -> jsonOk(mapOf("error" to "not found"))
    }
}
