package com.moa.dto.chat.upstage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstageEmbeddingRequest {
    private String model;
    private String input;
}
