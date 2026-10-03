package com.example.rehabinsight.ui.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.components.CalmScenery
import com.example.rehabinsight.ui.components.GradientScreenBackground
import com.example.rehabinsight.ui.components.LocalBottomChromeInset
import com.example.rehabinsight.ui.components.SceneryHeight
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import kotlinx.coroutines.delay

/**
 * Section 6 - Checklist Generation Screen.
 * Runs once after setup: takes the answers and builds the ongoing personalised checklist.
 */
@Composable
fun ChecklistGenerationScreen(onGenerated: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1400)
        onGenerated()
    }

    // A slow "breathing" halo rather than a spinner - waiting should feel unhurried.
    val transition = rememberInfiniteTransition(label = "breathe")
    val breath by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath"
    )

    GradientScreenBackground {
        CalmScenery(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(SceneryHeight + LocalBottomChromeInset.current)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .graphicsLayer { scaleX = breath; scaleY = breath }
                        .background(RehabPrimary.copy(alpha = 0.07f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .graphicsLayer { scaleX = breath; scaleY = breath }
                        .background(RehabPrimary.copy(alpha = 0.11f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .softShadow(CircleShape, 10.dp)
                        .background(RehabSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = RehabPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Building your personalised checklist",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "We're combining your answers to create a simple, ongoing set of small daily actions.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
