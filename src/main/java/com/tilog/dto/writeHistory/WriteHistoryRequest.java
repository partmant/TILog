package com.tilog.dto.writeHistory;

import jakarta.validation.constraints.NotNull;

public record WriteHistoryRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId
) {}
