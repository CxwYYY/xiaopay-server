package com.xiao.xiaopay.domain.channel.dto;

/**
 * 更新支付通道请求。
 */
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
