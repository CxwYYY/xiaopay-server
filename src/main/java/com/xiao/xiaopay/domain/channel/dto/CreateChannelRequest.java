package com.xiao.xiaopay.domain.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChannelRequest(
        @NotBlank String channelCode,
        @NotBlank String channelName,
        @NotBlank String channelType,
        @NotBlank String collectorType,
        String agentId,
        @NotBlank String qrCodeUrl,
        String receiverName,
        String configJson
) {
}
