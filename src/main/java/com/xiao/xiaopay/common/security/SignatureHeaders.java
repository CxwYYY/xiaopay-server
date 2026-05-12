package com.xiao.xiaopay.common.security;

public record SignatureHeaders(
        String identity,
        String timestamp,
        String nonce,
        String signature
) {
}
