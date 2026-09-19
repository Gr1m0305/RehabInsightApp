package com.example.rehabinsight.data.network

import android.util.Log
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

data class CheckinResult(
    val success: Boolean,
    val checkinId: Int?,
    val errorMessage: String?
)

fun createDailyCheckin(
    clientId: Int,
    checkinDate: LocalDate,
    completedAt: LocalDateTime?
): CheckinResult {
    return try {
        val json = JSONObject().apply {
            put("client_id", clientId)
            put("checkin_date", checkinDate.toString())
            put("completed_at", completedAt?.toString())
        }

        var (connection, body) = postJson(DAILY_CHECKIN_URL, json)
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

        Log.d("CHECKIN_TEST", "URL: $DAILY_CHECKIN_URL")
        Log.d("CHECKIN_TEST", "Code: $responseCode")
        Log.d("CHECKIN_TEST", "Body: $body")

        connection.disconnect()

        if (responseCode in 200..299) {
            val obj = JSONObject(body ?: "{}")
            val checkinId = when {
                obj.has("checkin_id") -> obj.optInt("checkin_id")
                obj.has("id") -> obj.optInt("id")
                else -> null
            }
            CheckinResult(success = true, checkinId = checkinId, errorMessage = null)
        } else {
            val obj = try {
                JSONObject(body ?: "{}")
            } catch (e: Exception) {
                null
            }
            val message = obj?.optString("detail")?.takeIf { it.isNotBlank() }
                ?: obj?.optString("message")?.takeIf { it.isNotBlank() }
                ?: "Check-in failed."
            CheckinResult(success = false, checkinId = null, errorMessage = message)
        }
    } catch (error: Exception) {
        Log.e("CHECKIN_TEST", "Check-in request failed", error)
        CheckinResult(success = false, checkinId = null, errorMessage = "Could not reach the server.")
    }
}
