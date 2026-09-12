package com.example.rehabinsight.ui.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Reflection
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.components.ProgressSection
import com.example.rehabinsight.ui.components.StreakBadge
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.theme.RehabBackgroundBottom
import com.example.rehabinsight.ui.theme.RehabBlue
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary

@Composable
fun HomeScreen(
    userName: String,
    streak: Int,
    topMessage: String,
    reflection: Reflection,
    highlightedTasks: List<ChecklistItem>,
    guidanceIntro: String,
    checklist: List<ChecklistItem>,
    compassionMessage: String,
    onToggleTask: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Welcome back", style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
                Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            StreakBadge(days = streak)
        }

        Spacer(Modifier.height(6.dp))
        Text(
            topMessage,
            style = MaterialTheme.typography.titleMedium,
            color = RehabBlue,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(18.dp))

        // Daily reflection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.FormatQuote, contentDescription = null, tint = RehabBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "DAILY REFLECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = RehabBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text("\"${reflection.quote}\"", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(6.dp))
                Text("\u2014 ${reflection.author}", style = MaterialTheme.typography.bodySmall, color = RehabTextSecondary)
            }
        }

        if (highlightedTasks.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = RehabBackgroundBottom),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "RECOMMENDED FOR TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = RehabBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(guidanceIntro, style = MaterialTheme.typography.bodyMedium, color = RehabTextPrimary)
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        highlightedTasks.forEach { item ->
                            TaskRow(
                                item = item,
                                onToggle = { onToggleTask(item.clientTaskId) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("Your checklist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap a task any time you complete it. Every bit of progress counts.",
            style = MaterialTheme.typography.bodySmall,
            color = RehabTextSecondary
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            checklist.forEach { item ->
                TaskRow(
                    item = item,
                    onToggle = { onToggleTask(item.clientTaskId) }
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.padding(18.dp)) {
                ProgressSection(
                    completed = checklist.count { it.completed },
                    total = checklist.size,
                    message = compassionMessage
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TaskRow(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (item.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (item.completed) RehabBlue else Color(0xFFC7CBE0)
            )
            Spacer(Modifier.size(10.dp))
            ColorDot(item.categoryColor)
            Spacer(Modifier.size(12.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.completed) RehabTextSecondary else RehabTextPrimary,
                textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
