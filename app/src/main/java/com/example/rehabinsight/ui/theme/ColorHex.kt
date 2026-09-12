package com.example.rehabinsight.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.rehabinsight.data.Category

/** Parses a "#RRGGBB" or "#AARRGGBB" hex string (as stored in Category.colour) into a Compose Color. */
fun hexToColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return when (cleaned.length) {
        6 -> Color((0xFF000000.toInt()) or cleaned.toInt(16))
        8 -> Color(cleaned.toLong(16).toInt())
        else -> RehabTextMuted
    }
}

/** The Compose colour used for this category's small colour-dot indicator. */
val Category.color: Color get() = hexToColor(colour)
