package com.xiao.xiaopay.domain.agent.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Agent-side request for claiming credentials with a one-time binding code.
 */
public record ClaimAgentBindCodeRequest(
        @NotBlank String bindCode,
        String hostName
) {
}
