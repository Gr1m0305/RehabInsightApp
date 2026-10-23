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

/** Creates a Client row. The server puts them with the first admin account, as the app has no way to choose one. */
fun signUpClient(
    firstName: String,
    lastName: String,
    email: String,
    phone: String?,
    password: String
): SignUpResult {
    return try {
        val json = JSONObject().apply {
            put("first_name", firstName)
            put("last_name", lastName)
            put("email", email)
            put("phone", phone ?: "")
            put("password", password)
        }

        fun send(): Pair<Int, String?> {
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
            return responseCode to body
        }

        val (responseCode, body) = send()

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

/** The server's shortest allowed new password (new_password on client/change-password and client/reset-password). */
const val MIN_PASSWORD_LENGTH = 8

/** How long a reset code works for, and how soon another can be sent (the server's RESET_CODE_MINUTES and NEW_CODE_COOLDOWN_SECONDS). */
const val RESET_CODE_MINUTES = 15
const val RESET_CODE_RESEND_SECONDS = 60

private const val SERVER_UNREACHABLE_MESSAGE =
    "We couldn't reach the server just now. Please check your connection and try again."

/** For a server that doesn't have the reset endpoints yet. */
private const val RESET_UNAVAILABLE_MESSAGE =
    "Resetting your password isn't available just yet. Please ask your clinician for help."

/**
 * Client: replaces the signed-in client's password_hash. The server checks that [sessionToken]
 * belongs to [email] and that [oldPassword] is their current password. On a refusal,
 * errorMessage is worded for the person changing it.
 */
fun changeClientPassword(email: String, sessionToken: String, oldPassword: String, newPassword: String): WriteResult {
    val json = JSONObject().apply {
        put("email", email)
        put("session_token", sessionToken)
        put("old_password", oldPassword)
        put("new_password", newPassword)
    }
    val response = postForResponse(CLIENT_CHANGE_PASSWORD_URL, json, alwaysTry = true)
        ?: return WriteResult(success = false, serverReached = false, errorMessage = SERVER_UNREACHABLE_MESSAGE)
    if (response.isSuccess) return WriteResult(success = true)

    val message = when (response.code) {
        400 -> "That current password doesn't match. Please try again."
        401 -> "Your session has ended. Please sign in again, then change your password."
        // A validation failure's detail echoes the submitted passwords, so it is never shown.
        422 -> "Your new password needs at least $MIN_PASSWORD_LENGTH characters."
        else -> "We couldn't change your password just now. Please try again in a little while."
    }
    return WriteResult(success = false, errorMessage = message)
}

/**
 * Client: asks the server to email a password reset code to [email]. It answers the same
 * whether or not the email has an account, so success doesn't mean one was sent.
 */
fun sendPasswordResetCode(email: String): WriteResult {
    val json = JSONObject().apply { put("email", email) }
    val response = postForResponse(CLIENT_FORGOT_PASSWORD_URL, json, alwaysTry = true)
        ?: return WriteResult(success = false, serverReached = false, errorMessage = SERVER_UNREACHABLE_MESSAGE)
    if (response.isSuccess) return WriteResult(success = true)

    val message = when (response.code) {
        404 -> RESET_UNAVAILABLE_MESSAGE
        503 -> "We couldn't send the email just now. Please try again in a little while."
        else -> "We couldn't send a reset code just now. Please try again in a little while."
    }
    return WriteResult(success = false, errorMessage = message)
}

/** Client: sets a new password using the code from the reset email. The server then signs the client out everywhere. */
fun resetClientPassword(email: String, code: String, newPassword: String): WriteResult {
    val json = JSONObject().apply {
        put("email", email)
        put("code", code)
        put("new_password", newPassword)
    }
    val response = postForResponse(CLIENT_RESET_PASSWORD_URL, json, alwaysTry = true)
        ?: return WriteResult(success = false, serverReached = false, errorMessage = SERVER_UNREACHABLE_MESSAGE)
    if (response.isSuccess) return WriteResult(success = true)

    val message = when (response.code) {
        400 -> "That code isn't right or has expired. Check the email, or ask for a new code."
        404 -> RESET_UNAVAILABLE_MESSAGE
        // As with change-password, a validation failure's detail echoes the password.
        422 -> "Your new password needs at least $MIN_PASSWORD_LENGTH characters."
        else -> "We couldn't reset your password just now. Please try again in a little while."
    }
    return WriteResult(success = false, errorMessage = message)
}

/** A Client row as the server sends it. It never sends password hashes back, so that field comes through empty. */
private fun clientFrom(row: JSONObject): Client? {
    return Client(
        clientId = row.intOrNull("client_id") ?: return null,
        adminId = row.intOrNull("admin_id"),
        firstName = row.stringOrNull("first_name").orEmpty(),
        lastName = row.stringOrNull("last_name").orEmpty(),
        email = row.stringOrNull("email").orEmpty(),
        phone = row.stringOrNull("phone")?.ifBlank { null },
        passwordHash = ""
    )
}

/** The signed-in client's own Client row, or null if the server couldn't supply it. */
fun fetchClientProfile(sessionToken: String): Client? {
    val json = JSONObject().apply { put("session_token", sessionToken) }
    return objectFrom(postForResponse(CLIENT_PROFILE_URL, json), "client")?.let(::clientFrom)
}

/**
 * Every client: any admin can see them all. The server checks the admin's email and password on this
 * call as on every admin call, so [accepted] also says whether they are right.
 */
data class AdminClients(
    val clients: List<Client>,
    val accepted: Boolean,
    /** False when the request never got an answer, as opposed to the server turning it down. */
    val serverReached: Boolean
)

fun fetchAdminClients(caller: Caller): AdminClients {
    val response = postForResponse(ADMIN_CLIENTS_URL, JSONObject().putCaller(caller), alwaysTry = true)
        ?: return AdminClients(emptyList(), accepted = false, serverReached = false)
    val clients = rowsFrom(response, ::clientFrom)
        ?: return AdminClients(emptyList(), accepted = false, serverReached = true)
    return AdminClients(clients, accepted = true, serverReached = true)
}
