package com.example.rehabinsight.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rehabinsight.data.Admin
import com.example.rehabinsight.data.Article
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.CheckInAnswers
import com.example.rehabinsight.data.ChecklistGenerator
import com.example.rehabinsight.data.Client
import com.example.rehabinsight.data.ClientStreak
import com.example.rehabinsight.data.ClientTask
import com.example.rehabinsight.data.ClientTaskStatus
import com.example.rehabinsight.data.DailyCheckin
import com.example.rehabinsight.data.DailyQuestion
import com.example.rehabinsight.data.GuidanceEngine
import com.example.rehabinsight.data.Goal
import com.example.rehabinsight.data.QuestionTemplate
import com.example.rehabinsight.data.Response
import com.example.rehabinsight.data.SeedData
import com.example.rehabinsight.data.SetupAnswers
import com.example.rehabinsight.data.StreakMilestone
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.data.TaskCategory
import com.example.rehabinsight.data.TaskRule
import com.example.rehabinsight.data.UniversalAppSetting
import com.example.rehabinsight.data.network.signUpClient
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.theme.RehabTextMuted
import com.example.rehabinsight.ui.theme.color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * The full in-memory relational store, shaped exactly like the ERD (one list per table).
 * There is no remote database in this app, so this StateFlow *is* the database.
 */
@Immutable
data class RehabUiState(
    val admins: List<Admin> = SeedData.admins,
    val clients: List<Client> = SeedData.demoClients,
    val clientStreaks: List<ClientStreak> = SeedData.demoClientStreaks,
    val categories: List<Category> = SeedData.categories,
    val articles: List<Article> = SeedData.articles,
    val tasks: List<Task> = SeedData.tasks,
    val taskCategories: List<TaskCategory> = SeedData.taskCategories,
    val taskRules: List<TaskRule> = SeedData.taskRules,
    val clientTasks: List<ClientTask> = SeedData.demoClientTasks,
    val questionTemplates: List<QuestionTemplate> = SeedData.questionTemplates,
    val dailyQuestions: List<DailyQuestion> = emptyList(),
    val dailyCheckins: List<DailyCheckin> = emptyList(),
    val responses: List<Response> = emptyList(),
    val streakMilestones: List<StreakMilestone> = SeedData.streakMilestones,
    val universalAppSettings: List<UniversalAppSetting> = SeedData.universalAppSettings,

    val currentClientId: Int? = null,
    val isAdminMode: Boolean = false,
    val authError: String? = null,
    val pendingWelcomeBack: Boolean = false,
    val pendingMilestoneDays: Int? = null,
    val isNewBestStreak: Boolean = false,
    /** Today's raw Daily Check-In answers, kept transiently to drive [GuidanceEngine] highlighting. */
    val latestCheckIn: CheckInAnswers? = null,

    // Simple auto-increment counters standing in for AUTO_INCREMENT primary keys.
    val nextClientId: Int = 1,
    val nextClientTaskId: Int = 1000,
    val nextTaskId: Int = SeedData.FIRST_TASK_ID_AFTER_LIBRARY,
    val nextDailyQuestionId: Int = 1,
    val nextCheckinId: Int = 1,
    val nextResponseId: Int = 1
) {
    val currentClient: Client? get() = clients.find { it.clientId == currentClientId }
    val isLoggedIn: Boolean get() = currentClientId != null

    fun settingInt(key: String, default: Int): Int =
        universalAppSettings.find { it.settingKey == key }?.settingValue?.toIntOrNull() ?: default
}

