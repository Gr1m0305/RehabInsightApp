package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.Article
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.data.TaskCategory
import com.example.rehabinsight.data.UniversalAppSetting
import org.json.JSONObject

/*
 * The tables every client shares and only reads: Category, Article, Task with its Task_Category,
 * and Universal_App_Setting. (Streak_Milestone is in MilestoneApi.) Like every call but sign-up
 * and sign-in, each is made only once the client is signed in, and carries their session_token.
 * Each returns null when the server couldn't supply the table.
 */

private fun categoryFrom(row: JSONObject): Category? {
    return Category(
        categoryId = row.intOrNull("category_id") ?: return null,
        name = row.stringOrNull("name").orEmpty(),
        colour = row.stringOrNull("colour") ?: return null
    )
}

fun fetchCategories(sessionToken: String): List<Category>? =
    rowsFrom(postForResponse(CATEGORIES_URL, sessionBody(sessionToken)), ::categoryFrom)

/** The Category rows, for an admin. */
fun fetchAdminCategories(caller: Caller): List<Category>? =
    rowsFrom(postForResponse(ADMIN_CATEGORIES_URL, JSONObject().putCaller(caller)), ::categoryFrom)

/**
 * The active Article rows. The server sends the body as "body", and in banner_image the photo
 * itself (Base64-encoded) rather than its file name.
 */
fun fetchArticles(sessionToken: String): List<Article>? =
    rowsFrom(postForResponse(ARTICLES_URL, sessionBody(sessionToken))) { row ->
        Article(
            articleId = row.intOrNull("article_id") ?: return@rowsFrom null,
            title = row.stringOrNull("title").orEmpty(),
            bodyText = (row.stringOrNull("body") ?: row.stringOrNull("body_text")).orEmpty(),
            bannerImage = row.stringOrNull("banner_image"),
            isActive = row.booleanOr("is_active", true),
            publishedAt = row.dateTimeOrNull("published_at"),
            sortOrder = row.intOrNull("sort_order") ?: 0
        )
    }

internal fun taskFrom(row: JSONObject): Task? {
    return Task(
        taskId = row.intOrNull("task_id") ?: return null,
        articleId = row.intOrNull("article_id"),
        title = row.stringOrNull("title").orEmpty(),
        description = row.stringOrNull("description").orEmpty(),
        minMinutes = row.intOrNull("min_minutes"),
        maxMinutes = row.intOrNull("max_minutes"),
        justification = row.stringOrNull("justification"),
        isActive = row.booleanOr("is_active", true)
    )
}

/** The Task_Category row the server sends with a task as its category_id, or null if it has none. */
internal fun taskCategoryFrom(row: JSONObject): TaskCategory? {
    val taskId = row.intOrNull("task_id") ?: return null
    return row.intOrNull("category_id")?.let { TaskCategory(categoryId = it, taskId = taskId) }
}

/** Task rows together with the Task_Category row of each (the colour it is filed under). */
data class TaskRows(val tasks: List<Task>, val taskCategories: List<TaskCategory>)

internal fun taskRowsFrom(rows: List<JSONObject>) = TaskRows(
    tasks = rows.mapNotNull(::taskFrom),
    taskCategories = rows.mapNotNull(::taskCategoryFrom)
)

/** The active Task rows: the library a client's checklist is drawn from. */
fun fetchTaskLibrary(sessionToken: String): TaskRows? =
    rowsFrom(postForResponse(TASK_LIBRARY_URL, sessionBody(sessionToken))) { it }?.let(::taskRowsFrom)

/** Universal_App_Setting rows: the app-wide numbers and messages the database keeps. */
fun fetchAppSettings(sessionToken: String): List<UniversalAppSetting>? =
    rowsFrom(postForResponse(APP_SETTINGS_URL, sessionBody(sessionToken))) { row ->
        UniversalAppSetting(
            settingId = row.intOrNull("setting_id") ?: 0,
            settingKey = row.stringOrNull("setting_key") ?: return@rowsFrom null,
            settingValue = row.stringOrNull("setting_value").orEmpty()
        )
    }
