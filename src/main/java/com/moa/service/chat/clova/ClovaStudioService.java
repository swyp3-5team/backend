package com.moa.service.chat.clova;

import com.moa.config.chat.ClovaStudioConfig;
import com.moa.dto.chat.clova.ClovaEmbeddingRequest;
import com.moa.dto.chat.clova.ClovaEmbeddingResponse;
import com.moa.dto.chat.clova.ClovaStudioRequest;
import com.moa.dto.chat.clova.ClovaStudioResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Clova Studio API 호출 서비스
 * HyperCLOVA X (HCX-005) 모델과 통신
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClovaStudioService {

    private final ClovaStudioConfig clovaConfig;
    private final WebClient webClient = WebClient.builder().build();

    public String sendMessage(List<ClovaStudioRequest.Message> messages) {
        try {
            // 요청 빌드
            ClovaStudioRequest request = clovaConfig.getDefaultRequestBuilder()
                    .messages(messages)
                    .build();

            log.info("Clova Studio API 호출 시작 - 메시지 수: {}", messages.size());

            // API 호출
            ClovaStudioResponse response = webClient.post()
                    .uri(clovaConfig.getInvokeUrl())
                    .header("Authorization", "Bearer " + clovaConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClovaStudioResponse.class)
                    .block();

            // 응답 검증
            if (response != null && response.getResult() != null
                    && response.getResult().getMessage() != null) {
                String content = response.getResult().getMessage().getContent();
                log.info("Clova Studio API 호출 성공 - 응답 길이: {} 자", content.length());
                
                // 응답에서 쓸모없는 문자 제거
                if(content != null) {
                    content = content.replaceAll("`", "");
                }
                return content;
            }

            throw new RuntimeException("Clova Studio API 응답이 비어있습니다.");

        } catch (Exception e) {
            log.error("Clova Studio API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 챗봇 응답 실패: " + e.getMessage());
        }
    }

    public List<Double> embedText(String text) {
        try {
            log.debug("텍스트 임베딩 요청: {}", text.substring(0, Math.min(50, text.length())));

            // 요청 DTO 생성
            ClovaEmbeddingRequest request = ClovaEmbeddingRequest.builder()
                    .text(text)
                    .build();

            // API 호출
            ClovaEmbeddingResponse response = webClient.post()
                    .uri(clovaConfig.getEmbeddingUrl())
                    .header("Authorization", "Bearer " + clovaConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClovaEmbeddingResponse.class)
                    .block();

            // 응답 검증
            if (response == null || response.getResult() == null ||
                response.getResult().getEmbedding() == null ||
                response.getResult().getEmbedding().isEmpty()) {
                log.error("임베딩 응답이 비어있습니다.");
                return null;
            }

            List<Double> embedding = response.getResult().getEmbedding();
            log.debug("임베딩 벡터 생성 완료: {}차원", embedding.size());

            return embedding;

        } catch (Exception e) {
            log.error("임베딩 생성 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

}
