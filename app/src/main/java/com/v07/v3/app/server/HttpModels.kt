package com.v07.v3.app.server

import java.io.OutputStream
import java.nio.charset.StandardCharsets

data class HttpRequest(
    val method: String,
    val path: String,
    val queryParams: Map<String, String>,
    val headers: Map<String, String>,
    val body: String
) {
    fun getHeader(name: String): String? {
        return headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
    }
}

data class HttpResponse(
    val statusCode: Int = 200,
    val statusText: String = "OK",
    val contentType: String = "application/json; charset=utf-8",
    val headers: Map<String, String> = emptyMap(),
    val bodyBytes: ByteArray = ByteArray(0)
) {
    companion object {
        fun json(jsonString: String, statusCode: Int = 200): HttpResponse {
            val bytes = jsonString.toByteArray(StandardCharsets.UTF_8)
            return HttpResponse(
                statusCode = statusCode,
                statusText = if (statusCode == 200) "OK" else if (statusCode == 201) "Created" else "Status $statusCode",
                contentType = "application/json; charset=utf-8",
                bodyBytes = bytes
            )
        }

        fun error(message: String, statusCode: Int = 400): HttpResponse {
            val json = """{"ok":false,"error":${quote(message)}}"""
            return json(json, statusCode)
        }

        fun success(payload: String = """{"ok":true}"""): HttpResponse {
            return json(payload, 200)
        }

        fun bytes(data: ByteArray, mimeType: String, statusCode: Int = 200, headers: Map<String, String> = emptyMap()): HttpResponse {
            return HttpResponse(
                statusCode = statusCode,
                statusText = "OK",
                contentType = mimeType,
                headers = headers,
                bodyBytes = data
            )
        }

        private fun quote(str: String): String {
            return "\"" + str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\""
        }
    }

    fun writeTo(outputStream: OutputStream, requestOrigin: String? = null) {
        val writer = outputStream.bufferedWriter(StandardCharsets.UTF_8)
        writer.write("HTTP/1.1 $statusCode $statusText\r\n")
        writer.write("Content-Type: $contentType\r\n")
        writer.write("Content-Length: ${bodyBytes.size}\r\n")
        writer.write("Connection: close\r\n")

        val allowOrigin = when {
            requestOrigin.isNullOrBlank() -> "http://127.0.0.1"
            requestOrigin.startsWith("http://127.0.0.1") ||
            requestOrigin.startsWith("http://localhost") ||
            requestOrigin == "https://v07-dashboard.web.app" ||
            requestOrigin == "https://v07-dashboard.firebaseapp.com" -> requestOrigin
            else -> "http://127.0.0.1"
        }
        writer.write("Access-Control-Allow-Origin: $allowOrigin\r\n")
        writer.write("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n")
        writer.write("Access-Control-Allow-Headers: Content-Type, X-V07-Token, Authorization\r\n")
        for ((k, v) in headers) {
            writer.write("$k: $v\r\n")
        }
        writer.write("\r\n")
        writer.flush()
        if (bodyBytes.isNotEmpty()) {
            outputStream.write(bodyBytes)
            outputStream.flush()
        }
    }
}
