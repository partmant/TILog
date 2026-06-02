package com.tilog.domain.payback.service;

import com.tilog.domain.payback.dto.PaybackParticipationRequest;
import com.tilog.domain.payback.dto.PaybackParticipationResponse;
import com.tilog.domain.payback.entity.PaybackParticipation;
import com.tilog.domain.payback.entity.PaybackPolicy;
import com.tilog.domain.payback.repository.PaybackParticipationRepository;
import com.tilog.domain.payback.repository.PaybackPolicyRepository;
import com.tilog.domain.writeHistory.repository.WriteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 페이백 정책입니다."));

        validateActivePolicy(policy);
        validatePolicyPeriod(policy, participationMonth);
        validateDuplicatedParticipation(memberId, policy.getPaybackPolicyId(), participationMonth);

        PaybackParticipation participation = PaybackParticipation.create(memberId, policy, participationMonth);
        PaybackParticipation savedParticipation = paybackParticipationRepository.save(participation);

        return PaybackParticipationResponse.from(savedParticipation);
    }

    @Transactional
    public PaybackParticipationResponse getMyParticipation(Long memberId, String month) {
        YearMonth participationMonth = parseYearMonth(month);

        PaybackParticipation participation = paybackParticipationRepository
                .findByMemberIdAndParticipationMonth(memberId, participationMonth.toString())
                .orElseThrow(() -> new IllegalArgumentException("해당 월의 페이백 참여 내역이 없습니다."));

        int achievedWriteDays = countAchievedWriteDays(memberId, participationMonth);
        participation.updateProgress(
                achievedWriteDays,
                participation.getPaybackPolicy().getRequiredWriteDays()
        );

        return PaybackParticipationResponse.from(participation);
    }

    private YearMonth parseYearMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (Exception e) {
            throw new IllegalArgumentException("참여 월은 yyyy-MM 형식이어야 합니다.");
        }
    }

    private void validateActivePolicy(PaybackPolicy policy) {
        if (!policy.isActive()) {
            throw new IllegalArgumentException("비활성화된 페이백 정책입니다.");
        }
    }

    private void validatePolicyPeriod(PaybackPolicy policy, YearMonth participationMonth) {
        LocalDate monthStartDate = participationMonth.atDay(1);
        LocalDate monthEndDate = participationMonth.atEndOfMonth();

        boolean isBeforePolicy = monthEndDate.isBefore(policy.getStartDate());
        boolean isAfterPolicy = monthStartDate.isAfter(policy.getEndDate());

        if (isBeforePolicy || isAfterPolicy) {
            throw new IllegalArgumentException("페이백 정책 기간에 포함되지 않는 참여 월입니다.");
        }
    }

    private void validateDuplicatedParticipation(Long memberId, Long paybackPolicyId, YearMonth participationMonth) {
        boolean duplicated = paybackParticipationRepository
                .existsByMemberIdAndPaybackPolicyPaybackPolicyIdAndParticipationMonth(
                        memberId,
                        paybackPolicyId,
                        participationMonth.toString()
                );

        if (duplicated) {
            throw new IllegalArgumentException("이미 해당 월 페이백 챌린지에 참여했습니다.");
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
