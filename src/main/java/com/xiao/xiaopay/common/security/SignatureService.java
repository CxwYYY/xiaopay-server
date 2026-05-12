package com.xiao.xiaopay.common.security;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * HMAC-SHA256 签名服务。
 *
 * <p>签名 canonical 串固定为 {@code timestamp + "\n" + nonce + "\n" + body}。</p>
 */
@Service
public class SignatureService {
    /**
     * 使用接入密钥对请求体签名。
     */
    public String sign(String secret, String timestamp, String nonce, String body) {
        String canonical = timestamp + "\n" + nonce + "\n" + (body == null ? "" : body);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
        return hmac.digestHex(canonical);
    }

    /**
     * 校验调用方传入的签名，签名大小写不敏感。
     */
    public boolean verify(String secret, String timestamp, String nonce, String body, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        return sign(secret, timestamp, nonce, body).equalsIgnoreCase(signature.trim());
    }
}
