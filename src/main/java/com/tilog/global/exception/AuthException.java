package com.tilog.global.exception;

/**
 * 인증/인가 관련 예외 (잘못된 비밀번호, 만료된 토큰 등)
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
