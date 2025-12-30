package com.moa.dto.notice;

import com.moa.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NoticeResponse(
        @Schema(description = "공지사항 ID", example = "1")
        Long id,

        @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "공지사항 내용", example = "2025년 1월 1일 00:00 ~ 06:00 서비스 점검이 예정되어 있습니다.")
        String content,

        @Schema(description = "생성일시", example = "2025-12-25T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2025-12-25T15:30:00")
        LocalDateTime updatedAt
) {
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
