package com.tilog.repository;

import com.tilog.entity.ReportProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportProcessRepository extends JpaRepository<ReportProcess, Long> {
}
