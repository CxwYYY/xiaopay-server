package com.xiao.xiaopay.domain.agent.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.agent.dto.AgentResponse;
import com.xiao.xiaopay.domain.agent.dto.CreateAgentRequest;
import com.xiao.xiaopay.domain.agent.dto.UpdateAgentRequest;
import com.xiao.xiaopay.domain.agent.service.AgentService;
import com.xiao.xiaopay.domain.common.Status;
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
@RequestMapping("/api/admin/agents")
public class AdminAgentController {
    private final AgentService agentService;

    @PostMapping
    public ApiResponse<AgentResponse> create(@Valid @RequestBody CreateAgentRequest request) {
        return ApiResponse.ok(agentService.create(request));
    }

    @GetMapping
    public ApiResponse<List<AgentResponse>> list() {
        return ApiResponse.ok(agentService.list());
    }

    @GetMapping("/{agentId}")
    public ApiResponse<AgentResponse> detail(@PathVariable String agentId) {
        return ApiResponse.ok(agentService.detail(agentId));
    }

    @PutMapping("/{agentId}")
    public ApiResponse<AgentResponse> update(@PathVariable String agentId,
                                             @RequestBody UpdateAgentRequest request) {
        return ApiResponse.ok(agentService.update(agentId, request));
    }

    @PostMapping("/{agentId}/enable")
    public ApiResponse<AgentResponse> enable(@PathVariable String agentId) {
        return ApiResponse.ok(agentService.setStatus(agentId, Status.ENABLED));
    }

    @PostMapping("/{agentId}/disable")
    public ApiResponse<AgentResponse> disable(@PathVariable String agentId) {
        return ApiResponse.ok(agentService.setStatus(agentId, Status.DISABLED));
    }

    @DeleteMapping("/{agentId}")
    public ApiResponse<AgentResponse> delete(@PathVariable String agentId) {
        return ApiResponse.ok(agentService.setStatus(agentId, Status.DISABLED));
    }

    @PostMapping("/{agentId}/secret/reset")
    public ApiResponse<AgentResponse> resetSecret(@PathVariable String agentId) {
        return ApiResponse.ok(agentService.resetSecret(agentId));
    }
}
