package com.example.rehabinsight.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rehabinsight.data.Article
import com.example.rehabinsight.ui.components.CalmPage
import com.example.rehabinsight.ui.components.CalmScenery
import com.example.rehabinsight.ui.components.CircleIconButton
import com.example.rehabinsight.ui.components.IconBubble
import com.example.rehabinsight.ui.components.LocalBottomChromeInset
import com.example.rehabinsight.ui.components.PrimaryGradient
import com.example.rehabinsight.ui.components.SceneryHeight
import com.example.rehabinsight.ui.components.ScreenHeader
import com.example.rehabinsight.ui.components.SectionHeading
import com.example.rehabinsight.ui.components.SoftCard
import com.example.rehabinsight.ui.components.SoftChip
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.theme.RehabDivider
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import kotlin.math.ceil

private const val RELATED_TASKS_PREFIX = "Related tasks:"
private const val WORDS_PER_MINUTE = 200.0

/** A bracketed research citation, e.g. "(Trauer et al., 2015, Annals of Internal Medicine)". */
private val citationPattern = Regex("""\([^()]*\b(?:19|20)\d{2}\b[^()]*\)""")

private val Article.paragraphs: List<String>
    get() = bodyText.split("\n\n").filter { it.isNotBlank() }

/** The article body without its trailing "Related tasks: ..." line (shown separately). */
private val Article.readingParagraphs: List<String>
    get() = paragraphs.filterNot { it.startsWith(RELATED_TASKS_PREFIX) }

/** Task titles from the body's "Related tasks: a · b · c" line, if it has one. */
private val Article.relatedTasks: List<String>
    get() = paragraphs.firstOrNull { it.startsWith(RELATED_TASKS_PREFIX) }
        ?.removePrefix(RELATED_TASKS_PREFIX)
        ?.split("·")
        ?.map { it.trim().removeSuffix(".") }
        ?.filter { it.isNotBlank() }
        .orEmpty()

private val Article.readTimeLabel: String
    get() {
        val words = bodyText.split(Regex("\\s+")).count { it.isNotBlank() }
        return "${ceil(words / WORDS_PER_MINUTE).toInt().coerceAtLeast(1)} min read"
    }

/** Renders research citations smaller and quieter so the sentence around them stays easy to follow. */
private fun withQuietCitations(paragraph: String): AnnotatedString = buildAnnotatedString {
    append(paragraph)
    citationPattern.findAll(paragraph).forEach { match ->
        addStyle(SpanStyle(color = RehabTextSecondary, fontSize = 13.sp), match.range.first, match.range.last + 1)
    }
}

/** Photo header with a gentle wave along its bottom edge, as in the reference article screen. */
private val HeroWaveShape = GenericShape { size, _ ->
    val dip = size.height * 0.07f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - dip * 1.6f)
    cubicTo(
        size.width * 0.7f, size.height - dip * 3f,
        size.width * 0.35f, size.height + dip * 1.2f,
        0f, size.height - dip * 0.8f
    )
    close()
}

@Composable
fun EducationLibraryScreen(articles: List<Article>) {
    var selectedArticleId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedArticle = articles.find { it.articleId == selectedArticleId }
    val listState = rememberLazyListState()

    BackHandler(enabled = selectedArticle != null) { selectedArticleId = null }

    Crossfade(targetState = selectedArticle, animationSpec = tween(350), label = "library") { article ->
        if (article == null) {
            ArticleList(articles = articles, listState = listState, onOpen = { selectedArticleId = it.articleId })
        } else {
            ArticleDetail(article = article, onBack = { selectedArticleId = null })
        }
    }
}

@Composable
private fun ArticleList(articles: List<Article>, listState: LazyListState, onOpen: (Article) -> Unit) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Education library",
                subtitle = "Short, practical reads for whenever you need them.",
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp)
            )
        }
        items(articles, key = { it.articleId }) { article ->
            ArticleCard(
                article = article,
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = { onOpen(article) }
            )
        }
        item {
            CalmScenery(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .height(SceneryHeight + LocalBottomChromeInset.current)
            )
        }
    }
}

