package com.xiao.xiaopay.domain.app.dto;

/**
 * 接入应用响应。
 *
 * <p>appSecret 只在创建或重置密钥时返回。</p>
 */
public record AppResponse(
        String appId,
        String appName,
        String appSecret,
        String status,
        String notifyUrl,
        String remark
) {
}
