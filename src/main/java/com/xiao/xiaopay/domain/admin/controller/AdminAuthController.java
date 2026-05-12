package com.xiao.xiaopay.domain.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.web.ClientIpResolver;
import com.xiao.xiaopay.domain.admin.dto.AdminChangePasswordRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminInitRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginResponse;
import com.xiao.xiaopay.domain.admin.dto.AdminUserResponse;
import com.xiao.xiaopay.domain.admin.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台认证接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    @GetMapping("/initialized")
    public ApiResponse<Boolean> initialized() {
        return ApiResponse.ok(adminAuthService.initialized());
    }

    @PostMapping("/init")
    public ApiResponse<AdminUserResponse> init(@Valid @RequestBody AdminInitRequest request,
                                               @RequestHeader(value = "X-XiaoPay-Setup-Token", required = false) String setupToken,
                                               HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminAuthService.init(request, setupToken, ClientIpResolver.resolve(servletRequest)));
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                                 HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminAuthService.login(request, ClientIpResolver.resolve(servletRequest)));
    }

    @GetMapping("/me")
    public ApiResponse<AdminUserResponse> me() {
        return ApiResponse.ok(adminAuthService.current());
    }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody AdminChangePasswordRequest request) {
        adminAuthService.changePassword(StpUtil.getLoginIdAsLong(), request.oldPassword(), request.newPassword());
        return ApiResponse.ok();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminAuthService.logout();
        return ApiResponse.ok(null);
    }
}
