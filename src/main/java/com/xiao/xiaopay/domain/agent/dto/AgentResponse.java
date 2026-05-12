package com.xiao.xiaopay.domain.agent.dto;

/**
 * 采集 Agent 响应。
 *
 * <p>agentSecret 只在创建或重置密钥时返回，列表和详情默认为空。</p>
 */
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
