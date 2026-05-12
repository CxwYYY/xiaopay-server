package com.xiao.xiaopay.domain.agent.dto;

/**
 * Agent credential verification result for startup and heartbeat checks.
 */
public record AgentCredentialVerifyResponse(
        String agentId,
        String agentName,
        Long channelId,
        String status
) {
}
