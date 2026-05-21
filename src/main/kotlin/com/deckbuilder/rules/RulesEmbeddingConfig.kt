package com.deckbuilder.rules

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RulesEmbeddingConfig {

    @Bean
    fun embeddingModel(): EmbeddingModel =
        AllMiniLmL6V2QuantizedEmbeddingModel()

    @Bean
    fun embeddingStore(): EmbeddingStore<TextSegment> =
        InMemoryEmbeddingStore() // TODO switch to Chroma for persistence

    @Bean
    fun contentRetriever(
        embeddingStore: EmbeddingStore<TextSegment>,
        embeddingModel: EmbeddingModel
    ): ContentRetriever =
        EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(5)          // top-5 most relevant rule chunks
            .minScore(0.6)          // ignore weak matches
            .build()
}