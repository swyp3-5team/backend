package com.moa.dto;

public record MessageResponse<T>(
        String message,
        T data
) {
    public static <T> MessageResponse<T> of(String message, T data){
        return new MessageResponse<>(message,data);
    }
}
