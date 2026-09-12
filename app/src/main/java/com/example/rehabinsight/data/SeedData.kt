package com.example.rehabinsight.data

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Fixed IDs for the 4 backend-only wellbeing categories, so business logic can refer to them
 * by name instead of magic numbers. These map 1:1 to the seeded Category rows below, and to the
 * colour dots used throughout the UI: orange = motivation, yellow = mood/sleep, blue = connection,
 * green = stress/physical.
 */
object CategoryIds {
    const val MOTIVATION = 1
    const val MOOD_SLEEP = 2
    const val CONNECTION = 3
    const val STRESS_PHYSICAL = 4
}

/**
 * All of the initial rows the app boots with, laid out exactly as tables in the ERD:
 * Category, Article, Task, Task_Category, Task_Rule, Question_Template, streak_milestone,
 * Universal_App_Setting, Admin - plus a handful of demo Client rows so the Admin Dashboard has
 * real, schema-shaped data to show from the very first run.
 *
 * The task library, setup questions, checklist-generation triggers, education articles and
 * streak milestones below are taken directly from "Insight Wellbeing - Setup Questions &
 * Evidence-Based Task Library": every task is universal (no equipment/cost/ability assumptions)
 * and grounded in the cited peer-reviewed literature.
 */
object SeedData {

    // ---------------------------------------------------------------------------------------
    // Category
    // ---------------------------------------------------------------------------------------
    val categories: List<Category> = listOf(
        Category(CategoryIds.MOTIVATION, "Motivation", "#FF9F5A"),
        Category(CategoryIds.MOOD_SLEEP, "Mood & Sleep", "#FFC94A"),
        Category(CategoryIds.CONNECTION, "Connection", "#4C8DFF"),
        Category(CategoryIds.STRESS_PHYSICAL, "Stress & Physical", "#4CC38A")
    )

    // ---------------------------------------------------------------------------------------
    // Article (Education Library) - Part 4: six short, practical articles that tasks link to.
    // ---------------------------------------------------------------------------------------
    const val ART_SLEEP_BASICS = 1
    const val ART_STRESS_BREATHING = 2
    const val ART_MOVEMENT_AND_MOOD = 3
    const val ART_CONNECTION = 4
    const val ART_BUILDING_MOMENTUM = 5
    const val ART_MOVING_WITH_PAIN = 6

