package com.tilog.dto.response;

import com.tilog.entity.Status;
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
    private int technicalScore;
    private int flowScore;
    private int designScore;
    private String comment;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}
