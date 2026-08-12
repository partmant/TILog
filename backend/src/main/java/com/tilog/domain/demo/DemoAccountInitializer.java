package com.tilog.domain.demo;

import com.tilog.domain.member.entity.CurrentStatus;
import com.tilog.domain.member.entity.Member;
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
 * 포트폴리오 공개 데모용 고정 계정을 서버 기동 시점에 준비한다.
 *
 * <p>TILog는 회원가입 시 이메일 인증 절차가 없어(로그인은 이메일+비밀번호만 검증) 별도의
 * "인증 우회" 처리는 필요 없다. 다만 방문자가 매번 회원가입을 거치지 않고 바로 로그인해
 * 글쓰기·스트릭·구독(Mock)·AI 리포트를 체험할 수 있도록 고정 계정을 미리 만들어 둔다.
 *
 * <p>demo.enabled=true (기본값 false)일 때만 동작하며, demo.account.password가 비어 있으면
 * 안전을 위해 계정 생성을 건너뛴다. 이미 존재하는 이메일이면 다시 만들지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoAccountInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${demo.account.email:demo@tilog.kr}")
    private String demoEmail;

    @Value("${demo.account.password:}")
    private String demoPassword;

    @Value("${demo.account.nickname:데모_방문자}")
    private String demoNickname;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (demoPassword == null || demoPassword.isBlank()) {
            log.warn("데모 계정 비밀번호(demo.account.password)가 설정되지 않아 계정 생성을 건너뜁니다.");
            return;
        }
        if (memberRepository.existsByEmail(demoEmail)) {
            return;
        }

        // Member.create()는 role을 별도로 지정하지 않으면 기본 USER로 생성한다.
        // 구독(Mock) 체험 흐름을 보여줘야 하므로 PREMIUM이 아닌 일반 USER로 시작해야 한다.
        Member demoMember = Member.create(
                demoEmail,
                passwordEncoder.encode(demoPassword),
                demoNickname,
                CurrentStatus.JOB_SEEKER,
                TargetJob.BACKEND
        );

        memberRepository.save(demoMember);
        log.info("데모 계정 생성 완료: {}", demoEmail);
    }
}
