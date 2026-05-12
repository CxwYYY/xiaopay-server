package com.xiao.xiaopay.common.security;

import com.xiao.xiaopay.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetupTokenVerifierTest {
    @Test
    void requiresConfiguredSetupToken() {
        SecurityProperties properties = new SecurityProperties();
        properties.setSetupToken("setup-secret");
        SetupTokenVerifier verifier = new SetupTokenVerifier(properties);

        assertThatThrownBy(() -> verifier.verify("wrong", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("invalid setup token");
        assertThatCode(() -> verifier.verify("setup-secret", "203.0.113.10")).doesNotThrowAnyException();
    }

    @Test
    void allowsLoopbackOnlyWhenSetupTokenIsNotConfigured() {
        SecurityProperties properties = new SecurityProperties();
        SetupTokenVerifier verifier = new SetupTokenVerifier(properties);

        assertThatCode(() -> verifier.verify(null, "127.0.0.1")).doesNotThrowAnyException();
        assertThatThrownBy(() -> verifier.verify(null, "203.0.113.10"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("setup token is required");
    }
}
