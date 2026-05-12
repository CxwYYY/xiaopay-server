package com.xiao.xiaopay.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 后台管理员登录请求。
 */
public record AdminLoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
