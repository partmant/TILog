package com.tilog.domain.feedback.dto;

import com.tilog.domain.feedback.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FeedbackDetailResponseDto {
    private Long feedbackId;
    private Long tilId;
    private Long mentorId;

    private Status status;
    private Integer technicalScore;
    private Integer flowScore;
    private Integer designScore;
    private String comment;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}
