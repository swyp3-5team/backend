package com.moa.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KakaoOAuthConfig {

    @Value("${kakao.api.key}")
    private String clientId;

    @Value("${kakao.client.secret:}")
    private String clientSecret;

    @Value("${kakao.login.redirect.uri}")
    private String redirectUri;

    @Value("${kakao.logout.redirect.uri}")
    private String logoutRedirectUri;

    // 카카오 OAuth 인증 URL
    private static final String KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize";

    // 카카오 토큰 발급 URL
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    // 카카오 사용자 정보 조회 URL
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public String getAuthorizationUrl() {
        return KAKAO_AUTH_URL + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
    }

    public String getTokenUrl() {
        return KAKAO_TOKEN_URL;
    }

    public String getUserInfoUrl() {
        return KAKAO_USER_INFO_URL;
    }
}
