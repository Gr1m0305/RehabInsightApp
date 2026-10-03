package com.example.rehabinsight.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rehabinsight.ui.theme.RehabBackgroundBottom
import com.example.rehabinsight.ui.theme.RehabBackgroundTop
import com.example.rehabinsight.ui.theme.RehabError
import com.example.rehabinsight.ui.theme.RehabErrorSoft
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
import com.example.rehabinsight.ui.theme.RehabPrimaryLight
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabShadow
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabSurfaceSoft
import com.example.rehabinsight.ui.theme.RehabTextMuted
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.ui.theme.SceneryCloud
import com.example.rehabinsight.ui.theme.SceneryFarHill
import com.example.rehabinsight.ui.theme.SceneryMidHill
import com.example.rehabinsight.ui.theme.SceneryNearHillBottom
import com.example.rehabinsight.ui.theme.SceneryNearHillTop
import com.example.rehabinsight.ui.theme.ScenerySun
import com.example.rehabinsight.ui.theme.StreakWarm
import kotlin.math.min

/** Length of the gentle cross-fade used when moving between screens. */
const val SCREEN_FADE_MILLIS = 300

/** The pale sky-to-lavender wash that sits behind every screen. */
fun Modifier.calmBackground(): Modifier =
    background(Brush.verticalGradient(listOf(RehabBackgroundTop, RehabBackgroundBottom)))

/** Wide, low-contrast tinted shadow: lifts a surface off the background without a hard grey edge. */
fun Modifier.softShadow(shape: Shape, elevation: Dp = 10.dp): Modifier =
    shadow(elevation = elevation, shape = shape, clip = false, ambientColor = RehabShadow, spotColor = RehabShadow)

/** The periwinkle gradient used on feature cards, the logo mark and the avatar. Dark enough for white text. */
val PrimaryGradient: Brush
    get() = Brush.linearGradient(listOf(RehabPrimary, RehabPrimaryDeep))

/**
 * How much of the bottom of the current page sits underneath chrome that decoration may run
 * behind but content must stay clear of: the system navigation bar on full-screen flows, or
 * the rounded top corners of the bottom tab bar inside the main tabs.
 */
val LocalBottomChromeInset = compositionLocalOf { 0.dp }

/** Height of the [CalmScenery] illustration when it closes a page that already fills the screen. */
val SceneryHeight = 168.dp

/** Below this size the scenery is a slim band of hills, too small to carry the sun as well. */
private const val MIN_SUN_SCALE = 0.45f

/**
 * Soft calming gradient background used behind auth / onboarding / check-in flows.
 * Content is kept clear of the status bar and the keyboard. The bottom edge is left to the page,
 * so its closing scenery can run underneath the system navigation bar ([LocalBottomChromeInset]).
 */
@Composable
fun GradientScreenBackground(content: @Composable BoxScope.() -> Unit) {
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    CompositionLocalProvider(
        LocalContentColor provides RehabTextPrimary,
        LocalBottomChromeInset provides navigationBarInset
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .calmBackground()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .imePadding(),
            content = content
        )
    }
}

/**
 * Vertically scrolling page that never ends in blank space: whatever height the content leaves
 * over is handed to a [CalmScenery] illustration, and pages longer than the screen simply close
 * with it once scrolled to the end. A page that almost fills the screen gets a slim band of hills
 * in the space that is left, so it never has to scroll just to reach the illustration.
 */
@Composable
fun CalmPage(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) {
    val bottomInset = LocalBottomChromeInset.current
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportHeight = constraints.maxHeight
        Layout(
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    horizontalAlignment = horizontalAlignment,
                    content = content
                )
                CalmScenery()
            },
            modifier = Modifier.verticalScroll(scrollState)
        ) { measurables, pageConstraints ->
            val width = pageConstraints.maxWidth
            val body = measurables[0].measure(Constraints(minWidth = width, maxWidth = width))
            val leftover = viewportHeight - body.height
            val sceneryHeight = if (leftover > 0) leftover else (SceneryHeight + bottomInset).roundToPx()
            val scenery = measurables[1].measure(Constraints.fixed(width, sceneryHeight))
            layout(width, body.height + sceneryHeight) {
                body.place(0, 0)
                scenery.place(0, body.height)
            }
        }
    }
}

