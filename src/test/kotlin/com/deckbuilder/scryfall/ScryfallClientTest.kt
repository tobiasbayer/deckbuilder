package com.deckbuilder.scryfall

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScryfallClientTest {

    private val wireMock = WireMockServer(wireMockConfig().dynamicPort())
    private lateinit var client: ScryfallClient

    @BeforeAll
    fun startWireMock() {
        wireMock.start()
        client = ScryfallClient(baseUrl = "http://localhost:${wireMock.port()}")
    }

    @AfterAll
    fun stopWireMock() = wireMock.stop()

    @BeforeEach
    fun resetStubs() = wireMock.resetAll()

    @Test
    fun `searchCards returns parsed cards from Scryfall response`() {
        wireMock.stubFor(
            get(urlPathEqualTo("/cards/search"))
                .withQueryParam("q", equalTo("t:creature o:proliferate"))
                .willReturn(
                    okJson(
                        """
                        {
                          "data": [
                            {
                              "name": "Viral Drake",
                              "mana_cost": "{4}{U}",
                              "type_line": "Creature — Drake",
                              "oracle_text": "Flying\nProliferate",
                              "power": "2", "toughness": "4",
                              "cmc": 5.0,
                              "colors": ["U"],
                              "color_identity": ["U"],
                              "rarity": "uncommon",
                              "set": "nph",
                              "legalities": { "commander": "legal" },
                              "scryfall_uri": "https://scryfall.com/card/nph/48"
                            }
                          ],
                          "total_cards": 1,
                          "has_more": false
                        }
                        """.trimIndent()
                    )
                )
        )

        val cards = client.searchCards("t:creature o:proliferate", maxResults = 5)

        assertThat(cards).hasSize(1)
        with(cards.first()) {
            assertThat(name).isEqualTo("Viral Drake")
            assertThat(manaCost).isEqualTo("{4}{U}")
            assertThat(typeLine).isEqualTo("Creature — Drake")
            assertThat(legalities["commander"]).isEqualTo("legal")
        }
    }

    @Test
    fun `searchCards returns empty list on Scryfall 404`() {
        wireMock.stubFor(
            get(urlPathEqualTo("/cards/search"))
                .willReturn(aResponse().withStatus(404).withBody("""{"code":"not_found"}"""))
        )

        val cards = client.searchCards("xyzzy nonexistent", maxResults = 5)

        assertThat(cards).isEmpty()  // should not throw
    }

    @Test
    fun `getCardByName returns null when card not found`() {
        wireMock.stubFor(
            get(urlPathEqualTo("/cards/named"))
                .willReturn(aResponse().withStatus(404))
        )

        val card = client.getCardByName("Definitely Not A Real Card")

        assertThat(card).isNull()
    }

    @Test
    fun `searchCards respects maxResults limit`() {
        // Scryfall returns 3 cards but we ask for 2
        val threeCards = (1..3).map { i ->
            """{"name": "Card $i", "type_line": "Creature", "cmc": 3.0,
               "colors": [], "color_identity": [], "rarity": "common",
               "set": "m21", "legalities": {}, "scryfall_uri": ""}"""
        }
        wireMock.stubFor(
            get(urlPathEqualTo("/cards/search"))
                .willReturn(okJson("""{"data": [${threeCards.joinToString(",")}], "total_cards": 3, "has_more": false}"""))
        )

        val cards = client.searchCards("anything", maxResults = 2)

        assertThat(cards).hasSize(2)  // client-side trimming works
    }
}