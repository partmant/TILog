package com.tilog.domain.report.entity;

import com.tilog.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long processId;

    private Long targetId;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Member admin;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    private TargetAction targetAction;

    private String processContent;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public ReportProcess(Long targetId, Member admin, TargetType targetType, TargetAction targetAction, String processContent) {
        this.targetId = targetId;
        this.admin = admin;
        this.targetType = targetType;
        this.targetAction = targetAction;
        this.processContent = processContent;
        this.createdAt = LocalDateTime.now();
    }
}
