package com.moa.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Apple OAuth Client Secret JWT 생성 유틸리티
 * 애플은 고정된 Client Secret 대신 매번 JWT를 생성해야 함
 */
@Component
@Slf4j
public class AppleJwtUtils {

    private final ResourceLoader resourceLoader;

    public AppleJwtUtils(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Apple Client Secret JWT 생성
     * @param teamId Apple Team ID
     * @param clientId Apple Client ID (Services ID)
     * @param keyId Apple Key ID
     * @param privateKeyPath Private Key 파일 경로 (classpath:apple/AuthKey_XXX.p8)
     * @return Client Secret JWT
     */
    public String generateClientSecret(String teamId, String clientId, String keyId, String privateKeyPath) {
        try {
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            Date expiryDate = new Date(nowMillis + 15777000000L); // 6개월

            return Jwts.builder()
                    .setHeaderParam("kid", keyId)
                    .setHeaderParam("alg", "ES256")
                    .setIssuer(teamId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .setAudience("https://appleid.apple.com")
                    .setSubject(clientId)
                    .signWith(privateKey, SignatureAlgorithm.ES256)
                    .compact();

        } catch (Exception e) {
            log.error("Apple Client Secret 생성 실패", e);
            throw new RuntimeException("Apple Client Secret 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * .p8 파일에서 Private Key 로드
     */
    private PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
    Resource resource = resourceLoader.getResource(privateKeyPath);
        try (InputStream is = resource.getInputStream()) {
            String privateKeyContent = new String(is.readAllBytes());

            // PEM 형식에서 헤더/푸터 제거
            privateKeyContent = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64 디코딩
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

            // PKCS8 형식으로 Private Key 생성
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            return keyFactory.generatePrivate(spec);

        } catch (IOException e) {
            log.error("Private Key 파일 로드 실패: {}", privateKeyPath, e);
            throw new RuntimeException("Private Key 파일을 찾을 수 없습니다: " + privateKeyPath, e);
        }
    }
}
