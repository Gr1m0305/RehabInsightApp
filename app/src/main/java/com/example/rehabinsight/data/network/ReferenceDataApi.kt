package com.example.rehabinsight.data.network

import com.example.rehabinsight.data.Article
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.QuestionTemplate
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.data.TaskCategory
import com.example.rehabinsight.data.TaskRule
import com.example.rehabinsight.data.UniversalAppSetting
import org.json.JSONObject

/*
 * The tables every client shares and only reads: Category, Article, Task, Task_Category,
 * Task_Rule, Question_Template and Universal_App_Setting. (Streak_Milestone is in MilestoneApi.)
 * Like every call but sign-up and sign-in, each is made only once the client is signed in, and
 * carries their session_token. Each returns null when the server couldn't supply the table.
 */

private fun sessionBody(sessionToken: String): JSONObject = JSONObject().apply { put("session_token", sessionToken) }

fun fetchCategories(sessionToken: String): List<Category>? =
    rowsFrom(postForResponse(CATEGORY_LIST_URL, sessionBody(sessionToken))) { row ->
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

/** Task rows. The app's own timeLabel / isVigorous have no column, so they come through unset. */
fun fetchTasks(sessionToken: String): List<Task>? =
    rowsFrom(postForResponse(TASK_LIST_URL, sessionBody(sessionToken))) { row ->
        Task(
            taskId = row.intOrNull("task_id") ?: return@rowsFrom null,
            articleId = row.intOrNull("article_id"),
            title = row.stringOrNull("title").orEmpty(),
            description = row.stringOrNull("description").orEmpty(),
            minMinutes = row.intOrNull("min_minutes"),
            maxMinutes = row.intOrNull("max_minutes"),
            justification = row.stringOrNull("justification"),
            isActive = row.booleanOr("is_active", true)
        )
    }

fun fetchTaskCategories(sessionToken: String): List<TaskCategory>? =
    rowsFrom(postForResponse(TASK_CATEGORY_LIST_URL, sessionBody(sessionToken))) { row ->
        TaskCategory(
            categoryId = row.intOrNull("category_id") ?: return@rowsFrom null,
            taskId = row.intOrNull("task_id") ?: return@rowsFrom null,
            difficulty = row.intOrNull("difficulty") ?: 1
        )
    }

fun fetchTaskRules(sessionToken: String): List<TaskRule>? =
    rowsFrom(postForResponse(TASK_RULE_LIST_URL, sessionBody(sessionToken))) { row ->
        TaskRule(
            ruleId = row.intOrNull("rule_id") ?: return@rowsFrom null,
            categoryId = row.intOrNull("category_id") ?: return@rowsFrom null,
            taskId = row.intOrNull("task_id") ?: return@rowsFrom null,
            operator = row.stringOrNull("operator") ?: return@rowsFrom null,
            thresholdValue = row.intOrNull("threshold_value") ?: 0
        )
    }

fun fetchQuestionTemplates(sessionToken: String): List<QuestionTemplate>? =
    rowsFrom(postForResponse(QUESTION_TEMPLATE_LIST_URL, sessionBody(sessionToken))) { row ->
        QuestionTemplate(
            templateQuestionId = row.intOrNull("template_question_id") ?: return@rowsFrom null,
            categoryId = row.intOrNull("category_id"),
            questionOrder = row.intOrNull("question_order") ?: return@rowsFrom null,
            questionText = row.stringOrNull("question_text").orEmpty()
        )
    }

fun fetchUniversalAppSettings(sessionToken: String): List<UniversalAppSetting>? =
    rowsFrom(postForResponse(SETTING_LIST_URL, sessionBody(sessionToken))) { row ->
        UniversalAppSetting(
            settingId = row.intOrNull("setting_id") ?: return@rowsFrom null,
            settingKey = row.stringOrNull("setting_key") ?: return@rowsFrom null,
            settingValue = row.stringOrNull("setting_value").orEmpty()
        )
    }
