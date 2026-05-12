package com.xiao.xiaopay.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.agent.mapper.XpAgentMapper;
import com.xiao.xiaopay.domain.app.entity.XpApp;
import com.xiao.xiaopay.domain.app.mapper.XpAppMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignedBodyVerifier {
    private final SignatureService signatureService;
    private final XpAppMapper appMapper;
    private final XpAgentMapper agentMapper;

    @Value("${xiaopay.security.signature-window-seconds:300}")
    private long signatureWindowSeconds;

    public XpApp verifyApp(String appId, String timestamp, String nonce, String signature, String body) {
        XpApp app = appMapper.selectOne(new LambdaQueryWrapper<XpApp>().eq(XpApp::getAppId, appId));
        if (app == null) {
            throw new BusinessException(401, "invalid app");
        }
        verifyTimestamp(timestamp);
        if (!signatureService.verify(app.getAppSecret(), timestamp, nonce, body, signature)) {
            throw new BusinessException(401, "invalid signature");
        }
        return app;
    }

    public XpAgent verifyAgent(String agentId, String timestamp, String nonce, String signature, String body) {
        XpAgent agent = agentMapper.selectOne(new LambdaQueryWrapper<XpAgent>().eq(XpAgent::getAgentId, agentId));
        if (agent == null) {
            throw new BusinessException(401, "invalid agent");
        }
        verifyTimestamp(timestamp);
        if (!signatureService.verify(agent.getAgentSecret(), timestamp, nonce, body, signature)) {
            throw new BusinessException(401, "invalid signature");
        }
        return agent;
    }

    private void verifyTimestamp(String timestamp) {
        try {
            long requestMs = Long.parseLong(timestamp);
            long nowMs = System.currentTimeMillis();
            long skewMs = Math.abs(nowMs - requestMs);
            if (skewMs > signatureWindowSeconds * 1000) {
                throw new BusinessException(402, "timestamp expired");
            }
        } catch (NumberFormatException ex) {
            throw new BusinessException(402, "invalid timestamp");
        }
    }
}
