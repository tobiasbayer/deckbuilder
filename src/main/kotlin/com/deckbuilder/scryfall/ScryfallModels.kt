package com.deckbuilder.scryfall

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScryfallSearchResponse(
    val data: List<ScryfallCard> = emptyList(),
    @JsonProperty("total_cards") val totalCards: Int = 0,
    @JsonProperty("has_more") val hasMore: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScryfallCard(
    val name: String = "",
    @JsonProperty("mana_cost") val manaCost: String? = null,
    @JsonProperty("type_line") val typeLine: String = "",
    @JsonProperty("oracle_text") val oracleText: String? = null,
    val power: String? = null,
    val toughness: String? = null,
    val loyalty: String? = null,
    val cmc: Double = 0.0,
    val colors: List<String> = emptyList(),
    @JsonProperty("color_identity") val colorIdentity: List<String> = emptyList(),
    val legalities: Map<String, String> = emptyMap(),
    val rarity: String = "",
    val set: String = "",
    @JsonProperty("scryfall_uri") val scryfallUri: String = "",
    val prices: ScryfallPrices? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScryfallPrices(
    val usd: String? = null,
    @JsonProperty("usd_foil")
    val usdFoil: String? = null,
    val eur: String? = null,
    @JsonProperty("eur_foil")
    val eurFoil: String? = null,
)