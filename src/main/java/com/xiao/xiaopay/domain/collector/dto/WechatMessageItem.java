package com.xiao.xiaopay.domain.collector.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agent 上报的单条微信到账消息。
 */
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
