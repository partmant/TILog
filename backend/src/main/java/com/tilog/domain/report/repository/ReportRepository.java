package com.tilog.domain.report.repository;

import com.tilog.domain.feedback.entity.Status;
import com.tilog.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findTop4ByOrderByCreatedAtDesc();  // 최신 신고 내역 4건

    long countByStatus(Status status);
}
