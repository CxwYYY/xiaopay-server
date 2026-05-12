package com.xiao.xiaopay.domain.agent.service;

import cn.hutool.json.JSONUtil;
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
import com.xiao.xiaopay.domain.agent.model.AgentBindCodeStatus;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Creates and claims one-time binding codes for xiaopay-agent.
 */
@Service
@RequiredArgsConstructor
public class AgentBindCodeService {
    private static final int DEFAULT_EXPIRE_MINUTES = 10;

    private final XpAgentBindCodeMapper bindCodeMapper;
    private final XpAgentMapper agentMapper;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final AuditLogService auditLogService;

    /**
     * Create a pending one-time binding code for the admin console.
     */
    public AgentBindCodeResponse create(CreateAgentBindCodeRequest request) {
        LocalDateTime now = timeProvider.now();
        int expireMinutes = request.expireMinutes() == null ? DEFAULT_EXPIRE_MINUTES : request.expireMinutes();
        XpAgentBindCode code = new XpAgentBindCode();
        code.setBindCode(idGenerator.bindCode());
        code.setAgentName(request.agentName());
        code.setChannelId(request.channelId());
        code.setWechatAccount(request.wechatAccount());
        code.setHostName(request.hostName());
        code.setStatus(AgentBindCodeStatus.PENDING);
        code.setExpiresAt(now.plusMinutes(expireMinutes));
        code.setCreatedAt(now);
        code.setUpdatedAt(now);
        bindCodeMapper.insert(code);
        auditLogService.record("CREATE_AGENT_BIND_CODE", "AGENT_BIND_CODE",
                code.getBindCode(), null, JSONUtil.toJsonStr(code));
        return toResponse(code);
    }

    /**
     * Claim a binding code once and return the newly created agent credentials.
     */
    @Transactional
    public ClaimAgentBindCodeResponse claim(ClaimAgentBindCodeRequest request) {
        XpAgentBindCode code = getByBindCode(request.bindCode());
        LocalDateTime now = timeProvider.now();
        if (!AgentBindCodeStatus.PENDING.equals(code.getStatus())) {
            throw new BusinessException(409, "bind code is not pending");
        }
        if (!code.getExpiresAt().isAfter(now)) {
            code.setStatus(AgentBindCodeStatus.EXPIRED);
            code.setUpdatedAt(now);
            bindCodeMapper.updateById(code);
            throw new BusinessException(410, "bind code expired");
        }

        XpAgent agent = new XpAgent();
        agent.setAgentId(idGenerator.agentId());
        agent.setAgentSecret(idGenerator.secret());
        agent.setAgentName(code.getAgentName());
        agent.setChannelId(code.getChannelId());
        agent.setWechatAccount(code.getWechatAccount());
        agent.setHostName(firstNonBlank(request.hostName(), code.getHostName()));
        agent.setStatus("OFFLINE");
        agent.setCreatedAt(now);
        agent.setUpdatedAt(now);
        agentMapper.insert(agent);

        code.setStatus(AgentBindCodeStatus.CLAIMED);
        code.setClaimedAgentId(agent.getAgentId());
        code.setClaimedAt(now);
        code.setUpdatedAt(now);
        bindCodeMapper.updateById(code);
        auditLogService.record("CLAIM_AGENT_BIND_CODE", "AGENT_BIND_CODE",
                code.getBindCode(), null, JSONUtil.toJsonStr(code));
        return new ClaimAgentBindCodeResponse(agent.getAgentId(), agent.getAgentSecret());
    }

    private XpAgentBindCode getByBindCode(String bindCode) {
        String normalized = normalizeBindCode(bindCode);
        XpAgentBindCode code = bindCodeMapper.selectOne(new LambdaQueryWrapper<XpAgentBindCode>()
                .eq(XpAgentBindCode::getBindCode, normalized));
        if (code == null) {
            throw new BusinessException(404, "bind code not found");
        }
        return code;
    }

    private AgentBindCodeResponse toResponse(XpAgentBindCode code) {
        return new AgentBindCodeResponse(code.getBindCode(), code.getExpiresAt(), code.getStatus());
    }

    private String normalizeBindCode(String bindCode) {
        return bindCode == null ? "" : bindCode.trim().toUpperCase();
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
