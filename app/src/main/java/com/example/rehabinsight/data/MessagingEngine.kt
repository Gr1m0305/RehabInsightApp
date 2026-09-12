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

    /** Part 6 - exact broken-streak / welcome-back copy. */
    fun welcomeBackMessage(): String =
        "Welcome back. Your streak reset — and that is completely okay. Missing days is being " +
            "human, not failing. Today is an opportunity to practice something just as important " +
            "as any streak: self-compassion. Speak to yourself the way you would to a good friend " +
            "having a hard time — then start small. We've added one gentle task to today to help " +
            "you do exactly that."

    const val WELCOME_BACK_BUTTON_TEXT = "Thank you — let's begin again."
}
