package com.example.rehabinsight.ui.navigation

/** Route constants mirroring the product flow chart. */
object Routes {
    const val LOGIN = "login"
    const val SETUP_QUESTIONNAIRE = "setup_questionnaire"
    const val CHECKLIST_GENERATION = "checklist_generation"
    const val REVIEW_CHECKLIST = "review_checklist"
    const val DAILY_CHECK_IN = "daily_check_in"
    const val MAIN = "main" // hosts bottom-nav (Home / Tasks / Library / Profile)

    const val ADMIN_LOGIN = "admin_login"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    // Bottom-nav tabs (nested inside MAIN)
    const val HOME = "home"
    const val TASKS = "tasks"
    const val LIBRARY = "library"
    const val PROFILE = "profile"
}
