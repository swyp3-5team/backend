package com.moa.exception;

/**
 * 이미지 검증 실패 시 발생하는 예외
 */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
