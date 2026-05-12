package com.xiao.xiaopay.domain.agent.service;

import com.xiao.xiaopay.common.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Lightweight rate limiter for public agent binding attempts.
 */
@Service
@RequiredArgsConstructor
public class AgentBindCodeGuardService {
    private static final int MAX_FAILURES = 5;
    private static final int LOCK_MINUTES = 15;

    private final StringRedisTemplate redisTemplate;

    /**
     * Rejects an IP after too many failed binding attempts.
     */
    public void checkAllowed(String clientIp) {
        if (counter(key(clientIp)) >= MAX_FAILURES) {
            throw new BusinessException(429, "too many bind code failures, please retry later");
        }
    }

    /**
     * Records a failed binding attempt.
     */
    public void recordFailure(String clientIp) {
        String key = key(clientIp);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * Clears failures after a successful binding attempt.
     */
    public void clear(String clientIp) {
        redisTemplate.delete(key(clientIp));
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

    private String key(String clientIp) {
        String normalized = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp.trim().toLowerCase();
        return "xiaopay:agent-bind-fail:ip:" + normalized;
    }
}
