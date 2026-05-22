package com.deckbuilder.scryfall

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component

@Component
class ScryfallTool(private val scryfallClient: ScryfallClient) {

    // TODO should we inject the complete scryfall query syntax guide into this prompt?
    // https://www.reddit.com/r/magicTCG/comments/eytjhw/scryfall_search_primer_a_guide_for_people_who/
    // Or rather use RAG?
    @Tool(
        """
        Search for Magic: The Gathering cards on Scryfall.
        Use Scryfall query syntax, for example:
          - "commander:legal type:creature color:green cmc<=3" for green creatures legal in commander with a mana value equal to or less than 3
          - "t:artifact o:draw" for artifacts with draw effects
          - "format:commander is:commander" to find valid commanders
        Returns a list of matching cards with their details.
        Always call this when you need to find specific cards!
    """,
    )
    fun searchCards(
        @P("Scryfall search query") query: String,
        @P("Maximum number of results, default 10") maxResults: Int = 10,
    ): String {
        val cards = scryfallClient.searchCards(query, maxResults)

        if (cards.isEmpty()) return "No cards found for query: $query"

        return cards.joinToString("\n---\n") { card ->
            buildString {
                appendLine("**${card.name}**")
                card.manaCost?.let { appendLine("Mana Cost: $it") }
                appendLine("Type: ${card.typeLine}")
                card.oracleText?.let { appendLine("Text: $it") }
                if (card.power != null) appendLine("P/T: ${card.power}/${card.toughness}")
                card.loyalty?.let { appendLine("Loyalty: $it") }
                appendLine("CMC: ${card.cmc.toInt()} | Rarity: ${card.rarity}")
                appendLine("Commander legal: ${card.legalities["commander"] == "legal"}")
            }
        }
    }

    @Tool(
        """
        Get detailed information about a specific Magic: The Gathering card by its exact name.
        Use this when the user mentions a specific card by name.
    """,
    )
    fun getCardDetails(
        @P("The exact card name") cardName: String,
    ): String {
        val card = scryfallClient.getCardByName(cardName)
            ?: return "Card not found: $cardName"

        return buildString {
            appendLine("**${card.name}**")
            card.manaCost?.let { appendLine("Mana Cost: $it") }
            appendLine("Type: ${card.typeLine}")
            card.oracleText?.let { appendLine("Text: $it") }
            if (card.power != null) appendLine("P/T: ${card.power}/${card.toughness}")
            appendLine("Color Identity: ${card.colorIdentity.joinToString(", ")}")
            appendLine("CMC: ${card.cmc.toInt()} | Rarity: ${card.rarity}")
            appendLine("Commander legal: ${card.legalities["commander"] == "legal"}")
            appendLine("Scryfall: ${card.scryfallUri}")
            card.prices?.usd?.let { appendLine("Price (TCGPlayer): $$it") }
        }
    }
}