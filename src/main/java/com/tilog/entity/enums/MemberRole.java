package com.tilog.entity.enums;

/**
 * 회원 권한
 * - USER    : 일반 사용자
 * - PREMIUM : 프리미엄 사용자 (AI 리포트, 멘토 피드백, 페이백 챌린지 이용 가능)
 * - MENTOR  : 멘토 (피드백 작성)
 * - ADMIN   : 관리자
 */
public enum MemberRole {
    USER,
    PREMIUM,
    MENTOR,
    ADMIN
}
