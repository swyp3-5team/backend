package com.moa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.config.chat.GeminiConfig;
import com.moa.dto.UpstageLLMRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiConfig geminiConfig;

    public String sendMessage(UpstageLLMRequest request) {
        log.info("Gemini API CALL");
        try {
            Map<String, Object> geminiRequest = buildGeminiRequest(request.getMessages(), null);
            String response = webClient.post()
                    .uri(geminiConfig.getChatUrl())
                    .header("Content-Type", "application/json")
                    .bodyValue(geminiRequest)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(root -> root.at("/candidates/0/content/parts/0/text").asText())
                    .block();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("Gemini API 응답이 비어있습니다.");
            }
            log.info("Gemini API 호출 성공");
            return response;
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 챗봇 응답 실패: " + e.getMessage());
        }
    }

    public UpstageLLMResponse sendReceiptMessage(UpstageLLMRequest request) {
        log.info("Gemini 영수증 분석 API 호출");
        try {
            Map<String, Object> geminiRequest = buildGeminiRequest(request.getMessages(), buildReceiptSchema());
            String content = webClient.post()
                    .uri(geminiConfig.getChatUrl())
                    .header("Content-Type", "application/json")
                    .bodyValue(geminiRequest)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(root -> root.at("/candidates/0/content/parts/0/text").asText())
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            UpstageLLMResponse response = mapper.readValue(content, UpstageLLMResponse.class);
            if (response == null || response.items() == null) {
                throw new RuntimeException("Gemini 응답 파싱 실패");
            }
            log.info("Gemini 영수증 분석 성공: {}", response.comment());
            return response;
        } catch (Exception e) {
            log.error("Gemini 영수증 분석 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 챗봇 응답 실패: " + e.getMessage());
        }
    }

    public List<Double> embedText(String text) {
        try {
            log.debug("텍스트 임베딩 요청: {}", text.substring(0, Math.min(50, text.length())));
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("model", "models/" + GeminiConfig.EMBEDDING_MODEL);
            request.put("content", Map.of("parts", List.of(Map.of("text", text))));
            request.put("outputDimensionality", GeminiConfig.EMBEDDING_DIMENSIONS);

            JsonNode response = webClient.post()
                    .uri(geminiConfig.getEmbeddingUrl())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) return null;

            List<Double> values = new ArrayList<>();
            for (JsonNode val : response.at("/embedding/values")) {
                values.add(val.asDouble());
            }
            log.debug("임베딩 벡터 생성 완료: {}차원", values.size());
            return values;
        } catch (Exception e) {
            log.error("임베딩 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public String extractOcrText(MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

            Map<String, Object> request = Map.of(
                    "contents", List.of(Map.of("parts", List.of(
                            Map.of("text", "이 영수증 이미지의 텍스트를 줄바꿈을 유지하며 그대로 추출해주세요."),
                            Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64))
                    )))
            );

            String result = webClient.post()
                    .uri(geminiConfig.getChatUrl())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(root -> root.at("/candidates/0/content/parts/0/text").asText())
                    .block();

            return result != null ? result : "";
        } catch (Exception e) {
            log.error("Gemini OCR 실패: {}", e.getMessage(), e);
            return "";
        }
    }

    private Map<String, Object> buildGeminiRequest(List<UpstageLLMRequest.Message> messages, Map<String, Object> schema) {
        Map<String, Object> request = new LinkedHashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        String systemPrompt = null;

        for (UpstageLLMRequest.Message msg : messages) {
            if ("system".equals(msg.getRole())) {
                systemPrompt = msg.getContent().toString();
            } else {
                String geminiRole = "assistant".equals(msg.getRole()) ? "model" : "user";
                List<Map<String, Object>> parts = convertContentToParts(msg.getContent());
                contents.add(Map.of("role", geminiRole, "parts", parts));
            }
        }

        if (systemPrompt != null) {
            request.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
        }
        request.put("contents", contents);

        if (schema != null) {
            request.put("generationConfig", Map.of(
                    "response_mime_type", "application/json",
                    "response_schema", schema
            ));
        }

        return request;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertContentToParts(Object content) {
        if (content instanceof String text) {
            return List.of(Map.of("text", text));
        }

        List<Map<String, Object>> parts = new ArrayList<>();
        for (Map<String, Object> part : (List<Map<String, Object>>) content) {
            String type = (String) part.get("type");
            if ("text".equals(type)) {
                parts.add(Map.of("text", part.get("text")));
            } else if ("image_url".equals(type)) {
                Map<String, Object> imageUrl = (Map<String, Object>) part.get("image_url");
                String url = (String) imageUrl.get("url");
                if (url.startsWith("data:")) {
                    String[] split = url.split(",", 2);
                    String mimeType = split[0].split(":")[1].split(";")[0];
                    parts.add(Map.of("inline_data", Map.of("mime_type", mimeType, "data", split[1])));
                }
            }
        }
        return parts;
    }

    private Map<String, Object> buildReceiptSchema() {
        Map<String, Object> itemProperties = new LinkedHashMap<>();
        itemProperties.put("category", Map.of("type", "STRING", "enum", List.of(
                "식비", "카페", "배달", "술", "교통", "구독",
                "쇼핑", "미용", "취미", "주거", "건강", "자기계발",
                "반려동물", "생활용품", "기타", "용돈", "월급", "기타수입"
        )));
        itemProperties.put("name", Map.of("type", "STRING"));
        itemProperties.put("amount", Map.of("type", "STRING"));

        Map<String, Object> itemSchema = new LinkedHashMap<>();
        itemSchema.put("type", "OBJECT");
        itemSchema.put("properties", itemProperties);
        itemSchema.put("required", List.of("name", "category", "amount"));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("items", Map.of("type", "ARRAY", "items", itemSchema));
        properties.put("emotion", Map.of("type", "STRING", "enum",
                List.of("NEUTRAL", "IMPULSE", "SATISFACTION", "STRESS_RELIEF", "REGRET")));
        properties.put("payment", Map.of("type", "STRING", "enum", List.of("card", "cash")));
        properties.put("comment", Map.of("type", "STRING"));
        properties.put("place", Map.of("type", "STRING"));
        properties.put("transactionDate", Map.of("type", "STRING"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "OBJECT");
        schema.put("properties", properties);
        schema.put("required", List.of("items", "emotion", "comment", "payment"));
        return schema;
    }
}
