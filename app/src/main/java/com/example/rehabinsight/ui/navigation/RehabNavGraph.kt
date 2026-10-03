package com.example.rehabinsight.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rehabinsight.data.GuidanceEngine
import com.example.rehabinsight.data.MessagingEngine
import com.example.rehabinsight.data.ReflectionQuotes
import com.example.rehabinsight.ui.admin.AdminDashboardScreen
import com.example.rehabinsight.ui.admin.AdminLoginScreen
import com.example.rehabinsight.ui.auth.AuthScreen
import com.example.rehabinsight.ui.checkin.DailyCheckInScreen
import com.example.rehabinsight.ui.components.PrimaryGradient
import com.example.rehabinsight.ui.components.SCREEN_FADE_MILLIS
import com.example.rehabinsight.ui.components.SecondaryButton
import com.example.rehabinsight.ui.components.SoftChip
import com.example.rehabinsight.ui.components.StreakPlant
import com.example.rehabinsight.ui.components.calmBackground
import com.example.rehabinsight.ui.main.MainScaffold
import com.example.rehabinsight.ui.onboarding.ChecklistGenerationScreen
import com.example.rehabinsight.ui.onboarding.ReviewChecklistScreen
import com.example.rehabinsight.ui.onboarding.SetupQuestionnaireScreen
import com.example.rehabinsight.ui.theme.RehabPrimary
import com.example.rehabinsight.ui.theme.RehabPrimaryDeep
import com.example.rehabinsight.ui.theme.RehabSurface
import com.example.rehabinsight.ui.theme.RehabTextPrimary
import com.example.rehabinsight.ui.theme.RehabTextSecondary
import com.example.rehabinsight.viewmodel.AppViewModel
import java.time.LocalDate

private fun goAfterAuth(navController: NavHostController, viewModel: AppViewModel) {
    if (viewModel.uiState.value.currentClient == null) return
    val destination = when {
        !viewModel.hasCompletedSetup() -> Routes.SETUP_QUESTIONNAIRE
        !viewModel.hasCheckedInToday() -> Routes.DAILY_CHECK_IN
        else -> Routes.MAIN
    }
    navController.navigate(destination) {
        popUpTo(Routes.LOGIN) { inclusive = true }
    }
}

