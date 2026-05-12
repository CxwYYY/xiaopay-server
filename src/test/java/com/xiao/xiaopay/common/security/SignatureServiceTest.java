package com.xiao.xiaopay.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureServiceTest {
    @Test
    void signsTimestampNonceAndBodyWithHmacSha256() {
        SignatureService service = new SignatureService();

        String signature = service.sign("secret-123", "1778490000000", "nonce-1", "{\"amount\":\"19.90\"}");

        assertThat(signature).isEqualTo("6f0eca8e5f4b63325b74eea3f9eda328a8e66e5f6963150195e50624c22067d7");
    }

    @Test
    void verifiesSignatureCaseInsensitively() {
        SignatureService service = new SignatureService();
        String body = "{\"amount\":\"19.90\"}";
        String signature = service.sign("secret-123", "1778490000000", "nonce-1", body).toUpperCase();

        assertThat(service.verify("secret-123", "1778490000000", "nonce-1", body, signature)).isTrue();
    }
}
