package com.example.rehabinsight.ui.model

import androidx.compose.ui.graphics.Color

/**
 * UI-friendly view of a single row on a client's checklist: a join across
 * client_task + Task + Task_Category + Category, flattened for easy rendering.
 */
data class ChecklistItem(
    val clientTaskId: Int,
    val taskId: Int,
    val title: String,
    val categoryId: Int?,
    val categoryColor: Color,
    val completed: Boolean
)
