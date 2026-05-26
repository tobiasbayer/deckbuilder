package com.deckbuilder.web

import com.deckbuilder.agent.DeckbuilderAgent
import com.deckbuilder.deck.BudgetTier
import com.deckbuilder.deck.CardCategory
import com.deckbuilder.deck.Color
import com.deckbuilder.deck.DeckCard
import com.deckbuilder.deck.DeckList
import com.deckbuilder.guardrails.BudgetContext
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.service.TokenStream
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class DeckbuilderControllerTest {

    private class BudgetCapturingAgent : DeckbuilderAgent {
        var capturedBudget: Int? = null
        var buildDeckCalls = 0

        override fun chat(sessionId: String, message: String): String = error("not used")
        override fun streamChat(sessionId: String, message: String): TokenStream = error("not used")
        override fun buildDeck(sessionId: String, message: String): DeckList {
            capturedBudget = BudgetContext.get()
            buildDeckCalls++
            return STUB_DECK
        }
    }

    private val agent = BudgetCapturingAgent()
    private val chatMemoryStore = InMemoryChatMemoryStore()
    private val controller = DeckbuilderController(agent, chatMemoryStore)

    @AfterEach
    fun clearContext() = BudgetContext.clear()

    @Test
    fun `backfills budget from chat history when API field is absent`() {
        chatMemoryStore.updateMessages(
            "s1",
            listOf(
                UserMessage.from("I want a \$150 Atraxa deck"),
                AiMessage.from("Sounds great!"),
            ),
        )

        controller.buildDeck(ChatRequest(message = "now build it", sessionId = "s1", maxBudgetUsd = null))

        assertThat(agent.capturedBudget).isEqualTo(150)
    }

    @Test
    fun `API field takes precedence over chat history budget`() {
        chatMemoryStore.updateMessages(
            "s2",
            listOf(
                UserMessage.from("I want a \$150 Atraxa deck"),
                AiMessage.from("Sounds great!"),
            ),
        )

        controller.buildDeck(ChatRequest(message = "now build it", sessionId = "s2", maxBudgetUsd = 200))

        assertThat(agent.capturedBudget).isEqualTo(200)
    }

    @Test
    fun `most recent budget mention wins when history has multiple`() {
        chatMemoryStore.updateMessages(
            "s3",
            listOf(
                UserMessage.from("I have \$200 to spend"),
                AiMessage.from("Got it"),
                UserMessage.from("Actually let's cap it at \$150"),
                AiMessage.from("Okay"),
            ),
        )

        controller.buildDeck(ChatRequest(message = "now build it", sessionId = "s3", maxBudgetUsd = null))

        assertThat(agent.capturedBudget).isEqualTo(150)
    }

    @Test
    fun `no budget set when neither API field nor history have one`() {
        chatMemoryStore.updateMessages(
            "s4",
            listOf(
                UserMessage.from("Build me an Atraxa deck"),
                AiMessage.from("Sure"),
            ),
        )

        controller.buildDeck(ChatRequest(message = "now build it", sessionId = "s4", maxBudgetUsd = null))

        assertThat(agent.capturedBudget).isNull()
    }

    @Test
    fun `BudgetContext is cleared after buildDeck returns`() {
        chatMemoryStore.updateMessages(
            "s5",
            listOf(UserMessage.from("I want a \$150 deck")),
        )

        controller.buildDeck(ChatRequest(message = "build it", sessionId = "s5", maxBudgetUsd = null))

        assertThat(BudgetContext.get()).isNull()
    }

    companion object {
        private val STUB_DECK = DeckList(
            commander = DeckCard(
                name = "Atraxa, Praetors' Voice",
                category = CardCategory.COMMANDER,
                reason = "stub",
                estimatedPrice = "\$30.00",
            ),
            strategy = "stub",
            cards = emptyList(),
            colorIdentity = listOf(Color.W, Color.U, Color.B, Color.G),
            budget = BudgetTier.MID_RANGE,
            keyCards = emptyList(),
            upgradeTargets = emptyList(),
            estimatedPrice = "\$100.00",
        )
    }
}
