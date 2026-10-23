package com.example.rehabinsight.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.rehabinsight.data.LevelRating
import com.example.rehabinsight.data.LocalIds
import com.example.rehabinsight.data.MoodRating
import com.example.rehabinsight.data.PainLevel
import com.example.rehabinsight.data.Response
import com.example.rehabinsight.data.RoutineRating
import com.example.rehabinsight.data.SetupAnswers
import com.example.rehabinsight.data.SleepRating
import com.example.rehabinsight.data.StreakMilestone
import com.example.rehabinsight.data.StressFrequency
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.data.TaskCategory
import com.example.rehabinsight.data.TaskIds
import com.example.rehabinsight.data.UniversalAppSetting
import com.example.rehabinsight.data.network.Caller
import com.example.rehabinsight.data.network.MIN_PASSWORD_LENGTH
import com.example.rehabinsight.data.network.TaskRows
import com.example.rehabinsight.data.network.UsageReport
import com.example.rehabinsight.data.network.addDailyQuestion
import com.example.rehabinsight.data.network.assignClientTask
import com.example.rehabinsight.data.network.changeClientPassword
import com.example.rehabinsight.data.network.completeClientTask
import com.example.rehabinsight.data.network.createTask
import com.example.rehabinsight.data.network.fetchAdminCategories
import com.example.rehabinsight.data.network.fetchAdminClientStreak
import com.example.rehabinsight.data.network.fetchAdminClientTasks
import com.example.rehabinsight.data.network.fetchAdminClients
import com.example.rehabinsight.data.network.fetchAdminTasks
import com.example.rehabinsight.data.network.fetchAdminUsageReport
import com.example.rehabinsight.data.network.fetchAnswerHistory
import com.example.rehabinsight.data.network.fetchAppSettings
import com.example.rehabinsight.data.network.fetchArticles
import com.example.rehabinsight.data.network.fetchCategories
import com.example.rehabinsight.data.network.fetchCheckedInToday
import com.example.rehabinsight.data.network.fetchCheckinHistory
import com.example.rehabinsight.data.network.fetchClientProfile
import com.example.rehabinsight.data.network.fetchClientStreak
import com.example.rehabinsight.data.network.fetchClientTasks
import com.example.rehabinsight.data.network.fetchDailyQuestions
import com.example.rehabinsight.data.network.fetchStreakMilestones
import com.example.rehabinsight.data.network.fetchTaskLibrary
import com.example.rehabinsight.data.network.loginClient
import com.example.rehabinsight.data.network.logoutSession
import com.example.rehabinsight.data.network.removeDailyQuestion
import com.example.rehabinsight.data.network.resetClientPassword
import com.example.rehabinsight.data.network.sendPasswordResetCode
import com.example.rehabinsight.data.network.saveTaskChanges
import com.example.rehabinsight.data.network.signUpClient
import com.example.rehabinsight.data.network.submitDailyCheckin
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
 * Screens read and change this copy; [AppViewModel] loads it from the server once someone signs
 * in and sends every change back. The app holds no rows of its own: until the server answers,
 * each list is empty.
 */
@Immutable
data class RehabUiState(
    val clients: List<Client> = emptyList(),
    val clientStreaks: List<ClientStreak> = emptyList(),
    val categories: List<Category> = emptyList(),
    val articles: List<Article> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val taskCategories: List<TaskCategory> = emptyList(),
    val clientTasks: List<ClientTask> = emptyList(),
    val dailyQuestions: List<DailyQuestion> = emptyList(),
    val dailyCheckins: List<DailyCheckin> = emptyList(),
    val responses: List<Response> = emptyList(),
    val streakMilestones: List<StreakMilestone> = emptyList(),
    val universalAppSettings: List<UniversalAppSetting> = emptyList(),
    /** The server's totals across all clients, for the dashboard. */
    val usageReport: UsageReport? = null,

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
    val nextTaskId: Int = LocalIds.FIRST + 1,
    val nextDailyQuestionId: Int = LocalIds.FIRST + 1,
    val nextCheckinId: Int = LocalIds.FIRST + 1,
    val nextResponseId: Int = LocalIds.FIRST + 1
) {
    val currentClient: Client? get() = clients.find { it.clientId == currentClientId }
    val isLoggedIn: Boolean get() = currentClientId != null

    /** A Universal_App_Setting value, or null when the database has none under [key]. */
    fun setting(key: String): String? = universalAppSettings.find { it.settingKey == key }?.settingValue

    fun settingInt(key: String, default: Int): Int = setting(key)?.toIntOrNull() ?: default
}

