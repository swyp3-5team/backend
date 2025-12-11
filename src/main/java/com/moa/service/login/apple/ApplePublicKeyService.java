package com.moa.service.login.apple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.dto.login.apple.ApplePublicKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Apple Public Key 서비스
 * Apple Public Keys 조회 및 RSA Public Key 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplePublicKeyService {

    private final WebClient webClient = WebClient.builder().build();
    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";

    /**
     * Apple Public Keys 조회 (캐싱)
     * 1시간 동안 캐시됨
     */
    @Cacheable(value = "applePublicKeys", unless = "#result == null")
    public ApplePublicKeys getApplePublicKeys() {
        try {
            log.info("Apple Public Keys 조회 시작");

            ApplePublicKeys publicKeys = webClient.get()
                .uri(APPLE_JWKS_URL)
                .retrieve()
                .bodyToMono(ApplePublicKeys.class)
                .block();

            if (publicKeys == null || publicKeys.getKeys() == null || publicKeys.getKeys().isEmpty()) {
                throw new RuntimeException("Apple Public Keys 응답이 비어있습니다.");
            }

            log.info("Apple Public Keys 조회 성공 - {} 개의 키", publicKeys.getKeys().size());
            return publicKeys;

        } catch (Exception e) {
            log.error("Apple Public Keys 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Apple Public Keys 조회 실패: " + e.getMessage());
        }
    }

    /**
     * ID Token 헤더에서 kid 추출
     *
     * @param idToken ID Token
     * @return kid (Key ID)
     */
    public String getKidFromToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("잘못된 ID Token 형식입니다.");
            }

            String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, String> headerMap = mapper.readValue(header, Map.class);

            String kid = headerMap.get("kid");
            if (kid == null || kid.isEmpty()) {
                throw new RuntimeException("ID Token 헤더에 kid가 없습니다.");
            }

            return kid;
        } catch (Exception e) {
            log.error("ID Token kid 추출 실패: {}", e.getMessage());
            throw new RuntimeException("ID Token kid 추출 실패: " + e.getMessage());
        }
    }

    /**
     * RSA Public Key 생성
     *
     * @param n Modulus (Base64 URL 인코딩)
     * @param e Exponent (Base64 URL 인코딩)
     * @return RSA Public Key
     */
    public PublicKey generateRSAPublicKey(String n, String e) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(n);
            byte[] eBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);
        } catch (Exception ex) {
            log.error("RSA Public Key 생성 실패: {}", ex.getMessage());
            throw new RuntimeException("RSA Public Key 생성 실패: " + ex.getMessage());
        }
    }
}
