package com.moa.config.login.apple;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppleOAuthConfig {

    @Value("${apple.team.id}")
    private String teamId;

    @Value("${apple.client.id}")
    private String clientId;

    @Value("${apple.key.id}")
    private String keyId;

    @Value("${apple.private.key.path}")
    private String privateKeyPath;

    @Value("${apple.redirect.uri}")
    private String redirectUri;

    // Apple OAuth 인증 URL
    private static final String APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize";

    // Apple 토큰 발급 URL
    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";

    // Apple 토큰 Revoke URL
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";

    // Apple 공개키 URL
    private static final String APPLE_PUBLIC_KEY_URL = "https://appleid.apple.com/auth/keys";

    public String getAuthorizationUrl() {
        return APPLE_AUTH_URL + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&response_mode=form_post"
                + "&scope=name email";
    }

    public String getTokenUrl() {
        return APPLE_TOKEN_URL;
    }

    public String getRevokeUrl() {
        return APPLE_REVOKE_URL;
    }

    public String getPublicKeyUrl() {
        return APPLE_PUBLIC_KEY_URL;
    }
}
