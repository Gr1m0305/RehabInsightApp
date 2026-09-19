package com.example.rehabinsight.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.Client
import com.example.rehabinsight.data.Task
import com.example.rehabinsight.ui.tasks.TaskEditorDialog
import com.example.rehabinsight.ui.theme.RehabBackgroundBottom
import com.example.rehabinsight.ui.theme.RehabBlue
import com.example.rehabinsight.ui.theme.RehabDivider
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.ui.theme.SuccessGreen
import com.example.rehabinsight.ui.theme.WarnAmber
import com.example.rehabinsight.ui.theme.color
import com.example.rehabinsight.viewmodel.ClientProgressSummary

private enum class AdminSection(val title: String, val icon: ImageVector) {
    OVERVIEW("Dashboard", Icons.Filled.Groups),
    USER_PROGRESS("View user progress", Icons.AutoMirrored.Filled.ListAlt),
    USER_TRENDS("View user trends", Icons.Filled.Insights),
    TASK_LIBRARY("Manage task library", Icons.AutoMirrored.Filled.Assignment)
}

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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (section != AdminSection.OVERVIEW) {
                    IconButton(onClick = { section = AdminSection.OVERVIEW }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Text(section.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Exit", tint = RehabTextSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
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

@Composable
private fun OverviewSection(onNavigate: (AdminSection) -> Unit) {
    Spacer(Modifier.height(8.dp))
    Text(
        "Read-only insights for reporting, clinical review, and business development.",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(18.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            AdminSection.USER_PROGRESS,
            AdminSection.USER_TRENDS,
            AdminSection.TASK_LIBRARY
        ).forEach { entry ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(entry) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RehabBackgroundBottom),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(entry.icon, contentDescription = null, tint = RehabBlue)
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(entry.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = RehabTextSecondary)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    Text("Business insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        businessInsights.forEach { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RehabBackgroundBottom),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(insight, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
            }
        }
    }
}

@Composable
private fun UserProgressSection(progressSummaries: List<ClientProgressSummary>) {
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        progressSummaries.forEach { summary ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(summary.client.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${(summary.completionRateToday * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = RehabBlue)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { summary.completionRateToday },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = RehabBlue,
                        trackColor = RehabDivider
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Current streak: ${summary.currentStreak} days \u00b7 Best: ${summary.bestStreak} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = RehabTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun UserTrendsSection() {
    Spacer(Modifier.height(8.dp))
    Text(
        "Aggregate behaviour trends across all active users (last 7 days).",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(16.dp))
    val trendStats = listOf(
        Triple("Average completion", "58%", SuccessGreen),
        Triple("Users at risk (low engagement)", "2", WarnAmber),
        Triple("Improving week over week", "3", SuccessGreen)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        trendStats.forEach { (label, value, color) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
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

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Spacer(Modifier.height(8.dp))
            Text(
                "The master library used to generate & suggest tasks across all users.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary
            )
            Spacer(Modifier.height(16.dp))
            tasksByCategory.forEach { (category, tasks) ->
                Text(category.name, style = MaterialTheme.typography.labelSmall, color = RehabTextSecondary)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(category.color)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(task.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { assigningTask = task }) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = "Assign to client", tint = RehabTextSecondary)
                            }
                            IconButton(onClick = { editingTask = task }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RehabTextSecondary)
                            }
                            IconButton(onClick = { onRemoveTask(task.taskId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = RehabTextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            Spacer(Modifier.height(60.dp))
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = RehabBlue,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add task", tint = Color.White)
        }
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
        title = { Text("Assign \"${task.title}\"", fontWeight = FontWeight.Bold) },
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
                            .clickable { onAssign(client.clientId) }
                            .padding(vertical = 10.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
