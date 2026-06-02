package com.tilog.service;

import com.tilog.dto.auth.LoginRequest;
import com.tilog.dto.auth.TokenResponse;
import com.tilog.entity.member.Member;
import com.tilog.global.exception.AuthException;
import com.tilog.global.security.JwtTokenProvider;
import com.tilog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

  // 로그인
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        if (member.isCurrentlyBanned()) {
            throw new AuthException("정지된 계정입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRole());
        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenValiditySeconds()
        );
    }
}
