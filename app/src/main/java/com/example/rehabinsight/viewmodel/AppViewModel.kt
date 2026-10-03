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
import com.example.rehabinsight.data.FiveLevel
import com.example.rehabinsight.data.GuidanceEngine
import com.example.rehabinsight.data.Goal
import com.example.rehabinsight.data.LocalIds
import com.example.rehabinsight.data.QuestionTemplate
import com.example.rehabinsight.data.Response
import com.example.rehabinsight.data.SeedData
import com.example.rehabinsight.data.SetupAnswers
import com.example.rehabinsight.data.StreakMilestone
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.data.TaskCategory
import com.example.rehabinsight.data.TaskRule
import com.example.rehabinsight.data.UniversalAppSetting
import com.example.rehabinsight.data.network.Caller
import com.example.rehabinsight.data.network.createClientTask
import com.example.rehabinsight.data.network.createDailyCheckin
import com.example.rehabinsight.data.network.createDailyQuestion
import com.example.rehabinsight.data.network.createResponse
import com.example.rehabinsight.data.network.createTask
import com.example.rehabinsight.data.network.createTaskCategory
import com.example.rehabinsight.data.network.fetchArticles
import com.example.rehabinsight.data.network.fetchCategories
import com.example.rehabinsight.data.network.fetchClientStreaks
import com.example.rehabinsight.data.network.fetchClientTasks
import com.example.rehabinsight.data.network.fetchClients
import com.example.rehabinsight.data.network.fetchDailyCheckins
import com.example.rehabinsight.data.network.fetchDailyQuestions
import com.example.rehabinsight.data.network.fetchQuestionTemplates
import com.example.rehabinsight.data.network.fetchResponses
import com.example.rehabinsight.data.network.fetchStreakMilestones
import com.example.rehabinsight.data.network.fetchTaskCategories
import com.example.rehabinsight.data.network.fetchTaskRules
import com.example.rehabinsight.data.network.fetchTasks
import com.example.rehabinsight.data.network.fetchUniversalAppSettings
import com.example.rehabinsight.data.network.loginClient
import com.example.rehabinsight.data.network.logoutSession
import com.example.rehabinsight.data.network.saveClientStreak
import com.example.rehabinsight.data.network.saveClientTaskStatus
import com.example.rehabinsight.data.network.saveTaskCategory
import com.example.rehabinsight.data.network.saveTaskChanges
import com.example.rehabinsight.data.network.signUpClient
import com.example.rehabinsight.data.network.validateSession
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.theme.RehabTextMuted
import com.example.rehabinsight.ui.theme.color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * The app's working copy of the database, shaped exactly like the ERD (one list per table).
 * Screens read and change this copy; [AppViewModel] loads it from the server where it can and
 * sends every change back. Until the server answers, each list starts from the built-in data.
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
    /** True while a sign-in or sign-up is waiting on the server. */
    val authInProgress: Boolean = false,
    val pendingWelcomeBack: Boolean = false,
    val pendingMilestoneDays: Int? = null,
    val isNewBestStreak: Boolean = false,
    /** Today's raw Daily Check-In answers, kept transiently to drive [GuidanceEngine] highlighting. */
    val latestCheckIn: CheckInAnswers? = null,

    // Ids for rows made on this device. Each is swapped for the server's own id once the row
    // has been saved there, so they start in the local range (see LocalIds).
    val nextClientId: Int = 1,
    val nextClientTaskId: Int = LocalIds.FIRST + 1000,
    val nextTaskId: Int = LocalIds.FIRST + SeedData.FIRST_TASK_ID_AFTER_LIBRARY,
    val nextDailyQuestionId: Int = LocalIds.FIRST + 1,
    val nextCheckinId: Int = LocalIds.FIRST + 1,
    val nextResponseId: Int = LocalIds.FIRST + 1
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

/**
 * Rows from the server laid over the ones the app already holds: a row with the same [key]
 * is replaced in place, any other is added at the end. A failed or empty fetch changes nothing.
 */
