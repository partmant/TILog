package com.tilog.domain.report.dto;

import com.tilog.domain.report.entity.Report; // (본인 프로젝트의 Report 엔티티 경로로 맞춰주세요)

public record ReportResponseDto(
        Long reportId,
        String reasonType,
        String reasonDetail,
        String status,
        String reporterNickname,
        String reportedNickname
) {
    // 엔티티를 DTO로 변환하는 마법의 메서드
    public static ReportResponseDto of(Report report, String reporterNickname, String reportedNickname) {
        return new ReportResponseDto(
                report.getReportId(),
                report.getReasonType().name(),
                report.getReasonDetail(),
                report.getStatus().name(),
                reporterNickname,
                reportedNickname
        );
    }
}