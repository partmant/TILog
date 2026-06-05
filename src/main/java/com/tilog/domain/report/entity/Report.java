package com.tilog.domain.report.entity;

import com.tilog.domain.feedback.entity.Status;
import com.tilog.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    private ReasonType reasonType;

    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public void completeReport(){
        this.status = Status.PROCESSED;
    }
}

