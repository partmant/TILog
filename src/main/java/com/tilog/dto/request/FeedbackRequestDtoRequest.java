package com.tilog.dto.request;

import com.tilog.entity.Member;
import com.tilog.entity.Status;
import com.tilog.entity.TilPost;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequestDtoRequest {
    private Long tilId;
    private Long requestorId;
    private Long mentorId;
}
