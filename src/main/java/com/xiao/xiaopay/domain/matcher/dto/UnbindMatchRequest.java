package com.xiao.xiaopay.domain.matcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnbindMatchRequest(
        @NotBlank String orderNo,
        @NotNull Long wechatMessageId,
        String reason
) {
}
