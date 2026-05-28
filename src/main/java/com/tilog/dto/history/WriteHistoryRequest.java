package com.tilog.dto.history;

import jakarta.validation.constraints.NotNull;

public record WriteHistoryRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId
) {}

/* 추후 로그인 구현 후 교체
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public WriteHistoryResponse recordWriteHistory(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {
    return writeHistoryService.recordWriteHistory(userDetails.getMemberId());
}
*/
