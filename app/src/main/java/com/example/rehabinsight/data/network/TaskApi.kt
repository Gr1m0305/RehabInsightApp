package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.Task
import org.json.JSONObject

/*
 * The admin's view of and changes to the Task library. The server has no call for Task_Category,
 * so the colour a task is filed under stays on the device.
 */

/** Every Task row, including ones taken out of use. */
fun fetchAdminTasks(caller: Caller): List<Task>? =
    rowsFrom(postForResponse(ADMIN_TASKS_URL, JSONObject().putCaller(caller)), ::taskFrom)

fun createTask(caller: Caller, task: Task): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        putNullable("article_id", task.articleId)
        put("title", task.title)
        // The server requires a description, and a task added from the dashboard has only a title.
        put("description", task.description.ifBlank { task.title })
        putNullable("justification", task.justification)
        putNullable("min_minutes", task.minMinutes)
        putNullable("max_minutes", task.maxMinutes)
    }
    return writeResultFrom(postForResponse(ADMIN_TASK_CREATE_URL, json), idKey = "task_id")
}

/** Stores a rename, or taking a task out of use (is_active = false). The server keeps its other columns as they are. */
fun saveTaskChanges(caller: Caller, task: Task): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("task_id", task.taskId)
        put("title", task.title)
        put("is_active", task.isActive)
    }
    return writeResultFrom(postForResponse(ADMIN_TASK_UPDATE_URL, json))
}
