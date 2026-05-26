package com.deckbuilder.guardrails

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.guardrail.InputGuardrail
import dev.langchain4j.guardrail.InputGuardrailResult
import org.springframework.stereotype.Component

@Component
class MtgTopicInputGuard : InputGuardrail {

    override fun validate(userMessage: UserMessage): InputGuardrailResult {
        val text = userMessage.singleText().lowercase()
        val isOffTopic = OFF_TOPIC_KEYWORDS.any { text.contains(it) } && MTG_KEYWORDS.none { text.contains(it) }
        return if (isOffTopic) {
            failure("This service only answers Magic: The Gathering and Commander deck building questions.")
        } else {
            success()
        }
    }

    companion object {
        private val MTG_KEYWORDS = listOf(
            "magic", "mtg", "commander", "edh", "card", "deck", "spell", "creature",
            "artifact", "enchantment", "planeswalker", "land", "mana", "color",
            "ramp", "removal", "wipe", "combo", "synergy", "scryfall", "tutor",
            "counterspell", "sacrifice", "graveyard", "battlefield", "exile",
            "flying", "lifelink", "deathtouch", "vigilance", "trample",
        )

        private val OFF_TOPIC_KEYWORDS = listOf(
            "pokemon", "recipe", "cooking", "weather", "soccer", "football", "basketball", "baseball",
            "movie", "tv show", "series", "music", "song", "album", "book", "novel",
            "stock", "invest", "crypto", "politics", "election", "news", "travel",
            "restaurant", "hotel", "flight", "shopping",
        )
    }
}
