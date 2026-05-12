package com.xiao.xiaopay.domain.collector.dto;

public record WechatMessagePushResponse(
        int received,
        int inserted,
        int duplicates
) {
}