    val articles: List<Article> = listOf(
        Article(
            articleId = ART_SLEEP_BASICS,
            title = "Sleep Basics",
            bodyText = listOf(
                "Sleep is one of the most powerful levers you have for mood, focus and recovery - and the good news is that " +
                    "you don't need to fix everything at once. CBT-I, the gold-standard treatment for insomnia, comes down to " +
                    "five simple levers: a consistent wind-down routine, a fixed wake time, morning light, cutting caffeine " +
                    "after midday, and dimming screens before bed.",
                "A wind-down routine - the same few calming steps each night - teaches your body that bed means sleep, not " +
                    "scrolling or worrying. This 'stimulus control' is one of the most consistently effective parts of CBT-I " +
                    "(Trauer et al., 2015, Annals of Internal Medicine).",
                "Getting outside into daylight within an hour of waking helps advance your body clock, so you feel sleepier " +
                    "at the right time and more alert during the day (Blume et al., 2019). Caffeine lingers longer than most " +
                    "people expect - even a cup 6 hours before bed can measurably disrupt sleep (Drake et al., 2013) - so an " +
                    "after-midday cut-off is a small change with an outsized effect.",
                "Screens in the hour before bed are a double hit: the light delays melatonin, and the content (feeds, news, " +
                    "messages) keeps your mind alert exactly when it should be winding down (Chang et al., 2015, PNAS). If your " +
                    "mind is busy with tomorrow's to-do list, writing it down on paper before bed has been shown to shorten the " +
                    "time it takes to fall asleep (Scullin et al., 2018) - you're not forgetting anything, you're just parking it.",
                "You don't need a perfect night. Pick one lever tonight, and let the rest follow.",
                "Related tasks: Follow a night wind-down routine · Get outside into daylight within an hour of waking · " +
                    "Screens off 30 minutes before bed · No caffeine after midday · Write tomorrow's worries or to-dos on paper before bed."
            ).joinToString("\n\n"),
            sortOrder = 1
        ),
        Article(
            articleId = ART_STRESS_BREATHING,
            title = "Stress & Breathing",
            bodyText = listOf(
                "Stress itself isn't the problem - it's chronic, unmanaged stress that wears the body down. What matters most " +
                    "isn't your objective workload but how overwhelmed you feel by it (Cohen et al., 1983); the good news is that " +
                    "feeling is something you can influence directly, in minutes, with your breath.",
                "A longer out-breath than in-breath activates your parasympathetic nervous system - your body's built-in brake " +
                    "pedal - lowering heart rate and easing the physical sensations of anxiety almost immediately (Ma et al., " +
                    "2017, Frontiers in Psychology; Zaccaro et al., 2018). Try five minutes: breathe in for a count of four, out " +
                    "for a count of six, and repeat.",
                "Movement helps too. A single 10-minute walk measurably reduces state anxiety and stress reactivity (Ensari et " +
                    "al., 2015), and spending just 5-10 minutes in green space lowers cortisol and quiets rumination - roughly " +
                    "120 minutes a week is linked to meaningfully better wellbeing (White et al., 2019; Bratman et al., 2015).",
                "When your mind is looping on worries, put them on paper and mark what's actually within your control. " +
                    "Expressive writing like this reliably reduces intrusive thoughts (Pennebaker & Beall, 1986; Baikie & " +
                    "Wilhelm, 2005). And if tension has settled in your shoulders and neck, five minutes of stretching eases " +
                    "both the physical tightness and the anxious feeling that comes with it.",
                "None of this requires stress to disappear - just a few minutes to bring your body back down.",
                "Related tasks: Slow breathing (5 minutes, longer out-breath) · Take a 10-minute walk, ideally outdoors · " +
                    "Spend 5-10 minutes in green space · Write your worries down and mark what you can control · " +
                    "Stretch your neck, shoulders and back for 5 minutes."
            ).joinToString("\n\n"),
            sortOrder = 2
        ),
        Article(
            articleId = ART_MOVEMENT_AND_MOOD,
            title = "Movement & Mood",
            bodyText = listOf(
                "Physical activity is one of the most effective single interventions we have for depression and anxiety - in " +
                    "large umbrella reviews its effects are comparable to psychotherapy or medication (Singh et al., 2023, " +
                    "British Journal of Sports Medicine; Schuch et al., 2016). And crucially, you don't need to hit any " +
                    "particular threshold to benefit.",
                "The '10-minute rule' exists because depression risk drops steeply from the very first doses of movement - the " +
                    "biggest gains come from going from nothing to something, not from something to a lot (Pearce et al., 2022, " +
                    "JAMA Psychiatry). Walking just 2.5 hours a week is associated with around 25% lower depression risk, and " +
                    "the benefits start well below that (Pearce et al., 2022).",
                "Movement doesn't have to look like exercise. Gentle stretching or mobility work reduces stiffness and supports " +
                    "pain self-management (Geneen et al., 2017, Cochrane); even incidental, everyday bouts count. Taking the " +
                    "stairs or parking further away creates short, vigorous bursts of 1-2 minutes that are linked to major " +
                    "reductions in mortality risk (Stamatakis et al., 2022, Nature Medicine - the 'VILPA' effect). Functional " +
                    "strength matters too: something as simple as 8-10 sit-to-stands from a chair trains the muscles that " +
                    "underpin everyday independence (Bohannon, 2012).",
                "If pain or a physical condition limits you, none of this is off the table - it just needs to be graded and " +
                    "paced. See Moving With Pain for how to keep moving safely within your limits.",
                "Related tasks: Walk for 10 minutes · Stretch or do gentle mobility for 5 minutes · " +
                    "Take the stairs / park further away today · Do 8-10 sit-to-stands from a chair."
            ).joinToString("\n\n"),
            sortOrder = 3
        ),
        Article(
            articleId = ART_CONNECTION,
            title = "Connection",
            bodyText = listOf(
                "Social connection is one of the strongest protective factors for health ever measured - in some analyses, " +
                    "comparable in size to quitting smoking (Holt-Lunstad et al., 2010, PLoS Medicine). Loneliness, on the other " +
                    "hand, independently predicts the onset of depression (Cacioppo et al., 2010). The encouraging part: the " +
                    "'dose' needed is smaller than most people assume.",
                "Even minimal contact with 'weak ties' - a short message to someone you don't talk to often - measurably lifts " +
                    "mood and belonging (Sandstrom & Dunn, 2014). You don't need a big gesture or a long conversation to matter " +
                    "to someone, or for it to matter to you.",
                "When you can, choose voice over text. Calling someone creates a stronger sense of connection and lowers " +
                    "loneliness compared with texting, even though people often predict texting will feel just as good (Kumar " +
                    "& Epley, 2021). And in-person contact carries the strongest protective link against depression of all - " +
                    "one face-to-face catch-up a week is a meaningful target, not a minimum (Teo et al., 2015).",
                "Kindness rounds this out: small prosocial acts - a favour, a compliment, a helping hand - boost the giver's " +
                    "wellbeing just as much as the receiver's (Curry et al., 2018; Lyubomirsky et al., 2005). You don't need to " +
                    "feel 'ready' to reach out. Connection often comes before motivation, not after.",
                "Related tasks: Message one person you care about · Call someone rather than text, once this week · " +
                    "Have one face-to-face catch-up this week · Do one small act of kindness."
            ).joinToString("\n\n"),
            sortOrder = 4
        ),
        Article(
            articleId = ART_BUILDING_MOMENTUM,
            title = "Building Momentum",
            bodyText = listOf(
                "Low motivation is not a character flaw and it isn't solved by bigger goals - it's solved by smaller first " +
                    "steps. Behavioural activation, one of the most evidence-backed approaches to low mood and low motivation, " +
                    "works by rebuilding the effort-reward loop one small, completed action at a time (Ekers et al., 2014).",
                "That's the logic behind the 2-minute rule: doing one tiny task right now bypasses avoidance entirely, because " +
                    "there's almost nothing to talk yourself out of. Starting is the lever, not motivation - motivation tends to " +
                    "follow action, not precede it (Gollwitzer, 1999).",
                "A 5-minute tidy of one small area works the same way. Cluttered environments quietly elevate cortisol and low " +
                    "mood, and a small, visible win restores a sense of control fast (Saxbe & Repetti, 2010). Writing down " +
                    "tomorrow's top 3 priorities tonight roughly doubles follow-through, because specific written intentions " +
                    "are far more likely to be acted on than vague ones (Gollwitzer & Sheeran, 2006, a meta-analysis of 94 " +
                    "studies).",
                "And when something's been left unfinished, finishing it matters more than it seems. Open loops carry a mental " +
                    "load (the 'Zeigarnik effect'), and completing them - even something small - builds self-efficacy, the " +
                    "belief that you can do the next thing too (Bandura, 1977).",
                "None of this asks you to feel motivated first. It just asks for one small, finishable thing.",
                "Related tasks: Do one 2-minute task right now · Do a 5-minute tidy of one small area · " +
                    "Write tomorrow's top 3 priorities tonight · Finish one thing you already started."
            ).joinToString("\n\n"),
            sortOrder = 5
        ),
        Article(
            articleId = ART_MOVING_WITH_PAIN,
            title = "Moving With Pain",
            bodyText = listOf(
                "If pain or a physical condition limits you, movement is still worth doing - it just needs to be graded, paced, " +
                    "and matched to what your body can handle today. The key distinction is 'hurt' versus 'harm': some " +
                    "discomfort during graded movement is common and not a sign of damage.",
                "Education plus graded activity - small, planned doses with built-in rest - has been shown to reduce " +
                    "fear-avoidance and disability more effectively than rest alone (Booth et al., 2017, Musculoskeletal Care). " +
                    "Understanding why this works can itself reduce the threat your body assigns to movement, which is part of " +
                    "why pain science education is now a core part of modern pain management (Moseley & Butler, 2015).",
                "In practice, that means swapping vigorous or high-impact items for something gentler within your limits - a " +
                    "slow stretch instead of stairs, a short easy walk instead of a fast one - rather than skipping movement " +
                    "altogether. Pacing (small doses with rest, rather than pushing through and then crashing) also protects " +
                    "consistency, which matters more than intensity over time (Nijs et al., 2009).",
                "There is no shame in modifying a task. A gentler version done today is worth more than a harder version " +
                    "skipped altogether.",
                "Related tasks: Move gently within your limits (replaces vigorous movement tasks when pain is high)."
            ).joinToString("\n\n"),
            sortOrder = 6
        )
    )

