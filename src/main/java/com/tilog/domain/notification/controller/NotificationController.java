package com.tilog.domain.notification.controller;

import com.tilog.domain.notification.dto.NotificationResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.notification.service.NotificationService;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 내 알림 목록 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<NotificationResponse> response = notificationService.getMyNotifications(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 안 읽은 알림 수 */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /** 알림 단건 읽음 처리 */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("읽음 처리되었습니다."));
    }

    /** 전체 읽음 처리 */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("전체 읽음 처리되었습니다."));
    }

    /** 알림 단건 삭제 */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.<Void>success("알림이 삭제되었습니다."));
    }

    /** 알림 전체 삭제 */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications() {
        notificationService.deleteAllNotifications();
        return ResponseEntity.ok(ApiResponse.<Void>success("전체 알림이 삭제되었습니다."));
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(){  // 클라이언트 실시간 알림 구독(SSE 연결)
        Long memberId = SecurityUtil.getCurrentMemberId();
        SseEmitter sseEmitter = notificationService.subscribe(memberId);
        return ResponseEntity.ok(sseEmitter);
    }
}