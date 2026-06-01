package com.tilog.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequestDtoRequest {
    private Long tilId;
    private Long requestorId;
    private Long mentorId;
}
