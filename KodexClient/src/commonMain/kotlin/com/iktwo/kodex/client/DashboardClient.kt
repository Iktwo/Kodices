package com.iktwo.kodex.client

import com.iktwo.kodices.KodicesParser
import com.iktwo.kodices.content.Content
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Where a dashboard is served from: a KodexRenderer instance (see KodexRenderer/README.md), or any
 * server that answers `GET /screens` and `GET /content/{screen}.json` the same way.
 *
 * @property baseUrl e.g. `http://192.168.1.20:8090`.
 * @property token the server's bearer token, or null if it has none.
 */
data class DashboardConnection(
    val baseUrl: String,
    val token: String? = null,
) {
    /** Whether [url] is on this server: the token must never be sent anywhere else. */
    fun owns(url: Url): Boolean {
        val base = runCatching { Url(baseUrl) }.getOrNull() ?: return false
        return url.protocol == base.protocol && url.host == base.host && url.port == base.port
    }

    /** The token is a secret: keep it out of logs and crash reports. */
    override fun toString(): String = "DashboardConnection(baseUrl=$baseUrl, token=${if (token == null) "none" else "***"})"

    companion object {
        /** A connection from what a person typed: surrounding spaces and a trailing `/` do not count. */
        fun of(
            baseUrl: String,
            token: String?,
        ): DashboardConnection = DashboardConnection(baseUrl.trim().trimEnd('/'), token?.trim()?.ifBlank { null })
    }
}

/** The screens a server offers, from `GET /screens`. */
data class DashboardScreens(
    val defaultScreen: String,
    val screens: List<String>,
)

/** What went wrong, for a UI that reacts differently to each: ask for the token again, retry... */
enum class DashboardFailure {
    /** The address is not a URL. */
    InvalidAddress,

    /** No answer: the server is down, the address is wrong, or the network is. */
    Unreachable,

    /** `401`: the server needs a token, or the token is wrong. */
    Unauthorized,

    /** `404`: there is no such screen (or, for the screen list, no dashboard server there). */
    NotFound,

    /** `422`: the server has the screen, but its JSON is not valid. */
    InvalidScreen,

    /** Any other status, or an answer that is not what a dashboard server sends. */
    UnexpectedAnswer,
}

sealed interface DashboardResult {
    /**
     * @property etag pass it to the next [DashboardClient.fetch] so an unchanged screen is not
     *   downloaded and parsed again.
     * @property refreshSeconds how long the server asks clients to wait before asking again.
     */
    data class Loaded(
        val content: Content,
        val etag: String?,
        val refreshSeconds: Int,
    ) : DashboardResult

    /** The screen is the one the caller already has. */
    data object NotModified : DashboardResult

    data class Failed(
        val message: String,
        val failure: DashboardFailure = DashboardFailure.UnexpectedAnswer,
    ) : DashboardResult
}

/**
 * Fetches screens as JSON from a dashboard server and parses them, so an app renders the same
 * dashboard an e-ink device gets as a picture.
 *
 * The [httpClient] should have [DashboardAuth] installed: it adds the token, to this client's
 * requests and to the image requests Coil makes through the same client.
 */
