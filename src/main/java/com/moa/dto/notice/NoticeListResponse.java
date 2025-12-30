package com.moa.dto.notice;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NoticeListResponse(
        @Schema(description = "전체 공지사항 개수", example = "25")
        long totalCount,

        @Schema(description = "공지사항 목록")
        List<NoticeResponse> notices
) {
    public static NoticeListResponse of(long totalCount, List<NoticeResponse> notices) {
        return new NoticeListResponse(totalCount, notices);
    }
}
