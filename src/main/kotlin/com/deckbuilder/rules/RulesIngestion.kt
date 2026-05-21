package com.deckbuilder.rules

import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class RulesIngestionService(
    private val embeddingStore: EmbeddingStore<TextSegment>,
    private val embeddingModel: EmbeddingModel,
    @Value("\${rag.rules-pdf-path}") private val rulesPdf: Resource,
    @Value("\${rag.chunk-size:600}") private val chunkSize: Int,
    @Value("\${rag.chunk-overlap:100}") private val chunkOverlap: Int,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        log.info("Starting MTG rules ingestion...")
        val startTime = System.currentTimeMillis()

        val tempFile = Files.createTempFile("mtg-rules", ".pdf").also {
            rulesPdf.inputStream.use { input ->
                Files.copy(
                    input,
                    it,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                )
            }
        }

        val document = FileSystemDocumentLoader.loadDocument(
            tempFile,
            ApachePdfBoxDocumentParser(),
        )
        log.info("Loaded PDF: ${document.text().length} characters")

        val elapsed = System.currentTimeMillis() - startTime

        val ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build()

        val result = ingestor.ingest(document)
        log.info("Rules ingestion complete: ${result.tokenUsage()} in ${elapsed}ms")

        Files.deleteIfExists(tempFile)
    }
}