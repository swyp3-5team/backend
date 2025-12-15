package com.moa.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 클라이언트 채팅 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String message;
}
