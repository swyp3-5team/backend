package com.moa.dto.chat.clova;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Clova Studio Embedding API 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClovaEmbeddingResponse {

    private Status status;
    private Result result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String code;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private List<Double> embedding;
        private Integer inputTokens;
    }
}
