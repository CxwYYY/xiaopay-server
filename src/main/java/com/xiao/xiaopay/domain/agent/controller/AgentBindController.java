package com.xiao.xiaopay.domain.agent.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.web.ClientIpResolver;
import com.xiao.xiaopay.domain.agent.dto.ClaimAgentBindCodeRequest;
import com.xiao.xiaopay.domain.agent.dto.ClaimAgentBindCodeResponse;
import com.xiao.xiaopay.domain.agent.service.AgentBindCodeService;
import com.xiao.xiaopay.domain.agent.service.AgentBindCodeGuardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public agent binding endpoint guarded by one-time binding codes.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agent-bind")
public class AgentBindController {
    private final AgentBindCodeService bindCodeService;
    private final AgentBindCodeGuardService guardService;

    @PostMapping("/claim")
    public ApiResponse<ClaimAgentBindCodeResponse> claim(@Valid @RequestBody ClaimAgentBindCodeRequest request,
                                                         HttpServletRequest httpRequest) {
        String clientIp = ClientIpResolver.resolve(httpRequest);
        guardService.checkAllowed(clientIp);
        try {
            ClaimAgentBindCodeResponse response = bindCodeService.claim(request);
            guardService.clear(clientIp);
            return ApiResponse.ok(response);
        } catch (RuntimeException ex) {
            guardService.recordFailure(clientIp);
            throw ex;
        }
    }
}