/**
 * Decorative landscape - soft layered hills, a low sun and a couple of clouds - that closes every
 * page. It adapts to whatever height it is given: in a short strip the hills are drawn smaller,
 * and in a tall one they stay along the bottom edge while the sky above them grows. The part
 * hidden under the navigation or tab bar ([LocalBottomChromeInset]) is simply filled by the
 * nearest hill, so the whole picture stays visible above it.
 */
@Composable
fun CalmScenery(modifier: Modifier = Modifier) {
    val bottomInset = LocalBottomChromeInset.current
    Canvas(modifier = modifier.fillMaxWidth()) {
        val w = size.width
        val h = size.height
        val visibleBottom = (h - bottomInset.toPx()).coerceAtLeast(0f)
        val band = min(visibleBottom, SceneryHeight.toPx())
        if (band <= 0f) {
            // Entirely underneath the navigation or tab bar: nothing to draw but the ground colour.
            drawRect(SceneryNearHillBottom)
            return@Canvas
        }
        val top = visibleBottom - band
        val scale = band / SceneryHeight.toPx()
        // A point's height within the hill band: 0 is the top of the band, 1 its visible bottom edge.
        fun y(fraction: Float) = top + band * fraction

        fun hill(startY: Float, control1: Offset, control2: Offset, endY: Float) = Path().apply {
            moveTo(0f, startY)
            cubicTo(control1.x, control1.y, control2.x, control2.y, w, endY)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        fun cloud(left: Float, baseline: Float, unit: Float) {
            drawRoundRect(
                color = SceneryCloud,
                topLeft = Offset(left, baseline - unit * 1.6f),
                size = Size(unit * 7.2f, unit * 1.6f),
                cornerRadius = CornerRadius(unit * 0.8f)
            )
            drawCircle(SceneryCloud, radius = unit * 1.5f, center = Offset(left + unit * 2.6f, baseline - unit * 1.9f))
            drawCircle(SceneryCloud, radius = unit * 1.1f, center = Offset(left + unit * 4.6f, baseline - unit * 1.7f))
        }

        // Clouds only appear when there is sky above the hills to put them in.
        if (top > 72.dp.toPx()) cloud(left = w * 0.12f, baseline = top * 0.68f, unit = 10.dp.toPx())
        if (top > 168.dp.toPx()) cloud(left = w * 0.58f, baseline = top * 0.30f, unit = 8.dp.toPx())

        if (scale >= MIN_SUN_SCALE) {
            val sun = Offset(w * 0.74f, y(0.30f))
            drawCircle(StreakWarm.copy(alpha = 0.08f), radius = 48.dp.toPx() * scale, center = sun)
            drawCircle(StreakWarm.copy(alpha = 0.14f), radius = 35.dp.toPx() * scale, center = sun)
            drawCircle(ScenerySun, radius = 23.dp.toPx() * scale, center = sun)
        }

        drawPath(
            hill(y(0.46f), Offset(w * 0.24f, y(0.02f)), Offset(w * 0.56f, y(0.58f)), y(0.32f)),
            SceneryFarHill
        )
        drawPath(
            hill(y(0.64f), Offset(w * 0.30f, y(0.84f)), Offset(w * 0.62f, y(0.26f)), y(0.52f)),
            SceneryMidHill
        )
        drawPath(
            hill(y(0.70f), Offset(w * 0.28f, y(0.50f)), Offset(w * 0.70f, y(1.04f)), y(0.76f)),
            Brush.verticalGradient(listOf(SceneryNearHillTop, SceneryNearHillBottom), startY = y(0.6f), endY = y(1f))
        )
    }
}

/** A topic icon on a white disc inside two soft halos: gives a sparse screen a gentle focal point. */
@Composable
fun HaloIcon(icon: ImageVector, modifier: Modifier = Modifier, diameter: Dp = 128.dp) {
    Box(
        modifier = modifier
            .size(diameter)
            .background(RehabPrimary.copy(alpha = 0.06f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(diameter * 0.76f)
                .background(RehabPrimary.copy(alpha = 0.09f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(diameter * 0.52f)
                    .softShadow(CircleShape, 10.dp)
                    .background(RehabSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = RehabPrimary, modifier = Modifier.size(diameter * 0.25f))
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .then(if (enabled) Modifier.softShadow(shape, 8.dp) else Modifier),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = RehabPrimary,
            contentColor = Color.White,
            disabledContainerColor = RehabPrimary.copy(alpha = 0.35f),
            disabledContentColor = Color.White
        ),
        elevation = null
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .softShadow(shape, 6.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = RehabSurface, contentColor = RehabPrimary),
        elevation = null
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    containerColor: Color = RehabSurface,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    elevated: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large
    val cardModifier = modifier
        .fillMaxWidth()
        .then(if (elevated) Modifier.softShadow(shape) else Modifier)
    val body: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = cardModifier, shape = shape, color = containerColor, content = body)
    } else {
        Surface(modifier = cardModifier, shape = shape, color = containerColor, content = body)
    }
}

/** A small icon on a soft tinted disc - the app's standard way of showing an icon. */
@Composable
fun IconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = RehabPrimary,
    background: Color = RehabPrimarySoft,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.52f))
    }
}

