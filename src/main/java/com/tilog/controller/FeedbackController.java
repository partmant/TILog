package com.tilog.controller;

import com.tilog.dto.request.FeedbackRequestDtoRequest;
import com.tilog.dto.request.FeedbackWriteRequestDto;
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
    public ResponseEntity<Void> requestFeedback(@RequestBody FeedbackRequestDtoRequest feedbackRequestDtoRequest){
        feedbackService.requestFeedback(feedbackRequestDtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{feedbackId}")
    public ResponseEntity<Void> writeFeedback(@PathVariable Long feedbackId, @RequestBody FeedbackWriteRequestDto feedbackWriteRequestDto){
        feedbackService.writeFeedback(feedbackId, feedbackWriteRequestDto);
        return ResponseEntity.ok().build();
    }

}
