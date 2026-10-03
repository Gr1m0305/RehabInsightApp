package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.ClientStreak
import com.example.rehabinsight.data.ClientTask
import com.example.rehabinsight.data.ClientTaskStatus
import com.example.rehabinsight.data.DailyCheckin
import com.example.rehabinsight.data.DailyQuestion
import com.example.rehabinsight.data.Response
import org.json.JSONObject
import java.time.LocalDate

/*
 * The tables that hold one client's own rows: Client_Streak, Client_Task, Daily_Question,
 * Daily_Checkin and Response. (Creating a Daily_Checkin is in CheckinApi.)
 * The list calls return null when the server couldn't supply the rows.
 */

// ----- Client_Streak -----

/** [clientId]'s streak when given, otherwise the streaks of every client the calling admin looks after. */
fun fetchClientStreaks(caller: Caller, clientId: Int? = null): List<ClientStreak>? {
    val json = JSONObject().putCaller(caller).apply {
        clientId?.let { put("client_id", it) }
    }
    return rowsFrom(postForResponse(CLIENT_STREAK_LIST_URL, json)) { row ->
        ClientStreak(
            clientId = row.intOrNull("client_id") ?: return@rowsFrom null,
            currentStreak = row.intOrNull("current_streak") ?: 0,
            bestStreak = row.intOrNull("best_streak") ?: 0,
            lastStreakDate = row.dateOrNull("last_streak_date")
        )
    }
}

/** Stores a client's streak, creating their Client_Streak row if they don't have one yet. */
fun saveClientStreak(caller: Caller, streak: ClientStreak): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("client_id", streak.clientId)
        put("current_streak", streak.currentStreak)
        put("best_streak", streak.bestStreak)
        putNullable("last_streak_date", streak.lastStreakDate?.toString())
    }
    return writeResultFrom(postForResponse(CLIENT_STREAK_UPDATE_URL, json))
}

// ----- Client_Task -----

/**
 * [clientId]'s rows when given, otherwise those of every client the calling admin looks after.
 * [dueDate] narrows either to a single day.
 */
fun fetchClientTasks(caller: Caller, clientId: Int? = null, dueDate: LocalDate? = null): List<ClientTask>? {
    val json = JSONObject().putCaller(caller).apply {
        clientId?.let { put("client_id", it) }
        dueDate?.let { put("due_date", it.toString()) }
    }
    return rowsFrom(postForResponse(CLIENT_TASK_LIST_URL, json)) { row ->
        ClientTask(
            clientTaskId = row.intOrNull("client_task_id") ?: return@rowsFrom null,
            clientId = row.intOrNull("client_id") ?: return@rowsFrom null,
            taskId = row.intOrNull("task_id") ?: return@rowsFrom null,
            assignedAt = row.dateTimeOrNull("assigned_at") ?: return@rowsFrom null,
            dueDate = row.dateOrNull("due_date"),
            completedAt = row.dateTimeOrNull("completed_at"),
            status = row.stringOrNull("status") ?: ClientTaskStatus.PENDING
        )
    }
}

fun createClientTask(caller: Caller, clientTask: ClientTask): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("client_id", clientTask.clientId)
        put("task_id", clientTask.taskId)
        put("assigned_at", clientTask.assignedAt.toString())
        putNullable("due_date", clientTask.dueDate?.toString())
        putNullable("completed_at", clientTask.completedAt?.toString())
        put("status", clientTask.status)
    }
    return writeResultFrom(postForResponse(CLIENT_TASK_CREATE_URL, json), idKey = "client_task_id")
}

/** Stores a row's status and completion time - ticking a task off, un-ticking it, or removing it. */
fun saveClientTaskStatus(caller: Caller, clientTask: ClientTask): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("client_task_id", clientTask.clientTaskId)
        put("status", clientTask.status)
        putNullable("completed_at", clientTask.completedAt?.toString())
    }
    return writeResultFrom(postForResponse(CLIENT_TASK_UPDATE_URL, json))
}

// ----- Daily_Question -----

fun fetchDailyQuestions(caller: Caller, clientId: Int): List<DailyQuestion>? {
    val json = JSONObject().putCaller(caller).apply { put("client_id", clientId) }
    return rowsFrom(postForResponse(DAILY_QUESTION_LIST_URL, json)) { row ->
        DailyQuestion(
            questionId = row.intOrNull("question_id") ?: return@rowsFrom null,
            categoryId = row.intOrNull("category_id"),
            clientId = row.intOrNull("client_id"),
            questionOrder = row.intOrNull("question_order") ?: return@rowsFrom null,
            questionText = row.stringOrNull("question_text").orEmpty()
        )
    }
}

fun createDailyQuestion(caller: Caller, question: DailyQuestion): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        putNullable("category_id", question.categoryId)
        putNullable("client_id", question.clientId)
        put("question_order", question.questionOrder)
        put("question_text", question.questionText)
    }
    return writeResultFrom(postForResponse(DAILY_QUESTION_CREATE_URL, json), idKey = "question_id")
}

// ----- Daily_Checkin -----

fun fetchDailyCheckins(caller: Caller, clientId: Int): List<DailyCheckin>? {
    val json = JSONObject().putCaller(caller).apply { put("client_id", clientId) }
    return rowsFrom(postForResponse(DAILY_CHECKIN_LIST_URL, json)) { row ->
        DailyCheckin(
            checkinId = row.intOrNull("checkin_id") ?: return@rowsFrom null,
            clientId = row.intOrNull("client_id") ?: return@rowsFrom null,
            checkinDate = row.dateOrNull("checkin_date") ?: return@rowsFrom null,
            completedAt = row.dateTimeOrNull("completed_at")
        )
    }
}

// ----- Response -----

/** Every answer [clientId] has given, across all of their check-ins. */
fun fetchResponses(caller: Caller, clientId: Int): List<Response>? {
    val json = JSONObject().putCaller(caller).apply { put("client_id", clientId) }
    return rowsFrom(postForResponse(RESPONSE_LIST_URL, json)) { row ->
        Response(
            responseId = row.intOrNull("response_id") ?: return@rowsFrom null,
            questionId = row.intOrNull("question_id") ?: return@rowsFrom null,
            checkinId = row.intOrNull("checkin_id") ?: return@rowsFrom null,
            answer = row.intOrNull("answer") ?: return@rowsFrom null
        )
    }
}

fun createResponse(caller: Caller, response: Response): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("question_id", response.questionId)
        put("checkin_id", response.checkinId)
        put("answer", response.answer)
    }
    return writeResultFrom(postForResponse(RESPONSE_CREATE_URL, json), idKey = "response_id")
}
