package com.tilog.domain.payback.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PaybackParticipationRequest(
        @NotNull(message = "페이백 정책 ID는 필수입니다.")
        Long paybackPolicyId,

        @NotNull(message = "참여 월은 필수입니다.")
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "참여 월은 yyyy-MM 형식이어야 합니다.")
        String participationMonth
) {
}
