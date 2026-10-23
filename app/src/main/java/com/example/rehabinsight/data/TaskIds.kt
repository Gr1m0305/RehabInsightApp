package com.example.rehabinsight.data

/**
 * Fixed IDs for the 4 backend-only wellbeing categories, so business logic can refer to them
 * by name instead of magic numbers. The Category rows themselves come from the server, whose
 * Test/Tests.py gives them these ids. They match the colour dots used throughout the UI:
 * orange = motivation, yellow = mood/sleep, blue = connection, green = stress/physical.
 */
object CategoryIds {
    const val MOTIVATION = 1
    const val MOOD_SLEEP = 2
    const val CONNECTION = 3
    const val STRESS_PHYSICAL = 4
}

/**
 * The Task ids the checklist-generation and guidance rules refer to (see ChecklistGenerator and
 * GuidanceEngine). The tasks themselves - their titles, timings, articles and colours - are rows
 * in the server's database, which its Test/Tests.py fills with these ids, from "Insight
 * Wellbeing - Setup Questions & Evidence-Based Task Library".
 *
 * The trigger logic (which set is added for which answer) lives in ChecklistGenerator rather
 * than in Task_Rule, since several sets share a colour category yet fire on different,
 * independent questions (e.g. Stress and Physical activity are both green, but are separate
 * triggers).
 */
object TaskIds {

    // Core Five - always included, regardless of answers (mixed colours).
    const val TASK_DRINK_WATER = 1
    const val TASK_EAT_WELL = 2
    const val TASK_MOVE_10_MIN = 3
    const val TASK_SLEEP_ROUTINE_TONIGHT = 4
    const val TASK_CALL_SOMEONE_YOU_LOVE = 5

    // Sleep set (yellow) - Sleep: Poor, or Goal: Sleep.
    const val TASK_WIND_DOWN_ROUTINE = 6
    const val TASK_MORNING_DAYLIGHT = 7
    const val TASK_SCREENS_OFF = 8
    const val TASK_NO_CAFFEINE_AFTER_MIDDAY = 9
    const val TASK_WRITE_TOMORROWS_WORRIES = 10

    // Calm set (green) - Stress: Often, or Goal: Stress.
    const val TASK_SLOW_BREATHING = 11
    const val TASK_10_MIN_WALK_CALM = 12
    const val TASK_GREEN_SPACE = 13
    const val TASK_WRITE_WORRIES_DOWN = 14
    const val TASK_STRETCH_NECK_SHOULDERS = 15

    // Mood set (yellow) - Mood: Low, or Goal: Mood.
    const val TASK_THREE_GRATITUDES = 16
    const val TASK_ENJOYABLE_ACTIVITY = 17
    const val TASK_10_MIN_SUNLIGHT = 18
    const val TASK_NOTE_ONE_SMALL_WIN = 19

    // Connection set (blue) - Connection: Low, or Goal: Connection.
    const val TASK_MESSAGE_SOMEONE = 20
    const val TASK_CALL_NOT_TEXT = 21
    const val TASK_FACE_TO_FACE_CATCHUP = 22
    const val TASK_ACT_OF_KINDNESS = 23

    // Momentum set (orange) - Motivation: Low, or Goal: Energy.
    const val TASK_TWO_MINUTE_TASK = 24
    const val TASK_FIVE_MINUTE_TIDY = 25
    const val TASK_WRITE_TOP_3 = 26
    const val TASK_FINISH_SOMETHING_STARTED = 27

    // Movement set (green) - Activity: Low, or Goal: Physical health.
    const val TASK_WALK_10_MIN = 28
    const val TASK_GENTLE_MOBILITY = 29
    const val TASK_TAKE_THE_STAIRS = 30
    const val TASK_SIT_TO_STANDS = 31
    /** Pain modifier: swapped in for [TASK_TAKE_THE_STAIRS]/[TASK_SIT_TO_STANDS] when Pain = A lot. */
    const val TASK_MOVE_GENTLY_WITH_PAIN = 32

    // Routine set (orange) - Routine: No.
    const val TASK_WAKE_TIME = 33
    const val TASK_PLAN_TOMORROW_NIGHT_BEFORE = 34
    const val TASK_ANCHOR_HABIT = 35

    // Self-compassion re-entry task, added automatically on a missed-day return (section 6/13).
    const val SELF_COMPASSION_TASK_ID = 36

    /** Movement-set tasks swapped out (not shown) when the client's Pain answer is "A lot". */
    val VIGOROUS_MOVEMENT_TASK_IDS = setOf(TASK_TAKE_THE_STAIRS, TASK_SIT_TO_STANDS)

    val CORE_FIVE_TASK_IDS = listOf(
        TASK_DRINK_WATER, TASK_EAT_WELL, TASK_MOVE_10_MIN, TASK_SLEEP_ROUTINE_TONIGHT, TASK_CALL_SOMEONE_YOU_LOVE
    )
    val SLEEP_SET_TASK_IDS = listOf(
        TASK_WIND_DOWN_ROUTINE, TASK_MORNING_DAYLIGHT, TASK_SCREENS_OFF, TASK_NO_CAFFEINE_AFTER_MIDDAY, TASK_WRITE_TOMORROWS_WORRIES
    )
    val CALM_SET_TASK_IDS = listOf(
        TASK_SLOW_BREATHING, TASK_10_MIN_WALK_CALM, TASK_GREEN_SPACE, TASK_WRITE_WORRIES_DOWN, TASK_STRETCH_NECK_SHOULDERS
    )
    val MOOD_SET_TASK_IDS = listOf(TASK_THREE_GRATITUDES, TASK_ENJOYABLE_ACTIVITY, TASK_10_MIN_SUNLIGHT, TASK_NOTE_ONE_SMALL_WIN)
    val CONNECTION_SET_TASK_IDS = listOf(TASK_MESSAGE_SOMEONE, TASK_CALL_NOT_TEXT, TASK_FACE_TO_FACE_CATCHUP, TASK_ACT_OF_KINDNESS)
    val MOMENTUM_SET_TASK_IDS = listOf(TASK_TWO_MINUTE_TASK, TASK_FIVE_MINUTE_TIDY, TASK_WRITE_TOP_3, TASK_FINISH_SOMETHING_STARTED)
    val MOVEMENT_SET_TASK_IDS = listOf(TASK_WALK_10_MIN, TASK_GENTLE_MOBILITY, TASK_TAKE_THE_STAIRS, TASK_SIT_TO_STANDS)
    val ROUTINE_SET_TASK_IDS = listOf(TASK_WAKE_TIME, TASK_PLAN_TOMORROW_NIGHT_BEFORE, TASK_ANCHOR_HABIT)
}
