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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private Object content;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageContentPart {
        private String type;
        private String text;
        private ImageData dataUri;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageData {
        private String data;
    }
}
