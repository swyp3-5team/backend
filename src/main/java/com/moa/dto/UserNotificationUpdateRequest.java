package com.moa.dto;

import java.time.LocalDateTime;

public record UserNotificationUpdateRequest(
        String content,
        LocalDateTime reservedAt
) {
}
