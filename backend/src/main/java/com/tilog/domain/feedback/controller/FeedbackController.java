package com.tilog.domain.feedback.controller;

import com.tilog.domain.feedback.dto.FeedbackListResponseDto;
import com.tilog.domain.feedback.dto.FeedbackRequestDtoRequest;
import com.tilog.domain.feedback.dto.FeedbackWriteRequestDto;
import com.tilog.domain.feedback.dto.FeedbackDetailResponseDto;
import com.tilog.domain.feedback.service.FeedbackService;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Void> requestFeedback(@RequestBody FeedbackRequestDtoRequest feedbackRequestDtoRequest){  // 피드백 요청
        feedbackService.requestFeedback(feedbackRequestDtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{feedbackId}")
    public ResponseEntity<Void> writeFeedback(@PathVariable Long feedbackId, @RequestBody FeedbackWriteRequestDto feedbackWriteRequestDto){ // 피드백 작성
        feedbackService.writeFeedback(feedbackId, feedbackWriteRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackDetailResponseDto> getFeedbackDetail(@PathVariable Long feedbackId){  // 피드백 상세 조회
        FeedbackDetailResponseDto feedbackDetail = feedbackService.getFeedbackDetail(feedbackId);
        return ResponseEntity.ok(feedbackDetail);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackListResponseDto>> getFeedbackList() {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        MemberRole role = feedbackService.getMemberRole(loginMemberId); // 서비스에 Role 조회 메서드 간단히 추가 필요

        List<FeedbackListResponseDto> list = feedbackService.getFeedbackList(loginMemberId, role);
        return ResponseEntity.ok(list);
    }
}
