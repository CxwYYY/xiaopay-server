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

/**
 * 采集 Agent 管理服务。
 *
 * <p>负责 Agent 凭据、绑定通道、在线状态和心跳状态维护。</p>
 */
@Service
@RequiredArgsConstructor
public class AgentService {
    private final XpAgentMapper agentMapper;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;
    private final AuditLogService auditLogService;

    /**
     * 创建采集 Agent，并返回首次可见的 agentSecret。
     */
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

    /**
     * 按 agentId 查询 Agent，不存在时抛出业务异常。
     */
    public XpAgent getByAgentId(String agentId) {
        XpAgent agent = agentMapper.selectOne(new LambdaQueryWrapper<XpAgent>().eq(XpAgent::getAgentId, agentId));
        if (agent == null) {
            throw new BusinessException(404, "agent not found");
        }
        return agent;
    }

    /**
     * 更新 Agent 心跳、主机名和健康状态。
     */
    public void heartbeat(String agentId, String hostName, String lastError) {
        XpAgent agent = getByAgentId(agentId);
        agent.setHostName(hostName);
        agent.setLastError(lastError);
        agent.setLastHeartbeatAt(timeProvider.now());
        agent.setStatus(lastError == null || lastError.isBlank() ? "ONLINE" : "DEGRADED");
        agent.setUpdatedAt(timeProvider.now());
        agentMapper.updateById(agent);
    }

    /**
     * 查询 Agent 列表，默认隐藏 agentSecret。
     */
    public List<AgentResponse> list() {
        return agentMapper.selectList(null).stream().map(agent -> toResponse(agent, false)).toList();
    }

    /**
     * 查询 Agent 详情。
     */
    public AgentResponse detail(String agentId) {
        return toResponse(getByAgentId(agentId), false);
    }

    /**
     * 更新 Agent 名称、绑定通道和运行主机信息。
     */
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

    /**
     * 设置 Agent 状态，后台删除动作也通过停用实现。
     */
    public AgentResponse setStatus(String agentId, String status) {
        XpAgent agent = getByAgentId(agentId);
        String before = JSONUtil.toJsonStr(agent);
        agent.setStatus(status);
        agent.setUpdatedAt(timeProvider.now());
        agentMapper.updateById(agent);
        auditLogService.record("UPDATE_AGENT_STATUS", "AGENT", agentId, before, JSONUtil.toJsonStr(agent));
        return toResponse(agent, false);
    }

    /**
     * 重置 Agent 签名密钥，并只在本次响应中返回新密钥。
     */
    public AgentResponse resetSecret(String agentId) {
        XpAgent agent = getByAgentId(agentId);
        String before = JSONUtil.toJsonStr(agent);
        agent.setAgentSecret(idGenerator.secret());
        agent.setUpdatedAt(timeProvider.now());
        agentMapper.updateById(agent);
        auditLogService.record("RESET_AGENT_SECRET", "AGENT", agentId, before, null);
        return toResponse(agent, true);
    }

    /**
     * 转换为接口响应对象，可按场景控制是否包含 agentSecret。
     */
    public AgentResponse toResponse(XpAgent agent, boolean includeSecret) {
        return new AgentResponse(agent.getAgentId(), includeSecret ? agent.getAgentSecret() : null,
                agent.getAgentName(), agent.getChannelId(), agent.getWechatAccount(),
                agent.getHostName(), agent.getStatus());
    }
}
