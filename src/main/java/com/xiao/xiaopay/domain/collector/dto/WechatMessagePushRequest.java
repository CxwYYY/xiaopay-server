package com.xiao.xiaopay.domain.collector.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Agent 批量推送微信到账消息请求。
 */
public record WechatMessagePushRequest(
        @Valid @NotEmpty List<WechatMessageItem> messages
) {
}
