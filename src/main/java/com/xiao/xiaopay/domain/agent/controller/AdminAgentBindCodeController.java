package com.xiao.xiaopay.domain.agent.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.agent.dto.AgentBindCodeResponse;
import com.xiao.xiaopay.domain.agent.dto.CreateAgentBindCodeRequest;
import com.xiao.xiaopay.domain.agent.service.AgentBindCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin API for generating one-time xiaopay-agent binding codes.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/agent-bind-codes")
public class AdminAgentBindCodeController {
    private final AgentBindCodeService bindCodeService;

    @PostMapping
    public ApiResponse<AgentBindCodeResponse> create(@Valid @RequestBody CreateAgentBindCodeRequest request) {
        return ApiResponse.ok(bindCodeService.create(request));
    }
}
