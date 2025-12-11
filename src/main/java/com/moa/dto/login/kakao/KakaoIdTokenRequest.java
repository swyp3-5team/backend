package com.moa.dto.login.kakao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 OIDC 로그인 요청 DTO
 * 앱에서 발급받은 ID Token을 서버로 전달
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoIdTokenRequest {
    /**
     * 카카오 SDK에서 발급받은 ID Token
     */
    private String idToken;

    /**
     * 디바이스 ID (선택)
     */
    private String deviceId;
}
