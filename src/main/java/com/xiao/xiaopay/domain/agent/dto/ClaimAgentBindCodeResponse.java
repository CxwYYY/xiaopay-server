package com.xiao.xiaopay.domain.agent.dto;

/**
 * Credentials returned once after a binding code is claimed.
 */
public record ClaimAgentBindCodeResponse(
        String agentId,
        String agentSecret
) {
}
