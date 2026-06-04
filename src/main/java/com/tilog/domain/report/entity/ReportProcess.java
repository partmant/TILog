package com.tilog.domain.report.entity;

import com.tilog.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
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
    private LocalDateTime createdAt;
}
