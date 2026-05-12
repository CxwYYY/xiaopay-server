package com.xiao.xiaopay.domain.channel.dto;

public record ChannelResponse(
        Long id,
        String channelCode,
        String channelName,
        String channelType,
        String collectorType,
        String agentId,
        String qrCodeUrl,
        String receiverName,
        String status
) {
}
