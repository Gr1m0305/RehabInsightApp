package com.example.rehabinsight.data.network

import android.util.Log
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class StreakMilestoneDto(
    val milestoneId: Int,
    val days: Int,
    val message: String
)

fun fetchStreakMilestones(): List<StreakMilestoneDto> {
    return try {
        val connection = URL(STREAK_MILESTONES_URL).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json")

        val responseCode = connection.responseCode
        val body = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }
        }

        connection.disconnect()

        Log.d("MILESTONES_TEST", "URL: $STREAK_MILESTONES_URL")
        Log.d("MILESTONES_TEST", "Code: $responseCode")
        Log.d("MILESTONES_TEST", "Body: $body")

        if (responseCode in 200..299) {
            val array = JSONArray(body ?: "[]")
            (0 until array.length()).map { index ->
                val obj = array.getJSONObject(index)
                StreakMilestoneDto(
                    milestoneId = obj.optInt("milestone_id"),
                    days = obj.optInt("days"),
                    message = obj.optString("message")
                )
            }
        } else {
            emptyList()
        }
    } catch (error: Exception) {
        Log.e("MILESTONES_TEST", "Fetching streak milestones failed", error)
        emptyList()
    }
}
