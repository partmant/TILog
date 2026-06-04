package com.tilog.domain.admin.dto;

import com.tilog.domain.member.entity.SanctionType;
import com.tilog.domain.report.entity.ReasonType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSanctionRequestDto {
    private SanctionType sanctionType;
    private ReasonType reasonType;
    private String content;
}
