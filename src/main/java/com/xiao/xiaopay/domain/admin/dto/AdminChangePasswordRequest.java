package com.xiao.xiaopay.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 后台管理员修改密码请求。
 */
public record AdminChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8, max = 64) String newPassword
) {
}
