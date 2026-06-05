package com.tilog.domain.admin.controller;

import com.tilog.domain.admin.dto.MemberRoleChangeRequest;
import com.tilog.domain.admin.dto.AdminMemberDetailResponse;
import com.tilog.domain.admin.dto.AdminMemberListResponse;
import com.tilog.domain.admin.dto.MemberSanctionRequestDto;
import com.tilog.domain.admin.service.AdminService;
import com.tilog.domain.report.dto.ReportResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/members")
    public ResponseEntity<Page<AdminMemberListResponse>> getMemberList(Pageable pageable){  // 회원 목록 페이징 조회
        Page<AdminMemberListResponse> memberList = adminService.getMemberList(pageable);
        return ResponseEntity.ok(memberList);
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<AdminMemberDetailResponse> getMemberDetail(@PathVariable Long memberId) { // 회원 상세 조회
        AdminMemberDetailResponse member = adminService.getMemberDetail(memberId);
        return ResponseEntity.ok(member);
    }

    @PatchMapping("/members/role")
    public ResponseEntity<Void> changeMemberRole(@RequestBody MemberRoleChangeRequest memberRoleChangeRequest) {  // 회원 권한 변경
        adminService.changeMemberRole(memberRoleChangeRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> forceDeletePost(@PathVariable Long postId){ // 게시글 강제 삭제
        adminService.forceDeletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{reportId}/sanction")
    public ResponseEntity<Void> doSanction(@PathVariable Long reportId, @RequestBody MemberSanctionRequestDto memberSanctionRequestDto){ // 제재
        adminService.doSanction(reportId, memberSanctionRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/reports/recent")
    public ResponseEntity<List<ReportResponseDto>> getRecentReports() {
        List<ReportResponseDto> response = adminService.getRecentReports();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/stats")
    public ResponseEntity<Map<String, Long>> getReportStats() {
        System.out.println("🚨 신고 통계 API가 호출되었습니다!"); // 🔥 이 로그가 콘솔에 찍히나요?
        Map<String, Long> response = adminService.getReportStatistics();
        return ResponseEntity.ok(response);
    }
}
