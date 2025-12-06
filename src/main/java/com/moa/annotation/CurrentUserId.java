package com.moa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 로그인한 사용자의 ID를 자동으로 주입하는 어노테이션
 *
 * 사용 예시:
 * public ResponseEntity<?> getMyInfo(@CurrentUserId Long userId) {
 *     // userId는 JWT 토큰에서 자동으로 추출됨
 * }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