/** Photo-led card: the picture carries the topic, with only a title, a tagline and the read time on top. */
@Composable
private fun ArticleCard(article: Article, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    val visuals = article.visuals
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .softShadow(shape),
        shape = shape,
        color = RehabSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            ArticleBanner(visuals, Modifier.matchParentSize())
            // Scrim so the title stays legible over any photo, with a faint tint up top for very bright skies.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(0f to Color(0x24181732), 0.35f to Color(0x0F181732), 1f to Color(0xC2181732)))
            )
            SoftChip(
                text = article.readTimeLabel,
                icon = Icons.Rounded.Schedule,
                containerColor = Color.White.copy(alpha = 0.92f),
                contentColor = RehabTextPrimary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(article.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                if (visuals != null) {
                    Text(visuals.tagline, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ArticleBanner(visuals: ArticleVisuals?, modifier: Modifier = Modifier) {
    if (visuals != null) {
        Image(
            painter = painterResource(visuals.banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(PrimaryGradient), contentAlignment = Alignment.Center) {
            Icon(
                Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

/**
 * Article view, lightest content first: photo, a handful of takeaways and the linked tasks.
 * The full evidence-based text sits behind a "read more" so nobody is met with a wall of words.
 */
@Composable
private fun ArticleDetail(article: Article, onBack: () -> Unit) {
    val visuals = article.visuals
    val relatedTasks = remember(article) { article.relatedTasks }
    val paragraphs = remember(article) { article.readingParagraphs.map(::withQuietCitations) }
    val readTime = remember(article) { article.readTimeLabel }
    // Articles with no condensed version simply open on their full text.
    var showFullText by rememberSaveable(article.articleId) { mutableStateOf(visuals == null) }

    CalmPage(horizontalPadding = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp)
        ) {
            ArticleBanner(
                visuals,
                Modifier
                    .matchParentSize()
                    .clip(HeroWaveShape)
            )
            CircleIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back to library",
                modifier = Modifier.padding(16.dp),
                onClick = onBack
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(4.dp))
            Text(
                article.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
            if (visuals != null) {
                Spacer(Modifier.height(2.dp))
                Text(visuals.tagline, style = MaterialTheme.typography.bodyLarge, color = RehabTextSecondary)
            }
            Spacer(Modifier.height(14.dp))
            SoftChip(text = readTime, icon = Icons.Rounded.Schedule)

            if (visuals != null) {
                Spacer(Modifier.height(28.dp))
                SectionHeading("In short")
                Spacer(Modifier.height(12.dp))
                SoftCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)) {
                    visuals.keyPoints.forEachIndexed { index, point ->
                        if (index > 0) HorizontalDivider(color = RehabDivider)
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconBubble(point.icon, size = 38.dp)
                            Spacer(Modifier.width(14.dp))
                            Text(point.text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (relatedTasks.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                SectionHeading("Small steps to try")
                Spacer(Modifier.height(12.dp))
                SoftCard(containerColor = RehabPrimarySoft, elevated = false, contentPadding = PaddingValues(18.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        relatedTasks.forEach { task ->
                            Row {
                                Icon(
                                    Icons.Rounded.TaskAlt,
                                    contentDescription = null,
                                    tint = RehabPrimary,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(task, style = MaterialTheme.typography.bodyMedium, color = RehabTextPrimary)
                            }
                        }
                    }
                }
            }

            if (visuals != null) {
                Spacer(Modifier.height(28.dp))
                FullArticleToggle(expanded = showFullText, onToggle = { showFullText = !showFullText })
            }
            AnimatedVisibility(visible = showFullText) {
                Column(
                    modifier = Modifier.padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    paragraphs.forEach { paragraph ->
                        Text(paragraph, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FullArticleToggle(expanded: Boolean, onToggle: () -> Unit) {
    val chevronRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevron")
    SoftCard(contentPadding = PaddingValues(16.dp), onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.AutoMirrored.Rounded.MenuBook, size = 38.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (expanded) "Hide the full article" else "Read the full article",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "With the research behind it",
                    style = MaterialTheme.typography.bodySmall,
                    color = RehabTextSecondary
                )
            }
            Icon(
                Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = RehabTextSecondary,
                modifier = Modifier.rotate(chevronRotation)
            )
        }
    }
}
