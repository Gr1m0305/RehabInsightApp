package com.example.rehabinsight.data

import java.time.LocalDate

/**
 * Transient, UI-only form results. These are NOT database tables - they're just a convenient
 * shape for passing a completed form from a Composable to the ViewModel, which then converts
 * them into real Daily_Question / Daily_Checkin / Response rows (see AppViewModel).
 */

/** Answers captured once, during the Initial Setup Questionnaire (the 10 questions, Part 1). */
data class SetupAnswers(
    val sleep: SleepRating = SleepRating.MODERATE,
    val stress: StressFrequency = StressFrequency.SOMETIMES,
    val mood: MoodRating = MoodRating.MODERATE,
    val connection: LevelRating = LevelRating.MODERATE,
    val motivation: LevelRating = LevelRating.MODERATE,
    val activity: LevelRating = LevelRating.MODERATE,
    val routine: RoutineRating = RoutineRating.SOMEWHAT,
    val energy: LevelRating = LevelRating.MODERATE,
    val pain: PainLevel = PainLevel.NONE,
    val goals: Set<Goal> = emptySet()
)

/** A quick daily check-in, completed in under 15 seconds. */
data class CheckInAnswers(
    val date: LocalDate,
    val mood: FiveLevel,
    val energy: FiveLevel,
    val stress: FiveLevel,
    val sleep: FiveLevel,
    val motivation: FiveLevel,
    val pain: FiveLevel? = null,
    val skipped: Boolean = false
)
