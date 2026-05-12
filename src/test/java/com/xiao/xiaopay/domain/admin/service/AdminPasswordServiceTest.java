package com.xiao.xiaopay.domain.admin.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminPasswordServiceTest {
    private final AdminPasswordService passwordService = new AdminPasswordService();

    @Test
    void hashesPasswordWithSaltAndVerifiesRawPassword() {
        String firstHash = passwordService.hash("admin123456");
        String secondHash = passwordService.hash("admin123456");

        assertThat(firstHash).isNotEqualTo("admin123456");
        assertThat(firstHash).isNotEqualTo(secondHash);
        assertThat(passwordService.matches("admin123456", firstHash)).isTrue();
        assertThat(passwordService.matches("wrong-password", firstHash)).isFalse();
    }
}
