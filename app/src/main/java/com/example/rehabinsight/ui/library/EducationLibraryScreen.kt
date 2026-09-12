package com.example.rehabinsight.ui.library

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
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
import com.example.rehabinsight.data.Article
import com.example.rehabinsight.ui.theme.RehabBackgroundBottom
import com.example.rehabinsight.ui.theme.RehabBlue
import com.example.rehabinsight.ui.theme.RehabTextSecondary

private fun iconFor(articleId: Int): ImageVector = when (articleId) {
    1 -> Icons.Filled.Bedtime // Sleep Basics
    2 -> Icons.Filled.SelfImprovement // Stress & Breathing
    3 -> Icons.AutoMirrored.Filled.DirectionsWalk // Movement & Mood
    4 -> Icons.Filled.Groups // Connection
    5 -> Icons.Filled.RocketLaunch // Building Momentum
    6 -> Icons.Filled.Spa // Moving With Pain
    else -> Icons.Filled.FavoriteBorder
}

/** The first paragraph of an article's body, used as its list-view summary. */
private val Article.summary: String
    get() = bodyText.substringBefore("\n\n")

/** The remaining paragraphs of the article body, split for individual paragraph cards. */
private val Article.paragraphs: List<String>
    get() = bodyText.split("\n\n").filter { it.isNotBlank() }

@Composable
fun EducationLibraryScreen(articles: List<Article>) {
    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    if (selectedArticle == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = RehabBlue)
                Spacer(Modifier.width(10.dp))
                Text("Education library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Short, practical reads for whenever you need them.",
                style = MaterialTheme.typography.bodyMedium,
                color = RehabTextSecondary
            )
            Spacer(Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                articles.forEach { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedArticle = article },
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
                                    .background(RehabBackgroundBottom),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(iconFor(article.articleId), contentDescription = null, tint = RehabBlue)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(article.summary, style = MaterialTheme.typography.bodySmall, color = RehabTextSecondary)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = RehabTextSecondary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    } else {
        val article = selectedArticle!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedArticle = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(article.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = RehabTextSecondary)
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                article.paragraphs.forEach { paragraph ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(paragraph, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
