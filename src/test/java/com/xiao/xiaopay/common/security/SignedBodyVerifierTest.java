package com.xiao.xiaopay.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.agent.mapper.XpAgentMapper;
import com.xiao.xiaopay.domain.app.mapper.XpAppMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignedBodyVerifierTest {
    private XpAgentMapper agentMapper;
    private SignedBodyVerifier verifier;

    @BeforeEach
    void setUp() {
        agentMapper = mock(XpAgentMapper.class);
        SignatureService signatureService = mock(SignatureService.class);
        when(signatureService.verify(any(), any(), any(), any(), any())).thenReturn(true);
        SecurityProperties properties = new SecurityProperties();
        properties.setSignatureWindowSeconds(300);
        SignedRequestNonceService nonceService = mock(SignedRequestNonceService.class);
        verifier = new SignedBodyVerifier(signatureService, nonceService, properties,
                mock(XpAppMapper.class), agentMapper);
    }

    @Test
    void rejectsDisabledAgentCredentialsEvenWhenSignatureMatches() {
        XpAgent agent = new XpAgent();
        agent.setAgentId("AGT123");
        agent.setAgentSecret("secret");
        agent.setStatus("DISABLED");
        when(agentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(agent);

        assertThatThrownBy(() -> verifier.verifyAgent("AGT123", String.valueOf(System.currentTimeMillis()),
                "nonce-1", "signature", "{}"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("agent disabled");
    }
}
