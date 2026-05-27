package com.deckbuilder.scryfall

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component

@Component
class ScryfallTool(private val scryfallClient: ScryfallClient) {

    @Tool(
        """
        Search for Magic: The Gathering cards on Scryfall.
        Use Scryfall query syntax. You can find the Scryfall query syntax specification in your context.
        When searching for cards always add "format:commander".
        Returns a list of matching cards with their details.
        Always call this when you need to find specific cards!
    """,
    )
    fun searchCards(
        @P("Scryfall search query") query: String,
        @P("Maximum number of results, default 10") maxResults: Int? = null,
    ): List<ScryfallCard> {
        return scryfallClient.searchCards(query, maxResults ?: 10)
    }

    @Tool(
        """
        Get detailed information about a specific Magic: The Gathering card by its exact name.
        Use this when the user mentions a specific card by name.
    """,
    )
    fun getCardDetails(
        @P("The exact card name") cardName: String,
    ): ScryfallCard? {
        return scryfallClient.getCardByName(cardName)
    }
}