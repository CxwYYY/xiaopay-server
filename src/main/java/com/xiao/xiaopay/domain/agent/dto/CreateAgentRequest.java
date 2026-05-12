package com.xiao.xiaopay.domain.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAgentRequest(
        @NotBlank String agentName,
        @NotNull Long channelId,
        String wechatAccount,
        String hostName
) {
}
