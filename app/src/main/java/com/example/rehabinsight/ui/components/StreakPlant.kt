package com.example.rehabinsight.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.theme.PlantGroundFar
import com.example.rehabinsight.ui.theme.PlantGroundNear
import com.example.rehabinsight.ui.theme.PlantLeafLight
import com.example.rehabinsight.ui.theme.RehabPrimaryLight
import com.example.rehabinsight.ui.theme.StreakWarm
import com.example.rehabinsight.ui.theme.SuccessGreen
import com.example.rehabinsight.ui.theme.SuccessSoft
import kotlin.math.cos
import kotlin.math.sin

/**
 * The streak lengths, in days, at which the plant takes its next step: taller with a new leaf
 * at each step up to a week, a bud at two weeks, a flower at a month and further blossoms
 * after that.
 */
private val growthSteps = listOf(1, 2, 3, 5, 7, 14, 30, 50, 100)

private const val BUD_STAGE = 6
private const val FIRST_FLOWER_STAGE = 7

/** Where the stem leaves the ground, on the 24-unit grid the plant is drawn on (the circle's diameter). */
private const val GROUND_Y = 17.8f

/** Stem height above the ground at each stage, in grid units. */
private val stemHeights = listOf(1.8f, 3.2f, 4.8f, 6.4f, 7.8f, 9f, 9.6f, 9.9f, 10.2f, 10.4f)

/** The seed leaves sit at the tip of a new sprout, then stay this far up the stem as it grows past them. */
private const val SEED_LEAF_HEIGHT = 3.2f

/** How far the plant has grown for a streak of [days]: 0 is a sprout just out of the ground. */
private fun growthStage(days: Int): Int = growthSteps.count { days >= it }

/**
 * The streak, drawn as a plant growing out of soft hills on a disc of [background]. It fills
 * the circle, and the hills follow its curve. Decorative only: wherever it appears the number
 * of days is written beside it.
 */
@Composable
fun StreakPlantBubble(
    days: Int,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    background: Color = SuccessSoft
) {
    val stage = growthStage(days)
    Canvas(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
    ) {
        val u = this.size.minDimension / 24f
        drawHills(u)

        val stem = Stem(stemHeights[stage], u)
        drawPath(stem.path(), SuccessGreen, style = Stroke(width = 1.25f * u, cap = StrokeCap.Round))
        drawSeedLeaves(stage, stem, u)
        drawLeaves(stage, stem, u)
        if (stage == BUD_STAGE) drawBud(stem.tip, u)
        if (stage >= FIRST_FLOWER_STAGE) drawBlossoms(stage, stem, u)
    }
}

/** Two layers of rolling ground across the bottom of the circle, like the scenery that closes each page. */
private fun DrawScope.drawHills(u: Float) {
    val far = Path().apply {
        moveTo(-1f * u, 17.2f * u)
        cubicTo(6f * u, 14.6f * u, 15f * u, 15.4f * u, 25f * u, 18.4f * u)
        lineTo(25f * u, 25f * u)
        lineTo(-1f * u, 25f * u)
        close()
    }
    drawPath(far, PlantGroundFar)
    val near = Path().apply {
        moveTo(-1f * u, 20.6f * u)
        cubicTo(5f * u, 16.8f * u, 18f * u, 16.6f * u, 25f * u, 19.8f * u)
        lineTo(25f * u, 25f * u)
        lineTo(-1f * u, 25f * u)
        close()
    }
    drawPath(near, PlantGroundNear)
}

/** The stem: a gentle S-curve from the ground that sways a little more the taller it gets. */
private class Stem(val height: Float, u: Float) {
    private val sway = 0.6f * (height / 12f)
    val base = Offset(12f * u, (GROUND_Y + 0.4f) * u)
    val tip = Offset((12f + sway * 0.4f) * u, (GROUND_Y - height) * u)
    private val control1 = Offset((12f - sway) * u, (GROUND_Y - height * 0.35f) * u)
    private val control2 = Offset((12f + sway) * u, (GROUND_Y - height * 0.7f) * u)

    /** The point [t] of the way up the stem, 0 at the ground and 1 at the tip. */
    fun at(t: Float): Offset {
        val s = 1f - t
        return base * (s * s * s) + control1 * (3f * s * s * t) + control2 * (3f * s * t * t) + tip * (t * t * t)
    }

