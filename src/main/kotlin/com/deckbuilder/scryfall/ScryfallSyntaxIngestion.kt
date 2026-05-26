package com.deckbuilder.scryfall

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser
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
import java.nio.file.StandardCopyOption

@Service
class ScryfallSyntaxIngestion(
    private val embeddingStore: EmbeddingStore<TextSegment>,
    private val embeddingModel: EmbeddingModel,
    @Value("\${rag.scryfall-syntax-location}") private val syntaxHtml: Resource,
    @Value("\${rag.chunk-size:600}") private val chunkSize: Int,
    @Value("\${rag.chunk-overlap:100}") private val chunkOverlap: Int,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        log.info("Starting Scryfall syntax ingestion...")
        val startTime = System.currentTimeMillis()

        val tempFile = Files.createTempFile("scryfall-syntax", ".html").also {
            syntaxHtml.inputStream.use { input ->
                Files.copy(input, it, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        val document = FileSystemDocumentLoader.loadDocument(tempFile, ApacheTikaDocumentParser())
        log.info("Parsed HTML: ${document.text().length} characters")

        val ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build()

        val result = ingestor.ingest(document)
        val elapsed = System.currentTimeMillis() - startTime
        log.info("Scryfall syntax ingestion complete: ${result.tokenUsage()} in ${elapsed}ms")

        Files.deleteIfExists(tempFile)
    }
}