    // ---------------------------------------------------------------------------------------
    // Task + Task_Category + Task_Rule (Part 3 - the full evidence-based task library).
    // Every task belongs to exactly one backend colour category via Task_Category. Task_Rule is
    // kept populated for ERD completeness/admin visibility, but the actual checklist-generation
    // trigger logic (Part 2 - which trigger adds which task SET) lives in ChecklistGenerator,
    // since several sets share a colour category yet fire on different, independent questions
    // (e.g. Stress and Physical activity are both green, but are separate triggers).
    // ---------------------------------------------------------------------------------------

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

    val tasks: List<Task> = listOf(
        // --- Core Five ---
        Task(TASK_DRINK_WATER, title = "Drink 8 cups of water across the day", timeLabel = "All day", minMinutes = 0,
            justification = "Even mild dehydration measurably worsens mood, fatigue and concentration (Armstrong et al., 2012, Journal of Nutrition)."),
        Task(TASK_EAT_WELL, title = "Eat well today — two to three proper meals", timeLabel = "All day", minMinutes = 0,
            justification = "Regular meals stabilise blood glucose and mood; diet quality is linked to depression risk (Firth et al., 2019, Psychosomatic Medicine)."),
        Task(TASK_MOVE_10_MIN, title = "Do at least 10 minutes of exercise or movement", timeLabel = "10+ min", minMinutes = 10,
            justification = "Any dose of activity beats none; depression risk drops steeply from the very first doses of movement (Pearce et al., 2022, JAMA Psychiatry; Singh et al., 2023, BJSM)."),
        Task(TASK_SLEEP_ROUTINE_TONIGHT, ART_SLEEP_BASICS, "Follow your sleep routine tonight", timeLabel = "Evening", minMinutes = 0,
            justification = "Consistent sleep-wake routine is the strongest behavioural anchor for sleep quality and mood, and the core of CBT-I (Lyall et al., 2018, Lancet Psychiatry; Trauer et al., 2015)."),
        Task(TASK_CALL_SOMEONE_YOU_LOVE, ART_CONNECTION, "Call someone you love — or connect with at least one person", timeLabel = "2-15 min", minMinutes = 2, maxMinutes = 15,
            justification = "Social connection is a leading protective factor against depression and early mortality; voice contact beats text for closeness (Holt-Lunstad et al., 2010, PLoS Medicine; Kumar & Epley, 2021)."),

        // --- Sleep set (yellow) ---
        Task(TASK_WIND_DOWN_ROUTINE, ART_SLEEP_BASICS, "Follow a night wind-down routine — read the Sleep Basics article first if you haven't",
            timeLabel = "20-30 min", minMinutes = 20, maxMinutes = 30,
            justification = "Stimulus-control and wind-down routines are core components of CBT-I, the gold-standard insomnia treatment (Trauer et al., 2015, Annals of Internal Medicine)."),
        Task(TASK_MORNING_DAYLIGHT, ART_SLEEP_BASICS, "Get outside into daylight within an hour of waking", timeLabel = "5-10 min", minMinutes = 5, maxMinutes = 10,
            justification = "Morning light advances the body clock and improves sleep and mood (Blume et al., 2019, Somnologie review)."),
        Task(TASK_SCREENS_OFF, ART_SLEEP_BASICS, "Screens off (or night mode + no feeds) 30 minutes before bed", timeLabel = "30 min", minMinutes = 30,
            justification = "Evening light and arousal delay melatonin and sleep onset (Chang et al., 2015, PNAS)."),
        Task(TASK_NO_CAFFEINE_AFTER_MIDDAY, ART_SLEEP_BASICS, "No caffeine after midday", timeLabel = null, minMinutes = 0,
            justification = "Caffeine taken even 6 hours before bed measurably disrupts sleep (Drake et al., 2013, Journal of Clinical Sleep Medicine)."),
        Task(TASK_WRITE_TOMORROWS_WORRIES, ART_SLEEP_BASICS, "Write tomorrow's worries or to-dos on paper before bed", timeLabel = "5 min", minMinutes = 5,
            justification = "A pre-sleep to-do list shortens time to fall asleep (Scullin et al., 2018, Journal of Experimental Psychology)."),

        // --- Calm set (green) ---
        Task(TASK_SLOW_BREATHING, ART_STRESS_BREATHING, "Slow breathing, 5 minutes (longer out-breath) — read the Stress & Breathing article first if you haven't",
            timeLabel = "5 min", minMinutes = 5,
            justification = "Slow-paced breathing activates the parasympathetic system and reduces anxiety and blood pressure (Ma et al., 2017, Frontiers in Psychology; Zaccaro et al., 2018)."),
        Task(TASK_10_MIN_WALK_CALM, ART_STRESS_BREATHING, "Take a 10-minute walk, ideally outdoors", timeLabel = "10 min", minMinutes = 10,
            justification = "A single bout of light exercise reduces state anxiety and stress reactivity (Ensari et al., 2015, Depression & Anxiety meta-analysis)."),
        Task(TASK_GREEN_SPACE, ART_STRESS_BREATHING, "Spend 5-10 minutes in green space", timeLabel = "5-10 min", minMinutes = 5, maxMinutes = 10,
            justification = "Nature exposure lowers cortisol and rumination; ~120 min/week associates with good health and wellbeing (White et al., 2019, Scientific Reports; Bratman et al., 2015, PNAS)."),
        Task(TASK_WRITE_WORRIES_DOWN, ART_STRESS_BREATHING, "Write your worries down and mark what you can control", timeLabel = "5-10 min", minMinutes = 5, maxMinutes = 10,
            justification = "Expressive writing reduces intrusive thoughts and improves wellbeing (Pennebaker & Beall, 1986; Baikie & Wilhelm, 2005)."),
        Task(TASK_STRETCH_NECK_SHOULDERS, ART_STRESS_BREATHING, "Stretch your neck, shoulders and back for 5 minutes", timeLabel = "5 min", minMinutes = 5,
            justification = "Stretching acutely reduces muscle tension and state anxiety (Montero-Marin et al., 2013; general exercise-anxiety literature)."),

        // --- Mood set (yellow) ---
        Task(TASK_THREE_GRATITUDES, title = "Write three things you're grateful for", timeLabel = "3 min", minMinutes = 3,
            justification = "Gratitude journaling reliably improves mood and life satisfaction over weeks (Emmons & McCullough, 2003, JPSP; Davis et al., 2016 meta-analysis)."),
        Task(TASK_ENJOYABLE_ACTIVITY, title = "Do one activity you enjoy — on purpose, scheduled", timeLabel = "15-30 min", minMinutes = 15, maxMinutes = 30,
            justification = "Deliberately scheduling rewarding activity is the active ingredient of behavioural activation, as effective as CBT for depression (Richards et al., 2016, The Lancet COBRA trial)."),
        Task(TASK_10_MIN_SUNLIGHT, title = "Get 10 minutes of sunlight on your face and arms", timeLabel = "10 min", minMinutes = 10,
            justification = "Light exposure supports serotonin function and circadian mood regulation; bright light treats both seasonal and non-seasonal depression (Golden et al., 2005, American Journal of Psychiatry)."),
        Task(TASK_NOTE_ONE_SMALL_WIN, title = "Note one small win from today", timeLabel = "2 min", minMinutes = 2,
            justification = "Recording progress builds perceived competence — the strongest daily driver of positive inner work life (Amabile & Kramer, 2011, HBR 'progress principle'; self-efficacy, Bandura)."),

        // --- Connection set (blue) ---
        Task(TASK_MESSAGE_SOMEONE, title = "Message one person you care about", timeLabel = "2 min", minMinutes = 2,
            justification = "Even minimal social contact ('weak ties') measurably lifts mood and belonging (Sandstrom & Dunn, 2014, PSPB)."),
        Task(TASK_CALL_NOT_TEXT, title = "Call someone rather than text, once this week", timeLabel = "10 min", minMinutes = 10,
            justification = "Voice contact creates stronger connection and lowers loneliness vs text (Kumar & Epley, 2021, JEP: General)."),
        Task(TASK_FACE_TO_FACE_CATCHUP, title = "Have one face-to-face catch-up this week", timeLabel = "30+ min", minMinutes = 30,
            justification = "In-person contact shows the strongest protective link against depression in older and general populations (Teo et al., 2015, JAGS)."),
        Task(TASK_ACT_OF_KINDNESS, ART_CONNECTION, "Do one small act of kindness — read the Connection article first if you haven't", timeLabel = "5 min", minMinutes = 5,
            justification = "Prosocial acts boost the giver's wellbeing as much as the receiver's (Curry et al., 2018, JESP meta-analysis; Lyubomirsky et al., 2005)."),

        // --- Momentum set (orange) ---
        Task(TASK_TWO_MINUTE_TASK, title = "Do one 2-minute task right now", timeLabel = "2 min", minMinutes = 2,
            justification = "Tiny first steps bypass avoidance; starting is the behavioural-activation lever that restores the effort-reward loop (Ekers et al., 2014; implementation intentions, Gollwitzer, 1999)."),
        Task(TASK_FIVE_MINUTE_TIDY, title = "Do a 5-minute tidy of one small area", timeLabel = "5 min", minMinutes = 5,
            justification = "Cluttered environments elevate cortisol and low mood; a small visible win restores a sense of control (Saxbe & Repetti, 2010, PSPB)."),
        Task(TASK_WRITE_TOP_3, title = "Write tomorrow's top 3 priorities tonight", timeLabel = "3 min", minMinutes = 3,
            justification = "Specific, written intentions roughly double follow-through on goals (Gollwitzer & Sheeran, 2006, meta-analysis of 94 studies)."),
        Task(TASK_FINISH_SOMETHING_STARTED, ART_BUILDING_MOMENTUM, "Finish one thing you already started — read the Building Momentum article first if you haven't",
            timeLabel = "10-20 min", minMinutes = 10, maxMinutes = 20,
            justification = "Completion releases the mental load of open loops (Zeigarnik effect) and builds self-efficacy through mastery (Bandura, 1977)."),

        // --- Movement set (green) ---
        Task(TASK_WALK_10_MIN, ART_MOVEMENT_AND_MOOD, "Walk for 10 minutes", timeLabel = "10 min", minMinutes = 10,
            justification = "Walking just 2.5 hrs/week associates with ~25% lower depression risk; benefits start well below guideline doses (Pearce et al., 2022, JAMA Psychiatry)."),
        Task(TASK_GENTLE_MOBILITY, ART_MOVEMENT_AND_MOOD, "Stretch or do gentle mobility for 5 minutes", timeLabel = "5 min", minMinutes = 5,
            justification = "Gentle movement reduces stiffness and supports pain self-management; movement variety aids adherence (Geneen et al., 2017, Cochrane)."),
        Task(TASK_TAKE_THE_STAIRS, ART_MOVEMENT_AND_MOOD, "Take the stairs / park further away today", timeLabel = "2 min", minMinutes = 2, isVigorous = true,
            justification = "Incidental vigorous bouts of 1-2 minutes associate with major reductions in mortality risk (Stamatakis et al., 2022, Nature Medicine, VILPA)."),
        Task(TASK_SIT_TO_STANDS, ART_MOVEMENT_AND_MOOD, "Do 8-10 sit-to-stands from a chair", timeLabel = "3 min", minMinutes = 3, isVigorous = true,
            justification = "Functional leg strength underpins independence and is trainable at any age (Bohannon, 2012; sarcopenia prevention literature)."),
        Task(TASK_MOVE_GENTLY_WITH_PAIN, ART_MOVING_WITH_PAIN, "Move gently within your limits — read the Moving With Pain article first if you haven't",
            timeLabel = "5-10 min", minMinutes = 5, maxMinutes = 10,
            justification = "Graded, paced activity with pain education reduces fear-avoidance and disability better than rest (Booth et al., 2017; Moseley & Butler, 2015)."),

        // --- Routine set (orange) ---
        Task(TASK_WAKE_TIME, title = "Get up at your planned time (within 30 minutes)", timeLabel = "Morning", minMinutes = 0,
            justification = "Rhythm regularity is protectively associated with mood and wellbeing at population scale (Lyall et al., 2018, Lancet Psychiatry)."),
        Task(TASK_PLAN_TOMORROW_NIGHT_BEFORE, title = "Plan tomorrow the night before (clothes, tasks, first step)", timeLabel = "5 min", minMinutes = 5,
            justification = "Reducing morning friction and decisions increases completion of intended behaviour (implementation intentions; Gollwitzer & Sheeran, 2006)."),
        Task(TASK_ANCHOR_HABIT, title = "Anchor one new habit to an existing meal or routine", timeLabel = "1 min", minMinutes = 1,
            justification = "Habit stacking onto stable cues is how behaviours become automatic (Lally et al., 2010, EJSP; Wood & Neal, 2007)."),

        // --- Self-compassion re-entry task ---
        Task(SELF_COMPASSION_TASK_ID, title = "Write one sentence of self-compassion, as if to a friend", timeLabel = "3 min", minMinutes = 3,
            justification = "A gentle re-entry point after missing a day — no catching up required.")
    )

