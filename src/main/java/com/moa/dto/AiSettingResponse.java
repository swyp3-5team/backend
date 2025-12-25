package com.moa.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 캐릭터 채팅 세팅 응답 DTO
 */
public record AiSettingResponse(
        @Schema(description = "AI 캐릭터 채팅 타입(공감형/팩폭형)" , example = "EMPATH")
        String aiChatType
) {
}
