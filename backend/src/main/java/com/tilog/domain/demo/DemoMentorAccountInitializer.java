package com.tilog.domain.demo;

import com.tilog.domain.member.entity.CurrentStatus;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.member.entity.TargetJob;
import com.tilog.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포트폴리오 공개 데모에서 "피드백 요청 -> 멘토 답변" 흐름을 양쪽 다 체험/테스트할 수 있도록,
 * 로그인 가능한 고정 멘토 계정을 서버 기동 시점에 준비한다.
 *
 * <p>demo.account.email(체험용 일반 회원)과는 완전히 분리된 별도 계정이다. 멘토 계정은
 * TIL/스트릭 데이터가 쌓이지 않아 매일 자정 초기화 대상에 포함할 필요가 없으므로,
 * DemoDataResetService/DemoDataResetScheduler와는 무관하게 한 번 생성되면 계속 유지된다.
 *
 * <p>demo.enabled=true이고 demo.mentor.password가 채워져 있을 때만 동작하며,
 * 이미 존재하는 이메일이면 다시 만들지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoMentorAccountInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${demo.mentor.email:mentor@tilog.kr}")
    private String mentorEmail;

    @Value("${demo.mentor.password:}")
    private String mentorPassword;

    @Value("${demo.mentor.nickname:데모_멘토}")
    private String mentorNickname;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (mentorPassword == null || mentorPassword.isBlank()) {
            log.warn("데모 멘토 계정 비밀번호(demo.mentor.password)가 설정되지 않아 계정 생성을 건너뜁니다.");
            return;
        }
        if (memberRepository.existsByEmail(mentorEmail)) {
            return;
        }

        Member mentorMember = Member.create(
                mentorEmail,
                passwordEncoder.encode(mentorPassword),
                mentorNickname,
                CurrentStatus.EMPLOYED,
                TargetJob.BACKEND
        );
        mentorMember.changeRole(MemberRole.MENTOR);

        memberRepository.save(mentorMember);
        log.info("데모 멘토 계정 생성 완료: {}", mentorEmail);
    }
}
