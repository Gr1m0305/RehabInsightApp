package com.example.rehabinsight.data

/**
 * Generates a client's ongoing personalised checklist directly from their typed [SetupAnswers],
 * implementing Part 2 of "Insight Wellbeing - Setup Questions & Evidence-Based Task Library"
 * exactly:
 *
 *  - Everyone always gets the Core Five, regardless of answers.
 *  - Each flagged answer (or matching self-chosen goal) adds its whole task set.
 *  - If a user flags many areas, the initial checklist is capped at 10-12 tasks, prioritised by
 *    their stated goals (Q10) - anything left out can still be added later via customisation.
 *  - Energy: Low is a pacing modifier (handled by [GuidanceEngine], not a task set).
 *  - Pain: A lot swaps the two vigorous Movement-set tasks for one gentle alternative.
 *
 * This intentionally does not reuse the generic Task_Rule/category-score machinery: several sets
 * share a colour category (e.g. Calm and Movement are both green) but fire on independent
 * questions, which a single per-category sum can't distinguish.
 */
object ChecklistGenerator {

    private const val CHECKLIST_CAP = 12

    /** One optional task-set trigger, in Part-2 priority order. */
    private data class Candidate(val taskIds: List<Int>, val linkedGoal: Goal?)

    /**
     * Returns the distinct, ordered task_ids that make up a client's initial ongoing checklist,
     * capped at [CHECKLIST_CAP] and prioritised so goal-linked sets (Q10) are kept first.
     */
    fun generateTaskIds(answers: SetupAnswers): List<Int> {
        val result = LinkedHashSet<Int>()
        result += SeedData.CORE_FIVE_TASK_IDS

        val candidates = mutableListOf<Candidate>()
        if (answers.sleep == SleepRating.POOR) candidates += Candidate(SeedData.SLEEP_SET_TASK_IDS, Goal.SLEEP)
        if (answers.stress == StressFrequency.OFTEN) candidates += Candidate(SeedData.CALM_SET_TASK_IDS, Goal.STRESS)
        if (answers.mood == MoodRating.LOW) candidates += Candidate(SeedData.MOOD_SET_TASK_IDS, Goal.MOOD)
        if (answers.connection == LevelRating.LOW) candidates += Candidate(SeedData.CONNECTION_SET_TASK_IDS, Goal.CONNECTION)
        if (answers.motivation == LevelRating.LOW) candidates += Candidate(SeedData.MOMENTUM_SET_TASK_IDS, Goal.ENERGY)
        if (answers.activity == LevelRating.LOW) candidates += Candidate(SeedData.MOVEMENT_SET_TASK_IDS, Goal.PHYSICAL_HEALTH)
        if (answers.routine == RoutineRating.NO) candidates += Candidate(SeedData.ROUTINE_SET_TASK_IDS, null)

        // A self-chosen goal also switches on its set, even if the matching answer wasn't itself
        // flagged (e.g. someone picks "Sleep" as a goal despite rating their sleep "Moderate").
        fun addGoalSet(goal: Goal, taskIds: List<Int>) {
            if (goal in answers.goals && candidates.none { it.linkedGoal == goal }) {
                candidates += Candidate(taskIds, goal)
            }
        }
        addGoalSet(Goal.SLEEP, SeedData.SLEEP_SET_TASK_IDS)
        addGoalSet(Goal.MOOD, SeedData.MOOD_SET_TASK_IDS)
        addGoalSet(Goal.STRESS, SeedData.CALM_SET_TASK_IDS)
        addGoalSet(Goal.ENERGY, SeedData.MOMENTUM_SET_TASK_IDS)
        addGoalSet(Goal.CONNECTION, SeedData.CONNECTION_SET_TASK_IDS)
        addGoalSet(Goal.PHYSICAL_HEALTH, SeedData.MOVEMENT_SET_TASK_IDS)

        // Prioritise: goal-linked sets first (in the order the user chose their up-to-two goals),
        // then everything else that was flagged by a plain survey answer.
        val goalOrder = Goal.entries.filter { it in answers.goals }
        val ordered = candidates.sortedBy { candidate ->
            val goal = candidate.linkedGoal
            if (goal != null && goal in answers.goals) goalOrder.indexOf(goal) else candidates.size + 1
        }

        for (candidate in ordered) {
            if (result.size + candidate.taskIds.size > CHECKLIST_CAP) continue
            result += candidate.taskIds
        }

        applyPainModifier(result, answers.pain)
        return result.toList()
    }

    /** Pain: A lot - drop vigorous Movement-set tasks and add the gentle alternative instead. */
    private fun applyPainModifier(taskIds: LinkedHashSet<Int>, pain: PainLevel) {
        if (pain != PainLevel.A_LOT) return
        SeedData.VIGOROUS_MOVEMENT_TASK_IDS.forEach { taskIds.remove(it) }
        taskIds += SeedData.TASK_MOVE_GENTLY_WITH_PAIN
    }
}
