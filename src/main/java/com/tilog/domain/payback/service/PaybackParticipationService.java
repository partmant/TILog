package com.tilog.domain.payback.service;

import com.tilog.domain.payback.dto.PaybackParticipationRequest;
import com.tilog.domain.payback.dto.PaybackParticipationResponse;
import com.tilog.domain.payback.entity.PaybackParticipation;
import com.tilog.domain.payback.entity.PaybackPolicy;
import com.tilog.domain.payback.repository.PaybackParticipationRepository;
import com.tilog.domain.payback.repository.PaybackPolicyRepository;
import com.tilog.domain.writeHistory.repository.WriteHistoryRepository;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaybackParticipationService {
    private final PaybackParticipationRepository paybackParticipationRepository;
    private final PaybackPolicyRepository paybackPolicyRepository;
    private final WriteHistoryRepository writeHistoryRepository;

    @Transactional
    public PaybackParticipationResponse participate(Long memberId, PaybackParticipationRequest request) {
        YearMonth participationMonth = parseYearMonth(request.participationMonth());

        PaybackPolicy policy = paybackPolicyRepository.findById(request.paybackPolicyId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYBACK_POLICY_NOT_FOUND));

        validateActivePolicy(policy);
        validatePolicyPeriod(policy, participationMonth);
        validateDuplicatedParticipation(memberId, policy.getPaybackPolicyId(), participationMonth);

        PaybackParticipation participation = PaybackParticipation.create(memberId, policy, participationMonth);
        PaybackParticipation savedParticipation = paybackParticipationRepository.save(participation);

        return PaybackParticipationResponse.from(savedParticipation);
    }

    public PaybackParticipationResponse getMyParticipation(Long memberId, String month) {
        YearMonth participationMonth = parseYearMonth(month);

        PaybackParticipation participation = paybackParticipationRepository
                .findByMemberIdAndParticipationMonth(memberId, participationMonth.toString())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYBACK_PARTICIPATION_NOT_FOUND));

        int achievedWriteDays = countAchievedWriteDays(memberId, participationMonth);

        return PaybackParticipationResponse.from(participation, achievedWriteDays);
    }

    private YearMonth parseYearMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_PAYBACK_MONTH);
        }
    }

    private void validateActivePolicy(PaybackPolicy policy) {
        if (!policy.isActive()) {
            throw new CustomException(ErrorCode.INACTIVE_PAYBACK_POLICY);
        }
    }

    private void validatePolicyPeriod(PaybackPolicy policy, YearMonth participationMonth) {
        LocalDate monthStartDate = participationMonth.atDay(1);
        LocalDate monthEndDate = participationMonth.atEndOfMonth();

        boolean isBeforePolicy = monthEndDate.isBefore(policy.getStartDate());
        boolean isAfterPolicy = monthStartDate.isAfter(policy.getEndDate());

        if (isBeforePolicy || isAfterPolicy) {
            throw new CustomException(ErrorCode.PAYBACK_POLICY_PERIOD_MISMATCH);
        }
    }

    private void validateDuplicatedParticipation(Long memberId, Long paybackPolicyId, YearMonth participationMonth) {
        boolean duplicated = paybackParticipationRepository.existsByMemberPolicyMonth(
                memberId,
                paybackPolicyId,
                participationMonth.toString()
        );

        if (duplicated) {
            throw new CustomException(ErrorCode.PAYBACK_ALREADY_PARTICIPATED);
        }
    }

    private int countAchievedWriteDays(Long memberId, YearMonth participationMonth) {
        return writeHistoryRepository.countByMember_IdAndWrittenDateBetweenAndWriteCountGreaterThan(
                memberId,
                participationMonth.atDay(1),
                participationMonth.atEndOfMonth(),
                0
        );
    }
}
