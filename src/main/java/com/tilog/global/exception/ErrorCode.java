package com.tilog.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    COMMENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "댓글 수정/삭제 권한이 없습니다."),
    COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."),
    PARENT_COMMENT_POST_MISMATCH(HttpStatus.BAD_REQUEST, "부모 댓글이 해당 게시글에 속하지 않습니다."),

    // Like
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요한 게시글입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요 내역을 찾을 수 없습니다."),

    // Follow
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
    ALREADY_FOLLOWING(HttpStatus.CONFLICT, "이미 팔로우한 사용자입니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 내역을 찾을 수 없습니다."),
    TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 대상 회원을 찾을 수 없습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    NOTIFICATION_UNAUTHORIZED(HttpStatus.FORBIDDEN, "알림 접근 권한이 없습니다."),

    // Subscription
    SUBSCRIPTION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 활성화된 구독이 존재합니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "활성 구독 내역을 찾을 수 없습니다."),

    // Payback
    // Payback
    PAYBACK_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 페이백 정책입니다."),
    ACTIVE_PAYBACK_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "현재 활성화된 페이백 정책이 없습니다."),
    INACTIVE_PAYBACK_POLICY(HttpStatus.BAD_REQUEST, "비활성화된 페이백 정책입니다."),
    PAYBACK_POLICY_PERIOD_MISMATCH(HttpStatus.BAD_REQUEST, "페이백 정책 기간에 포함되지 않는 참여 기간입니다."),
    PAYBACK_ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "이미 해당 구독 회차의 페이백 참여 내역이 존재합니다."),
    PAYBACK_PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "진행 중인 페이백 참여 내역이 없습니다."),

    // Common
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
