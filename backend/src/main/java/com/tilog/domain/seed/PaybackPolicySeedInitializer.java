package com.tilog.domain.seed;

import com.tilog.domain.payback.entity.PaybackPolicy;
import com.tilog.domain.payback.repository.PaybackPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 서버 최초 기동 시점에 활성 페이백 정책이 하나도 없으면 기본 정책을 하나 생성한다.
 *
 * <p>PaybackPolicy는 관리자 화면/API가 없어 DB에 직접 넣는 것 외에는 생성 경로가 없다.
 * 정책이 하나도 없는 상태에서는 신규 구독(SubscriptionService#subscribe)이 활성 정책
 * 조회 실패로 트랜잭션 전체가 롤백되어, 데모 계정을 포함한 어떤 회원도 구독을 시작할
 * 수 없다. 배포 환경에서도 별도 설정 없이 정상 동작하도록 기동 시점에 기본값을 채운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "seed.enabled", havingValue = "true", matchIfMissing = true)
public class PaybackPolicySeedInitializer implements ApplicationRunner {

    private final PaybackPolicyRepository paybackPolicyRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (paybackPolicyRepository.count() > 0) {
            log.info("페이백 정책이 이미 존재하여 기본 정책 생성을 건너뜁니다.");
            return;
        }

        PaybackPolicy policy = PaybackPolicy.create(
                "기본 페이백 정책",
                20,
                9900,
                9900,
                LocalDate.now().minusDays(1),
                null,
                true
        );
        paybackPolicyRepository.save(policy);

        log.info("활성 페이백 정책이 없어 기본 정책을 생성했습니다: {}", policy.getName());
    }
}
