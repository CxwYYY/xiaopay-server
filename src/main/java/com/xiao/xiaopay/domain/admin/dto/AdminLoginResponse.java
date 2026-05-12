package com.xiao.xiaopay.domain.admin.dto;

/**
 * 后台管理员登录响应，包含 Sa-Token token 信息。
 */
public record AdminLoginResponse(
        String tokenName,
        String tokenValue,
        AdminUserResponse user
) {
}
