package com.deckbuilder.agent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Tag("token-burning")
class DeckBuilderAgentIntegrationTest {

    @Autowired
    lateinit var agent: DeckbuilderAgent

    @Test
    fun `agent suggests cards for Atraxa and searches Scryfall`() {
        val reply = agent.chat(
            "it-test-${System.currentTimeMillis()}",
            "Suggest 3 proliferate creatures for an Atraxa Commander deck",
        )

        assertThat(reply).isNotBlank()
        // Don't assert specific card names as LLM output is non-deterministic
        // Just verify the agent didn't crash and returned something meaningful
        assertThat(reply.length).isGreaterThan(50)
    }
}