package com.moa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpstageLLMRequest {

    private String model;
    private List<Message> messages;

    private ResponseFormat response_format;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseFormat {
        private String type; // "json_schema"
        private JsonSchema json_schema;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class JsonSchema {
            private String name;    // e.g. "restaurant_receipt"
            private Boolean strict; // typically true
            private Map<String, Object> schema; // original json schema
        }

        public static ResponseFormat createUpstageResponseFormat() {

            Map<String, Object> category = Map.of(
                    "type", "string",
                    "enum", List.of(
                            "식비", "카페", "배달", "술", "교통", "구독",
                            "쇼핑", "미용", "취미", "주거", "건강", "자기계발",
                            "반려동물", "생활용품", "기타","용돈","월급","기타수입"
                    ),
                    "description", "상품 카테고리 분류"
            );

            Map<String, Object> name = Map.of(
                    "type", "string",
                    "description", "상품의 이름"
            );

            Map<String, Object> rawAmount = Map.of(
                    "type", "string",
                    "description", "금액 그대로(특수문자 제외 숫자만)"
            );

            Map<String, Object> itemProperties = new LinkedHashMap<>();
            itemProperties.put("category", category);
            itemProperties.put("name", name);
            itemProperties.put("amount", rawAmount);

            Map<String, Object> itemSchema = new LinkedHashMap<>();
            itemSchema.put("type", "object");
            itemSchema.put("properties", itemProperties);
            itemSchema.put("required", List.of("name", "category", "amount"));

            Map<String, Object> itemsProperty = new LinkedHashMap<>();
            itemsProperty.put("type", "array");
            itemsProperty.put("description", "영수증 개별 상품 리스트");
            itemsProperty.put("items", itemSchema);

            Map<String, Object> emotionProperty = Map.of(
                    "type", "string",
                    "enum", List.of("NEUTRAL", "IMPULSE", "SATISFACTION", "STRESS_RELIEF", "REGRET"),
                    "description", "소비 내역에 대한 감정 분류"
            );

            Map<String, Object> commentProperty = Map.of(
                    "type", "string",
                    "description", "부드러운 감정 코칭 한마디"
            );

            Map<String, Object> placeProperty = Map.of(
                    "type", "string",
                    "description", "결제 장소"
            );

            Map<String, Object> transactionDateProperty = Map.of(
                    "type", "string",
                    "description", "결제 날짜"
            );
            Map<String, Object> payment = Map.of(
                    "type", "string",
                    "enum", List.of("card", "cash"), // 딱 여기
                    "description", "결제 수단"
            );

            Map<String, Object> properties = new LinkedHashMap<>();

            properties.put("items", itemsProperty);
            properties.put("emotion", emotionProperty);
            properties.put("payment",payment);
            properties.put("comment", commentProperty);
            properties.put("place", placeProperty);
            properties.put("transactionDate", transactionDateProperty);

            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            schema.put("properties", properties);
            schema.put("required", List.of("items", "emotion", "comment","payment"));

            // Upstage JSON Schema Wrapper
            JsonSchema jsonSchema = new ResponseFormat.JsonSchema(
                    "ai_finance_receipt", // schema name (원하는 이름)
                    true,                 // strict mode
                    schema                // 기존 schema 삽입
            );

            return new ResponseFormat(
                    "json_schema",
                    jsonSchema
            );
        }
    }

}
