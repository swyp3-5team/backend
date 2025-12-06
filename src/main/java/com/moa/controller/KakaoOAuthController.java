package com.moa.controller;

import com.moa.config.KakaoOAuthConfig;
import com.moa.dto.LoginResponse;
import com.moa.dto.KakaoTokenResponse;
import com.moa.dto.KakaoUserInfo;
import com.moa.entity.Provider;
import com.moa.entity.User;
import com.moa.service.JwtService;
import com.moa.service.ProviderService;
import com.moa.service.UserService;
import com.moa.service.KakaoOAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kakao OAuth", description = "카카오 OAuth 인증 API")
public class KakaoOAuthController {

    private final KakaoOAuthConfig kakaoConfig;
    private final KakaoOAuthService kakaoOAuthService;
    private final UserService userService;
    private final ProviderService providerService;
    private final JwtService jwtService;
    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/authorize")
    @Operation(summary = "1. 카카오 로그인 URL 생성", description = "카카오 OAuth 인증 페이지로 리다이렉트할 URL을 반환합니다.")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl() {
        String authUrl = kakaoConfig.getAuthorizationUrl();
        log.info("카카오 인증 URL 생성: {}", authUrl);

        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authUrl);
        response.put("message", "URL로 이동하여 카카오 로그인을 진행하세요");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    @Operation(summary = "2. 카카오 OAuth 콜백", description = "카카오 로그인 후 리다이렉트되는 콜백 엔드포인트입니다.")
    public ResponseEntity<LoginResponse> callback(
            @RequestParam("code") String code,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "deviceName", required = false) String deviceName) {
        try {
            log.info("카카오 콜백 수신 - Authorization Code: {}", code);

            // 1. Authorization Code로 Access Token 발급
            KakaoTokenResponse tokenResponse = getAccessToken(code);
            log.info("카카오 액세스 토큰 발급 성공");

            // 2. Access Token으로 사용자 정보 조회
            KakaoUserInfo kakaoUserInfo = kakaoOAuthService.getUserInfo(tokenResponse.getAccessToken());
            String username = kakaoOAuthService.getUsername(kakaoUserInfo);
            String email = kakaoOAuthService.getEmail(kakaoUserInfo);

            log.info("카카오 사용자 정보 조회 성공 - username: {}, email: {}", username, email);

            // 3. 이메일로 기존 사용자 확인
            Optional<User> existingUser = userService.getUserByEmail(email);
            User user;
            boolean isNewUser;

            if (existingUser.isPresent()) {
                // 기존 사용자 - 로그인
                user = existingUser.get();
                userService.updateLastLoginAt(user.getUserId());
                isNewUser = false;
                log.info("기존 사용자 로그인 - userId: {}", user.getUserId());
            } else {
                // 신규 사용자 - 회원가입
                user = userService.createUser(username, email, null);
                isNewUser = true;
                log.info("신규 사용자 가입 - userId: {}", user.getUserId());
            }

            // 4. Provider 정보 저장/업데이트 (카카오 Access Token)
            LocalDateTime tokenExpiresAt = tokenResponse.getExpiresIn() != null
                    ? LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn())
                    : null;
            providerService.saveOrUpdateProvider(user.getUserId(), "KAKAO", tokenResponse.getAccessToken(), tokenExpiresAt);

            // 5. JWT 토큰 생성 (Access Token + Refresh Token)
            String device = deviceId != null ? deviceId : "UNKNOWN";
            Map<String, String> tokens = jwtService.generateTokens(user.getUserId(), device);
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            log.info("카카오 로그인/가입 완료 - userId: {}, isNewUser: {}", user.getUserId(), isNewUser);

            // 6. LoginResponse 생성
            LoginResponse response = LoginResponse.from(user, "KAKAO", accessToken, refreshToken, isNewUser);

            return isNewUser
                    ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                    : ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("카카오 OAuth 콜백 처리 중 오류 발생", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    private KakaoTokenResponse getAccessToken(String code) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", kakaoConfig.getClientId());
            formData.add("redirect_uri", kakaoConfig.getRedirectUri());
            formData.add("code", code);

            // Client Secret이 설정된 경우 추가
            if (kakaoConfig.getClientSecret() != null && !kakaoConfig.getClientSecret().isEmpty()) {
                formData.add("client_secret", kakaoConfig.getClientSecret());
                log.info("카카오 토큰 요청 - client_id: {}, redirect_uri: {}, client_secret 포함",
                        kakaoConfig.getClientId(), kakaoConfig.getRedirectUri());
            } else {
                log.info("카카오 토큰 요청 - client_id: {}, redirect_uri: {}, client_secret 미포함 (Native App 모드)",
                        kakaoConfig.getClientId(), kakaoConfig.getRedirectUri());
            }

            KakaoTokenResponse response = webClient.post()
                    .uri(kakaoConfig.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

            log.info("카카오 토큰 발급 성공 - access_token: {}...",
                    response.getAccessToken().substring(0, Math.min(10, response.getAccessToken().length())));

            return response;
        } catch (Exception e) {
            log.error("카카오 토큰 발급 실패", e);
            throw new RuntimeException("카카오 Access Token 발급 실패: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/unlink/{userId}")
    @Operation(summary = "3. 카카오 회원 탈퇴", description = "카카오 연결 해제 및 회원 탈퇴를 처리합니다.")
    public ResponseEntity<Map<String, String>> unlinkKakao(@PathVariable Long userId) {
        try {
            log.info("카카오 회원 탈퇴 요청 - userId: {}", userId);

            // 1. 사용자 존재 확인
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 1-1. 이미 탈퇴한 사용자 처리
            if("DELETED".equals(user.getUserStatus())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "이미 탈퇴한 사용자입니다.");
                response.put("userId", userId.toString());
                return ResponseEntity.ok(response);
            }
            
            // 2. 카카오 Provider 정보 조회
            Optional<Provider> kakaoProvider = providerService.getProviderByUserIdAndProvider(userId, "KAKAO");

            if (kakaoProvider.isPresent()) {
                // 3. 카카오 연결 끊기 (카카오 Access Token 사용)
                try {
                    kakaoOAuthService.unlinkKakao(kakaoProvider.get().getToken());
                    log.info("카카오 API 연결 끊기 성공");
                } catch (Exception e) {
                    log.warn("카카오 API 연결 끊기 실패 (계속 진행): {}", e.getMessage());
                }
            }

            // 4. DB에서 Provider 삭제
            providerService.deleteAllProvidersByUserId(userId);

            // 5. 사용자 Soft Delete (JWT는 서버에 저장하지 않음)
            userService.deleteUser(userId);

            log.info("카카오 회원 탈퇴 완료 - userId: {}", userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원 탈퇴가 완료되었습니다.");
            response.put("userId", userId.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("카카오 회원 탈퇴 실패 - userId: {}", userId, e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "WITHDRAWAL_FAILED");
            errorResponse.put("message", "회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
