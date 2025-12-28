package com.moa.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HyperCLOVA X (HCX-007) 분석 요청 전용 DTO
 * 구조화된 JSON 응답(Response Format)을 요청하기 위한 스키마 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Hcx007RequestDto {

    private List<Message> messages;

    // 파라미터 설정
    private Double topP;              // 생성 토큰의 누적 확률 임계값
    private Integer topK;             // 상위 k개 토큰 중에서 샘플링
    private Integer maxCompletionTokens; // 최대 생성 토큰 수
    private Double temperature;       // 창의성 정도 (낮을수록 정해진 답, 높을수록 창의적)
    private Double repetitionPenalty; // 반복 억제 패널티

    private Thinking thinking;        // Thinking 옵션 (필요시)
    private List<String> stop = new ArrayList<>();        // 생성을 중단할 단어 목록

    // 핵심: JSON 구조화 요청 설정
    private ResponseFormat responseFormat;

    // --- Inner Classes ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;    // "system", "user", "assistant"
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Thinking {
        private String effort; // "none", "low", "medium", "high"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseFormat {
        private String type;            // "json" 등
        private Map<String, Object> schema; // 복잡한 JSON 스키마를 담기 위한 Map

        public static ResponseFormat createResponseFormat(){
            Map<String, Object> categoryProperty = Map.of(
                    "type", "string",
                    "enum", List.of("식비", "의약품", "의류", "생필품", "화장품", "생활용품", "기타"),
                    "description", "상품 카테고리 분류"
            );

            Map<String, Object> nameProperty = Map.of(
                    "type", "string",
                    "description", "상품의 이름"
            );

            Map<String, Object> rawAmountProperty = Map.of(
                    "type", "string",
                    "description", "금액 그대로(특수문자 제외 숫자만)"
            );

            Map<String, Object> itemProperties = new LinkedHashMap<>();
            itemProperties.put("category", categoryProperty);
            itemProperties.put("name", nameProperty);
            itemProperties.put("raw_amount", rawAmountProperty);

            Map<String, Object> itemSchema = new LinkedHashMap<>();
            itemSchema.put("type", "object");
            itemSchema.put("properties", itemProperties);
            itemSchema.put("required", List.of("name", "category", "raw_amount"));

            Map<String, Object> itemsProperty = new LinkedHashMap<>();
            itemsProperty.put("type", "array");
            itemsProperty.put("description", "영수증 개별 상품 리스트");
            itemsProperty.put("items", itemSchema);

            Map<String, Object> emotionProperty = Map.of(
                    "type", "string",
                    "enum", List.of("NEUTRAL", "IMPULSE", "SATISFACTION", "STRESS_RELIEF", "IMPULSE", "REGRET"),
                    "description", "소비 내역에 대한 감정 분류"
            );

            Map<String, Object> commentProperty = Map.of(
                    "type", "string",
                    "description", "추출 결과 기반 사용자에게 건네는 부드러운 감정 코칭 한마디 이모지를 섞어서"
            );

            Map<String, Object> placeProperty = Map.of(
                    "type", "string",
                    "description", "결제 장소"
            );

            Map<String, Object> transactionDateProperty = Map.of(
                    "type", "string",
                    "description", "결제 날짜"
            );

            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("items", itemsProperty);
            properties.put("emotion", emotionProperty);
            properties.put("comment", commentProperty);
            properties.put("place", placeProperty);
            properties.put("transactionDate", transactionDateProperty);

            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            schema.put("properties", properties);
            schema.put("required", List.of("items", "emotion", "comment"));

            return new ResponseFormat("json", schema);
        }
    }
}