package com.deckbuilder.web

import com.deckbuilder.agent.DeckbuilderAgent
import com.deckbuilder.deck.DeckList
import com.deckbuilder.guardrails.BudgetContext
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

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

    @GetMapping("/chat/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamChat(
        @RequestParam sessionId: String,
        @RequestParam message: String,
    ): SseEmitter {
        val emitter = SseEmitter(180_000L)
        agent.streamChat(sessionId, message)
            .onPartialResponse { emitter.send(it) }
            .onCompleteResponse { emitter.complete() }
            .onError { emitter.completeWithError(it) }
            .start()
        return emitter
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