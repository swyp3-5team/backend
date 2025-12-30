package com.moa.controller;

import com.moa.dto.notice.NoticeCreateRequest;
import com.moa.dto.notice.NoticeListResponse;
import com.moa.dto.notice.NoticeResponse;
import com.moa.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notice", description = "공지사항 API")
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다.")
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody NoticeCreateRequest request) {
        log.info("공지사항 등록 요청 - title: {}", request.title());
        NoticeResponse response = noticeService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{noticeId}")
    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeCreateRequest request) {
        log.info("공지사항 수정 요청 - noticeId: {}", noticeId);
        NoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다. (Soft Delete)")
    public ResponseEntity<Map<String, String>> deleteNotice(
            @PathVariable Long noticeId) {
        log.info("공지사항 삭제 요청 - noticeId: {}", noticeId);
        noticeService.deleteNotice(noticeId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "공지사항이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 단건 조회", description = "특정 공지사항을 조회합니다.")
    public ResponseEntity<NoticeResponse> getNotice(
            @PathVariable Long noticeId) {
        log.info("공지사항 조회 요청 - noticeId: {}", noticeId);
        NoticeResponse response = noticeService.getNotice(noticeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 페이징하여 조회합니다. (10개씩, 최신순) - 전체 개수 포함")
    public ResponseEntity<NoticeListResponse> getNotices(
            @RequestParam(defaultValue = "0") int page) {
        log.info("공지사항 목록 조회 요청 - page: {}", page);
        NoticeListResponse response = noticeService.getNotices(page);
        return ResponseEntity.ok(response);
    }
}
