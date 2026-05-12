package com.xiao.xiaopay.domain.agent.dto;

/**
 * 更新采集 Agent 请求。
 */
public record UpdateAgentRequest(
        String agentName,
        Long channelId,
        String wechatAccount,
        String hostName
) {
}
