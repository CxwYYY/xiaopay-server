package com.xiao.xiaopay.domain.collector.dto;

/**
 * 微信到账消息批量接收结果。
 */
public record WechatMessagePushResponse(
        int received,
        int inserted,
        int duplicates
) {
}
