package com.example.ui.components

object PersianUtils {
    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String): String {
        val builder = StringBuilder()
        for (char in text) {
            if (char in '0'..'9') {
                builder.append(persianDigits[char - '0'])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    fun toPersianDigits(number: Number): String {
        return toPersianDigits(number.toString())
    }

    fun formatDuration(durationSeconds: Int): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        val formatted = String.format("%02d:%02d", minutes, seconds)
        return toPersianDigits(formatted)
    }

    fun formatMillis(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt().coerceAtLeast(0)
        return formatDuration(totalSeconds)
    }

    fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> toPersianDigits(String.format("%.1f هزار", count / 1000.0))
            count >= 1000 -> toPersianDigits(String.format("%.1f هزار", count / 1000.0))
            else -> toPersianDigits(count)
        }
    }
}
