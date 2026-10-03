package com.example.rehabinsight.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.theme.PlantLeafLight
import com.example.rehabinsight.ui.theme.PlantPot
import com.example.rehabinsight.ui.theme.RehabPrimaryLight
import com.example.rehabinsight.ui.theme.StreakWarm
import com.example.rehabinsight.ui.theme.SuccessGreen
import com.example.rehabinsight.ui.theme.SuccessSoft
import kotlin.math.cos
import kotlin.math.sin

/**
 * The streak lengths, in days, at which the plant takes its next step: a first leaf, a pair, then
 * a leaf more each time, a bud at two weeks, a flower at a month and further blossoms after that.
 */
private val growthSteps = listOf(1, 2, 3, 5, 7, 14, 30, 50, 100)

private const val BUD_STAGE = 6
private const val FIRST_FLOWER_STAGE = 7

/** Stem height at each stage, in units of the 24-unit grid the plant is drawn on. */
private val stemHeights = listOf(0f, 4f, 5.2f, 6.6f, 8.2f, 9.8f, 11.2f, 12f, 12.4f, 12.8f)

/** How far the plant has grown for a streak of [days]: 0 is a seed in its pot. */
private fun growthStage(days: Int): Int = growthSteps.count { days >= it }

/**
 * The streak, drawn as a pot plant that grows with it. Decorative only: wherever it appears the
 * number of days is written beside it.
 */
@Composable
fun StreakPlant(days: Int, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    val stage = growthStage(days)
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.minDimension / 24f
        val centreX = 12f * u
        val stemBase = 16.6f * u
        val stemTop = stemBase - stemHeights[stage] * u

        if (stage == 0) {
            // A seed just showing above the rim: nothing has grown yet, but it is planted.
            drawCircle(SuccessGreen, radius = 1.2f * u, center = Offset(centreX, 16.1f * u))
        } else {
            drawLine(
                color = SuccessGreen,
                start = Offset(centreX, stemBase),
                end = Offset(centreX, stemTop),
                strokeWidth = 1.3f * u,
                cap = StrokeCap.Round
            )
            drawLeaves(stage, centreX, stemBase, stemTop, u)
            if (stage == BUD_STAGE) drawCircle(RehabPrimaryLight, radius = 1.5f * u, center = Offset(centreX, stemTop))
            if (stage >= FIRST_FLOWER_STAGE) drawBlossoms(stage, centreX, stemTop, u)
        }
        drawPot(u)
    }
}

/** [StreakPlant] on a soft green disc, sized like an [IconBubble]. */
@Composable
fun StreakPlantBubble(days: Int, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(SuccessSoft, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        StreakPlant(days = days, size = size * 0.8f)
    }
}

private fun DrawScope.drawPot(u: Float) {
    val body = Path().apply {
        moveTo(7.6f * u, 18.4f * u)
        lineTo(16.4f * u, 18.4f * u)
        lineTo(15.2f * u, 22.2f * u)
        cubicTo(15.1f * u, 22.8f * u, 14.8f * u, 23f * u, 14.2f * u, 23f * u)
        lineTo(9.8f * u, 23f * u)
        cubicTo(9.2f * u, 23f * u, 8.9f * u, 22.8f * u, 8.8f * u, 22.2f * u)
        close()
    }
    drawPath(body, PlantPot)
    drawRoundRect(
        color = StreakWarm,
        topLeft = Offset(6.4f * u, 16.4f * u),
        size = Size(11.2f * u, 2.4f * u),
        cornerRadius = CornerRadius(1.2f * u)
    )
}

/**
 * One leaf per stage up to six, alternating sides. The first two open as a pair at the tip, the
 * way a seedling does; after that they climb the stem, smaller and more upright towards the top.
 */
private fun DrawScope.drawLeaves(stage: Int, centreX: Float, stemBase: Float, stemTop: Float, u: Float) {
    val count = stage.coerceAtMost(6)
    // Once there is a bud or flower, the leaves stop short of the tip to leave it room.
    val highest = if (stage >= BUD_STAGE) 0.74f else 1f
    for (index in 0 until count) {
        val along = when {
            count <= 2 -> 1f
            else -> 0.26f + (highest - 0.26f) * index / (count - 1)
        }
        val side = if (index % 2 == 0) 1f else -1f
        val length = (5.4f - 1.5f * along) * u
        val angle = Math.toRadians((26f + 30f * along).toDouble())
        val base = Offset(centreX, stemBase + (stemTop - stemBase) * along)
        val tip = Offset(base.x + side * length * cos(angle).toFloat(), base.y - length * sin(angle).toFloat())
        drawPath(leafPath(base, tip), if (side > 0) PlantLeafLight else SuccessGreen)
    }
}

/** An almond-shaped leaf from [base] to [tip]. */
private fun leafPath(base: Offset, tip: Offset): Path {
    val along = tip - base
    // Half the leaf's width, at right angles to its length.
    val across = Offset(-along.y, along.x) * 0.36f
    val nearBase = base + along * 0.25f
    val nearTip = base + along * 0.75f
    return Path().apply {
        moveTo(base.x, base.y)
        cubicTo(nearBase.x + across.x, nearBase.y + across.y, nearTip.x + across.x, nearTip.y + across.y, tip.x, tip.y)
        cubicTo(nearTip.x - across.x, nearTip.y - across.y, nearBase.x - across.x, nearBase.y - across.y, base.x, base.y)
        close()
    }
}

/** A flower at the tip, joined by a second and then a third on side stalks as the streak goes on. */
private fun DrawScope.drawBlossoms(stage: Int, centreX: Float, stemTop: Float, u: Float) {
    val sideBlossoms = listOf(
        Offset(centreX - 4.6f * u, stemTop + 2.2f * u),
        Offset(centreX + 4.6f * u, stemTop + 3.4f * u)
    ).take(stage - FIRST_FLOWER_STAGE)
    sideBlossoms.forEach { centre ->
        drawLine(
            color = SuccessGreen,
            start = Offset(centreX, centre.y + 1.8f * u),
            end = centre,
            strokeWidth = 0.9f * u,
            cap = StrokeCap.Round
        )
        drawFlower(centre, petalRadius = 0.95f * u)
    }
    drawFlower(Offset(centreX, stemTop - 0.4f * u), petalRadius = 1.35f * u)
}

private fun DrawScope.drawFlower(centre: Offset, petalRadius: Float) {
    repeat(5) { petal ->
        val angle = Math.toRadians(-90.0 + 72.0 * petal)
        val petalCentre = Offset(
            centre.x + 1.25f * petalRadius * cos(angle).toFloat(),
            centre.y + 1.25f * petalRadius * sin(angle).toFloat()
        )
        drawCircle(RehabPrimaryLight, radius = petalRadius, center = petalCentre)
    }
    drawCircle(StreakWarm, radius = petalRadius * 0.8f, center = centre)
}
