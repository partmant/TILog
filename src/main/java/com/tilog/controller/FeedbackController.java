package com.tilog.controller;

import com.tilog.dto.feedback.FeedbackRequestDtoRequest;
import com.tilog.dto.feedback.FeedbackWriteRequestDto;
import com.tilog.dto.feedback.FeedbackDetailResponseDto;
import com.tilog.service.FeedbackService;
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
