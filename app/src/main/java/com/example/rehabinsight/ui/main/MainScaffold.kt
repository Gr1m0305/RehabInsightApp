package com.example.rehabinsight.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rehabinsight.data.Article
import com.example.rehabinsight.data.Category
import com.example.rehabinsight.data.Reflection
import com.example.rehabinsight.ui.home.HomeScreen
import com.example.rehabinsight.ui.library.EducationLibraryScreen
import com.example.rehabinsight.ui.model.ChecklistItem
import com.example.rehabinsight.ui.navigation.Routes
import com.example.rehabinsight.ui.profile.ProfileScreen
import com.example.rehabinsight.ui.tasks.TaskCustomisationScreen
import com.example.rehabinsight.ui.theme.RehabBlue
import com.example.rehabinsight.ui.theme.RehabTextMuted

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home),
    BottomTab(Routes.TASKS, "Tasks", Icons.Filled.CheckCircle),
    BottomTab(Routes.LIBRARY, "Library", Icons.AutoMirrored.Filled.MenuBook),
    BottomTab(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

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
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                bottomTabs.forEach { tab ->
                    val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RehabBlue,
                            selectedTextColor = RehabBlue,
                            unselectedIconColor = RehabTextMuted,
                            unselectedTextColor = RehabTextMuted,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
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
