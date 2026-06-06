package com.tilog.domain.report.dto;

import com.tilog.domain.report.entity.TargetType;
import com.tilog.domain.report.entity.ReasonType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequestDto {
    private TargetType targetType;
    private Long targetId;
    private ReasonType reasonType;
    private String reasonDetail;
}