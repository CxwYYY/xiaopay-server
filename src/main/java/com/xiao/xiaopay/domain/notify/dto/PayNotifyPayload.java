package com.xiao.xiaopay.domain.notify.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * XiaoPay 回调业务系统的支付结果载荷。
 */
public record PayNotifyPayload(
        String notifyEventId,
        String orderNo,
        String appOrderNo,
        String payStatus,
        BigDecimal amount,
        String payNum,
        LocalDateTime paidAt,
        String businessType,
        String businessPayload
) {
}
