package com.example.rehabinsight.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.DailyQuestion
import com.example.rehabinsight.data.network.MIN_PASSWORD_LENGTH
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.components.GentleError
import com.example.rehabinsight.ui.components.IconBubble
import com.example.rehabinsight.ui.components.PrimaryGradient
import com.example.rehabinsight.ui.components.RehabPasswordField
import com.example.rehabinsight.ui.components.ScreenHeader
import com.example.rehabinsight.ui.components.SecondaryButton
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.StreakPlantBubble
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.tasks.TaskEditorDialog
import com.example.rehabinsight.ui.theme.RehabDivider
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextMuted
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.ui.theme.color
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Up to two initials for the avatar, e.g. "Anna B." -> "AB". */
private fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    currentStreak: Int,
    longestStreak: Int,
    weeklyConsistency: List<Boolean>,
    checkInQuestions: List<DailyQuestion>,
    categories: List<Category>,
    onAddCheckInQuestion: (String, Int) -> Unit,
    onRemoveCheckInQuestion: (Int) -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String, onResult: (String?) -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    var showAddQuestion by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }

    CalmPage {
        Spacer(Modifier.height(16.dp))
        ScreenHeader("Profile")
        Spacer(Modifier.height(20.dp))

        IdentityCard(name = name, email = email)

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(title = "Current streak", days = currentStreak, modifier = Modifier.weight(1f)) {
                StreakPlantBubble(days = currentStreak, size = 44.dp)
            }
            StatCard(title = "Longest streak", days = longestStreak, modifier = Modifier.weight(1f)) {
                IconBubble(Icons.Rounded.EmojiEvents, size = 44.dp)
            }
        }

        Spacer(Modifier.height(16.dp))

        SoftCard {
            Text("This week's consistency", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "No pressure, just a gentle picture of your week.",
                style = MaterialTheme.typography.bodySmall,
                color = RehabTextSecondary
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val today = LocalDate.now()
                weeklyConsistency.forEachIndexed { index, active ->
                    val date = today.minusDays((weeklyConsistency.lastIndex - index).toLong())
                    DayMarker(
                        dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        active = active,
                        isToday = date == today
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        CheckInQuestionsCard(
            questions = checkInQuestions,
            categories = categories,
            onAdd = { showAddQuestion = true },
            onRemove = onRemoveCheckInQuestion
        )

        Spacer(Modifier.height(16.dp))
        SoftCard(contentPadding = PaddingValues(16.dp), onClick = { showChangePassword = true }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Rounded.Lock, size = 38.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Change password", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Pick a new one whenever you like",
                        style = MaterialTheme.typography.bodySmall,
                        color = RehabTextSecondary
                    )
                }
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = RehabTextSecondary
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        SecondaryButton(text = "Log out", onClick = onLogout)
        Spacer(Modifier.height(32.dp))
    }

    if (showAddQuestion && categories.isNotEmpty()) {
        TaskEditorDialog(
            initialTitle = "",
            initialCategory = categories.first(),
            categories = categories,
            title = "Add a check-in question",
            placeholder = "e.g. How was my walk today?",
            onDismiss = { showAddQuestion = false },
            onSave = { text, categoryId ->
                onAddCheckInQuestion(text, categoryId)
                showAddQuestion = false
            }
        )
    }

    if (showChangePassword) {
        ChangePasswordDialog(onChangePassword = onChangePassword, onDismiss = { showChangePassword = false })
    }
}

/** The client's own Daily Check-In questions, which they can add to and remove from. */
@Composable
private fun CheckInQuestionsCard(
    questions: List<DailyQuestion>,
    categories: List<Category>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit
) {
    val colourByCategoryId = categories.associate { it.categoryId to it.color }
    SoftCard(contentPadding = PaddingValues(start = 16.dp, end = 4.dp, top = 16.dp, bottom = 6.dp)) {
        Row(modifier = Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Rounded.QuestionAnswer, size = 38.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Your check-in questions", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Add a few of your own. They come at the end of each check-in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RehabTextSecondary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        questions.forEach { question ->
            HorizontalDivider(color = RehabDivider, modifier = Modifier.padding(end = 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(colourByCategoryId[question.categoryId] ?: RehabTextMuted)
                Spacer(Modifier.width(14.dp))
                Text(
                    question.questionText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp)
                )
                IconButton(onClick = { onRemove(question.questionId) }) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove question", tint = RehabTextSecondary)
                }
            }
        }
        if (questions.size < DailyQuestion.MAX_OWN_QUESTIONS) {
            TextButton(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = RehabPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add a question", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
            }
        } else {
            Text(
                "That's the most you can add, so your check-in stays quick.",
                style = MaterialTheme.typography.bodySmall,
                color = RehabTextSecondary,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 10.dp)
            )
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    onChangePassword: (currentPassword: String, newPassword: String, onResult: (String?) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordsVisible by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var changed by remember { mutableStateOf(false) }

    @Composable
    fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String) {
        RehabPasswordField(
            value = value,
            onValueChange = { onValueChange(it); problem = null },
            label = label,
            placeholder = placeholder,
            visible = passwordsVisible,
            onToggleVisible = { passwordsVisible = !passwordsVisible }
        )
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = RehabSurface,
        titleContentColor = RehabTextPrimary,
        textContentColor = RehabTextPrimary,
        title = {
            Text(if (changed) "Password changed" else "Change password", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            if (changed) {
                Text(
                    "All done. Use your new password next time you sign in.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RehabTextSecondary
                )
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    PasswordField(currentPassword, { currentPassword = it }, "Current password", "Enter your current password")
                    Spacer(Modifier.height(16.dp))
                    PasswordField(newPassword, { newPassword = it }, "New password", "At least $MIN_PASSWORD_LENGTH characters")
                    Spacer(Modifier.height(16.dp))
                    PasswordField(confirmPassword, { confirmPassword = it }, "Confirm new password", "Type it once more")
                    problem?.let {
                        Spacer(Modifier.height(14.dp))
                        GentleError(it)
                    }
                }
            }
        },
        confirmButton = {
            if (changed) {
                TextButton(onClick = onDismiss) {
                    Text("Done", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
                }
            } else {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        if (newPassword != confirmPassword) {
                            problem = "Those new passwords don't match. Please try again."
                        } else {
                            busy = true
                            onChangePassword(currentPassword, newPassword) { result ->
                                busy = false
                                if (result == null) changed = true else problem = result
                            }
                        }
                    }
                ) {
                    Text(if (busy) "Saving…" else "Save", style = MaterialTheme.typography.labelLarge, color = RehabPrimary)
                }
            }
        },
        dismissButton = {
            if (!changed) {
                TextButton(enabled = !busy, onClick = onDismiss) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = RehabTextSecondary)
                }
            }
        }
    )
}

/** Who is signed in, on the same gradient as Home's reflection card. */
@Composable
private fun IdentityCard(name: String, email: String) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 12.dp)
            .background(PrimaryGradient, shape)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val initials = initialsOf(name)
            if (initials.isEmpty()) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = RehabPrimary)
            } else {
                Text(initials, style = MaterialTheme.typography.titleLarge, color = RehabPrimaryDeep)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DayMarker(dayName: String, active: Boolean, isToday: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = "$dayName: ${if (active) "active" else "no activity"}"
        }
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .then(
                    if (active) Modifier.background(PrimaryGradient, CircleShape)
                    else Modifier.background(RehabPrimarySoft, CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (active) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            dayName.take(1),
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) RehabTextPrimary else RehabTextSecondary
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    days: Int,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    SoftCard(modifier = modifier, contentPadding = PaddingValues(18.dp)) {
        icon()
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$days", style = MaterialTheme.typography.displaySmall, color = RehabTextPrimary)
            Spacer(Modifier.width(6.dp))
            Text(
                if (days == 1) "day" else "days",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary,
                modifier = Modifier.padding(bottom = 7.dp)
            )
        }
        Text(title, style = MaterialTheme.typography.bodySmall, color = RehabTextSecondary)
    }
}
