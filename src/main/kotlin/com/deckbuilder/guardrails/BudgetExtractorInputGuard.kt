package com.deckbuilder.guardrails

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.guardrail.InputGuardrail
import dev.langchain4j.guardrail.InputGuardrailResult
import org.springframework.stereotype.Component

@Component
class BudgetExtractorInputGuard : InputGuardrail {

    override fun validate(userMessage: UserMessage): InputGuardrailResult {
        if (BudgetContext.get() != null) return success()
        extractBudget(userMessage.singleText())?.let { BudgetContext.set(it) }
        return success()
    }

    private fun extractBudget(text: String): Int? {
        for (pattern in PATTERNS) {
            val match = pattern.find(text) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            return raw.toDoubleOrNull()?.toInt()
        }
        return null
    }

    companion object {
        // Matches: "$150", "$ 1,500", "$1500.00", "150 dollars", "150 usd", "150 bucks"
        private val PATTERNS = listOf(
            Regex("""\$\s*(\d[\d,]*(?:\.\d+)?)"""),
            Regex("""(\d[\d,]*(?:\.\d+)?)\s*(?:dollars?|usd|bucks?)""", RegexOption.IGNORE_CASE),
        )
    }
}
