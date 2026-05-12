package com.xiao.xiaopay.domain.admin.dto;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String status,
        LocalDateTime lastLoginAt
) {
}
