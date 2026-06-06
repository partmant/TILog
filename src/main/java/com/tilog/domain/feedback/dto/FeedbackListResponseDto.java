package com.tilog.domain.feedback.dto;

import com.tilog.domain.feedback.entity.MentorFeedback;
import com.tilog.domain.feedback.entity.Status;
import java.time.format.DateTimeFormatter;

public record FeedbackListResponseDto(
        Long id,
        String type,           // "REQUESTED" (내가 요청함) or "RECEIVED" (멘토로서 받음)
        String postTitle,
        Status status,         // WAITING or COMPLETED
        String mentorName,
        String requesterName,
        String date
) {
    public static FeedbackListResponseDto from(MentorFeedback feedback, String type) {
        return new FeedbackListResponseDto(
                feedback.getFeedbackId(),
                type,
                feedback.getTil().getTitle(),
                feedback.getStatus(),
                feedback.getMentor().getNickname(),
                feedback.getRequestor().getNickname(),
                feedback.getRequestedAt() != null ? feedback.getRequestedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) : ""
        );
    }
}