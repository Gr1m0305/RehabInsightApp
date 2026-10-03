package com.example.rehabinsight.data.network

const val SERVER_BASE_URL: String = "http://10.0.2.2:8000"

/*
 * One address per server call, grouped by database table. Every call sends and receives JSON
 * whose field names are the table's column names. A call that reads or changes one client's
 * rows also carries that client's session_token; a call made from the admin dashboard carries
 * admin_id instead (see Caller).
 *
 *  - ".../list" returns an array of rows. The shared tables are plain GETs; the per-client ones
 *    are POSTs whose body says whose rows are wanted (client_id, or admin_id for all of an
 *    admin's clients).
 *  - ".../create" returns the new row's primary key under its column name, as client/create does.
 *  - ".../update" identifies the row by its primary key and sends the columns to store.
 *
 * On the server today: client/create, client/login and session/validate. The rest are what the
 * app expects; until the server has them the app carries on with the data it holds itself.
 */

// Client, Client_Session
const val CLIENT_SIGNUP_URL: String = "$SERVER_BASE_URL/api/client/create"
const val CLIENT_LOGIN_URL: String = "$SERVER_BASE_URL/api/client/login"
const val CLIENT_LIST_URL: String = "$SERVER_BASE_URL/api/client/list"
const val SESSION_VALIDATE_URL: String = "$SERVER_BASE_URL/api/session/validate"
const val SESSION_LOGOUT_URL: String = "$SERVER_BASE_URL/api/session/logout"

// Client_Streak
const val CLIENT_STREAK_LIST_URL: String = "$SERVER_BASE_URL/api/client_streak/list"
const val CLIENT_STREAK_UPDATE_URL: String = "$SERVER_BASE_URL/api/client_streak/update"

// Client_Task
const val CLIENT_TASK_LIST_URL: String = "$SERVER_BASE_URL/api/client_task/list"
const val CLIENT_TASK_CREATE_URL: String = "$SERVER_BASE_URL/api/client_task/create"
const val CLIENT_TASK_UPDATE_URL: String = "$SERVER_BASE_URL/api/client_task/update"

// Daily_Question
const val DAILY_QUESTION_LIST_URL: String = "$SERVER_BASE_URL/api/daily_question/list"
const val DAILY_QUESTION_CREATE_URL: String = "$SERVER_BASE_URL/api/daily_question/create"

// Daily_Checkin
const val DAILY_CHECKIN_URL: String = "$SERVER_BASE_URL/api/checkin"
const val DAILY_CHECKIN_LIST_URL: String = "$SERVER_BASE_URL/api/checkin/list"

// Response
const val RESPONSE_LIST_URL: String = "$SERVER_BASE_URL/api/response/list"
const val RESPONSE_CREATE_URL: String = "$SERVER_BASE_URL/api/response/create"

// Task, Task_Category, Task_Rule
const val TASK_LIST_URL: String = "$SERVER_BASE_URL/api/task/list"
const val TASK_CREATE_URL: String = "$SERVER_BASE_URL/api/task/create"
const val TASK_UPDATE_URL: String = "$SERVER_BASE_URL/api/task/update"
const val TASK_CATEGORY_LIST_URL: String = "$SERVER_BASE_URL/api/task_category/list"
const val TASK_CATEGORY_CREATE_URL: String = "$SERVER_BASE_URL/api/task_category/create"
const val TASK_CATEGORY_UPDATE_URL: String = "$SERVER_BASE_URL/api/task_category/update"
const val TASK_RULE_LIST_URL: String = "$SERVER_BASE_URL/api/task_rule/list"

// Category, Article, Question_Template, Streak_Milestone, Universal_App_Setting (read-only)
const val CATEGORY_LIST_URL: String = "$SERVER_BASE_URL/api/category/list"
const val ARTICLE_LIST_URL: String = "$SERVER_BASE_URL/api/article/list"
const val QUESTION_TEMPLATE_LIST_URL: String = "$SERVER_BASE_URL/api/question_template/list"
const val STREAK_MILESTONES_URL: String = "$SERVER_BASE_URL/api/milestones"
const val SETTING_LIST_URL: String = "$SERVER_BASE_URL/api/setting/list"
