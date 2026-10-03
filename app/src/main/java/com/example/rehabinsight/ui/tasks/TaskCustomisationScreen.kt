package com.example.rehabinsight.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.components.HaloIcon
import com.example.rehabinsight.ui.components.LocalBottomChromeInset
import com.example.rehabinsight.ui.components.RehabTextField
import com.example.rehabinsight.ui.components.ScreenHeader
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.ui.theme.color

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

    Box(modifier = Modifier.fillMaxSize()) {
        CalmPage {
            Spacer(Modifier.height(16.dp))
            ScreenHeader(
                title = "Your tasks",
                subtitle = "Add, edit, remove or recolour any task. Your checklist stays the same until you change it."
            )
            Spacer(Modifier.height(20.dp))

            if (checklist.isEmpty()) {
                SoftCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HaloIcon(Icons.Rounded.Spa, diameter = 104.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nothing here yet. Add one small task whenever you're ready.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RehabTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
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
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 10.dp)
                            )
                            IconButton(onClick = { editingTask = item }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = RehabTextSecondary)
                            }
                            IconButton(onClick = { onRemoveTask(item.clientTaskId) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove", tint = RehabTextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        // Floats over the page's closing scenery, clear of the tab bar's rounded corner.
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp + LocalBottomChromeInset.current)
                .softShadow(CircleShape, 10.dp),
            shape = CircleShape,
            containerColor = RehabPrimary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add task")
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
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = RehabSurface,
        titleContentColor = RehabTextPrimary,
        textContentColor = RehabTextPrimary,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                RehabTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "Task name"
                )
                Spacer(Modifier.height(20.dp))
                // The category name is backend-only: users pick by colour and never see the label.
                Text("Colour", style = MaterialTheme.typography.labelMedium, color = RehabTextSecondary)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    categories.forEachIndexed { index, cat ->
                        val selected = cat.categoryId == category.categoryId
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .then(if (selected) Modifier.border(2.dp, cat.color, CircleShape) else Modifier)
                                .selectable(selected = selected, role = Role.RadioButton, onClick = { category = cat })
                                .semantics { contentDescription = "Colour ${index + 1}" },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (selected) cat.color else cat.color.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onSave(text, category.categoryId) }) {
                Text("Save", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = RehabTextSecondary)
            }
        }
    )
}
