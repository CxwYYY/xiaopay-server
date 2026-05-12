package com.xiao.xiaopay.domain.admin.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginRequest;
import com.xiao.xiaopay.domain.admin.dto.AdminLoginResponse;
import com.xiao.xiaopay.domain.admin.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminAuthAliasController {
    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(adminAuthService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminAuthService.logout();
        return ApiResponse.ok();
    }
}
