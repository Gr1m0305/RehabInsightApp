package com.example.rehabinsight.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rehabinsight.data.Article
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.Reflection
import com.example.rehabinsight.ui.components.LocalBottomChromeInset
import com.example.rehabinsight.ui.components.SCREEN_FADE_MILLIS
import com.example.rehabinsight.ui.components.calmBackground
import com.example.rehabinsight.ui.components.softShadow
import com.example.rehabinsight.ui.home.HomeScreen
import com.example.rehabinsight.ui.library.EducationLibraryScreen
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.navigation.Routes
import com.example.rehabinsight.ui.profile.ProfileScreen
import com.example.rehabinsight.ui.tasks.TaskCustomisationScreen
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimarySoft
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Rounded.Home),
    BottomTab(Routes.TASKS, "Tasks", Icons.Rounded.TaskAlt),
    BottomTab(Routes.LIBRARY, "Library", Icons.AutoMirrored.Rounded.MenuBook),
    BottomTab(Routes.PROFILE, "Profile", Icons.Rounded.Person)
)

private val bottomBarCornerRadius = 28.dp
private val bottomBarShape = RoundedCornerShape(topStart = bottomBarCornerRadius, topEnd = bottomBarCornerRadius)

@Composable
fun MainScaffold(
    userName: String,
    userEmail: String,
    streak: Int,
    longestStreak: Int,
    topMessage: String,
    reflection: Reflection,
    highlightedTasks: List<ChecklistItem>,
    guidanceIntro: String,
    checklist: List<ChecklistItem>,
    compassionMessage: String,
    weeklyConsistency: List<Boolean>,
    categories: List<Category>,
    articles: List<Article>,
    onToggleTask: (Int) -> Unit,
    onAddTask: (String, Int) -> Unit,
    onUpdateTask: (Int, String, Int) -> Unit,
    onRemoveTask: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.calmBackground(),
        containerColor = Color.Transparent,
        contentColor = RehabTextPrimary,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(bottomBarShape, 16.dp),
                shape = bottomBarShape,
                color = RehabSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(72.dp)
                        .padding(horizontal = 12.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomTabs.forEach { tab ->
                        BottomTabItem(
                            tab = tab,
                            selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        // Pages run on underneath the tab bar's rounded corners, so their closing scenery meets its curve.
        val pageBottomPadding = (padding.calculateBottomPadding() - bottomBarCornerRadius).coerceAtLeast(0.dp)
        CompositionLocalProvider(LocalBottomChromeInset provides bottomBarCornerRadius) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding(), bottom = pageBottomPadding)
                    .consumeWindowInsets(padding),
                enterTransition = { fadeIn(tween(SCREEN_FADE_MILLIS)) },
                exitTransition = { fadeOut(tween(SCREEN_FADE_MILLIS)) }
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        userName = userName,
                        streak = streak,
                        topMessage = topMessage,
                        reflection = reflection,
                        highlightedTasks = highlightedTasks,
                        guidanceIntro = guidanceIntro,
                        checklist = checklist,
                        compassionMessage = compassionMessage,
                        onToggleTask = onToggleTask
                    )
                }
                composable(Routes.TASKS) {
                    TaskCustomisationScreen(
                        checklist = checklist,
                        categories = categories,
                        onAddTask = onAddTask,
                        onUpdateTask = onUpdateTask,
                        onRemoveTask = onRemoveTask
                    )
                }
                composable(Routes.LIBRARY) {
                    EducationLibraryScreen(articles = articles)
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        name = userName,
                        email = userEmail,
                        currentStreak = streak,
                        longestStreak = longestStreak,
                        weeklyConsistency = weeklyConsistency,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomTabItem(tab: BottomTab, selected: Boolean, onClick: () -> Unit) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) RehabPrimary else RehabTextSecondary,
        animationSpec = tween(250),
        label = "tab-content"
    )
    val pillColor by animateColorAsState(
        targetValue = if (selected) RehabPrimarySoft else Color.Transparent,
        animationSpec = tween(250),
        label = "tab-pill"
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(vertical = 6.dp)
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 30.dp)
                .background(pillColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(tab.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(tab.label, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}
