package com.moa.dto;

import java.time.LocalDateTime;

public record UserNotificationResponse(
        Long id,
        String content,
        LocalDateTime reservedAt
) {
}