/** Round white button used for back / close, as in the reference designs. */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .softShadow(CircleShape, 6.dp),
        shape = CircleShape,
        color = RehabSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = RehabTextPrimary, modifier = Modifier.size(22.dp))
        }
    }
}

/** Category colour indicator: a solid dot inside a faint halo of the same colour. */
@Composable
fun ColorDot(color: Color, size: Dp = 10.dp) {
    Box(
        modifier = Modifier
            .size(size + 10.dp)
            .background(color.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun SoftChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = RehabPrimarySoft,
    contentColor: Color = RehabPrimary
) {
    Row(
        modifier = modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}

@Composable
fun StreakBadge(days: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .softShadow(CircleShape, 6.dp)
            .background(RehabSurface, CircleShape)
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp)
    ) {
        StreakPlantBubble(days = days, size = 34.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            "$days ${if (days == 1) "day" else "days"}",
            style = MaterialTheme.typography.labelLarge,
            color = RehabTextPrimary
        )
    }
}

/**
 * Circular progress in the style of the reference "score" ring: a faint halo, a gradient arc
 * and a white centre disc that holds [content].
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 172.dp,
    strokeWidth: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val haloWidth = 14.dp
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "ring-progress"
    )
    Box(
        modifier = modifier
            .size(diameter)
            .progressSemantics(progress.coerceIn(0f, 1f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val inset = haloWidth.toPx() + stroke / 2
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            drawCircle(color = RehabPrimary.copy(alpha = 0.07f))
            drawArc(
                color = RehabPrimary.copy(alpha = 0.14f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke)
            )
            if (animated > 0f) {
                drawArc(
                    brush = Brush.linearGradient(listOf(RehabPrimaryLight, RehabPrimary)),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(diameter - (haloWidth + strokeWidth + 10.dp) * 2)
                .softShadow(CircleShape, 8.dp)
                .background(RehabSurface, CircleShape),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/** Rounded linear progress bar with a gentle fill animation. */
@Composable
fun SoftProgressBar(progress: Float, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "bar-progress"
    )
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(RehabPrimarySoft)
            .progressSemantics(target)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .background(Brush.horizontalGradient(listOf(RehabPrimaryLight, RehabPrimary)), CircleShape)
        )
    }
}

/** Page title with an optional one-line explanation underneath. */
@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
        }
    }
}

@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.semantics { heading() }
    )
}

/** Text input on a soft tinted fill - no outline until it has focus. */
@Composable
fun RehabTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val leading: @Composable (() -> Unit)? = leadingIcon?.let { icon ->
        { Icon(icon, contentDescription = null) }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = RehabTextSecondary)
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = leading,
            trailingIcon = trailingIcon,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RehabSurfaceSoft,
                unfocusedContainerColor = RehabSurfaceSoft,
                focusedBorderColor = RehabPrimary,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = RehabPrimary,
                focusedTextColor = RehabTextPrimary,
                unfocusedTextColor = RehabTextPrimary,
                focusedLeadingIconColor = RehabPrimary,
                unfocusedLeadingIconColor = RehabTextMuted,
                focusedTrailingIconColor = RehabTextSecondary,
                unfocusedTrailingIconColor = RehabTextMuted,
                focusedPlaceholderColor = RehabTextMuted,
                unfocusedPlaceholderColor = RehabTextMuted
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Sign-in / validation feedback, kept deliberately quiet: a soft tinted note rather than bare red text. */
@Composable
fun GentleError(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RehabErrorSoft, MaterialTheme.shapes.small)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = RehabError, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = RehabError)
    }
}

@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier, color = RehabPrimary, trackColor = RehabPrimarySoft)
}
