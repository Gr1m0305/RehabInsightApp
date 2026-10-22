package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.ClientStreak
import com.example.rehabinsight.data.ClientTask
import com.example.rehabinsight.data.ClientTaskStatus
import com.example.rehabinsight.data.DailyCheckin
import com.example.rehabinsight.data.Response
import com.example.rehabinsight.data.Task
import org.json.JSONObject
import java.time.LocalDate

/*
 * The tables that hold one client's own rows: Client_Streak, Client_Task, and past Daily_Checkin
 * and Response rows. (The server's own Daily_Question and check-in calls are in CheckinApi.)
 * The client calls name the client by session_token alone; the admin ones by client_id.
 * The list calls return null when the server couldn't supply the rows.
 */

// ----- Client_Streak -----

/** The server sends a streak's best as longest_streak; it has no last_streak_date in an admin's report. */
private fun streakFrom(row: JSONObject, clientId: Int) = ClientStreak(
    clientId = clientId,
    currentStreak = row.intOrNull("current_streak") ?: 0,
    bestStreak = row.intOrNull("longest_streak") ?: row.intOrNull("best_streak") ?: 0,
    lastStreakDate = row.dateOrNull("last_streak_date")
)

/**
 * The signed-in client's streak. The server works it out from the Client_Task rows they have
 * completed, and keeps their Client_Streak row up to date itself, so the app only ever reads it.
 */
fun fetchClientStreak(sessionToken: String, clientId: Int): ClientStreak? =
    objectFrom(postForResponse(CLIENT_STREAK_URL, sessionBody(sessionToken)), "streak")?.let { streakFrom(it, clientId) }

// ----- Client_Task -----

/**
 * A Client_Task row. The server's statuses are "assigned" and "completed". An assignment with
 * no due_date is due until it is done, so it belongs on today's checklist, or on the day it was
 * completed.
 */
private fun clientTaskFrom(row: JSONObject): ClientTask? {
    val completedAt = row.dateTimeOrNull("completed_at")
    val status = row.stringOrNull("status")
    return ClientTask(
        clientTaskId = row.intOrNull("client_task_id") ?: return null,
        clientId = row.intOrNull("client_id") ?: return null,
        taskId = row.intOrNull("task_id") ?: return null,
        assignedAt = row.dateTimeOrNull("assigned_at") ?: return null,
        dueDate = row.dateOrNull("due_date") ?: completedAt?.toLocalDate() ?: LocalDate.now(),
        completedAt = completedAt,
        status = when {
            completedAt != null || status == ClientTaskStatus.COMPLETED -> ClientTaskStatus.COMPLETED
            status == ClientTaskStatus.REMOVED -> ClientTaskStatus.REMOVED
            else -> ClientTaskStatus.PENDING
        }
    )
}

/** A client's Client_Task rows, with the Task row each is for (which may since have left the active library). */
data class AssignedTasks(val clientTasks: List<ClientTask>, val tasks: List<Task>)

fun fetchClientTasks(sessionToken: String): AssignedTasks? {
    val rows = rowsFrom(postForResponse(CLIENT_TASKS_URL, sessionBody(sessionToken))) { it } ?: return null
    return AssignedTasks(
        clientTasks = rows.mapNotNull(::clientTaskFrom),
        tasks = rows.mapNotNull { row -> row.optJSONObject("task")?.let(::taskFrom) }
    )
}

/**
 * Ticks off one of the client's Client_Task rows; the server then brings their streak up to date.
 * It has no call to un-tick or remove one.
 */
fun completeClientTask(sessionToken: String, clientTaskId: Int): WriteResult {
    val json = sessionBody(sessionToken).apply { put("client_task_id", clientTaskId) }
    return writeResultFrom(postForResponse(CLIENT_TASK_COMPLETE_URL, json))
}

// ----- Daily_Checkin, Response -----

/** Every Daily_Checkin row the signed-in client has. */
fun fetchCheckinHistory(sessionToken: String): List<DailyCheckin>? =
    allPagesFrom(CHECKIN_HISTORY_URL, sessionBody(sessionToken)) { row ->
        DailyCheckin(
            checkinId = row.intOrNull("checkin_id") ?: return@allPagesFrom null,
            clientId = row.intOrNull("client_id") ?: return@allPagesFrom null,
            checkinDate = row.dateOrNull("checkin_date") ?: return@allPagesFrom null,
            completedAt = row.dateTimeOrNull("completed_at")
        )
    }

/** Every answer the signed-in client has given, across all of their check-ins. */
fun fetchAnswerHistory(sessionToken: String): List<Response>? =
    allPagesFrom(ANSWER_HISTORY_URL, sessionBody(sessionToken)) { row ->
        Response(
            responseId = row.intOrNull("response_id") ?: return@allPagesFrom null,
            questionId = row.intOrNull("question_id") ?: return@allPagesFrom null,
            checkinId = row.intOrNull("checkin_id") ?: return@allPagesFrom null,
            answer = row.intOrNull("answer") ?: return@allPagesFrom null
        )
    }

// ----- An admin's clients -----

/** Client_Task rows for one of the admin's clients. */
fun fetchAdminClientTasks(caller: Caller, clientId: Int): List<ClientTask>? {
    val json = JSONObject().putCaller(caller).apply { put("client_id", clientId) }
    return rowsFrom(postForResponse(ADMIN_CLIENT_TASKS_URL, json), ::clientTaskFrom)
}

/** One of the admin's clients' streak, read from the server's report on them. */
fun fetchAdminClientStreak(caller: Caller, clientId: Int): ClientStreak? {
    val json = JSONObject().putCaller(caller).apply { put("client_id", clientId) }
    val response = postForResponse(ADMIN_CLIENT_REPORT_URL, json)
    if (response == null || !response.isSuccess) return null
    return jsonObjectOrNull(response.body)?.let { streakFrom(it, clientId) }
}

/** Puts a Task on one of the admin's clients' checklists, due on the row's due_date. Returns its client_task_id. */
fun assignClientTask(caller: Caller, clientTask: ClientTask): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("client_id", clientTask.clientId)
        put("task_id", clientTask.taskId)
        putNullable("due_date", clientTask.dueDate?.toString())
    }
    return writeResultFrom(postForResponse(ADMIN_TASK_ASSIGN_URL, json), idKey = "client_task_id")
}
