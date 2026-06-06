package com.tilog.domain.report.entity;

import com.tilog.domain.feedback.entity.Status;
import com.tilog.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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


    @Builder
    public Report(Member reporter, Long targetId, TargetType targetType,
                  ReasonType reasonType, String reasonDetail,
                  Status status, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.reporter = reporter;
        this.targetId = targetId;
        this.targetType = targetType;
        this.reasonType = reasonType;
        this.reasonDetail = reasonDetail;
        this.status = status;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.processedAt = processedAt;
    }

    public void completeReport(){
        this.status = Status.PROCESSED;
    }
}

