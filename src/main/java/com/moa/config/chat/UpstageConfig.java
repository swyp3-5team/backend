package com.moa.config.chat;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Upstage Studio 설정
 */
@Configuration
@Getter
public class UpstageConfig {

    @Value("${UPSTAGE_OCR_SECRET_KEY}")
    private String key;

    private String ocrUri = "https://api.upstage.ai/v1/document-digitization";
    private String chatUri = "https://api.upstage.ai/v1/chat/completions";
}
