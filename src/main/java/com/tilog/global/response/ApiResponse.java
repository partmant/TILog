package com.tilog.global.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    // 성공 응답 (데이터 미포함)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // 실패 응답
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
