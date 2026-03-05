package com.moa.dto.chat.upstage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstageEmbeddingResponse {
    private String object;
    private List<EmbeddingData> data;

    @Data
    @NoArgsConstructor
    public static class EmbeddingData {
        private String object;
        private List<Double> embedding;
        private int index;
    }
}
