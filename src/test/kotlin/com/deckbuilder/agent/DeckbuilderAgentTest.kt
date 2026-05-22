package com.deckbuilder.agent

import com.deckbuilder.scryfall.ScryfallTool
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.service.AiServices
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckbuilderAgentTest {

    private val mockScryfallTool = mockk<ScryfallTool>()

    private fun agentWith(vararg responses: String): DeckbuilderAgent {
        val fakeModel = FakeChatModel(*responses)
        return AiServices.builder(DeckbuilderAgent::class.java)
            .chatModel(fakeModel)
            .tools(mockScryfallTool)
            .chatMemoryProvider { _ ->
                MessageWindowChatMemory.withMaxMessages(10)
            }
            .build()
    }

    @Test
    fun `agent returns response from model`() {
        val agent = agentWith("Here are some great proliferate creatures for Atraxa!")

        val reply = agent.chat("session-1", "Suggest creatures for Atraxa")

        assertThat(reply).contains("proliferate")
    }

    @Test
    fun `agent maintains memory across turns`() {
        val agent = agentWith(
            "I'll help you build an Atraxa deck!",
            "Based on your Atraxa deck, here are budget options:",
        )

        agent.chat("session-1", "I want to build Atraxa")
        val reply = agent.chat("session-1", "Make it budget")

        assertThat(reply).contains("here are budget options")
    }

    @Test
    fun `different session IDs have isolated memory`() {
        val agent = agentWith(
            "Session A response",
            "Session B response",
        )

        val replyA = agent.chat("session-A", "Hello from A")
        val replyB = agent.chat("session-B", "Hello from B")

        assertThat(replyA).isNotEqualTo(replyB)
    }
}