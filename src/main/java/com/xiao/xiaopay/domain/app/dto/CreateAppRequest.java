package com.xiao.xiaopay.domain.app.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAppRequest(
        @NotBlank String appName,
        String notifyUrl,
        String remark
) {
}
