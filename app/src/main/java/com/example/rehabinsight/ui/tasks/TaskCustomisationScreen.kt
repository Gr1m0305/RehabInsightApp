package com.example.rehabinsight.ui.tasks

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.theme.RehabBlue
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.ui.theme.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCustomisationScreen(
    checklist: List<ChecklistItem>,
    categories: List<Category>,
    onAddTask: (String, Int) -> Unit,
    onUpdateTask: (Int, String, Int) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<ChecklistItem?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = RehabBlue) {
                Icon(Icons.Filled.Add, contentDescription = "Add task", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Text("Your tasks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Add, edit, remove or recolour any task. Your checklist stays the same until you change it.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary
            )
            Spacer(Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                checklist.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorDot(item.categoryColor)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { editingTask = item }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RehabTextSecondary)
                            }
                            IconButton(onClick = { onRemoveTask(item.clientTaskId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = RehabTextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(90.dp))
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

    editingTask?.let { item ->
        val initial = categories.find { it.categoryId == item.categoryId } ?: categories.firstOrNull()
        if (initial != null) {
            TaskEditorDialog(
                initialTitle = item.title,
                initialCategory = initial,
                categories = categories,
                title = "Edit task",
                onDismiss = { editingTask = null },
                onSave = { title, categoryId ->
                    onUpdateTask(item.taskId, title, categoryId)
                    editingTask = null
                }
            )
        }
    }
}

@Composable
fun TaskEditorDialog(
    initialTitle: String,
    initialCategory: Category,
    categories: List<Category>,
    title: String,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var text by remember { mutableStateOf(initialTitle) }
    var category by remember { mutableStateOf(initialCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Task name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Colour category (backend only \u2014 users never see this label)", style = MaterialTheme.typography.bodySmall, color = RehabTextSecondary)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    categories.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (cat.categoryId == category.categoryId) cat.color else cat.color.copy(alpha = 0.35f))
                                .clickable { category = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cat.categoryId == category.categoryId) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onSave(text, category.categoryId) }) {
                Text("Save", color = RehabBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
