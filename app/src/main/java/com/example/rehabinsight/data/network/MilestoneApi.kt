package com.example.rehabinsight.data.network

import org.json.JSONObject

data class StreakMilestoneDto(
    val milestoneId: Int,
    val days: Int,
    val message: String
)

/** Streak_Milestone rows, for a signed-in client. Empty when the server couldn't supply them. */
fun fetchStreakMilestones(sessionToken: String): List<StreakMilestoneDto> {
    val json = JSONObject().apply { put("session_token", sessionToken) }
    return rowsFrom(postForResponse(STREAK_MILESTONES_URL, json)) { row ->
        StreakMilestoneDto(
            milestoneId = row.intOrNull("milestone_id") ?: return@rowsFrom null,
            days = row.intOrNull("days") ?: return@rowsFrom null,
            message = row.stringOrNull("message").orEmpty()
        )
    }.orEmpty()
}
