package com.triager.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the incident documents into the vector store once, at startup.
 * <p>
 * This is the "ingestion" half of RAG: read raw documents, split them into
 * chunks, embed the chunks, and store them. The retrieval half happens later,
 * at query time, in the query services.
 */
@Service
public class IngestionService implements ApplicationRunner {

    private final VectorStore vectorStore;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources("classpath:/docs/*.txt");

        // TokenTextSplitter breaks each document into chunks sized for embedding.
        // Chunk size is one of the levers that quietly controls retrieval quality.
        var splitter = new TokenTextSplitter();

        List<Document> allChunks = new ArrayList<>();
        for (Resource file : files) {
            var reader = new TextReader(file);
            // Tag every chunk with its source filename so retrieved results can
            // be traced back to the document they came from.
            reader.getCustomMetadata().put("source", file.getFilename());
            List<Document> chunks = splitter.apply(reader.get());
            allChunks.addAll(chunks);
        }

        vectorStore.add(allChunks);

        System.out.println(">> Ingested " + files.length + " documents into "
            + allChunks.size() + " chunks.");
    }
}
