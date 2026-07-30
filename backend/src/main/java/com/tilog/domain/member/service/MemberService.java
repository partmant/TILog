package com.tilog.domain.member.service;

import com.tilog.domain.auth.dto.SignUpRequest;
import com.tilog.domain.member.dto.MemberResponse;
import com.tilog.domain.member.dto.UpdateProfileRequest;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.global.exception.MemberException;
import com.tilog.domain.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload-path}")
    private String uploadPath;

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
                request.nickname(),
                request.currentStatus(),
                request.targetJob()
        );

        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }

    // 내 정보 조회
    public MemberResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateProfile(Long memberId, @Valid UpdateProfileRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));

        // 닉네임 중복 체크 (본인 제외)
        if (request.nickname() != null && !request.nickname().isBlank()
                && !member.getNickname().equals(request.nickname())
                && memberRepository.existsByNickname(request.nickname())) {
            throw new MemberException("이미 사용 중인 닉네임입니다.");
        }

        member.updateProfile(request.nickname(), request.currentStatus(), request.targetJob());
        return MemberResponse.from(member);
    }

    // 프로필 이미지 업데이트
    @Transactional
    public MemberResponse updateProfileImage(Long memberId, MultipartFile image) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));

        String imageUrl = saveProfileImage(image);
        member.updateProfileImage(imageUrl);

        return MemberResponse.from(member);
    }

    private String saveProfileImage(MultipartFile image) {
        try {
            String originalName = image.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
            }

            String extension = extractExtension(originalName);
            String storedName = UUID.randomUUID() + extension;

            Path directory = Paths.get(uploadPath, "profile");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path savePath = directory.resolve(storedName);
            image.transferTo(savePath);

            return "/uploads/profile/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 업로드 실패", e);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public List<MemberResponse> getMentors() {  // 멘토 권한 가진 사용자 목록 조회
        return memberRepository.findByRole(MemberRole.MENTOR)
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}
