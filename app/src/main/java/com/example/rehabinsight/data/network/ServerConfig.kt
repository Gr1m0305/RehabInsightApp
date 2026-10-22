package com.example.rehabinsight.data.network

const val SERVER_BASE_URL: String = "http://10.0.2.2:8000"

/*
 * One address per server call (RehabInsightServer, api/). Every call is a POST that sends and
 * receives JSON whose field names are the database's column names.
 *
 * Only client/create and client/login (and the password reset pair) are made before sign-in.
 * A client's calls carry their session_token, from which the server works out whose rows they
 * are. An admin's calls carry admin_email and admin_password instead: the server has no admin
 * session, so it checks those on every request.
 *
 * The server has no calls for the client to add or change their own Task or Client_Task rows,
 * or to read Task_Category, Task_Rule, Question_Template or Universal_App_Setting, so the app
 * keeps those on the device, starting from its built-in library.
 */

// Client, Client_Session
const val CLIENT_SIGNUP_URL: String = "$SERVER_BASE_URL/api/client/create"
const val CLIENT_LOGIN_URL: String = "$SERVER_BASE_URL/api/client/login"
const val SESSION_VALIDATE_URL: String = "$SERVER_BASE_URL/api/client/validate"
const val SESSION_LOGOUT_URL: String = "$SERVER_BASE_URL/api/client/logout"
const val CLIENT_PROFILE_URL: String = "$SERVER_BASE_URL/api/client/profile"
const val CLIENT_CHANGE_PASSWORD_URL: String = "$SERVER_BASE_URL/api/client/change-password"
// Not on the server since it was re-cloned on 2026-10-23; until they're back, a 404 tells the client to ask their clinician.
const val CLIENT_FORGOT_PASSWORD_URL: String = "$SERVER_BASE_URL/api/client/forgot-password"
const val CLIENT_RESET_PASSWORD_URL: String = "$SERVER_BASE_URL/api/client/reset-password"

// Daily_Question, Daily_Checkin, Response, Category
const val DAILY_CHECKIN_COMPLETED_TODAY_URL: String = "$SERVER_BASE_URL/api/daily-checkin/completed-today"
const val DAILY_CHECKIN_QUESTIONS_URL: String = "$SERVER_BASE_URL/api/daily-checkin/questions"
const val DAILY_CHECKIN_QUESTION_ADD_URL: String = "$SERVER_BASE_URL/api/daily-checkin/questions/add"
const val DAILY_CHECKIN_QUESTION_REMOVE_URL: String = "$SERVER_BASE_URL/api/daily-checkin/questions/remove"
const val DAILY_CHECKIN_SUBMIT_URL: String = "$SERVER_BASE_URL/api/daily-checkin/submit"
const val CHECKIN_HISTORY_URL: String = "$SERVER_BASE_URL/api/daily-checkin/checkins/history"
const val ANSWER_HISTORY_URL: String = "$SERVER_BASE_URL/api/daily-checkin/answers/history"
const val CATEGORIES_URL: String = "$SERVER_BASE_URL/api/daily-checkin/categories"

// Client_Task, Task
const val CLIENT_TASKS_URL: String = "$SERVER_BASE_URL/api/daily-checkin/tasks"
const val TASK_LIBRARY_URL: String = "$SERVER_BASE_URL/api/daily-checkin/tasks/library"
const val CLIENT_TASK_COMPLETE_URL: String = "$SERVER_BASE_URL/api/daily-checkin/tasks/complete"

// Client_Streak, Streak_Milestone
const val CLIENT_STREAK_URL: String = "$SERVER_BASE_URL/api/daily-checkin/streak"
const val STREAK_MILESTONES_URL: String = "$SERVER_BASE_URL/api/daily-checkin/streak/milestones"

// Article
const val ARTICLES_URL: String = "$SERVER_BASE_URL/api/articles/get"

// Admin dashboard: the admin's clients and their progress, the Task library, and assigning tasks
const val ADMIN_CLIENTS_URL: String = "$SERVER_BASE_URL/api/admin/clients"
const val ADMIN_CLIENT_TASKS_URL: String = "$SERVER_BASE_URL/api/admin/tasks/client"
const val ADMIN_CLIENT_REPORT_URL: String = "$SERVER_BASE_URL/api/admin/reports/client"
const val ADMIN_TASKS_URL: String = "$SERVER_BASE_URL/api/admin/tasks"
const val ADMIN_TASK_CREATE_URL: String = "$SERVER_BASE_URL/api/admin/tasks/create"
const val ADMIN_TASK_UPDATE_URL: String = "$SERVER_BASE_URL/api/admin/tasks/update"
const val ADMIN_TASK_ASSIGN_URL: String = "$SERVER_BASE_URL/api/admin/tasks/assign"
