package com.xiao.xiaopay.domain.admin.service;

import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 后台登录失败限流服务。
 */
@Service
@RequiredArgsConstructor
public class AdminLoginGuardService {
    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 检查用户名或 IP 是否已因连续失败被临时锁定。
     */
    public void checkAllowed(String username, String clientIp) {
        if (counter(usernameKey(username)) >= securityProperties.getLoginMaxFailures()
                || counter(ipKey(clientIp)) >= securityProperties.getLoginMaxFailures()) {
            throw new BusinessException(429, "too many login failures, please retry later");
        }
    }

    /**
     * 记录一次登录失败。
     */
    public void recordFailure(String username, String clientIp) {
        increment(usernameKey(username));
        increment(ipKey(clientIp));
    }

    /**
     * 登录成功后清理失败计数。
     */
    public void clear(String username, String clientIp) {
        redisTemplate.delete(usernameKey(username));
        redisTemplate.delete(ipKey(clientIp));
    }

    private long counter(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private void increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, securityProperties.getLoginLockMinutes(), TimeUnit.MINUTES);
        }
    }

    private String usernameKey(String username) {
        return "xiaopay:admin-login-fail:user:" + normalize(username);
    }

    private String ipKey(String clientIp) {
        return "xiaopay:admin-login-fail:ip:" + normalize(clientIp);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "unknown" : value.trim().toLowerCase();
    }
}
