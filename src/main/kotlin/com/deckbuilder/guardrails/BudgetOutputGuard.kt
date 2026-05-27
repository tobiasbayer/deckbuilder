package com.deckbuilder.guardrails

import tools.jackson.module.kotlin.jacksonObjectMapper
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.guardrail.OutputGuardrail
import dev.langchain4j.guardrail.OutputGuardrailResult
import org.springframework.stereotype.Component

@Component
class BudgetOutputGuard : OutputGuardrail {

    private val objectMapper = jacksonObjectMapper()

    override fun validate(responseFromLLM: AiMessage): OutputGuardrailResult {
        val maxBudget = BudgetContext.get() ?: return success()

        val text = responseFromLLM.text() ?: return success()
        val estimatedPrice = parseEstimatedPrice(text) ?: return success()

        return if (estimatedPrice > maxBudget) {
            reprompt(
                $$"Deck price $$${"%.2f".format(estimatedPrice)} exceeds budget of $$$maxBudget.",
                $$"The deck you suggested costs $$${"%.2f".format(estimatedPrice)}, " +
                    $$"which is over the $$$maxBudget budget limit. " +
                    $$"Please rebuild the deck staying strictly under $$$maxBudget total. " +
                    "Replace expensive cards with budget-friendly alternatives.",
            )
        } else {
            success()
        }
    }

    private fun parseEstimatedPrice(json: String): Double? = runCatching {
        val node = objectMapper.readTree(json)
        val raw = node.get("estimatedPrice")?.asString() ?: return null
        raw.replace("$", "").replace(",", "").trim().toDouble()
    }.getOrNull()
}
