package com.tilog.service;

import com.tilog.dto.auth.SignUpRequest;
import com.tilog.dto.member.MemberResponse;
import com.tilog.entity.Member;
import com.tilog.global.exception.MemberException;
import com.tilog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new MemberException("이미 사용 중인 닉네임입니다.");
        }

        Member member = Member.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname()
        );

        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }
}
