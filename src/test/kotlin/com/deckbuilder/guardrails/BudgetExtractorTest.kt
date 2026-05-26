package com.deckbuilder.guardrails

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BudgetExtractorTest {

    @Test
    fun `extracts dollar sign format`() {
        assertThat(BudgetExtractor.extractBudget("I want a deck, my budget is \$150")).isEqualTo(150)
    }

    @Test
    fun `extracts dollar sign with space`() {
        assertThat(BudgetExtractor.extractBudget("Budget: \$ 200")).isEqualTo(200)
    }

    @Test
    fun `extracts dollar sign with thousands separator`() {
        assertThat(BudgetExtractor.extractBudget("I can spend \$1,500")).isEqualTo(1500)
    }

    @Test
    fun `extracts dollars keyword`() {
        assertThat(BudgetExtractor.extractBudget("My budget is 100 dollars")).isEqualTo(100)
    }

    @Test
    fun `extracts usd keyword`() {
        assertThat(BudgetExtractor.extractBudget("keep it under 250 USD")).isEqualTo(250)
    }

    @Test
    fun `extracts bucks keyword`() {
        assertThat(BudgetExtractor.extractBudget("no more than 75 bucks please")).isEqualTo(75)
    }

    @Test
    fun `returns null when message has no amount`() {
        assertThat(BudgetExtractor.extractBudget("build me an Atraxa deck")).isNull()
    }
}
