package com.xiao.xiaopay.domain.admin.dto;

public record AdminLoginResponse(
        String tokenName,
        String tokenValue,
        AdminUserResponse user
) {
}
