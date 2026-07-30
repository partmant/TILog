package com.tilog.domain.member.dto;

import com.tilog.domain.member.entity.CurrentStatus;
import com.tilog.domain.member.entity.TargetJob;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
        String nickname,

        CurrentStatus currentStatus,
        TargetJob targetJob
) {}
