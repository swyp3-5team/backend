package com.moa.service.login.apple;

import com.moa.config.login.apple.AppleOAuthConfig;
import com.moa.dto.login.apple.ApplePublicKeys;
import com.moa.dto.login.apple.AppleUserInfo;
import com.moa.util.AppleJwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Apple OAuth 서비스
 * ID Token 서명 검증 및 사용자 정보 추출
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthService {

    private final AppleOAuthConfig appleConfig;
    private final AppleJwtUtils appleJwtUtils;
    private final ApplePublicKeyService applePublicKeyService;

    /**
     * Apple ID Token에서 사용자 정보 추출 (서명 검증 포함)
     * Apple Public Key로 ID Token 서명 검증 후 사용자 정보 추출
     */
    public AppleUserInfo getUserInfoFromIdToken(String idToken) {
        try {
            log.info("Apple ID Token 서명 검증 시작");

            // 1. ID Token 헤더에서 kid 추출
            String kid = applePublicKeyService.getKidFromToken(idToken);
            log.info("Apple ID Token kid: {}", kid);

            // 2. Apple Public Keys 조회
            ApplePublicKeys publicKeys = applePublicKeyService.getApplePublicKeys();

            // 3. kid와 일치하는 Public Key 찾기
            ApplePublicKeys.Key matchedKey = publicKeys.getKeys().stream()
                .filter(key -> key.getKid().equals(kid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("일치하는 Apple Public Key를 찾을 수 없습니다. kid: " + kid));

            log.info("일치하는 Apple Public Key 찾음 - kid: {}, alg: {}", matchedKey.getKid(), matchedKey.getAlg());

            // 4. RSA Public Key 생성
            PublicKey publicKey = applePublicKeyService.generateRSAPublicKey(
                matchedKey.getN(),
                matchedKey.getE()
            );

            // 5. ID Token 서명 검증 및 파싱
            Jws<Claims> jws = Jwts.parser()
                .verifyWith((RSAPublicKey) publicKey)
                .build()
                .parseSignedClaims(idToken);

            Claims claims = jws.getPayload();

            log.info("Apple ID Token 서명 검증 성공");

            // 6. AppleUserInfo 생성 (sub 필드 사용)
            AppleUserInfo userInfo = AppleUserInfo.builder()
                .sub(claims.getSubject())  // 고유 식별자
                .email(claims.get("email", String.class))
                .emailVerified(claims.get("email_verified", Boolean.class))
                .iss(claims.getIssuer())
                .aud(claims.getAudience().iterator().next())
                .exp(claims.getExpiration().getTime() / 1000)
                .iat(claims.getIssuedAt().getTime() / 1000)
                .build();

            log.info("Apple 사용자 정보 추출 성공 - sub: {}, email: {}", userInfo.getSub(), userInfo.getEmail());

            return userInfo;

        } catch (Exception e) {
            log.error("Apple ID Token 검증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Apple ID Token 검증 실패: " + e.getMessage());
        }
    }

    /**
     * Apple Client Secret 생성
     */
    public String generateClientSecret() {
        return appleJwtUtils.generateClientSecret(
                appleConfig.getTeamId(),
                appleConfig.getClientId(),
                appleConfig.getKeyId(),
                appleConfig.getPrivateKeyPath()
        );
    }

    /**
     * OAuth ID 추출 (Apple 고유 사용자 ID)
     */
    public String getOAuthId(AppleUserInfo userInfo) {
        return userInfo.getSub();
    }

    /**
     * 이메일 추출 (sub 우선 사용)
     * Apple은 이메일이 없거나 변경될 수 있으므로 sub를 기본 식별자로 사용
     */
    public String getEmail(AppleUserInfo userInfo) {
        // sub 필드를 primary identifier로 사용
        // email이 없거나 변경될 수 있으므로 sub를 기본 식별자로 사용
        if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
            return userInfo.getEmail();
        }

        // email이 없는 경우 sub를 email 형식으로 변환
        return userInfo.getSub() + "@privaterelay.appleid.com";
    }

    /**
     * 사용자 이름 추출 (Apple은 최초 1회만 제공)
     */
    public String getUsername(AppleUserInfo userInfo) {
        // name은 최초 로그인 시에만 제공됨
        // 이후에는 email 앞부분을 사용하거나 기본값 사용
        if (userInfo.getName() != null) {
            return userInfo.getName();
        }

        // 이메일에서 이름 추출 (예: user@example.com → user)
        String email = userInfo.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }

        return "Apple User";
    }

    /**
     * Apple Revoke Token (회원 탈퇴 시)
     */
    public void revokeToken(String accessToken) {
        try {
            // Apple Revoke API 호출
            // 구현 생략 - 필요 시 추가
            log.info("Apple 토큰 Revoke 요청");
        } catch (Exception e) {
            log.error("Apple 토큰 Revoke 실패", e);
            throw new RuntimeException("Apple 연결 해제 실패: " + e.getMessage());
        }
    }
}