/** Read-only, admin-facing rollup of one client's progress (derived from Client/ClientStreak/client_task). */
data class ClientProgressSummary(
    val client: Client,
    val completionRateToday: Float,
    val currentStreak: Int,
    val bestStreak: Int
)

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RehabUiState())
    val uiState: StateFlow<RehabUiState> = _uiState.asStateFlow()

    private fun today(): LocalDate = LocalDate.now()

    // ---------------------------------------------------------------------
    // Auth (Client / Admin)
    // ---------------------------------------------------------------------

    fun signUp(name: String, email: String, phone: String, password: String, onResult: (Boolean) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim().ifBlank { null }
        if (name.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(authError = "Please fill in every field.") }
            onResult(false)
            return
        }

        val nameParts = name.trim().split(" ", limit = 2)
        val firstName = nameParts.getOrElse(0) { name.trim() }
        val lastName = nameParts.getOrElse(1) { "" }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                signUpClient(firstName, lastName, trimmedEmail, trimmedPhone, password)
            }

            if (result.success && result.clientId != null) {
                val newClientId = result.clientId
                val state = _uiState.value
                val newClient = Client(
                    clientId = newClientId,
                    adminId = null,
                    firstName = firstName,
                    lastName = lastName,
                    email = trimmedEmail,
                    phone = trimmedPhone,
                    passwordHash = password
                )
                val newQuestions = state.questionTemplates.mapIndexed { index, template ->
                    DailyQuestion(
                        questionId = state.nextDailyQuestionId + index,
                        categoryId = template.categoryId,
                        clientId = newClientId,
                        questionOrder = template.questionOrder,
                        questionText = template.questionText
                    )
                }

                _uiState.update {
                    it.copy(
                        clients = it.clients + newClient,
                        clientStreaks = it.clientStreaks + ClientStreak(clientId = newClientId),
                        dailyQuestions = it.dailyQuestions + newQuestions,
                        currentClientId = newClientId,
                        authError = null,
                        isAdminMode = false,
                        nextDailyQuestionId = it.nextDailyQuestionId + newQuestions.size
                    )
                }
                onResult(true)
            } else {
                _uiState.update { it.copy(authError = result.errorMessage ?: "Sign up failed.") }
                onResult(false)
            }
        }
    }

    fun login(email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        val match = _uiState.value.clients.find {
            it.email.equals(trimmedEmail, ignoreCase = true) && it.passwordHash == password
        }
        return if (match != null) {
            _uiState.update { it.copy(currentClientId = match.clientId, authError = null, isAdminMode = false) }
            true
        } else {
            _uiState.update { it.copy(authError = "We couldn't find a matching account. Check your details or create an account.") }
            false
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun logout() {
        _uiState.update { it.copy(currentClientId = null, isAdminMode = false) }
    }

    fun adminLogin(passcode: String): Boolean {
        return if (_uiState.value.admins.any { it.passwordHash == passcode }) {
            _uiState.update { it.copy(isAdminMode = true, currentClientId = null, authError = null) }
            true
        } else {
            _uiState.update { it.copy(authError = "Incorrect admin passcode.") }
            false
        }
    }

    fun adminLogout() {
        _uiState.update { it.copy(isAdminMode = false) }
    }

    // ---------------------------------------------------------------------
    // Setup questionnaire -> Response rows -> rule-driven checklist generation
    // ---------------------------------------------------------------------

    fun submitSetupAnswers(answers: SetupAnswers) {
        val clientId = _uiState.value.currentClientId ?: return
        val state = _uiState.value

        val questionsByOrder = state.dailyQuestions
            .filter { it.clientId == clientId && it.questionOrder < 100 }
            .associateBy { it.questionOrder }

        // Store every raw setup answer (its option index, or 1/0 for goal booleans) for audit/
        // history purposes only - the actual checklist-generation trigger logic below reads the
        // typed SetupAnswers directly (see ChecklistGenerator), since several sets share a colour
        // category but fire on independent questions.
        val rawAnswers = mapOf(
            1 to answers.sleep.ordinal,
            2 to answers.stress.ordinal,
            3 to answers.mood.ordinal,
            4 to answers.connection.ordinal,
            5 to answers.motivation.ordinal,
            6 to answers.activity.ordinal,
            7 to answers.routine.ordinal,
            8 to answers.energy.ordinal,
            9 to answers.pain.ordinal,
            10 to (Goal.SLEEP in answers.goals).toInt(),
            11 to (Goal.MOOD in answers.goals).toInt(),
            12 to (Goal.STRESS in answers.goals).toInt(),
            13 to (Goal.ENERGY in answers.goals).toInt(),
            14 to (Goal.CONNECTION in answers.goals).toInt(),
            15 to (Goal.PHYSICAL_HEALTH in answers.goals).toInt()
        )

        val checkinId = state.nextCheckinId
        val checkin = DailyCheckin(checkinId, clientId, today(), completedAt = LocalDateTime.now())

        var responseId = state.nextResponseId
        val newResponses = mutableListOf<Response>()
        rawAnswers.forEach { (order, value) ->
            val question = questionsByOrder[order] ?: return@forEach
            newResponses += Response(responseId, question.questionId, checkinId, value)
            responseId++
        }

        val taskIds = ChecklistGenerator.generateTaskIds(answers)

        var nextClientTaskId = state.nextClientTaskId
        val now = LocalDateTime.now()
        val newClientTasks = taskIds.map { taskId ->
            ClientTask(
                clientTaskId = nextClientTaskId++,
                clientId = clientId,
                taskId = taskId,
                assignedAt = now,
                dueDate = today(),
                status = ClientTaskStatus.PENDING
            )
        }

        _uiState.update {
            it.copy(
                dailyCheckins = it.dailyCheckins + checkin,
                responses = it.responses + newResponses,
                clientTasks = it.clientTasks + newClientTasks,
                nextCheckinId = checkinId + 1,
                nextResponseId = responseId,
                nextClientTaskId = nextClientTaskId
            )
        }
    }

    private fun Boolean.toInt() = if (this) 1 else 0

    // ---------------------------------------------------------------------
    // Daily flow helpers
    // ---------------------------------------------------------------------

    /** True once the client has completed the Initial Setup Questionnaire (i.e. has any client_task rows). */
    fun hasCompletedSetup(): Boolean {
        val clientId = _uiState.value.currentClientId ?: return false
        return _uiState.value.clientTasks.any { it.clientId == clientId }
    }

    fun hasCheckedInToday(): Boolean {
        val clientId = _uiState.value.currentClientId ?: return false
        return _uiState.value.dailyCheckins.any { it.clientId == clientId && it.checkinDate == today() }
    }

    /** Ensures today's client_task rows exist, carrying forward yesterday's active task set. Idempotent. */
    fun ensureTodayAssignments() {
        val clientId = _uiState.value.currentClientId ?: return
        val state = _uiState.value
        val hasToday = state.clientTasks.any { it.clientId == clientId && it.dueDate == today() }
        if (hasToday) return

        val priorRows = state.clientTasks.filter { it.clientId == clientId && it.status != ClientTaskStatus.REMOVED }
        val mostRecentDate = priorRows.mapNotNull { it.dueDate }.maxOrNull() ?: return
        val carryTaskIds = priorRows
            .filter { it.dueDate == mostRecentDate && it.taskId != SeedData.SELF_COMPASSION_TASK_ID }
            .map { it.taskId }
            .distinct()
        if (carryTaskIds.isEmpty()) return

        val now = LocalDateTime.now()
        var nextId = state.nextClientTaskId
        val newRows = carryTaskIds.map { taskId ->
            ClientTask(
                clientTaskId = nextId++,
                clientId = clientId,
                taskId = taskId,
                assignedAt = now,
                dueDate = today(),
                status = ClientTaskStatus.PENDING
            )
        }
        _uiState.update { it.copy(clientTasks = it.clientTasks + newRows, nextClientTaskId = nextId) }
    }

    /** Call once when entering the Daily Check-In flow to detect a missed day. */
    fun checkForMissedDay() {
        val clientId = _uiState.value.currentClientId ?: return
        ensureTodayAssignments()
        val state = _uiState.value
        val streak = state.clientStreaks.find { it.clientId == clientId } ?: return
        val last = streak.lastStreakDate ?: return
        val thresholdDays = state.settingInt("missed_day_threshold_days", 2)
        val gap = ChronoUnit.DAYS.between(last, today())

        val alreadyHasSelfCompassionToday = state.clientTasks.any {
            it.clientId == clientId && it.taskId == SeedData.SELF_COMPASSION_TASK_ID && it.dueDate == today()
        }
        if (gap >= thresholdDays && !alreadyHasSelfCompassionToday) {
            val newRow = ClientTask(
                clientTaskId = _uiState.value.nextClientTaskId,
                clientId = clientId,
                taskId = SeedData.SELF_COMPASSION_TASK_ID,
                assignedAt = LocalDateTime.now(),
                dueDate = today(),
                status = ClientTaskStatus.PENDING
            )
            _uiState.update {
                it.copy(
                    clientTasks = it.clientTasks + newRow,
                    nextClientTaskId = it.nextClientTaskId + 1,
                    pendingWelcomeBack = true
                )
            }
        }
    }

    fun dismissWelcomeBack() {
        _uiState.update { it.copy(pendingWelcomeBack = false) }
    }

    fun submitDailyCheckIn(checkIn: CheckInAnswers) {
        val clientId = _uiState.value.currentClientId ?: return
        ensureTodayAssignments()
        val state = _uiState.value

        val questionsByOrder = state.dailyQuestions
            .filter { it.clientId == clientId && it.questionOrder in 100..199 }
            .associateBy { it.questionOrder }

        val rawAnswers = buildMap {
            put(101, GuidanceEngine.answerValue(checkIn.mood))
            put(102, GuidanceEngine.answerValue(checkIn.energy))
            put(103, GuidanceEngine.answerValue(checkIn.stress))
            put(104, GuidanceEngine.answerValue(checkIn.sleep))
            put(105, GuidanceEngine.answerValue(checkIn.motivation))
            checkIn.pain?.let { put(106, GuidanceEngine.answerValue(it)) }
        }

        val checkinId = state.nextCheckinId
        val checkinRow = DailyCheckin(checkinId, clientId, checkIn.date, completedAt = LocalDateTime.now())

        var responseId = state.nextResponseId
        val newResponses = mutableListOf<Response>()
        rawAnswers.forEach { (order, value) ->
            val question = questionsByOrder[order] ?: return@forEach
            newResponses += Response(responseId, question.questionId, checkinId, value)
            responseId++
        }

        _uiState.update {
            it.copy(
                dailyCheckins = it.dailyCheckins + checkinRow,
                responses = it.responses + newResponses,
                nextCheckinId = checkinId + 1,
                nextResponseId = responseId,
                latestCheckIn = checkIn
            )
        }
    }

    /** The backend colour categories tied to the client's self-chosen goals (Q10) at setup. */
    private fun goalCategoryIds(clientId: Int): Set<Int> {
        val state = _uiState.value
        val goalQuestionIds = state.dailyQuestions
            .filter { it.clientId == clientId && it.questionOrder in 10..15 }
            .associateBy { it.questionId }
        val flaggedCategoryIds = state.responses
            .filter { it.questionId in goalQuestionIds.keys && it.answer == 1 }
            .mapNotNull { goalQuestionIds[it.questionId]?.categoryId }
        return flaggedCategoryIds.toSet()
    }

    /** Today's active tasks that sit in one of the client's goal categories - used to "build on momentum" when feeling good. */
    fun goalAreaTaskIds(): List<Int> {
        val clientId = _uiState.value.currentClientId ?: return emptyList()
        val cats = goalCategoryIds(clientId)
        if (cats.isEmpty()) return emptyList()
        val categoryIdByTaskId = _uiState.value.taskCategories.associate { it.taskId to it.categoryId }
        return todaysChecklistItems().map { it.taskId }.filter { categoryIdByTaskId[it] in cats }
    }

    /** The client's checklist for today: client_task rows joined to Task + Task_Category + Category. */
    fun todaysChecklistItems(): List<ChecklistItem> {
        val clientId = _uiState.value.currentClientId ?: return emptyList()
        val state = _uiState.value
        val today = today()
        val tasksById = state.tasks.associateBy { it.taskId }
        val categoryIdByTaskId = state.taskCategories.associate { it.taskId to it.categoryId }
        val categoriesById = state.categories.associateBy { it.categoryId }

        return state.clientTasks
            .filter { it.clientId == clientId && it.dueDate == today && it.status != ClientTaskStatus.REMOVED }
            .sortedBy { it.clientTaskId }
            .mapNotNull { clientTask ->
                val task = tasksById[clientTask.taskId] ?: return@mapNotNull null
                val categoryId = categoryIdByTaskId[clientTask.taskId]
                ChecklistItem(
                    clientTaskId = clientTask.clientTaskId,
                    taskId = task.taskId,
                    title = task.title,
                    categoryId = categoryId,
                    categoryColor = categoriesById[categoryId]?.color ?: RehabTextMuted,
                    completed = clientTask.status == ClientTaskStatus.COMPLETED
                )
            }
    }

    /** Part 5: highlights 2-4 already-assigned tasks based on today's check-in only. */
    fun todaysHighlightedItems(): List<ChecklistItem> {
        _uiState.value.currentClientId ?: return emptyList()
        val state = _uiState.value
        val checkIn = state.latestCheckIn?.takeIf { it.date == today() } ?: return emptyList()
        val items = todaysChecklistItems()
        val tasksById = state.tasks.associateBy { it.taskId }

        val highlightIds = GuidanceEngine.highlightTaskIds(
            checkIn = checkIn,
            activeTaskIds = items.map { it.taskId },
            tasksById = tasksById,
            goalAreaTaskIds = goalAreaTaskIds(),
            minTasks = state.settingInt("min_daily_highlighted_tasks", 2),
            maxTasks = state.settingInt("max_daily_highlighted_tasks", 4)
        )
        return highlightIds.mapNotNull { taskId -> items.find { it.taskId == taskId } }
    }

    fun toggleTaskCompletion(clientTaskId: Int) {
        val clientId = _uiState.value.currentClientId ?: return
        val state = _uiState.value
        val row = state.clientTasks.find { it.clientTaskId == clientTaskId } ?: return
        val nowCompleting = row.status != ClientTaskStatus.COMPLETED
        val updatedRow = row.copy(
            status = if (nowCompleting) ClientTaskStatus.COMPLETED else ClientTaskStatus.PENDING,
            completedAt = if (nowCompleting) LocalDateTime.now() else null
        )
        val newClientTasks = state.clientTasks.map { if (it.clientTaskId == clientTaskId) updatedRow else it }

        var newStreaks = state.clientStreaks
        var milestoneDays: Int? = null
        var newBestReached = false

        if (nowCompleting) {
            val alreadyCompletedAnyToday = state.clientTasks.any {
                it.clientId == clientId && it.dueDate == today() &&
                    it.status == ClientTaskStatus.COMPLETED && it.clientTaskId != clientTaskId
            }
            if (!alreadyCompletedAnyToday) {
                val streak = state.clientStreaks.find { it.clientId == clientId } ?: ClientStreak(clientId)
                val newCurrent = when {
                    streak.lastStreakDate == null -> 1
                    streak.lastStreakDate == today().minusDays(1) -> streak.currentStreak + 1
                    streak.lastStreakDate == today() -> streak.currentStreak
                    else -> 1
                }
                val newBest = maxOf(streak.bestStreak, newCurrent)
                newBestReached = newBest > streak.bestStreak
                val updatedStreak = streak.copy(currentStreak = newCurrent, bestStreak = newBest, lastStreakDate = today())
                newStreaks = if (state.clientStreaks.any { it.clientId == clientId }) {
                    state.clientStreaks.map { if (it.clientId == clientId) updatedStreak else it }
                } else {
                    state.clientStreaks + updatedStreak
                }
                milestoneDays = state.streakMilestones.find { it.days == newCurrent }?.days
            }
        }

        _uiState.update {
            it.copy(
                clientTasks = newClientTasks,
                clientStreaks = newStreaks,
                pendingMilestoneDays = milestoneDays ?: it.pendingMilestoneDays,
                isNewBestStreak = if (milestoneDays != null) newBestReached else it.isNewBestStreak
            )
        }
    }

    fun dismissMilestone() {
        _uiState.update { it.copy(pendingMilestoneDays = null, isNewBestStreak = false) }
    }

    fun milestoneMessage(days: Int): String =
        _uiState.value.streakMilestones.find { it.days == days }?.message
            ?: "$days day streak! Celebrate consistency, not perfection."

    // ---------------------------------------------------------------------
    // Task customisation (adds/edits Task + Task_Category + client_task rows)
    // ---------------------------------------------------------------------

    fun addCustomTask(title: String, categoryId: Int) {
        if (title.isBlank()) return
        val clientId = _uiState.value.currentClientId ?: return
        ensureTodayAssignments()
        val state = _uiState.value
        val newTaskId = state.nextTaskId
        val newTask = Task(taskId = newTaskId, title = title.trim())
        val newTaskCategory = TaskCategory(categoryId = categoryId, taskId = newTaskId, difficulty = 1)
        val newClientTask = ClientTask(
            clientTaskId = state.nextClientTaskId,
            clientId = clientId,
            taskId = newTaskId,
            assignedAt = LocalDateTime.now(),
            dueDate = today(),
            status = ClientTaskStatus.PENDING
        )
        _uiState.update {
            it.copy(
                tasks = it.tasks + newTask,
                taskCategories = it.taskCategories + newTaskCategory,
                clientTasks = it.clientTasks + newClientTask,
                nextTaskId = newTaskId + 1,
                nextClientTaskId = state.nextClientTaskId + 1
            )
        }
    }

    /** Removes a task from today's checklist (and therefore from tomorrow's carry-forward). */
    fun removeTask(clientTaskId: Int) {
        _uiState.update { state ->
            state.copy(
                clientTasks = state.clientTasks.map {
                    if (it.clientTaskId == clientTaskId) it.copy(status = ClientTaskStatus.REMOVED) else it
                }
            )
        }
    }

    fun updateTask(taskId: Int, newTitle: String, newCategoryId: Int) {
        if (newTitle.isBlank()) return
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { if (it.taskId == taskId) it.copy(title = newTitle.trim()) else it },
                taskCategories = state.taskCategories.map {
                    if (it.taskId == taskId) it.copy(categoryId = newCategoryId) else it
                }
            )
        }
    }

    // ---------------------------------------------------------------------
    // Insights (Client-facing Profile + Admin Dashboard)
    // ---------------------------------------------------------------------

    fun weeklyConsistency(): List<Boolean> {
        val clientId = _uiState.value.currentClientId ?: return List(7) { false }
        val state = _uiState.value
        return (6 downTo 0).map { offset ->
            val date = today().minusDays(offset.toLong())
            state.clientTasks.any {
                it.clientId == clientId && it.dueDate == date && it.status == ClientTaskStatus.COMPLETED
            }
        }
    }

    fun todayCompletionText(): Pair<Int, Int> {
        val items = todaysChecklistItems()
        return items.count { it.completed } to items.size
    }

    /** Admin Dashboard: read-only rollup for every client in the system (derived, not mocked). */
    fun clientProgressSummaries(): List<ClientProgressSummary> {
        val state = _uiState.value
        val today = today()
        return state.clients.map { client ->
            val todaysRows = state.clientTasks.filter {
                it.clientId == client.clientId && it.dueDate == today && it.status != ClientTaskStatus.REMOVED
            }
            val completed = todaysRows.count { it.status == ClientTaskStatus.COMPLETED }
            val rate = if (todaysRows.isEmpty()) 0f else completed.toFloat() / todaysRows.size
            val streak = state.clientStreaks.find { it.clientId == client.clientId }
            ClientProgressSummary(client, rate, streak?.currentStreak ?: 0, streak?.bestStreak ?: 0)
        }
    }

    /** Admin Dashboard: the master task library grouped by Category (via Task_Category). */
    fun tasksByCategory(): Map<Category, List<Task>> {
        val state = _uiState.value
        val categoryIdByTaskId = state.taskCategories.associate { it.taskId to it.categoryId }
        return state.categories.associateWith { category ->
            state.tasks.filter { task ->
                task.isActive &&
                    task.taskId != SeedData.SELF_COMPASSION_TASK_ID &&
                    categoryIdByTaskId[task.taskId] == category.categoryId
            }
        }
    }

    fun activeArticles(): List<Article> = _uiState.value.articles.filter { it.isActive }.sortedBy { it.sortOrder }
}
