package com.xiao.xiaopay.domain.channel.dto;

public record UpdateChannelRequest(
        String channelName,
        String channelType,
        String collectorType,
        String agentId,
        String qrCodeUrl,
        String receiverName,
        String configJson
) {
}
