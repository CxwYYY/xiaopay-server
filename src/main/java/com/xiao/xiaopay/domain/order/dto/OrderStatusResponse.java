package com.xiao.xiaopay.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户查询订单状态响应。
 */
public record OrderStatusResponse(
        String orderNo,
        String appOrderNo,
        BigDecimal amount,
        String payNum,
        String orderStatus,
        String notifyStatus,
        LocalDateTime expireAt,
        LocalDateTime paidAt,
        String businessType,
        String businessPayload
) {
}
