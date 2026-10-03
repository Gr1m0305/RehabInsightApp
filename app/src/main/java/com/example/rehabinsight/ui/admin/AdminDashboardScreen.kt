package com.example.rehabinsight.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Support
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
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.Client
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.CircleIconButton
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.IconBubble
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.SoftProgressBar
import com.example.rehabinsight.ui.tasks.TaskEditorDialog
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.ui.theme.SuccessGreen
import com.example.rehabinsight.ui.theme.WarnAmber
import com.example.rehabinsight.ui.theme.color
import com.example.rehabinsight.viewmodel.ClientProgressSummary

private enum class AdminSection(val title: String, val icon: ImageVector) {
    OVERVIEW("Dashboard", Icons.Rounded.Groups),
    USER_PROGRESS("View user progress", Icons.AutoMirrored.Rounded.ListAlt),
    USER_TRENDS("View user trends", Icons.Rounded.Insights),
    TASK_LIBRARY("Manage task library", Icons.AutoMirrored.Rounded.Assignment)
}

private data class TrendStat(val label: String, val value: String, val icon: ImageVector, val color: Color)

private val businessInsights = listOf(
    "Clients complete 68% of their checklist on an average day.",
    "The most-completed tasks are short, low-effort actions (5-minute tidy, gratitude practice).",
    "Streak milestones correlate strongly with continued engagement past week 2."
)

@Composable
fun AdminDashboardScreen(
    progressSummaries: List<ClientProgressSummary>,
    tasksByCategory: Map<Category, List<Task>>,
    onAddTask: (String, Int) -> Unit,
    onUpdateTask: (Int, String, Int) -> Unit,
    onRemoveTask: (Int) -> Unit,
    onAssignTask: (Int, Int) -> Unit,
    onLogout: () -> Unit
) {
    var section by remember { mutableStateOf(AdminSection.OVERVIEW) }

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
                        onClick = { section = AdminSection.OVERVIEW }
                    )
                    Spacer(Modifier.width(14.dp))
                } else {
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    section.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Exit", tint = RehabTextSecondary)
                }
            }

            // Keyed so each section opens at the top rather than at the previous one's scroll position.
            key(section) {
                CalmPage {
                    when (section) {
                        AdminSection.OVERVIEW -> OverviewSection { section = it }
                        AdminSection.USER_PROGRESS -> UserProgressSection(progressSummaries)
                        AdminSection.USER_TRENDS -> UserTrendsSection()
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
        "Read-only insights for reporting, clinical review, and business development.",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            AdminSection.USER_PROGRESS,
            AdminSection.USER_TRENDS,
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

    Spacer(Modifier.height(28.dp))
    Text("Business insights", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        businessInsights.forEach { insight ->
            SoftCard(containerColor = RehabPrimarySoft, elevated = false, contentPadding = PaddingValues(16.dp)) {
                Text(insight, style = MaterialTheme.typography.bodyMedium, color = RehabTextPrimary)
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

@Composable
private fun UserTrendsSection() {
    Spacer(Modifier.height(4.dp))
    Text(
        "Aggregate behaviour trends across all active users (last 7 days).",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(20.dp))
    val trendStats = listOf(
        TrendStat("Average completion", "58%", Icons.Rounded.DonutLarge, SuccessGreen),
        TrendStat("Users at risk (low engagement)", "2", Icons.Rounded.Support, WarnAmber),
        TrendStat("Improving week over week", "3", Icons.AutoMirrored.Rounded.TrendingUp, SuccessGreen)
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
    Surface(
        onClick = { showAddDialog = true },
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
