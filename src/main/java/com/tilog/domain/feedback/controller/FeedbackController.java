package com.tilog.domain.feedback.controller;

import com.tilog.domain.feedback.dto.FeedbackRequestDtoRequest;
import com.tilog.domain.feedback.dto.FeedbackWriteRequestDto;
import com.tilog.domain.feedback.dto.FeedbackDetailResponseDto;
import com.tilog.domain.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
