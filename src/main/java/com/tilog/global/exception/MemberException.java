package com.tilog.global.exception;

/**
 * 회원 도메인 관련 공통 예외
 * - 전역 예외 처리기에서 잡아 일관된 응답으로 변환할 수 있도록 RuntimeException 상속
 */
public class MemberException extends RuntimeException {
    public MemberException(String message) {
        super(message);
    }
}
