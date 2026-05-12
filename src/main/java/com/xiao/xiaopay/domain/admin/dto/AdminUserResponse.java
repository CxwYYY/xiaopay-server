package com.xiao.xiaopay.domain.admin.dto;

import java.time.LocalDateTime;

/**
 * 后台管理员资料响应。
 */
public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String status,
        LocalDateTime lastLoginAt
) {
}
