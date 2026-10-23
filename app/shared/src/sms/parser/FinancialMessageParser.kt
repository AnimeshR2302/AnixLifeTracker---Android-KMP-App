package com.anix.android.anixlifetracker.sms.parser

import com.anix.android.anixlifetracker.model.FinancialMessage
import com.anix.android.anixlifetracker.model.TransactionParseResult

/**
 * Interface for parsing raw SMS text into a structured financial transaction candidate
 */
interface FinancialMessageParser {
    fun parse(message: FinancialMessage): TransactionParseResult
}
