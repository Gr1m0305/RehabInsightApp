package com.example.rehabinsight.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Reflection
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.components.IconBubble
import com.example.rehabinsight.ui.components.PrimaryGradient
import com.example.rehabinsight.ui.components.ProgressRing
import com.example.rehabinsight.ui.components.SectionHeading
import com.example.rehabinsight.ui.components.StreakBadge
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextMuted
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
    CalmPage {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Welcome back", style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
                Text(
                    userName,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            StreakBadge(days = streak)
        }

        Spacer(Modifier.height(24.dp))
        TodayProgress(
            completed = checklist.count { it.completed },
            total = checklist.size,
            topMessage = topMessage,
            compassionMessage = compassionMessage
        )

        Spacer(Modifier.height(24.dp))
        ReflectionCard(reflection)

        if (highlightedTasks.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            SectionHeading("Recommended for today")
            Spacer(Modifier.height(4.dp))
            Text(guidanceIntro, style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                highlightedTasks.forEach { item ->
                    TaskRow(
                        item = item,
                        onToggle = { onToggleTask(item.clientTaskId) }
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionHeading("Your checklist")
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap a task any time you complete it. Every bit of progress counts.",
            style = MaterialTheme.typography.bodyMedium,
            color = RehabTextSecondary
        )
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            checklist.forEach { item ->
                TaskRow(
                    item = item,
                    onToggle = { onToggleTask(item.clientTaskId) }
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

/** Today's progress as a soft ring, with the supportive messaging directly underneath. */
@Composable
private fun TodayProgress(completed: Int, total: Int, topMessage: String, compassionMessage: String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ProgressRing(
            progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "You've completed $completed of $total tasks today"
            }
        ) {
            if (total > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$completed", style = MaterialTheme.typography.displaySmall, color = RehabPrimaryDeep)
                    Text("of $total today", style = MaterialTheme.typography.labelSmall, color = RehabTextSecondary)
                }
            } else {
                Icon(Icons.Rounded.Spa, contentDescription = null, tint = RehabPrimary, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            topMessage,
            style = MaterialTheme.typography.titleMedium,
            color = RehabPrimaryDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            compassionMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = RehabTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun ReflectionCard(reflection: Reflection) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 12.dp)
            .background(PrimaryGradient, shape)
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBubble(Icons.Rounded.FormatQuote, background = Color.White, size = 36.dp)
        Spacer(Modifier.height(10.dp))
        Text("Daily reflection", style = MaterialTheme.typography.labelSmall, color = Color.White)
        Spacer(Modifier.height(10.dp))
        Text(
            "“${reflection.quote}”",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "— ${reflection.author}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TaskRow(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    val checkTint by animateColorAsState(
        targetValue = if (item.completed) RehabPrimary else RehabTextMuted,
        label = "check-tint"
    )
    // Finished tasks settle back into the background instead of being struck through.
    Surface(
        checked = item.completed,
        onCheckedChange = { onToggle() },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (item.completed) Modifier else Modifier.softShadow(shape, 6.dp))
            .semantics { role = Role.Checkbox },
        shape = shape,
        color = if (item.completed) RehabSurface.copy(alpha = 0.55f) else RehabSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (item.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = checkTint,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.completed) RehabTextSecondary else RehabTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            ColorDot(item.categoryColor)
        }
    }
}
