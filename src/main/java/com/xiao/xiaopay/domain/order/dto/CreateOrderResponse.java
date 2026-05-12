package com.xiao.xiaopay.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateOrderResponse(
        String orderNo,
        String appOrderNo,
        BigDecimal amount,
        String payNum,
        String payType,
        Long channelId,
        String qrCodeUrl,
        String orderStatus,
        LocalDateTime expireAt,
        LocalDateTime createdAt
) {
}
