package com.rork.budgetflow.data

import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object Money {
    private val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val compact: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun format(value: Double): String = formatter.format(value)

    /** Raw number string without currency symbol or commas — used for editing fields. */
    fun formatNoComma(value: Double): String {
        val rounded = (value * 100).toLong() / 100.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format("%.2f", rounded)
    }

    fun formatSigned(value: Double): String =
        if (value >= 0) "+${formatter.format(value)}" else "-${formatter.format(-value)}"

    fun formatCompact(value: Double): String {
        val abs = kotlin.math.abs(value)
        return when {
            abs >= 1_000_000 -> "$${trim(value / 1_000_000)}M"
            abs >= 1_000 -> "$${trim(value / 1_000)}k"
            else -> compact.format(value)
        }
    }

    private fun trim(v: Double): String {
        val rounded = (v * 10).toLong() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }
}

object Dates {
    fun relative(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> dayMonth(timestamp)
        }
    }

    fun dayMonth(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val month = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )[cal.get(Calendar.MONTH)]
        return "$month ${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    /** Section header label for a transaction time. */
    fun section(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        fun sameDay(a: Calendar, b: Calendar) =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
        return when {
            sameDay(cal, today) -> "Today"
            sameDay(cal, yesterday) -> "Yesterday"
            else -> dayMonth(timestamp)
        }
    }

    fun isMonth(timestamp: Long, month: Calendar): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.YEAR) == month.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == month.get(Calendar.MONTH)
    }

    fun isThisMonth(timestamp: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val now = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }
}
