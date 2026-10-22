package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.Article
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.Task
import org.json.JSONObject

/*
 * The tables every client shares and only reads: Category, Article and Task. (Streak_Milestone
 * is in MilestoneApi.) Like every call but sign-up and sign-in, each is made only once the client
 * is signed in, and carries their session_token. Each returns null when the server couldn't
 * supply the table.
 */

fun fetchCategories(sessionToken: String): List<Category>? =
    rowsFrom(postForResponse(CATEGORIES_URL, sessionBody(sessionToken))) { row ->
        Category(
            categoryId = row.intOrNull("category_id") ?: return@rowsFrom null,
            name = row.stringOrNull("name").orEmpty(),
            colour = row.stringOrNull("colour") ?: return@rowsFrom null
        )
    }

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

/** A Task row. The app's own timeLabel / isVigorous have no column, so they come through unset. */
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

/** The active Task rows: the library a client's checklist is drawn from. */
fun fetchTaskLibrary(sessionToken: String): List<Task>? =
    rowsFrom(postForResponse(TASK_LIBRARY_URL, sessionBody(sessionToken)), ::taskFrom)
