package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.DailyQuestion
import org.json.JSONObject

/*
 * The server's daily check-in calls (/api/daily-checkin/): Daily_Question, Daily_Checkin and
 * Response. Each names the client by session_token alone.
 */

/** Whether the client has a Daily_Checkin row for the server's today, or null if the server couldn't say. */
fun fetchCheckedInToday(sessionToken: String): Boolean? {
    val json = JSONObject().apply { put("session_token", sessionToken) }
    val response = postForResponse(DAILY_CHECKIN_COMPLETED_TODAY_URL, json) ?: return null
    if (!response.isSuccess) return null
    val obj = jsonObjectOrNull(response.body) ?: return null
    return if (obj.has("completed_today")) obj.booleanOr("completed_today", false) else null
}

/**
 * The client's Daily_Question rows. A client who has none yet is first given a copy of every
 * Question_Template row the server holds.
 */
fun fetchDailyQuestions(sessionToken: String): List<DailyQuestion>? {
    val json = JSONObject().apply {
        put("session_token", sessionToken)
        put("create_questions", true)
    }
    return rowsFrom(postForResponse(DAILY_CHECKIN_QUESTIONS_URL, json)) { row ->
        DailyQuestion(
            questionId = row.intOrNull("question_id") ?: return@rowsFrom null,
            categoryId = row.intOrNull("category_id"),
            clientId = row.intOrNull("client_id"),
            questionOrder = row.intOrNull("question_order") ?: return@rowsFrom null,
            questionText = row.stringOrNull("question_text").orEmpty()
        )
    }
}

/**
 * Adds one Daily_Question row for the client. The server turns it away unless category_id is a
 * Category row it holds, and if the client already has a question at that question_order.
 */
fun addDailyQuestion(sessionToken: String, question: DailyQuestion): WriteResult {
    val json = JSONObject().apply {
        put("session_token", sessionToken)
        putNullable("category_id", question.categoryId)
        put("question_order", question.questionOrder)
        put("question_text", question.questionText)
    }
    return writeResultFrom(postForResponse(DAILY_CHECKIN_QUESTION_ADD_URL, json), idKey = "question_id")
}

fun removeDailyQuestion(sessionToken: String, questionId: Int): WriteResult {
    val json = JSONObject().apply {
        put("session_token", sessionToken)
        put("question_id", questionId)
    }
    return writeResultFrom(postForResponse(DAILY_CHECKIN_QUESTION_REMOVE_URL, json))
}

/**
 * A Daily_Checkin and its Response rows in one go; [answers] maps question_id to answer. The
 * server files them under its own today, adding to (or overwriting answers in) a check-in the
 * client already made that day, and returns that check-in's checkin_id.
 */
fun submitDailyCheckin(sessionToken: String, answers: Map<Int, Int>): WriteResult {
    val json = JSONObject().apply {
        put("session_token", sessionToken)
        put("answers", JSONObject().apply {
            answers.forEach { (questionId, answer) -> put(questionId.toString(), answer) }
        })
    }
    return writeResultFrom(postForResponse(DAILY_CHECKIN_SUBMIT_URL, json), idKey = "checkin_id")
}
