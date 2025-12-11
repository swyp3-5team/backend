package com.moa.dto.login.kakao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 카카오 Public Keys 응답 DTO
 * https://kauth.kakao.com/.well-known/jwks.json 에서 조회
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPublicKeys {
    /**
     * Public Key 목록
     */
    private List<Key> keys;

    /**
     * Public Key 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key {
        /**
         * Key ID (토큰 헤더의 kid와 매칭)
         */
        private String kid;

        /**
         * Key Type (RSA)
         */
        private String kty;

        /**
         * Algorithm (RS256)
         */
        private String alg;

        /**
         * Public Key Use (sig)
         */
        private String use;

        /**
         * Modulus (RSA Public Key)
         */
        private String n;

        /**
         * Exponent (RSA Public Key)
         */
        private String e;
    }
}
