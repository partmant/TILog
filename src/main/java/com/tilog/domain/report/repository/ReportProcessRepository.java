package com.tilog.domain.report.repository;

import com.tilog.domain.report.entity.ReportProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportProcessRepository extends JpaRepository<ReportProcess, Long> {
}
