package com.example.rehabinsight.data.network

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

private const val TAG = "SERVER_LINK"

internal const val CONNECT_TIMEOUT_MILLIS = 5_000
internal const val READ_TIMEOUT_MILLIS = 10_000

/** Once the server can't be contacted, later requests are skipped for this long rather than each waiting out its own timeout. */
private const val RETRY_AFTER_NANOS = 30_000_000_000L

@Volatile
private var unreachableUntil = Long.MIN_VALUE

/** Who a request is made for, so the server can check they are allowed to make it. */
data class Caller(
    val sessionToken: String? = null,
    val adminId: Int? = null
)

/** Outcome of a call that stores something. [id] is the primary key the server gave a new row. */
data class WriteResult(
    val success: Boolean,
    val id: Int? = null,
    /** False when the request got no answer, as opposed to the server turning it down. */
    val serverReached: Boolean = true,
    val errorMessage: String? = null
)

internal data class ApiResponse(val code: Int, val body: String?) {
    val isSuccess: Boolean get() = code in 200..299
}

internal fun JSONObject.putCaller(caller: Caller): JSONObject = apply {
    caller.sessionToken?.let { put("session_token", it) }
    caller.adminId?.let { put("admin_id", it) }
}

/** JSONObject.put drops a key whose value is null; this sends an explicit null so the server clears the column. */
internal fun JSONObject.putNullable(key: String, value: Any?): JSONObject = put(key, value ?: JSONObject.NULL)

private fun serverRecentlyUnreachable(): Boolean = System.nanoTime() < unreachableUntil

/** Backs off only when the server can't be contacted at all; one dropped reply is no reason to. */
private fun noteFailure(error: Exception) {
    val cannotConnect = error is ConnectException || error is SocketTimeoutException ||
        error is UnknownHostException || error is NoRouteToHostException
    if (cannotConnect) unreachableUntil = System.nanoTime() + RETRY_AFTER_NANOS
}

/**
 * POSTs [json] the way sign-up does (see [postJson]), following the server's 307/308 redirects.
 * Returns null when the server could not be reached. [alwaysTry] is for requests someone is
 * waiting on, such as signing in.
 */
internal fun postForResponse(url: String, json: JSONObject, alwaysTry: Boolean = false): ApiResponse? {
    if (!alwaysTry && serverRecentlyUnreachable()) return null
    return try {
        var (connection, body) = postJson(url, json)
        var responseCode = connection.responseCode

        if (responseCode == 307 || responseCode == 308) {
            val redirectUrl = connection.getHeaderField("Location")
            connection.disconnect()
            if (redirectUrl != null) {
                val redirected = postJson(redirectUrl, json)
                connection = redirected.first
                body = redirected.second
                responseCode = connection.responseCode
            }
        }

        connection.disconnect()
        // Bodies are not logged: they can hold session tokens and personal details.
        Log.d(TAG, "POST $url -> $responseCode")
        ApiResponse(responseCode, body)
    } catch (error: Exception) {
        Log.e(TAG, "POST $url failed", error)
        noteFailure(error)
        null
    }
}

/** GET counterpart of [postForResponse], for the shared read-only tables. */
internal fun getForResponse(url: String): ApiResponse? {
    // Reading is safe to repeat, so a reply that gets dropped part-way is asked for once more.
    repeat(2) {
        if (serverRecentlyUnreachable()) return null
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
            connection.readTimeout = READ_TIMEOUT_MILLIS
            connection.setRequestProperty("Content-Type", "application/json")

            val responseCode = connection.responseCode
            val body = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
            }

            connection.disconnect()
            Log.d(TAG, "GET $url -> $responseCode")
            return ApiResponse(responseCode, body)
        } catch (error: Exception) {
            Log.e(TAG, "GET $url failed", error)
            noteFailure(error)
        }
    }
    return null
}

/**
 * The rows in a list response, or null when the call failed - so the caller can tell "the
 * server has none" from "the server couldn't say" and keep what it already holds.
 * [parse] returns null for a row it can't use (for instance one with no primary key).
 */
internal fun <T> rowsFrom(response: ApiResponse?, parse: (JSONObject) -> T?): List<T>? {
    if (response == null || !response.isSuccess) return null
    return try {
        jsonRows(response.body).mapNotNull(parse)
    } catch (error: Exception) {
        Log.e(TAG, "Could not read the server's rows", error)
        null
    }
}

/** Accepts a bare array of rows, an object wrapping one (e.g. {"tasks": [...]}), or a single row. */
private fun jsonRows(body: String?): List<JSONObject> {
    val text = body?.trim().orEmpty()
    if (text.isEmpty()) return emptyList()
    if (!text.startsWith("[")) {
        val wrapper = JSONObject(text)
        val wrapped = wrapper.keys().asSequence().mapNotNull { wrapper.optJSONArray(it) }.firstOrNull()
            ?: return listOf(wrapper)
        return (0 until wrapped.length()).mapNotNull { wrapped.optJSONObject(it) }
    }
    val array = JSONArray(text)
    return (0 until array.length()).mapNotNull { array.optJSONObject(it) }
}

/** Reads a create/update response. [idKey] is the primary-key column a create is expected to return. */
internal fun writeResultFrom(response: ApiResponse?, idKey: String? = null): WriteResult {
    if (response == null) {
        return WriteResult(success = false, serverReached = false, errorMessage = "Could not reach the server.")
    }
    val obj = jsonObjectOrNull(response.body)
    return if (response.isSuccess) {
        val id = idKey?.let { key -> obj?.intOrNull(key) ?: obj?.intOrNull("id") }
        WriteResult(success = true, id = id)
    } else {
        WriteResult(success = false, errorMessage = obj?.let(::serverMessage))
    }
}

internal fun jsonObjectOrNull(body: String?): JSONObject? = try {
    JSONObject(body ?: "{}")
} catch (error: Exception) {
    null
}

/**
 * The server's own explanation of a refusal, when it gives one as plain text. On a validation
 * failure "detail" is a list that echoes the submitted fields, so that form is never used.
 */
internal fun serverMessage(obj: JSONObject): String? =
    (obj.opt("detail") as? String)?.takeIf { it.isNotBlank() }
        ?: obj.optString("message").takeIf { it.isNotBlank() }

internal fun JSONObject.intOrNull(key: String): Int? {
    if (!has(key) || isNull(key)) return null
    return optInt(key, Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE }
}

internal fun JSONObject.stringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) optString(key) else null

/** SQLite stores BOOLEAN as 0 or 1, so either form is accepted. */
internal fun JSONObject.booleanOr(key: String, default: Boolean): Boolean = when (val value = opt(key)) {
    is Boolean -> value
    is Number -> value.toInt() != 0
    else -> default
}

internal fun JSONObject.dateOrNull(key: String): LocalDate? {
    val text = stringOrNull(key)?.trim() ?: return null
    return runCatching { LocalDate.parse(text.take(10)) }.getOrNull()
}

/** Reads a DATETIME whether it arrives as "2026-10-04T09:30:00", with a space, or with a time zone. */
internal fun JSONObject.dateTimeOrNull(key: String): LocalDateTime? {
    val text = stringOrNull(key)?.trim()?.replace(' ', 'T') ?: return null
    return runCatching { LocalDateTime.parse(text) }
        .recoverCatching { OffsetDateTime.parse(text).toLocalDateTime() }
        .getOrNull()
}
