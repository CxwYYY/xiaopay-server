package com.xiao.xiaopay.domain.collector.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WechatMessageItem(
        @NotBlank String messageId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String payNum,
        String remarkRaw,
        @NotNull LocalDateTime payTime,
        String title,
        String description,
        String rawContent
) {
}
