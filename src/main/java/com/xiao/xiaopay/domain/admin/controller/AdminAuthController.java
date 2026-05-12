package com.xiao.xiaopay.domain.admin.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.admin.dto.AdminInitRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginResponse;
import com.xiao.xiaopay.domain.admin.dto.AdminUserResponse;
import com.xiao.xiaopay.domain.admin.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ApiResponse<AdminUserResponse> init(@Valid @RequestBody AdminInitRequest request) {
        return ApiResponse.ok(adminAuthService.init(request));
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(adminAuthService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AdminUserResponse> me() {
        return ApiResponse.ok(adminAuthService.current());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminAuthService.logout();
        return ApiResponse.ok(null);
    }
}
