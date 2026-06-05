package com.tilog.global.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    // 성공 응답 (메시지 기본값)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }

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
