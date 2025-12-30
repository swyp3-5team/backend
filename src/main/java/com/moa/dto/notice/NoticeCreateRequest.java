package com.moa.dto.notice;

import io.swagger.v3.oas.annotations.media.Schema;

public record NoticeCreateRequest(
        @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "공지사항 내용", example = "2025년 1월 1일 00:00 ~ 06:00 서비스 점검이 예정되어 있습니다.")
        String content
) {
}
