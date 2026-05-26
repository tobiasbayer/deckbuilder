package com.deckbuilder.guardrails

object BudgetExtractor {

    // Matches: "$150", "$ 1,500", "$1500.00", "150 dollars", "150 usd", "150 bucks"
    private val PATTERNS = listOf(
        Regex("""\$\s*(\d[\d,]*(?:\.\d+)?)"""),
        Regex("""(\d[\d,]*(?:\.\d+)?)\s*(?:dollars?|usd|bucks?)""", RegexOption.IGNORE_CASE),
    )

    fun extractBudget(text: String): Int? {
        for (pattern in PATTERNS) {
            val match = pattern.find(text) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            return raw.toDoubleOrNull()?.toInt()
        }
        return null
    }
}
