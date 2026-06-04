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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mock 구독 서비스
 *
 * [흐름 요약]
 * 1. subscribe()  : USER → Subscription(ACTIVE) 생성 + member.role = PREMIUM 으로 변경
 * 2. cancel()     : ACTIVE Subscription → CANCELED + member.role = USER 로 복원
 * 3. getStatus()  : 현재 로그인 회원의 구독 상태 반환
 * 4. getHistory() : 전체 구독 이력 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final PaybackPolicyService paybackPolicyService;
    private final PaybackParticipationService paybackParticipationService;

    // Mock 구독 신청 (30일 ACTIVE → PREMIUM 권한 부여)
    @Transactional
    public SubscriptionStatusResponse subscribe() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validateNoActiveSubscription(memberId, now);

        Subscription subscription = Subscription.createMock(member);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        PaybackPolicy policy = paybackPolicyService.getCurrentActivePolicy(LocalDate.now());
        paybackParticipationService.createForSubscription(
                memberId,
                savedSubscription,
                policy
        );

        member.changeRole(MemberRole.PREMIUM);

        return SubscriptionStatusResponse.from(savedSubscription);
    }

    // 구독 취소 (ACTIVE → CANCELED, PREMIUM → USER)
    @Transactional
    public SubscriptionStatusResponse cancel() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Subscription subscription = findCurrentActiveSubscription(memberId, now);

        subscription.cancel();

        if (member.getRole() == MemberRole.PREMIUM) {
            member.changeRole(MemberRole.USER);
        }

        return SubscriptionStatusResponse.from(subscription);
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

    private void validateNoActiveSubscription(Long memberId, LocalDateTime now) {
        boolean hasActiveSubscription = subscriptionRepository.findCurrentActiveSubscriptions(
                        memberId,
                        now,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .isPresent();

        if (hasActiveSubscription) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);
        }
    }

    private Subscription findCurrentActiveSubscription(Long memberId, LocalDateTime now) {
        return subscriptionRepository.findCurrentActiveSubscriptions(
                        memberId,
                        now,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
    }
}
