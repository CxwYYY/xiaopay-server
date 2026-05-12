package com.xiao.xiaopay.domain.agent.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiao.xiaopay.common.error.BusinessException;
import com.xiao.xiaopay.common.id.IdGenerator;
import com.xiao.xiaopay.common.time.TimeProvider;
import com.xiao.xiaopay.domain.agent.dto.AgentResponse;
import com.xiao.xiaopay.domain.agent.dto.CreateAgentRequest;
import com.xiao.xiaopay.domain.agent.dto.UpdateAgentRequest;
import com.xiao.xiaopay.domain.agent.entity.XpAgent;
import com.xiao.xiaopay.domain.agent.mapper.XpAgentMapper;
import com.xiao.xiaopay.domain.audit.service.AuditLogService;
import com.xiao.xiaopay.domain.common.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final XpAgentMapper agentMapper;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final AuditLogService auditLogService;

    public AgentResponse create(CreateAgentRequest request) {
        LocalDateTime now = timeProvider.now();
        XpAgent agent = new XpAgent();
        agent.setAgentId(idGenerator.agentId());
        agent.setAgentSecret(idGenerator.secret());
        agent.setAgentName(request.agentName());
        agent.setChannelId(request.channelId());
        agent.setWechatAccount(request.wechatAccount());
        agent.setHostName(request.hostName());
        agent.setStatus("OFFLINE");
        agent.setCreatedAt(now);
        agent.setUpdatedAt(now);
        agentMapper.insert(agent);
        auditLogService.record("CREATE_AGENT", "AGENT", agent.getAgentId(), null, JSONUtil.toJsonStr(agent));
        return toResponse(agent, true);
    }

    public XpAgent getByAgentId(String agentId) {
        XpAgent agent = agentMapper.selectOne(new LambdaQueryWrapper<XpAgent>().eq(XpAgent::getAgentId, agentId));
        if (agent == null) {
            throw new BusinessException(404, "agent not found");
        }
        return agent;
    }

    public void heartbeat(String agentId, String hostName, String lastError) {
        XpAgent agent = getByAgentId(agentId);
        agent.setHostName(hostName);
        agent.setLastError(lastError);
        agent.setLastHeartbeatAt(timeProvider.now());
        agent.setStatus(lastError == null || lastError.isBlank() ? "ONLINE" : "DEGRADED");
        agent.setUpdatedAt(timeProvider.now());
        agentMapper.updateById(agent);
    }

    public List<AgentResponse> list() {
        return agentMapper.selectList(null).stream().map(agent -> toResponse(agent, false)).toList();
    }

    public AgentResponse detail(String agentId) {
        return toResponse(getByAgentId(agentId), false);
    }

    public AgentResponse update(String agentId, UpdateAgentRequest request) {
        XpAgent agent = getByAgentId(agentId);
        String before = JSONUtil.toJsonStr(agent);
        if (request.agentName() != null && !request.agentName().isBlank()) {
            agent.setAgentName(request.agentName());
        }
        if (request.channelId() != null) {
            agent.setChannelId(request.channelId());
        }
        agent.setWechatAccount(request.wechatAccount());
        agent.setHostName(request.hostName());
        agent.setUpdatedAt(timeProvider.now());
        agentMapper.updateById(agent);
        auditLogService.record("UPDATE_AGENT", "AGENT", agentId, before, JSONUtil.toJsonStr(agent));
        return toResponse(agent, false);
    }

    public AgentResponse setStatus(String agentId, String status) {
        XpAgent agent = getByAgentId(agentId);
        String before = JSONUtil.toJsonStr(agent);
        agent.setStatus(status);
        agent.setUpdatedAt(timeProvider.now());
        agentMapper.updateById(agent);
        auditLogService.record("UPDATE_AGENT_STATUS", "AGENT", agentId, before, JSONUtil.toJsonStr(agent));
        return toResponse(agent, false);
    }

    public AgentResponse resetSecret(String agentId) {
        XpAgent agent = getByAgentId(agentId);
        String before = JSONUtil.toJsonStr(agent);
        agent.setAgentSecret(idGenerator.secret());
        agent.setUpdatedAt(timeProvider.now());
        agentMapper.updateById(agent);
        auditLogService.record("RESET_AGENT_SECRET", "AGENT", agentId, before, null);
        return toResponse(agent, true);
    }

    public AgentResponse toResponse(XpAgent agent, boolean includeSecret) {
        return new AgentResponse(agent.getAgentId(), includeSecret ? agent.getAgentSecret() : null,
                agent.getAgentName(), agent.getChannelId(), agent.getWechatAccount(),
                agent.getHostName(), agent.getStatus());
    }
}
