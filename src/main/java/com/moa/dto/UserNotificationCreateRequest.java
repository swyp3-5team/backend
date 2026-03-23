package com.moa.dto;

import java.time.LocalDateTime;

public record UserNotificationCreateRequest(
        LocalDateTime reservedAt,
        String content
) {
}
