package com.anix.android.anixlifetracker.model

import kotlin.math.abs

/**
 * Exact monetary value representation using integer minor units (e.g. cents, paise).
 * Avoids floating point inaccuracies.
 */
data class Money(
    val amountInCents: Long,
    val currencyCode: String = "INR"
) {
    val isPositive: Boolean get() = amountInCents > 0
    val isNegative: Boolean get() = amountInCents < 0
    val isZero: Boolean get() = amountInCents == 0L

    val currencySymbol: String
        get() = when (currencyCode.uppercase()) {
            "INR" -> "₹"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            else -> "$currencyCode "
        }

    fun formattedString(includeSign: Boolean = false): String {
        val absValue = abs(amountInCents)
        val units = absValue / 100
        val decimals = (absValue % 100).toString().padStart(2, '0')
        val formattedNumber = "$units.$decimals"

        val sign = when {
            amountInCents < 0 -> "-"
            amountInCents > 0 && includeSign -> "+"
            else -> ""
        }

        return "$sign$currencySymbol$formattedNumber"
    }

    operator fun plus(other: Money): Money {
        require(currencyCode == other.currencyCode) { "Cannot add different currencies: $currencyCode and ${other.currencyCode}" }
        return Money(amountInCents + other.amountInCents, currencyCode)
    }

    operator fun minus(other: Money): Money {
        require(currencyCode == other.currencyCode) { "Cannot subtract different currencies: $currencyCode and ${other.currencyCode}" }
        return Money(amountInCents - other.amountInCents, currencyCode)
    }

    companion object {
        fun zero(currencyCode: String = "INR"): Money = Money(0L, currencyCode)

        fun fromMajor(units: Long, cents: Long = 0, currencyCode: String = "INR"): Money {
            val total = (units * 100) + (if (units < 0) -cents else cents)
            return Money(total, currencyCode)
        }
    }
}
