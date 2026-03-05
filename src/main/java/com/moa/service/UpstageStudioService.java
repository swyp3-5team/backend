package com.moa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.config.chat.UpstageConfig;
import com.moa.dto.UpstageLLMRequest;
import com.moa.dto.chat.upstage.UpstageEmbeddingRequest;
import com.moa.dto.chat.upstage.UpstageEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

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
                log.info(response.comment());
                log.info(response.transactionDate());

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
                    // 이미 여기서 "안녕! 😆..." 같은 순수 String을 완벽하게 꺼냈습니다.
                    .map(root -> root.at("/choices/0/message/content").asText())
                    // 두 번째 map(ObjectMapper)은 통째로 삭제했습니다!
                    .block();

            if (response != null && !response.isBlank()) {
                log.info("Upstage API 호출 성공: {}", response); // 로그로 답변 내용 살짝 확인
                return response;
            }

            throw new RuntimeException("Upstage API 응답이 비어있습니다.");
        } catch (Exception e) {
            log.error("Upstage API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 챗봇 응답 실패: " + e.getMessage());
        }
    }

    public List<Double> embedText(String text) {
        try {
            log.debug("텍스트 임베딩 요청: {}", text.substring(0, Math.min(50, text.length())));

            UpstageEmbeddingRequest request = UpstageEmbeddingRequest.builder()
                    .model("solar-embedding-1-large-query") // or solar-embedding-1-large-passage
                    .input(text)
                    .build();

            UpstageEmbeddingResponse response = webClient.post()
                    .uri(upstageConfig.getEmbeddingUri())
                    .header("Authorization", "Bearer " + upstageConfig.getKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UpstageEmbeddingResponse.class)
                    .block();

            if (response == null || response.getData() == null || response.getData().isEmpty() ||
                    response.getData().get(0).getEmbedding() == null ||
                    response.getData().get(0).getEmbedding().isEmpty()) {
                log.error("임베딩 응답이 비어있습니다.");
                return null;
            }

            List<Double> embedding = response.getData().get(0).getEmbedding();
            log.debug("임베딩 벡터 생성 완료: {}차원", embedding.size());

            return embedding;

        } catch (Exception e) {
            log.error("임베딩 생성 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
