package com.tilog.domain.report.service;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.report.dto.ReportCreateRequestDto;
import com.tilog.domain.report.entity.Report;
import com.tilog.domain.feedback.entity.Status;
import com.tilog.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createReport(Long reporterId, ReportCreateRequestDto dto) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .reasonType(dto.getReasonType())
                .reasonDetail(dto.getReasonDetail())
                .status(Status.PENDING)
                .build();

        // 3. DB에 저장
        reportRepository.save(report);
    }
}