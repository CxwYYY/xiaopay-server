package com.xiao.xiaopay.domain.admin.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.web.ClientIpResolver;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginResponse;
import com.xiao.xiaopay.domain.admin.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台认证兼容入口。
 *
 * <p>保留 {@code /api/admin/login} 和 {@code /api/admin/logout}，便于前端使用短路径。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminAuthAliasController {
    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                                 HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminAuthService.login(request, ClientIpResolver.resolve(servletRequest)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminAuthService.logout();
        return ApiResponse.ok();
    }
}
