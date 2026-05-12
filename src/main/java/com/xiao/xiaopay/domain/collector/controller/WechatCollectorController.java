package com.xiao.xiaopay.domain.collector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.common.security.SignedBodyVerifier;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.collector.dto.WechatMessagePushRequest;
import com.xiao.xiaopay.domain.collector.dto.WechatMessagePushResponse;
import com.xiao.xiaopay.domain.collector.service.WechatCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collector")
public class WechatCollectorController {
    private final SignedBodyVerifier signedBodyVerifier;
    private final WechatCollectorService collectorService;
    private final ObjectMapper objectMapper;

    @PostMapping("/wechat/messages")
    public ApiResponse<WechatMessagePushResponse> receive(@RequestHeader("X-XiaoPay-Agent") String agentId,
                                                          @RequestHeader("X-XiaoPay-Timestamp") String timestamp,
                                                          @RequestHeader("X-XiaoPay-Nonce") String nonce,
                                                          @RequestHeader("X-XiaoPay-Signature") String signature,
                                                          @RequestBody String body) throws Exception {
        XpAgent agent = signedBodyVerifier.verifyAgent(agentId, timestamp, nonce, signature, body);
        WechatMessagePushRequest request = objectMapper.readValue(body, WechatMessagePushRequest.class);
        return ApiResponse.ok(collectorService.receive(agent, request));
    }
}
