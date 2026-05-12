package com.xiao.xiaopay.common.security;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class SignatureService {
    public String sign(String secret, String timestamp, String nonce, String body) {
        String canonical = timestamp + "\n" + nonce + "\n" + (body == null ? "" : body);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
        return hmac.digestHex(canonical);
    }

    public boolean verify(String secret, String timestamp, String nonce, String body, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        return sign(secret, timestamp, nonce, body).equalsIgnoreCase(signature.trim());
    }
}
