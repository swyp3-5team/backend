package com.moa.controller.finance;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.response.MonthlyCategoryExpenseResponse;
import com.moa.dto.response.MonthlyEmotionPercentageResponse;
import com.moa.dto.response.MonthlyEmotionStatisticsResponse;
import com.moa.dto.response.MonthlyTotalExpenseResponse;
import com.moa.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Statistics", description = "월간 통계 API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/total/{date}")
    @Operation(summary = "월간 총 지출 금액 조회", description = "특정 월의 총 지출 금액을 조회합니다. date 형식: YYYY-MM (예: 2025-01)")
    public ResponseEntity<?> getMonthlyTotalExpense(
            @CurrentUserId Long userId,
            @PathVariable String date) {
        try {
            log.info("사용자 {} 월간 총 지출 금액 조회 요청: {}", userId, date);
            MonthlyTotalExpenseResponse response = statisticsService.getMonthlyTotalExpense(userId, date);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ie.getMessage()));
        } catch (Exception e) {
            log.error("월간 총 지출 금액 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "월간 총 지출 금액 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/transactions/{date}")
    @Operation(summary = "월간 총 지출 금액을 카테고리별로 조회", description = "특정 월의 카테고리별 지출 금액을 조회합니다. date 형식: YYYY-MM (예: 2025-01)")
    public ResponseEntity<?> getMonthlyCategoryExpense(
            @CurrentUserId Long userId,
            @PathVariable String date) {
        try {
            log.info("사용자 {} 월간 카테고리별 지출 조회 요청: {}", userId, date);
            List<MonthlyCategoryExpenseResponse> response = statisticsService.getMonthlyCategoryExpense(userId, date);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ie.getMessage()));
        } catch (Exception e) {
            log.error("월간 카테고리별 지출 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "월간 카테고리별 지출 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/emotion/{date}")
    @Operation(summary = "월간 감정 통계 조회", description = "특정 월의 감정별 거래 건수 및 총 금액을 조회합니다. date 형식: YYYY-MM (예: 2025-01)")
    public ResponseEntity<?> getMonthlyEmotionStatistics(
            @CurrentUserId Long userId,
            @PathVariable String date) {
        try {
            log.info("사용자 {} 월간 감정 통계 조회 요청: {}", userId, date);
            List<MonthlyEmotionStatisticsResponse> response = statisticsService.getMonthlyEmotionStatistics(userId, date);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ie.getMessage()));
        } catch (Exception e) {
            log.error("월간 감정 통계 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "월간 감정 통계 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/emotion/percentage/{date}")
    @Operation(summary = "월간 감정 비율 조회", description = "특정 월의 감정별 거래 건수 및 비율(%)을 조회합니다. date 형식: YYYY-MM (예: 2025-01)")
    public ResponseEntity<?> getMonthlyEmotionPercentage(
            @CurrentUserId Long userId,
            @PathVariable String date) {
        try {
            log.info("사용자 {} 월간 감정 비율 조회 요청: {}", userId, date);
            List<MonthlyEmotionPercentageResponse> response = statisticsService.getMonthlyEmotionPercentage(userId, date);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ie.getMessage()));
        } catch (Exception e) {
            log.error("월간 감정 비율 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "월간 감정 비율 조회 중 오류가 발생했습니다."));
        }
    }
}