/** Admin-facing rollup of one client's progress and today's checklist (derived from Client/ClientStreak/client_task). */
data class ClientProgressSummary(
    val client: Client,
    val completionRateToday: Float,
    val currentStreak: Int,
    val bestStreak: Int,
    val todaysChecklist: List<ChecklistItem>
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

/** question_order of the setup questionnaire's questions: nine ratings, then the six goals. */
private val SETUP_QUESTION_ORDERS = 1..15

/** The setup goals, by the question_order of the yes/no answer each is stored as. */
private val GOAL_QUESTION_ORDERS = mapOf(
    10 to Goal.SLEEP,
    11 to Goal.MOOD,
    12 to Goal.STRESS,
    13 to Goal.ENERGY,
    14 to Goal.CONNECTION,
    15 to Goal.PHYSICAL_HEALTH
)

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RehabUiState())
    val uiState: StateFlow<RehabUiState> = _uiState.asStateFlow()

    /** Tables whose rows are made on this device first and renumbered once the server has saved them. */
    private enum class Table { TASK, CLIENT_TASK, DAILY_QUESTION, DAILY_CHECKIN }

    /** The signed-in client's Client_Session token. Held in memory only, for this run of the app. */
    private var sessionToken: String? = null

    /**
     * The email and password the server accepted for the admin whose dashboard is open. The
     * server has no admin session, so every admin call carries them; they are held in memory
     * only, and only while the dashboard is open.
     */
    private var adminCaller: Caller? = null

    /** The clients the server sends the signed-in admin: every client, as any admin can see them all. */
    private var adminClientIds: Set<Int> = emptySet()

    /** The server's id for each row this run of the app first saved under a local id. */
    private val serverIds = mutableMapOf<Pair<Table, Int>, Int>()

    /** Lets one exchange with the server finish before the next begins, so a row is always saved before anything that refers to it. */
    private val serverQueue = Mutex()

    /** Changes still to be sent, oldest first, each with the caller it was made for. */
    private val unsent = ArrayDeque<Pair<Caller, suspend (Caller) -> Boolean>>()

    private fun today(): LocalDate = LocalDate.now()

    // ---------------------------------------------------------------------
    // Server link
    // ---------------------------------------------------------------------

    private suspend fun <T> onServer(call: () -> T): T = withContext(Dispatchers.IO) { call() }

    private fun currentCaller(): Caller =
        if (_uiState.value.isAdminMode) adminCaller ?: Caller() else Caller(sessionToken = sessionToken)

    /**
     * Sends a change the app has already made to its own copy on to the server, after every
     * change queued before it. [write] brings the server up to date with the app's copy and
     * returns false only when the server couldn't be reached; the change is then kept and tried
     * again ahead of the next one. A change the server refuses is dropped, and the app's copy
     * simply stays as it is.
     *
     * The server only answers a client with a session token or an admin it has accepted, so
     * nothing is sent until someone has signed in.
     */
    private fun mirror(write: suspend (Caller) -> Boolean) {
        val caller = currentCaller()
        if (!caller.isSignedIn) return
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
        _uiState.update { state ->
            when (table) {
                Table.TASK -> state.copy(
                    tasks = state.tasks.map { if (it.taskId == localId) it.copy(taskId = serverId) else it },
                    taskCategories = state.taskCategories.map { if (it.taskId == localId) it.copy(taskId = serverId) else it },
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
                    // The server keeps one check-in a day, so a second one made that day becomes the same row.
                    dailyCheckins = state.dailyCheckins
                        .map { if (it.checkinId == localId) it.copy(checkinId = serverId) else it }
                        .distinctBy { it.checkinId },
                    responses = state.responses.map { if (it.checkinId == localId) it.copy(checkinId = serverId) else it }
                )
            }
        }
    }

    /** Server Task rows, and the Task_Category row (colour) of each, laid over the app's. */
    private fun RehabUiState.withServerTasks(rows: TaskRows?): RehabUiState {
        if (rows == null) return this
        return copy(
            tasks = tasks.mergedWith(rows.tasks) { it.taskId },
            taskCategories = taskCategories.mergedWith(rows.taskCategories) { it.taskId }
        )
    }

    /**
     * Category, Article, Task with its Task_Category, Streak_Milestone and Universal_App_Setting:
     * the tables every client shares, loaded once a client has signed in (the server serves them
     * only with a session token). The checklist and guidance rules refer to tasks by the ids in
     * [TaskIds], which the server's database gives them.
     */
    private fun loadSharedTables(token: String) {
        viewModelScope.launch {
            val categories = onServer { fetchCategories(token) }
            val articles = onServer { fetchArticles(token) }
            val tasks = onServer { fetchTaskLibrary(token) }
            val milestones = onServer { fetchStreakMilestones(token) }
            val settings = onServer { fetchAppSettings(token) }

            _uiState.update { state ->
                state.withServerTasks(tasks).copy(
                    categories = state.categories.mergedWith(categories) { it.categoryId },
                    articles = state.articles.mergedWith(articles) { it.articleId },
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
        val token = caller.sessionToken ?: return@withLock
        val profile = onServer { fetchClientProfile(token) }?.takeIf { it.clientId == clientId }
        val streak = onServer { fetchClientStreak(token, clientId) }
        val assigned = onServer { fetchClientTasks(token) }
        val clientTasks = assigned?.clientTasks?.sortedBy { it.clientTaskId }
        val checkins = onServer { fetchCheckinHistory(token) }
        val responses = onServer { fetchAnswerHistory(token) }
        val checkedInToday = onServer { fetchCheckedInToday(token) }

        _uiState.update { state ->
            val held = state.clients.find { it.clientId == clientId }
            val client = when {
                profile != null -> profile
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
            val heldCheckins = state.dailyCheckins.mergedWith(checkins) { it.checkinId }
            // The server can say today's check-in is done without sending the row, so the app notes
            // that it happened (and doesn't ask again) with a row of its own.
            val todaysCheckin = DailyCheckin(state.nextCheckinId, clientId, today()).takeIf {
                checkedInToday == true && heldCheckins.none { it.clientId == clientId && it.checkinDate == today() }
            }
            state.withServerTasks(assigned?.tasks).copy(
                clients = if (held == null) state.clients + client else state.clients.mergedWith(listOf(client)) { it.clientId },
                clientStreaks = if (state.clientStreaks.any { it.clientId == clientId }) {
                    state.clientStreaks.mergedWith(listOf(clientStreak)) { it.clientId }
                } else {
                    state.clientStreaks + clientStreak
                },
                clientTasks = state.clientTasks.mergedWith(clientTasks) { it.clientTaskId },
                dailyCheckins = heldCheckins + listOfNotNull(todaysCheckin),
                responses = state.responses.mergedWith(responses) { it.responseId },
                nextCheckinId = if (todaysCheckin != null) state.nextCheckinId + 1 else state.nextCheckinId
            )
        }
        unsent += caller to { questionCaller -> syncDailyQuestions(questionCaller, clientId) }
        sendUnsent()
        // Only now are the client's questions in, which say which answers were their setup ones.
        restoreChecklistFromSetup(clientId)
    }

    /**
     * The server has no call for a client to save the checklist the setup questionnaire made
     * them, but it does keep their setup answers (Response rows to question orders 1-15). So a
     * client who has done the questionnaire gets the same checklist made again from their latest
     * answers, rather than being asked the questions again. Rows an admin assigned them (which the
     * server does keep) sit alongside it; rows already made on this device are left as they are.
     */
    private fun restoreChecklistFromSetup(clientId: Int) {
        val state = _uiState.value
        if (state.clientTasks.any { it.clientId == clientId && LocalIds.isLocal(it.clientTaskId) }) return
        val orderByQuestionId = state.dailyQuestions
            .filter { it.clientId == clientId && it.questionOrder in SETUP_QUESTION_ORDERS }
            .associate { it.questionId to it.questionOrder }
        val setupResponses = state.responses.filter { it.questionId in orderByQuestionId }
        val latestCheckinId = setupResponses.maxOfOrNull { it.checkinId } ?: return
        val raw = setupResponses
            .filter { it.checkinId == latestCheckinId }
            .associate { orderByQuestionId.getValue(it.questionId) to it.answer }

        val newClientTasks = newChecklistRows(clientId, generateChecklist(setupAnswersFrom(raw)))
        _uiState.update {
            it.copy(clientTasks = it.clientTasks + newClientTasks, nextClientTaskId = it.nextClientTaskId + newClientTasks.size)
        }
    }

    /** The task ids of a new checklist, no longer than the database's checklist_task_cap setting allows. */
    private fun generateChecklist(answers: SetupAnswers): List<Int> =
        ChecklistGenerator.generateTaskIds(answers, cap = _uiState.value.settingInt("checklist_task_cap", 12))

    /**
     * Daily_Question: takes on the server's copy of each of the client's questions (the server
     * first copies its Question_Template rows for a client who has none, which is where the setup
     * and check-in questions come from), then sends it any the client added on this device that
     * it is missing. Returns false if the server couldn't be reached.
     */
    private suspend fun syncDailyQuestions(caller: Caller, clientId: Int): Boolean {
        val token = caller.sessionToken ?: return true
        val onServerAlready = onServer { fetchDailyQuestions(token) }
        onServerAlready?.forEach { question ->
            val sameQuestion = _uiState.value.dailyQuestions.find {
                it.clientId == clientId && it.questionOrder == question.questionOrder && LocalIds.isLocal(it.questionId)
            }
            if (sameQuestion != null) adoptServerId(Table.DAILY_QUESTION, sameQuestion.questionId, question.questionId)
        }
        _uiState.update { it.copy(dailyQuestions = it.dailyQuestions.mergedWith(onServerAlready) { q -> q.questionId }) }

        val unsaved = _uiState.value.dailyQuestions.filter { it.clientId == clientId && LocalIds.isLocal(it.questionId) }
        for (question in unsaved) {
            val saved = onServer { addDailyQuestion(token, question) }
            if (!saved.serverReached) return false
            // One the server turns away (for a category it doesn't hold, say) stays on this device.
            saved.id?.let { adoptServerId(Table.DAILY_QUESTION, question.questionId, it) }
        }
        return true
    }

    // Each of the writes below may run more than once (see [mirror]), so each starts from what
    // the app's copy says now and sends only what the server doesn't have yet.

    // The server has no call for a client to add Client_Task rows, so the rows the app makes for
    // a client (their setup checklist, each day's carried-forward one, tasks they add) stay on
    // this device. Only the rows an admin assigns, and those the server sent, are on the server.

    /** Client_Task: an admin putting a task on one of their client's checklists. */
    private fun mirrorAssignedTask(clientTaskId: Int) = mirror { caller ->
        val id = currentId(Table.CLIENT_TASK, clientTaskId)
        if (!LocalIds.isLocal(id)) return@mirror true
        // Read afresh: the row may have been removed while it waited its turn.
        val row = _uiState.value.clientTasks.find { it.clientTaskId == id } ?: return@mirror true
        if (LocalIds.isLocal(row.clientId)) return@mirror true
        val taskId = serverId(Table.TASK, row.taskId) ?: return@mirror true
        val saved = onServer { assignClientTask(caller, row.copy(taskId = taskId)) }
        saved.id?.let { adoptServerId(Table.CLIENT_TASK, id, it) }
        saved.serverReached
    }

    /**
     * Client_Task: a client ticking off a row the server holds. The server then updates their
     * Client_Streak itself. It has no call to un-tick or remove a row, so those stay on this device.
     */
    private fun mirrorTaskCompleted(clientTaskId: Int) = mirror { caller ->
        val token = caller.sessionToken ?: return@mirror true
        val id = serverId(Table.CLIENT_TASK, clientTaskId) ?: return@mirror true
        onServer { completeClientTask(token, id) }.serverReached
    }

    /**
     * Daily_Checkin together with its Response rows, which the server takes in one request.
     * Answers to questions the server doesn't hold stay on this device.
     */
    private fun mirrorCheckin(checkinId: Int) = mirror { caller ->
        val token = caller.sessionToken ?: return@mirror true
        val id = currentId(Table.DAILY_CHECKIN, checkinId)
        val state = _uiState.value
        val checkin = state.dailyCheckins.find { it.checkinId == id } ?: return@mirror true
        // The server files a check-in under its own today, so one held back past its day isn't sent.
        if (checkin.checkinDate != today()) return@mirror true
        val answers = state.responses
            .filter { it.checkinId == id }
            .mapNotNull { response ->
                serverId(Table.DAILY_QUESTION, response.questionId)?.let { questionId -> questionId to response.answer }
            }
            .toMap()
        val saved = onServer { submitDailyCheckin(token, answers) }
        if (LocalIds.isLocal(id)) saved.id?.let { adoptServerId(Table.DAILY_CHECKIN, id, it) }
        saved.serverReached
    }

    /** Daily_Question: saves a question the client has just added to their check-in. */
    private fun mirrorNewDailyQuestion(questionId: Int) = mirror { caller ->
        val token = caller.sessionToken ?: return@mirror true
        val id = currentId(Table.DAILY_QUESTION, questionId)
        if (!LocalIds.isLocal(id)) return@mirror true
        // Read afresh: the question may have been removed while it waited its turn.
        val question = _uiState.value.dailyQuestions.find { it.questionId == id } ?: return@mirror true
        val saved = onServer { addDailyQuestion(token, question) }
        saved.id?.let { adoptServerId(Table.DAILY_QUESTION, id, it) }
        saved.serverReached
    }

    // Task rows are changed only by an admin: the server has no call for a client to add or edit
    // one, nor any for Task_Category, so a task's colour stays on this device.

    /** Task: an admin adding a task to the library. */
    private fun mirrorNewTask(taskId: Int) = mirror { caller ->
        val id = currentId(Table.TASK, taskId)
        if (!LocalIds.isLocal(id)) return@mirror true
        val task = _uiState.value.tasks.find { it.taskId == id } ?: return@mirror true
        val saved = onServer { createTask(caller, task, categoryIdOf(id)) }
        saved.id?.let { adoptServerId(Table.TASK, id, it) }
        saved.serverReached
    }

    /** Task: an admin renaming or recolouring a library task, or taking it out of use. */
    private fun mirrorTaskChanges(taskId: Int) = mirror { caller ->
        val id = serverId(Table.TASK, taskId) ?: return@mirror true
        val task = _uiState.value.tasks.find { it.taskId == id } ?: return@mirror true
        onServer { saveTaskChanges(caller, task, categoryIdOf(id)) }.serverReached
    }

    /** The colour category a task is filed under (its Task_Category row). */
    private fun categoryIdOf(taskId: Int): Int? = _uiState.value.taskCategories.find { it.taskId == taskId }?.categoryId

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
                signUpClient(firstName, lastName, trimmedEmail, trimmedPhone, password)
            }

            if (result.success && result.clientId != null) {
                val newClientId = result.clientId
                // Creating a client doesn't start a session, so sign in straight away to get one.
                sessionToken = onServer { loginClient(trimmedEmail, password) }.sessionToken
                if (sessionToken == null) {
                    _uiState.update {
                        it.copy(authError = "Your account is ready, but we couldn't sign you in just now. Please sign in.", authInProgress = false)
                    }
                    onResult(false)
                    return@launch
                }
                _uiState.update {
                    it.copy(clients = it.clients + Client(newClientId, null, firstName, lastName, trimmedEmail, trimmedPhone, passwordHash = ""))
                }
                // Also brings in the setup and check-in questions the server has just copied for them.
                loadClientFromServer(newClientId, trimmedEmail)
                sessionToken?.let(::loadSharedTables)

                _uiState.update {
                    it.copy(
                        currentClientId = newClientId,
                        authError = null,
                        authInProgress = false,
                        isAdminMode = false,
                        latestCheckIn = null
                    )
                }
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
            val clientId = signIn.clientId ?: token?.let { onServer { validateSession(it) } }

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

            sessionToken = token
            loadClientFromServer(clientId, trimmedEmail)
            sessionToken?.let(::loadSharedTables)

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
        _uiState.update { it.copy(currentClientId = null, isAdminMode = false, latestCheckIn = null) }
    }

    /**
     * Opens the admin dashboard. The server checks an admin's email and password on every admin
     * call, so asking it for their clients is also how they are signed in.
     */
    fun adminLogin(email: String, password: String, onResult: (Boolean) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter your email and password.") }
            onResult(false)
            return
        }
        if (_uiState.value.authInProgress) return

        _uiState.update { it.copy(authInProgress = true) }
        viewModelScope.launch {
            val caller = Caller(adminEmail = trimmedEmail, adminPassword = password)
            val signIn = onServer { fetchAdminClients(caller) }

            if (!signIn.accepted) {
                val message = if (signIn.serverReached) {
                    "That email and password don't match an admin account."
                } else {
                    "We couldn't reach the server just now. Please check your connection and try again."
                }
                _uiState.update { it.copy(authError = message, authInProgress = false) }
                onResult(false)
                return@launch
            }

            adminCaller = caller
            adminClientIds = signIn.clients.mapTo(HashSet()) { it.clientId }
            loadAdminFromServer(caller, signIn.clients)
            _uiState.update {
                it.copy(isAdminMode = true, currentClientId = null, authError = null, authInProgress = false)
            }
            onResult(true)
        }
    }

    /**
     * Client, Client_Task, Client_Streak, Task and Category as the admin who has just signed in
     * sees them: every client, each one's checklist rows and streak, the whole task library
     * with each task's colour, and the server's usage totals across all clients.
     */
    private suspend fun loadAdminFromServer(caller: Caller, clients: List<Client>) = serverQueue.withLock {
        val clientTasks = clients.flatMap { client -> onServer { fetchAdminClientTasks(caller, client.clientId) }.orEmpty() }
        val streaks = clients.mapNotNull { client -> onServer { fetchAdminClientStreak(caller, client.clientId) } }
        val tasks = onServer { fetchAdminTasks(caller) }
        val categories = onServer { fetchAdminCategories(caller) }
        val usageReport = onServer { fetchAdminUsageReport(caller) }

        _uiState.update { state ->
            state.withServerTasks(tasks).copy(
                clients = state.clients.mergedWith(clients) { it.clientId },
                clientTasks = state.clientTasks.mergedWith(clientTasks) { it.clientTaskId },
                clientStreaks = state.clientStreaks.mergedWith(streaks) { it.clientId },
                categories = state.categories.mergedWith(categories) { it.categoryId },
                usageReport = usageReport
            )
        }
    }

    fun adminLogout() {
        adminCaller = null
        adminClientIds = emptySet()
        _uiState.update { it.copy(isAdminMode = false, usageReport = null) }
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

        val checkinId = state.nextCheckinId
        val checkin = DailyCheckin(checkinId, clientId, today(), completedAt = LocalDateTime.now())

        var responseId = state.nextResponseId
        val newResponses = mutableListOf<Response>()
        rawSetupAnswers(answers).forEach { (order, value) ->
            val question = questionsByOrder[order] ?: return@forEach
            newResponses += Response(responseId, question.questionId, checkinId, value)
            responseId++
        }

        val newClientTasks = newChecklistRows(clientId, generateChecklist(answers))

        _uiState.update {
            it.copy(
                dailyCheckins = it.dailyCheckins + checkin,
                responses = it.responses + newResponses,
                clientTasks = it.clientTasks + newClientTasks,
                nextCheckinId = checkinId + 1,
                nextResponseId = responseId,
                nextClientTaskId = it.nextClientTaskId + newClientTasks.size
            )
        }
        // The answers go to the server. The checklist can't, so it is made again from them at sign-in (see restoreChecklistFromSetup).
        mirrorCheckin(checkinId)
    }

    /** Today's Client_Task rows for a new checklist of [taskIds], numbered from the next free local id. */
    private fun newChecklistRows(clientId: Int, taskIds: List<Int>): List<ClientTask> {
        val now = LocalDateTime.now()
        val firstId = _uiState.value.nextClientTaskId
        return taskIds.mapIndexed { index, taskId ->
            ClientTask(
                clientTaskId = firstId + index,
                clientId = clientId,
                taskId = taskId,
                assignedAt = now,
                dueDate = today(),
                status = ClientTaskStatus.PENDING
            )
        }
    }

    /**
     * Every setup answer as its Response value, by question_order: an option's index, or 1/0 for
     * a goal. The checklist itself is made from the typed [SetupAnswers] (see ChecklistGenerator),
     * since several task sets share a colour category but fire on independent questions.
     */
    private fun rawSetupAnswers(answers: SetupAnswers): Map<Int, Int> = mapOf(
        1 to answers.sleep.ordinal,
        2 to answers.stress.ordinal,
        3 to answers.mood.ordinal,
        4 to answers.connection.ordinal,
        5 to answers.motivation.ordinal,
        6 to answers.activity.ordinal,
        7 to answers.routine.ordinal,
        8 to answers.energy.ordinal,
        9 to answers.pain.ordinal
    ) + GOAL_QUESTION_ORDERS.mapValues { (_, goal) -> if (goal in answers.goals) 1 else 0 }

    /** [rawSetupAnswers] read back. An answer that is missing or out of range keeps the questionnaire's default. */
    private fun setupAnswersFrom(raw: Map<Int, Int>): SetupAnswers {
        val defaults = SetupAnswers()
        fun <E> option(order: Int, entries: List<E>, default: E): E = raw[order]?.let(entries::getOrNull) ?: default
        return SetupAnswers(
            sleep = option(1, SleepRating.entries, defaults.sleep),
            stress = option(2, StressFrequency.entries, defaults.stress),
            mood = option(3, MoodRating.entries, defaults.mood),
            connection = option(4, LevelRating.entries, defaults.connection),
            motivation = option(5, LevelRating.entries, defaults.motivation),
            activity = option(6, LevelRating.entries, defaults.activity),
            routine = option(7, RoutineRating.entries, defaults.routine),
            energy = option(8, LevelRating.entries, defaults.energy),
            pain = option(9, PainLevel.entries, defaults.pain),
            goals = GOAL_QUESTION_ORDERS.filterKeys { raw[it] == 1 }.values.toSet()
        )
    }

    // ---------------------------------------------------------------------
    // Daily flow helpers
    // ---------------------------------------------------------------------

    /**
     * True once the client has completed the Initial Setup Questionnaire, i.e. has client_task rows
     * the app made for them: the checklist it made from their answers (made again at sign-in from
     * the answers the server keeps). Rows an admin assigned them, which have server ids, don't count.
     */
    fun hasCompletedSetup(): Boolean {
        val clientId = _uiState.value.currentClientId ?: return false
        return _uiState.value.clientTasks.any { it.clientId == clientId && LocalIds.isLocal(it.clientTaskId) }
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
            .filter { it.dueDate == mostRecentDate && it.taskId != TaskIds.SELF_COMPASSION_TASK_ID }
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
            it.clientId == clientId && it.taskId == TaskIds.SELF_COMPASSION_TASK_ID && it.dueDate == today()
        }
        if (gap >= thresholdDays && !alreadyHasSelfCompassionToday) {
            val newRow = ClientTask(
                clientTaskId = _uiState.value.nextClientTaskId,
                clientId = clientId,
                taskId = TaskIds.SELF_COMPASSION_TASK_ID,
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
        checkIn.ownAnswers.forEach { (questionId, level) ->
            // The screen may still hold the id a question had before the server renumbered it.
            val id = currentId(Table.DAILY_QUESTION, questionId)
            if (state.dailyQuestions.none { it.questionId == id }) return@forEach
            newResponses += Response(responseId, id, checkinId, GuidanceEngine.answerValue(level))
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

    /** The signed-in client's checklist for today. */
    fun todaysChecklistItems(): List<ChecklistItem> {
        val clientId = _uiState.value.currentClientId ?: return emptyList()
        return checklistFor(clientId)
    }

    /** [clientId]'s checklist for today: client_task rows joined to Task + Task_Category + Category. */
    private fun checklistFor(clientId: Int): List<ChecklistItem> {
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
        // The server works out the streak itself from completed rows, so only the completion is sent.
        if (nowCompleting) mirrorTaskCompleted(rowId)
    }

    fun dismissMilestone() {
        _uiState.update { it.copy(pendingMilestoneDays = null, isNewBestStreak = false) }
    }

    fun milestoneMessage(days: Int): String =
        _uiState.value.streakMilestones.find { it.days == days }?.message
            ?: "$days day streak! Celebrate consistency, not perfection."

    /** What the welcome-back dialog says after a missed day: the database's streak_broken_message setting. */
    fun welcomeBackMessage(): String? = _uiState.value.setting("streak_broken_message")

    // ---------------------------------------------------------------------
    // The client's own Daily Check-In questions (Daily_Question)
    // ---------------------------------------------------------------------

    /**
     * The wording of the signed-in client's questions, by question_order: their copies of the
     * database's setup questions (1-15) and Daily Check-In questions (101-106).
     */
    fun questionTexts(): Map<Int, String> {
        val clientId = _uiState.value.currentClientId ?: return emptyMap()
        return _uiState.value.dailyQuestions
            .filter { it.clientId == clientId }
            .associate { it.questionOrder to it.questionText }
    }

    /** Questions the client added to their Daily Check-In themselves, in the order they are asked. */
    fun ownCheckInQuestions(): List<DailyQuestion> {
        val clientId = _uiState.value.currentClientId ?: return emptyList()
        return _uiState.value.dailyQuestions
            .filter { it.clientId == clientId && it.isOwnQuestion }
            .sortedBy { it.questionOrder }
    }

    /** Adds a question to the end of the client's Daily Check-In. [categoryId] is the colour they picked for it. */
    fun addCheckInQuestion(text: String, categoryId: Int) {
        if (text.isBlank() || ownCheckInQuestions().size >= DailyQuestion.MAX_OWN_QUESTIONS) return
        val clientId = _uiState.value.currentClientId ?: return
        val state = _uiState.value
        val usedOrders = state.dailyQuestions.filter { it.clientId == clientId }.mapTo(HashSet()) { it.questionOrder }
        val order = DailyQuestion.OWN_QUESTION_ORDERS.firstOrNull { it !in usedOrders } ?: return
        val question = DailyQuestion(state.nextDailyQuestionId, categoryId, clientId, order, text.trim())
        _uiState.update {
            it.copy(dailyQuestions = it.dailyQuestions + question, nextDailyQuestionId = question.questionId + 1)
        }
        mirrorNewDailyQuestion(question.questionId)
    }

    /** Takes one of the client's own questions out of their Daily Check-In. Their past answers to it are kept. */
    fun removeCheckInQuestion(questionId: Int) {
        val id = currentId(Table.DAILY_QUESTION, questionId)
        if (_uiState.value.dailyQuestions.none { it.questionId == id && it.isOwnQuestion }) return
        _uiState.update { state -> state.copy(dailyQuestions = state.dailyQuestions.filterNot { it.questionId == id }) }
        mirror { caller ->
            val token = caller.sessionToken ?: return@mirror true
            val serverQuestionId = serverId(Table.DAILY_QUESTION, id) ?: return@mirror true
            onServer { removeDailyQuestion(token, serverQuestionId) }.serverReached
        }
    }

    // ---------------------------------------------------------------------
    // Account (Client)
    // ---------------------------------------------------------------------

    /**
     * Replaces the signed-in client's password. [onResult] gets null once it has been changed,
     * or what to tell them if it couldn't be.
     */
    fun changePassword(currentPassword: String, newPassword: String, onResult: (String?) -> Unit) {
        val client = _uiState.value.currentClient ?: return
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            onResult("Please fill in every field.")
            return
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            onResult("Your new password needs at least $MIN_PASSWORD_LENGTH characters.")
            return
        }
        val token = sessionToken
        if (token == null) {
            onResult("Please sign in again, then change your password.")
            return
        }
        viewModelScope.launch {
            val result = onServer { changeClientPassword(client.email, token, currentPassword, newPassword) }
            onResult(if (result.success) null else result.errorMessage ?: "We couldn't change your password just now.")
        }
    }

    /** Asks the server to email [email] a reset code. [onResult] gets null once it has asked, or what to tell them. */
    fun requestPasswordReset(email: String, onResult: (String?) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            onResult("Please enter your email address.")
            return
        }
        viewModelScope.launch {
            val result = onServer { sendPasswordResetCode(trimmedEmail) }
            onResult(if (result.success) null else result.errorMessage ?: "We couldn't send a reset code just now.")
        }
    }

    /** Sets a new password with the code from the reset email. [onResult] gets null once it is reset, or what to tell them. */
    fun resetPassword(email: String, code: String, newPassword: String, onResult: (String?) -> Unit) {
        val trimmedEmail = email.trim()
        val digits = code.filter { it.isDigit() }
        if (digits.isEmpty() || newPassword.isBlank()) {
            onResult("Please fill in every field.")
            return
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            onResult("Your new password needs at least $MIN_PASSWORD_LENGTH characters.")
            return
        }
        viewModelScope.launch {
            val result = onServer { resetClientPassword(trimmedEmail, digits, newPassword) }
            onResult(if (result.success) null else result.errorMessage ?: "We couldn't reset your password just now.")
        }
    }

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

    /**
     * Removes a task from today's checklist (and therefore from tomorrow's carry-forward), for the
     * client themselves or for an admin. The server has no call to remove a Client_Task row, so
     * the removal stays on this device.
     */
    fun removeTask(clientTaskId: Int) {
        val rowId = currentId(Table.CLIENT_TASK, clientTaskId)
        _uiState.update { state ->
            state.copy(
                clientTasks = state.clientTasks.map {
                    if (it.clientTaskId == rowId) it.copy(status = ClientTaskStatus.REMOVED) else it
                }
            )
        }
    }

    /**
     * A client renaming or recolouring a task on their own checklist. The server has no call for
     * a client to change a Task row (which every client with that task shares), so the change
     * stays on this device.
     */
    fun updateTask(taskId: Int, newTitle: String, newCategoryId: Int) {
        applyTaskEdit(currentId(Table.TASK, taskId), newTitle, newCategoryId)
    }

    /** An admin renaming or recolouring a task in the master library. Only the name reaches the server. */
    fun updateLibraryTask(taskId: Int, newTitle: String, newCategoryId: Int) {
        val id = currentId(Table.TASK, taskId)
        if (applyTaskEdit(id, newTitle, newCategoryId)) mirrorTaskChanges(id)
    }

    /** Applies a rename / recolour to the app's copy. Returns false if there was nothing to change. */
    private fun applyTaskEdit(taskId: Int, newTitle: String, newCategoryId: Int): Boolean {
        if (newTitle.isBlank()) return false
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { if (it.taskId == taskId) it.copy(title = newTitle.trim()) else it },
                taskCategories = state.taskCategories.map {
                    if (it.taskId == taskId) it.copy(categoryId = newCategoryId) else it
                }
            )
        }
        return true
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
        mirrorAssignedTask(newClientTask.clientTaskId)
    }

    /**
     * An admin renaming or recolouring a task on one client's checklist, for that client alone.
     * A task the server holds or another client has is shared, so the client gets their own copy
     * of it in the same place on their checklist, which is saved to the server as a new task and
     * assigned to them. A task made on this device for this client only is changed as it is.
     */
    fun updateClientTask(clientTaskId: Int, newTitle: String, newCategoryId: Int) {
        if (newTitle.isBlank()) return
        val state = _uiState.value
        val rowId = currentId(Table.CLIENT_TASK, clientTaskId)
        val row = state.clientTasks.find { it.clientTaskId == rowId } ?: return
        val task = state.tasks.find { it.taskId == row.taskId } ?: return
        val shared = !LocalIds.isLocal(task.taskId) ||
            state.clientTasks.any { it.taskId == task.taskId && it.clientId != row.clientId }
        if (!shared) {
            applyTaskEdit(task.taskId, newTitle, newCategoryId)
            return
        }

        val copyId = state.nextTaskId
        val copyRow = row.copy(clientTaskId = state.nextClientTaskId, taskId = copyId, assignedAt = LocalDateTime.now())
        _uiState.update {
            it.copy(
                tasks = it.tasks + task.copy(taskId = copyId, title = newTitle.trim(), isActive = true),
                taskCategories = it.taskCategories + TaskCategory(categoryId = newCategoryId, taskId = copyId, difficulty = 1),
                clientTasks = it.clientTasks.flatMap { clientTask ->
                    if (clientTask.clientTaskId == rowId) {
                        listOf(clientTask.copy(status = ClientTaskStatus.REMOVED), copyRow)
                    } else {
                        listOf(clientTask)
                    }
                },
                nextTaskId = copyId + 1,
                nextClientTaskId = state.nextClientTaskId + 1
            )
        }
        mirrorNewTask(copyId)
        mirrorAssignedTask(copyRow.clientTaskId)
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

    /** Admin Dashboard: a rollup for every client. */
    fun clientProgressSummaries(): List<ClientProgressSummary> {
        val state = _uiState.value
        val today = today()
        return state.clients.filter { it.clientId in adminClientIds }.map { client ->
            val todaysRows = state.clientTasks.filter {
                it.clientId == client.clientId && it.dueDate == today && it.status != ClientTaskStatus.REMOVED
            }
            val completed = todaysRows.count { it.status == ClientTaskStatus.COMPLETED }
            val rate = if (todaysRows.isEmpty()) 0f else completed.toFloat() / todaysRows.size
            val streak = state.clientStreaks.find { it.clientId == client.clientId }
            ClientProgressSummary(client, rate, streak?.currentStreak ?: 0, streak?.bestStreak ?: 0, checklistFor(client.clientId))
        }
    }

    /** Admin Dashboard: the master task library grouped by Category (via Task_Category). */
    fun tasksByCategory(): Map<Category, List<Task>> {
        val state = _uiState.value
        val categoryIdByTaskId = state.taskCategories.associate { it.taskId to it.categoryId }
        return state.categories.associateWith { category ->
            state.tasks.filter { task ->
                task.isActive &&
                    task.taskId != TaskIds.SELF_COMPASSION_TASK_ID &&
                    categoryIdByTaskId[task.taskId] == category.categoryId
            }
        }
    }

    fun activeArticles(): List<Article> = _uiState.value.articles.filter { it.isActive }.sortedBy { it.sortOrder }
}
