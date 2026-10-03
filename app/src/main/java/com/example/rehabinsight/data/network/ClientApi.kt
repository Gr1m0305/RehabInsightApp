package com.example.rehabinsight.data.network

import android.util.Log
import com.example.rehabinsight.data.Client
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SignUpResult(
    val success: Boolean,
    val clientId: Int?,
    val errorMessage: String?
)

internal fun postJson(urlString: String, json: JSONObject): Pair<HttpURLConnection, String?> {
    val connection = URL(urlString).openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = false
    connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
    connection.readTimeout = READ_TIMEOUT_MILLIS
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true

    connection.outputStream.use { output ->
        output.write(json.toString().toByteArray())
    }

    val responseCode = connection.responseCode

    val body = if (responseCode in 200..299) {
        connection.inputStream.bufferedReader().use { it.readText() }
    } else {
        connection.errorStream?.bufferedReader()?.use { it.readText() }
    }

    return connection to body
}

fun signUpClient(
    adminId: Int,
    firstName: String,
    lastName: String,
    email: String,
    phone: String?,
    password: String
): SignUpResult {
    return try {
        val json = JSONObject().apply {
            put("admin_id", adminId)
            put("first_name", firstName)
            put("last_name", lastName)
            put("email", email)
            put("phone", phone ?: "")
            put("password", password)
        }

        var (connection, body) = postJson(CLIENT_SIGNUP_URL, json)
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

        Log.d("SIGNUP_TEST", "URL: $CLIENT_SIGNUP_URL")
        Log.d("SIGNUP_TEST", "Code: $responseCode")
        Log.d("SIGNUP_TEST", "Allow header: ${connection.getHeaderField("Allow")}")
        Log.d("SIGNUP_TEST", "Body: $body")

        connection.disconnect()

        if (responseCode in 200..299) {
            val obj = JSONObject(body ?: "{}")
            val clientId = when {
                obj.has("client_id") -> obj.optInt("client_id")
                obj.has("id") -> obj.optInt("id")
                else -> null
            }
            SignUpResult(success = true, clientId = clientId, errorMessage = null)
        } else {
            val obj = try {
                JSONObject(body ?: "{}")
            } catch (e: Exception) {
                null
            }
            // On a 422 the server's "detail" is a list of validation objects that echo the submitted
            // fields (password included), so only a plain-string detail is ever shown to the user.
            val detail = (obj?.opt("detail") as? String)?.takeIf { it.isNotBlank() }
            val message = when {
                isEmailAlreadyInUse(responseCode, detail) -> "That email is already in use. Try signing in instead."
                // Any other database error is no use to the person signing up, so it gets the general wording.
                detail != null && "constraint failed" !in detail.lowercase() -> detail
                else -> obj?.optString("message")?.takeIf { it.isNotBlank() }
                    ?: "We couldn't create your account just now. Please try again in a little while."
            }
            SignUpResult(success = false, clientId = null, errorMessage = message)
        }
    } catch (error: Exception) {
        Log.e("SIGNUP_TEST", "Sign up request failed", error)
        SignUpResult(success = false, clientId = null, errorMessage = "Could not reach the server.")
    }
}

/**
 * True when the server turned sign-up down because the email belongs to an existing client.
 * Today it answers 400 with the database's own "UNIQUE constraint failed: Client.email"; a 409,
 * or wording such as "email already registered", is recognised too in case that is tidied up.
 */
private fun isEmailAlreadyInUse(responseCode: Int, detail: String?): Boolean {
    if (responseCode == 409) return true
    val text = detail?.lowercase() ?: return false
    return "email" in text && ("unique" in text || "already" in text || "exists" in text)
}

data class LoginResult(
    val success: Boolean,
    val sessionToken: String?,
    val clientId: Int?,
    /** False when the request never got an answer, as opposed to the server turning it down. */
    val serverReached: Boolean,
    val errorMessage: String?
)

/** Client_Session: signs a client in. The server answers with the session token for later calls. */
fun loginClient(email: String, password: String): LoginResult {
    val json = JSONObject().apply {
        put("email", email)
        put("password", password)
    }
    val response = postForResponse(CLIENT_LOGIN_URL, json, alwaysTry = true)
        ?: return LoginResult(false, null, null, serverReached = false, errorMessage = "Could not reach the server.")

    val obj = jsonObjectOrNull(response.body)
    val token = obj?.stringOrNull("session_token")
    return if (response.isSuccess && token != null) {
        LoginResult(true, token, obj.intOrNull("client_id"), serverReached = true, errorMessage = null)
    } else {
        LoginResult(false, null, null, serverReached = true, errorMessage = obj?.let(::serverMessage) ?: "Login failed.")
    }
}

/** Client_Session: the client a session token belongs to, or null if the server doesn't accept it. */
fun validateSession(sessionToken: String): Int? {
    val json = JSONObject().apply { put("session_token", sessionToken) }
    val response = postForResponse(SESSION_VALIDATE_URL, json, alwaysTry = true) ?: return null
    if (!response.isSuccess) return null
    return jsonObjectOrNull(response.body)?.intOrNull("client_id")
}

/** Client_Session: ends the session so its token stops working. */
fun logoutSession(sessionToken: String): WriteResult {
    val json = JSONObject().apply { put("session_token", sessionToken) }
    return writeResultFrom(postForResponse(SESSION_LOGOUT_URL, json))
}

/**
 * Client rows: just [clientId]'s own when given, otherwise every client the calling admin looks
 * after. The server never sends password hashes back, so that field comes through empty.
 */
fun fetchClients(caller: Caller, clientId: Int? = null): List<Client>? {
    val json = JSONObject().putCaller(caller).apply {
        clientId?.let { put("client_id", it) }
    }
    return rowsFrom(postForResponse(CLIENT_LIST_URL, json)) { row ->
        Client(
            clientId = row.intOrNull("client_id") ?: return@rowsFrom null,
            adminId = row.intOrNull("admin_id"),
            firstName = row.stringOrNull("first_name").orEmpty(),
            lastName = row.stringOrNull("last_name").orEmpty(),
            email = row.stringOrNull("email").orEmpty(),
            phone = row.stringOrNull("phone")?.ifBlank { null },
            passwordHash = ""
        )
    }
}
