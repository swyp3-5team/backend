package com.moa.service;

import com.moa.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Access Token과 Refresh Token 생성
     */
    public Map<String, String> generateTokens(Long userId, String deviceId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, deviceId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, deviceId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        log.info("JWT 토큰 생성 - userId: {}, deviceId: {}", userId, deviceId);
        return tokens;
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Long userId, String deviceId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, deviceId);
        log.info("JWT Access Token 생성 - userId: {}, deviceId: {}", userId, deviceId);
        return accessToken;
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 토큰에서 userId 추출
     */
    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    /**
     * 토큰에서 deviceId 추출
     */
    public String getDeviceIdFromToken(String token) {
        return jwtTokenProvider.getDeviceIdFromToken(token);
    }

    /**
     * 토큰 만료 시간 조회
     */
    public Date getExpirationDate(String token) {
        return jwtTokenProvider.getExpirationDateFromToken(token);
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String deviceId = jwtTokenProvider.getDeviceIdFromToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, deviceId);
        log.info("Access Token 갱신 - userId: {}, deviceId: {}", userId, deviceId);

        return newAccessToken;
    }
}
