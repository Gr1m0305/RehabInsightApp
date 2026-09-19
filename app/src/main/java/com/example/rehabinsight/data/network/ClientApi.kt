package com.example.rehabinsight.data.network

import android.util.Log
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
            val message = obj?.optString("detail")?.takeIf { it.isNotBlank() }
                ?: obj?.optString("message")?.takeIf { it.isNotBlank() }
                ?: "Sign up failed."
            SignUpResult(success = false, clientId = null, errorMessage = message)
        }
    } catch (error: Exception) {
        Log.e("SIGNUP_TEST", "Sign up request failed", error)
        SignUpResult(success = false, clientId = null, errorMessage = "Could not reach the server.")
    }
}
