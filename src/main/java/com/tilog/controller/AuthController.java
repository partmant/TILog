package com.tilog.controller;

import com.tilog.dto.auth.LoginRequest;
import com.tilog.dto.auth.SignUpRequest;
import com.tilog.dto.auth.TokenResponse;
import com.tilog.dto.member.MemberResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.service.AuthService;
import com.tilog.service.MemberService;
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
