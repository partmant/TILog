package com.tilog.domain.notification.entity;

public enum NotificationType {
    COMMENT,   // 댓글 알림
    LIKE,      // 좋아요 알림
    FOLLOW,    // 팔로우 알림
    FEEDBACK,  // 피드백 알림
    AI_REPORT  // AI 주간 리포트 생성 완료 (시스템 알림, sender=null)
}
