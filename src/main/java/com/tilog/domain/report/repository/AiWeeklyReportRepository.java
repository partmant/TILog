package com.tilog.domain.report.repository;

import com.tilog.domain.report.entity.AiWeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AiWeeklyReportRepository extends JpaRepository<AiWeeklyReport, Long> {

    /** 특정 주간 리포트 단건 조회 (중복 저장 방지 및 조회용) */
    Optional<AiWeeklyReport> findByMemberIdAndWeekStartDate(Long memberId, LocalDate weekStartDate);

    /** 이번 주 시작일보다 이전인 리포트 중 가장 최근 것 (지난주 비교용) */
    Optional<AiWeeklyReport> findTopByMemberIdAndWeekStartDateBeforeOrderByWeekStartDateDesc(
            Long memberId, LocalDate weekStartDate);
}