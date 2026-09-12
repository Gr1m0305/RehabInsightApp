package com.example.rehabinsight.data

import java.time.LocalDate

data class Reflection(val quote: String, val author: String)

object ReflectionQuotes {
    private val quotes = listOf(
        Reflection("Recovery is not a race. You don't have to feel guilty if it takes you longer than you thought it would.", "Unknown Author"),
        Reflection("Progress, not perfection.", "Unknown Author"),
        Reflection("Small steps in the right direction can turn out to be the biggest step of your life.", "Unknown Author"),
        Reflection("Be gentle with yourself. You're doing the best you can.", "Unknown Author"),
        Reflection("Consistency is built one compassionate day at a time.", "Unknown Author"),
        Reflection("You don't have to see the whole staircase, just take the first step.", "Martin Luther King Jr."),
        Reflection("Healing isn't linear, and that's okay.", "Unknown Author"),
        Reflection("Showing up is half the battle. You showed up today.", "Unknown Author")
    )

    fun forDate(date: LocalDate): Reflection {
        val index = ((date.toEpochDay() % quotes.size) + quotes.size) % quotes.size
        return quotes[index.toInt()]
    }
}
