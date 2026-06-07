package com.tilog.domain.member.controller;

import com.tilog.domain.member.dto.MemberResponse;
import com.tilog.domain.member.dto.UpdateProfileRequest;
import com.tilog.domain.member.service.MemberService;
import com.tilog.global.response.ApiResponse;
import com.tilog.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 내 정보 조회 (프로필 이미지 URL 포함)
    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMyInfo() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.success(memberService.getMyInfo(memberId));
    }

    // 프로필 이미지 업로드/수정
    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MemberResponse> updateProfileImage(
            @RequestParam("image") MultipartFile image
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.success(memberService.updateProfileImage(memberId, image));
    }

    @GetMapping("/mentors")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMentors(){
        List<MemberResponse> mentors = memberService.getMentors();
        return ResponseEntity.ok(ApiResponse.success(mentors));
    }

    @PatchMapping("/me")
    public ApiResponse<MemberResponse> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ApiResponse.success(memberService.updateProfile(memberId, request));
    }
}
