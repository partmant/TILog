package com.tilog.domain.member.entity;

import com.tilog.domain.report.entity.ReasonType;
import com.tilog.domain.report.entity.ReportProcess;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSanction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sanctionId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Member admin;

    @ManyToOne
    @JoinColumn(name = "process_id")
    private ReportProcess process;

    @Enumerated(EnumType.STRING)
    private SanctionType sanctionType;

    @Enumerated(EnumType.STRING)
    private ReasonType reasonType;

    private String content;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;


    public MemberSanction(Member member, Member admin, SanctionType sanctionType, ReasonType reasonType, String content){
        this.member = member;
        this.admin = admin;
        this.sanctionType = sanctionType;
        this.reasonType = reasonType;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.startAt = LocalDateTime.now();
    }
}
