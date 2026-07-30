package com.tilog.domain.notification.service;

import com.tilog.domain.notification.dto.NotificationResponse;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.notification.entity.Notification;
import com.tilog.domain.notification.entity.NotificationType;
import com.tilog.domain.notification.repository.EmitterRepository;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final EmitterRepository emitterRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // SSE 통신 유효 시간: 1시간

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

        Notification notification = notificationRepository.save(
                Notification.create(receiver, sender, type, message, relatedEntityId, relatedEntityType)
        );

        sendToClient(receiverId, NotificationResponse.from(notification));
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

    public SseEmitter subscribe(Long memberId) {    // client의 SSE 구독 처리
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(memberId, emitter);

        emitter.onCompletion(() -> emitterRepository.deleteById(memberId));
        emitter.onTimeout(() -> emitterRepository.deleteById(memberId));
        emitter.onError((e) -> emitterRepository.deleteById(memberId));

        sendToClient(memberId, "EventStream Created. [userId=" + memberId + "]");

        return emitter;
    }

    private void sendToClient(Long receiverId, Object data) {   // 특정 회원에게 데이터 실제 전송
        SseEmitter emitter = emitterRepository.get(receiverId);

        if(emitter != null){
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(receiverId))
                        .name("sse")
                        .data(data));
            } catch (IOException exception){
                emitterRepository.deleteById(receiverId);
            }
        }
    }

    /** 전체 읽음 처리 */
    @Transactional
    public void markAllAsRead() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationRepository.markAllAsRead(memberId);
    }

    /** 알림 단건 삭제 */
    @Transactional
    public void deleteNotification(Long notificationId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOTIFICATION_UNAUTHORIZED);
        }

        notificationRepository.delete(notification);
    }

    /** 알림 전체 삭제 */
    @Transactional
    public void deleteAllNotifications() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationRepository.deleteAllByReceiverId(memberId);
    }

    /**
     * 시스템 알림 발송 — sender 없는 서버 발신 알림 (AI 리포트 완료 등)
     */
    @Transactional
    public void sendSystem(Long receiverId, NotificationType type,
                           String message, Long relatedEntityId, String relatedEntityType) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = notificationRepository.save(
                Notification.create(receiver, null, type, message, relatedEntityId, relatedEntityType));

        sendToClient(receiverId, NotificationResponse.from(notification));
    }

    private String buildMessage(String senderNickname, NotificationType type) {
        return switch (type) {
            case COMMENT  -> senderNickname + "님이 회원님의 TIL에 댓글을 남겼습니다.";
            case LIKE     -> senderNickname + "님이 회원님의 TIL을 좋아합니다.";
            case FOLLOW   -> senderNickname + "님이 회원님을 팔로우하기 시작했습니다.";
            case FEEDBACK -> senderNickname + " 멘토님이 회원님의 TIL에 피드백을 남겼습니다!";
            case AI_REPORT -> "주간 성장 리포트가 생성되었습니다."; // sendSystem() 경로에서는 호출 안 됨
        };
    }
}