package com.deckbuilder.deck

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckListParsingTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `DeckList deserializes from valid JSON`() {
        val json = """
        {
          "commander": {
            "name": "Atraxa, Praetors' Voice",
            "category": "COMMANDER",
            "reason": "Proliferates every turn.",
            "estimatedPrice": "$8.00"
          },
          "strategy": "Proliferate to win.",
          "colorIdentity": ["W", "U", "B", "G"],
          "budget": "MID_RANGE",
          "keyCards": ["Doubling Season"],
          "upgradeTargets": ["Demonic Tutor"],
          "estimatedPrice": "$8.00",
          "cards": []
        }
        """.trimIndent()

        val deck = mapper.readValue<DeckList>(json)

        assertThat(deck.commander.name).isEqualTo("Atraxa, Praetors' Voice")
        assertThat(deck.commander.category).isEqualTo(CardCategory.COMMANDER)
        assertThat(deck.budget).isEqualTo(BudgetTier.MID_RANGE)
        assertThat(deck.colorIdentity).containsExactly(Color.W, Color.U, Color.B, Color.G)
        assertThat(deck.keyCards).containsExactly("Doubling Season")
    }

    @Test
    fun `DeckList rejects unknown enum values`() {
        val json = """
        {
          "commander": { "name": "X", "category": "COMMANDER", "reason": "r", "estimatedPrice": "$1" },
          "strategy": "s",
          "colorIdentity": [],
          "budget": "UNKNOWN_TIER",
          "keyCards": [],
          "upgradeTargets": [],
          "cards": []
        }
        """.trimIndent()

        org.junit.jupiter.api.assertThrows<Exception> {
            mapper.readValue<DeckList>(json)
        }
    }
}