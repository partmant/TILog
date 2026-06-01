package com.tilog.controller;

import com.tilog.dto.admin.MemberRoleChangeRequest;
import com.tilog.dto.admin.AdminMemberDetailResponse;
import com.tilog.dto.admin.AdminMemberListResponse;
import com.tilog.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
