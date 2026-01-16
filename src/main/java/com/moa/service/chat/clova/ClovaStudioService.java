package com.moa.service.chat.clova;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.config.chat.ClovaStudioConfig;
import com.moa.dto.AiJson;
import com.moa.dto.AiTransactionResponse;
import com.moa.dto.Hcx007RequestDto;
import com.moa.dto.TransactionDetailRequest;
import com.moa.dto.chat.clova.ClovaEmbeddingRequest;
import com.moa.dto.chat.clova.ClovaEmbeddingResponse;
import com.moa.dto.chat.clova.ClovaStudioRequest;
import com.moa.dto.chat.clova.ClovaStudioResponse;
import com.moa.entity.TransactionEmotion;
import com.moa.reponse.AiReceiptResponse;
import com.moa.repository.CategoryRepository;
import com.moa.service.chat.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.moa.util.JsonParser.cleanUpJson;
import static com.moa.util.LocalDateParser.parseLocalDate;

/**
 * Clova Studio API 호출 서비스
 * HyperCLOVA X (HCX-005) 모델과 통신
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClovaStudioService {

    private final ClovaStudioConfig clovaConfig;
    private final CategoryRepository categoryRepository;
    private final TransactionService transactionService;
    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            return clientResponse.bodyToMono(ClovaStudioResponse.class);
                        } else {
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("[Clova ERROR] status={}, body={}",
                                                clientResponse.statusCode(), errorBody);
                                        return Mono.error(new RuntimeException(errorBody));
                                    });
                        }
                    })
                    .block();

            // 응답 검증
            if (response != null && response.getResult() != null
                    && response.getResult().getMessage() != null) {
                String content = response.getResult().getMessage().getContent();
                log.info("Clova Studio API 호출 성공 - 응답 길이: {} 자", content.length());

                // 응답에서 쓸모없는 문자 제거
                if (content != null) {
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

    public String sendReceiptMessage(Hcx007RequestDto requestDto) {
        try {
            // 요청 빌드
            log.info("Clova Studio API 호출 시작 - 메시지 수: {}", requestDto.getMessages().size());

            // API 호출
            ClovaStudioResponse response = webClient.post()
                    .uri(clovaConfig.getHCX007Url())
                    .header("Authorization", "Bearer " + clovaConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(ClovaStudioResponse.class)
                    .block();

            // 응답 검증
            if (response != null && response.getResult() != null
                    && response.getResult().getMessage() != null) {
                String content = response.getResult().getMessage().getContent();
                log.info("Clova Studio API 호출 성공 - 응답 길이: {} 자", content.length());

                // 응답에서 쓸모없는 문자 제거
                if (content != null) {
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

    public AiReceiptResponse extractTransaction(String content) {
        log.info("Clova JSON (head 1000): {}",
                content != null ? content.substring(0, Math.min(1000, content.length())) : "null");
        String json = cleanUpJson(content);

        AiJson aijson = jsonToDto(json);
        log.info("Clova receipt parsed items size: {}",
                aijson.getItems() != null ? aijson.getItems().size() : 0);
        if (aijson.getItems() == null || aijson.getItems().isEmpty()) {
            throw new IllegalArgumentException("AI 파싱 결과가 없습니다.");
        }


        List<TransactionDetailRequest> transactions = aijson.getItems().stream().map(
                item -> {
                    String categoryName = item.getCategory();
                    return TransactionDetailRequest.fromJson(
                            item, categoryName
                    );
                }
        ).toList();
        Long totalAmount = transactions.stream().mapToLong(
                TransactionDetailRequest::amount
        ).sum();
        String place = aijson.getPlace();
        if (place == null || place.isBlank()) {
            place = null;
        }
        log.info("emotion : {}",aijson.getEmotion());
        // 거래 내역 생성 요청 DTO 형태로 반환
        AiTransactionResponse request = new AiTransactionResponse(
                place,
                parseLocalDate(aijson.getTransactionDate()),
                "CARD",
                null,
                totalAmount,
                TransactionEmotion.from(aijson.getEmotion()).name(),
                transactions
        );

        return new AiReceiptResponse(
                aijson.getComment(),
                request
        );
    }

    private AiJson jsonToDto(String json) {
        try {
            return objectMapper.readValue(json, AiJson.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "JSON 파싱 실패: 스키마 불일치 또는 형식 오류",
                    e
            );
        }
    }
}
