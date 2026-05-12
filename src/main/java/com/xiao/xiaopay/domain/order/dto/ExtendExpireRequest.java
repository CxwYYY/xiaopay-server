package com.xiao.xiaopay.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExtendExpireRequest(
        @NotNull @Min(1) Integer minutes,
        String reason
) {
}
