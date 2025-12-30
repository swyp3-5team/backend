package com.moa.service;

import com.moa.dto.notice.NoticeCreateRequest;
import com.moa.dto.notice.NoticeListResponse;
import com.moa.dto.notice.NoticeResponse;
import com.moa.entity.Notice;
import com.moa.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest request) {
        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .build();

        Notice savedNotice = noticeRepository.save(notice);
        log.info("공지사항 등록 완료 - noticeId: {}", savedNotice.getId());

        return NoticeResponse.from(savedNotice);
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeCreateRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        notice.update(request.title(), request.content());
        log.info("공지사항 수정 완료 - noticeId: {}", noticeId);

        return NoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        noticeRepository.delete(notice);
        log.info("공지사항 삭제 완료 - noticeId: {}", noticeId);
    }

    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        return NoticeResponse.from(notice);
    }

    public NoticeListResponse getNotices(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Notice> noticePage = noticeRepository.findAllByOrderByCreatedAtDesc(pageable);

        long totalCount = noticeRepository.count();
        List<NoticeResponse> notices = noticePage.map(NoticeResponse::from).getContent();

        log.info("공지사항 목록 조회 - page: {}, totalElements: {}, totalCount: {}",
                page, noticePage.getTotalElements(), totalCount);

        return NoticeListResponse.of(totalCount, notices);
    }
}
