package com.example.rehabinsight.ui.library

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rehabinsight.R
import com.example.rehabinsight.data.Article
import com.example.rehabinsight.data.SeedData

/** One short, scannable takeaway shown on an article's detail screen. */
data class KeyPoint(val icon: ImageVector, val text: String)

/**
 * Presentation-only companion to an [Article] row: its banner photo and a condensed, scannable
 * version of the body. Kept here, keyed by article_id, so the Article table stays identical to
 * the ERD. The key points paraphrase the article's own body text - keep the two in step.
 */
data class ArticleVisuals(
    @DrawableRes val banner: Int,
    val tagline: String,
    val keyPoints: List<KeyPoint>
)

// Banner photos are from Unsplash (unsplash.com/license); the photo id is noted beside each one.
private val visualsByArticleId: Map<Int, ArticleVisuals> = mapOf(
    SeedData.ART_SLEEP_BASICS to ArticleVisuals(
        banner = R.drawable.article_sleep_basics, // photo-1519681393784-d120267933ba
        tagline = "Five small levers for better rest",
        keyPoints = listOf(
            KeyPoint(Icons.Rounded.Tune, "You don't need to fix everything at once. Pick one lever tonight."),
            KeyPoint(Icons.Rounded.Bedtime, "A steady wind-down teaches your body that bed means sleep."),
            KeyPoint(Icons.Rounded.WbSunny, "Morning daylight sets your body clock for the day."),
            KeyPoint(Icons.Rounded.Coffee, "Caffeine lingers. Even six hours before bed it can disrupt sleep."),
            KeyPoint(Icons.Rounded.PhoneAndroid, "Screens delay melatonin and keep your mind alert.")
        )
    ),
    SeedData.ART_STRESS_BREATHING to ArticleVisuals(
        banner = R.drawable.article_stress_breathing, // photo-1439066615861-d1af74d74000
        tagline = "Calm your body in a few minutes",
        keyPoints = listOf(
            KeyPoint(Icons.Rounded.Psychology, "What matters most is how overwhelmed you feel, and you can influence that."),
            KeyPoint(Icons.Rounded.Air, "A longer out-breath is your body's built-in brake pedal."),
            KeyPoint(Icons.AutoMirrored.Rounded.DirectionsWalk, "A 10-minute walk measurably eases anxiety."),
            KeyPoint(Icons.Rounded.Park, "A few minutes in green space lowers cortisol."),
            KeyPoint(Icons.Rounded.EditNote, "Putting worries on paper quiets a looping mind.")
        )
    ),
    SeedData.ART_MOVEMENT_AND_MOOD to ArticleVisuals(
        banner = R.drawable.article_movement_mood, // photo-1551632811-561732d1e306
        tagline = "Why a little movement goes a long way",
        keyPoints = listOf(
            KeyPoint(Icons.Rounded.Mood, "Movement is one of the most effective things we have for depression and anxiety."),
            KeyPoint(Icons.AutoMirrored.Rounded.TrendingUp, "The biggest gain comes from going from nothing to something."),
            KeyPoint(Icons.Rounded.Stairs, "It doesn't have to look like exercise. Stairs and stretching count."),
            KeyPoint(Icons.Rounded.FitnessCenter, "Simple strength moves protect everyday independence."),
            KeyPoint(Icons.Rounded.Spa, "In pain? Keep moving, gently and at your own pace.")
        )
    ),
    SeedData.ART_CONNECTION to ArticleVisuals(
        banner = R.drawable.article_connection, // photo-1511632765486-a01980e01a18
        tagline = "Small moments of contact matter",
        keyPoints = listOf(
            KeyPoint(Icons.Rounded.Favorite, "Connection is one of the strongest protectors of health."),
            KeyPoint(Icons.AutoMirrored.Rounded.Chat, "Even a short message lifts mood and belonging."),
            KeyPoint(Icons.Rounded.Call, "A call brings you closer than a text."),
            KeyPoint(Icons.Rounded.Groups, "One face-to-face catch-up a week is a meaningful target."),
            KeyPoint(Icons.Rounded.VolunteerActivism, "Kindness lifts the giver as much as the receiver.")
        )
    ),
    SeedData.ART_BUILDING_MOMENTUM to ArticleVisuals(
        banner = R.drawable.article_building_momentum, // photo-1470252649378-9c29740c9fa8
        tagline = "Start smaller than you think",
        keyPoints = listOf(
            KeyPoint(Icons.Rounded.FavoriteBorder, "Low motivation is not a character flaw."),
            KeyPoint(Icons.Rounded.PlayArrow, "Motivation follows action. Starting is the lever."),
            KeyPoint(Icons.Rounded.CleaningServices, "A small, visible win restores a sense of control."),
            KeyPoint(Icons.Rounded.EditNote, "Written plans roughly double follow-through."),
            KeyPoint(Icons.Rounded.DoneAll, "Finishing something small builds belief for the next thing.")
        )
    ),
    SeedData.ART_MOVING_WITH_PAIN to ArticleVisuals(
        banner = R.drawable.article_moving_with_pain, // photo-1441974231531-c6227db76b6e
        tagline = "Keep moving, gently and safely",
        keyPoints = listOf(
            KeyPoint(Icons.Rounded.Healing, "Some discomfort while moving is common. It isn't a sign of damage."),
            KeyPoint(Icons.Rounded.Timelapse, "Small, planned doses with rest work better than rest alone."),
            KeyPoint(Icons.Rounded.SwapHoriz, "Swap for something gentler rather than skipping movement."),
            KeyPoint(Icons.Rounded.Speed, "Pacing protects consistency, which matters more than intensity."),
            KeyPoint(Icons.Rounded.ThumbUp, "There is no shame in modifying a task.")
        )
    )
)

/** Photo + condensed content for this article, or null for articles that don't have any yet. */
val Article.visuals: ArticleVisuals? get() = visualsByArticleId[articleId]
