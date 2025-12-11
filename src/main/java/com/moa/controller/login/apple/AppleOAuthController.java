package com.moa.controller.login.apple;

import com.moa.annotation.CurrentUserId;
import com.moa.config.login.apple.AppleOAuthConfig;
import com.moa.dto.login.LoginResponse;
import com.moa.dto.login.apple.AppleTokenResponse;
import com.moa.dto.login.apple.AppleUserInfo;
import com.moa.entity.User;
import com.moa.service.JwtService;
import com.moa.service.UserService;
import com.moa.service.login.ProviderService;
import com.moa.service.login.apple.AppleOAuthService;

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
@RequestMapping("/auth/apple")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Apple OAuth", description = "애플 OAuth 인증 API")
public class AppleOAuthController {

    private final AppleOAuthConfig appleConfig;
    private final AppleOAuthService appleOAuthService;
    private final UserService userService;
    private final ProviderService providerService;
    private final JwtService jwtService;
    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/authorize")
    @Operation(summary = "테스트1) 애플 로그인 URL 생성", description = "애플 OAuth 인증 페이지로 리다이렉트할 URL을 반환합니다.")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl() {
        String authUrl = appleConfig.getAuthorizationUrl();
        log.info("애플 인증 URL 생성: {}", authUrl);

        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authUrl);
        response.put("message", "URL로 이동하여 애플 로그인을 진행하세요");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    @Operation(summary = "애플 OAuth 콜백", description = "애플 로그인 후 리다이렉트되는 콜백 엔드포인트입니다.")
    public ResponseEntity<LoginResponse> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "deviceId", required = false) String deviceId) {
        try {
            log.info("애플 콜백 수신 - Authorization Code: {}, user: {}", code);

            if (code == null) {
                throw new RuntimeException("Authorization Code가 없습니다.");
            }

            // 1. Authorization Code로 Token 발급
            AppleTokenResponse tokenResponse = getAccessToken(code);
            log.info("애플 액세스 토큰 발급 성공");

            // 2. ID Token에서 사용자 정보 추출
            AppleUserInfo appleUserInfo = appleOAuthService.getUserInfoFromIdToken(tokenResponse.getIdToken());
            String email = appleOAuthService.getEmail(appleUserInfo);
            String username = appleOAuthService.getUsername(appleUserInfo);

            log.info("애플 사용자 정보 조회 성공 - username: {}, email: {}", username, email);

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

            // 4. Provider 정보 저장/업데이트 (애플 Access Token)
            LocalDateTime tokenExpiresAt = tokenResponse.getExpiresIn() != null
                    ? LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn())
                    : null;
            providerService.saveOrUpdateProvider(user.getUserId(), "APPLE", tokenResponse.getAccessToken(), tokenExpiresAt);

            // 5. JWT 토큰 생성 (Access Token + Refresh Token)
            String device = deviceId != null ? deviceId : "UNKNOWN";
            Map<String, String> tokens = jwtService.generateTokens(user.getUserId(), device);
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            log.info("애플 로그인/가입 완료 - userId: {}, isNewUser: {}", user.getUserId(), isNewUser);

            // 6. LoginResponse 생성
            LoginResponse response = LoginResponse.from(user, "APPLE", accessToken, refreshToken, isNewUser);

            return isNewUser
                    ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                    : ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("애플 OAuth 콜백 처리 중 오류 발생", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    private AppleTokenResponse getAccessToken(String code) {
        try {
            // Client Secret JWT 생성 (애플은 매번 생성해야 함!)
            String clientSecret = appleOAuthService.generateClientSecret();

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", appleConfig.getClientId());
            formData.add("client_secret", clientSecret);
            formData.add("code", code);
            formData.add("redirect_uri", appleConfig.getRedirectUri());

            log.info("애플 토큰 요청 - client_id: {}, redirect_uri: {}",
                    appleConfig.getClientId(), appleConfig.getRedirectUri());

            AppleTokenResponse response = webClient.post()
                    .uri(appleConfig.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(AppleTokenResponse.class)
                    .block();

            log.info("애플 토큰 발급 성공");

            return response;
        } catch (Exception e) {
            log.error("애플 토큰 발급 실패", e);
            throw new RuntimeException("애플 Access Token 발급 실패: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/unlink")
    @Operation(summary = "애플 회원 탈퇴",
               description = "JWT 토큰을 사용하여 애플 연결 해제 및 회원 탈퇴를 처리합니다.")
    public ResponseEntity<Map<String, String>> unlinkApple(@CurrentUserId Long userId) {
        try {
            log.info("애플 회원 탈퇴 요청 - userId: {} (JWT에서 추출)", userId);

            // 1. 사용자 존재 확인
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 1-1. 이미 탈퇴한 사용자 처리
            if ("DELETED".equals(user.getUserStatus())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "이미 탈퇴한 사용자입니다.");
                response.put("userId", userId.toString());
                return ResponseEntity.ok(response);
            }

            // 2. 애플 Provider 정보 조회
            Optional<com.moa.entity.Provider> appleProvider = providerService.getProviderByUserIdAndProvider(userId, "APPLE");

            if (appleProvider.isPresent()) {
                // 3. 애플 연결 끊기 (애플 Revoke Token API)
                try {
                    appleOAuthService.revokeToken(appleProvider.get().getToken());
                    log.info("애플 API 연결 끊기 성공");
                } catch (Exception e) {
                    log.warn("애플 API 연결 끊기 실패 (계속 진행): {}", e.getMessage());
                }
            }

            // 4. DB에서 Provider 삭제
            providerService.deleteAllProvidersByUserId(userId);

            // 5. 사용자 Soft Delete
            userService.deleteUser(userId);

            log.info("애플 회원 탈퇴 완료 - userId: {}", userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원 탈퇴가 완료되었습니다.");
            response.put("userId", userId.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("애플 회원 탈퇴 실패 - userId: {}", userId, e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "WITHDRAWAL_FAILED");
            errorResponse.put("message", "회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
