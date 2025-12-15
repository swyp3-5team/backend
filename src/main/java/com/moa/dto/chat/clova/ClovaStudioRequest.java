package com.moa.dto.chat.clova;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Clova Studio API 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClovaStudioRequest {

    private List<Message> messages;

    private Double topP;
    private Integer topK;
    private Integer maxTokens;
    private Double temperature;
    private Double repeatPenalty;
    private List<String> stopBefore;
    private Boolean includeAiFilters;

    /**
     * 메시지 DTO (Nested Class)
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
