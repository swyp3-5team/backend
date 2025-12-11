package com.moa.controller.login;

import com.moa.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestHeader("Authorization") String refreshTokenHeader) {
        try {
            // Bearer 토큰 형식에서 토큰만 추출
            String refreshToken = refreshTokenHeader.replace("Bearer ", "").trim();

            log.info("토큰 갱신 요청");

            // Refresh Token으로 새 Access Token 발급
            String newAccessToken = jwtService.refreshAccessToken(refreshToken);

            log.info("Access Token 갱신 완료");

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("message", "Access Token이 갱신되었습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "TOKEN_REFRESH_FAILED");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(401).body(errorResponse);

        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", "토큰 갱신 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다.")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String tokenHeader) {
        try {
            // Bearer 토큰 형식에서 토큰만 추출
            String token = tokenHeader.replace("Bearer ", "").trim();

            log.info("토큰 검증 요청");

            // 토큰 검증
            boolean isValid = jwtService.validateToken(token);

            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                Long userId = jwtService.getUserIdFromToken(token);
                Date expiresAt = jwtService.getExpirationDate(token);

                response.put("valid", true);
                response.put("userId", userId);
                response.put("expiresAt", expiresAt);
                response.put("message", "유효한 토큰입니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("valid", false);
                response.put("message", "유효하지 않거나 만료된 토큰입니다.");
                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            log.error("토큰 검증 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "TOKEN_VALIDATION_FAILED");
            errorResponse.put("message", "토큰 검증 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
