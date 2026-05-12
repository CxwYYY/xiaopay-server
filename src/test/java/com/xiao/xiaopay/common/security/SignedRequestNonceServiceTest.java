package com.xiao.xiaopay.common.security;

import com.xiao.xiaopay.common.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignedRequestNonceServiceTest {
    private ValueOperations<String, String> valueOperations;
    private SignedRequestNonceService nonceService;

    @BeforeEach
    void setUp() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SecurityProperties properties = new SecurityProperties();
        properties.setSignatureWindowSeconds(300);
        nonceService = new SignedRequestNonceService(redisTemplate, properties);
    }

    @Test
    void remembersNonceInsideSignatureWindow() {
        when(valueOperations.setIfAbsent("xiaopay:signature-nonce:APP1:n1", "1", Duration.ofSeconds(300)))
                .thenReturn(true);

        assertThatCode(() -> nonceService.checkAndRemember("APP1", "n1")).doesNotThrowAnyException();
    }

    @Test
    void rejectsBlankOrDuplicateNonce() {
        assertThatThrownBy(() -> nonceService.checkAndRemember("APP1", " "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("invalid nonce");

        when(valueOperations.setIfAbsent("xiaopay:signature-nonce:APP1:n1", "1", Duration.ofSeconds(300)))
                .thenReturn(false);
        assertThatThrownBy(() -> nonceService.checkAndRemember("APP1", "n1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("duplicate nonce");
    }
}
