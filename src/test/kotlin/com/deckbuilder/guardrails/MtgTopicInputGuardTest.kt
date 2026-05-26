package com.deckbuilder.guardrails

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.guardrail.InputGuardrailResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MtgTopicInputGuardTest {

    private val guard = MtgTopicInputGuard()

    @Test
    fun `blocks clearly off-topic message`() {
        val result = guard.validate(UserMessage.from("What is the best pizza recipe?"))
        assertThat(result.isSuccess).isFalse()
    }

    @Test
    fun `passes message with no keywords`() {
        val result = guard.validate(UserMessage.from("Hello, I need some help"))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `passes message with MTG keyword`() {
        val result = guard.validate(UserMessage.from("Suggest some creatures for my deck"))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `passes message when MTG keyword overrides off-topic keyword`() {
        val result = guard.validate(UserMessage.from("I want a deck for my soccer-themed commander"))
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `failure message is user-friendly`() {
        val result = guard.validate(UserMessage.from("Recommend a good cooking book"))
        val failures = result.failures<InputGuardrailResult.Failure>()
        assertThat(failures).isNotEmpty()
        assertThat(failures.first().message()).contains("Magic: The Gathering")
    }
}
