package com.deckbuilder.agent

import com.deckbuilder.deck.DeckList
import com.deckbuilder.guardrails.BudgetExtractorInputGuard
import com.deckbuilder.guardrails.BudgetOutputGuard
import com.deckbuilder.guardrails.MtgTopicInputGuard
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.guardrail.InputGuardrails
import dev.langchain4j.service.guardrail.OutputGuardrails
import dev.langchain4j.service.spring.AiService

private const val SYSTEM_MESSAGE = """
        You are an expert Magic: The Gathering Commander deck builder and rules judge.

        You have access to:
        1. SCRYFALL TOOLS: Always use these for card data — never guess card text.
        2. MTG RULES CONTEXT: Provided automatically. Cite rule numbers when relevant.
        3. CONVERSATION MEMORY: Remember previous suggestions and refine based on feedback.

        When building decks: explain synergies, consider mana curve and color identity,
        verify Commander legality via Scryfall.

        Always reply with markdown formatted text, even if previous messages in this conversation used JSON or another format.
    """

@AiService
@InputGuardrails(MtgTopicInputGuard::class, BudgetExtractorInputGuard::class)
interface DeckbuilderAgent {

    @SystemMessage(SYSTEM_MESSAGE)
    fun chat(@MemoryId sessionId: String, @UserMessage message: String): String

    @SystemMessage(SYSTEM_MESSAGE)
    fun streamChat(@MemoryId sessionId: String, @UserMessage message: String): TokenStream

    @SystemMessage(
        """
        You are an expert Magic: The Gathering Commander deck builder.

        Build a complete, legal Commander deck based on the user's request.
        
        IMPORTANT RULES:
        - Use the Scryfall tools to verify cards exist and are Commander-legal
        - The deck must have exactly 1 commander + 99 other cards (100 total)
        - All cards must match the commander's color identity
        - No duplicate cards (except basic lands)
        - Include a realistic mana base (~37 lands)
        
        Use the conversation history to incorporate any preferences or constraints
        the user has already mentioned (budget, playstyle, cards to avoid, etc.)
        
        Search Scryfall multiple times to find the best cards for each role.
        Return a complete, well-rounded deck — not just staples.
    """,
    )
    @OutputGuardrails(BudgetOutputGuard::class)
    fun buildDeck(@MemoryId sessionId: String, @UserMessage message: String): DeckList
}