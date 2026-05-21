package com.deckbuilder.web

import com.deckbuilder.agent.DeckbuilderAgent
import org.springframework.web.bind.annotation.*

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String)

@RestController
@RequestMapping("/api")
class DeckbuilderController(private val agent: DeckbuilderAgent) {

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ChatResponse {
        val reply = agent.chat(request.message)
        return ChatResponse(reply)
    }
}