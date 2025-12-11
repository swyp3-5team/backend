package com.moa.dto.login.apple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Apple Public Keys 응답 DTO
 * https://appleid.apple.com/auth/keys 에서 조회
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplePublicKeys {
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
         * Key Type (RSA)
         */
        private String kty;

        /**
         * Key ID (토큰 헤더의 kid와 매칭)
         */
        private String kid;

        /**
         * Public Key Use (sig)
         */
        private String use;

        /**
         * Algorithm (RS256)
         */
        private String alg;

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
