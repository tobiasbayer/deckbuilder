package com.deckbuilder.web

import com.deckbuilder.agent.DeckbuilderAgent
import com.deckbuilder.deck.DeckList
import com.deckbuilder.guardrails.BudgetContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @PostMapping("/deck") // TODO after this has been called for the first time, chat() also responds with JSON instead of markdown.
    fun buildDeck(@RequestBody request: ChatRequest): DeckList {
        BudgetContext.set(request.maxBudgetUsd)
        try {
            return agent.buildDeck(
                sessionId = request.sessionId,
                message = request.message,
            )
        } finally {
            BudgetContext.clear()
        }
    }
}