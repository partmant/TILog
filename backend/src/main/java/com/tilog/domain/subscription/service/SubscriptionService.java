package com.tilog.domain.subscription.service;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.payback.entity.PaybackPolicy;
import com.tilog.domain.payback.service.PaybackParticipationService;
import com.tilog.domain.payback.service.PaybackPolicyService;
import com.tilog.domain.subscription.dto.SubscriptionHistoryResponse;
import com.tilog.domain.subscription.dto.SubscriptionStatusResponse;
import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.domain.subscription.entity.SubscriptionStatus;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mock 구독 서비스
 *
 * [흐름 요약]
 * 1. subscribe()                  : 유효 구독 없으면 신규 생성(ACTIVE) + PREMIUM 부여
 *                                    유효 구독 있으면(ACTIVE/CANCEL_RESERVED) endedAt 1달 연장 → ACTIVE 복원
 * 2. cancel()                     : ACTIVE → CANCEL_RESERVED (endedAt 유지, 기간 만료까지 PREMIUM/페이백 유지)
 * 3. getStatus()                  : 현재 로그인 회원의 구독 상태 반환
 * 4. getHistory()                 : 전체 구독 이력 반환
 * 5. processExpiredSubscriptions(): [스케줄러 호출용]
 *    - CANCEL_RESERVED 만료 → EXPIRED + 멤버 역할 USER 복원
 *    - ACTIVE 만료 → EXPIRED 후 신규 구독 자동 생성(갱신)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final PaybackPolicyService paybackPolicyService;
    private final PaybackParticipationService paybackParticipationService;

    // Mock 구독 신청
    // - 유효한 구독(ACTIVE/CANCEL_RESERVED)이 있으면 endedAt을 1달 연장
    // - 없으면 신규 구독 생성 (30일 ACTIVE → PREMIUM 권한 부여)
    @Transactional
    public SubscriptionStatusResponse subscribe() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<Subscription> existingOpt = subscriptionRepository
                .findCurrentActiveSubscriptions(memberId, now, PageRequest.of(0, 1))
                .stream()
                .findFirst();

        if (existingOpt.isPresent()) {
            // 기존 구독 기간에서 1달 연장
            Subscription existing = existingOpt.get();
            existing.extend();
            paybackParticipationService.extendForSubscription(
                    existing.getId(),
                    existing.getEndedAt().toLocalDate()
            );
            member.changeRole(MemberRole.PREMIUM);
            return SubscriptionStatusResponse.from(existing);
        }

        // 신규 구독 생성
        Subscription subscription = Subscription.createMock(member);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // 활성 페이백 정책이 없어도 구독 자체는 막히면 안 되므로,
        // processExpiredSubscriptions()의 자동 갱신과 동일하게 실패를 흡수하고 구독만 진행한다.
        try {
            PaybackPolicy policy = paybackPolicyService.getCurrentActivePolicy(LocalDate.now());
            paybackParticipationService.createForSubscription(
                    memberId,
                    savedSubscription,
                    policy
            );
        } catch (Exception e) {
            log.warn("[신규 구독] 활성 페이백 정책 없음 - 구독만 진행됨: memberId={}", memberId);
        }

        member.changeRole(MemberRole.PREMIUM);

        return SubscriptionStatusResponse.from(savedSubscription);
    }

    // 구독 취소 예약 (ACTIVE → CANCEL_RESERVED)
    // endedAt 유지 → 기간 만료까지 PREMIUM 혜택 및 페이백 유지
    @Transactional
    public SubscriptionStatusResponse cancel() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Subscription> activeSubscriptions = subscriptionRepository.findAllActiveByMemberId(memberId);

        if (activeSubscriptions.isEmpty()) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        Subscription latest = activeSubscriptions.get(0);

        // endedAt은 유지, 상태만 CANCEL_RESERVED로 변경
        // 멤버 역할(PREMIUM) 및 페이백은 구독 기간 만료 시까지 유지
        activeSubscriptions.forEach(Subscription::cancel);

        return SubscriptionStatusResponse.from(latest);
    }

    // 현재 구독 상태 조회
    public SubscriptionStatusResponse getMyStatus() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return subscriptionRepository.findCurrentActiveSubscriptions(
                        memberId,
                        now,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .map(SubscriptionStatusResponse::from)
                .orElseGet(() -> SubscriptionStatusResponse.noSubscription(member.getNickname()));
    }

    // 구독 이력 전체 조회
    public List<SubscriptionHistoryResponse> getMyHistory() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return subscriptionRepository.findByMemberIdOrderByStartedAtDesc(memberId)
                .stream()
                .map(SubscriptionHistoryResponse::from)
                .toList();
    }

    // [스케줄러 호출용] 만료 구독 일괄 처리
    // - CANCEL_RESERVED 만료 → EXPIRED + 멤버 역할 USER 복원
    // - ACTIVE 만료 → EXPIRED 후 신규 구독 자동 갱신
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(now);

        for (Subscription subscription : expired) {
            Member member = subscription.getMember();

            if (subscription.getStatus() == SubscriptionStatus.CANCEL_RESERVED) {
                // 취소 예약 만료: EXPIRED 처리 + 역할 복원
                subscription.expire();
                member.changeRole(MemberRole.USER);
                log.info("[구독 만료] memberId={} CANCEL_RESERVED → EXPIRED", member.getId());

            } else if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                // 자동 갱신: 기존 구독 EXPIRED 처리 후 신규 구독 생성
                subscription.expire();

                Subscription newSubscription = Subscription.createMock(member);
                subscriptionRepository.save(newSubscription);

                try {
                    PaybackPolicy policy = paybackPolicyService.getCurrentActivePolicy(LocalDate.now());
                    paybackParticipationService.createForSubscription(
                            member.getId(),
                            newSubscription,
                            policy
                    );
                } catch (Exception e) {
                    log.warn("[자동 갱신] 활성 페이백 정책 없음 - 구독만 갱신됨: memberId={}", member.getId());
                }

                log.info("[자동 갱신] memberId={} 구독 갱신 완료", member.getId());
            }
        }
    }

}
