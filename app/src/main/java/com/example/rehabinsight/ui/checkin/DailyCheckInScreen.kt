package com.example.rehabinsight.ui.checkin

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.data.CheckInAnswers
import com.example.rehabinsight.data.FiveLevel
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.theme.RehabTextMuted
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class LevelOption(
    val level: FiveLevel,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

private val greatColor = Color(0xFF3FB27F)
private val goodColor = Color(0xFF4C8DFF)
private val okayColor = Color(0xFF8B8FA8)
private val lowColor = Color(0xFF8A6FD8)
private val strugglingColor = Color(0xFFE0577A)

private fun optionsFor(question: Int): List<LevelOption> = when (question) {
    0 -> listOf( // mood
        LevelOption(FiveLevel.GREAT, "Optimistic, energized, and clear-minded.", Icons.Filled.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Calm, steady, and feeling grounded.", Icons.Filled.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Just drifting, average, or quiet.", Icons.Filled.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Feeling heavy, tired, or unmotivated.", Icons.Filled.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Anxious, overwhelmed, or in pain.", Icons.Filled.Favorite, strugglingColor)
    )
    1 -> listOf( // energy
        LevelOption(FiveLevel.GREAT, "Energized and ready to go.", Icons.Filled.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Steady energy today.", Icons.Filled.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "A little tired, but managing.", Icons.Filled.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Running low on energy.", Icons.Filled.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Completely drained.", Icons.Filled.SentimentVeryDissatisfied, strugglingColor)
    )
    2 -> listOf( // stress
        LevelOption(FiveLevel.GREAT, "Calm and relaxed.", Icons.Filled.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Mostly at ease.", Icons.Filled.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "A bit on edge.", Icons.Filled.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Feeling tense or pressured.", Icons.Filled.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Overwhelmed by stress.", Icons.Filled.SentimentVeryDissatisfied, strugglingColor)
    )
    3 -> listOf( // sleep
        LevelOption(FiveLevel.GREAT, "Slept deeply, woke up refreshed.", Icons.Filled.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Decent night's rest.", Icons.Filled.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Restless, but got some sleep.", Icons.Filled.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Poor sleep, still tired.", Icons.Filled.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Barely slept at all.", Icons.Filled.SentimentVeryDissatisfied, strugglingColor)
    )
    4 -> listOf( // motivation
        LevelOption(FiveLevel.GREAT, "Excited and driven today.", Icons.Filled.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Feeling capable and willing.", Icons.Filled.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Neutral, going through the motions.", Icons.Filled.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Hard to get started.", Icons.Filled.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "No motivation at all.", Icons.Filled.SentimentVeryDissatisfied, strugglingColor)
    )
    else -> listOf( // pain (optional)
        LevelOption(FiveLevel.GREAT, "No pain or discomfort.", Icons.Filled.SentimentVerySatisfied, greatColor),
        LevelOption(FiveLevel.GOOD, "Minimal, barely noticeable.", Icons.Filled.SentimentSatisfied, goodColor),
        LevelOption(FiveLevel.OKAY, "Some mild discomfort.", Icons.Filled.SentimentNeutral, okayColor),
        LevelOption(FiveLevel.LOW, "Noticeable pain today.", Icons.Filled.SentimentDissatisfied, lowColor),
        LevelOption(FiveLevel.STRUGGLING, "Significant pain.", Icons.Filled.SentimentVeryDissatisfied, strugglingColor)
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
        "TODAY, " + today.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())).uppercase(Locale.getDefault())
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { if (step > 0) step-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(dateLabel, style = MaterialTheme.typography.labelSmall, color = RehabTextSecondary)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Good morning, $userName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RehabTextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    questionTitles[step],
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    optionsFor(step).forEach { option ->
                        LevelOptionCard(option) { selectAndAdvance(option.level) }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "\uD83D\uDD12 Your responses are private and secure",
                    style = MaterialTheme.typography.bodySmall,
                    color = RehabTextMuted
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    if (step == 5) "Skip for now" else "Skip check-in for today",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RehabTextSecondary,
                    modifier = Modifier.clickable {
                        if (step == 5) finish(null) else onSkipAll()
                    }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LevelOptionCard(option: LevelOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(option.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(option.icon, contentDescription = null, tint = option.color)
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(option.level.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(option.subtitle, style = MaterialTheme.typography.bodySmall, color = RehabTextSecondary)
            }
        }
    }
}
