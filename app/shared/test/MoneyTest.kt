package com.anix.android.anixlifetracker

import com.anix.android.anixlifetracker.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun testMoneyFormattingPositive() {
        val money = Money.fromMajor(1250, 50, "INR")
        assertEquals("₹1250.50", money.formattedString())
        assertEquals("+₹1250.50", money.formattedString(includeSign = true))
        assertTrue(money.isPositive)
        assertFalse(money.isNegative)
    }

    @Test
    fun testMoneyFormattingNegative() {
        val money = Money.fromMajor(-450, 0, "USD")
        assertEquals("-$450.00", money.formattedString())
        assertTrue(money.isNegative)
        assertFalse(money.isPositive)
    }

    @Test
    fun testMoneyAdditionAndSubtraction() {
        val m1 = Money.fromMajor(100, 0, "INR")
        val m2 = Money.fromMajor(50, 25, "INR")

        val sum = m1 + m2
        assertEquals(15025L, sum.amountInCents)
        assertEquals("₹150.25", sum.formattedString())

        val diff = m1 - m2
        assertEquals(4975L, diff.amountInCents)
        assertEquals("₹49.75", diff.formattedString())
    }
}
