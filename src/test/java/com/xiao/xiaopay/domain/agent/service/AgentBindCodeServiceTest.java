package com.xiao.xiaopay.domain.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.agent.dto.AgentBindCodeResponse;
import com.xiao.xiaopay.domain.agent.dto.ClaimAgentBindCodeRequest;
import com.xiao.xiaopay.domain.agent.dto.ClaimAgentBindCodeResponse;
import com.xiao.xiaopay.domain.agent.dto.CreateAgentBindCodeRequest;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.agent.entity.XpAgentBindCode;
import com.xiao.xiaopay.domain.agent.mapper.XpAgentBindCodeMapper;
import com.xiao.xiaopay.domain.agent.mapper.XpAgentMapper;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentBindCodeServiceTest {
    private XpAgentBindCodeMapper bindCodeMapper;
    private XpAgentMapper agentMapper;
    private IdGenerator idGenerator;
    private AgentBindCodeService service;
    private final LocalDateTime now = LocalDateTime.of(2026, 5, 12, 10, 0);

    @BeforeEach
    void setUp() {
        bindCodeMapper = mock(XpAgentBindCodeMapper.class);
        agentMapper = mock(XpAgentMapper.class);
        idGenerator = mock(IdGenerator.class);
        service = new AgentBindCodeService(bindCodeMapper, agentMapper, idGenerator,
                new TimeProvider(Clock.fixed(Instant.parse("2026-05-12T02:00:00Z"), ZoneId.of("Asia/Shanghai"))),
                mock(AuditLogService.class));
    }

    @Test
    void createsPendingBindCodeWithDefaultExpiry() {
        when(idGenerator.bindCode()).thenReturn("AB12CD34");

        AgentBindCodeResponse response = service.create(new CreateAgentBindCodeRequest(
                "收银台电脑", 9L, "wxid-test", "cashier-pc", null));

        assertThat(response.bindCode()).isEqualTo("AB12CD34");
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.expiresAt()).isEqualTo(now.plusMinutes(10));
        verify(bindCodeMapper).insert(any(XpAgentBindCode.class));
    }

    @Test
    void claimCreatesAgentAndMarksBindCodeClaimed() {
        XpAgentBindCode code = pendingCode();
        when(bindCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(code);
        when(idGenerator.agentId()).thenReturn("AGT123");
        when(idGenerator.secret()).thenReturn("secret-123");

        ClaimAgentBindCodeResponse response = service.claim(new ClaimAgentBindCodeRequest(
                "AB12CD34", "cashier-runtime"));

        assertThat(response.agentId()).isEqualTo("AGT123");
        assertThat(response.agentSecret()).isEqualTo("secret-123");
        assertThat(code.getStatus()).isEqualTo("CLAIMED");
        assertThat(code.getClaimedAgentId()).isEqualTo("AGT123");
        assertThat(code.getClaimedAt()).isEqualTo(now);
        verify(agentMapper).insert(any(XpAgent.class));
        verify(bindCodeMapper).updateById(code);
    }

    @Test
    void claimRejectsAlreadyClaimedBindCode() {
        XpAgentBindCode code = pendingCode();
        code.setStatus("CLAIMED");
        when(bindCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(code);

        assertThatThrownBy(() -> service.claim(new ClaimAgentBindCodeRequest("AB12CD34", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("bind code is not pending");
    }

    @Test
    void claimRejectsExpiredBindCodeAndPersistsExpiredStatus() {
        XpAgentBindCode code = pendingCode();
        code.setExpiresAt(now.minusSeconds(1));
        when(bindCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(code);

        assertThatThrownBy(() -> service.claim(new ClaimAgentBindCodeRequest("AB12CD34", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("bind code expired");

        assertThat(code.getStatus()).isEqualTo("EXPIRED");
        verify(bindCodeMapper).updateById(code);
    }

    private XpAgentBindCode pendingCode() {
        XpAgentBindCode code = new XpAgentBindCode();
        code.setId(1L);
        code.setBindCode("AB12CD34");
        code.setAgentName("收银台电脑");
        code.setChannelId(9L);
        code.setWechatAccount("wxid-test");
        code.setHostName("cashier-pc");
        code.setStatus("PENDING");
        code.setExpiresAt(now.plusMinutes(10));
        code.setCreatedAt(now);
        code.setUpdatedAt(now);
        return code;
    }
}
