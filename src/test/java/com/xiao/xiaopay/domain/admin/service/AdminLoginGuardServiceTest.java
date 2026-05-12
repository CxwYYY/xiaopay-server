package com.xiao.xiaopay.domain.admin.service;

import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.security.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLoginGuardServiceTest {
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private AdminLoginGuardService guardService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SecurityProperties properties = new SecurityProperties();
        properties.setLoginMaxFailures(3);
        properties.setLoginLockMinutes(10);
        guardService = new AdminLoginGuardService(redisTemplate, properties);
    }

    @Test
    void recordsFailureAndSetsExpiryForUsernameAndIp() {
        when(valueOperations.increment("xiaopay:admin-login-fail:user:cxw")).thenReturn(1L);
        when(valueOperations.increment("xiaopay:admin-login-fail:ip:127.0.0.1")).thenReturn(1L);

        guardService.recordFailure("cxw", "127.0.0.1");

        verify(redisTemplate).expire("xiaopay:admin-login-fail:user:cxw", 10, TimeUnit.MINUTES);
        verify(redisTemplate).expire("xiaopay:admin-login-fail:ip:127.0.0.1", 10, TimeUnit.MINUTES);
    }

    @Test
    void rejectsLoginWhenUsernameOrIpIsLocked() {
        when(valueOperations.get("xiaopay:admin-login-fail:user:cxw")).thenReturn("3");
        when(valueOperations.get("xiaopay:admin-login-fail:ip:127.0.0.1")).thenReturn("0");

        assertThatThrownBy(() -> guardService.checkAllowed("cxw", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("too many login failures, please retry later");
    }

    @Test
    void clearsUsernameAndIpCountersAfterSuccessfulLogin() {
        guardService.clear("cxw", "127.0.0.1");

        verify(redisTemplate).delete("xiaopay:admin-login-fail:user:cxw");
        verify(redisTemplate).delete("xiaopay:admin-login-fail:ip:127.0.0.1");
    }

    @Test
    void allowsLoginWhenCountersAreBelowLimit() {
        when(valueOperations.get("xiaopay:admin-login-fail:user:cxw")).thenReturn("2");
        when(valueOperations.get("xiaopay:admin-login-fail:ip:127.0.0.1")).thenReturn("1");

        assertThatCode(() -> guardService.checkAllowed("cxw", "127.0.0.1")).doesNotThrowAnyException();
    }
}
