package com.moa.dto.login.apple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Apple ID Token에서 추출한 사용자 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppleUserInfo {

    private String sub;          // Apple 고유 사용자 ID
    private String email;        // 이메일
    private Boolean emailVerified; // 이메일 인증 여부
    private String name;         // 이름 (최초 로그인 시에만 제공)

    // ID Token Claims
    private String iss;          // Issuer (https://appleid.apple.com)
    private String aud;          // Audience (Client ID)
    private Long exp;            // Expiration time
    private Long iat;            // Issued at
}
