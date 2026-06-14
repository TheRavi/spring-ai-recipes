package com.triager.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    /**
     * An in-memory vector store, good for development and demos and explicitly
     * not for production. It needs an EmbeddingModel to turn documents into
     * vectors.
     * <p>
     * That EmbeddingModel is auto-configured by the
     * spring-ai-starter-model-google-genai-embedding dependency. The chat
     * starter does not provide one. If you forget the embedding starter, this
     * bean fails to construct because there is no EmbeddingModel to inject.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
