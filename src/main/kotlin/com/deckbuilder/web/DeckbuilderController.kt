package com.deckbuilder.web

import com.deckbuilder.agent.DeckbuilderAgent
import com.deckbuilder.deck.DeckList
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class DeckbuilderController(private val agent: DeckbuilderAgent) {

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ChatResponse {
        val reply = agent.chat(
            sessionId = request.sessionId,
            message = request.message,
        )

        return ChatResponse(
            reply = reply,
            sessionId = request.sessionId,
        )
    }

    @PostMapping("/deck")
    fun buildDeck(@RequestBody request: ChatRequest): DeckList {
        return agent.buildDeck(
            sessionId = request.sessionId,
            message = request.message
        )
    }
}