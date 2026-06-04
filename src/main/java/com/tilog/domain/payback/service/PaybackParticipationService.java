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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

        PaybackParticipation participation = paybackParticipationRepository
                .findCurrentParticipation(memberId, PaybackResultStatus.IN_PROGRESS, today)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYBACK_PARTICIPATION_NOT_FOUND));

        int achievedWriteDays = countAchievedWriteDays(
                memberId,
                participation.getPeriodStartDate(),
                participation.getPeriodEndDate()
        );

        return PaybackParticipationResponse.from(participation, achievedWriteDays);
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
