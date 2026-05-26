package com.deckbuilder.web

import com.deckbuilder.agent.DeckbuilderAgent
import com.deckbuilder.deck.DeckList
import com.deckbuilder.guardrails.BudgetContext
import com.deckbuilder.guardrails.BudgetExtractor
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api")
class DeckbuilderController(
    private val agent: DeckbuilderAgent,
    private val chatMemoryStore: ChatMemoryStore,
) {

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

    @PostMapping("/deck")
    fun buildDeck(@RequestBody request: ChatRequest): DeckList {
        // Take a snapshot of the chat memory and replay it after the deck was built.
        // This avoids priming the model with the huge JSON blob causing subsequent calls
        // to chat() to return JSON instead of markdown.
        val snapshot = chatMemoryStore.getMessages(request.sessionId)
        // API field wins; otherwise fall back to the most recent budget mention in the
        // chat history so a budget stated in /chat carries over to /deck.
        val effectiveBudget = request.maxBudgetUsd
            ?: snapshot.asReversed()
                .filterIsInstance<UserMessage>()
                .firstNotNullOfOrNull { BudgetExtractor.extractBudget(it.singleText()) }
        BudgetContext.set(effectiveBudget)
        try {
            return agent.buildDeck(
                sessionId = request.sessionId,
                message = request.message,
            )
        } finally {
            chatMemoryStore.updateMessages(request.sessionId, snapshot)
            BudgetContext.clear()
        }
    }
}
