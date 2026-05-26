package com.deckbuilder.guardrails

import dev.langchain4j.data.message.UserMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class BudgetExtractorInputGuardTest {

    private val guard = BudgetExtractorInputGuard()

    @AfterEach
    fun clearContext() = BudgetContext.clear()

    @Test
    fun `extracts dollar sign format`() {
        guard.validate(UserMessage.from("I want a deck, my budget is \$150"))
        assertThat(BudgetContext.get()).isEqualTo(150)
    }

    @Test
    fun `extracts dollar sign with space`() {
        guard.validate(UserMessage.from("Budget: \$ 200"))
        assertThat(BudgetContext.get()).isEqualTo(200)
    }

    @Test
    fun `extracts dollar sign with thousands separator`() {
        guard.validate(UserMessage.from("I can spend \$1,500"))
        assertThat(BudgetContext.get()).isEqualTo(1500)
    }

    @Test
    fun `extracts dollars keyword`() {
        guard.validate(UserMessage.from("My budget is 100 dollars"))
        assertThat(BudgetContext.get()).isEqualTo(100)
    }

    @Test
    fun `extracts usd keyword`() {
        guard.validate(UserMessage.from("keep it under 250 USD"))
        assertThat(BudgetContext.get()).isEqualTo(250)
    }

    @Test
    fun `extracts bucks keyword`() {
        guard.validate(UserMessage.from("no more than 75 bucks please"))
        assertThat(BudgetContext.get()).isEqualTo(75)
    }

    @Test
    fun `sets no budget when message has no amount`() {
        guard.validate(UserMessage.from("build me an Atraxa deck"))
        assertThat(BudgetContext.get()).isNull()
    }

    @Test
    fun `does not overwrite budget already set via API field`() {
        BudgetContext.set(100)
        guard.validate(UserMessage.from("my budget is \$200"))
        assertThat(BudgetContext.get()).isEqualTo(100)
    }

    @Test
    fun `always returns success`() {
        val result = guard.validate(UserMessage.from("my budget is \$150"))
        assertThat(result.isSuccess).isTrue()
    }
}
