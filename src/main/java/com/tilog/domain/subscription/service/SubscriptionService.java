package com.tilog.domain.subscription.service;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.subscription.dto.SubscriptionHistoryResponse;
import com.tilog.domain.subscription.dto.SubscriptionStatusResponse;
import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.domain.subscription.entity.SubscriptionStatus;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Mock 구독 신청 (30일 ACTIVE → PREMIUM 권한 부여)
    @Transactional
    public SubscriptionStatusResponse subscribe() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미 활성 구독 존재 여부 체크
        Optional<Subscription> existing = subscriptionRepository
                .findTopByMemberIdAndStatusOrderByStartedAtDesc(memberId, SubscriptionStatus.ACTIVE);

        if (existing.isPresent() && existing.get().isActive()) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);
        }

        // Mock 구독 생성
        Subscription subscription = Subscription.createMock(member);
        subscriptionRepository.save(subscription);

        // 회원 권한 PREMIUM으로 승격
        member.changeRole(MemberRole.PREMIUM);

        return SubscriptionStatusResponse.from(subscription);
    }

    // 구독 취소 (ACTIVE → CANCELED, PREMIUM → USER)
    @Transactional
    public SubscriptionStatusResponse cancel() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Subscription subscription = subscriptionRepository
                .findTopByMemberIdAndStatusOrderByStartedAtDesc(memberId, SubscriptionStatus.ACTIVE)
                .filter(Subscription::isActive)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.cancel();

        // 회원 권한 USER로 복원 (MENTOR/ADMIN은 그대로 유지)
        if (member.getRole() == MemberRole.PREMIUM) {
            member.changeRole(MemberRole.USER);
        }

        return SubscriptionStatusResponse.from(subscription);
    }

    // 현재 구독 상태 조회
    public SubscriptionStatusResponse getMyStatus() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return subscriptionRepository
                .findTopByMemberIdAndStatusOrderByStartedAtDesc(memberId, SubscriptionStatus.ACTIVE)
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
                .collect(Collectors.toList());
    }
}
