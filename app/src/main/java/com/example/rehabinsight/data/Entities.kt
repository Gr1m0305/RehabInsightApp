package com.example.rehabinsight.data

import java.time.LocalDate
import java.time.LocalDateTime

/*
 * ---------------------------------------------------------------------------------------------
 * This file mirrors the project's Entity-Relationship Diagram table-for-table and
 * column-for-column. Every class below corresponds to exactly one table in the diagram:
 *
 *   Admin, Client, Client_Streak, Category, Task, Task_Category, Task_Rule, client_task,
 *   Article, Question_Template, Daily_Question, Daily_Checkin, Response,
 *   streak_milestone, Universal_App_Setting
 *
 * Since this app has no remote database, these tables live in-memory inside AppViewModel's
 * StateFlow (see RehabUiState), but the shapes/relationships (PK/FK) are kept identical to the
 * diagram so the data model can be lifted onto a real SQL database with no redesign.
 * ---------------------------------------------------------------------------------------------
 */

/** Admin table. One row per staff/clinician account. */
data class Admin(
    val adminId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

/** Client table. FK admin_id -> Admin.admin_id (the admin who manages/onboarded this client). */
data class Client(
    val clientId: Int,
    val adminId: Int? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val passwordHash: String
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

/** Client_Streak table. 1-to-1 with Client via client_id. */
data class ClientStreak(
    val clientId: Int,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastStreakDate: LocalDate? = null
)

/** Category table. The 4 backend-only wellbeing categories (never shown by name/label to users). */
data class Category(
    val categoryId: Int,
    val name: String,
    val colour: String // hex string, e.g. "#FF9F5A"
)

/** Article table. Education Library content. FK'd from Task.article_id (a task's "why" reading). */
data class Article(
    val articleId: Int,
    val title: String,
    val bodyText: String,
    val bannerImage: String? = null,
    val isActive: Boolean = true,
    val publishedAt: LocalDateTime? = null,
    val sortOrder: Int = 0
)

/** Task table. The master library of wellbeing tasks. FK article_id -> Article.article_id (optional). */
data class Task(
    val taskId: Int,
    val articleId: Int? = null,
    val title: String,
    val description: String = "",
    /** Human-friendly time commitment, e.g. "10 min", "2-15 min", "All day". */
    val timeLabel: String? = null,
    val minMinutes: Int? = null,
    val maxMinutes: Int? = null,
    /** One-line evidence/rationale shown as the task's "why". */
    val justification: String? = null,
    /** Vigorous/higher-impact movement tasks that should be swapped out under the Pain modifier. */
    val isVigorous: Boolean = false,
    val isActive: Boolean = true
)

/** Task_Category join table. FK category_id -> Category, FK task_id -> Task. */
data class TaskCategory(
    val categoryId: Int,
    val taskId: Int,
    val difficulty: Int = 1
)

/**
 * Task_Rule table. FK category_id -> Category, FK task_id -> Task.
 * Drives both initial checklist generation and daily guidance: a task becomes
 * "relevant" for a client once their score for `category_id` satisfies `operator`/`threshold_value`.
 */
data class TaskRule(
    val ruleId: Int,
    val categoryId: Int,
    val taskId: Int,
    val operator: String, // one of RuleOperator constants: ">=", ">", "<=", "<", "=="
    val thresholdValue: Int
)

object RuleOperator {
    const val GTE = ">="
    const val GT = ">"
    const val LTE = "<="
    const val LT = "<"
    const val EQ = "=="
}

/** client_task table. FK client_id -> Client, FK task_id -> Task. One row per day a task is assigned. */
data class ClientTask(
    val clientTaskId: Int,
    val clientId: Int,
    val taskId: Int,
    val assignedAt: LocalDateTime,
    val dueDate: LocalDate? = null,
    val completedAt: LocalDateTime? = null,
    val status: String = ClientTaskStatus.PENDING
)

object ClientTaskStatus {
    const val PENDING = "pending"
    const val COMPLETED = "completed"
    const val REMOVED = "removed"
}

/**
 * Question_Template table. The master library of question wording, optionally FK'd to a
 * Category. `questionOrder` bands distinguish the two question sets this app uses:
 *  - 1..99   -> Initial Setup Questionnaire (asked once)
 *  - 100..199 -> recurring Daily Check-In questions (asked every day)
 */
data class QuestionTemplate(
    val templateQuestionId: Int,
    val categoryId: Int? = null,
    val questionOrder: Int,
    val questionText: String
) {
    val isSetupQuestion: Boolean get() = questionOrder < 100
}

/** Daily_Question table. A per-client materialised copy of a Question_Template. */
data class DailyQuestion(
    val questionId: Int,
    val categoryId: Int? = null,
    val clientId: Int? = null,
    val questionOrder: Int,
    val questionText: String
)

/** Daily_Checkin table. One row per check-in "session" a client completes (setup counts as the first). */
data class DailyCheckin(
    val checkinId: Int,
    val clientId: Int,
    val checkinDate: LocalDate,
    val completedAt: LocalDateTime? = null
)

/** Response table. FK question_id -> Daily_Question, FK checkin_id -> Daily_Checkin. */
data class Response(
    val responseId: Int,
    val questionId: Int,
    val checkinId: Int,
    val answer: Int
)

/** streak_milestone table. Static list of celebration thresholds + copy. */
data class StreakMilestone(
    val milestoneId: Int,
    val days: Int,
    val message: String
)

/** Universal_App_Setting table. Simple global key/value config used across the app. */
data class UniversalAppSetting(
    val settingId: Int,
    val settingKey: String,
    val settingValue: String
)
