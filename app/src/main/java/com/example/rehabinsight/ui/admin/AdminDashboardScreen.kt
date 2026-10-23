package com.example.rehabinsight.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.Client
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.data.network.UsageReport
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.CircleIconButton
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.IconBubble
import com.example.rehabinsight.ui.components.RehabTextField
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.SoftProgressBar
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.tasks.TaskEditorDialog
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.ui.theme.SuccessGreen
import com.example.rehabinsight.ui.theme.color
import com.example.rehabinsight.viewmodel.ClientProgressSummary

private enum class AdminSection(val title: String, val icon: ImageVector) {
    OVERVIEW("Dashboard", Icons.Rounded.Groups),
    USER_PROGRESS("View user progress", Icons.AutoMirrored.Rounded.ListAlt),
    USER_TRENDS("View user trends", Icons.Rounded.Insights),
    USER_TASKS("Manage user tasks", Icons.Rounded.PersonSearch),
    TASK_LIBRARY("Manage task library", Icons.AutoMirrored.Rounded.Assignment)
}

private data class TrendStat(val label: String, val value: String, val icon: ImageVector, val color: Color)

@Composable
fun AdminDashboardScreen(
    progressSummaries: List<ClientProgressSummary>,
    usageReport: UsageReport?,
    tasksByCategory: Map<Category, List<Task>>,
    onAddTask: (String, Int) -> Unit,
    onUpdateTask: (Int, String, Int) -> Unit,
    onRemoveTask: (Int) -> Unit,
    onAssignTask: (Int, Int) -> Unit,
    onUpdateClientTask: (Int, String, Int) -> Unit,
    onRemoveClientTask: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var section by remember { mutableStateOf(AdminSection.OVERVIEW) }
    // The client whose checklist is open under Manage user tasks, and the search that found them,
    // both kept while the admin goes back to the list and out again.
    var openClientId by remember { mutableStateOf<Int?>(null) }
    var clientSearch by remember { mutableStateOf("") }
    val openClient = progressSummaries
        .find { it.client.clientId == openClientId }
        ?.takeIf { section == AdminSection.USER_TASKS }

    fun goBack() {
        when {
            openClient != null -> openClientId = null
            section != AdminSection.OVERVIEW -> section = AdminSection.OVERVIEW
            else -> onLogout()
        }
    }

    // The system back key does what the screen's own Back and Exit buttons do.
    BackHandler { goBack() }

    GradientScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (section != AdminSection.OVERVIEW) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        onClick = { goBack() }
                    )
                    Spacer(Modifier.width(14.dp))
                } else {
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    openClient?.client?.fullName ?: section.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Exit", tint = RehabTextSecondary)
                }
            }

            // Keyed so each section opens at the top rather than at the previous one's scroll position.
            key(section, openClient?.client?.clientId) {
                CalmPage {
                    when (section) {
                        AdminSection.OVERVIEW -> OverviewSection { section = it }
                        AdminSection.USER_PROGRESS -> UserProgressSection(progressSummaries)
                        AdminSection.USER_TRENDS -> UserTrendsSection(usageReport)
                        AdminSection.USER_TASKS -> if (openClient == null) {
                            ClientSearchSection(
                                progressSummaries = progressSummaries,
                                search = clientSearch,
                                onSearchChange = { clientSearch = it },
                                onOpenClient = { openClientId = it }
                            )
                        } else {
                            ClientTasksSection(
                                summary = openClient,
                                tasksByCategory = tasksByCategory,
                                onAddTask = { taskId -> onAssignTask(openClient.client.clientId, taskId) },
                                onUpdateTask = onUpdateClientTask,
                                onRemoveTask = onRemoveClientTask
                            )
                        }
                        AdminSection.TASK_LIBRARY -> TaskLibrarySection(
                            tasksByCategory = tasksByCategory,
                            clients = progressSummaries.map { it.client },
                            onAddTask = onAddTask,
                            onUpdateTask = onUpdateTask,
                            onRemoveTask = onRemoveTask,
                            onAssignTask = onAssignTask
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(onNavigate: (AdminSection) -> Unit) {
    Spacer(Modifier.height(4.dp))
    Text(
        "Your clients' progress, their checklists, and the task library they draw from.",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            AdminSection.USER_PROGRESS,
            AdminSection.USER_TRENDS,
            AdminSection.USER_TASKS,
            AdminSection.TASK_LIBRARY
        ).forEach { entry ->
            SoftCard(contentPadding = PaddingValues(16.dp), onClick = { onNavigate(entry) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBubble(entry.icon)
                    Spacer(Modifier.width(14.dp))
                    Text(entry.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = RehabTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun UserProgressSection(progressSummaries: List<ClientProgressSummary>) {
    Spacer(Modifier.height(4.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        progressSummaries.forEach { summary ->
            SoftCard(contentPadding = PaddingValues(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(summary.client.fullName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${(summary.completionRateToday * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = RehabPrimary
                    )
                }
                Spacer(Modifier.height(10.dp))
                SoftProgressBar(progress = summary.completionRateToday, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Text(
                    "Current streak: ${summary.currentStreak} days · Best: ${summary.bestStreak} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = RehabTextSecondary
                )
            }
        }
    }
}

/** The server's usage report: totals across every client. */
@Composable
private fun UserTrendsSection(usageReport: UsageReport?) {
    Spacer(Modifier.height(4.dp))
    Text(
        "Totals across all clients.",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(20.dp))
    if (usageReport == null) {
        SoftCard(containerColor = RehabPrimarySoft, elevated = false, contentPadding = PaddingValues(16.dp)) {
            Text(
                "We couldn't load these figures just now.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary
            )
        }
        return
    }
    val trendStats = listOf(
        TrendStat("Clients", "${usageReport.clients}", Icons.Rounded.Groups, RehabPrimary),
        TrendStat("Checked in during the last 7 days", "${usageReport.activeClientsLastWeek}", Icons.AutoMirrored.Rounded.TrendingUp, SuccessGreen),
        TrendStat("Check-ins completed", "${usageReport.completedCheckins}", Icons.Rounded.DonutLarge, SuccessGreen),
        TrendStat("Tasks completed", "${usageReport.tasksCompleted}", Icons.Rounded.TaskAlt, SuccessGreen),
        TrendStat("Answers recorded", "${usageReport.answersRecorded}", Icons.Rounded.Insights, RehabPrimary)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        trendStats.forEach { stat ->
            SoftCard(contentPadding = PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBubble(stat.icon, tint = stat.color, background = stat.color.copy(alpha = 0.14f))
                    Spacer(Modifier.width(14.dp))
                    Text(stat.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    Text(stat.value, style = MaterialTheme.typography.headlineSmall, color = stat.color)
                }
            }
        }
    }
}

/** True when [query] is blank or appears anywhere in the client's name, ignoring case. */
private fun Client.matchesName(query: String): Boolean {
    val term = query.trim()
    return term.isEmpty() || fullName.contains(term, ignoreCase = true)
}

@Composable
private fun ClientSearchSection(
    progressSummaries: List<ClientProgressSummary>,
    search: String,
    onSearchChange: (String) -> Unit,
    onOpenClient: (Int) -> Unit
) {
    val matches = progressSummaries.filter { it.client.matchesName(search) }

    Spacer(Modifier.height(4.dp))
    Text(
        "Find a client to add, change or remove the tasks on their checklist.",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(16.dp))
    RehabTextField(
        value = search,
        onValueChange = onSearchChange,
        placeholder = "Search by name",
        leadingIcon = Icons.Rounded.Search,
        trailingIcon = if (search.isNotEmpty()) {
            {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    )
    Spacer(Modifier.height(16.dp))

    if (matches.isEmpty()) {
        SoftCard(containerColor = RehabPrimarySoft, elevated = false, contentPadding = PaddingValues(16.dp)) {
            Text(
                if (progressSummaries.isEmpty()) "No clients yet." else "No clients match \"${search.trim()}\".",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        matches.forEach { summary ->
            val checklist = summary.todaysChecklist
            SoftCard(contentPadding = PaddingValues(16.dp), onClick = { onOpenClient(summary.client.clientId) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBubble(Icons.Rounded.Person)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(summary.client.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${checklist.count { it.completed }} of ${checklist.size} tasks done today",
                            style = MaterialTheme.typography.bodySmall,
                            color = RehabTextSecondary
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = RehabTextSecondary)
                }
            }
        }
    }
}

/** One client's checklist for today, which the admin can add to, rename or recolour, and remove from. */
@Composable
private fun ClientTasksSection(
    summary: ClientProgressSummary,
    tasksByCategory: Map<Category, List<Task>>,
    onAddTask: (Int) -> Unit,
    onUpdateTask: (Int, String, Int) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    val categories = tasksByCategory.keys.toList()
    val checklist = summary.todaysChecklist
    val firstName = summary.client.firstName
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ChecklistItem?>(null) }

    Spacer(Modifier.height(4.dp))
    Text(
        "Today's checklist. Changes here are for $firstName only.",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(16.dp))
    AddTaskPill { showAddDialog = true }
    Spacer(Modifier.height(24.dp))

    if (checklist.isEmpty()) {
        SoftCard(containerColor = RehabPrimarySoft, elevated = false, contentPadding = PaddingValues(16.dp)) {
            Text(
                "Nothing on $firstName's checklist today.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        checklist.forEach { item ->
            SoftCard(contentPadding = PaddingValues(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorDot(item.categoryColor)
                    Spacer(Modifier.width(14.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp)
                    ) {
                        Text(item.title, style = MaterialTheme.typography.bodyLarge)
                        if (item.completed) {
                            Text("Done today", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                        }
                    }
                    IconButton(onClick = { editingItem = item }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = RehabTextSecondary)
                    }
                    IconButton(onClick = { onRemoveTask(item.clientTaskId) }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove", tint = RehabTextSecondary)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddClientTaskDialog(
            clientName = firstName,
            tasksByCategory = tasksByCategory,
            taskIdsHeld = checklist.mapTo(HashSet()) { it.taskId },
            onDismiss = { showAddDialog = false },
            onAdd = { taskId ->
                onAddTask(taskId)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        val initial = categories.find { it.categoryId == item.categoryId } ?: categories.firstOrNull()
        if (initial != null) {
            TaskEditorDialog(
                initialTitle = item.title,
                initialCategory = initial,
                categories = categories,
                title = "Edit $firstName's task",
                onDismiss = { editingItem = null },
                onSave = { title, categoryId ->
                    onUpdateTask(item.clientTaskId, title, categoryId)
                    editingItem = null
                }
            )
        }
    }
}

/** Picks a library task to put on a client's checklist, leaving out the ones already on it. */
@Composable
private fun AddClientTaskDialog(
    clientName: String,
    tasksByCategory: Map<Category, List<Task>>,
    taskIdsHeld: Set<Int>,
    onDismiss: () -> Unit,
    onAdd: (Int) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val choices = tasksByCategory.flatMap { (category, tasks) ->
        tasks
            .filter { it.taskId !in taskIdsHeld && it.title.contains(search.trim(), ignoreCase = true) }
            .map { category to it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = RehabSurface,
        titleContentColor = RehabTextPrimary,
        textContentColor = RehabTextPrimary,
        title = { Text("Add to $clientName's checklist", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                RehabTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Search tasks",
                    leadingIcon = Icons.Rounded.Search
                )
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (choices.isEmpty()) {
                        Text(
                            "No tasks match.",
                            color = RehabTextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        )
                    }
                    choices.forEach { (category, task) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onAdd(task.taskId) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorDot(category.color)
                            Spacer(Modifier.width(12.dp))
                            Text(task.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = RehabTextSecondary)
            }
        }
    )
}

@Composable
private fun AddTaskPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = RehabPrimary,
        contentColor = RehabSurface
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add a task", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TaskLibrarySection(
    tasksByCategory: Map<Category, List<Task>>,
    clients: List<Client>,
    onAddTask: (String, Int) -> Unit,
    onUpdateTask: (Int, String, Int) -> Unit,
    onRemoveTask: (Int) -> Unit,
    onAssignTask: (Int, Int) -> Unit
) {
    val categories = tasksByCategory.keys.toList()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var assigningTask by remember { mutableStateOf<Task?>(null) }

    Spacer(Modifier.height(4.dp))
    Text(
        "The master library used to generate & suggest tasks across all users.",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(16.dp))
    AddTaskPill { showAddDialog = true }
    Spacer(Modifier.height(24.dp))

    tasksByCategory.forEach { (category, tasks) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            ColorDot(category.color)
            Spacer(Modifier.width(10.dp))
            Text(category.name, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tasks.forEach { task ->
                SoftCard(contentPadding = PaddingValues(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp)
                        )
                        IconButton(onClick = { assigningTask = task }) {
                            Icon(Icons.Rounded.PersonAdd, contentDescription = "Assign to client", tint = RehabTextSecondary)
                        }
                        IconButton(onClick = { editingTask = task }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = RehabTextSecondary)
                        }
                        IconButton(onClick = { onRemoveTask(task.taskId) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove", tint = RehabTextSecondary)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showAddDialog && categories.isNotEmpty()) {
        TaskEditorDialog(
            initialTitle = "",
            initialCategory = categories.first(),
            categories = categories,
            title = "Add a task",
            onDismiss = { showAddDialog = false },
            onSave = { title, categoryId ->
                onAddTask(title, categoryId)
                showAddDialog = false
            }
        )
    }

    editingTask?.let { task ->
        val initial = categories.find { cat -> tasksByCategory[cat]?.any { it.taskId == task.taskId } == true }
            ?: categories.firstOrNull()
        if (initial != null) {
            TaskEditorDialog(
                initialTitle = task.title,
                initialCategory = initial,
                categories = categories,
                title = "Edit task",
                onDismiss = { editingTask = null },
                onSave = { title, categoryId ->
                    onUpdateTask(task.taskId, title, categoryId)
                    editingTask = null
                }
            )
        }
    }

    assigningTask?.let { task ->
        AssignTaskDialog(
            task = task,
            clients = clients,
            onDismiss = { assigningTask = null },
            onAssign = { clientId ->
                onAssignTask(clientId, task.taskId)
                assigningTask = null
            }
        )
    }
}

@Composable
private fun AssignTaskDialog(
    task: Task,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onAssign: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = RehabSurface,
        titleContentColor = RehabTextPrimary,
        textContentColor = RehabTextPrimary,
        title = { Text("Assign \"${task.title}\"", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (clients.isEmpty()) {
                    Text("No clients yet.", color = RehabTextSecondary)
                }
                clients.forEach { client ->
                    Text(
                        client.fullName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onAssign(client.clientId) }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = RehabTextSecondary)
            }
        }
    )
}
