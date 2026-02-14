package com.moa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.config.chat.UpstageConfig;
import com.moa.dto.UpstageLLMRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpstageStudioService {
    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UpstageConfig upstageConfig;

    public UpstageLLMResponse sendReceiptMessage(UpstageLLMRequest upstageLLMRequest) {
        log.info("Upstage LLM API 호출 시작");

        try {
            // API 호출
            UpstageLLMResponse response = webClient.post()
                    .uri(upstageConfig.getChatUri())
                    .header("Authorization", "Bearer " + upstageConfig.getKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(upstageLLMRequest)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(root -> {
                        return root.at("/choices/0/message/content").asText();
                    })
                    .map(content -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.findAndRegisterModules();
                            return mapper.readValue(content, UpstageLLMResponse.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .block();
            if (response != null && response.items() != null) {

                log.info("Upstage API 호출 성공");

                return response;
            }

            throw new RuntimeException("Upstage API 응답이 비어있습니다.");
        } catch (Exception e) {
            log.error("Upstage API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 챗봇 응답 실패: " + e.getMessage());
        }
    }

    public String sendMessage(UpstageLLMRequest upstageLLMRequest) {
        log.info("Upstage API CALL");

        try {
            String response = webClient.post()
                    .uri(upstageConfig.getChatUri())
                    .header("Authorization", "Bearer " + upstageConfig.getKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(upstageLLMRequest)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(root -> {
                        return root.at("/choices/0/message/content").asText();
                    })
                    .map(content -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.findAndRegisterModules();
                            return mapper.readValue(content, String.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .block();
            if (response != null) {

                log.info("Upstage API 호출 성공");

                return response;
            }

            throw new RuntimeException("Upstage API 응답이 비어있습니다.");
        } catch (Exception e) {
            log.error("Upstage API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 챗봇 응답 실패: " + e.getMessage());
        }
    }
}