@Composable
fun RehabNavGraph() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .calmBackground()
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            enterTransition = { fadeIn(tween(SCREEN_FADE_MILLIS)) },
            exitTransition = { fadeOut(tween(SCREEN_FADE_MILLIS)) }
        ) {

            composable(Routes.LOGIN) {
                AuthScreen(
                    authError = uiState.authError,
                    isBusy = uiState.authInProgress,
                    onClearError = { viewModel.clearAuthError() },
                    onLogin = { email, password ->
                        viewModel.login(email, password) { success ->
                            if (success) goAfterAuth(navController, viewModel)
                        }
                    },
                    onSignUp = { name, email, phone, password ->
                        viewModel.signUp(name, email, phone, password) { success ->
                            if (success) goAfterAuth(navController, viewModel)
                        }
                    },
                    onAdminLoginClick = { navController.navigate(Routes.ADMIN_LOGIN) }
                )
            }

            composable(Routes.SETUP_QUESTIONNAIRE) {
                SetupQuestionnaireScreen(
                    onComplete = { answers ->
                        viewModel.submitSetupAnswers(answers)
                        navController.navigate(Routes.CHECKLIST_GENERATION) {
                            popUpTo(Routes.SETUP_QUESTIONNAIRE) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.CHECKLIST_GENERATION) {
                ChecklistGenerationScreen(
                    onGenerated = {
                        navController.navigate(Routes.REVIEW_CHECKLIST) {
                            popUpTo(Routes.CHECKLIST_GENERATION) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.REVIEW_CHECKLIST) {
                val checklist = viewModel.todaysChecklistItems()
                ReviewChecklistScreen(
                    checklist = checklist,
                    onContinue = {
                        navController.navigate(Routes.DAILY_CHECK_IN) {
                            popUpTo(Routes.REVIEW_CHECKLIST) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.DAILY_CHECK_IN) {
                LaunchedEffect(Unit) { viewModel.checkForMissedDay() }
                DailyCheckInScreen(
                    userName = uiState.currentClient?.firstName.orEmpty(),
                    onSubmit = { checkIn ->
                        viewModel.submitDailyCheckIn(checkIn)
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.DAILY_CHECK_IN) { inclusive = true }
                        }
                    },
                    onSkipAll = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.DAILY_CHECK_IN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.MAIN) {
                val client = uiState.currentClient
                if (client != null) {
                    val checklist = viewModel.todaysChecklistItems()
                    val highlighted = viewModel.todaysHighlightedItems()
                    val today = LocalDate.now()
                    val (completed, total) = viewModel.todayCompletionText()
                    val streak = uiState.clientStreaks.find { it.clientId == client.clientId }

                    MainScaffold(
                        userName = client.fullName,
                        userEmail = client.email,
                        streak = streak?.currentStreak ?: 0,
                        longestStreak = streak?.bestStreak ?: 0,
                        topMessage = MessagingEngine.topSupportiveMessage(completed, total),
                        reflection = ReflectionQuotes.forDate(today),
                        highlightedTasks = highlighted,
                        guidanceIntro = GuidanceEngine.guidanceIntro(),
                        checklist = checklist,
                        compassionMessage = MessagingEngine.compassionateFeedback(completed, total),
                        weeklyConsistency = viewModel.weeklyConsistency(),
                        categories = uiState.categories,
                        articles = viewModel.activeArticles(),
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onAddTask = { title, categoryId -> viewModel.addCustomTask(title, categoryId) },
                        onUpdateTask = { taskId, title, categoryId -> viewModel.updateTask(taskId, title, categoryId) },
                        onRemoveTask = { viewModel.removeTask(it) },
                        onLogout = {
                            viewModel.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Routes.ADMIN_LOGIN) {
                AdminLoginScreen(
                    authError = uiState.authError,
                    onClearError = { viewModel.clearAuthError() },
                    onLogin = { passcode ->
                        if (viewModel.adminLogin(passcode)) {
                            navController.navigate(Routes.ADMIN_DASHBOARD) {
                                popUpTo(Routes.ADMIN_LOGIN) { inclusive = true }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    progressSummaries = viewModel.clientProgressSummaries(),
                    tasksByCategory = viewModel.tasksByCategory(),
                    onAddTask = { title, categoryId -> viewModel.addLibraryTask(title, categoryId) },
                    onUpdateTask = { taskId, title, categoryId -> viewModel.updateLibraryTask(taskId, title, categoryId) },
                    onRemoveTask = { taskId -> viewModel.removeLibraryTask(taskId) },
                    onAssignTask = { clientId, taskId -> viewModel.assignTaskToClient(clientId, taskId) },
                    onLogout = {
                        viewModel.adminLogout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Global, compassionate ephemeral moments (Part 6) shown above any screen.
        if (uiState.pendingWelcomeBack) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissWelcomeBack() },
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = RehabSurface,
                titleContentColor = RehabTextPrimary,
                textContentColor = RehabTextSecondary,
                title = { Text("Welcome back", style = MaterialTheme.typography.titleLarge) },
                text = { Text(MessagingEngine.welcomeBackMessage(), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissWelcomeBack() }) {
                        Text(
                            MessagingEngine.WELCOME_BACK_BUTTON_TEXT,
                            style = MaterialTheme.typography.labelLarge,
                            color = RehabPrimary
                        )
                    }
                }
            )
        }

        uiState.pendingMilestoneDays?.let { days ->
            MilestoneCelebrationOverlay(
                days = days,
                message = viewModel.milestoneMessage(days),
                isNewBest = uiState.isNewBestStreak,
                onDismiss = { viewModel.dismissMilestone() }
            )
        }
    }
}

/** Part 6 - full-screen streak-milestone celebration: number, colour dots, message, "Keep going". */
@Composable
private fun MilestoneCelebrationOverlay(
    days: Int,
    message: String,
    isNewBest: Boolean,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    // Slow, breath-paced glow behind the plant - celebratory without being jittery.
    val transition = rememberInfiniteTransition(label = "plant-glow")
    val glow by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "plant-glow-scale"
    )
    val dotColors = listOf(
        Color(0xFFFF9F5A), Color(0xFFFFC94A), Color(0xFF4C8DFF), Color(0xFF4CC38A)
    )

    Surface(modifier = Modifier.fillMaxSize(), color = RehabPrimaryDeep, contentColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryGradient)
                .safeDrawingPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .graphicsLayer { scaleX = glow; scaleY = glow }
                        .background(Color.White.copy(alpha = 0.10f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .graphicsLayer { scaleX = glow; scaleY = glow }
                        .background(Color.White.copy(alpha = 0.14f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    StreakPlant(days = days, size = 58.dp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("$days", style = MaterialTheme.typography.displayLarge)
            Text(
                "days in a row",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                dotColors.forEach { c ->
                    Box(modifier = Modifier.size(12.dp).background(c, CircleShape))
                }
            }
            if (isNewBest) {
                Spacer(Modifier.height(18.dp))
                SoftChip(text = "New personal best", containerColor = Color.White, contentColor = RehabPrimaryDeep)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            SecondaryButton(text = "Keep going", onClick = onDismiss)
            Spacer(Modifier.height(16.dp))
            Text(
                "No pressure to be perfect — just keep coming back.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
