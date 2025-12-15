package com.moa.dto.chat;

import com.moa.entity.AiChattingLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 대화 히스토리 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryResponse {

    private Long chattingId;
    private String chatType;
    private String chatContent;
    private String emotion;
    private LocalDateTime createdAt;

    public static ChatHistoryResponse from(AiChattingLog log) {
        return ChatHistoryResponse.builder()
                .chattingId(log.getChattingId())
                .chatType(log.getChatType())
                .chatContent(log.getChatContent())
                .emotion(log.getEmotion())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