private fun <T, K> List<T>.mergedWith(incoming: List<T>?, key: (T) -> K): List<T> {
    if (incoming.isNullOrEmpty()) return this
    val incomingByKey = incoming.associateBy(key)
    val heldKeys = mapTo(HashSet()) { key(it) }
    return map { incomingByKey[key(it)] ?: it } + incoming.filter { key(it) !in heldKeys }
}

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RehabUiState())
    val uiState: StateFlow<RehabUiState> = _uiState.asStateFlow()

    /** Tables whose rows are made on this device first and renumbered once the server has saved them. */
    private enum class Table { TASK, CLIENT_TASK, DAILY_QUESTION, DAILY_CHECKIN, RESPONSE }

    /** The signed-in client's Client_Session token. Held in memory only, for this run of the app. */
    private var sessionToken: String? = null

    /** The Admin whose passcode opened the dashboard, while it is open. */
    private var signedInAdminId: Int? = null

    /** The server's id for each row this run of the app first saved under a local id. */
    private val serverIds = mutableMapOf<Pair<Table, Int>, Int>()

    /**
     * Tasks the signed-in client added themselves since signing in. Task rows are shared by
     * every client, so these are the only ones a client's edits are sent to the server for.
     */
    private val ownTaskIds = mutableSetOf<Int>()

    /** Lets one exchange with the server finish before the next begins, so a row is always saved before anything that refers to it. */
    private val serverQueue = Mutex()

    /** Changes still to be sent, oldest first, each with the caller it was made for. */
    private val unsent = ArrayDeque<Pair<Caller, suspend (Caller) -> Boolean>>()

    init {
        loadSharedTables()
    }

    private fun today(): LocalDate = LocalDate.now()

    // ---------------------------------------------------------------------
    // Server link
    // ---------------------------------------------------------------------

    private suspend fun <T> onServer(call: () -> T): T = withContext(Dispatchers.IO) { call() }

    private fun currentCaller(): Caller =
        if (_uiState.value.isAdminMode) Caller(adminId = signedInAdminId) else Caller(sessionToken = sessionToken)

    /**
     * Sends a change the app has already made to its own copy on to the server, after every
     * change queued before it. [write] brings the server up to date with the app's copy and
     * returns false only when the server couldn't be reached; the change is then kept and tried
     * again ahead of the next one. A change the server refuses is dropped, and the app's copy
     * simply stays as it is.
     */
    private fun mirror(write: suspend (Caller) -> Boolean) {
        val caller = currentCaller()
        viewModelScope.launch {
            serverQueue.withLock {
                unsent += caller to write
                sendUnsent()
            }
        }
    }

    /** Works through [unsent] in order, stopping at the first change that still can't reach the server. */
    private suspend fun sendUnsent() {
        while (unsent.isNotEmpty()) {
            val (caller, write) = unsent.first()
            if (!write(caller)) return
            unsent.removeFirst()
        }
    }

    /** The id a row has on the server, or null while it exists only on this device. */
    private fun serverId(table: Table, id: Int): Int? =
        if (LocalIds.isLocal(id)) serverIds[table to id] else id

    /** The id a row goes by now, for a caller that may still be holding the local id it started with. */
    private fun currentId(table: Table, id: Int): Int = serverIds[table to id] ?: id

    /** Swaps a row's local id for the one the server gave it, everywhere that id appears. */
    private fun adoptServerId(table: Table, localId: Int, serverId: Int) {
        serverIds[table to localId] = serverId
        if (table == Table.TASK && localId in ownTaskIds) ownTaskIds += serverId
        _uiState.update { state ->
            when (table) {
                Table.TASK -> state.copy(
                    tasks = state.tasks.map { if (it.taskId == localId) it.copy(taskId = serverId) else it },
                    taskCategories = state.taskCategories.map { if (it.taskId == localId) it.copy(taskId = serverId) else it },
                    taskRules = state.taskRules.map { if (it.taskId == localId) it.copy(taskId = serverId) else it },
                    clientTasks = state.clientTasks.map { if (it.taskId == localId) it.copy(taskId = serverId) else it }
                )
                Table.CLIENT_TASK -> state.copy(
                    clientTasks = state.clientTasks.map { if (it.clientTaskId == localId) it.copy(clientTaskId = serverId) else it }
                )
                Table.DAILY_QUESTION -> state.copy(
                    dailyQuestions = state.dailyQuestions.map { if (it.questionId == localId) it.copy(questionId = serverId) else it },
                    responses = state.responses.map { if (it.questionId == localId) it.copy(questionId = serverId) else it }
                )
                Table.DAILY_CHECKIN -> state.copy(
                    dailyCheckins = state.dailyCheckins.map { if (it.checkinId == localId) it.copy(checkinId = serverId) else it },
                    responses = state.responses.map { if (it.checkinId == localId) it.copy(checkinId = serverId) else it }
                )
                Table.RESPONSE -> state.copy(
                    responses = state.responses.map { if (it.responseId == localId) it.copy(responseId = serverId) else it }
                )
            }
        }
    }

    /**
     * Category, Article, Task, Task_Category, Task_Rule, Question_Template, Streak_Milestone and
     * Universal_App_Setting: the tables every client shares. Built-in rows the server doesn't
     * return are kept, because the checklist and guidance rules refer to the built-in task ids.
     */
    private fun loadSharedTables() {
        viewModelScope.launch {
            val categories = onServer { fetchCategories() }
            val articles = onServer { fetchArticles() }
            val tasks = onServer { fetchTasks() }
            val taskCategories = onServer { fetchTaskCategories() }
            val taskRules = onServer { fetchTaskRules() }
            val questionTemplates = onServer { fetchQuestionTemplates() }
            val settings = onServer { fetchUniversalAppSettings() }
            val milestones = onServer { fetchStreakMilestones() }

            _uiState.update { state ->
                // timeLabel and isVigorous have no database column, so they carry over from the app's own row.
                val heldTasks = state.tasks.associateBy { it.taskId }
                val serverTasks = tasks?.map { task ->
                    heldTasks[task.taskId]?.let { task.copy(timeLabel = it.timeLabel, isVigorous = it.isVigorous) } ?: task
                }
                state.copy(
                    categories = state.categories.mergedWith(categories) { it.categoryId },
                    articles = state.articles.mergedWith(articles) { it.articleId },
                    tasks = state.tasks.mergedWith(serverTasks) { it.taskId },
                    // The app gives each task one colour, so a task's row is matched on task_id alone.
                    taskCategories = state.taskCategories.mergedWith(taskCategories) { it.taskId },
                    taskRules = state.taskRules.mergedWith(taskRules) { it.ruleId },
                    questionTemplates = state.questionTemplates.mergedWith(questionTemplates) { it.templateQuestionId },
                    universalAppSettings = state.universalAppSettings.mergedWith(settings) { it.settingKey },
                    streakMilestones = if (milestones.isNotEmpty()) {
                        milestones.map { dto -> StreakMilestone(dto.milestoneId, dto.days, dto.message) }
                    } else {
                        state.streakMilestones
                    }
                )
            }
        }
    }

    /**
     * Client, Client_Streak, Client_Task, Daily_Checkin, Response and Daily_Question for the
     * client who has just signed in. Whatever the server can't supply is left as the app has it.
     */
    private suspend fun loadClientFromServer(clientId: Int, email: String) = serverQueue.withLock {
        sendUnsent()
        val caller = Caller(sessionToken = sessionToken)
        val profile = onServer { fetchClients(caller, clientId) }?.find { it.clientId == clientId }
        val streak = onServer { fetchClientStreaks(caller, clientId) }?.find { it.clientId == clientId }
        val clientTasks = onServer { fetchClientTasks(caller, clientId) }?.sortedBy { it.clientTaskId }
        val checkins = onServer { fetchDailyCheckins(caller, clientId) }
        val responses = onServer { fetchResponses(caller, clientId) }

        _uiState.update { state ->
            val held = state.clients.find { it.clientId == clientId }
            val client = when {
                profile != null -> profile.copy(passwordHash = held?.passwordHash.orEmpty())
                held != null -> held
                // Until the server can return the profile, the email they signed in with is all the app knows.
                else -> Client(
                    clientId = clientId,
                    firstName = email.substringBefore('@'),
                    lastName = "",
                    email = email,
                    passwordHash = ""
                )
            }
            val clientStreak = streak ?: state.clientStreaks.find { it.clientId == clientId } ?: ClientStreak(clientId)
            state.copy(
                clients = if (held == null) state.clients + client else state.clients.mergedWith(listOf(client)) { it.clientId },
                clientStreaks = if (state.clientStreaks.any { it.clientId == clientId }) {
                    state.clientStreaks.mergedWith(listOf(clientStreak)) { it.clientId }
                } else {
                    state.clientStreaks + clientStreak
                },
                clientTasks = state.clientTasks.mergedWith(clientTasks) { it.clientTaskId },
                dailyCheckins = state.dailyCheckins.mergedWith(checkins) { it.checkinId },
                responses = state.responses.mergedWith(responses) { it.responseId }
            )
        }
        unsent += caller to { questionCaller -> syncDailyQuestions(questionCaller, clientId) }
        sendUnsent()
    }

    /** Gives a client their own Daily_Question rows, copied from the templates, if they have none yet. */
    private fun addDailyQuestionsFor(clientId: Int) {
        val state = _uiState.value
        if (state.dailyQuestions.any { it.clientId == clientId }) return
        val newQuestions = state.questionTemplates.mapIndexed { index, template ->
            DailyQuestion(
                questionId = state.nextDailyQuestionId + index,
                categoryId = template.categoryId,
                clientId = clientId,
                questionOrder = template.questionOrder,
                questionText = template.questionText
            )
        }
        _uiState.update {
            it.copy(
                dailyQuestions = it.dailyQuestions + newQuestions,
                nextDailyQuestionId = it.nextDailyQuestionId + newQuestions.size
            )
        }
    }

    /**
     * Daily_Question: takes on the server's copy of each of the client's questions where it
     * has one, then sends it the ones it is missing. Returns false if the server couldn't be reached.
     */
    private suspend fun syncDailyQuestions(caller: Caller, clientId: Int): Boolean {
        val onServerAlready = onServer { fetchDailyQuestions(caller, clientId) }
        onServerAlready?.forEach { question ->
            val sameQuestion = _uiState.value.dailyQuestions.find {
                it.clientId == clientId && it.questionOrder == question.questionOrder && LocalIds.isLocal(it.questionId)
            }
            if (sameQuestion != null) adoptServerId(Table.DAILY_QUESTION, sameQuestion.questionId, question.questionId)
        }
        _uiState.update { it.copy(dailyQuestions = it.dailyQuestions.mergedWith(onServerAlready) { q -> q.questionId }) }
        addDailyQuestionsFor(clientId)

        val unsaved = _uiState.value.dailyQuestions.filter { it.clientId == clientId && LocalIds.isLocal(it.questionId) }
        for (question in unsaved) {
            val saved = onServer { createDailyQuestion(caller, question) }
            // The questions are all sent the same way, so if one is refused the rest would be too.
            val savedId = saved.id ?: return saved.serverReached
            adoptServerId(Table.DAILY_QUESTION, question.questionId, savedId)
        }
        return true
    }

    // Each of the writes below may run more than once (see [mirror]), so each starts from what
    // the app's copy says now and sends only what the server doesn't have yet.

    /** Client_Task: saves rows the app has just added to a checklist. */
    private fun mirrorNewClientTasks(clientTaskIds: List<Int>) = mirror { caller ->
        clientTaskIds.all { saveNewClientTask(caller, it) }
    }

    /** Returns false only if the server couldn't be reached. */
    private suspend fun saveNewClientTask(caller: Caller, clientTaskId: Int): Boolean {
        val id = currentId(Table.CLIENT_TASK, clientTaskId)
        if (!LocalIds.isLocal(id)) return true
        // Read afresh: the row may have been ticked off or removed while it waited its turn.
        val row = _uiState.value.clientTasks.find { it.clientTaskId == id } ?: return true
        if (LocalIds.isLocal(row.clientId)) return true
        val taskId = serverId(Table.TASK, row.taskId) ?: return true
        val saved = onServer { createClientTask(caller, row.copy(taskId = taskId)) }
        saved.id?.let { adoptServerId(Table.CLIENT_TASK, id, it) }
        return saved.serverReached
    }

    /** Client_Task: saves a row's status and completion time after it is ticked, un-ticked or removed. */
    private fun mirrorClientTaskStatus(clientTaskId: Int) = mirror { caller ->
        val id = serverId(Table.CLIENT_TASK, clientTaskId) ?: return@mirror true
        val row = _uiState.value.clientTasks.find { it.clientTaskId == id } ?: return@mirror true
        onServer { saveClientTaskStatus(caller, row) }.serverReached
    }

    /** Client_Streak: saves a client's streak after it changes. */
    private fun mirrorStreak(clientId: Int) = mirror { caller ->
        if (LocalIds.isLocal(clientId)) return@mirror true
        val streak = _uiState.value.clientStreaks.find { it.clientId == clientId } ?: return@mirror true
        onServer { saveClientStreak(caller, streak) }.serverReached
    }

    /** Daily_Checkin, then its Response rows (which need the check-in's server id). */
    private fun mirrorCheckin(checkinId: Int) = mirror { caller ->
        var id = currentId(Table.DAILY_CHECKIN, checkinId)
        val checkin = _uiState.value.dailyCheckins.find { it.checkinId == id } ?: return@mirror true
        if (LocalIds.isLocal(checkin.clientId)) return@mirror true
        if (LocalIds.isLocal(id)) {
            val saved = onServer {
                createDailyCheckin(checkin.clientId, checkin.checkinDate, checkin.completedAt, caller.sessionToken)
            }
            val savedId = saved.checkinId ?: return@mirror saved.serverReached
            adoptServerId(Table.DAILY_CHECKIN, id, savedId)
            id = savedId
        }

        val unsavedResponses = _uiState.value.responses.filter { it.checkinId == id && LocalIds.isLocal(it.responseId) }
        unsavedResponses.all { response ->
            val questionId = serverId(Table.DAILY_QUESTION, response.questionId) ?: return@all true
            val saved = onServer { createResponse(caller, response.copy(questionId = questionId)) }
            saved.id?.let { adoptServerId(Table.RESPONSE, response.responseId, it) }
            saved.serverReached
        }
    }

    /** Task and its Task_Category, then (for a client's own task) the Client_Task that puts it on their checklist. */
    private fun mirrorNewTask(taskId: Int, clientTaskId: Int? = null) = mirror { caller ->
        var id = currentId(Table.TASK, taskId)
        if (LocalIds.isLocal(id)) {
            val task = _uiState.value.tasks.find { it.taskId == id } ?: return@mirror true
            val saved = onServer { createTask(caller, task) }
            val savedId = saved.id ?: return@mirror saved.serverReached
            adoptServerId(Table.TASK, id, savedId)
            id = savedId
        }
        // Sent again on a retry; the server turns away a (category_id, task_id) pair it already has.
        val categorySaved = _uiState.value.taskCategories.filter { it.taskId == id }.all { taskCategory ->
            onServer { createTaskCategory(caller, taskCategory) }.serverReached
        }
        categorySaved && (clientTaskId == null || saveNewClientTask(caller, clientTaskId))
    }

    /** Task (title, is_active) and, when [previousCategoryId] shows the colour changed, Task_Category. */
    private fun mirrorTaskChanges(taskId: Int, previousCategoryId: Int? = null) = mirror { caller ->
        val id = serverId(Table.TASK, taskId) ?: return@mirror true
        val state = _uiState.value
        val task = state.tasks.find { it.taskId == id } ?: return@mirror true
        if (!onServer { saveTaskChanges(caller, task) }.serverReached) return@mirror false
        val taskCategory = state.taskCategories.find { it.taskId == id }
        if (taskCategory == null || previousCategoryId == null || previousCategoryId == taskCategory.categoryId) {
            return@mirror true
        }
        onServer { saveTaskCategory(caller, taskCategory, previousCategoryId) }.serverReached
    }

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
        if (_uiState.value.authInProgress) return

        val nameParts = name.trim().split(" ", limit = 2)
        val firstName = nameParts.getOrElse(0) { name.trim() }
        val lastName = nameParts.getOrElse(1) { "" }

        _uiState.update { it.copy(authInProgress = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                signUpClient(SeedData.DEFAULT_ADMIN_ID, firstName, lastName, trimmedEmail, trimmedPhone, password)
            }

            if (result.success && result.clientId != null) {
                val newClientId = result.clientId
                val newClient = Client(
                    clientId = newClientId,
                    adminId = SeedData.DEFAULT_ADMIN_ID,
                    firstName = firstName,
                    lastName = lastName,
                    email = trimmedEmail,
                    phone = trimmedPhone,
                    passwordHash = password
                )
                // Creating a client doesn't start a session, so sign in straight away to get one.
                sessionToken = onServer { loginClient(trimmedEmail, password) }.sessionToken
                ownTaskIds.clear()

                _uiState.update {
                    it.copy(
                        clients = it.clients + newClient,
                        clientStreaks = it.clientStreaks + ClientStreak(clientId = newClientId),
                        currentClientId = newClientId,
                        authError = null,
                        authInProgress = false,
                        isAdminMode = false,
                        latestCheckIn = null
                    )
                }
                addDailyQuestionsFor(newClientId)
                mirror { caller -> syncDailyQuestions(caller, newClientId) }
                onResult(true)
            } else {
                _uiState.update { it.copy(authError = result.errorMessage ?: "Sign up failed.", authInProgress = false) }
                onResult(false)
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter your email and password.") }
            onResult(false)
            return
        }
        if (_uiState.value.authInProgress) return

        _uiState.update { it.copy(authInProgress = true) }
        viewModelScope.launch {
            val signIn = onServer { loginClient(trimmedEmail, password) }
            val token = signIn.sessionToken
            // The login answer carries only the token; validating it says which client it is for.
            val serverClientId = signIn.clientId ?: token?.let { onServer { validateSession(it) } }

            // If the server doesn't sign them in, fall back to the accounts this app already
            // holds: the demo clients, and anyone who signed up since it was opened.
            val clientId = serverClientId ?: _uiState.value.clients.find {
                it.email.equals(trimmedEmail, ignoreCase = true) && it.passwordHash.isNotEmpty() && it.passwordHash == password
            }?.clientId

            if (clientId == null) {
                val message = if (signIn.serverReached) {
                    "We couldn't find a matching account. Check your details or create an account."
                } else {
                    "We couldn't reach the server just now. Please check your connection and try again."
                }
                _uiState.update { it.copy(authError = message, authInProgress = false) }
                onResult(false)
                return@launch
            }

            sessionToken = token.takeIf { serverClientId != null }
            ownTaskIds.clear()
            if (serverClientId != null) loadClientFromServer(serverClientId, trimmedEmail)

            val todaysCheckIn = todaysCheckInFromResponses(clientId)
            _uiState.update {
                it.copy(
                    currentClientId = clientId,
                    authError = null,
                    authInProgress = false,
                    isAdminMode = false,
                    latestCheckIn = todaysCheckIn
                )
            }
            onResult(true)
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun logout() {
        // Queued behind any changes still on their way, so they are saved before the session ends.
        mirror { caller ->
            val token = caller.sessionToken ?: return@mirror true
            onServer { logoutSession(token) }.serverReached
        }
        sessionToken = null
        ownTaskIds.clear()
        _uiState.update { it.copy(currentClientId = null, isAdminMode = false, latestCheckIn = null) }
    }

    fun adminLogin(passcode: String): Boolean {
        val admin = _uiState.value.admins.find { it.passwordHash == passcode }
        return if (admin != null) {
            signedInAdminId = admin.adminId
            _uiState.update { it.copy(isAdminMode = true, currentClientId = null, authError = null) }
            loadSharedTables()
            loadAdminTables(admin.adminId)
            true
        } else {
            _uiState.update { it.copy(authError = "Incorrect admin passcode.") }
            false
        }
    }

    fun adminLogout() {
        signedInAdminId = null
        _uiState.update { it.copy(isAdminMode = false) }
    }

    /**
     * Admin Dashboard: the admin's Client rows with their Client_Streak and today's Client_Task
     * rows. Once the server supplies real clients they take the place of the demo ones.
     */
    private fun loadAdminTables(adminId: Int) {
        viewModelScope.launch {
            serverQueue.withLock {
                val caller = Caller(adminId = adminId)
                val clients = onServer { fetchClients(caller) }
                if (clients.isNullOrEmpty()) return@withLock
                val streaks = onServer { fetchClientStreaks(caller) }
                val todaysTasks = onServer { fetchClientTasks(caller, dueDate = today()) }?.sortedBy { it.clientTaskId }

                _uiState.update { state ->
                    val heldClients = state.clients.associateBy { it.clientId }
                    val serverClients = clients.map { it.copy(passwordHash = heldClients[it.clientId]?.passwordHash.orEmpty()) }
                    state.copy(
                        clients = state.clients.filterNot { LocalIds.isLocal(it.clientId) }
                            .mergedWith(serverClients) { it.clientId },
                        clientStreaks = state.clientStreaks.filterNot { LocalIds.isLocal(it.clientId) }
                            .mergedWith(streaks) { it.clientId },
                        clientTasks = state.clientTasks.filterNot { LocalIds.isLocal(it.clientId) }
                            .mergedWith(todaysTasks) { it.clientTaskId }
                    )
                }
            }
        }
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

        val now = LocalDateTime.now()
        val checkinId = state.nextCheckinId
        val checkin = DailyCheckin(checkinId, clientId, today(), completedAt = now)

        var responseId = state.nextResponseId
        val newResponses = mutableListOf<Response>()
        rawAnswers.forEach { (order, value) ->
            val question = questionsByOrder[order] ?: return@forEach
            newResponses += Response(responseId, question.questionId, checkinId, value)
            responseId++
        }

        val taskIds = ChecklistGenerator.generateTaskIds(answers)

        var nextClientTaskId = state.nextClientTaskId
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
        mirrorCheckin(checkinId)
        mirrorNewClientTasks(newClientTasks.map { it.clientTaskId })
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
        mirrorNewClientTasks(newRows.map { it.clientTaskId })
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
            mirrorNewClientTasks(listOf(newRow.clientTaskId))
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
        mirrorCheckin(checkinId)
    }

    /**
     * Rebuilds today's check-in answers from their Response rows, so the day's guidance is
     * still there after signing in again.
     */
    private fun todaysCheckInFromResponses(clientId: Int): CheckInAnswers? {
        val state = _uiState.value
        val todaysCheckinIds = state.dailyCheckins
            .filter { it.clientId == clientId && it.checkinDate == today() }
            .map { it.checkinId }
            .toSet()
        val orderByQuestionId = state.dailyQuestions
            .filter { it.clientId == clientId && it.questionOrder in 100..199 }
            .associate { it.questionId to it.questionOrder }
        val levelByOrder = state.responses
            .filter { it.checkinId in todaysCheckinIds }
            .mapNotNull { response ->
                val order = orderByQuestionId[response.questionId] ?: return@mapNotNull null
                val level = FiveLevel.entries.find { GuidanceEngine.answerValue(it) == response.answer }
                    ?: return@mapNotNull null
                order to level
            }
            .toMap()

        return CheckInAnswers(
            date = today(),
            mood = levelByOrder[101] ?: return null,
            energy = levelByOrder[102] ?: return null,
            stress = levelByOrder[103] ?: return null,
            sleep = levelByOrder[104] ?: return null,
            motivation = levelByOrder[105] ?: return null,
            pain = levelByOrder[106]
        )
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

        // Rows stay in the order they were added. Their ids can't give that order: a row takes
        // on the server's id once it has been saved there.
        return state.clientTasks
            .filter { it.clientId == clientId && it.dueDate == today && it.status != ClientTaskStatus.REMOVED }
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
        val rowId = currentId(Table.CLIENT_TASK, clientTaskId)
        val row = state.clientTasks.find { it.clientTaskId == rowId } ?: return
        val nowCompleting = row.status != ClientTaskStatus.COMPLETED
        val updatedRow = row.copy(
            status = if (nowCompleting) ClientTaskStatus.COMPLETED else ClientTaskStatus.PENDING,
            completedAt = if (nowCompleting) LocalDateTime.now() else null
        )
        val newClientTasks = state.clientTasks.map { if (it.clientTaskId == rowId) updatedRow else it }

        var newStreaks = state.clientStreaks
        var milestoneDays: Int? = null
        var newBestReached = false

        if (nowCompleting) {
            val alreadyCompletedAnyToday = state.clientTasks.any {
                it.clientId == clientId && it.dueDate == today() &&
                    it.status == ClientTaskStatus.COMPLETED && it.clientTaskId != rowId
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
        mirrorClientTaskStatus(rowId)
        if (newStreaks !== state.clientStreaks) mirrorStreak(clientId)
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
        ownTaskIds += newTaskId
        mirrorNewTask(newTaskId, newClientTask.clientTaskId)
    }

    /** Removes a task from today's checklist (and therefore from tomorrow's carry-forward). */
    fun removeTask(clientTaskId: Int) {
        val rowId = currentId(Table.CLIENT_TASK, clientTaskId)
        _uiState.update { state ->
            state.copy(
                clientTasks = state.clientTasks.map {
                    if (it.clientTaskId == rowId) it.copy(status = ClientTaskStatus.REMOVED) else it
                }
            )
        }
        mirrorClientTaskStatus(rowId)
    }

    /**
     * A client renaming or recolouring a task on their own checklist. A Task row is shared by
     * every client who has that task, so the change goes to the server only for a task this
     * client added themselves; for any other it stays on this device.
     */
    fun updateTask(taskId: Int, newTitle: String, newCategoryId: Int) {
        val id = currentId(Table.TASK, taskId)
        val previousCategoryId = applyTaskEdit(id, newTitle, newCategoryId) ?: return
        if (id in ownTaskIds) mirrorTaskChanges(id, previousCategoryId)
    }

    /** An admin renaming or recolouring a task in the master library. */
    fun updateLibraryTask(taskId: Int, newTitle: String, newCategoryId: Int) {
        val id = currentId(Table.TASK, taskId)
        val previousCategoryId = applyTaskEdit(id, newTitle, newCategoryId) ?: return
        mirrorTaskChanges(id, previousCategoryId)
    }

    /** Applies a rename / recolour to the app's copy. Returns the category the task had before, or null if nothing was changed. */
    private fun applyTaskEdit(taskId: Int, newTitle: String, newCategoryId: Int): Int? {
        if (newTitle.isBlank()) return null
        val previousCategoryId = _uiState.value.taskCategories.find { it.taskId == taskId }?.categoryId ?: newCategoryId
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { if (it.taskId == taskId) it.copy(title = newTitle.trim()) else it },
                taskCategories = state.taskCategories.map {
                    if (it.taskId == taskId) it.copy(categoryId = newCategoryId) else it
                }
            )
        }
        return previousCategoryId
    }

    fun addLibraryTask(title: String, categoryId: Int) {
        if (title.isBlank()) return
        val state = _uiState.value
        val newTaskId = state.nextTaskId
        val newTask = Task(taskId = newTaskId, title = title.trim())
        val newTaskCategory = TaskCategory(categoryId = categoryId, taskId = newTaskId, difficulty = 1)
        _uiState.update {
            it.copy(
                tasks = it.tasks + newTask,
                taskCategories = it.taskCategories + newTaskCategory,
                nextTaskId = newTaskId + 1
            )
        }
        mirrorNewTask(newTaskId)
    }

    fun removeLibraryTask(taskId: Int) {
        val id = currentId(Table.TASK, taskId)
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { if (it.taskId == id) it.copy(isActive = false) else it })
        }
        mirrorTaskChanges(id)
    }

    fun assignTaskToClient(clientId: Int, taskId: Int) {
        val state = _uiState.value
        val date = today()
        val id = currentId(Table.TASK, taskId)
        val alreadyAssigned = state.clientTasks.any {
            it.clientId == clientId && it.taskId == id && it.dueDate == date && it.status != ClientTaskStatus.REMOVED
        }
        if (alreadyAssigned) return
        val newClientTask = ClientTask(
            clientTaskId = state.nextClientTaskId,
            clientId = clientId,
            taskId = id,
            assignedAt = LocalDateTime.now(),
            dueDate = date,
            status = ClientTaskStatus.PENDING
        )
        _uiState.update {
            it.copy(
                clientTasks = it.clientTasks + newClientTask,
                nextClientTaskId = state.nextClientTaskId + 1
            )
        }
        mirrorNewClientTasks(listOf(newClientTask.clientTaskId))
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
