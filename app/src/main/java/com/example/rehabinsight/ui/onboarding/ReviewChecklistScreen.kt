package com.example.rehabinsight.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.components.CalmScenery
import com.example.rehabinsight.ui.components.ColorDot
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.LocalBottomChromeInset
import com.example.rehabinsight.ui.components.PrimaryButton
import com.example.rehabinsight.ui.components.SceneryHeight
import com.example.rehabinsight.ui.components.ScreenHeader
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.model.ChecklistItem

/**
 * Section 7 - Review Personalised Checklist.
 * A single continuous list, no headings, small colour indicators only.
 */
@Composable
fun ReviewChecklistScreen(
    checklist: List<ChecklistItem>,
    onContinue: () -> Unit
) {
    GradientScreenBackground {
        // The scenery sits behind the list and the pinned button, so the page still closes on it.
        CalmScenery(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(SceneryHeight + LocalBottomChromeInset.current)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = LocalBottomChromeInset.current)
        ) {
            ScreenHeader(
                title = "Your checklist is ready",
                subtitle = "This is your ongoing checklist — it stays the same day to day. " +
                    "You can add, remove or edit tasks anytime from the Tasks tab.",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(checklist, key = { it.clientTaskId }) { item ->
                    SoftCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorDot(item.categoryColor)
                            Spacer(Modifier.width(14.dp))
                            Text(item.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                PrimaryButton(text = "Looks good, let's begin", onClick = onContinue)
            }
        }
    }
}
