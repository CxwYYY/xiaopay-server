package com.xiao.xiaopay.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.agent.mapper.XpAgentMapper;
import com.xiao.xiaopay.domain.app.entity.XpApp;
import com.xiao.xiaopay.domain.app.mapper.XpAppMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 带签名请求体验签器。
 *
 * <p>先根据 appId/agentId 取密钥，再校验时间窗口、nonce 防重放和 HMAC 签名。</p>
 */
@Service
@RequiredArgsConstructor
public class SignedBodyVerifier {
    private final SignatureService signatureService;
    private final SignedRequestNonceService nonceService;
    private final SecurityProperties securityProperties;
    private final XpAppMapper appMapper;
    private final XpAgentMapper agentMapper;

    /**
     * 验证业务应用请求，并返回有效的应用实体。
     */
    public XpApp verifyApp(String appId, String timestamp, String nonce, String signature, String body) {
        XpApp app = appMapper.selectOne(new LambdaQueryWrapper<XpApp>().eq(XpApp::getAppId, appId));
        if (app == null) {
            throw new BusinessException(401, "invalid app");
        }
        verifyTimestamp(timestamp);
        nonceService.checkAndRemember(appId, nonce);
        if (!signatureService.verify(app.getAppSecret(), timestamp, nonce, body, signature)) {
            throw new BusinessException(401, "invalid signature");
        }
        return app;
    }

    /**
     * 验证采集 Agent 请求，并返回有效的 Agent 实体。
     */
    public XpAgent verifyAgent(String agentId, String timestamp, String nonce, String signature, String body) {
        XpAgent agent = agentMapper.selectOne(new LambdaQueryWrapper<XpAgent>().eq(XpAgent::getAgentId, agentId));
        if (agent == null) {
            throw new BusinessException(401, "invalid agent");
        }
        verifyTimestamp(timestamp);
        nonceService.checkAndRemember(agentId, nonce);
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
            if (skewMs > securityProperties.getSignatureWindowSeconds() * 1000) {
                throw new BusinessException(402, "timestamp expired");
            }
        } catch (NumberFormatException ex) {
            throw new BusinessException(402, "invalid timestamp");
        }
    }
}