    val taskCategories: List<TaskCategory> = buildList {
        // Core Five - mixed colours.
        add(TaskCategory(CategoryIds.STRESS_PHYSICAL, TASK_DRINK_WATER))
        add(TaskCategory(CategoryIds.STRESS_PHYSICAL, TASK_EAT_WELL))
        add(TaskCategory(CategoryIds.STRESS_PHYSICAL, TASK_MOVE_10_MIN))
        add(TaskCategory(CategoryIds.MOOD_SLEEP, TASK_SLEEP_ROUTINE_TONIGHT))
        add(TaskCategory(CategoryIds.CONNECTION, TASK_CALL_SOMEONE_YOU_LOVE))

        SLEEP_SET_TASK_IDS.forEach { add(TaskCategory(CategoryIds.MOOD_SLEEP, it)) }
        CALM_SET_TASK_IDS.forEach { add(TaskCategory(CategoryIds.STRESS_PHYSICAL, it)) }
        MOOD_SET_TASK_IDS.forEach { add(TaskCategory(CategoryIds.MOOD_SLEEP, it)) }
        CONNECTION_SET_TASK_IDS.forEach { add(TaskCategory(CategoryIds.CONNECTION, it)) }
        MOMENTUM_SET_TASK_IDS.forEach { add(TaskCategory(CategoryIds.MOTIVATION, it)) }
        MOVEMENT_SET_TASK_IDS.forEach { add(TaskCategory(CategoryIds.STRESS_PHYSICAL, it)) }
        add(TaskCategory(CategoryIds.STRESS_PHYSICAL, TASK_MOVE_GENTLY_WITH_PAIN))
        ROUTINE_SET_TASK_IDS.forEach { add(TaskCategory(CategoryIds.MOTIVATION, it)) }

        add(TaskCategory(CategoryIds.MOOD_SLEEP, SELF_COMPASSION_TASK_ID))
    }

