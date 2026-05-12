package com.xiao.xiaopay.domain.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.security.SignedBodyVerifier;
import com.xiao.xiaopay.domain.agent.dto.HeartbeatRequest;
import com.xiao.xiaopay.domain.agent.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 心跳接口。
 *
 * <p>调用方必须使用 agentSecret 对原始请求体签名。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collector")
public class AgentHeartbeatController {
    private final SignedBodyVerifier signedBodyVerifier;
    private final AgentService agentService;
    private final ObjectMapper objectMapper;

    @PostMapping("/heartbeat")
    public ApiResponse<Void> heartbeat(@RequestHeader("X-XiaoPay-Agent") String agentId,
                                       @RequestHeader("X-XiaoPay-Timestamp") String timestamp,
                                       @RequestHeader("X-XiaoPay-Nonce") String nonce,
                                       @RequestHeader("X-XiaoPay-Signature") String signature,
                                       @RequestBody String body) throws Exception {
        signedBodyVerifier.verifyAgent(agentId, timestamp, nonce, signature, body);
        HeartbeatRequest request = objectMapper.readValue(body, HeartbeatRequest.class);
        agentService.heartbeat(agentId, request.hostName(), request.lastError());
        return ApiResponse.ok();
    }
}
