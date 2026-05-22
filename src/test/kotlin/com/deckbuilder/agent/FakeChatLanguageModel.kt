package com.deckbuilder.agent

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse

class FakeChatModel(vararg responses: String) : ChatModel {

    private val queue = ArrayDeque(responses.toList())
    val recordedRequests = mutableListOf<ChatRequest>()

    override fun chat(request: ChatRequest): ChatResponse {
        recordedRequests.add(request)
        val next = queue.removeFirstOrNull()
            ?: error("FakeChatModel: no more responses in queue!\nMessages: ${request.messages()}")
        return ChatResponse.builder()
            .aiMessage(AiMessage(next))
            .build()
    }
}