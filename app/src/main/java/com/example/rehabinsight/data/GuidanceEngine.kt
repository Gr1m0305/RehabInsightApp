package com.example.rehabinsight.data

/**
 * Part 5 - Daily check-in guidance (highlighting, not rebuilding).
 * Never rebuilds the checklist - only highlights 2-4 already-assigned tasks, following the
 * exact mapping table from "Insight Wellbeing":
 *
 *  Check-in says      -> Highlight
 *  Low motivation      -> the single 2-minute task (orange) only - one thing, not four
 *  Pain flare           -> gentle movement within limits (green) + one non-physical task
 *  High stress          -> breathing (green) + a walk (green)
 *  Poor sleep last night -> daylight task now + wind-down routine tonight (yellow)
 *  Low mood             -> one yellow task + the easiest orange task
 *  Low energy           -> the two shortest tasks on their list, whatever the colour
 *  Feeling good         -> their goal-area tasks - build on momentum
 */
object GuidanceEngine {

    /** Maps a FiveLevel answer to an integer "need" score (higher = more support needed). Kept for admin/history use. */
    fun answerValue(level: FiveLevel): Int = when (level) {
        FiveLevel.GREAT -> 1
        FiveLevel.GOOD -> 2
        FiveLevel.OKAY -> 3
        FiveLevel.LOW -> 4
        FiveLevel.STRUGGLING -> 5
    }

    private fun isConcerning(level: FiveLevel?): Boolean = level == FiveLevel.LOW || level == FiveLevel.STRUGGLING
    private fun isGood(level: FiveLevel?): Boolean = level == FiveLevel.GREAT || level == FiveLevel.GOOD

    /**
     * @param checkIn Today's Daily Check-In answers.
     * @param activeTaskIds The client's currently assigned task_ids (their existing checklist).
     * @param tasksById The full Task library, by task_id (used to find the shortest tasks).
     * @param goalAreaTaskIds Tasks belonging to the categories the client chose as goals at setup -
     *   used for the "Feeling good" case, to build on their own momentum.
     */
    fun highlightTaskIds(
        checkIn: CheckInAnswers,
        activeTaskIds: List<Int>,
        tasksById: Map<Int, Task>,
        goalAreaTaskIds: List<Int> = emptyList(),
        minTasks: Int = 2,
        maxTasks: Int = 4
    ): List<Int> {
        if (activeTaskIds.isEmpty()) return emptyList()
        val active = activeTaskIds.toSet()
        fun keep(vararg ids: Int) = ids.filter { it in active }

        // Low motivation is a hard override: one thing, not four.
        if (isConcerning(checkIn.motivation)) {
            val only = keep(SeedData.TASK_TWO_MINUTE_TASK)
            if (only.isNotEmpty()) return only
        }

        val result = LinkedHashSet<Int>()

        if (isConcerning(checkIn.pain)) {
            result += keep(SeedData.TASK_MOVE_GENTLY_WITH_PAIN)
            result += keep(SeedData.TASK_THREE_GRATITUDES, SeedData.TASK_MESSAGE_SOMEONE).take(1)
        }
        if (isConcerning(checkIn.stress)) {
            result += keep(SeedData.TASK_SLOW_BREATHING, SeedData.TASK_10_MIN_WALK_CALM)
        }
        if (isConcerning(checkIn.sleep)) {
            result += keep(SeedData.TASK_MORNING_DAYLIGHT, SeedData.TASK_WIND_DOWN_ROUTINE)
        }
        if (isConcerning(checkIn.mood) && result.size < maxTasks) {
            result += keep(SeedData.TASK_THREE_GRATITUDES).take(1)
            result += keep(SeedData.TASK_TWO_MINUTE_TASK).take(1)
        }
        if (isConcerning(checkIn.energy) && result.size < maxTasks) {
            val shortest = activeTaskIds
                .mapNotNull { tasksById[it] }
                .sortedBy { it.minMinutes ?: Int.MAX_VALUE }
                .map { it.taskId }
            for (id in shortest) {
                if (result.size >= maxTasks) break
                result += id
            }
        }

        val allGood = listOf(checkIn.mood, checkIn.energy, checkIn.stress, checkIn.sleep, checkIn.motivation).all(::isGood)
        if (allGood && result.isEmpty()) {
            result += goalAreaTaskIds.filter { it in active }
        }

        // Gentle fallback so something is always shown, without ever exceeding maxTasks.
        if (result.size < minTasks) {
            for (id in activeTaskIds) {
                if (result.size >= minTasks) break
                result += id
            }
        }
        return result.take(maxTasks).toList()
    }

    fun guidanceIntro(): String =
        "Based on how you're feeling, here are a few helpful starting points"
}
