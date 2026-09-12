package com.example.rehabinsight.data

// ----- Initial Setup Questionnaire options (UI-facing only; persisted as Response rows) -----

enum class SleepRating(val label: String) {
    POOR("Poor"), MODERATE("Moderate"), GOOD("Good")
}

enum class StressFrequency(val label: String) {
    RARELY("Rarely"), SOMETIMES("Sometimes"), OFTEN("Often")
}

enum class MoodRating(val label: String) {
    LOW("Low"), MODERATE("Moderate"), GOOD("Good")
}

enum class LevelRating(val label: String) {
    LOW("Low"), MODERATE("Moderate"), HIGH("High")
}

enum class RoutineRating(val label: String) {
    NO("No"), SOMEWHAT("Somewhat"), YES("Yes")
}

enum class PainLevel(val label: String) {
    NONE("No"), A_LITTLE("A little"), A_LOT("A lot")
}

enum class Goal(val label: String) {
    SLEEP("Sleep"),
    MOOD("Mood"),
    STRESS("Stress"),
    ENERGY("Energy"),
    CONNECTION("Connection"),
    PHYSICAL_HEALTH("Physical health")
}

// ----- Daily Check-In scale -----

enum class FiveLevel(val label: String) {
    GREAT("Great"),
    GOOD("Good"),
    OKAY("Okay"),
    LOW("Low"),
    STRUGGLING("Struggling")
}