    fun path(): Path = Path().apply {
        moveTo(base.x, base.y)
        cubicTo(control1.x, control1.y, control2.x, control2.y, tip.x, tip.y)
    }
}

/** The first pair of rounded leaves: open in a V at the tip of a new sprout, then left behind low on the stem. */
private fun DrawScope.drawSeedLeaves(stage: Int, stem: Stem, u: Float) {
    val atTip = stage <= 1
    val node = if (atTip) stem.tip else stem.at(SEED_LEAF_HEIGHT / stem.height)
    val length = if (atTip) (2.8f + 0.9f * stage) * u else 4.2f * u
    val angle = Math.toRadians(if (atTip) 34.0 else 22.0)
    listOf(1f, -1f).forEach { side ->
        val tip = Offset(node.x + side * length * cos(angle).toFloat(), node.y - length * sin(angle).toFloat())
        drawPath(leafPath(node, tip, width = 0.56f), if (side > 0) PlantLeafLight else SuccessGreen)
    }
}

/**
 * The leaves that follow the seed leaves, one more at each stage up to five, alternating sides
 * as they climb. The newest, at the top, is the smallest and most upright.
 */
private fun DrawScope.drawLeaves(stage: Int, stem: Stem, u: Float) {
    val count = (stage - 1).coerceIn(0, 5)
    if (count == 0) return
    val lowest = (SEED_LEAF_HEIGHT + 0.9f) / stem.height
    // Once there is a bud or flower, the leaves stop short of the tip, and the top ones are
    // shorter, so none of them reaches up over it.
    val flowering = stage >= BUD_STAGE
    val highest = if (flowering) 0.68f else 0.94f
    val shrink = if (flowering) 2.4f else 1.9f
    for (index in 0 until count) {
        val position = if (count == 1) 1f else index / (count - 1f)
        val along = if (count == 1) highest else lowest + (highest - lowest) * position
        val side = if (index % 2 == 0) -1f else 1f
        val length = (5.2f - shrink * position) * u
        val angle = Math.toRadians((24f + 32f * position).toDouble())
        val base = stem.at(along)
        val tip = Offset(base.x + side * length * cos(angle).toFloat(), base.y - length * sin(angle).toFloat())
        drawPath(leafPath(base, tip, width = 0.38f), if (side > 0) PlantLeafLight else SuccessGreen)
    }
}

/** An almond-shaped leaf from [base] to [tip]; [width] is half its width as a share of its length. */
private fun leafPath(base: Offset, tip: Offset, width: Float): Path {
    val along = tip - base
    val across = Offset(-along.y, along.x) * width
    val nearBase = base + along * 0.25f
    val nearTip = base + along * 0.75f
    return Path().apply {
        moveTo(base.x, base.y)
        cubicTo(nearBase.x + across.x, nearBase.y + across.y, nearTip.x + across.x, nearTip.y + across.y, tip.x, tip.y)
        cubicTo(nearTip.x - across.x, nearTip.y - across.y, nearBase.x - across.x, nearBase.y - across.y, base.x, base.y)
        close()
    }
}

/** A closed bud at the tip, cupped by two small green leaves. */
private fun DrawScope.drawBud(tip: Offset, u: Float) {
    drawOval(
        color = RehabPrimaryLight,
        topLeft = Offset(tip.x - 1.2f * u, tip.y - 3f * u),
        size = Size(2.4f * u, 3.2f * u)
    )
    listOf(1f, -1f).forEach { side ->
        drawPath(leafPath(tip, Offset(tip.x + side * 1.5f * u, tip.y - 1.7f * u), width = 0.4f), SuccessGreen)
    }
}

/** A flower at the tip, joined by a second and then a third on side stalks as the streak goes on. */
private fun DrawScope.drawBlossoms(stage: Int, stem: Stem, u: Float) {
    val sideBlossoms = listOf(
        stem.at(0.6f) to Offset(-4.8f * u, -2.6f * u),
        stem.at(0.48f) to Offset(4.9f * u, -2.2f * u)
    ).take(stage - FIRST_FLOWER_STAGE)
    sideBlossoms.forEach { (from, offset) ->
        val centre = from + offset
        drawLine(
            color = SuccessGreen,
            start = from,
            end = centre,
            strokeWidth = 0.9f * u,
            cap = StrokeCap.Round
        )
        drawFlower(centre, petalRadius = 1.05f * u)
    }
    drawFlower(Offset(stem.tip.x, stem.tip.y - 0.6f * u), petalRadius = 1.45f * u)
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
