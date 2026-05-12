package com.xiao.xiaopay.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 后台延长订单过期时间请求。
 */
public record ExtendExpireRequest(
        @NotNull @Min(1) Integer minutes,
        String reason
) {
}
