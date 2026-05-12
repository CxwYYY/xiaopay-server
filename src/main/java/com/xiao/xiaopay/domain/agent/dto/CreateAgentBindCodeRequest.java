package com.xiao.xiaopay.domain.agent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Admin request for generating a one-time agent binding code.
 */
public record CreateAgentBindCodeRequest(
        @NotBlank String agentName,
        @NotNull Long channelId,
        String wechatAccount,
        String hostName,
        @Min(1) @Max(1440) Integer expireMinutes
) {
}
