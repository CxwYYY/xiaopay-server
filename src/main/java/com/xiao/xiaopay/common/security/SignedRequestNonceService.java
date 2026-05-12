package com.xiao.xiaopay.common.security;

import com.xiao.xiaopay.common.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 签名请求 nonce 防重放服务。
 */
@Service
@RequiredArgsConstructor
public class SignedRequestNonceService {
    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 在签名时间窗口内记录 nonce，重复 nonce 直接拒绝。
     */
    public void checkAndRemember(String identity, String nonce) {
        if (nonce == null || nonce.isBlank()) {
            throw new BusinessException(401, "invalid nonce");
        }
        String key = "xiaopay:signature-nonce:" + identity + ":" + nonce.trim();
        Boolean stored = redisTemplate.opsForValue().setIfAbsent(
                key, "1", Duration.ofSeconds(securityProperties.getSignatureWindowSeconds()));
        if (!Boolean.TRUE.equals(stored)) {
            throw new BusinessException(401, "duplicate nonce");
        }
    }
}
