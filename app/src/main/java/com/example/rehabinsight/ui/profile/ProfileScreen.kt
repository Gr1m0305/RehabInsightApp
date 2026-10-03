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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.IconBubble
import com.example.rehabinsight.ui.components.PrimaryGradient
import com.example.rehabinsight.ui.components.ScreenHeader
import com.example.rehabinsight.ui.components.SecondaryButton
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.StreakPlantBubble
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
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
    onLogout: () -> Unit
) {
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

        Spacer(Modifier.height(28.dp))
        SecondaryButton(text = "Log out", onClick = onLogout)
        Spacer(Modifier.height(32.dp))
    }
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
