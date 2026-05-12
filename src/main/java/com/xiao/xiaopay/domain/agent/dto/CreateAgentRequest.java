package com.xiao.xiaopay.domain.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建采集 Agent 请求。
 */
public record CreateAgentRequest(
        @NotBlank String agentName,
        @NotNull Long channelId,
        String wechatAccount,
        String hostName
) {
}
