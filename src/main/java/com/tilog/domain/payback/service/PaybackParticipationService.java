package com.tilog.domain.payback.service;

import com.tilog.domain.payback.dto.PaybackParticipationResponse;
import com.tilog.domain.payback.entity.PaybackParticipation;
import com.tilog.domain.payback.entity.PaybackPolicy;
import com.tilog.domain.payback.entity.PaybackResultStatus;
import com.tilog.domain.payback.repository.PaybackParticipationRepository;
import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.domain.writeHistory.repository.WriteHistoryRepository;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaybackParticipationService {
    private final PaybackParticipationRepository paybackParticipationRepository;
    private final WriteHistoryRepository writeHistoryRepository;

    @Transactional
    public PaybackParticipation createForSubscription(
            Long memberId,
            Subscription subscription,
            PaybackPolicy policy
    ) {
        if (paybackParticipationRepository.existsBySubscription_Id(subscription.getId())) {
            throw new CustomException(ErrorCode.PAYBACK_ALREADY_PARTICIPATED);
        }

        LocalDate periodStart = subscription.getStartedAt().toLocalDate();
        LocalDate periodEnd = subscription.getEndedAt().toLocalDate();

        // 같은 기간의 레코드가 이미 있으면 (당일 취소 후 재구독) 재활성화
        Optional<PaybackParticipation> existing = paybackParticipationRepository
                .findByMemberIdAndPeriodStartDateAndPeriodEndDate(memberId, periodStart, periodEnd);

        if (existing.isPresent()) {
            existing.get().reactivate(subscription);
            return existing.get();
        }

        PaybackParticipation participation = PaybackParticipation.create(
                memberId,
                subscription,
                policy
        );

        return paybackParticipationRepository.save(participation);
    }

    public PaybackParticipationResponse getCurrentParticipation() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        LocalDate today = LocalDate.now();

        List<PaybackParticipation> results = paybackParticipationRepository
                .findCurrentParticipation(memberId, PaybackResultStatus.IN_PROGRESS, today, PageRequest.of(0, 1));

        PaybackParticipation participation = results.stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PAYBACK_PARTICIPATION_NOT_FOUND));

        int achievedWriteDays = countAchievedWriteDays(
                memberId,
                participation.getPeriodStartDate(),
                participation.getPeriodEndDate()
        );

        return PaybackParticipationResponse.from(participation, achievedWriteDays);
    }

    @Transactional
    public void cancelForSubscription(Long subscriptionId) {
        paybackParticipationRepository
                .findBySubscription_IdAndResultStatus(subscriptionId, PaybackResultStatus.IN_PROGRESS)
                .ifPresent(PaybackParticipation::cancel);
    }

    private int countAchievedWriteDays(
            Long memberId,
            LocalDate periodStartDate,
            LocalDate periodEndDate
    ) {
        return writeHistoryRepository.countByMember_IdAndWrittenDateBetweenAndWriteCountGreaterThan(
                memberId,
                periodStartDate,
                periodEndDate,
                0
        );
    }
}
