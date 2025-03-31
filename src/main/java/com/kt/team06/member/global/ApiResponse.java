package com.kt.team06.member.global;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청이 성공했습니다.", data, LocalDateTime.now());
    }

    public static ApiResponse<?> error(int code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
}