    /**
     * Task_Rule rows, kept for ERD completeness and Admin visibility (every task always has a
     * rule referencing its colour category at threshold 0). The real set-by-set trigger logic
     * that decides which tasks make it onto a given client's checklist lives in
     * [ChecklistGenerator], driven directly by their typed [SetupAnswers] (Part 2 of the guide).
     */
    val taskRules: List<TaskRule> = tasks.mapIndexed { index, task ->
        val categoryId = taskCategories.find { it.taskId == task.taskId }?.categoryId ?: CategoryIds.MOOD_SLEEP
        TaskRule(ruleId = index + 1, categoryId = categoryId, taskId = task.taskId, operator = RuleOperator.GTE, thresholdValue = 0)
    }

    // ---------------------------------------------------------------------------------------
    // Question_Template - Part 1: the 10 setup questions (asked once), plus the recurring
    // Daily Check-In questions. Order bands: 1-99 = setup (asked once), 100-199 = daily.
    // Goal selections (multi-select, up to 2) are modelled as one boolean template per goal.
    // ---------------------------------------------------------------------------------------
    val questionTemplates: List<QuestionTemplate> = listOf(
        QuestionTemplate(1, CategoryIds.MOOD_SLEEP, 1, "How would you rate your sleep lately?"),
        QuestionTemplate(2, CategoryIds.STRESS_PHYSICAL, 2, "How often do you feel stressed or overwhelmed?"),
        QuestionTemplate(3, CategoryIds.MOOD_SLEEP, 3, "How have you been feeling emotionally?"),
        QuestionTemplate(4, CategoryIds.CONNECTION, 4, "How connected do you feel to others?"),
        QuestionTemplate(5, CategoryIds.MOTIVATION, 5, "How motivated have you been?"),
        QuestionTemplate(6, CategoryIds.STRESS_PHYSICAL, 6, "How physically active are you?"),
        QuestionTemplate(7, CategoryIds.MOTIVATION, 7, "Do you have a consistent daily routine?"),
        QuestionTemplate(8, CategoryIds.MOTIVATION, 8, "How is your energy through the day?"),
        QuestionTemplate(9, CategoryIds.STRESS_PHYSICAL, 9, "Does pain or a physical condition limit what you can do?"),

        QuestionTemplate(10, CategoryIds.MOOD_SLEEP, 10, "Goal: improve sleep"),
        QuestionTemplate(11, CategoryIds.MOOD_SLEEP, 11, "Goal: improve mood"),
        QuestionTemplate(12, CategoryIds.STRESS_PHYSICAL, 12, "Goal: reduce stress"),
        QuestionTemplate(13, CategoryIds.MOTIVATION, 13, "Goal: increase energy"),
        QuestionTemplate(14, CategoryIds.CONNECTION, 14, "Goal: build connection"),
        QuestionTemplate(15, CategoryIds.STRESS_PHYSICAL, 15, "Goal: improve physical health"),

        QuestionTemplate(16, CategoryIds.MOOD_SLEEP, 101, "How are you feeling today?"),
        QuestionTemplate(17, CategoryIds.MOTIVATION, 102, "How's your energy?"),
        QuestionTemplate(18, CategoryIds.STRESS_PHYSICAL, 103, "How stressed do you feel?"),
        QuestionTemplate(19, CategoryIds.MOOD_SLEEP, 104, "How did you sleep?"),
        QuestionTemplate(20, CategoryIds.MOTIVATION, 105, "How motivated do you feel?"),
        QuestionTemplate(21, CategoryIds.STRESS_PHYSICAL, 106, "Any pain or discomfort?")
    )

