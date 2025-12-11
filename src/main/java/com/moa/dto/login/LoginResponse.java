package com.moa.dto.login;

import com.moa.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long userId;
    private String provider;
    private String userName;
    private String userEmail;
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
    private String message;

    public static LoginResponse from(User user, String provider, String accessToken, String refreshToken, boolean isNewUser) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .provider(provider)
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .message(isNewUser ? "회원가입 및 로그인 성공" : "로그인 성공")
                .build();
    }
}
