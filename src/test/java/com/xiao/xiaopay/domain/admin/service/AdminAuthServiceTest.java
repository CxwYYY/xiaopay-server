package com.xiao.xiaopay.domain.admin.service;

import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.security.SetupTokenVerifier;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.admin.entity.XpAdminUser;
import com.xiao.xiaopay.domain.admin.mapper.XpAdminUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuthServiceTest {
    private XpAdminUserMapper adminUserMapper;
    private AdminPasswordService passwordService;
    private AdminAuthService authService;

    @BeforeEach
    void setUp() {
        adminUserMapper = mock(XpAdminUserMapper.class);
        passwordService = mock(AdminPasswordService.class);
        authService = new AdminAuthService(adminUserMapper, passwordService, mock(IdGenerator.class),
                new TimeProvider(), mock(AdminLoginGuardService.class), mock(SetupTokenVerifier.class));
    }

    @Test
    void changesPasswordWhenOldPasswordMatches() {
        XpAdminUser user = new XpAdminUser();
        user.setId(1L);
        user.setPasswordHash("old-hash");
        when(adminUserMapper.selectById(1L)).thenReturn(user);
        when(passwordService.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordService.hash("new-password")).thenReturn("new-hash");

        authService.changePassword(1L, "old-password", "new-password");

        verify(adminUserMapper).updateById(user);
        org.assertj.core.api.Assertions.assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void rejectsPasswordChangeWhenOldPasswordDoesNotMatch() {
        XpAdminUser user = new XpAdminUser();
        user.setId(1L);
        user.setPasswordHash("old-hash");
        when(adminUserMapper.selectById(1L)).thenReturn(user);
        when(passwordService.matches("wrong", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, "wrong", "new-password"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("old password is invalid");
    }
}