    // ---------------------------------------------------------------------------------------
    // streak_milestone - Part 6 exact milestone copy.
    // ---------------------------------------------------------------------------------------
    val streakMilestones: List<StreakMilestone> = listOf(
        StreakMilestone(1, 2, "Two days in a row! You came back. That is the whole secret — not perfection, just returning. You're building a rhythm."),
        StreakMilestone(2, 3, "Three days straight! This is exactly how habits are born — three small days, stacked. Your brain is starting to expect the good stuff."),
        StreakMilestone(3, 5, "Five days! Most people never get here. Quietly, steadily, you're proving something to yourself."),
        StreakMilestone(4, 7, "A full week! Seven days of feeding your four. Whatever this week held, you kept showing up — that is resilience in action."),
        StreakMilestone(5, 10, "Ten days! Double digits. The compound interest of small actions is real, and you're collecting it."),
        StreakMilestone(6, 14, "Two whole weeks! Fourteen days of choosing yourself. This is no longer luck — it's who you're becoming."),
        StreakMilestone(7, 21, "Three weeks! They say it takes about this long for things to start feeling automatic. Notice how much easier it is now?"),
        StreakMilestone(8, 30, "Thirty days! A full month of showing up for your own wellbeing. Genuinely rare. Genuinely impressive."),
        StreakMilestone(9, 50, "Fifty days! Half a hundred. You've built something most people only plan to build."),
        StreakMilestone(10, 75, "Seventy-five days! This is a lifestyle now, not a streak."),
        StreakMilestone(11, 100, "One hundred days! A hundred days of small brave actions. Thank you for trusting the process — look what you made.")
    )

