package com.deckbuilder.deck

import dev.langchain4j.model.output.structured.Description

enum class CardCategory {
    COMMANDER,
    RAMP,
    CARD_DRAW,
    REMOVAL,
    BOARD_WIPE,
    CREATURE,
    ENCHANTMENT,
    ARTIFACT,
    PLANESWALKER,
    COMBO_PIECE,
    UTILITY_LAND,
    BASIC_LAND
}

enum class BudgetTier {
    BUDGET,       // under $100 total
    MID_RANGE,    // $100–$300
    EXPENSIVE,    // $300–$800
    CEDH          // $800+
}

enum class Color { W, U, B, R, G, C }

data class DeckCard(
    @Description("The exact card name as it appears on the card")
    val name: String,

    @Description("The primary role this card plays in the deck")
    val category: CardCategory,

    @Description("Why this specific card fits the deck strategy (1-2 sentences)")
    val reason: String,

    @Description("Approximate single card price in USD, e.g. '$2.50' or '$0.50'")
    val estimatedPrice: String,
)

data class DeckList(
    @Description("The commander card for this deck")
    val commander: DeckCard,

    @Description("2-3 sentence description of the deck's win condition and overall strategy")
    val strategy: String,

    @Description(
        """
        The 99 non-commander cards in the deck.
        Include a good mix: ~10 ramp, ~10 card draw, ~8 removal, ~37 lands,
        and the rest creatures/spells/artifacts that support the strategy.
    """,
    )
    val cards: List<DeckCard>,

    @Description("The color identity of the commander")
    val colorIdentity: List<Color>,

    @Description("Overall budget estimate for the full 100-card deck")
    val budget: BudgetTier,

    @Description("3-5 key cards that the deck absolutely needs to function")
    val keyCards: List<String>,

    @Description("Potential upgrades to consider if the player has more budget")
    val upgradeTargets: List<String>,
)