class DashboardClient(
    private val httpClient: HttpClient,
    private val parser: KodicesParser,
) {
    /** @throws DashboardException if the server cannot be reached or does not answer as expected. */
    suspend fun screens(connection: DashboardConnection): DashboardScreens {
        val response = request(connection, null, "screens")
        if (response.status != HttpStatusCode.OK) throw DashboardException(describe(response), failureOf(response.status))

        return try {
            val json = Json.parseToJsonElement(response.body).jsonObject
            DashboardScreens(
                defaultScreen = json.getValue("default").jsonPrimitive.content,
                screens = json.getValue("screens").jsonArray.map { it.jsonPrimitive.content },
            )
        } catch (e: IllegalArgumentException) {
            throw DashboardException(NOT_A_SERVER)
        } catch (e: NoSuchElementException) {
            throw DashboardException(NOT_A_SERVER)
        }
    }

    /** Fetches [screen]. Failures are reported as [DashboardResult.Failed], never thrown. */
    suspend fun fetch(
        connection: DashboardConnection,
        screen: String,
        etag: String? = null,
    ): DashboardResult {
        val response = try {
            request(connection, etag, "content", "$screen.json")
        } catch (e: DashboardException) {
            return DashboardResult.Failed(e.message.orEmpty(), e.failure)
        }

        return when (response.status) {
            HttpStatusCode.NotModified -> DashboardResult.NotModified
            HttpStatusCode.OK -> parse(response)
            else -> DashboardResult.Failed(describe(response, screen), failureOf(response.status))
        }
    }

    /** The body is `{"content": ..., "data": ..., "refresh_seconds": ...}`. */
    private fun parse(response: Response): DashboardResult {
        val content = parser.parseJSONWithDataToContent(response.body)
            ?: return DashboardResult.Failed("The server sent a screen this app could not parse.")
        val refreshSeconds = runCatching {
            Json.parseToJsonElement(response.body).jsonObject["refresh_seconds"]?.jsonPrimitive?.intOrNull
        }.getOrNull()

        return DashboardResult.Loaded(
            content = content,
            etag = response.etag,
            refreshSeconds = (refreshSeconds ?: DEFAULT_REFRESH_SECONDS).coerceAtLeast(MIN_REFRESH_SECONDS),
        )
    }

    private class Response(
        val status: HttpStatusCode,
        val body: String,
        val etag: String?,
    )

    /** GETs a path under the connection's base URL. */
    private suspend fun request(
        connection: DashboardConnection,
        etag: String?,
        vararg path: String,
    ): Response {
        val url = try {
            URLBuilder(connection.baseUrl.trim()).appendPathSegments(*path).build()
        } catch (e: URLParserException) {
            throw DashboardException("'${connection.baseUrl}' is not a valid server address.", DashboardFailure.InvalidAddress)
        }

        return try {
            val response = httpClient.get(url) {
                etag?.let { header(HttpHeaders.IfNoneMatch, it) }
            }
            Response(response.status, response.bodyAsText(), response.headers[HttpHeaders.ETag])
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // Throwable, not Exception: in a browser Ktor reports a failed fetch (the server is down,
            // or the page's Content-Security-Policy forbids the address) as kotlin.Error, and a
            // rejected promise arrives as JsException. Neither is an Exception, so both escaped
            // and the connect form showed nothing at all.
            throw DashboardException(
                "Could not reach ${connection.baseUrl}: ${e.message ?: e::class.simpleName}",
                DashboardFailure.Unreachable,
            )
        }
    }

    private fun failureOf(status: HttpStatusCode): DashboardFailure =
        when (status) {
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> DashboardFailure.Unauthorized
            HttpStatusCode.NotFound -> DashboardFailure.NotFound
            HttpStatusCode.UnprocessableEntity -> DashboardFailure.InvalidScreen
            else -> DashboardFailure.UnexpectedAnswer
        }

    private fun describe(
        response: Response,
        screen: String? = null,
    ): String =
        when (response.status) {
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> "The server needs a token, or the token is wrong."
            HttpStatusCode.NotFound -> if (screen != null) "The server has no screen named '$screen'." else NOT_A_SERVER
            HttpStatusCode.UnprocessableEntity -> "The screen's JSON is not valid: ${response.body.take(MAX_ERROR_BODY)}"
            else -> "The server answered ${response.status.value}: ${response.body.take(MAX_ERROR_BODY)}"
        }

    companion object {
        const val DEFAULT_REFRESH_SECONDS = 900

        /** A floor, so a misconfigured server cannot make the app poll in a tight loop. */
        const val MIN_REFRESH_SECONDS = 5

        private const val MAX_ERROR_BODY = 200
        private const val NOT_A_SERVER = "That address did not answer like a dashboard server."
    }
}

class DashboardException(
    message: String,
    val failure: DashboardFailure = DashboardFailure.UnexpectedAnswer,
) : Exception(message)
