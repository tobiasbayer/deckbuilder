package com.deckbuilder.agent

import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatMemoryConfig {

    @Bean
    fun chatMemoryStore(): ChatMemoryStore = InMemoryChatMemoryStore()

    /**
     * ChatMemoryProvider creates or retrieves a memory instance per memoryId.
     * IMPORTANT: Do not use a ChatMemory bean instead of a provider. All users would
     * share one global conversation. The provider pattern creates isolated memory per
     * memoryId, which is what you almost always want in a real API.
     *
     * LangChain4j calls this automatically when the agent method has @MemoryId.
     */
    @Bean
    fun chatMemoryProvider(chatMemoryStore: ChatMemoryStore): ChatMemoryProvider {
        return ChatMemoryProvider { memoryId ->
            MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20) // keep last 20 messages per session
                .alwaysKeepSystemMessageFirst(true)
                .chatMemoryStore(chatMemoryStore)
                .build()
        }
    }
}