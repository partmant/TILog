package com.tilog.domain.writeHistory.dto;

import jakarta.validation.constraints.NotNull;

public record WriteHistoryRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId
) {}
