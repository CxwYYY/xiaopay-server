package com.xiao.xiaopay.domain.app.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.app.dto.AppResponse;
import com.xiao.xiaopay.domain.app.dto.CreateAppRequest;
import com.xiao.xiaopay.domain.app.dto.UpdateAppRequest;
import com.xiao.xiaopay.domain.app.service.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/apps")
public class AdminAppController {
    private final AppService appService;

    @PostMapping
    public ApiResponse<AppResponse> create(@Valid @RequestBody CreateAppRequest request) {
        return ApiResponse.ok(appService.create(request));
    }

    @GetMapping
    public ApiResponse<List<AppResponse>> list() {
        return ApiResponse.ok(appService.list());
    }

    @GetMapping("/{appId}")
    public ApiResponse<AppResponse> detail(@PathVariable String appId) {
        return ApiResponse.ok(appService.detail(appId));
    }

    @PutMapping("/{appId}")
    public ApiResponse<AppResponse> update(@PathVariable String appId,
                                           @RequestBody UpdateAppRequest request) {
        return ApiResponse.ok(appService.update(appId, request));
    }

    @PostMapping("/{appId}/enable")
    public ApiResponse<AppResponse> enable(@PathVariable String appId) {
        return ApiResponse.ok(appService.setStatus(appId, "ENABLED"));
    }

    @PostMapping("/{appId}/disable")
    public ApiResponse<AppResponse> disable(@PathVariable String appId) {
        return ApiResponse.ok(appService.setStatus(appId, "DISABLED"));
    }

    @DeleteMapping("/{appId}")
    public ApiResponse<AppResponse> delete(@PathVariable String appId) {
        return ApiResponse.ok(appService.setStatus(appId, "DISABLED"));
    }

    @PostMapping("/{appId}/secret/reset")
    public ApiResponse<AppResponse> resetSecret(@PathVariable String appId) {
        return ApiResponse.ok(appService.resetSecret(appId));
    }
}
