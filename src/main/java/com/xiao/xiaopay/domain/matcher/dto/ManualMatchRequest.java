package com.xiao.xiaopay.domain.matcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 人工匹配订单和到账消息请求。
 */
public record ManualMatchRequest(
        @NotBlank String orderNo,
        @NotNull Long wechatMessageId
) {
}
