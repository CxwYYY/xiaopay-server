package com.xiao.xiaopay.domain.agent.dto;

public record AgentResponse(
        String agentId,
        String agentSecret,
        String agentName,
        Long channelId,
        String wechatAccount,
        String hostName,
        String status
) {
}
