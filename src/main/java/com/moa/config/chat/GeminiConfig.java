package com.moa.config.chat;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GeminiConfig {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    public static final String CHAT_MODEL = "gemini-3.1-flash-lite-preview";
    public static final String EMBEDDING_MODEL = "gemini-embedding-001";
    public static final int EMBEDDING_DIMENSIONS = 1536;
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    public String getChatUrl() {
        return BASE_URL + "/" + CHAT_MODEL + ":generateContent?key=" + apiKey;
    }

    public String getEmbeddingUrl() {
        return BASE_URL + "/" + EMBEDDING_MODEL + ":embedContent?key=" + apiKey;
    }
}
