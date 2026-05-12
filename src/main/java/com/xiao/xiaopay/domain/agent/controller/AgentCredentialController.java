package com.xiao.xiaopay.domain.agent.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.security.SignedBodyVerifier;
import com.xiao.xiaopay.domain.agent.dto.AgentCredentialVerifyResponse;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Signed credential verification API used by agent startup checks.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collector/credential")
public class AgentCredentialController {
    private final SignedBodyVerifier signedBodyVerifier;

    @PostMapping("/verify")
    public ApiResponse<AgentCredentialVerifyResponse> verify(@RequestHeader("X-XiaoPay-Agent") String agentId,
                                                             @RequestHeader("X-XiaoPay-Timestamp") String timestamp,
                                                             @RequestHeader("X-XiaoPay-Nonce") String nonce,
                                                             @RequestHeader("X-XiaoPay-Signature") String signature,
                                                             @RequestBody(required = false) String body) {
        String requestBody = body == null ? "" : body;
        XpAgent agent = signedBodyVerifier.verifyAgent(agentId, timestamp, nonce, signature, requestBody);
        return ApiResponse.ok(new AgentCredentialVerifyResponse(agent.getAgentId(), agent.getAgentName(),
                agent.getChannelId(), agent.getStatus()));
    }
}
