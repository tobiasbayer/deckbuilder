package com.deckbuilder.guardrails

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.guardrail.OutputGuardrailResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class BudgetOutputGuardTest {

    private val guard = BudgetOutputGuard()

    @AfterEach
    fun clearContext() = BudgetContext.clear()

    @Test
    fun `passes when no budget is set`() {
        val result = guard.validate(deckResponse("$500.00"))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `passes when deck price is under budget`() {
        BudgetContext.set(200)
        val result = guard.validate(deckResponse("$150.00"))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `passes when deck price equals budget`() {
        BudgetContext.set(150)
        val result = guard.validate(deckResponse("$150.00"))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `reprompts when deck price exceeds budget`() {
        BudgetContext.set(100)
        val result = guard.validate(deckResponse("$250.00"))
        assertThat(result.isReprompt).isTrue()
    }

    @Test
    fun `reprompt message contains price and budget limit`() {
        BudgetContext.set(100)
        val result = guard.validate(deckResponse("$250.00"))
        val reprompt = result.failures<OutputGuardrailResult.Failure>().first().reprompt()
        assertThat(reprompt).contains("250")
        assertThat(reprompt).contains("100")
    }

    @Test
    fun `passes when estimatedPrice field is missing`() {
        BudgetContext.set(100)
        val result = guard.validate(AiMessage("""{"strategy": "aggro"}"""))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `passes when response is not valid JSON`() {
        BudgetContext.set(100)
        val result = guard.validate(AiMessage("Here is your deck suggestion..."))
        assertThat(result.isSuccess).isTrue()
    }

    private fun deckResponse(price: String) =
        AiMessage("""{"estimatedPrice": "$price", "strategy": "test"}""")
}
