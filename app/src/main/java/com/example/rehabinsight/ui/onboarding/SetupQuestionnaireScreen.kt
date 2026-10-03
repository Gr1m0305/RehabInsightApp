package com.example.rehabinsight.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.Goal
import com.example.rehabinsight.data.LevelRating
import com.example.rehabinsight.data.MoodRating
import com.example.rehabinsight.data.PainLevel
import com.example.rehabinsight.data.RoutineRating
import com.example.rehabinsight.data.SetupAnswers
import com.example.rehabinsight.data.SleepRating
import com.example.rehabinsight.data.StressFrequency
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.CircleIconButton
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.HaloIcon
import com.example.rehabinsight.ui.components.PrimaryButton
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.SoftProgressBar
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextMuted
import com.example.rehabinsight.ui.theme.RehabTextSecondary

private const val TOTAL_STEPS = 10

/** One icon per question, in step order, shown above it so each step has a gentle focal point. */
private val stepIcons = listOf(
    Icons.Rounded.Bedtime, // sleep
    Icons.Rounded.Spa, // stress
    Icons.Rounded.Favorite, // mood
    Icons.Rounded.Groups, // connection
    Icons.AutoMirrored.Rounded.TrendingUp, // motivation
    Icons.AutoMirrored.Rounded.DirectionsWalk, // activity
    Icons.Rounded.Schedule, // routine
    Icons.Rounded.WbSunny, // energy
    Icons.Rounded.Healing, // pain
    Icons.Rounded.Flag // goals
)

@Composable
fun SetupQuestionnaireScreen(
    onComplete: (SetupAnswers) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var sleep by remember { mutableStateOf<SleepRating?>(null) }
    var stress by remember { mutableStateOf<StressFrequency?>(null) }
    var mood by remember { mutableStateOf<MoodRating?>(null) }
    var connection by remember { mutableStateOf<LevelRating?>(null) }
    var motivation by remember { mutableStateOf<LevelRating?>(null) }
    var activity by remember { mutableStateOf<LevelRating?>(null) }
    var routine by remember { mutableStateOf<RoutineRating?>(null) }
    var energy by remember { mutableStateOf<LevelRating?>(null) }
    var pain by remember { mutableStateOf<PainLevel?>(null) }
    var goals by remember { mutableStateOf(setOf<Goal>()) }

    fun goNext() {
        if (step < TOTAL_STEPS - 1) step++ else {
            onComplete(
                SetupAnswers(
                    sleep = sleep ?: SleepRating.MODERATE,
                    stress = stress ?: StressFrequency.SOMETIMES,
                    mood = mood ?: MoodRating.MODERATE,
                    connection = connection ?: LevelRating.MODERATE,
                    motivation = motivation ?: LevelRating.MODERATE,
                    activity = activity ?: LevelRating.MODERATE,
                    routine = routine ?: RoutineRating.SOMEWHAT,
                    energy = energy ?: LevelRating.MODERATE,
                    pain = pain ?: PainLevel.NONE,
                    goals = goals
                )
            )
        }
    }

    GradientScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        onClick = { step-- }
                    )
                } else {
                    Spacer(Modifier.size(48.dp))
                }
                Spacer(Modifier.width(16.dp))
                SoftProgressBar(
                    progress = (step + 1) / TOTAL_STEPS.toFloat(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    "${step + 1} of $TOTAL_STEPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = RehabTextSecondary
                )
                Spacer(Modifier.width(8.dp))
            }

            CalmPage(horizontalPadding = 24.dp) {
                Spacer(Modifier.height(4.dp))

                Crossfade(targetState = step, animationSpec = tween(300), label = "setup-step") { shownStep ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HaloIcon(stepIcons[shownStep], diameter = 124.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Getting to know you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RehabTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))

                        when (shownStep) {
                            0 -> SingleSelectQuestion(
                                title = "How would you rate your sleep lately?",
                                options = SleepRating.entries,
                                label = { it.label },
                                selected = sleep
                            ) { sleep = it; goNext() }

                            1 -> SingleSelectQuestion(
                                title = "How often do you feel stressed or overwhelmed?",
                                options = StressFrequency.entries,
                                label = { it.label },
                                selected = stress
                            ) { stress = it; goNext() }

                            2 -> SingleSelectQuestion(
                                title = "How have you been feeling emotionally?",
                                options = MoodRating.entries,
                                label = { it.label },
                                selected = mood
                            ) { mood = it; goNext() }

                            3 -> SingleSelectQuestion(
                                title = "How connected do you feel to others?",
                                options = LevelRating.entries,
                                label = { it.label },
                                selected = connection
                            ) { connection = it; goNext() }

                            4 -> SingleSelectQuestion(
                                title = "How motivated have you been?",
                                options = LevelRating.entries,
                                label = { it.label },
                                selected = motivation
                            ) { motivation = it; goNext() }

                            5 -> SingleSelectQuestion(
                                title = "How physically active are you?",
                                options = LevelRating.entries,
                                label = { it.label },
                                selected = activity
                            ) { activity = it; goNext() }

                            6 -> SingleSelectQuestion(
                                title = "Do you have a consistent daily routine?",
                                options = RoutineRating.entries,
                                label = { it.label },
                                selected = routine
                            ) { routine = it; goNext() }

                            7 -> SingleSelectQuestion(
                                title = "How is your energy through the day?",
                                options = LevelRating.entries,
                                label = { it.label },
                                selected = energy
                            ) { energy = it; goNext() }

                            8 -> SingleSelectQuestion(
                                title = "Does pain or a physical condition limit what you can do?",
                                options = PainLevel.entries,
                                label = { it.label },
                                selected = pain
                            ) { pain = it; goNext() }

                            9 -> MultiSelectGoalsQuestion(
                                selected = goals,
                                onToggle = { g ->
                                    goals = when {
                                        g in goals -> goals - g
                                        goals.size >= 2 -> goals
                                        else -> goals + g
                                    }
                                },
                                onContinue = { goNext() }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun QuestionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun <T> SingleSelectQuestion(
    title: String,
    options: List<T>,
    label: (T) -> String,
    selected: T?,
    onSelect: (T) -> Unit
) {
    QuestionTitle(title)
    Spacer(Modifier.height(22.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { option ->
            OptionRow(
                text = label(option),
                selected = option == selected,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun MultiSelectGoalsQuestion(
    selected: Set<Goal>,
    onToggle: (Goal) -> Unit,
    onContinue: () -> Unit
) {
    QuestionTitle("What would you like to improve?")
    Spacer(Modifier.height(6.dp))
    Text(
        "Choose up to two",
        style = MaterialTheme.typography.bodyMedium,
        color = RehabTextSecondary
    )
    Spacer(Modifier.height(22.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Goal.entries.forEach { goal ->
            OptionRow(
                text = goal.label,
                selected = goal in selected,
                onClick = { onToggle(goal) }
            )
        }
    }
    Spacer(Modifier.height(28.dp))
    PrimaryButton(text = "Finish setup", enabled = selected.isNotEmpty(), onClick = onContinue)
}

@Composable
private fun OptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    SoftCard(
        containerColor = if (selected) RehabPrimarySoft else RehabSurface,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text, style = MaterialTheme.typography.titleMedium)
            // Radio-style marker: an empty ring that fills in once the answer is chosen
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        if (selected) Modifier.background(RehabPrimary, CircleShape)
                        else Modifier.border(2.dp, RehabTextMuted, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
