package com.deckbuilder.scryfall

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScryfallToolTest {

    private val mockClient = mockk<ScryfallClient>()
    private val scryfallTool = ScryfallTool(mockClient)

    @Test
    fun `searchCards formats card data correctly`() {
        every { mockClient.searchCards(any(), any()) } returns listOf(
            ScryfallCard(
                name = "Viral Drake",
                manaCost = "{4}{U}",
                typeLine = "Creature — Drake",
                oracleText = "Flying\nProliferate",
                power = "2",
                toughness = "4",
                cmc = 5.0,
                rarity = "uncommon",
                legalities = mapOf("commander" to "legal")
            )
        )

        val result = scryfallTool.searchCards("t:creature o:proliferate", maxResults = 10)

        assertThat(result).contains("Viral Drake")
        assertThat(result).contains("Commander legal: true")
        assertThat(result).contains("CMC: 5")
    }

    @Test
    fun `searchCards returns friendly message when no cards found`() {
        every { mockClient.searchCards(any(), any()) } returns emptyList()

        val result = scryfallTool.searchCards("xyzzy", 5)

        assertThat(result).contains("No cards found")
    }

    @Test
    fun `getCardDetails returns not found message for unknown cards`() {
        every { mockClient.getCardByName(any()) } returns null

        val result = scryfallTool.getCardDetails("Fake Card Name")

        assertThat(result).contains("Card not found")
        verify { mockClient.getCardByName("Fake Card Name") }
    }
}