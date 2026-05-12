package com.xiao.xiaopay.domain.agent.dto;

public record UpdateAgentRequest(
        String agentName,
        Long channelId,
        String wechatAccount,
        String hostName
) {
}
