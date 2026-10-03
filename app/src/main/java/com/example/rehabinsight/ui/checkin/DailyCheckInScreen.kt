package com.example.rehabinsight.ui.checkin

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.CheckInAnswers
import com.example.rehabinsight.data.FiveLevel
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.CircleIconButton
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.IconBubble
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.SoftChip
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
import com.example.rehabinsight.ui.theme.RehabPrimaryLight
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class LevelOption(
    val level: FiveLevel,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

// Soft, equal-weight tones: no answer should look like the "bad" or "alarming" one.
private val greatColor = Color(0xFF5FB894)
private val goodColor = Color(0xFF6F9FE8)
private val okayColor = Color(0xFF9195B5)
private val lowColor = Color(0xFF9584D6)
private val strugglingColor = Color(0xFFD9879B)

private fun optionsFor(question: Int): List<LevelOption> = when (question) {
    0 -> listOf( // mood
        LevelOption(FiveLevel.GREAT, "Optimistic, energized, and clear-minded.", Icons.Rounded.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Calm, steady, and feeling grounded.", Icons.Rounded.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Just drifting, average, or quiet.", Icons.Rounded.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Feeling heavy, tired, or unmotivated.", Icons.Rounded.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Anxious, overwhelmed, or in pain.", Icons.Rounded.Favorite, strugglingColor)
    )
    1 -> listOf( // energy
        LevelOption(FiveLevel.GREAT, "Energized and ready to go.", Icons.Rounded.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Steady energy today.", Icons.Rounded.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "A little tired, but managing.", Icons.Rounded.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Running low on energy.", Icons.Rounded.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Completely drained.", Icons.Rounded.SentimentVeryDissatisfied, strugglingColor)
    )
    2 -> listOf( // stress
        LevelOption(FiveLevel.GREAT, "Calm and relaxed.", Icons.Rounded.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Mostly at ease.", Icons.Rounded.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "A bit on edge.", Icons.Rounded.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Feeling tense or pressured.", Icons.Rounded.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Overwhelmed by stress.", Icons.Rounded.SentimentVeryDissatisfied, strugglingColor)
    )
    3 -> listOf( // sleep
        LevelOption(FiveLevel.GREAT, "Slept deeply, woke up refreshed.", Icons.Rounded.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Decent night's rest.", Icons.Rounded.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Restless, but got some sleep.", Icons.Rounded.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Poor sleep, still tired.", Icons.Rounded.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Barely slept at all.", Icons.Rounded.SentimentVeryDissatisfied, strugglingColor)
    )
    4 -> listOf( // motivation
        LevelOption(FiveLevel.GREAT, "Excited and driven today.", Icons.Rounded.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Feeling capable and willing.", Icons.Rounded.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Neutral, going through the motions.", Icons.Rounded.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Hard to get started.", Icons.Rounded.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "No motivation at all.", Icons.Rounded.SentimentVeryDissatisfied, strugglingColor)
    )
    else -> listOf( // pain (optional)
        LevelOption(FiveLevel.GREAT, "No pain or discomfort.", Icons.Rounded.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Minimal, barely noticeable.", Icons.Rounded.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Some mild discomfort.", Icons.Rounded.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Noticeable pain today.", Icons.Rounded.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Significant pain.", Icons.Rounded.SentimentVeryDissatisfied, strugglingColor)
    )
}

private val questionTitles = listOf(
    "How are you feeling today?",
    "How's your energy?",
    "How stressed do you feel?",
    "How did you sleep?",
    "How motivated do you feel?",
    "Any pain or discomfort?"
)

private fun greetingFor(time: LocalTime): String = when (time.hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
fun DailyCheckInScreen(
    userName: String,
    onSubmit: (CheckInAnswers) -> Unit,
    onSkipAll: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var mood by remember { mutableStateOf<FiveLevel?>(null) }
    var energy by remember { mutableStateOf<FiveLevel?>(null) }
    var stress by remember { mutableStateOf<FiveLevel?>(null) }
    var sleep by remember { mutableStateOf<FiveLevel?>(null) }
    var motivation by remember { mutableStateOf<FiveLevel?>(null) }

    val today = LocalDate.now()
    val dateLabel = remember(today) {
        "Today, " + today.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }
    val greeting = remember(userName) {
        listOf(greetingFor(LocalTime.now()), userName).filter { it.isNotBlank() }.joinToString(", ")
    }

    fun finish(pain: FiveLevel?) {
        onSubmit(
            CheckInAnswers(
                date = today,
                mood = mood ?: FiveLevel.OKAY,
                energy = energy ?: FiveLevel.OKAY,
                stress = stress ?: FiveLevel.OKAY,
                sleep = sleep ?: FiveLevel.OKAY,
                motivation = motivation ?: FiveLevel.OKAY,
                pain = pain
            )
        )
    }

    fun selectAndAdvance(level: FiveLevel) {
        when (step) {
            0 -> mood = level
            1 -> energy = level
            2 -> stress = level
            3 -> sleep = level
            4 -> motivation = level
            5 -> { finish(level); return }
        }
        step++
    }

    GradientScreenBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (step > 0) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = { step-- }
                    )
                }
                StepDots(
                    current = step,
                    total = questionTitles.size,
                    modifier = Modifier.align(Alignment.Center)
                )
                SoftChip(
                    text = dateLabel,
                    containerColor = RehabSurface,
                    contentColor = RehabTextSecondary,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            CalmPage(horizontalPadding = 24.dp) {
                Spacer(Modifier.height(14.dp))
                Text(greeting, style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
                Spacer(Modifier.height(4.dp))

                Crossfade(targetState = step, animationSpec = tween(300), label = "check-in-step") { shownStep ->
                    Column {
                        Text(questionTitles[shownStep], style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(22.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            optionsFor(shownStep).forEach { option ->
                                LevelOptionCard(option) { selectAndAdvance(option.level) }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(26.dp))
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = RehabTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Your responses are private and secure",
                        style = MaterialTheme.typography.bodySmall,
                        color = RehabTextSecondary
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (step == 5) "Skip for now" else "Skip check-in for today",
                    style = MaterialTheme.typography.labelLarge,
                    color = RehabPrimaryDeep,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            if (step == 5) finish(null) else onSkipAll()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/** Quiet "where am I" indicator: one dot per question, the current one drawn as a short bar. */
@Composable
private fun StepDots(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Question ${current + 1} of $total"
        }
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (index == current) 18.dp else 6.dp)
                    .background(
                        when {
                            index == current -> RehabPrimary
                            index < current -> RehabPrimaryLight
                            else -> RehabPrimary.copy(alpha = 0.2f)
                        },
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun LevelOptionCard(option: LevelOption, onClick: () -> Unit) {
    SoftCard(contentPadding = PaddingValues(16.dp), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(option.icon, tint = option.color, background = option.color.copy(alpha = 0.16f), size = 46.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(option.level.label, style = MaterialTheme.typography.titleMedium)
                Text(option.subtitle, style = MaterialTheme.typography.bodySmall, color = RehabTextSecondary)
            }
        }
    }
}
