package com.xiao.xiaopay.domain.agent.service;

import com.xiao.xiaopay.common.error.BusinessException;
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

class AgentBindCodeGuardServiceTest {
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private AgentBindCodeGuardService guardService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        guardService = new AgentBindCodeGuardService(redisTemplate);
    }

    @Test
    void allowsClaimWhenFailureCounterIsBelowLimit() {
        when(valueOperations.get("xiaopay:agent-bind-fail:ip:127.0.0.1")).thenReturn("4");

        assertThatCode(() -> guardService.checkAllowed("127.0.0.1")).doesNotThrowAnyException();
    }

    @Test
    void rejectsClaimWhenFailureCounterReachesLimit() {
        when(valueOperations.get("xiaopay:agent-bind-fail:ip:127.0.0.1")).thenReturn("5");

        assertThatThrownBy(() -> guardService.checkAllowed("127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("too many bind code failures, please retry later");
    }

    @Test
    void recordsFailureWithExpireWindow() {
        when(valueOperations.increment("xiaopay:agent-bind-fail:ip:127.0.0.1")).thenReturn(1L);

        guardService.recordFailure("127.0.0.1");

        verify(redisTemplate).expire("xiaopay:agent-bind-fail:ip:127.0.0.1", 15, TimeUnit.MINUTES);
    }
}
