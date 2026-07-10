package com.moa.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ad")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advertisement", description = "광고 API")
public class AdController {
    @Value("${UPSTAGE_SECRET_KEY}")
    private String apiKey;

    private final String UPSTAGE_URL = "https://api.upstage.ai/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/test")
    public String getChatResponse(String userMessage) throws Exception {
        // 1. 도구(Function) 정의
        Map<String, Object> functionDef = Map.of(
                "name", "search_coupang_products",
                "description", "사용자가 상품 추천을 요청할 때 쿠팡 상품과 파트너스 링크를 검색합니다.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "query", Map.of("type", "string", "description", "검색 키워드 (예: 게이밍 키보드)")
                        ),
                        "required", List.of("query")
                )
        );

        List<Map<String, Object>> tools = List.of(Map.of("type", "function", "function", functionDef));

        // 2. 대화 기록 설정
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userMessage));

        // 3. 첫 번째 요청 (LLM에게 판단 맡기기)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "solar-pro");
        requestBody.put("messages", messages);
        requestBody.put("tools", tools);

        JsonNode firstResponse = callUpstage(requestBody);
        JsonNode messageNode = firstResponse.get("choices").get(0).get("message");

        // 4. Function Calling 확인
        if (messageNode.has("tool_calls")) {
            JsonNode toolCall = messageNode.get("tool_calls").get(0);
            String functionName = toolCall.get("function").get("name").asText();

            if ("search_coupang_products".equals(functionName)) {
                String query = objectMapper.readTree(toolCall.get("function").get("arguments").asText()).get("query").asText();

                // 실제 쿠팡 API 연동 대신 가짜 데이터 생성
                String adResult = "[{\"name\": \"" + query + " 추천 상품\", \"price\": \"30,000원\", \"link\": \"https://link.coupang.com/a/xyz\"}]";

                // 대화 흐름 업데이트
                messages.add(convertToMap(messageNode)); // Assistant의 tool_call 메시지 추가
                messages.add(Map.of(
                        "role", "tool",
                        "tool_call_id", toolCall.get("id").asText(),
                        "name", functionName,
                        "content", adResult
                ));

                // 5. 최종 답변 생성 요청
                requestBody.put("messages", messages);
                JsonNode secondResponse = callUpstage(requestBody);
                return secondResponse.get("choices").get(0).get("message").get("content").asText();
            }
        }

        return messageNode.get("content").asText();
    }

    private JsonNode callUpstage(Map<String, Object> body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(UPSTAGE_URL, entity, String.class);
        return objectMapper.readTree(response);
    }

    private Map<String, Object> convertToMap(JsonNode node) {
        return objectMapper.convertValue(node, Map.class);
    }
}
