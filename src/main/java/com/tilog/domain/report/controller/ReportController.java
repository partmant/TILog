package com.tilog.domain.report.controller;

import com.tilog.domain.report.dto.ReportCreateRequestDto;
import com.tilog.domain.report.service.ReportService;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> submitReport(@RequestBody ReportCreateRequestDto requestDto) {
        Long currentUserId = SecurityUtil.getCurrentMemberId();
        reportService.createReport(currentUserId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created 반환
    }
}