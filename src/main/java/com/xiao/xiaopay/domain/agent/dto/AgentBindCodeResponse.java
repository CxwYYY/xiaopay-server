package com.xiao.xiaopay.domain.agent.dto;

import java.time.LocalDateTime;

/**
 * Binding code returned to the admin console.
 */
public record AgentBindCodeResponse(
        String bindCode,
        LocalDateTime expiresAt,
        String status
) {
}