    // ---------------------------------------------------------------------------------------
    // Universal_App_Setting
    // ---------------------------------------------------------------------------------------
    val universalAppSettings: List<UniversalAppSetting> = listOf(
        UniversalAppSetting(1, "missed_day_threshold_days", "2"),
        UniversalAppSetting(2, "max_daily_highlighted_tasks", "4"),
        UniversalAppSetting(3, "min_daily_highlighted_tasks", "2"),
        UniversalAppSetting(4, "checklist_task_cap", "12")
    )

    // ---------------------------------------------------------------------------------------
    // Admin
    // ---------------------------------------------------------------------------------------
    val admins: List<Admin> = listOf(
        Admin(1, "Rehab", "Admin", "admin@rehabinsight.app", "admin123")
    )

    // ---------------------------------------------------------------------------------------
    // Demo Client data - purely so the Admin Dashboard has real, schema-shaped rows to
    // read/aggregate from the very first run (section 17 of the build guide).
    // ---------------------------------------------------------------------------------------
    val demoClients: List<Client> = listOf(
        Client(1001, 1, "Anna", "B.", "anna@demo.rehabinsight.app", passwordHash = "demo"),
        Client(1002, 1, "James", "T.", "james@demo.rehabinsight.app", passwordHash = "demo"),
        Client(1003, 1, "Priya", "S.", "priya@demo.rehabinsight.app", passwordHash = "demo"),
        Client(1004, 1, "Michael", "O.", "michael@demo.rehabinsight.app", passwordHash = "demo")
    )

