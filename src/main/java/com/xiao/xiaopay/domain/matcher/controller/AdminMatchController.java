package com.xiao.xiaopay.domain.matcher.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.matcher.dto.ManualMatchRequest;
import com.xiao.xiaopay.domain.matcher.dto.UnbindMatchRequest;
import com.xiao.xiaopay.domain.matcher.service.OrderMatcherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台到账匹配人工操作接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/matches")
public class AdminMatchController {
    private final OrderMatcherService matcherService;
    private final AuditLogService auditLogService;

    @PostMapping("/manual")
    public ApiResponse<Void> manual(@Valid @RequestBody ManualMatchRequest request) {
        matcherService.manualMatch(request.orderNo(), request.wechatMessageId());
        auditLogService.record("MANUAL_MATCH", "ORDER", request.orderNo(), null, String.valueOf(request.wechatMessageId()));
        return ApiResponse.ok();
    }

    @PostMapping("/unbind")
    public ApiResponse<Void> unbind(@Valid @RequestBody UnbindMatchRequest request) {
        matcherService.unbindMatch(request.orderNo(), request.wechatMessageId(), request.reason());
        auditLogService.record("UNBIND_MATCH", "ORDER", request.orderNo(), null, String.valueOf(request.wechatMessageId()));
        return ApiResponse.ok();
    }
}
