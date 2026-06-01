package com.tilog.global.security;

import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    /**
     * SecurityContext에서 현재 로그인한 회원의 memberId를 반환합니다.
     * JWT 필터에서 authentication.setName(memberId)로 설정해야 동작합니다.
     * 1번 담당자의 JWT 구현 방식에 따라 조정이 필요할 수 있습니다.
     */
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
