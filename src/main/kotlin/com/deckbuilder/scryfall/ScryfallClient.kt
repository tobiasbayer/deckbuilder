package com.deckbuilder.scryfall

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class ScryfallClient(
    @Value("\${scryfall.base-url}") private val baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("User-Agent", "DeckBuilder/1.0")
        .defaultHeader("Accept", "*/*")
        .build()

    fun searchCards(query: String, maxResults: Int = 10): List<ScryfallCard> {
        log.info("Searching Scryfall for: $query")
        return try {
            val response = client.get()
                .uri("/cards/search?q={q}&order=edhrec", query)
                .retrieve()
                .body<ScryfallSearchResponse>()
            response?.data?.take(maxResults) ?: emptyList()
        } catch (ex: Exception) {
            log.warn("Scryfall search failed for '$query': ${ex.message}")
            emptyList()
        }
    }

    fun getCardByName(name: String): ScryfallCard? {
        log.info("Fetching card by name: $name")
        return try {
            client.get()
                .uri("/cards/named?exact={name}", name)
                .retrieve()
                .body<ScryfallCard>()
        } catch (ex: Exception) {
            log.warn("Could not find card '$name': ${ex.message}")
            null
        }
    }
}