package com.xiao.xiaopay.domain.matcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 人工解除订单和到账消息绑定请求。
 */
public record UnbindMatchRequest(
        @NotBlank String orderNo,
        @NotNull Long wechatMessageId,
        String reason
) {
}
