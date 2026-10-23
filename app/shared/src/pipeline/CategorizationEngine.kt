package com.anix.android.anixlifetracker.pipeline

import com.anix.android.anixlifetracker.database.entity.CategorizationRuleEntity
import com.anix.android.anixlifetracker.model.TransactionCandidate
import com.anix.android.anixlifetracker.model.TransactionType

data class CategorizationResult(
    val categoryId: String,
    val ruleId: String? = null,
    val confidence: Double = 1.0,
    val isFallback: Boolean = false
)

class CategorizationEngine {

    fun determineCategory(
        candidate: TransactionCandidate,
        activeRules: List<CategorizationRuleEntity> = emptyList()
    ): CategorizationResult {
        val merchantLower = candidate.merchantName?.lowercase() ?: ""
        val descLower = candidate.description?.lowercase() ?: ""

        // 1. Evaluate User Custom Rules ordered by priority
        for (rule in activeRules.sortedBy { it.priority }) {
            var isMatch = true
            rule.merchantPattern?.let { pattern ->
                if (!merchantLower.contains(pattern.lowercase())) isMatch = false
            }
            rule.descriptionPattern?.let { pattern ->
                if (!descLower.contains(pattern.lowercase())) isMatch = false
            }
            if (isMatch) {
                return CategorizationResult(
                    categoryId = rule.targetCategoryId,
                    ruleId = rule.id,
                    confidence = rule.confidenceScore,
                    isFallback = false
                )
            }
        }

        // 2. Transaction Type Specific Defaults
        when (candidate.transactionType) {
            TransactionType.INCOME -> {
                if (descLower.contains("salary") || merchantLower.contains("payroll") || descLower.contains("sal")) {
                    return CategorizationResult("cat_sal", confidence = 0.9)
                }
                if (descLower.contains("interest") || descLower.contains("int.pd")) {
                    return CategorizationResult("cat_int", confidence = 0.9)
                }
                if (descLower.contains("dividend") || descLower.contains("div")) {
                    return CategorizationResult("cat_div", confidence = 0.9)
                }
                return CategorizationResult("cat_oth_inc", confidence = 0.7, isFallback = true)
            }
            TransactionType.REFUND -> return CategorizationResult("cat_oth_inc", confidence = 0.8)
            TransactionType.TRANSFER -> return CategorizationResult("cat_trf", confidence = 0.95)
            TransactionType.INVESTMENT -> return CategorizationResult("cat_div", confidence = 0.85)
            else -> {}
        }

        // 3. Keyword Heuristics for Expense Categories
        val textToSearch = "$merchantLower $descLower"

        if (textToSearch.containsAny("swiggy", "zomato", "restaurant", "cafe", "mcdonald", "starbucks", "diner", "pizza")) {
            return CategorizationResult("cat_food", confidence = 0.85)
        }
        if (textToSearch.containsAny("blinkit", "zepto", "instamart", "grocery", "supermarket", "mart", "bigbasket")) {
            return CategorizationResult("cat_groc", confidence = 0.85)
        }
        if (textToSearch.containsAny("amazon", "flipkart", "myntra", "shopping", "apparel", "zara", "h&m")) {
            return CategorizationResult("cat_shop", confidence = 0.80)
        }
        if (textToSearch.containsAny("rent", "housing", "landlord")) {
            return CategorizationResult("cat_rent", confidence = 0.90)
        }
        if (textToSearch.containsAny("electricity", "water", "gas", "utility", "bescom", "airtel", "jio", "recharge")) {
            return CategorizationResult("cat_util", confidence = 0.85)
        }
        if (textToSearch.containsAny("uber", "ola", "metro", "fuel", "petrol", "shell", "fastag", "irctc", "flight")) {
            return CategorizationResult("cat_tran", confidence = 0.85)
        }
        if (textToSearch.containsAny("apollo", "pharmacy", "hospital", "doctor", "lab", "practo", "medical")) {
            return CategorizationResult("cat_heal", confidence = 0.85)
        }
        if (textToSearch.containsAny("movie", "cinema", "bookmyshow", "game", "steam", "playstation")) {
            return CategorizationResult("cat_ent", confidence = 0.80)
        }
        if (textToSearch.containsAny("netflix", "spotify", "youtube", "apple.com", "prime", "google play")) {
            return CategorizationResult("cat_sub", confidence = 0.85)
        }

        return CategorizationResult("cat_oth_exp", confidence = 0.5, isFallback = true)
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
