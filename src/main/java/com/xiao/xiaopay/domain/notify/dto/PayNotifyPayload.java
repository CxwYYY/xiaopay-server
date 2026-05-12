package com.xiao.xiaopay.domain.notify.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
