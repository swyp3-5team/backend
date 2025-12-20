package com.moa.controller.login.kakao;

import com.moa.annotation.CurrentUserId;
import com.moa.config.login.kakao.KakaoOAuthConfig;
import com.moa.dto.login.LoginResponse;
import com.moa.dto.login.kakao.KakaoIdTokenRequest;
import com.moa.dto.login.kakao.KakaoTokenResponse;
import com.moa.dto.login.kakao.KakaoUserInfo;
import com.moa.entity.Provider;
import com.moa.entity.User;
import com.moa.service.JwtService;
import com.moa.service.UserService;
import com.moa.service.login.ProviderService;
import com.moa.service.login.kakao.KakaoOAuthService;
import com.moa.service.login.kakao.KakaoOIDCService;

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
    private final KakaoOIDCService kakaoOIDCService;
    private final UserService userService;
    private final ProviderService providerService;
    private final JwtService jwtService;
    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/authorize")
    @Operation(summary = "테스트1) 카카오 로그인 URL 생성", description = "카카오 OAuth 인증 페이지로 리다이렉트할 URL을 반환합니다.")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl() {
        String authUrl = kakaoConfig.getAuthorizationUrl();
        log.info("카카오 인증 URL 생성: {}", authUrl);

        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authUrl);
        response.put("message", "URL로 이동하여 카카오 로그인을 진행하세요");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    @Operation(summary = "(사용X) 카카오 OAuth 콜백", description = "카카오 로그인 후 리다이렉트되는 콜백 엔드포인트입니다.")
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
            String email = kakaoOAuthService.getEmail(kakaoUserInfo);
            String username = kakaoOAuthService.getUsername(kakaoUserInfo);

            log.info("카카오 사용자 정보 조회 성공 - email: {}", email);

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
            providerService.saveOrUpdateProvider(user.getUserId(), "KAKAO", tokenResponse.getAccessToken(), tokenExpiresAt, kakaoUserInfo.getId().toString());

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

    @DeleteMapping("/unlink")
    @Operation(summary = "카카오 회원 탈퇴",
               description = "JWT 토큰을 사용하여 카카오 연결 해제 및 회원 탈퇴를 처리합니다.")
    public ResponseEntity<Map<String, String>> unlinkKakao(@CurrentUserId Long userId) {
        try {
            log.info("카카오 회원 탈퇴 요청 - userId: {} (JWT에서 추출)", userId);

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
                // 3. 카카오 연결 끊기 (Admin Key 사용)
                String oauthId = kakaoProvider.get().getOauthId();

                if (oauthId != null && !oauthId.isEmpty()) {
                    try {
                        kakaoOAuthService.unlinkKakaoByAdminKey(oauthId);
                        log.info("카카오 API 연결 끊기 성공 (Admin Key) - oauthId: {}", oauthId);
                    } catch (Exception e) {
                        log.warn("카카오 API 연결 끊기 실패 (계속 진행): {}", e.getMessage());
                    }
                } else {
                    log.warn("oauth_id가 없어 카카오 연결 끊기 생략 - userId: {}", userId);
                }
            }

            // 4. DB에서 Provider 삭제
            providerService.deleteAllProvidersByUserId(userId);

            // 5. 사용자 Soft Delete (JWT는 서버에 저장하지 않음)
            userService.hardDeleteUser(userId);

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

    @GetMapping("/test-oidc")
    @Operation(summary = "테스트2) Code로 ID Token 받기", description = "테스트1 URL 파라미터(Code)를 통한 ID Token을 생성합니다.")
    public ResponseEntity<Map<String, String>> testGetIdToken(@RequestParam("code") String code) {
        try {
            log.info("테스트: Code로 ID Token 받기 - code: {}", code);

            // 1. Code로 Token 발급
            KakaoTokenResponse tokenResponse = getAccessToken(code);

            // 2. ID Token 반환
            Map<String, String> response = new HashMap<>();
            response.put("id_token", tokenResponse.getIdToken());
            response.put("access_token", tokenResponse.getAccessToken());
            response.put("message", "이 id_token을 POST /auth/kakao/token으로 전송하세요");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("테스트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/token")
    @Operation(summary = "카카오 OIDC 로그인 (앱 전용)",
               description = "앱에서 발급받은 ID Token으로 로그인합니다.")
    public ResponseEntity<LoginResponse> loginWithIdToken(@RequestBody KakaoIdTokenRequest request) {
        try {
            log.info("request.getIdToken(): {}", request.getIdToken());
            log.info("카카오 OIDC 로그인 요청 - deviceId: {}", request.getDeviceId());

            // 1. ID Token 서명 검증 및 사용자 정보 추출
            Map<String, Object> userInfo = kakaoOIDCService.verifyIdToken(request.getIdToken());
            String email = (String) userInfo.get("email");
            String username = (String) userInfo.get("nickname");

            log.info("카카오 OIDC 사용자 정보 검증 성공 - username: {}, email: {}", username, email);

            // 2. 이메일로 기존 사용자 확인
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

            // 3. Provider 정보 저장/업데이트 (ID Token을 Access Token으로 저장)
            String oauthId = userInfo.get("oauthId").toString();
            log.info("카카오 oauthId 저장 - userId: {}, oauthId: {}", user.getUserId(), oauthId);
            providerService.saveOrUpdateProvider(user.getUserId(), "KAKAO", request.getIdToken(), null, oauthId);

            // 4. JWT 토큰 생성
            String device = request.getDeviceId() != null ? request.getDeviceId() : "UNKNOWN";
            Map<String, String> tokens = jwtService.generateTokens(user.getUserId(), device);
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            log.info("카카오 OIDC 로그인/가입 완료 - userId: {}, isNewUser: {}", user.getUserId(), isNewUser);

            // 5. LoginResponse 생성
            LoginResponse response = LoginResponse.from(user, "KAKAO", accessToken, refreshToken, isNewUser);

            return isNewUser
                ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                : ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("카카오 OIDC 로그인 실패", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
