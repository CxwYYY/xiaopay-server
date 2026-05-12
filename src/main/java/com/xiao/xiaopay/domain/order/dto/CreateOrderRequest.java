package com.xiao.xiaopay.domain.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank String appOrderNo,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String subject,
        String description,
        String buyerId,
        String buyerName,
        String notifyUrl,
        String returnUrl,
        String businessType,
        String businessPayload,
        Integer expireSeconds
) {
}
