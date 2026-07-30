package com.tilog.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminMemberListResponse {
    private Long memberId;
    private String email;
    private String nickname;
    private String role;
    private boolean isBanned;
    private LocalDateTime createdAt;
}
