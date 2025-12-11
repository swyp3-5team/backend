package com.moa.service.login.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.moa.config.login.kakao.KakaoOAuthConfig;
import com.moa.dto.login.kakao.KakaoUserInfo;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final WebClient webClient = WebClient.builder().build();
    private final KakaoOAuthConfig kakaoConfig;

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserInfo userInfo = webClient.get()
                    .uri(KAKAO_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfo.class)
                    .block();

            log.info("카카오 사용자 정보 조회 성공: {}", userInfo);
            return userInfo;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new RuntimeException("카카오 로그인 실패: " + e.getMessage());
        }
    }

    public String getOAuthId(KakaoUserInfo userInfo) {
        return String.valueOf(userInfo.getId());
    }

    public String getUsername(KakaoUserInfo userInfo) {
        if (userInfo.getKakaoAccount() != null &&
            userInfo.getKakaoAccount().getProfile() != null) {
            return userInfo.getKakaoAccount().getProfile().getUsername();
        }
        return null;
    }

    public String getEmail(KakaoUserInfo userInfo) {
        if (userInfo.getKakaoAccount() != null) {
            return userInfo.getKakaoAccount().getEmail();
        }
        return null;
    }

    /**
     * 카카오 연결 끊기 (Admin Key 사용)
     * @param kakaoUserId 카카오 회원번호
     */
    public void unlinkKakaoByAdminKey(String kakaoUserId) {
        try {
            log.info("카카오 연결 끊기 시작 (Admin Key) - kakaoUserId: {}", kakaoUserId);

            // Form Data 생성
            String formData = "target_id_type=user_id&target_id=" + kakaoUserId;

            webClient.post()
                .uri("https://kapi.kakao.com/v1/user/unlink")
                .header("Authorization", "KakaoAK " + kakaoConfig.getAdminKey())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            log.info("카카오 연결 끊기 성공 (Admin Key) - kakaoUserId: {}", kakaoUserId);

        } catch (Exception e) {
            log.error("카카오 연결 끊기 실패 (Admin Key) - kakaoUserId: {}", kakaoUserId, e);
            throw new RuntimeException("카카오 연결 해제 실패: " + e.getMessage());
        }
    }

    /**
     * 카카오 연결 끊기 (회원 탈퇴 시 호출)
     */
    public void unlinkKakao(String accessToken) {
        try {
            webClient.post()
                    .uri("https://kapi.kakao.com/v1/user/unlink")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("카카오 연결 끊기 성공");
        } catch (Exception e) {
            log.error("카카오 연결 끊기 실패", e);
            throw new RuntimeException("카카오 연결 해제 실패: " + e.getMessage());
        }
    }
}
