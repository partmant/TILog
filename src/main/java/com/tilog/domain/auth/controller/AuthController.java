package com.tilog.domain.auth.controller;

import com.tilog.domain.auth.dto.LoginRequest;
import com.tilog.domain.auth.dto.SignUpRequest;
import com.tilog.domain.auth.dto.TokenResponse;
import com.tilog.domain.member.dto.MemberResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.auth.service.AuthService;
import com.tilog.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);
        return ApiResponse.success(response, "회원가입 성공");
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ApiResponse.success(response, "로그인 성공");
    }
}
