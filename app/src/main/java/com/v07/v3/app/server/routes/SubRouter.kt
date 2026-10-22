package com.v07.v3.app.server.routes

import com.v07.v3.app.server.HttpRequest
import com.v07.v3.app.server.HttpResponse

/**
 * SubRouter interface for domain-specific REST route controllers.
 * Decouples monolithic ApiRouter into modular, testable handlers.
 */
interface SubRouter {
    /**
     * Returns true if this sub-router can process the incoming request path.
     */
    fun matches(path: String): Boolean

    /**
     * Handles the HTTP request for matching routes.
     */
    suspend fun handle(method: String, path: String, request: HttpRequest): HttpResponse
}
