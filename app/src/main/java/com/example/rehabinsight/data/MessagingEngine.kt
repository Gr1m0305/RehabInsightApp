package com.example.rehabinsight.data

/** Section 12/13 - compassionate, non-judgemental messaging shown on the Home screen. */
object MessagingEngine {

    fun topSupportiveMessage(completed: Int, total: Int): String {
        if (total == 0) return "Small steps today still count"
        return when {
            completed == 0 -> "Small steps today still count"
            completed >= total -> "You've shown up for yourself today. Wonderful work."
            else -> "Small steps today still count"
        }
    }

    fun compassionateFeedback(completed: Int, total: Int): String {
        if (total == 0) return "Your checklist is ready whenever you are."
        val ratio = completed.toFloat() / total.toFloat()
        return when {
            completed == 0 -> "That's okay. Today is an opportunity to practice compassion and try again tomorrow."
            ratio <= 0.4f -> "That's okay. Today is an opportunity to practice compassion and try again tomorrow."
            ratio <= 0.75f -> "That's a great start. Small steps build momentum."
            else -> "You're building strong consistency. Keep going."
        }
    }

    // The welcome-back message itself is the database's streak_broken_message setting.
    const val WELCOME_BACK_BUTTON_TEXT = "Thank you — let's begin again."
}
