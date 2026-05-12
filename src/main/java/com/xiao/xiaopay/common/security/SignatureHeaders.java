package com.xiao.xiaopay.common.security;

/**
 * 请求签名头集合。
 *
 * <p>identity 对应 appId 或 agentId，其他字段用于 HMAC-SHA256 验签。</p>
 */
public record SignatureHeaders(
        String identity,
        String timestamp,
        String nonce,
        String signature
) {
}
