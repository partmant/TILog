package com.tilog.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackWriteRequestDto {
    private Long mentorId;
    private int technicalScore;
    private int flowScore;
    private int designScore;
    private String comment;
}
