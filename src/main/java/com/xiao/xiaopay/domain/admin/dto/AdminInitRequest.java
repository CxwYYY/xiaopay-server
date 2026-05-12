package com.xiao.xiaopay.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 后台首次初始化管理员请求。
 */
public record AdminInitRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank String nickname
) {
}
