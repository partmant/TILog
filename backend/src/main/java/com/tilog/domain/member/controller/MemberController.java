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
        // 멘토가 피드백 요청 모달을 열 때 자기 자신이 담당 멘토 목록에 뜨지 않도록 제외한다.
        // 비로그인 상태에서도 호출 가능한 엔드포인트라 인증 정보가 없으면 제외 없이 전체 반환한다.
        Long currentMemberId = null;
        try {
            currentMemberId = SecurityUtil.getCurrentMemberId();
        } catch (Exception ignored) {
        }

        List<MemberResponse> mentors = memberService.getMentors(currentMemberId);
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
