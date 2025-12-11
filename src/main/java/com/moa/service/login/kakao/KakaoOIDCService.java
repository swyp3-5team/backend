package com.moa.service.login.kakao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.dto.login.kakao.KakaoPublicKeys;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 카카오 OIDC 서비스
 * ID Token 서명 검증 및 사용자 정보 추출
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOIDCService {

    private final WebClient webClient = WebClient.builder().build();
    private static final String KAKAO_JWKS_URL = "https://kauth.kakao.com/.well-known/jwks.json";

    /**
     * 카카오 Public Keys 조회 (캐싱)
     * 1시간 동안 캐시됨
     */
    @Cacheable(value = "kakaoPublicKeys", unless = "#result == null")
    public KakaoPublicKeys getKakaoPublicKeys() {
        try {
            log.info("카카오 Public Keys 조회 시작");

            KakaoPublicKeys publicKeys = webClient.get()
                .uri(KAKAO_JWKS_URL)
                .retrieve()
                .bodyToMono(KakaoPublicKeys.class)
                .block();

            if (publicKeys == null || publicKeys.getKeys() == null || publicKeys.getKeys().isEmpty()) {
                throw new RuntimeException("카카오 Public Keys 응답이 비어있습니다.");
            }

            log.info("카카오 Public Keys 조회 성공 - {} 개의 키", publicKeys.getKeys().size());
            return publicKeys;

        } catch (Exception e) {
            log.error("카카오 Public Keys 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 Public Keys 조회 실패: " + e.getMessage());
        }
    }

    /**
     * ID Token 서명 검증 및 사용자 정보 추출
     *
     * @param idToken 카카오 ID Token
     * @return 사용자 정보 (sub, email, nickname)
     */
    public Map<String, Object> verifyIdToken(String idToken) {
        try {
            log.info("카카오 ID Token 검증 시작");

            // 1. ID Token 헤더에서 kid 추출
            String kid = getKidFromToken(idToken);
            log.info("ID Token kid: {}", kid);

            // 2. Public Keys 조회
            KakaoPublicKeys publicKeys = getKakaoPublicKeys();

            // 3. kid와 일치하는 Public Key 찾기
            KakaoPublicKeys.Key matchedKey = publicKeys.getKeys().stream()
                .filter(key -> key.getKid().equals(kid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("일치하는 Public Key를 찾을 수 없습니다. kid: " + kid));

            log.info("일치하는 Public Key 찾음 - kid: {}, alg: {}", matchedKey.getKid(), matchedKey.getAlg());

            // 4. RSA Public Key 생성
            PublicKey publicKey = generateRSAPublicKey(matchedKey.getN(), matchedKey.getE());

            // 5. ID Token 서명 검증 및 파싱
            Jws<Claims> claims = Jwts.parser()
                .verifyWith((RSAPublicKey) publicKey)
                .build()
                .parseSignedClaims(idToken);

            log.info("카카오 ID Token 서명 검증 성공");

            // 6. Claims 추출
            Map<String, Object> userInfo = new HashMap<>();
            String sub = claims.getPayload().getSubject(); // 카카오 회원번호
            userInfo.put("sub", sub);
            userInfo.put("oauthId", sub); // sub가 카카오 회원번호 (고유 식별자)
            userInfo.put("email", claims.getPayload().get("email", String.class));
            userInfo.put("nickname", claims.getPayload().get("nickname", String.class));

            log.info("카카오 사용자 정보 추출 완료 - sub: {}, email: {}, nickname: {}",
                    userInfo.get("sub"), userInfo.get("email"), userInfo.get("nickname"));

            return userInfo;

        } catch (Exception e) {
            log.error("카카오 ID Token 검증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("ID Token 검증 실패: " + e.getMessage());
        }
    }

    /**
     * ID Token 헤더에서 kid 추출
     *
     * @param idToken ID Token
     * @return kid (Key ID)
     */
    private String getKidFromToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("잘못된 ID Token 형식입니다.");
            }

            String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
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
    private PublicKey generateRSAPublicKey(String n, String e) {
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
