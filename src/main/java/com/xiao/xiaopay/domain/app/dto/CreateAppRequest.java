package com.xiao.xiaopay.domain.app.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建接入应用请求。
 */
public record CreateAppRequest(
        @NotBlank String appName,
        String notifyUrl,
        String remark
) {
}
