package com.deckbuilder.guardrails

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.guardrail.InputGuardrail
import dev.langchain4j.guardrail.InputGuardrailResult
import org.springframework.stereotype.Component

@Component
class BudgetExtractorInputGuard : InputGuardrail {

    override fun validate(userMessage: UserMessage): InputGuardrailResult {
        if (BudgetContext.get() != null) return success()
        BudgetExtractor.extractBudget(userMessage.singleText())?.let { BudgetContext.set(it) }
        return success()
    }
}
