package com.tilog.service;

import com.tilog.dto.notification.NotificationResponse;
import com.tilog.entity.member.Member;
import com.tilog.entity.notification.Notification;
import com.tilog.entity.notification.NotificationType;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.repository.MemberRepository;
import com.tilog.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    /**
     * 알림 발송 — CommentService, LikeService, FollowService에서 호출
     * 자기 자신에게는 알림 발송하지 않음
     */
    @Transactional
    public void send(Long receiverId, Long senderId, NotificationType type,
                     Long relatedEntityId, String relatedEntityType) {

        // 자기 자신 알림 방지
        if (receiverId.equals(senderId)) return;

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String message = buildMessage(sender.getNickname(), type);

        notificationRepository.save(
                Notification.create(receiver, sender, type, message, relatedEntityId, relatedEntityType)
        );
    }

    /** 내 알림 목록 (최신순, 슬라이스 페이징) */
    public List<NotificationResponse> getMyNotifications(int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Slice<Notification> notifications = notificationRepository
                .findByReceiver_IdOrderByCreatedAtDesc(memberId, PageRequest.of(page, size));

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /** 안 읽은 알림 수 */
    public long getUnreadCount() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return notificationRepository.countByReceiver_IdAndIsReadFalse(memberId);
    }

    /** 알림 단건 읽음 처리 */
    @Transactional
    public void markAsRead(Long notificationId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notification.read();
    }

    /** 전체 읽음 처리 */
    @Transactional
    public void markAllAsRead() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationRepository.markAllAsRead(memberId);
    }

    private String buildMessage(String senderNickname, NotificationType type) {
        return switch (type) {
            case COMMENT -> senderNickname + "님이 회원님의 TIL에 댓글을 남겼습니다.";
            case LIKE    -> senderNickname + "님이 회원님의 TIL을 좋아합니다.";
            case FOLLOW  -> senderNickname + "님이 회원님을 팔로우하기 시작했습니다.";
        };
    }
}
