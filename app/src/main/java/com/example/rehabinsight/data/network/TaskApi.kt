package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.Task
import com.example.rehabinsight.data.TaskCategory
import org.json.JSONObject

/* Changes to the Task library and its colour categories (Task, Task_Category). */

private fun JSONObject.putTaskColumns(task: Task): JSONObject = apply {
    putNullable("article_id", task.articleId)
    put("title", task.title)
    put("description", task.description)
    putNullable("min_minutes", task.minMinutes)
    putNullable("max_minutes", task.maxMinutes)
    putNullable("justification", task.justification)
    put("is_active", task.isActive)
}

fun createTask(caller: Caller, task: Task): WriteResult {
    val json = JSONObject().putCaller(caller).putTaskColumns(task)
    return writeResultFrom(postForResponse(TASK_CREATE_URL, json), idKey = "task_id")
}

/** Stores a Task row's current columns - a rename, or taking it out of use (is_active = false). */
fun saveTaskChanges(caller: Caller, task: Task): WriteResult {
    val json = JSONObject().putCaller(caller).apply { put("task_id", task.taskId) }.putTaskColumns(task)
    return writeResultFrom(postForResponse(TASK_UPDATE_URL, json))
}

fun createTaskCategory(caller: Caller, taskCategory: TaskCategory): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("category_id", taskCategory.categoryId)
        put("task_id", taskCategory.taskId)
        put("difficulty", taskCategory.difficulty)
    }
    return writeResultFrom(postForResponse(TASK_CATEGORY_CREATE_URL, json))
}

/**
 * Moves a task to a different colour category. category_id is half of Task_Category's primary
 * key, so the row being replaced is identified by [previousCategoryId].
 */
fun saveTaskCategory(caller: Caller, taskCategory: TaskCategory, previousCategoryId: Int): WriteResult {
    val json = JSONObject().putCaller(caller).apply {
        put("task_id", taskCategory.taskId)
        put("previous_category_id", previousCategoryId)
        put("category_id", taskCategory.categoryId)
        put("difficulty", taskCategory.difficulty)
    }
    return writeResultFrom(postForResponse(TASK_CATEGORY_UPDATE_URL, json))
}