    val demoClientStreaks: List<ClientStreak> = listOf(
        ClientStreak(1001, currentStreak = 12, bestStreak = 14, lastStreakDate = LocalDate.now()),
        ClientStreak(1002, currentStreak = 3, bestStreak = 8, lastStreakDate = LocalDate.now()),
        ClientStreak(1003, currentStreak = 0, bestStreak = 5, lastStreakDate = LocalDate.now().minusDays(4)),
        ClientStreak(1004, currentStreak = 6, bestStreak = 9, lastStreakDate = LocalDate.now())
    )

    /** The Core Five, assigned to every demo client, with a canned completion count. */
    private val demoBaselineTaskIds = CORE_FIVE_TASK_IDS
    private val demoCompletedCounts = mapOf(1001 to 4, 1002 to 2, 1003 to 1, 1004 to 3)

    val demoClientTasks: List<ClientTask> = buildList {
        var nextId = 1
        val today = LocalDate.now()
        val now = LocalDateTime.now()
        demoClients.forEach { client ->
            val completedCount = demoCompletedCounts[client.clientId] ?: 0
            demoBaselineTaskIds.forEachIndexed { index, taskId ->
                val completed = index < completedCount
                add(
                    ClientTask(
                        clientTaskId = nextId++,
                        clientId = client.clientId,
                        taskId = taskId,
                        assignedAt = now,
                        dueDate = today,
                        completedAt = if (completed) now else null,
                        status = if (completed) ClientTaskStatus.COMPLETED else ClientTaskStatus.PENDING
                    )
                )
            }
        }
    }

    const val FIRST_REAL_CLIENT_ID = 1
    const val FIRST_CLIENT_TASK_ID_AFTER_DEMO = 100
    const val FIRST_TASK_ID_AFTER_LIBRARY = SELF_COMPASSION_TASK_ID + 1
}
